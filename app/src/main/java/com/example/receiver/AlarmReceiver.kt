package com.example.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.DateTimeApplication
import com.example.R
import com.example.alarm.AlarmPlayer
import com.example.alarm.AlarmScheduler
import com.example.ui.AlarmAlertActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, 0L)
        val title = intent.getStringExtra(EXTRA_ALARM_TITLE) ?: "Alarm"
        val description = intent.getStringExtra(EXTRA_ALARM_DESC) ?: ""
        val vibrate = intent.getBooleanExtra(EXTRA_ALARM_VIBRATE, true)
        val isRegular = intent.action == ACTION_REGULAR_ALARM

        // Play alarm ringtone and vibrate
        AlarmPlayer.play(context, shouldVibrate = vibrate)

        // Dismiss action intent
        val dismissIntent = Intent(context, AlarmDismissReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Full screen / tap intent to open AlarmAlertActivity
        val fullScreenIntent = Intent(context, AlarmAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_TITLE, title)
            putExtra(EXTRA_ALARM_DESC, description)
            putExtra(EXTRA_IS_REGULAR, isRegular)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, DateTimeApplication.ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_icon)
            .setContentTitle(title)
            .setContentText(if (description.isNotEmpty()) description else "Alarm is ringing")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setOngoing(true)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss / বন্ধ করুন", dismissPendingIntent)
            .build()

        notificationManager.notify(alarmId.toInt(), notification)

        // Launch AlarmAlertActivity directly
        try {
            context.startActivity(fullScreenIntent)
        } catch (_: Exception) {}

        // Handle DB updates / Rescheduling in background
        val appContext = context.applicationContext as? DateTimeApplication ?: return
        CoroutineScope(Dispatchers.IO).launch {
            if (isRegular) {
                // If recurring alarm, reschedule for next matching day
                val alarm = appContext.alarmRepository.getRegularAlarmById(alarmId)
                if (alarm != null && alarm.isEnabled) {
                    AlarmScheduler(context).scheduleRegularAlarm(alarm)
                }
            } else {
                // Date alarm: mark isCompleted = true
                val dateAlarm = appContext.alarmRepository.getDateAlarmById(alarmId)
                if (dateAlarm != null) {
                    appContext.alarmRepository.updateDateAlarm(dateAlarm.copy(isCompleted = true))
                }
            }
        }
    }

    companion object {
        const val ACTION_REGULAR_ALARM = "com.example.action.REGULAR_ALARM"
        const val ACTION_DATE_ALARM = "com.example.action.DATE_ALARM"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_TITLE = "extra_alarm_title"
        const val EXTRA_ALARM_DESC = "extra_alarm_desc"
        const val EXTRA_ALARM_VIBRATE = "extra_alarm_vibrate"
        const val EXTRA_IS_REGULAR = "extra_is_regular"
    }
}
