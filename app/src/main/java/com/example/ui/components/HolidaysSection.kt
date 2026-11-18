package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.HolidayEntity
import com.example.util.DateFormatter
import java.util.Calendar
import java.util.Locale

fun countryCodeToFlagEmoji(countryCode: String): String {
    val clean = countryCode.trim().uppercase(Locale.US)
    if (clean.length != 2 || !clean[0].isLetter() || !clean[1].isLetter()) return "🏳️"
    val firstLetter = Character.codePointAt(clean, 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(clean, 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
}

fun calculateDaysUntilHoliday(
    holiday: HolidayEntity,
    todayCalendar: Calendar = Calendar.getInstance()
): Long {
    val todayMidnight = (todayCalendar.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val targetMidnight = Calendar.getInstance().apply {
        clear()
        set(holiday.year, holiday.month, holiday.day, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val diffMillis = targetMidnight.timeInMillis - todayMidnight.timeInMillis
    return diffMillis / (1000L * 60L * 60L * 24L)
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HolidaysSection(
    selectedCountryCode: String,
    selectedCountryName: String,
    holidays: List<HolidayEntity>,
    remindersEnabled: Boolean,
    remindDaysBefore: Int,
    remindHour: Int,
    remindMinute: Int,
    isLoading: Boolean,
    errorMessage: String?,
    is24Hour: Boolean,
    isBengali: Boolean,
    onOpenCountryPicker: () -> Unit,
    onRefresh: () -> Unit,
    onToggleRemindersEnabled: (Boolean) -> Unit,
    onChangeLeadDays: (Int) -> Unit,
    onChangeRemindTime: (hour: Int, minute: Int) -> Unit,
    onToggleSingleHoliday: (HolidayEntity, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    lastSyncedAtMillis: Long = 0L
) {
    val context = LocalContext.current
    var notificationPermissionDenied by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            notificationPermissionDenied = false
            onToggleRemindersEnabled(true)
        } else {
            notificationPermissionDenied = true
            onToggleRemindersEnabled(false)
        }
    }

    val todayCal = remember(holidays) { Calendar.getInstance() }
    val upcomingHolidays = remember(holidays) {
        holidays
            .map { holiday -> holiday to calculateDaysUntilHoliday(holiday, todayCal) }
            .filter { (_, daysUntil) -> daysUntil >= 0L }
            .sortedWith(compareBy({ it.first.year }, { it.first.month }, { it.first.day }))
    }

    val formattedTime = remember(remindHour, remindMinute, is24Hour, isBengali) {
        formatReminderClockTime(remindHour, remindMinute, is24Hour, isBengali)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("holidays_card"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Country Selection & Refresh Header Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (selectedCountryCode.isNotBlank()) {
                        Text(
                            text = countryCodeToFlagEmoji(selectedCountryCode),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "সরকারি ছুটির দিন ও রিমাইন্ডার" else "Government Holidays & Reminders",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (selectedCountryCode.isNotBlank()) {
                                val displayName = selectedCountryName.ifBlank { selectedCountryCode }
                                "$displayName ($selectedCountryCode)"
                            } else {
                                if (isBengali) "কোনো দেশ নির্বাচন করা হয়নি" else "No country selected yet"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("selected_country_label")
                        )
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenCountryPicker,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("select_country_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBengali) "দেশ পরিবর্তন করুন" else "Change Country")
                    }

                    OutlinedButton(
                        onClick = onRefresh,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("refresh_holidays_btn")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBengali) "আপডেট করুন" else "Sync Holidays")
                    }
                }

                val syncInfoText = remember(lastSyncedAtMillis, is24Hour, isBengali) {
                    if (lastSyncedAtMillis > 0L) {
                        val cal = Calendar.getInstance().apply { timeInMillis = lastSyncedAtMillis }
                        val d = DateFormatter.formatNumber(cal.get(Calendar.DAY_OF_MONTH), isBengali)
                        val m = DateFormatter.getMonthName(cal.get(Calendar.MONTH), isBengali)
                        val timeStr = formatReminderClockTime(
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            is24Hour,
                            isBengali
                        )
                        if (isBengali) {
                            "সর্বশেষ আপডেট: $d $m, $timeStr • প্রতিদিন স্বয়ংক্রিয়ভাবে আপডেট হয়"
                        } else {
                            "Last synced: $d $m, $timeStr • Auto-syncs daily"
                        }
                    } else {
                        if (isBengali) {
                            "অনলাইনে প্রতিদিন স্বয়ংক্রিয়ভাবে সরকারি ছুটির তালিকা আপডেট হয়"
                        } else {
                            "Auto-syncs daily when online for holiday updates"
                        }
                    }
                }
                Text(
                    text = syncInfoText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("holiday_sync_status_text")
                )
            }
        }

        // 2. Reminder Preferences Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (remindersEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = if (remindersEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBengali) "স্বয়ংক্রিয় ছুটি রিমাইন্ডার" else "Upcoming Holiday Reminders",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            val leadDesc = formatLeadDaysLabel(remindDaysBefore, isBengali)
                            Text(
                                text = if (remindersEnabled) {
                                    if (isBengali) "$leadDesc • $formattedTime-এ নোটিফিকেশন"
                                    else "Notify $leadDesc at $formattedTime"
                                } else {
                                    if (isBengali) "বন্ধ আছে (চালু করলে আগে থেকেই মনে করিয়ে দেবে)"
                                    else "Off (enable to get reminded before holidays)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = remindersEnabled,
                        onCheckedChange = { checked ->
                            if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val granted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    notificationPermissionDenied = false
                                    onToggleRemindersEnabled(true)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                notificationPermissionDenied = false
                                onToggleRemindersEnabled(checked)
                            }
                        },
                        modifier = Modifier.testTag("holiday_reminders_switch")
                    )
                }

                if (notificationPermissionDenied) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBengali) "রিমাইন্ডার পেতে নোটিফিকেশন অনুমতি (Permission) প্রয়োজন।" else "Notification permission is required for holiday reminders.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Lead days chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isBengali) "কতদিন আগে রিমাইন্ডার দেবে:" else "Remind me:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val leadOptions = listOf(0, 1, 3, 7)
                        leadOptions.forEach { days ->
                            FilterChip(
                                selected = remindDaysBefore == days,
                                onClick = { onChangeLeadDays(days) },
                                label = {
                                    Text(
                                        text = formatLeadDaysLabel(days, isBengali),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                modifier = Modifier.testTag("holiday_lead_days_$days")
                            )
                        }
                    }
                }

                // Reminder Time Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isBengali) "রিমাইন্ডারের সময়:" else "Reminder time:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = { showTimePickerDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("holiday_time_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(formattedTime)
                    }
                }
            }
        }

        // 3. Offline/Error warning if cached list also exists
        if (errorMessage != null && upcomingHolidays.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBengali) "অনলাইন আপডেট ব্যর্থ হয়েছে — অফলাইনে সংরক্ষিত তালিকা দেখানো হচ্ছে।" else "Refresh failed ($errorMessage) — showing saved offline holidays.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // 4. Main Content States
        when {
            isLoading && upcomingHolidays.isEmpty() -> {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("holidays_loading_state")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = if (isBengali) "সরকারি ছুটির তালিকা আনা হচ্ছে..." else "Fetching upcoming public holidays...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            errorMessage != null && upcomingHolidays.isEmpty() -> {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("holidays_error_banner")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = if (isBengali) "ছুটির তথ্য আনা যায়নি। ইন্টারনেট সংযোগ পরীক্ষা করে আবার চেষ্টা করুন।" else "Unable to fetch holidays ($errorMessage). Please check your connection and try again.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(
                            onClick = onRefresh,
                            modifier = Modifier.testTag("holidays_retry_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBengali) "আবার চেষ্টা করুন" else "Retry")
                        }
                    }
                }
            }

            upcomingHolidays.isEmpty() -> {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("holidays_empty_state")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = if (isBengali) "আসন্ন কোনো ছুটির তালিকা নেই।" else "No upcoming holidays loaded yet.",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isBengali) "আপনার দেশ নির্বাচন করে আপডেট বাটনে চাপ দিন।" else "Select a country and tap Sync Holidays to view upcoming public holidays.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                // Next Holiday Highlight Banner
                val (nextHoliday, daysUntilNext) = upcomingHolidays.first()
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("next_holiday_banner")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBengali) "পরবর্তী সরকারি ছুটি" else "Next Public Holiday",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = DateFormatter.formatRelativeDays(daysUntilNext, isBengali),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = nextHoliday.localName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        if (nextHoliday.name != nextHoliday.localName) {
                            Text(
                                text = nextHoliday.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }

                        Text(
                            text = formatHolidayDateWithDay(nextHoliday, isBengali),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Text(
                    text = if (isBengali) {
                        "আসন্ন সরকারি ছুটিসমূহ (${DateFormatter.formatNumber(upcomingHolidays.size, true)})"
                    } else {
                        "Upcoming Holidays (${upcomingHolidays.size})"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                upcomingHolidays.forEach { (holiday, daysUntil) ->
                    HolidayListItemCard(
                        holiday = holiday,
                        daysUntil = daysUntil,
                        globalRemindersEnabled = remindersEnabled,
                        isBengali = isBengali,
                        onToggleReminder = { enabled -> onToggleSingleHoliday(holiday, enabled) }
                    )
                }
            }
        }
    }

    if (showTimePickerDialog) {
        HolidayReminderTimeDialog(
            initialHour = remindHour,
            initialMinute = remindMinute,
            is24Hour = is24Hour,
            isBengali = isBengali,
            onDismiss = { showTimePickerDialog = false },
            onSave = { h, m ->
                onChangeRemindTime(h, m)
                showTimePickerDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HolidayListItemCard(
    holiday: HolidayEntity,
    daysUntil: Long,
    globalRemindersEnabled: Boolean,
    isBengali: Boolean,
    onToggleReminder: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("holiday_item_${holiday.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formatHolidayDateWithDay(holiday, isBengali),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = holiday.localName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (holiday.name != holiday.localName) {
                    Text(
                        text = holiday.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = DateFormatter.formatRelativeDays(daysUntil, isBengali),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    val scopeLabel = if (holiday.isGlobal) {
                        if (isBengali) "জাতীয়" else "National"
                    } else {
                        if (isBengali) "আঞ্চলিক" else "Regional"
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "$scopeLabel • ${holiday.typesCsv.replace(",", ", ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(
                    checked = holiday.reminderEnabled,
                    onCheckedChange = onToggleReminder,
                    enabled = globalRemindersEnabled,
                    modifier = Modifier.testTag("holiday_toggle_${holiday.id}")
                )
                Text(
                    text = if (holiday.reminderEnabled && globalRemindersEnabled) {
                        if (isBengali) "চালু" else "Notify"
                    } else {
                        if (isBengali) "বন্ধ" else "Muted"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HolidayReminderTimeDialog(
    initialHour: Int,
    initialMinute: Int,
    is24Hour: Boolean,
    isBengali: Boolean,
    onDismiss: () -> Unit,
    onSave: (hour: Int, minute: Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = is24Hour
    )
    var useCompactInput by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.imePadding(),
        title = {
            Text(
                text = if (isBengali) "রিমাইন্ডারের সময় নির্বাচন" else "Holiday Reminder Time",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = !useCompactInput,
                    onClick = { useCompactInput = !useCompactInput },
                    label = {
                        Text(
                            if (useCompactInput) {
                                if (isBengali) "ঘড়ি ডায়াল দেখান" else "Show Clock Dial"
                            } else {
                                if (isBengali) "কীবোর্ড ইনপুট" else "Keyboard Input"
                            }
                        )
                    }
                )
                if (useCompactInput) {
                    TimeInput(state = timePickerState)
                } else {
                    TimePicker(state = timePickerState)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(timePickerState.hour, timePickerState.minute) },
                modifier = Modifier.testTag("save_holiday_time_btn")
            ) {
                Text(if (isBengali) "সংরক্ষণ" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBengali) "বাতিল" else "Cancel")
            }
        }
    )
}

private fun formatLeadDaysLabel(daysBefore: Int, isBengali: Boolean): String {
    return when (daysBefore) {
        0 -> if (isBengali) "ছুটির দিনেই" else "Same day"
        1 -> if (isBengali) "১ দিন আগে" else "1 day before"
        else -> {
            val d = DateFormatter.formatNumber(daysBefore, isBengali)
            if (isBengali) "$d দিন আগে" else "$d days before"
        }
    }
}

private fun formatReminderClockTime(hour: Int, minute: Int, is24Hour: Boolean, isBengali: Boolean): String {
    return if (is24Hour) {
        val hStr = DateFormatter.formatNumber(hour, isBengali, minDigits = 2)
        val mStr = DateFormatter.formatNumber(minute, isBengali, minDigits = 2)
        "$hStr:$mStr"
    } else {
        val h12 = if (hour % 12 == 0) 12 else hour % 12
        val hStr = DateFormatter.formatNumber(h12, isBengali, minDigits = 2)
        val mStr = DateFormatter.formatNumber(minute, isBengali, minDigits = 2)
        val period = if (hour < 12) {
            if (isBengali) "সকাল (AM)" else "AM"
        } else {
            if (isBengali) "বিকাল/রাত (PM)" else "PM"
        }
        "$hStr:$mStr $period"
    }
}

private fun formatHolidayDateWithDay(holiday: HolidayEntity, isBengali: Boolean): String {
    val cal = Calendar.getInstance().apply {
        clear()
        set(holiday.year, holiday.month, holiday.day)
    }
    val dayOfWeek = DateFormatter.getDayOfWeekName(cal.get(Calendar.DAY_OF_WEEK), isBengali)
    val dayStr = DateFormatter.formatNumber(holiday.day, isBengali)
    val monthStr = DateFormatter.getMonthName(holiday.month, isBengali)
    val yearStr = DateFormatter.formatNumber(holiday.year, isBengali)
    return "$dayOfWeek, $dayStr $monthStr $yearStr"
}
