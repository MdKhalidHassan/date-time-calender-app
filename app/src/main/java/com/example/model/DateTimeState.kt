package com.example.model

import java.util.Calendar

data class TimeData(
    val timestamp: Long = System.currentTimeMillis(),
    val hour12: Int = 12,
    val hour24: Int = 0,
    val minute: Int = 0,
    val second: Int = 0,
    val millisecond: Int = 0,
    val isPm: Boolean = false,
    val timeZoneId: String = "",
    val timeZoneOffsetStr: String = "",
    val dayOfWeek: Int = Calendar.SUNDAY,
    val dayOfMonth: Int = 1,
    val month: Int = Calendar.JANUARY,
    val year: Int = 2026,
    val dayOfYear: Int = 1,
    val weekOfYear: Int = 1,
    val isLeapYear: Boolean = false
)

data class CalendarDay(
    val year: Int,
    val month: Int, // 0-based: 0 = Jan, 11 = Dec
    val day: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean
)

data class SelectedDateInfo(
    val year: Int,
    val month: Int,
    val day: Int,
    val dayOfWeek: Int,
    val dayOfYear: Int,
    val isLeapYear: Boolean,
    val daysFromToday: Long
)
