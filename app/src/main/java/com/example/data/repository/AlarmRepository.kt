package com.example.data.repository

import com.example.data.dao.AlarmDao
import com.example.data.model.DateAlarm
import com.example.data.model.RegularAlarm
import kotlinx.coroutines.flow.Flow

class AlarmRepository(private val alarmDao: AlarmDao) {

    // Regular Alarms
    val allRegularAlarms: Flow<List<RegularAlarm>> = alarmDao.getAllRegularAlarms()

    suspend fun getEnabledRegularAlarms(): List<RegularAlarm> = alarmDao.getEnabledRegularAlarms()

    suspend fun getRegularAlarmById(id: Long): RegularAlarm? = alarmDao.getRegularAlarmById(id)

    suspend fun insertRegularAlarm(alarm: RegularAlarm): Long = alarmDao.insertRegularAlarm(alarm)

    suspend fun updateRegularAlarm(alarm: RegularAlarm) = alarmDao.updateRegularAlarm(alarm)

    suspend fun deleteRegularAlarm(id: Long) = alarmDao.deleteRegularAlarmById(id)

    // Date Alarms
    val allDateAlarms: Flow<List<DateAlarm>> = alarmDao.getAllDateAlarms()

    fun getDateAlarmsForDay(year: Int, month: Int, day: Int): Flow<List<DateAlarm>> =
        alarmDao.getDateAlarmsForDay(year, month, day)

    suspend fun getActiveDateAlarms(): List<DateAlarm> = alarmDao.getActiveDateAlarms()

    suspend fun getDateAlarmById(id: Long): DateAlarm? = alarmDao.getDateAlarmById(id)

    suspend fun insertDateAlarm(alarm: DateAlarm): Long = alarmDao.insertDateAlarm(alarm)

    suspend fun updateDateAlarm(alarm: DateAlarm) = alarmDao.updateDateAlarm(alarm)

    suspend fun deleteDateAlarm(id: Long) = alarmDao.deleteDateAlarmById(id)
}
