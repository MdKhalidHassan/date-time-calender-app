package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.DateAlarm
import com.example.data.model.HolidayEntity
import com.example.data.model.RegularAlarm
import com.example.receiver.AlarmReceiver
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun scheduleRegularAlarm(alarm: RegularAlarm) {
        if (!alarm.isEnabled) return
        val triggerMillis = calculateNextTriggerForRegular(alarm) ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_REGULAR_ALARM
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmReceiver.EXTRA_ALARM_TITLE, alarm.label)
            putExtra(AlarmReceiver.EXTRA_ALARM_DESC, alarm.getRepeatDaysText(isBengali = false))
            putExtra(AlarmReceiver.EXTRA_ALARM_VIBRATE, alarm.vibrate)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getRegularAlarmRequestCode(alarm.id),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setExactAlarm(triggerMillis, pendingIntent)
    }

    fun cancelRegularAlarm(alarm: RegularAlarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_REGULAR_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getRegularAlarmRequestCode(alarm.id),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun scheduleDateAlarm(alarm: DateAlarm) {
        if (!alarm.isEnabled || alarm.isCompleted) return
        val now = System.currentTimeMillis()
        if (alarm.triggerTimeMillis <= now) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_DATE_ALARM
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmReceiver.EXTRA_ALARM_TITLE, alarm.title)
            putExtra(AlarmReceiver.EXTRA_ALARM_DESC, alarm.description)
            putExtra(AlarmReceiver.EXTRA_ALARM_VIBRATE, alarm.vibrate)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getDateAlarmRequestCode(alarm.id),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setExactAlarm(alarm.triggerTimeMillis, pendingIntent)
    }

    fun cancelDateAlarm(alarm: DateAlarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_DATE_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getDateAlarmRequestCode(alarm.id),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun setExactAlarm(triggerMillis: Long, pendingIntent: PendingIntent) {
        if (alarmManager == null) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    private fun calculateNextTriggerForRegular(alarm: RegularAlarm): Long? {
        val now = Calendar.getInstance()
        val candidate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If no repeat days specified, just next occurrence today or tomorrow
        if (alarm.repeatDaysMask == 0) {
            if (candidate.timeInMillis <= now.timeInMillis) {
                candidate.add(Calendar.DAY_OF_YEAR, 1)
            }
            return candidate.timeInMillis
        }

        // Check the next 7 days for the earliest active day
        for (i in 0..7) {
            val dayOfWeek = candidate.get(Calendar.DAY_OF_WEEK)
            if (alarm.isDayActive(dayOfWeek) && candidate.timeInMillis > now.timeInMillis) {
                return candidate.timeInMillis
            }
            candidate.add(Calendar.DAY_OF_YEAR, 1)
        }
        return null
    }

    fun scheduleHolidayReminders(
        holidays: List<HolidayEntity>,
        daysBefore: Int,
        hour: Int,
        minute: Int
    ) {
        holidays.forEach { holiday ->
            if (holiday.reminderEnabled) {
                scheduleHolidayReminder(holiday, daysBefore, hour, minute)
            } else {
                cancelHolidayReminder(holiday)
            }
        }
    }

    fun cancelHolidayReminders(holidays: List<HolidayEntity>) {
        holidays.forEach { cancelHolidayReminder(it) }
    }

    fun scheduleHolidayReminder(
        holiday: HolidayEntity,
        daysBefore: Int,
        hour: Int,
        minute: Int
    ) {
        if (!holiday.reminderEnabled) {
            cancelHolidayReminder(holiday)
            return
        }
        val triggerMillis = calculateHolidayTriggerMillis(holiday, daysBefore, hour, minute)
        if (triggerMillis <= System.currentTimeMillis()) {
            cancelHolidayReminder(holiday)
            return
        }

        val whenLabel = when (daysBefore) {
            0 -> "Today (${holiday.dateIso})"
            1 -> "Tomorrow (${holiday.dateIso})"
            else -> "In $daysBefore days (${holiday.dateIso})"
        }

        val displayTitle = if (holiday.localName != holiday.name && holiday.localName.isNotBlank()) {
            "${holiday.localName} (${holiday.name})"
        } else {
            holiday.name
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_HOLIDAY_REMINDER
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, getHolidayRequestCode(holiday.id).toLong())
            putExtra(AlarmReceiver.EXTRA_ALARM_TITLE, "Upcoming Holiday: $displayTitle")
            putExtra(AlarmReceiver.EXTRA_ALARM_DESC, "${holiday.countryName} • $whenLabel")
            putExtra(AlarmReceiver.EXTRA_ALARM_VIBRATE, true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getHolidayRequestCode(holiday.id),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setExactAlarm(triggerMillis, pendingIntent)
    }

    fun cancelHolidayReminder(holiday: HolidayEntity) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_HOLIDAY_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getHolidayRequestCode(holiday.id),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun getRegularAlarmRequestCode(id: Long): Int = (100000 + (id % 800000)).toInt()
    private fun getDateAlarmRequestCode(id: Long): Int = (900000 + (id % 800000)).toInt()
    private fun getHolidayRequestCode(id: String): Int = 2000000 + (id.hashCode() and 0x000FFFFF)

    companion object {
        fun calculateHolidayTriggerMillis(
            holiday: HolidayEntity,
            daysBefore: Int,
            hour: Int,
            minute: Int
        ): Long {
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, holiday.year)
                set(Calendar.MONTH, holiday.month)
                set(Calendar.DAY_OF_MONTH, holiday.day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, -daysBefore.coerceAtLeast(0))
            }
            return cal.timeInMillis
        }
    }
}
