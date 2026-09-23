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
        }
    }
}
