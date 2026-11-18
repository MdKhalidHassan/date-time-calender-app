package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.HolidayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HolidayDao {

    @Query("SELECT * FROM govt_holidays WHERE countryCode = :countryCode ORDER BY year ASC, month ASC, day ASC")
    fun getHolidaysForCountry(countryCode: String): Flow<List<HolidayEntity>>

    @Query("SELECT * FROM govt_holidays WHERE countryCode = :countryCode ORDER BY year ASC, month ASC, day ASC")
    suspend fun getHolidaysForCountryOnce(countryCode: String): List<HolidayEntity>

    @Query("SELECT * FROM govt_holidays WHERE countryCode = :countryCode AND reminderEnabled = 1 ORDER BY year ASC, month ASC, day ASC")
    suspend fun getEnabledHolidaysForCountry(countryCode: String): List<HolidayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolidays(holidays: List<HolidayEntity>)

    @Query("DELETE FROM govt_holidays WHERE countryCode = :countryCode")
    suspend fun deleteHolidaysForCountry(countryCode: String)

    @Query("UPDATE govt_holidays SET reminderEnabled = :enabled WHERE id = :id")
    suspend fun updateHolidayReminder(id: String, enabled: Boolean)

    @Query("UPDATE govt_holidays SET reminderEnabled = :enabled WHERE countryCode = :countryCode")
    suspend fun setAllRemindersForCountry(countryCode: String, enabled: Boolean)

    @Transaction
    suspend fun replaceHolidaysPreservingToggles(countryCode: String, incoming: List<HolidayEntity>) {
        val existingStates = getHolidaysForCountryOnce(countryCode)
            .associate { it.id to it.reminderEnabled }
        val merged = incoming.map { holiday ->
            val savedToggle = existingStates[holiday.id]
            if (savedToggle != null) holiday.copy(reminderEnabled = savedToggle) else holiday
        }
        deleteHolidaysForCountry(countryCode)
        insertHolidays(merged)
    }
}
