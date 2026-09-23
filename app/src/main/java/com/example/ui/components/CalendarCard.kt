package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CalendarDay
import com.example.model.TimeData
import com.example.util.DateFormatter

@Composable
fun CalendarCard(
    displayedYear: Int,
    displayedMonth: Int,
    todayTime: TimeData,
    selectedDay: CalendarDay?,
    daysWithAlarms: Set<String> = emptySet(),
    daysWithNotes: Set<String> = emptySet(),
    isBengali: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onResetToToday: () -> Unit,
    onSelectDay: (CalendarDay) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthName = DateFormatter.getMonthName(displayedMonth, isBengali)
    val yearStr = DateFormatter.formatNumber(displayedYear, isBengali)
    val weekDays = DateFormatter.getShortWeekDays(isBengali)
    val calendarDays = DateFormatter.generateCalendarDays(
        displayedYear = displayedYear,
        displayedMonth = displayedMonth,
        todayTime = todayTime,
        selectedDay = selectedDay
    )

    val isViewingCurrentMonth = (displayedYear == todayTime.year && displayedMonth == todayTime.month)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("calendar_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Month Header & Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Month and Year Title
                AnimatedContent(
                    targetState = "$monthName $yearStr",
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "calendar_header_anim"
                ) { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("calendar_month_year_text")
                    )
                }

                // Navigation buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (!isViewingCurrentMonth) {
                        FilledTonalButton(
                            onClick = onResetToToday,
                            contentPadding = ButtonDefaults.TextButtonContentPadding,
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("today_quick_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = "Jump to Today",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = if (isBengali) "আজ" else "Today",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    IconButton(
                        onClick = onPreviousMonth,
                        modifier = Modifier.testTag("prev_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Previous Month",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.testTag("next_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Next Month",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Weekday Headers (7 columns)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEachIndexed { index, dayName ->
                    val isWeekend = (index == 0 || index == 6) // Sun / Sat
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isWeekend) {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Days Grid (multiples of 7)
            val chunkedWeeks = calendarDays.chunked(7)
            chunkedWeeks.forEach { week ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    week.forEach { dayItem ->
                        val dayKey = "${dayItem.year}-${dayItem.month}-${dayItem.day}"
                        val hasAlarm = daysWithAlarms.contains(dayKey)
                        val hasNote = daysWithNotes.contains(dayKey)

                        CalendarDayCell(
                            day = dayItem,
                            hasAlarm = hasAlarm,
                            hasNote = hasNote,
                            isBengali = isBengali,
                            onClick = { onSelectDay(dayItem) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    hasAlarm: Boolean,
    hasNote: Boolean,
    isBengali: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayNumberStr = DateFormatter.formatNumber(day.day, isBengali)

    // Cell styling state
    val backgroundColor = when {
        day.isToday -> MaterialTheme.colorScheme.primary
        day.isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }

    val contentColor = when {
        day.isToday -> MaterialTheme.colorScheme.onPrimary
        day.isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderModifier = if (day.isSelected && !day.isToday) {
        Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(borderModifier)
            .background(backgroundColor)
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .testTag("calendar_day_${day.year}_${day.month}_${day.day}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayNumberStr,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (day.isToday || day.isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor,
                textAlign = TextAlign.Center
            )

            // Indicators row (Today dot, Alarm dot, Note dot)
            Row(
                modifier = Modifier.padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (day.isToday) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary)
                    )
                }
                if (hasAlarm) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(
                                if (day.isToday) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.error
                            )
                    )
                }
                if (hasNote) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(
                                if (day.isToday) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.tertiary
                            )
                    )
                }
            }
        }
    }
}
