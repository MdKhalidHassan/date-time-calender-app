package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TimeData
import com.example.model.TimeZoneData
import com.example.util.DateFormatter

@Composable
fun ClockCard(
    timeData: TimeData,
    is24Hour: Boolean,
    isBengali: Boolean,
    onToggle24Hour: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenTimeZoneSelector: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hourToDisplay = if (is24Hour) timeData.hour24 else timeData.hour12
    val hourStr = DateFormatter.formatNumber(hourToDisplay, isBengali, minDigits = 2)
    val minuteStr = DateFormatter.formatNumber(timeData.minute, isBengali, minDigits = 2)
    val secondStr = DateFormatter.formatNumber(timeData.second, isBengali, minDigits = 2)

    val dayOfWeekName = DateFormatter.getDayOfWeekName(timeData.dayOfWeek, isBengali)
    val monthName = DateFormatter.getMonthName(timeData.month, isBengali)
    val dayOfMonthStr = DateFormatter.formatNumber(timeData.dayOfMonth, isBengali)
    val yearStr = DateFormatter.formatNumber(timeData.year, isBengali)

    val dateFormatted = "$dayOfWeekName, $dayOfMonthStr $monthName $yearStr"

    // Timezone readable label
    val tzCity = timeData.timeZoneId.substringAfterLast('/').replace('_', ' ')
    val tzDisplay = if (tzCity.isNotEmpty()) "${timeData.timeZoneOffsetStr} ($tzCity)" else timeData.timeZoneOffsetStr

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("clock_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Timezone Selector & Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Timezone & GMT Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    modifier = Modifier
                        .clickable(onClick = onOpenTimeZoneSelector)
                        .testTag("timezone_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Select Time Zone",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tzDisplay,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Toggles Row: 12h/24h and Bengali/English
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // 12/24 Hour format toggle chip
                    FilterChip(
                        selected = is24Hour,
                        onClick = onToggle24Hour,
                        label = {
                            Text(
                                text = if (is24Hour) "24h" else "12h",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Toggle 12/24 hour format",
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("toggle_hour_format")
                    )

                    // Language toggle chip
                    FilterChip(
                        selected = isBengali,
                        onClick = onToggleLanguage,
                        label = {
                            Text(
                                text = if (isBengali) "বাংলা" else "ENG",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Toggle Bengali and English language",
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier.testTag("toggle_language")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Clock display: Digital clock + Analog Clock side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Digital Clock Column
                Column(modifier = Modifier.weight(1f)) {
                    // Digital Time Numbers with Seconds & AM/PM
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // Hour
                        AnimatedContent(
                            targetState = hourStr,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "hour_anim"
                        ) { h ->
                            Text(
                                text = h,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.testTag("clock_hours")
                            )
                        }

                        Text(
                            text = ":",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 1.dp)
                        )

                        // Minute
                        AnimatedContent(
                            targetState = minuteStr,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "min_anim"
                        ) { m ->
                            Text(
                                text = m,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.testTag("clock_minutes")
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Stacked Seconds & Prominent AM/PM Badge
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            // Seconds
                            AnimatedContent(
                                targetState = secondStr,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "sec_anim"
                            ) { s ->
                                Text(
                                    text = ":$s",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.testTag("clock_seconds")
                                )
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            // AM / PM Badge: Ultra Prominent and Clear
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (timeData.isPm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.testTag("clock_ampm_badge")
                            ) {
                                Text(
                                    text = if (is24Hour) {
                                        if (isBengali) "২৪ ঘণ্টা" else "24-HR"
                                    } else {
                                        if (timeData.isPm) {
                                            if (isBengali) "অপরাহ্ন (PM)" else "PM"
                                        } else {
                                            if (isBengali) "পূর্বাহ্ন (AM)" else "AM"
                                        }
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date text
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                        modifier = Modifier.testTag("clock_date_text")
                    )

                    // Subtitle showing full time with AM/PM clearly
                    val amPmLabel = if (timeData.isPm) (if (isBengali) "অপরাহ্ন (PM)" else "PM") else (if (isBengali) "পূর্বাহ্ন (AM)" else "AM")
                    Text(
                        text = if (is24Hour) {
                            if (isBengali) "২৪ ঘণ্টার সময়: $hourStr:$minuteStr:$secondStr" else "24h Time: $hourStr:$minuteStr:$secondStr"
                        } else {
                            if (isBengali) "সময়: $hourStr:$minuteStr:$secondStr $amPmLabel" else "Time: $hourStr:$minuteStr:$secondStr $amPmLabel"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Mini Analog Clock
                AnalogClockView(
                    timeData = timeData,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .testTag("analog_clock_view")
                )
            }
        }
    }
}
