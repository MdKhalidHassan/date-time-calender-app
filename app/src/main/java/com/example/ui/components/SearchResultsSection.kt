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
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DateAlarm
import com.example.data.model.DateNote
import com.example.data.model.RegularAlarm
import com.example.util.DateFormatter

@Composable
fun SearchBarHeader(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = if (isBengali) "অ্যালার্ম ও নোটস অনুসন্ধান করুন..." else "Search saved alarms & notes...",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = onClearQuery,
                        modifier = Modifier.testTag("clear_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("local_search_input")
        )
    }
}

@Composable
fun SearchResultsSection(
    searchQuery: String,
    filteredRegularAlarms: List<RegularAlarm>,
    filteredDateAlarms: List<DateAlarm>,
    filteredNotes: List<DateNote>,
    is24Hour: Boolean,
    isBengali: Boolean,
    onToggleRegularAlarm: (RegularAlarm, Boolean) -> Unit,
    onDeleteRegularAlarm: (RegularAlarm) -> Unit,
    onToggleDateAlarm: (DateAlarm, Boolean) -> Unit,
    onDeleteDateAlarm: (DateAlarm) -> Unit,
    onEditNote: (DateNote) -> Unit = {},
    onDeleteNote: (DateNote) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableIntStateOf(0) } // 0 = All, 1 = Alarms, 2 = Notes

    val totalMatches = (if (selectedFilter != 2) filteredRegularAlarms.size + filteredDateAlarms.size else 0) +
            (if (selectedFilter != 1) filteredNotes.size else 0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("search_results_container"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Filter Chips & Matches Count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedFilter == 0,
                    onClick = { selectedFilter = 0 },
                    label = { Text(if (isBengali) "সব (${filteredRegularAlarms.size + filteredDateAlarms.size + filteredNotes.size})" else "All") }
                )
                FilterChip(
                    selected = selectedFilter == 1,
                    onClick = { selectedFilter = 1 },
                    label = { Text(if (isBengali) "অ্যালার্ম (${filteredRegularAlarms.size + filteredDateAlarms.size})" else "Alarms") }
                )
                FilterChip(
                    selected = selectedFilter == 2,
                    onClick = { selectedFilter = 2 },
                    label = { Text(if (isBengali) "নোটস (${filteredNotes.size})" else "Notes") }
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            ) {
                Text(
                    text = if (isBengali) "$totalMatches টি ফলাফল" else "$totalMatches found",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        if (totalMatches == 0) {
            // Empty Search State
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isBengali)
                            "\"$searchQuery\" এর সাথে কোনো অ্যালার্ম বা নোট মেলেনি"
                        else
                            "No alarms or notes matched \"$searchQuery\"",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBengali)
                            "ভিন্ন কোনো শব্দ বা তারিখ দিয়ে পুনরায় অনুসন্ধান করে দেখুন।"
                        else
                            "Try searching with a different keyword, date, or time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            // Show Matching Regular Alarms
            if (selectedFilter != 2 && filteredRegularAlarms.isNotEmpty()) {
                Text(
                    text = if (isBengali) "রেগুলার অ্যালার্ম (${filteredRegularAlarms.size})" else "Recurring Alarms (${filteredRegularAlarms.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredRegularAlarms.forEach { alarm ->
                        SearchRegularAlarmItem(
                            alarm = alarm,
                            is24Hour = is24Hour,
                            isBengali = isBengali,
                            onToggle = { isEnabled -> onToggleRegularAlarm(alarm, isEnabled) },
                            onDelete = { onDeleteRegularAlarm(alarm) }
                        )
                    }
                }
            }

            // Show Matching Date Alarms
            if (selectedFilter != 2 && filteredDateAlarms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isBengali) "তারিখের অ্যালার্ম (${filteredDateAlarms.size})" else "Date Alarms (${filteredDateAlarms.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredDateAlarms.forEach { alarm ->
                        SearchDateAlarmItem(
                            alarm = alarm,
                            is24Hour = is24Hour,
                            isBengali = isBengali,
                            onToggle = { isEnabled -> onToggleDateAlarm(alarm, isEnabled) },
                            onDelete = { onDeleteDateAlarm(alarm) }
                        )
                    }
                }
            }

            // Show Matching Notes
            if (selectedFilter != 1 && filteredNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isBengali) "তারিখের নোটস (${filteredNotes.size})" else "Notes (${filteredNotes.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredNotes.forEach { note ->
                        SearchNoteItem(
                            note = note,
                            is24Hour = is24Hour,
                            isBengali = isBengali,
                            onEdit = { onEditNote(note) },
                            onDelete = { onDeleteNote(note) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchRegularAlarmItem(
    alarm: RegularAlarm,
    is24Hour: Boolean,
    isBengali: Boolean,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val displayHour = if (is24Hour) alarm.hour else {
        val h = alarm.hour % 12
        if (h == 0) 12 else h
    }
    val hourStr = DateFormatter.formatNumber(displayHour, isBengali, minDigits = 2)
    val minStr = DateFormatter.formatNumber(alarm.minute, isBengali, minDigits = 2)
    val amPm = if (!is24Hour) {
        if (alarm.hour >= 12) (if (isBengali) "অপরাহ্ন" else "PM") else (if (isBengali) "পূর্বাহ্ন" else "AM")
    } else ""

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$hourStr:$minStr $amPm",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = alarm.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchDateAlarmItem(
    alarm: DateAlarm,
    is24Hour: Boolean,
    isBengali: Boolean,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val displayHour = if (is24Hour) alarm.hour else {
        val h = alarm.hour % 12
        if (h == 0) 12 else h
    }
    val hourStr = DateFormatter.formatNumber(displayHour, isBengali, minDigits = 2)
    val minStr = DateFormatter.formatNumber(alarm.minute, isBengali, minDigits = 2)
    val amPm = if (!is24Hour) {
        if (alarm.hour >= 12) (if (isBengali) "অপরাহ্ন" else "PM") else (if (isBengali) "পূর্বাহ্ন" else "AM")
    } else ""

    val dateStr = "${DateFormatter.formatNumber(alarm.day, isBengali)} ${DateFormatter.getMonthName(alarm.month, isBengali)} ${DateFormatter.formatNumber(alarm.year, isBengali)}"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationImportant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$hourStr:$minStr $amPm",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = alarm.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (alarm.description.isNotEmpty()) {
                    Text(
                        text = alarm.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!alarm.isCompleted) {
                    Switch(
                        checked = alarm.isEnabled,
                        onCheckedChange = onToggle
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchNoteItem(
    note: DateNote,
    is24Hour: Boolean,
    isBengali: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val displayHour = if (is24Hour) note.hour else {
        val h = note.hour % 12
        if (h == 0) 12 else h
    }
    val hourStr = DateFormatter.formatNumber(displayHour, isBengali, minDigits = 2)
    val minStr = DateFormatter.formatNumber(note.minute, isBengali, minDigits = 2)
    val amPmStr = if (is24Hour) "" else if (note.hour < 12) (if (isBengali) "পূর্বাহ্ন" else "AM") else (if (isBengali) "অপরাহ্ন" else "PM")

    val dateStr = "${DateFormatter.formatNumber(note.day, isBengali)} ${DateFormatter.getMonthName(note.month, isBengali)} ${DateFormatter.formatNumber(note.year, isBengali)}"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$dateStr • $hourStr:$minStr $amPmStr",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (note.updatedAt != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = if (isBengali) "আপডেটেড" else "Edited",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (note.content.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
