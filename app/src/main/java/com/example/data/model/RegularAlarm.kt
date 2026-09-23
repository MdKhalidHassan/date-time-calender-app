package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "regular_alarms")
data class RegularAlarm(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int, // 0 - 23
    val minute: Int, // 0 - 59
    val label: String = "Alarm",
    val repeatDaysMask: Int = ALL_DAYS_MASK, // Default to all days or custom
    val isEnabled: Boolean = true,
    val vibrate: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isDayActive(dayOfWeek: Int): Boolean {
        // dayOfWeek is Calendar.SUNDAY (1) .. Calendar.SATURDAY (7)
        val bit = 1 shl (dayOfWeek - 1)
        return (repeatDaysMask and bit) != 0
    }

    fun hasAnyRepeatDay(): Boolean {
        return repeatDaysMask != 0
    }

    fun getRepeatDaysText(isBengali: Boolean): String {
        if (repeatDaysMask == ALL_DAYS_MASK) {
            return if (isBengali) "প্রতিদিন" else "Every day"
        }
        if (repeatDaysMask == WEEKDAYS_MASK) {
            return if (isBengali) "কর্মদিবস (সোম-শুক্র)" else "Weekdays"
        }
        if (repeatDaysMask == WEEKENDS_MASK) {
            return if (isBengali) "ছুটির দিন (শনি-রবি)" else "Weekends"
        }
        if (repeatDaysMask == 0) {
            return if (isBengali) "শুধুমাত্র একবার" else "Once"
        }

        val daysEn = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val daysBn = arrayOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি")

        val activeList = mutableListOf<String>()
        for (i in 0..6) {
            val bit = 1 shl i
            if ((repeatDaysMask and bit) != 0) {
                activeList.add(if (isBengali) daysBn[i] else daysEn[i])
            }
        }
        return activeList.joinToString(", ")
    }

    companion object {
        const val ALL_DAYS_MASK = (1 shl 7) - 1 // 127
        const val WEEKDAYS_MASK = (1 shl 1) or (1 shl 2) or (1 shl 3) or (1 shl 4) or (1 shl 5) // Mon-Fri
        const val WEEKENDS_MASK = (1 shl 0) or (1 shl 6) // Sun, Sat
    }
}
