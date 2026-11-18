package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.DateTimeApplication
import com.example.alarm.AlarmScheduler
import com.example.data.model.DateAlarm
import com.example.data.model.DateNote
import com.example.data.model.HolidayEntity
import com.example.data.model.RegularAlarm
import com.example.model.CalendarDay
import com.example.model.SelectedDateInfo
import com.example.model.TimeData
import com.example.network.AvailableCountryDto
import com.example.util.DateFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class DateTimeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as? DateTimeApplication
    private val alarmRepository = app?.alarmRepository
    private val noteRepository = app?.noteRepository
    private val holidayRepository = app?.holidayRepository
    private val scheduler = AlarmScheduler(application)

    private val prefs = application.getSharedPreferences("datetime_prefs", Context.MODE_PRIVATE)
    private val KEY_TIMEZONE = "pref_user_timezone"
    private val KEY_HOLIDAY_COUNTRY_CODE = "pref_holiday_country_code"
    private val KEY_HOLIDAY_COUNTRY_NAME = "pref_holiday_country_name"
    private val KEY_HOLIDAY_REMINDERS_ENABLED = "pref_holiday_reminders_enabled"
    private val KEY_HOLIDAY_REMIND_DAYS_BEFORE = "pref_holiday_remind_days_before"
    private val KEY_HOLIDAY_REMIND_HOUR = "pref_holiday_remind_hour"
    private val KEY_HOLIDAY_REMIND_MINUTE = "pref_holiday_remind_minute"
    private val KEY_HOLIDAY_LAST_SYNC_AT = "pref_holiday_last_sync_at"
    private val KEY_HOLIDAY_LAST_SYNC_COUNTRY = "pref_holiday_last_sync_country"

    // Default to Asia/Dhaka if default is America/Los_Angeles, or use saved/system timezone
    private val initialTzId = prefs.getString(KEY_TIMEZONE, null) ?: run {
        val defTz = TimeZone.getDefault().id
        if (defTz == "America/Los_Angeles") "Asia/Dhaka" else defTz
    }

    private val _selectedTimeZoneId = MutableStateFlow(initialTzId)
    val selectedTimeZoneId: StateFlow<String> = _selectedTimeZoneId.asStateFlow()

    private val initialTime = DateFormatter.extractTimeData(TimeZone.getTimeZone(initialTzId))

    private val _timeData = MutableStateFlow(initialTime)
    val timeData: StateFlow<TimeData> = _timeData.asStateFlow()

    private val _displayedYear = MutableStateFlow(initialTime.year)
    val displayedYear: StateFlow<Int> = _displayedYear.asStateFlow()

    private val _displayedMonth = MutableStateFlow(initialTime.month)
    val displayedMonth: StateFlow<Int> = _displayedMonth.asStateFlow()

    private val _selectedDay = MutableStateFlow(
        CalendarDay(
            year = initialTime.year,
            month = initialTime.month,
            day = initialTime.dayOfMonth,
            isCurrentMonth = true,
            isToday = true,
            isSelected = true
        )
    )
    val selectedDay: StateFlow<CalendarDay> = _selectedDay.asStateFlow()

    private val _is24Hour = MutableStateFlow(false)
    val is24Hour: StateFlow<Boolean> = _is24Hour.asStateFlow()

    // Default to Bengali true
    private val _isBengali = MutableStateFlow(true)
    val isBengali: StateFlow<Boolean> = _isBengali.asStateFlow()

    // 0 = Calendar & Clock, 1 = Regular Alarms, 2 = Date Alarms, 3 = Notes, 4 = Compass, 5 = Holidays
    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    // --- Government Holiday & Reminder State ---
    private val _selectedCountryCode = MutableStateFlow(prefs.getString(KEY_HOLIDAY_COUNTRY_CODE, "") ?: "")
    val selectedCountryCode: StateFlow<String> = _selectedCountryCode.asStateFlow()

    private val _selectedCountryName = MutableStateFlow(prefs.getString(KEY_HOLIDAY_COUNTRY_NAME, "") ?: "")
    val selectedCountryName: StateFlow<String> = _selectedCountryName.asStateFlow()

    private val _holidayRemindersEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_HOLIDAY_REMINDERS_ENABLED, false)
    )
    val holidayRemindersEnabled: StateFlow<Boolean> = _holidayRemindersEnabled.asStateFlow()

    private val _holidayRemindDaysBefore = MutableStateFlow(
        prefs.getInt(KEY_HOLIDAY_REMIND_DAYS_BEFORE, 1).coerceIn(0, 30)
    )
    val holidayRemindDaysBefore: StateFlow<Int> = _holidayRemindDaysBefore.asStateFlow()

    private val _holidayRemindHour = MutableStateFlow(
        prefs.getInt(KEY_HOLIDAY_REMIND_HOUR, 9).coerceIn(0, 23)
    )
    val holidayRemindHour: StateFlow<Int> = _holidayRemindHour.asStateFlow()

    private val _holidayRemindMinute = MutableStateFlow(
        prefs.getInt(KEY_HOLIDAY_REMIND_MINUTE, 0).coerceIn(0, 59)
    )
    val holidayRemindMinute: StateFlow<Int> = _holidayRemindMinute.asStateFlow()

    private val _availableCountries = MutableStateFlow<List<AvailableCountryDto>>(emptyList())
    val availableCountries: StateFlow<List<AvailableCountryDto>> = _availableCountries.asStateFlow()

    private val _countriesLoading = MutableStateFlow(false)
    val countriesLoading: StateFlow<Boolean> = _countriesLoading.asStateFlow()

    private val _holidaysLoading = MutableStateFlow(false)
    val holidaysLoading: StateFlow<Boolean> = _holidaysLoading.asStateFlow()

    private val _holidaysError = MutableStateFlow<String?>(null)
    val holidaysError: StateFlow<String?> = _holidaysError.asStateFlow()

    private val _lastHolidaySyncAt = MutableStateFlow(prefs.getLong(KEY_HOLIDAY_LAST_SYNC_AT, 0L))
    val lastHolidaySyncAt: StateFlow<Long> = _lastHolidaySyncAt.asStateFlow()

    // --- Search Query State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val selectedDateInfo: StateFlow<SelectedDateInfo> = combine(
        _selectedDay,
        _timeData
    ) { day, today ->
        DateFormatter.getSelectedDateInfo(day, today)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DateFormatter.getSelectedDateInfo(_selectedDay.value, initialTime)
    )

    // Regular Alarms Flow
    val regularAlarms: StateFlow<List<RegularAlarm>> = (alarmRepository?.allRegularAlarms
        ?: MutableStateFlow(emptyList()))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // All Date Alarms Flow
    val allDateAlarms: StateFlow<List<DateAlarm>> = (alarmRepository?.allDateAlarms
        ?: MutableStateFlow(emptyList()))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered date alarms for currently selected calendar day
    val selectedDayDateAlarms: StateFlow<List<DateAlarm>> = combine(
        _selectedDay,
        allDateAlarms
    ) { day, alarms ->
        alarms.filter { it.year == day.year && it.month == day.month && it.day == day.day }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // All Notes Flow
    val allNotes: StateFlow<List<DateNote>> = (noteRepository?.allNotes
        ?: MutableStateFlow(emptyList()))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered notes for currently selected calendar day
    val selectedDayNotes: StateFlow<List<DateNote>> = combine(
        _selectedDay,
        allNotes
    ) { day, notes ->
        notes.filter { it.year == day.year && it.month == day.month && it.day == day.day }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Set of "year-month-day" strings that have alarms
    val daysWithAlarms: StateFlow<Set<String>> = allDateAlarms.map { alarms ->
        alarms.map { "${it.year}-${it.month}-${it.day}" }.toSet()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    // Set of "year-month-day" strings that have notes
    val daysWithNotes: StateFlow<Set<String>> = allNotes.map { notes ->
        notes.map { "${it.year}-${it.month}-${it.day}" }.toSet()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    // --- Search Filtered Flows ---
    val filteredRegularAlarms: StateFlow<List<RegularAlarm>> = combine(
        regularAlarms,
        _searchQuery
    ) { alarms, query ->
        if (query.isBlank()) alarms else {
            val q = query.trim().lowercase()
            alarms.filter { alarm ->
                alarm.label.lowercase().contains(q) ||
                "${alarm.hour}:${alarm.minute}".contains(q) ||
                String.format("%02d:%02d", alarm.hour, alarm.minute).contains(q)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredDateAlarms: StateFlow<List<DateAlarm>> = combine(
        allDateAlarms,
        _searchQuery
    ) { alarms, query ->
        if (query.isBlank()) alarms else {
            val q = query.trim().lowercase()
            alarms.filter { alarm ->
                alarm.title.lowercase().contains(q) ||
                alarm.description.lowercase().contains(q) ||
                "${alarm.year}".contains(q) ||
                "${alarm.day}".contains(q) ||
                "${alarm.hour}:${alarm.minute}".contains(q) ||
                String.format("%02d:%02d", alarm.hour, alarm.minute).contains(q)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredNotes: StateFlow<List<DateNote>> = combine(
        allNotes,
        _searchQuery
    ) { notes, query ->
        if (query.isBlank()) notes else {
            val q = query.trim().lowercase()
            notes.filter { note ->
                note.title.lowercase().contains(q) ||
                note.content.lowercase().contains(q) ||
                "${note.year}".contains(q) ||
                "${note.day}".contains(q) ||
                "${note.hour}:${note.minute}".contains(q) ||
                String.format("%02d:%02d", note.hour, note.minute).contains(q)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val holidays: StateFlow<List<HolidayEntity>> = _selectedCountryCode
        .flatMapLatest { code ->
            if (code.isBlank() || holidayRepository == null) {
                flowOf(emptyList())
            } else {
                holidayRepository.observeHolidays(code)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        startClockTicker()
        if (_selectedCountryCode.value.isNotBlank()) {
            val isStale = com.example.data.repository.HolidayRepository.isSyncStale(
                lastSyncMillis = _lastHolidaySyncAt.value,
                lastSyncCountry = prefs.getString(KEY_HOLIDAY_LAST_SYNC_COUNTRY, null),
                currentCountry = _selectedCountryCode.value
            )
            if (_holidayRemindersEnabled.value || isStale) {
                ensureHolidaysLoaded()
            }
        }
    }

    private fun startClockTicker() {
        viewModelScope.launch {
            while (isActive) {
                val tz = TimeZone.getTimeZone(_selectedTimeZoneId.value)
                val current = DateFormatter.extractTimeData(tz)
                _timeData.value = current
                delay(500)
            }
        }
    }

    fun setTimeZone(timeZoneId: String) {
        _selectedTimeZoneId.value = timeZoneId
        prefs.edit().putString(KEY_TIMEZONE, timeZoneId).apply()
        val tz = TimeZone.getTimeZone(timeZoneId)
        val current = DateFormatter.extractTimeData(tz)
        _timeData.value = current
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    fun setActiveTab(tab: Int) {
        _activeTab.value = tab
        if (tab == 5) {
            ensureHolidaysLoaded()
        }
    }

    fun onPreviousMonth() {
        val currentMonth = _displayedMonth.value
        val currentYear = _displayedYear.value
        if (currentMonth == 0) {
            _displayedMonth.value = 11
            _displayedYear.value = currentYear - 1
        } else {
            _displayedMonth.value = currentMonth - 1
        }
    }

    fun onNextMonth() {
        val currentMonth = _displayedMonth.value
        val currentYear = _displayedYear.value
        if (currentMonth == 11) {
            _displayedMonth.value = 0
            _displayedYear.value = currentYear + 1
        } else {
            _displayedMonth.value = currentMonth + 1
        }
    }

    fun onResetToToday() {
        val today = _timeData.value
        _displayedYear.value = today.year
        _displayedMonth.value = today.month
        _selectedDay.value = CalendarDay(
            year = today.year,
            month = today.month,
            day = today.dayOfMonth,
            isCurrentMonth = true,
            isToday = true,
            isSelected = true
        )
    }

    fun onSelectDay(day: CalendarDay) {
        _selectedDay.value = day.copy(isSelected = true)
        if (!day.isCurrentMonth) {
            _displayedYear.value = day.year
            _displayedMonth.value = day.month
        }
    }

    fun toggle24Hour() {
        _is24Hour.value = !_is24Hour.value
    }

    fun toggleLanguage() {
        _isBengali.value = !_isBengali.value
    }

    // --- Regular Alarm Operations ---

    fun addRegularAlarm(hour: Int, minute: Int, label: String, repeatMask: Int, vibrate: Boolean) {
        viewModelScope.launch {
            val repo = alarmRepository ?: return@launch
            val newAlarm = RegularAlarm(
                hour = hour,
                minute = minute,
                label = label,
                repeatDaysMask = repeatMask,
                isEnabled = true,
                vibrate = vibrate
            )
            val id = repo.insertRegularAlarm(newAlarm)
            scheduler.scheduleRegularAlarm(newAlarm.copy(id = id))
        }
    }

    fun toggleRegularAlarm(alarm: RegularAlarm, isEnabled: Boolean) {
        viewModelScope.launch {
            val repo = alarmRepository ?: return@launch
            val updated = alarm.copy(isEnabled = isEnabled)
            repo.updateRegularAlarm(updated)
            if (isEnabled) {
                scheduler.scheduleRegularAlarm(updated)
            } else {
                scheduler.cancelRegularAlarm(updated)
            }
        }
    }

    fun deleteRegularAlarm(alarm: RegularAlarm) {
        viewModelScope.launch {
            val repo = alarmRepository ?: return@launch
            scheduler.cancelRegularAlarm(alarm)
            repo.deleteRegularAlarm(alarm.id)
        }
    }

    // --- Date Alarm Operations ---

    fun addDateAlarm(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        title: String,
        description: String,
        vibrate: Boolean
    ) {
        viewModelScope.launch {
            val repo = alarmRepository ?: return@launch
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val newAlarm = DateAlarm(
                year = year,
                month = month,
                day = day,
                hour = hour,
                minute = minute,
                title = title,
                description = description,
                triggerTimeMillis = cal.timeInMillis,
                isEnabled = true,
                isCompleted = false,
                vibrate = vibrate
            )

            val id = repo.insertDateAlarm(newAlarm)
            scheduler.scheduleDateAlarm(newAlarm.copy(id = id))
        }
    }

    fun toggleDateAlarm(alarm: DateAlarm, isEnabled: Boolean) {
        viewModelScope.launch {
            val repo = alarmRepository ?: return@launch
            val updated = alarm.copy(isEnabled = isEnabled)
            repo.updateDateAlarm(updated)
            if (isEnabled) {
                scheduler.scheduleDateAlarm(updated)
            } else {
                scheduler.cancelDateAlarm(updated)
            }
        }
    }

    fun deleteDateAlarm(alarm: DateAlarm) {
        viewModelScope.launch {
            val repo = alarmRepository ?: return@launch
            scheduler.cancelDateAlarm(alarm)
            repo.deleteDateAlarm(alarm.id)
        }
    }

    // --- Date Note Operations ---

    fun addNote(year: Int, month: Int, day: Int, hour: Int, minute: Int, title: String, content: String) {
        viewModelScope.launch {
            val repo = noteRepository ?: return@launch
            val newNote = DateNote(
                year = year,
                month = month,
                day = day,
                hour = hour,
                minute = minute,
                title = title,
                content = content
            )
            repo.insertNote(newNote)
        }
    }

    fun updateNote(
        existingNote: DateNote,
        newYear: Int,
        newMonth: Int,
        newDay: Int,
        newHour: Int,
        newMinute: Int,
        newTitle: String,
        newContent: String
    ) {
        viewModelScope.launch {
            val repo = noteRepository ?: return@launch
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()
            val updated = existingNote.copy(
                year = newYear,
                month = newMonth,
                day = newDay,
                hour = newHour,
                minute = newMinute,
                title = newTitle,
                content = newContent,
                updatedAt = now,
                updatedHour = cal.get(Calendar.HOUR_OF_DAY),
                updatedMinute = cal.get(Calendar.MINUTE),
                updatedYear = cal.get(Calendar.YEAR),
                updatedMonth = cal.get(Calendar.MONTH),
                updatedDay = cal.get(Calendar.DAY_OF_MONTH),
                editCount = existingNote.editCount + 1
            )
            repo.updateNote(updated)
        }
    }

    fun deleteNote(note: DateNote) {
        viewModelScope.launch {
            val repo = noteRepository ?: return@launch
            repo.deleteNote(note.id)
        }
    }

    // --- Government Holiday & Reminder Operations ---

    fun ensureHolidaysLoaded() {
        val repo = holidayRepository ?: return
        val isStale = com.example.data.repository.HolidayRepository.isSyncStale(
            lastSyncMillis = _lastHolidaySyncAt.value,
            lastSyncCountry = prefs.getString(KEY_HOLIDAY_LAST_SYNC_COUNTRY, null),
            currentCountry = _selectedCountryCode.value
        )
        if (_availableCountries.value.isEmpty() || _selectedCountryCode.value.isBlank() || holidays.value.isEmpty() || isStale) {
            loadCountriesAndSyncHolidays(forceCountries = _availableCountries.value.isEmpty())
        }
    }

    fun loadCountriesAndSyncHolidays(forceCountries: Boolean = false) {
        val repo = holidayRepository ?: return
        if (_countriesLoading.value || _holidaysLoading.value) return

        viewModelScope.launch {
            _holidaysError.value = null
            if (forceCountries || _availableCountries.value.isEmpty()) {
                _countriesLoading.value = true
                val countriesResult = repo.fetchAvailableCountries()
                _countriesLoading.value = false

                countriesResult.onSuccess { countries ->
                    _availableCountries.value = countries
                    if (_selectedCountryCode.value.isBlank() && countries.isNotEmpty()) {
                        val deviceCountry = Locale.getDefault().country.uppercase(Locale.US)
                        val picked = countries.firstOrNull { it.countryCode.equals(deviceCountry, ignoreCase = true) }
                            ?: countries.firstOrNull { it.countryCode.equals("US", ignoreCase = true) }
                            ?: countries.first()
                        persistCountrySelection(picked.countryCode, picked.name)
                    }
                }.onFailure { err ->
                    if (_selectedCountryCode.value.isBlank()) {
                        _holidaysError.value = err.localizedMessage ?: "Failed to load country list"
                        return@launch
                    }
                }
            }

            val code = _selectedCountryCode.value
            val name = _selectedCountryName.value.ifBlank { code }
            if (code.isNotBlank()) {
                syncHolidaysInternal(code, name)
            }
        }
    }

    fun refreshHolidays() {
        loadCountriesAndSyncHolidays(forceCountries = _availableCountries.value.isEmpty())
    }

    fun selectHolidayCountry(countryCode: String, countryName: String) {
        val repo = holidayRepository ?: return
        val oldCode = _selectedCountryCode.value
        if (oldCode.equals(countryCode, ignoreCase = true) && holidays.value.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            if (oldCode.isNotBlank()) {
                val oldHolidays = repo.getHolidaysOnce(oldCode)
                scheduler.cancelHolidayReminders(oldHolidays)
            }
            persistCountrySelection(countryCode, countryName)
            syncHolidaysInternal(countryCode, countryName)
        }
    }

    private fun persistCountrySelection(countryCode: String, countryName: String) {
        _selectedCountryCode.value = countryCode
        _selectedCountryName.value = countryName
        prefs.edit()
            .putString(KEY_HOLIDAY_COUNTRY_CODE, countryCode)
            .putString(KEY_HOLIDAY_COUNTRY_NAME, countryName)
            .apply()
    }

    private suspend fun syncHolidaysInternal(countryCode: String, countryName: String) {
        val repo = holidayRepository ?: return
        _holidaysLoading.value = true
        _holidaysError.value = null
        val result = repo.syncAndReconcileReminders(
            countryCode = countryCode,
            countryName = countryName,
            scheduler = scheduler,
            remindersEnabled = _holidayRemindersEnabled.value,
            daysBefore = _holidayRemindDaysBefore.value,
            hour = _holidayRemindHour.value,
            minute = _holidayRemindMinute.value
        )
        _holidaysLoading.value = false

        result.onSuccess {
            val now = System.currentTimeMillis()
            _lastHolidaySyncAt.value = now
            prefs.edit()
                .putLong(KEY_HOLIDAY_LAST_SYNC_AT, now)
                .putString(KEY_HOLIDAY_LAST_SYNC_COUNTRY, countryCode)
                .apply()
        }.onFailure { err ->
            _holidaysError.value = err.localizedMessage ?: "Could not fetch holidays"
        }
    }

    fun setHolidayRemindersEnabled(enabled: Boolean) {
        _holidayRemindersEnabled.value = enabled
        prefs.edit().putBoolean(KEY_HOLIDAY_REMINDERS_ENABLED, enabled).apply()
        val repo = holidayRepository ?: return
        val code = _selectedCountryCode.value
        if (code.isBlank()) return

        viewModelScope.launch {
            val list = repo.getHolidaysOnce(code)
            if (enabled) {
                scheduler.scheduleHolidayReminders(
                    holidays = list,
                    daysBefore = _holidayRemindDaysBefore.value,
                    hour = _holidayRemindHour.value,
                    minute = _holidayRemindMinute.value
                )
            } else {
                scheduler.cancelHolidayReminders(list)
            }
        }
    }

    fun setHolidayRemindDaysBefore(days: Int) {
        val clamped = days.coerceIn(0, 30)
        _holidayRemindDaysBefore.value = clamped
        prefs.edit().putInt(KEY_HOLIDAY_REMIND_DAYS_BEFORE, clamped).apply()
        rescheduleActiveHolidayReminders()
    }

    fun setHolidayRemindTime(hour: Int, minute: Int) {
        val safeHour = hour.coerceIn(0, 23)
        val safeMinute = minute.coerceIn(0, 59)
        _holidayRemindHour.value = safeHour
        _holidayRemindMinute.value = safeMinute
        prefs.edit()
            .putInt(KEY_HOLIDAY_REMIND_HOUR, safeHour)
            .putInt(KEY_HOLIDAY_REMIND_MINUTE, safeMinute)
            .apply()
        rescheduleActiveHolidayReminders()
    }

    fun toggleSingleHolidayReminder(holiday: HolidayEntity, enabled: Boolean) {
        val repo = holidayRepository ?: return
        viewModelScope.launch {
            repo.toggleHolidayReminder(holiday.id, enabled)
            val updated = holiday.copy(reminderEnabled = enabled)
            if (enabled && _holidayRemindersEnabled.value) {
                scheduler.scheduleHolidayReminder(
                    holiday = updated,
                    daysBefore = _holidayRemindDaysBefore.value,
                    hour = _holidayRemindHour.value,
                    minute = _holidayRemindMinute.value
                )
            } else {
                scheduler.cancelHolidayReminder(updated)
            }
        }
    }

    private fun rescheduleActiveHolidayReminders() {
        val repo = holidayRepository ?: return
        val code = _selectedCountryCode.value
        if (code.isBlank() || !_holidayRemindersEnabled.value) return

        viewModelScope.launch {
            val list = repo.getHolidaysOnce(code)
            scheduler.scheduleHolidayReminders(
                holidays = list,
                daysBefore = _holidayRemindDaysBefore.value,
                hour = _holidayRemindHour.value,
                minute = _holidayRemindMinute.value
            )
        }
    }
}
