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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
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
import com.example.data.model.RegularAlarm
import com.example.util.DateFormatter

@Composable
fun RegularAlarmListSection(
    alarms: List<RegularAlarm>,
    is24Hour: Boolean,
    isBengali: Boolean,
    onToggleAlarm: (RegularAlarm, Boolean) -> Unit,
    onDeleteAlarm: (RegularAlarm) -> Unit,
    onAddNewAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("regular_alarms_card"),
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
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBengali) "রেগুলার অ্যালার্ম" else "Recurring Alarms",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isBengali) "সাপ্তাহিক ও নির্দিষ্ট দিনে পুনরাবৃত্তি" else "Repeat on selected weekdays",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prominent Full-Width Horizontal Add Alarm Button Box
            Button(
                onClick = onAddNewAlarm,
                shape = RoundedCornerShape(14.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("add_regular_alarm_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBengali) "নতুন রেগুলার অ্যালার্ম" else "Add Recurring Alarm",
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
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isBengali)
                                "কোনো রেগুলার অ্যালার্ম সেট করা নেই।"
                            else
                                "No recurring alarms scheduled.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBengali)
                                "উপরে 'নতুন অ্যালার্ম' বাটনে ট্যাপ করে যেকোনো দিন ও সময়ে অ্যালার্ম সেট করতে পারেন।"
                            else
                                "Tap 'Add Alarm' to schedule day-based recurring alarms.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    alarms.forEach { alarm ->
                        RegularAlarmItem(
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
private fun RegularAlarmItem(
    alarm: RegularAlarm,
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

    val repeatText = alarm.getRepeatDaysText(isBengali)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (alarm.isEnabled) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Time
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$hourStr:$minStr",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                    if (amPm.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = amPm,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (alarm.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Label
                Text(
                    text = alarm.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Repeat Days and vibration
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = repeatText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (alarm.vibrate) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.testTag("alarm_switch_${alarm.id}")
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_alarm_${alarm.id}")
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
