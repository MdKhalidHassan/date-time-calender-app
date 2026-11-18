package com.example.data.repository

import com.example.alarm.AlarmScheduler
import com.example.data.dao.HolidayDao
import com.example.data.model.HolidayEntity
import com.example.network.AvailableCountryDto
import com.example.network.HolidayApiClient
import com.example.network.HolidayApiService
import com.example.network.PublicHolidayDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

class HolidayRepository(
    private val holidayDao: HolidayDao,
    private val apiService: HolidayApiService = HolidayApiClient.service
) {

    fun observeHolidays(countryCode: String): Flow<List<HolidayEntity>> =
        holidayDao.getHolidaysForCountry(countryCode)

    suspend fun getEnabledHolidays(countryCode: String): List<HolidayEntity> =
        holidayDao.getEnabledHolidaysForCountry(countryCode)

    suspend fun getHolidaysOnce(countryCode: String): List<HolidayEntity> =
        holidayDao.getHolidaysForCountryOnce(countryCode)

    suspend fun toggleHolidayReminder(id: String, enabled: Boolean) {
        holidayDao.updateHolidayReminder(id, enabled)
    }

    suspend fun setAllReminders(countryCode: String, enabled: Boolean) {
        holidayDao.setAllRemindersForCountry(countryCode, enabled)
    }

    suspend fun fetchAvailableCountries(): Result<List<AvailableCountryDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAvailableCountries()
            if (response.isSuccessful) {
                val list = response.body().orEmpty().sortedBy { it.name }
                Result.success(list)
            } else {
                Result.failure(IllegalStateException("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncUpcomingHolidays(countryCode: String, countryName: String): Result<List<HolidayEntity>> =
        withContext(Dispatchers.IO) {
            try {
                val nextResponse = apiService.getNextPublicHolidays(countryCode)
                val dtos: List<PublicHolidayDto> = if (nextResponse.isSuccessful && !nextResponse.body().isNullOrEmpty()) {
                    nextResponse.body().orEmpty()
                } else {
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val thisYearResp = apiService.getPublicHolidaysForYear(currentYear, countryCode)
                    val nextYearResp = apiService.getPublicHolidaysForYear(currentYear + 1, countryCode)
                    if (!thisYearResp.isSuccessful && !nextYearResp.isSuccessful) {
                        val code = if (nextResponse.code() != 200) nextResponse.code() else thisYearResp.code()
                        return@withContext Result.failure(IllegalStateException("HTTP $code"))
                    }
                    (thisYearResp.body().orEmpty() + nextYearResp.body().orEmpty())
                }

                val entities = dtos.mapNotNull { dto ->
                    parseHolidayDto(dto, countryCode, countryName)
                }.distinctBy { it.id }

                holidayDao.replaceHolidaysPreservingToggles(countryCode, entities)
                Result.success(holidayDao.getHolidaysForCountryOnce(countryCode))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun syncAndReconcileReminders(
        countryCode: String,
        countryName: String,
        scheduler: AlarmScheduler,
        remindersEnabled: Boolean,
        daysBefore: Int,
        hour: Int,
        minute: Int
    ): Result<List<HolidayEntity>> {
        val previousHolidays = getHolidaysOnce(countryCode)
        val syncResult = syncUpcomingHolidays(countryCode, countryName)
        syncResult.onSuccess { updatedHolidays ->
            // Cancel any pending alarms for holidays that were cancelled or moved to a new date
            findRemovedHolidays(previousHolidays, updatedHolidays).forEach { removed ->
                scheduler.cancelHolidayReminder(removed)
            }
            if (remindersEnabled) {
                scheduler.scheduleHolidayReminders(
                    holidays = updatedHolidays,
                    daysBefore = daysBefore,
                    hour = hour,
                    minute = minute
                )
            }
        }
        return syncResult
    }

    companion object {
        const val STALE_SYNC_INTERVAL_MS: Long = 24L * 60L * 60L * 1000L

        fun isSyncStale(
            lastSyncMillis: Long,
            lastSyncCountry: String?,
            currentCountry: String,
            nowMillis: Long = System.currentTimeMillis()
        ): Boolean {
            if (currentCountry.isBlank()) return false
            if (!currentCountry.equals(lastSyncCountry, ignoreCase = true)) return true
            if (lastSyncMillis <= 0L) return true
            return (nowMillis - lastSyncMillis) >= STALE_SYNC_INTERVAL_MS
        }

        fun findRemovedHolidays(
            oldList: List<HolidayEntity>,
            newList: List<HolidayEntity>
        ): List<HolidayEntity> {
            val newIds = newList.map { it.id }.toSet()
            return oldList.filter { it.id !in newIds }
        }

        fun parseHolidayDto(
            dto: PublicHolidayDto,
            countryCode: String,
            countryName: String
        ): HolidayEntity? {
            val parts = dto.date.split("-")
            if (parts.size != 3) return null
            val year = parts[0].toIntOrNull() ?: return null
            val monthOneBased = parts[1].toIntOrNull() ?: return null
            val day = parts[2].toIntOrNull() ?: return null
            val monthZeroBased = (monthOneBased - 1).coerceIn(0, 11)
            val id = "${countryCode}_${dto.date}_${dto.name}"
            return HolidayEntity(
                id = id,
                countryCode = countryCode,
                countryName = countryName,
                dateIso = dto.date,
                year = year,
                month = monthZeroBased,
                day = day,
                name = dto.name,
                localName = dto.localName.ifBlank { dto.name },
                typesCsv = dto.types?.joinToString(",") ?: "Public",
                isGlobal = dto.global ?: true,
                reminderEnabled = true
            )
        }
    }
}
