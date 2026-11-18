package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.DateTimeApplication
import com.example.alarm.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val appContext = context.applicationContext as? DateTimeApplication ?: return
        val scheduler = AlarmScheduler(context)

        CoroutineScope(Dispatchers.IO).launch {
            // Reschedule regular alarms
            val regularAlarms = appContext.alarmRepository.getEnabledRegularAlarms()
            regularAlarms.forEach { alarm ->
                scheduler.scheduleRegularAlarm(alarm)
            }

            // Reschedule active date alarms
            val dateAlarms = appContext.alarmRepository.getActiveDateAlarms()
            dateAlarms.forEach { alarm ->
                scheduler.scheduleDateAlarm(alarm)
            }

            // Reschedule enabled government holiday reminders
            val prefs = context.getSharedPreferences("datetime_prefs", Context.MODE_PRIVATE)
            val holidayRemindersEnabled = prefs.getBoolean("pref_holiday_reminders_enabled", false)
            val countryCode = prefs.getString("pref_holiday_country_code", null)
            if (holidayRemindersEnabled && !countryCode.isNullOrBlank()) {
                val daysBefore = prefs.getInt("pref_holiday_remind_days_before", 1)
                val hour = prefs.getInt("pref_holiday_remind_hour", 9)
                val minute = prefs.getInt("pref_holiday_remind_minute", 0)
                val holidays = appContext.holidayRepository.getEnabledHolidays(countryCode)
                scheduler.scheduleHolidayReminders(holidays, daysBefore, hour, minute)
            }
        }
    }
}
