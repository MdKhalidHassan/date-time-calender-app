package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DateAlarm
import com.example.data.model.RegularAlarm
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {

    // Regular Alarms
    @Query("SELECT * FROM regular_alarms ORDER BY hour ASC, minute ASC")
    fun getAllRegularAlarms(): Flow<List<RegularAlarm>>

    @Query("SELECT * FROM regular_alarms WHERE isEnabled = 1")
    suspend fun getEnabledRegularAlarms(): List<RegularAlarm>

    @Query("SELECT * FROM regular_alarms WHERE id = :id LIMIT 1")
    suspend fun getRegularAlarmById(id: Long): RegularAlarm?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegularAlarm(alarm: RegularAlarm): Long

    @Update
    suspend fun updateRegularAlarm(alarm: RegularAlarm)

    @Query("DELETE FROM regular_alarms WHERE id = :id")
    suspend fun deleteRegularAlarmById(id: Long)

    // Date Alarms
    @Query("SELECT * FROM date_alarms ORDER BY triggerTimeMillis ASC")
    fun getAllDateAlarms(): Flow<List<DateAlarm>>

    @Query("SELECT * FROM date_alarms WHERE year = :year AND month = :month AND day = :day ORDER BY hour ASC, minute ASC")
    fun getDateAlarmsForDay(year: Int, month: Int, day: Int): Flow<List<DateAlarm>>

    @Query("SELECT * FROM date_alarms WHERE isEnabled = 1 AND isCompleted = 0")
    suspend fun getActiveDateAlarms(): List<DateAlarm>

    @Query("SELECT * FROM date_alarms WHERE id = :id LIMIT 1")
    suspend fun getDateAlarmById(id: Long): DateAlarm?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDateAlarm(alarm: DateAlarm): Long

    @Update
    suspend fun updateDateAlarm(alarm: DateAlarm)

    @Query("DELETE FROM date_alarms WHERE id = :id")
    suspend fun deleteDateAlarmById(id: Long)
}
