package com.example.util

import com.example.model.CalendarDay
import com.example.model.SelectedDateInfo
import com.example.model.TimeData
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object DateFormatter {

    private val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun formatNumber(number: Int, isBengali: Boolean, minDigits: Int = 1): String {
        val formatted = if (minDigits > 1) {
            String.format(Locale.US, "%0${minDigits}d", number)
        } else {
            number.toString()
        }
        if (!isBengali) return formatted

        return formatted.map { char ->
            if (char in '0'..'9') {
                bengaliDigits[char - '0']
            } else {
                char
            }
        }.joinToString("")
    }

    fun formatNumber(number: Long, isBengali: Boolean): String {
        val formatted = number.toString()
        if (!isBengali) return formatted

        return formatted.map { char ->
            if (char in '0'..'9') {
                bengaliDigits[char - '0']
            } else {
                char
            }
        }.joinToString("")
    }

    private val monthNamesEn = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private val monthNamesBn = arrayOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    fun getMonthName(month: Int, isBengali: Boolean): String {
        val safeMonth = month.coerceIn(0, 11)
        return if (isBengali) monthNamesBn[safeMonth] else monthNamesEn[safeMonth]
    }

    private val dayNamesEn = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    private val dayNamesBn = arrayOf("রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার")

    private val dayShortNamesEn = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    private val dayShortNamesBn = arrayOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি")

    fun getDayOfWeekName(dayOfWeek: Int, isBengali: Boolean, short: Boolean = false): String {
        // Calendar.SUNDAY is 1, Calendar.SATURDAY is 7
        val index = (dayOfWeek - Calendar.SUNDAY).coerceIn(0, 6)
        return if (short) {
            if (isBengali) dayShortNamesBn[index] else dayShortNamesEn[index]
        } else {
            if (isBengali) dayNamesBn[index] else dayNamesEn[index]
        }
    }

    fun getShortWeekDays(isBengali: Boolean): List<String> {
        return (Calendar.SUNDAY..Calendar.SATURDAY).map { dayOfWeek ->
            getDayOfWeekName(dayOfWeek, isBengali, short = true)
        }
    }

    fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    fun getDaysInMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun extractTimeData(timeZone: TimeZone = TimeZone.getDefault()): TimeData {
        val calendar = Calendar.getInstance(timeZone)
        return extractTimeData(calendar)
    }

    fun extractTimeData(calendar: Calendar): TimeData {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val hour24 = calendar.get(Calendar.HOUR_OF_DAY)
        val hour12Raw = calendar.get(Calendar.HOUR)
        val hour12 = if (hour12Raw == 0) 12 else hour12Raw
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val millisecond = calendar.get(Calendar.MILLISECOND)
        val isPm = calendar.get(Calendar.AM_PM) == Calendar.PM
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)

        val tz = calendar.timeZone
        val offsetMillis = tz.getOffset(calendar.timeInMillis)
        val offsetHours = offsetMillis / (1000 * 60 * 60)
        val offsetMins = Math.abs(offsetMillis / (1000 * 60)) % 60
        val sign = if (offsetMillis >= 0) "+" else "-"
        val offsetStr = String.format(Locale.US, "GMT%s%02d:%02d", sign, Math.abs(offsetHours), offsetMins)

        return TimeData(
            timestamp = calendar.timeInMillis,
            hour12 = hour12,
            hour24 = hour24,
            minute = minute,
            second = second,
            millisecond = millisecond,
            isPm = isPm,
            timeZoneId = tz.id,
            timeZoneOffsetStr = offsetStr,
            dayOfWeek = dayOfWeek,
            dayOfMonth = dayOfMonth,
            month = month,
            year = year,
            dayOfYear = dayOfYear,
            weekOfYear = weekOfYear,
            isLeapYear = isLeapYear(year)
        )
    }

    fun generateCalendarDays(
        displayedYear: Int,
        displayedMonth: Int,
        todayTime: TimeData,
        selectedDay: CalendarDay?
    ): List<CalendarDay> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, displayedYear)
        cal.set(Calendar.MONTH, displayedMonth)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday
        val daysInCurrentMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Previous month days
        val prevCal = cal.clone() as Calendar
        prevCal.add(Calendar.MONTH, -1)
        val daysInPrevMonth = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val prevMonthYear = prevCal.get(Calendar.YEAR)
        val prevMonth = prevCal.get(Calendar.MONTH)

        // Leading days from previous month
        val leadingDaysCount = firstDayOfWeek - Calendar.SUNDAY
        val result = mutableListOf<CalendarDay>()

        for (i in (daysInPrevMonth - leadingDaysCount + 1)..daysInPrevMonth) {
            val isToday = (todayTime.year == prevMonthYear && todayTime.month == prevMonth && todayTime.dayOfMonth == i)
            val isSelected = selectedDay != null && selectedDay.year == prevMonthYear && selectedDay.month == prevMonth && selectedDay.day == i
            result.add(
                CalendarDay(
                    year = prevMonthYear,
                    month = prevMonth,
                    day = i,
                    isCurrentMonth = false,
                    isToday = isToday,
                    isSelected = isSelected
                )
            )
        }

        // Current month days
        for (i in 1..daysInCurrentMonth) {
            val isToday = (todayTime.year == displayedYear && todayTime.month == displayedMonth && todayTime.dayOfMonth == i)
            val isSelected = selectedDay != null && selectedDay.year == displayedYear && selectedDay.month == displayedMonth && selectedDay.day == i
            result.add(
                CalendarDay(
                    year = displayedYear,
                    month = displayedMonth,
                    day = i,
                    isCurrentMonth = true,
                    isToday = isToday,
                    isSelected = isSelected
                )
            )
        }

        // Trailing days to complete the grid (multiples of 7, usually 35 or 42)
        val nextCal = cal.clone() as Calendar
        nextCal.add(Calendar.MONTH, 1)
        val nextMonthYear = nextCal.get(Calendar.YEAR)
        val nextMonth = nextCal.get(Calendar.MONTH)

        val totalDays = if (result.size > 35) 42 else 35
        val trailingDaysCount = totalDays - result.size
        for (i in 1..trailingDaysCount) {
            val isToday = (todayTime.year == nextMonthYear && todayTime.month == nextMonth && todayTime.dayOfMonth == i)
            val isSelected = selectedDay != null && selectedDay.year == nextMonthYear && selectedDay.month == nextMonth && selectedDay.day == i
            result.add(
                CalendarDay(
                    year = nextMonthYear,
                    month = nextMonth,
                    day = i,
                    isCurrentMonth = false,
                    isToday = isToday,
                    isSelected = isSelected
                )
            )
        }

        return result
    }

    fun getSelectedDateInfo(day: CalendarDay, todayTime: TimeData): SelectedDateInfo {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, day.year)
        cal.set(Calendar.MONTH, day.month)
        cal.set(Calendar.DAY_OF_MONTH, day.day)

        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)

        val todayCal = Calendar.getInstance()
        todayCal.set(todayTime.year, todayTime.month, todayTime.dayOfMonth, 0, 0, 0)
        todayCal.set(Calendar.MILLISECOND, 0)

        val targetCal = Calendar.getInstance()
        targetCal.set(day.year, day.month, day.day, 0, 0, 0)
        targetCal.set(Calendar.MILLISECOND, 0)

        val diffMillis = targetCal.timeInMillis - todayCal.timeInMillis
        val diffDays = diffMillis / (1000 * 60 * 60 * 24)

        return SelectedDateInfo(
            year = day.year,
            month = day.month,
            day = day.day,
            dayOfWeek = dayOfWeek,
            dayOfYear = dayOfYear,
            isLeapYear = isLeapYear(day.year),
            daysFromToday = diffDays
        )
    }

    fun formatRelativeDays(daysFromToday: Long, isBengali: Boolean): String {
        return when {
            daysFromToday == 0L -> if (isBengali) "আজ" else "Today"
            daysFromToday == 1L -> if (isBengali) "আগামীকাল" else "Tomorrow"
            daysFromToday == -1L -> if (isBengali) "গতকাল" else "Yesterday"
            daysFromToday > 0L -> {
                val num = formatNumber(daysFromToday, isBengali)
                if (isBengali) "$num দিন পর" else "In $num days"
            }
            else -> {
                val num = formatNumber(Math.abs(daysFromToday), isBengali)
                if (isBengali) "$num দিন আগে" else "$num days ago"
            }
        }
    }
}
