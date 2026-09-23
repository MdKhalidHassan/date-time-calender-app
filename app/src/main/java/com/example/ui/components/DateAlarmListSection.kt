package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DateAlarm
import com.example.util.DateFormatter

@Composable
fun DateAlarmListSection(
    alarms: List<DateAlarm>,
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    is24Hour: Boolean,
    isBengali: Boolean,
    onToggleAlarm: (DateAlarm, Boolean) -> Unit,
    onDeleteAlarm: (DateAlarm) -> Unit,
    onAddNewDateAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthName = DateFormatter.getMonthName(selectedMonth, isBengali)
    val dayStr = DateFormatter.formatNumber(selectedDay, isBengali)
    val yearStr = DateFormatter.formatNumber(selectedYear, isBengali)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("date_alarms_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Title & Target Date Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationImportant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBengali) "তারিখের অ্যালার্ম" else "Date Alarms",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isBengali) "$dayStr $monthName $yearStr-এর অ্যালার্ম ও রিমাইন্ডার" else "Alarms & Reminders for $dayStr $monthName $yearStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prominent Full-Width Horizontal Add Alarm Button Box
            Button(
                onClick = onAddNewDateAlarm,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("add_date_alarm_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBengali) "নতুন তারিখের অ্যালার্ম" else "Add Date Alarm",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (alarms.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isBengali)
                                "এই তারিখে কোনো নির্দিষ্ট অ্যালার্ম বা রিমাইন্ডার নেই।"
                            else
                                "No alarms scheduled for this date.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBengali)
                                "উপরে '+ নতুন তারিখের অ্যালার্ম' বাটনে চাপ দিয়ে যেকোনো সময়ে বাজবে এমন অ্যালার্ম ও রিমাইন্ডার যোগ করতে পারেন।"
                            else
                                "Tap 'Add Date Alarm' above to schedule an alarm with reminder notes.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    alarms.forEach { alarm ->
                        DateAlarmItem(
                            alarm = alarm,
                            is24Hour = is24Hour,
                            isBengali = isBengali,
                            onToggle = { isEnabled -> onToggleAlarm(alarm, isEnabled) },
                            onDelete = { onDeleteAlarm(alarm) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateAlarmItem(
    alarm: DateAlarm,
    is24Hour: Boolean,
    isBengali: Boolean,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val displayHour = if (is24Hour) {
        alarm.hour
    } else {
        val h = alarm.hour % 12
        if (h == 0) 12 else h
    }
    val hourStr = DateFormatter.formatNumber(displayHour, isBengali, minDigits = 2)
    val minStr = DateFormatter.formatNumber(alarm.minute, isBengali, minDigits = 2)
    val amPm = if (!is24Hour) {
        if (alarm.hour >= 12) (if (isBengali) "অপরাহ্ন" else "PM") else (if (isBengali) "পূর্বাহ্ন" else "AM")
    } else ""

    val dateStr = "${DateFormatter.formatNumber(alarm.day, isBengali)} ${DateFormatter.getMonthName(alarm.month, isBengali)} ${DateFormatter.formatNumber(alarm.year, isBengali)}"

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (alarm.isCompleted) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        } else if (alarm.isEnabled) {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Time & Status Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$hourStr:$minStr",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (alarm.isEnabled && !alarm.isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                    if (amPm.isNotEmpty()) {
                        Text(
                            text = amPm,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (alarm.isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isBengali) "বাজা সম্পন্ন" else "Completed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                Text(
                    text = alarm.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Description / Reminder notes
                if (alarm.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = alarm.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Date badge
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.EventNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!alarm.isCompleted) {
                    Switch(
                        checked = alarm.isEnabled,
                        onCheckedChange = onToggle,
                        modifier = Modifier.testTag("date_alarm_switch_${alarm.id}")
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_date_alarm_${alarm.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete alarm",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
