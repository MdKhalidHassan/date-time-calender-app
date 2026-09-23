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
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.data.model.DateNote
import com.example.util.DateFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteDialog(
    year: Int,
    month: Int,
    day: Int,
    existingNote: DateNote? = null,
    is24Hour: Boolean = false,
    isBengali: Boolean,
    onDismiss: () -> Unit,
    onSave: (hour: Int, minute: Int, title: String, content: String) -> Unit
) {
    val isEditMode = existingNote != null
    val currentCal = Calendar.getInstance()
    val defaultHour = existingNote?.hour ?: currentCal.get(Calendar.HOUR_OF_DAY)
    val defaultMinute = existingNote?.minute ?: currentCal.get(Calendar.MINUTE)

    val timePickerState = rememberTimePickerState(
        initialHour = defaultHour,
        initialMinute = defaultMinute,
        is24Hour = is24Hour
    )

    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var useCompactTimeInput by remember { mutableStateOf(true) }

    val monthName = DateFormatter.getMonthName(year, isBengali)
    val dayStr = DateFormatter.formatNumber(day, isBengali)
    val yearStr = DateFormatter.formatNumber(year, isBengali)

    // Formatted selected time preview
    val displayHour = if (is24Hour) {
        timePickerState.hour
    } else {
        val h = timePickerState.hour % 12
        if (h == 0) 12 else h
    }
    val hourStr = DateFormatter.formatNumber(displayHour, isBengali, minDigits = 2)
    val minStr = DateFormatter.formatNumber(timePickerState.minute, isBengali, minDigits = 2)
    val amPmStr = if (is24Hour) "" else if (timePickerState.hour < 12) {
        if (isBengali) "পূর্বাহ্ন (AM)" else "AM"
    } else {
        if (isBengali) "অপরাহ্ন (PM)" else "PM"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .imePadding(),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isEditMode) Icons.Default.Edit else Icons.AutoMirrored.Filled.NoteAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isEditMode) {
                            if (isBengali) "নোট সম্পাদন / এডিট করুন" else "Edit Date Note"
                        } else {
                            if (isBengali) "তারিখ ও সময়ের নোট লিখুন" else "Add Date & Time Note"
                        },
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
                // If editing existing note, show creation & history details
                if (isEditMode && existingNote != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBengali) "নোটের ইতিহাস ও তথ্য" else "Note History",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val origH = if (is24Hour) existingNote.hour else {
                                val h = existingNote.hour % 12
                                if (h == 0) 12 else h
                            }
                            val origAmPm = if (is24Hour) "" else if (existingNote.hour < 12) (if (isBengali) "পূর্বাহ্ন" else "AM") else (if (isBengali) "অপরাহ্ন" else "PM")
                            val origTime = "${DateFormatter.formatNumber(origH, isBengali, minDigits = 2)}:${DateFormatter.formatNumber(existingNote.minute, isBengali, minDigits = 2)} $origAmPm"
                            Text(
                                text = if (isBengali) "মূল সৃষ্টির সময়: $origTime" else "Originally Created: $origTime",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            if (existingNote.updatedAt != null) {
                                val editCountStr = DateFormatter.formatNumber(existingNote.editCount, isBengali)
                                Text(
                                    text = if (isBengali) "পূর্বে আপডেট করা হয়েছে: $editCountStr বার" else "Previously edited: $editCountStr time(s)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Time Preview Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
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
                                    text = if (isBengali) "নোটের সময়:" else "Note Time:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "$hourStr:$minStr $amPmStr",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        // Compact / Dial Toggle
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

                // Time Picker (Compact Input default to save screen space)
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

                // Note Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isBengali) "নোটের শিরোনাম (Title)" else "Note Title") },
                    placeholder = { Text(if (isBengali) "যেমন: মিটিং বা জরুরি কাজের নোট" else "e.g. Important Task or Meeting") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Title, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_title_input")
                )

                // Note Content
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(if (isBengali) "নোটের বিস্তারিত বিষয়বস্তু" else "Note Content") },
                    placeholder = { Text(if (isBengali) "এই তারিখ ও সময়ের যা লিখে রাখতে চান..." else "Write note details here...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null)
                    },
                    minLines = 3,
                    maxLines = 8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_content_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = if (title.trim().isEmpty()) {
                        if (isBengali) "তারিখের নোট" else "Date Note"
                    } else {
                        title.trim()
                    }
                    onSave(
                        timePickerState.hour,
                        timePickerState.minute,
                        finalTitle,
                        content.trim()
                    )
                },
                modifier = Modifier.testTag("save_note_btn")
            ) {
                Text(
                    if (isEditMode) {
                        if (isBengali) "আপডেট সংরক্ষণ করুন" else "Update Note"
                    } else {
                        if (isBengali) "নোট সংরক্ষণ করুন" else "Save Note"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBengali) "বাতিল" else "Cancel")
            }
        }
    )
}
