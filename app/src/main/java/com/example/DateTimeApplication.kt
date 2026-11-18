package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.alarm.HolidaySyncJobService
import com.example.data.AppDatabase
import com.example.data.repository.AlarmRepository
import com.example.data.repository.HolidayRepository
import com.example.data.repository.NoteRepository

class DateTimeApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val alarmRepository: AlarmRepository by lazy { AlarmRepository(database.alarmDao()) }
    val noteRepository: NoteRepository by lazy { NoteRepository(database.noteDao()) }
    val holidayRepository: HolidayRepository by lazy { HolidayRepository(database.holidayDao()) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        HolidaySyncJobService.schedulePeriodicSync(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "DateTime Alarms & Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channels for Date & Time Alarms and Scheduled Reminders"
                enableVibration(true)
                setBypassDnd(true)
            }
            notificationManager.createNotificationChannel(channel)

            val holidayChannel = NotificationChannel(
                HOLIDAY_CHANNEL_ID,
                "Government Holiday Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Upcoming public and government holiday notifications for your selected country"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(holidayChannel)
        }
    }

    companion object {
        const val ALARM_CHANNEL_ID = "datetime_alarm_channel"
        const val HOLIDAY_CHANNEL_ID = "datetime_holiday_channel"
    }
}
