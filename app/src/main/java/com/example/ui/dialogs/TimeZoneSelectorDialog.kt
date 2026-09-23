package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.model.TimeZoneData
import com.example.model.TimeZoneItem
import java.util.TimeZone

@Composable
fun TimeZoneSelectorDialog(
    currentTimeZoneId: String,
    isBengali: Boolean,
    onDismiss: () -> Unit,
    onSelectTimeZone: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableIntStateOf(0) } // 0 = Popular, 1 = GMT Offsets, 2 = All

    val allSystemIds = remember {
        TimeZone.getAvailableIDs().sorted()
    }

    val filteredItems: List<TimeZoneItem> = remember(searchQuery, selectedCategory) {
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            // Filter from popular, offsets and all system timezones
            val matchingPopular = TimeZoneData.popularPresets.filter {
                it.nameEn.lowercase().contains(q) ||
                it.nameBn.lowercase().contains(q) ||
                it.id.lowercase().contains(q) ||
                it.gmtOffset.lowercase().contains(q)
            }
            val matchingOffsets = TimeZoneData.standardGmtOffsets.filter {
                it.id.lowercase().contains(q) || it.nameEn.lowercase().contains(q)
            }
            val matchingSystem = allSystemIds.filter { it.lowercase().contains(q) }.take(40).map { id ->
                TimeZoneItem(
                    id = id,
                    nameEn = id.replace('_', ' '),
                    nameBn = id.replace('_', ' '),
                    gmtOffset = TimeZoneData.getDisplayName(id, isBengali = false).substringBefore(' ')
                )
            }
            (matchingPopular + matchingOffsets + matchingSystem).distinctBy { it.id }
        } else {
            when (selectedCategory) {
                0 -> TimeZoneData.popularPresets
                1 -> TimeZoneData.standardGmtOffsets
                else -> allSystemIds.take(100).map { id ->
                    TimeZoneItem(
                        id = id,
                        nameEn = id.replace('_', ' '),
                        nameBn = id.replace('_', ' '),
                        gmtOffset = TimeZoneData.getDisplayName(id, isBengali = false).substringBefore(' ')
                    )
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isBengali) "টাইম জোন ও GMT নির্বাচন করুন" else "Select Time Zone / GMT",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isBengali) "ঘড়ির সময় এই টাইম জোন অনুযায়ী চলবে" else "Clock will reflect chosen time zone",
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
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Reset to Device Timezone Button
                OutlinedButton(
                    onClick = {
                        val deviceDefault = TimeZone.getDefault().id
                        onSelectTimeZone(deviceDefault)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_device_tz_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBengali) "ডিভাইসের সিস্টেম টাইম জোনে রিসেট করুন" else "Reset to Device System Time Zone",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(if (isBengali) "শহর, দেশ বা GMT খুঁজুন (যেমন: Dhaka, +6)" else "Search city, country or GMT...")
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tz_search_input")
                )

                // Category Chips (when not actively searching)
                if (searchQuery.isEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == 0,
                            onClick = { selectedCategory = 0 },
                            label = { Text(if (isBengali) "জনপ্রিয় শহর" else "Popular") }
                        )
                        FilterChip(
                            selected = selectedCategory == 1,
                            onClick = { selectedCategory = 1 },
                            label = { Text(if (isBengali) "GMT অফসেট" else "GMT Offsets") }
                        )
                        FilterChip(
                            selected = selectedCategory == 2,
                            onClick = { selectedCategory = 2 },
                            label = { Text(if (isBengali) "সকল" else "All") }
                        )
                    }
                }

                // List of Timezones
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 300.dp)
                ) {
                    if (filteredItems.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isBengali) "কোনো টাইম জোন মেলেনি।" else "No matching time zone found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn {
                            items(filteredItems, key = { it.id }) { item ->
                                val isSelected = item.id.equals(currentTimeZoneId, ignoreCase = true)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectTimeZone(item.id) }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = item.flag,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = if (isBengali) item.nameBn else item.nameEn,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${item.gmtOffset} • ${item.id}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBengali) "বন্ধ করুন" else "Close")
            }
        }
    )
}
