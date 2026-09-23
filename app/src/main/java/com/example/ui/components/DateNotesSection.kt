package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DateNote
import com.example.util.DateFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DateNotesSection(
    notes: List<DateNote>,
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    is24Hour: Boolean = false,
    isBengali: Boolean,
    onAddNewNote: () -> Unit,
    onEditNote: (DateNote) -> Unit = {},
    onDeleteNote: (DateNote) -> Unit,
    allNotes: List<DateNote> = emptyList(),
    onSelectDateOnCalendar: ((year: Int, month: Int, day: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val monthName = DateFormatter.getMonthName(selectedMonth, isBengali)
    val dayStr = DateFormatter.formatNumber(selectedDay, isBengali)
    val yearStr = DateFormatter.formatNumber(selectedYear, isBengali)

    // Note Search State
    var noteSearchQuery by rememberSaveable { mutableStateOf("") }
    var searchScopeAllDates by rememberSaveable { mutableStateOf(false) }
    var filterEditedOnly by rememberSaveable { mutableStateOf(false) }

    // Can we toggle between selected date notes and all calendar notes?
    val hasAllNotesScope = allNotes.isNotEmpty() && allNotes != notes

    // Determine candidate list based on scope
    val baseCandidateNotes = if (searchScopeAllDates && hasAllNotesScope) {
        allNotes
    } else {
        notes
    }

    // Filter candidate notes by search query and edit status
    val trimmedQuery = noteSearchQuery.trim()
    val filteredNotes = baseCandidateNotes.filter { note ->
        val matchesQuery = if (trimmedQuery.isEmpty()) {
            true
        } else {
            val q = trimmedQuery.lowercase()
            note.title.lowercase().contains(q) ||
            note.content.lowercase().contains(q) ||
            DateFormatter.getMonthName(note.month, isBengali).lowercase().contains(q) ||
            DateFormatter.getMonthName(note.month, false).lowercase().contains(q) ||
            "${note.day}".contains(q) ||
            "${note.year}".contains(q) ||
            DateFormatter.formatNumber(note.day, isBengali).contains(q) ||
            DateFormatter.formatNumber(note.year, isBengali).contains(q) ||
            "${note.hour}:${note.minute}".contains(q)
        }

        val matchesEdited = if (filterEditedOnly) {
            note.updatedAt != null
        } else {
            true
        }

        matchesQuery && matchesEdited
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("date_notes_card"),
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
            // Header Row: Title & Date Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBengali) "ক্যালেন্ডার নোটস" else "Calendar Notes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (searchScopeAllDates && hasAllNotesScope) {
                            if (isBengali) "সকল সংরক্ষিত নোটবুক (${DateFormatter.formatNumber(allNotes.size, true)}টি)" else "All Saved Notes (${allNotes.size})"
                        } else {
                            if (isBengali) "$dayStr $monthName $yearStr-এর নোটবুক" else "Notes for $dayStr $monthName $yearStr"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Add note mini badge or count
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = if (isBengali)
                            "${DateFormatter.formatNumber(filteredNotes.size, true)}টি নোট"
                        else
                            "${filteredNotes.size} Notes",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prominent Full-Width Horizontal Add Note Button Box
            Button(
                onClick = onAddNewNote,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("add_note_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBengali) "নতুন নোট লিখুন" else "Add New Note",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==================== IN-NOTE KEYWORD SEARCH BAR ====================
            OutlinedTextField(
                value = noteSearchQuery,
                onValueChange = { noteSearchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_search_input"),
                placeholder = {
                    Text(
                        text = if (isBengali)
                            "নোট খুঁজুন (কি-ওয়ার্ড, শিরোনাম বা লেখা)..."
                        else
                            "Search notes by keyword, title or content...",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Notes",
                        tint = if (noteSearchQuery.isNotEmpty())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (noteSearchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { noteSearchQuery = "" },
                            modifier = Modifier.testTag("clear_note_search_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                )
            )

            // Search Scope & Filter Chips
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (hasAllNotesScope) {
                    FilterChip(
                        selected = !searchScopeAllDates,
                        onClick = { searchScopeAllDates = false },
                        label = {
                            Text(
                                text = if (isBengali) "এই তারিখের (${DateFormatter.formatNumber(notes.size, true)})" else "This Date (${notes.size})",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.testTag("chip_this_date_notes")
                    )

                    FilterChip(
                        selected = searchScopeAllDates,
                        onClick = { searchScopeAllDates = true },
                        label = {
                            Text(
                                text = if (isBengali) "সব তারিখের নোট (${DateFormatter.formatNumber(allNotes.size, true)})" else "All Notes (${allNotes.size})",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("chip_all_dates_notes")
                    )
                }

                FilterChip(
                    selected = filterEditedOnly,
                    onClick = { filterEditedOnly = !filterEditedOnly },
                    label = {
                        Text(
                            text = if (isBengali) "এডিটেড নোট" else "Edited Only",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier.testTag("chip_edited_notes_filter")
                )
            }

            // Keyword Match Summary Bar
            AnimatedVisibility(visible = trimmedQuery.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBengali)
                                "\"$trimmedQuery\" কি-ওয়ার্ডে ${DateFormatter.formatNumber(filteredNotes.size, true)}টি ফলাফল পাওয়া গেছে"
                            else
                                "${filteredNotes.size} results for \"$trimmedQuery\"",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        TextButton(
                            onClick = {
                                noteSearchQuery = ""
                                filterEditedOnly = false
                            },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = if (isBengali) "রিসেট" else "Reset",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==================== LIST OF NOTES OR EMPTY STATE ====================
            if (filteredNotes.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (trimmedQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isBengali)
                                    "\"$trimmedQuery\" দিয়ে কোনো নোট খুঁজে পাওয়া যায়নি।"
                                else
                                    "No notes found matching \"$trimmedQuery\".",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isBengali)
                                    "ভিন্ন কোনো শব্দ দিয়ে খুঁজুন অথবা সকল তারিখের নোট অপশন চালু করুন।"
                                else
                                    "Try another keyword or switch to 'All Notes' scope.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { noteSearchQuery = "" },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (isBengali) "সার্চ ক্লিয়ার করুন" else "Clear Search")
                            }
                        } else {
                            Text(
                                text = if (isBengali)
                                    "এই তারিখে এখনো কোনো নোট লেখা হয়নি।"
                                else
                                    "No notes written for this date.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isBengali)
                                    "উপরে '+ নতুন নোট লিখুন' চাপলে নির্দিষ্ট সময় ও তারিখ সহ গোপনীয় ব্যক্তিগত নোট সেভ থাকবে।"
                                else
                                    "Tap 'Add New Note' above to record private notes with exact date and time.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredNotes.forEach { note ->
                        val isDifferentDate = note.year != selectedYear || note.month != selectedMonth || note.day != selectedDay
                        NoteItem(
                            note = note,
                            searchKeyword = trimmedQuery,
                            isDifferentDate = isDifferentDate,
                            is24Hour = is24Hour,
                            isBengali = isBengali,
                            onEdit = { onEditNote(note) },
                            onDelete = { onDeleteNote(note) },
                            onJumpToDate = if (isDifferentDate && onSelectDateOnCalendar != null) {
                                { onSelectDateOnCalendar(note.year, note.month, note.day) }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteItem(
    note: DateNote,
    searchKeyword: String = "",
    isDifferentDate: Boolean = false,
    is24Hour: Boolean,
    isBengali: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onJumpToDate: (() -> Unit)? = null
) {
    // Created Time calculation
    val displayHour = if (is24Hour) {
        note.hour
    } else {
        val h = note.hour % 12
        if (h == 0) 12 else h
    }
    val hourStr = DateFormatter.formatNumber(displayHour, isBengali, minDigits = 2)
    val minStr = DateFormatter.formatNumber(note.minute, isBengali, minDigits = 2)
    val amPmStr = if (is24Hour) "" else if (note.hour < 12) {
        if (isBengali) "পূর্বাহ্ন" else "AM"
    } else {
        if (isBengali) "অপরাহ্ন" else "PM"
    }

    val noteDateStr = "${DateFormatter.formatNumber(note.day, isBengali)} ${DateFormatter.getMonthName(note.month, isBengali)} ${DateFormatter.formatNumber(note.year, isBengali)}"

    // Highlight styles
    val highlightColor = MaterialTheme.colorScheme.primary
    val highlightBg = MaterialTheme.colorScheme.primaryContainer

    val highlightedTitle = remember(note.title, searchKeyword) {
        buildHighlightedText(
            text = note.title,
            query = searchKeyword,
            highlightColor = highlightColor,
            highlightBg = highlightBg
        )
    }

    val highlightedContent = remember(note.content, searchKeyword) {
        buildHighlightedText(
            text = note.content,
            query = searchKeyword,
            highlightColor = highlightColor,
            highlightBg = highlightBg
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("note_item_${note.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Time & Date Meta Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Exact Creation Time Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (amPmStr.isNotEmpty()) "$hourStr:$minStr $amPmStr" else "$hourStr:$minStr",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    // Date Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDifferentDate)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = if (isDifferentDate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = noteDateStr,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isDifferentDate) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDifferentDate) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Edit count badge if edited
                    if (note.updatedAt != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isBengali) "আপডেটেড (${DateFormatter.formatNumber(note.editCount, true)})" else "Edited (${note.editCount})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // Jump to date shortcut button if from another date
                if (onJumpToDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        onClick = onJumpToDate,
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBengali) "ক্যালেন্ডারের তারিখে যান" else "Jump to Date in Calendar",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title with Highlighted Match
                Text(
                    text = highlightedTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Content with Highlighted Match
                if (note.content.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = highlightedContent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // If updated, display last update timestamp row
                if (note.updatedAt != null && note.updatedHour != null && note.updatedMinute != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val updH = if (is24Hour) note.updatedHour else {
                        val h = note.updatedHour % 12
                        if (h == 0) 12 else h
                    }
                    val updAmPm = if (is24Hour) "" else if (note.updatedHour < 12) {
                        if (isBengali) "পূর্বাহ্ন" else "AM"
                    } else {
                        if (isBengali) "অপরাহ্ন" else "PM"
                    }
                    val updTimeStr = "${DateFormatter.formatNumber(updH, isBengali, minDigits = 2)}:${DateFormatter.formatNumber(note.updatedMinute, isBengali, minDigits = 2)} $updAmPm"
                    val updDateStr = if (note.updatedDay != null && note.updatedMonth != null && note.updatedYear != null) {
                        "${DateFormatter.formatNumber(note.updatedDay, isBengali)} ${DateFormatter.getMonthName(note.updatedMonth, isBengali)}"
                    } else ""

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBengali)
                                    "শেষ আপডেট: $updTimeStr ${if (updDateStr.isNotEmpty()) "($updDateStr)" else ""}"
                                else
                                    "Last Update: $updTimeStr ${if (updDateStr.isNotEmpty()) "($updDateStr)" else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Action Buttons: Edit and Delete
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("edit_note_btn_${note.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Note",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_note_btn_${note.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Note",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Highlights matches of query within text.
 */
private fun buildHighlightedText(
    text: String,
    query: String,
    highlightColor: Color,
    highlightBg: Color
): AnnotatedString {
    if (query.isBlank() || !text.contains(query, ignoreCase = true)) {
        return AnnotatedString(text)
    }

    return buildAnnotatedString {
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        var startIndex = 0

        while (startIndex < text.length) {
            val matchIndex = lowerText.indexOf(lowerQuery, startIndex)
            if (matchIndex == -1) {
                append(text.substring(startIndex))
                break
            }

            if (matchIndex > startIndex) {
                append(text.substring(startIndex, matchIndex))
            }

            val endIndex = matchIndex + query.length
            withStyle(
                style = SpanStyle(
                    background = highlightBg,
                    color = highlightColor,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(text.substring(matchIndex, endIndex))
            }

            startIndex = endIndex
        }
    }
}
