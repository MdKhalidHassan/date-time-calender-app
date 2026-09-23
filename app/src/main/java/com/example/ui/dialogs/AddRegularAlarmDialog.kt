package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.RegularAlarm
import com.example.util.DateFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddRegularAlarmDialog(
    isBengali: Boolean,
    onDismiss: () -> Unit,
    onSave: (hour: Int, minute: Int, label: String, repeatMask: Int, vibrate: Boolean) -> Unit
) {
    val initialCal = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = initialCal.get(Calendar.HOUR_OF_DAY),
        initialMinute = initialCal.get(Calendar.MINUTE),
        is24Hour = false
    )

    var label by remember { mutableStateOf("") }
    var repeatDaysMask by remember { mutableIntStateOf(RegularAlarm.ALL_DAYS_MASK) }
    var vibrate by remember { mutableStateOf(true) }
    var useCompactTimeInput by remember { mutableStateOf(true) }

    val daysBn = arrayOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি")
    val daysEn = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    val displayH = valFor12H(timePickerState.hour)
    val amPmStr = if (timePickerState.hour < 12) (if (isBengali) "পূর্বাহ্ন (AM)" else "AM") else (if (isBengali) "অপরাহ্ন (PM)" else "PM")
    val previewTimeStr = "${DateFormatter.formatNumber(displayH, isBengali, minDigits = 2)}:${DateFormatter.formatNumber(timePickerState.minute, isBengali, minDigits = 2)} $amPmStr"

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .imePadding(),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBengali) "নতুন রেগুলার অ্যালার্ম" else "New Recurring Alarm",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Time & Dial Toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isBengali) "বাজবে:" else "Alarm Time:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = previewTimeStr,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        FilterChip(
                            selected = !useCompactTimeInput,
                            onClick = { useCompactTimeInput = !useCompactTimeInput },
                            label = {
                                Text(
                                    text = if (useCompactTimeInput)
                                        (if (isBengali) "ঘড়ি ডায়াল" else "Dial")
                                    else
                                        (if (isBengali) "সহজ ইনপুট" else "Input"),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }

                // Time Picker
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    if (useCompactTimeInput) {
                        TimeInput(
                            state = timePickerState,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        TimePicker(
                            state = timePickerState,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Label TextField
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(if (isBengali) "লেবেল / বিবরণ" else "Alarm Label") },
                    placeholder = { Text(if (isBengali) "যেমন: সকালের অ্যালার্ম" else "e.g. Morning Routine") },
                    leadingIcon = {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Label, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alarm_label_input")
                )

                // Repeat Days Selection
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isBengali) "পুনরাবৃত্তি দিনসমূহ (Repeat Days):" else "Repeat Days:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PresetButton(
                            text = if (isBengali) "প্রতিদিন" else "Every day",
                            isSelected = repeatDaysMask == RegularAlarm.ALL_DAYS_MASK,
                            onClick = { repeatDaysMask = RegularAlarm.ALL_DAYS_MASK },
                            modifier = Modifier.weight(1f)
                        )
                        PresetButton(
                            text = if (isBengali) "সোম-শুক্র" else "Weekdays",
                            isSelected = repeatDaysMask == RegularAlarm.WEEKDAYS_MASK,
                            onClick = { repeatDaysMask = RegularAlarm.WEEKDAYS_MASK },
                            modifier = Modifier.weight(1f)
                        )
                        PresetButton(
                            text = if (isBengali) "শনি-রবি" else "Weekends",
                            isSelected = repeatDaysMask == RegularAlarm.WEEKENDS_MASK,
                            onClick = { repeatDaysMask = RegularAlarm.WEEKENDS_MASK },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Individual Day Toggle Chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 0..6) {
                            val bit = 1 shl i
                            val isSelected = (repeatDaysMask and bit) != 0
                            val dayText = if (isBengali) daysBn[i] else daysEn[i]

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    repeatDaysMask = if (isSelected) {
                                        repeatDaysMask and bit.inv()
                                    } else {
                                        repeatDaysMask or bit
                                    }
                                },
                                label = { Text(dayText, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                // Vibration Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBengali) "ভাইব্রেশন (Vibration)" else "Vibration",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Switch(
                        checked = vibrate,
                        onCheckedChange = { vibrate = it },
                        modifier = Modifier.testTag("alarm_vibrate_switch")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalLabel = if (label.trim().isEmpty()) {
                        if (isBengali) "রেগুলার অ্যালার্ম" else "Regular Alarm"
                    } else {
                        label.trim()
                    }
                    onSave(
                        timePickerState.hour,
                        timePickerState.minute,
                        finalLabel,
                        repeatDaysMask,
                        vibrate
                    )
                },
                modifier = Modifier.testTag("save_regular_alarm_btn")
            ) {
                Text(if (isBengali) "সংরক্ষণ করুন" else "Save Alarm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBengali) "বাতিল" else "Cancel")
            }
        }
    )
}

@Composable
private fun PresetButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text, style = MaterialTheme.typography.labelSmall) },
        modifier = modifier
    )
}

private fun valFor12H(hour24: Int): Int {
    val h = hour24 % 12
    return if (h == 0) 12 else h
}
