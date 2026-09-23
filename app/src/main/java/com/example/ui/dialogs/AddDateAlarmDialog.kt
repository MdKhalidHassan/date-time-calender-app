package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Title
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.util.DateFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDateAlarmDialog(
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int,
    isBengali: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        title: String,
        description: String,
        vibrate: Boolean
    ) -> Unit
) {
    val initialCal = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = initialCal.get(Calendar.HOUR_OF_DAY),
        initialMinute = initialCal.get(Calendar.MINUTE),
        is24Hour = false
    )

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var vibrate by remember { mutableStateOf(true) }
    var useCompactTimeInput by remember { mutableStateOf(true) }

    val monthName = DateFormatter.getMonthName(initialMonth, isBengali)
    val dayStr = DateFormatter.formatNumber(initialDay, isBengali)
    val yearStr = DateFormatter.formatNumber(initialYear, isBengali)

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
                    imageVector = Icons.Default.NotificationImportant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isBengali) "নির্দিষ্ট তারিখের অ্যালার্ম" else "Date-Specific Alarm",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$dayStr $monthName $yearStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Time & Style Header
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

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isBengali) "অ্যালার্মের শিরোনাম / টাইটেল" else "Alarm Title") },
                    placeholder = { Text(if (isBengali) "যেমন: জরুরি মিটিং বা বিশেষ স্মরণিকা" else "e.g. Important Meeting") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Title, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("date_alarm_title_input")
                )

                // Description / Reminder Note Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (isBengali) "রিমাইন্ডার বিবরণ (Description)" else "Reminder / Note Details") },
                    placeholder = { Text(if (isBengali) "অ্যালার্মের সাথে যা মনে রাখতে চান..." else "Notes to recall when alarm rings...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null)
                    },
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("date_alarm_desc_input")
                )

                // Vibration Switch
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
                        modifier = Modifier.testTag("date_alarm_vibrate_switch")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = if (title.trim().isEmpty()) {
                        if (isBengali) "তারিখের অ্যালার্ম" else "Date Alarm"
                    } else {
                        title.trim()
                    }
                    onSave(
                        initialYear,
                        initialMonth,
                        initialDay,
                        timePickerState.hour,
                        timePickerState.minute,
                        finalTitle,
                        description.trim(),
                        vibrate
                    )
                },
                modifier = Modifier.testTag("save_date_alarm_btn")
            ) {
                Text(if (isBengali) "অ্যালার্ম সেট করুন" else "Save Alarm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBengali) "বাতিল" else "Cancel")
            }
        }
    )
}

private fun valFor12H(hour24: Int): Int {
    val h = hour24 % 12
    return if (h == 0) 12 else h
}
