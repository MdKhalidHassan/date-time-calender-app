package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.AppDatabase
import com.example.data.repository.AlarmRepository
import com.example.data.repository.NoteRepository

class DateTimeApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val alarmRepository: AlarmRepository by lazy { AlarmRepository(database.alarmDao()) }
    val noteRepository: NoteRepository by lazy { NoteRepository(database.noteDao()) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "DateTime Alarms & Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channels for Date & Time Alarms and Scheduled Reminders"
                enableVibration(true)
                setBypassDnd(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val ALARM_CHANNEL_ID = "datetime_alarm_channel"
    }
}
