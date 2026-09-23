package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.DateTimeApplication
import com.example.alarm.AlarmScheduler
import com.example.data.model.DateAlarm
import com.example.data.model.DateNote
import com.example.data.model.RegularAlarm
import com.example.model.CalendarDay
import com.example.model.SelectedDateInfo
import com.example.model.TimeData
import com.example.util.DateFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class DateTimeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as? DateTimeApplication
    private val alarmRepository = app?.alarmRepository
    private val noteRepository = app?.noteRepository
    private val scheduler = AlarmScheduler(application)

    private val prefs = application.getSharedPreferences("datetime_prefs", Context.MODE_PRIVATE)
    private val KEY_TIMEZONE = "pref_user_timezone"

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

    // 0 = Calendar & Clock, 1 = Regular Alarms, 2 = Date Alarms, 3 = Notes
    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

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

    init {
        startClockTicker()
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
}
