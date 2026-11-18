package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.DateNote
import com.example.model.CalendarDay
import com.example.ui.components.CalendarCard
import com.example.ui.components.ClockCard
import com.example.ui.components.CompassSection
import com.example.ui.components.DateAlarmListSection
import com.example.ui.components.DateDetailsCard
import com.example.ui.components.DateNotesSection
import com.example.ui.components.HolidaysSection
import com.example.ui.components.RegularAlarmListSection
import com.example.ui.components.SearchBarHeader
import com.example.ui.components.SearchResultsSection
import com.example.ui.dialogs.AboutDeveloperDialog
import com.example.ui.dialogs.AddDateAlarmDialog
import com.example.ui.dialogs.AddEditNoteDialog
import com.example.ui.dialogs.AddRegularAlarmDialog
import com.example.ui.dialogs.CountrySelectorDialog
import com.example.ui.dialogs.PrivacyDialog
import com.example.ui.dialogs.TimeZoneSelectorDialog
import com.example.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeScreen(
    viewModel: DateTimeViewModel,
    modifier: Modifier = Modifier
) {
    val timeData by viewModel.timeData.collectAsStateWithLifecycle()
    val selectedTimeZoneId by viewModel.selectedTimeZoneId.collectAsStateWithLifecycle()
    val displayedYear by viewModel.displayedYear.collectAsStateWithLifecycle()
    val displayedMonth by viewModel.displayedMonth.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    val is24Hour by viewModel.is24Hour.collectAsStateWithLifecycle()
    val isBengali by viewModel.isBengali.collectAsStateWithLifecycle()
    val selectedDateInfo by viewModel.selectedDateInfo.collectAsStateWithLifecycle()

    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()

    val regularAlarms by viewModel.regularAlarms.collectAsStateWithLifecycle()
    val allDateAlarms by viewModel.allDateAlarms.collectAsStateWithLifecycle()
    val allNotes by viewModel.allNotes.collectAsStateWithLifecycle()

    // Search state & filtered results
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filteredRegularAlarms by viewModel.filteredRegularAlarms.collectAsStateWithLifecycle()
    val filteredDateAlarms by viewModel.filteredDateAlarms.collectAsStateWithLifecycle()
    val filteredNotes by viewModel.filteredNotes.collectAsStateWithLifecycle()

    val daysWithAlarms by viewModel.daysWithAlarms.collectAsStateWithLifecycle()
    val daysWithNotes by viewModel.daysWithNotes.collectAsStateWithLifecycle()

    // Government Holiday state
    val selectedCountryCode by viewModel.selectedCountryCode.collectAsStateWithLifecycle()
    val selectedCountryName by viewModel.selectedCountryName.collectAsStateWithLifecycle()
    val holidays by viewModel.holidays.collectAsStateWithLifecycle()
    val availableCountries by viewModel.availableCountries.collectAsStateWithLifecycle()
    val countriesLoading by viewModel.countriesLoading.collectAsStateWithLifecycle()
    val holidaysLoading by viewModel.holidaysLoading.collectAsStateWithLifecycle()
    val holidaysError by viewModel.holidaysError.collectAsStateWithLifecycle()
    val holidayRemindersEnabled by viewModel.holidayRemindersEnabled.collectAsStateWithLifecycle()
    val holidayRemindDaysBefore by viewModel.holidayRemindDaysBefore.collectAsStateWithLifecycle()
    val holidayRemindHour by viewModel.holidayRemindHour.collectAsStateWithLifecycle()
    val holidayRemindMinute by viewModel.holidayRemindMinute.collectAsStateWithLifecycle()
    val lastHolidaySyncAt by viewModel.lastHolidaySyncAt.collectAsStateWithLifecycle()

    var showMenu by remember { mutableStateOf(false) }
    var showTimeZoneDialog by remember { mutableStateOf(false) }
    var showCountryDialog by remember { mutableStateOf(false) }
    var isSearchVisible by remember { mutableStateOf(false) }
    var showAddRegularAlarmDialog by remember { mutableStateOf(false) }
    var showAddDateAlarmDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<DateNote?>(null) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val currentActiveTab by rememberUpdatedState(activeTab)

    val pagerState = rememberPagerState(
        initialPage = activeTab.coerceIn(0, 5),
        pageCount = { 6 }
    )

    LaunchedEffect(activeTab) {
        val target = activeTab.coerceIn(0, 5)
        if (pagerState.currentPage != target) {
            pagerState.animateScrollToPage(target)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            if (page != currentActiveTab) {
                viewModel.setActiveTab(page)
            }
        }
    }

    BackHandler(enabled = isSearchVisible || searchQuery.isNotBlank() || activeTab != 0) {
        if (isSearchVisible || searchQuery.isNotBlank()) {
            isSearchVisible = false
            viewModel.clearSearchQuery()
        } else if (activeTab != 0) {
            viewModel.setActiveTab(0)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_icon),
                            contentDescription = if (isBengali) "অ্যাপ লোগো" else "App Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .testTag("app_top_bar_logo")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBengali) "তারিখ, সময় ও ক্যালেন্ডার" else "Date, Time & Calendar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // Search toggle icon
                    IconButton(
                        onClick = {
                            isSearchVisible = !isSearchVisible
                            if (!isSearchVisible) {
                                viewModel.clearSearchQuery()
                            }
                        },
                        modifier = Modifier.testTag("search_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isSearchVisible || searchQuery.isNotEmpty()) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isSearchVisible || searchQuery.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // 3-bar Menu
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (is24Hour) "১২ ঘণ্টার ঘড়ি ব্যবহার করুন" else "২৪ ঘণ্টার ঘড়ি ব্যবহার করুন") },
                                onClick = {
                                    showMenu = false
                                    viewModel.toggle24Hour()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Schedule, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (isBengali) "Switch to English" else "বাংলা ভাষায় দেখুন") },
                                onClick = {
                                    showMenu = false
                                    viewModel.toggleLanguage()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Public, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (isBengali) "টাইম জোন পরিবর্তন (TimeZone)" else "Change TimeZone (GMT)") },
                                onClick = {
                                    showMenu = false
                                    showTimeZoneDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Public, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (isBengali) "রেগুলার অ্যালার্ম" else "Regular Alarms") },
                                onClick = {
                                    showMenu = false
                                    viewModel.setActiveTab(1)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Alarm, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (isBengali) "নির্দিষ্ট তারিখের অ্যালার্ম" else "Date-Specific Alarms") },
                                onClick = {
                                    showMenu = false
                                    viewModel.setActiveTab(2)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.NotificationImportant, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (isBengali) "তারিখের নোটস (Notes)" else "Date Notes") },
                                onClick = {
                                    showMenu = false
                                    viewModel.setActiveTab(3)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.EditNote, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (isBengali) "কম্পাস (Compass)" else "Compass") },
                                onClick = {
                                    showMenu = false
                                    viewModel.setActiveTab(4)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Explore, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (isBengali) "সরকারি ছুটি ও রিমাইন্ডার" else "Govt Holiday Reminders") },
                                onClick = {
                                    showMenu = false
                                    viewModel.setActiveTab(5)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.EventAvailable, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (isBengali) "অ্যাপ ও ডেভেলপার তথ্য" else "About & Developer") },
                                onClick = {
                                    showMenu = false
                                    showAboutDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Info, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (isBengali) "প্রাইভেসি ও ওপেন সোর্স" else "Privacy & Local Database") },
                                onClick = {
                                    showMenu = false
                                    showPrivacyDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Security, contentDescription = null)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.testTag("app_top_bar")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Local Search Bar Header
            AnimatedVisibility(visible = isSearchVisible || searchQuery.isNotEmpty()) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    SearchBarHeader(
                        searchQuery = searchQuery,
                        onQueryChange = viewModel::setSearchQuery,
                        onClearQuery = viewModel::clearSearchQuery,
                        isBengali = isBengali
                    )
                }
            }

            if (searchQuery.isNotBlank()) {
                // Unified Search Results View with dedicated full-page scroll state
                val searchScrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(searchScrollState)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 640.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SearchResultsSection(
                            searchQuery = searchQuery,
                            filteredRegularAlarms = filteredRegularAlarms,
                            filteredDateAlarms = filteredDateAlarms,
                            filteredNotes = filteredNotes,
                            is24Hour = is24Hour,
                            isBengali = isBengali,
                            onToggleRegularAlarm = viewModel::toggleRegularAlarm,
                            onDeleteRegularAlarm = viewModel::deleteRegularAlarm,
                            onToggleDateAlarm = viewModel::toggleDateAlarm,
                            onDeleteDateAlarm = viewModel::deleteDateAlarm,
                            onEditNote = { note -> editingNote = note },
                            onDeleteNote = viewModel::deleteNote
                        )
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            } else {
                // Navigation Primary Tabs (scrollable on compact screens, full-width on wider screens)
                val selectedTabIdx = pagerState.currentPage.coerceIn(0, 5)
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val useScrollableTabs = maxWidth < 760.dp
                    val tabItems: @Composable () -> Unit = {
                        Tab(
                            selected = selectedTabIdx == 0,
                            onClick = { viewModel.setActiveTab(0) },
                            text = {
                                Text(
                                    text = if (isBengali) "ক্যালেন্ডার" else "Calendar",
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.testTag("tab_calendar")
                        )
                        Tab(
                            selected = selectedTabIdx == 1,
                            onClick = { viewModel.setActiveTab(1) },
                            text = {
                                Text(
                                    text = if (isBengali) "রেগুলার অ্যালার্ম" else "Alarms",
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            icon = { Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.testTag("tab_regular_alarms")
                        )
                        Tab(
                            selected = selectedTabIdx == 2,
                            onClick = { viewModel.setActiveTab(2) },
                            text = {
                                Text(
                                    text = if (isBengali) "ডেট অ্যালার্ম" else "Date Alarms",
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            icon = { Icon(Icons.Default.NotificationImportant, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.testTag("tab_date_alarms")
                        )
                        Tab(
                            selected = selectedTabIdx == 3,
                            onClick = { viewModel.setActiveTab(3) },
                            text = {
                                Text(
                                    text = if (isBengali) "নোটস" else "Notes",
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            icon = { Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.testTag("tab_notes")
                        )
                        Tab(
                            selected = selectedTabIdx == 4,
                            onClick = { viewModel.setActiveTab(4) },
                            text = {
                                Text(
                                    text = if (isBengali) "কম্পাস" else "Compass",
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            icon = { Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.testTag("tab_compass")
                        )
                        Tab(
                            selected = selectedTabIdx == 5,
                            onClick = { viewModel.setActiveTab(5) },
                            text = {
                                Text(
                                    text = if (isBengali) "ছুটির দিন" else "Holidays",
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            icon = { Icon(Icons.Default.EventAvailable, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.testTag("tab_holidays")
                        )
                    }

                    if (useScrollableTabs) {
                        PrimaryScrollableTabRow(
                            selectedTabIndex = selectedTabIdx,
                            edgePadding = 8.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tabItems()
                        }
                    } else {
                        PrimaryTabRow(
                            selectedTabIndex = selectedTabIdx,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tabItems()
                        }
                    }
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isExpanded = maxWidth >= 840.dp
                    val scrollStateTab0Left = rememberScrollState()
                    val scrollStateTab0Right = rememberScrollState()
                    val scrollStateTab1 = rememberScrollState()
                    val scrollStateTab2 = rememberScrollState()
                    val scrollStateTab3 = rememberScrollState()
                    val scrollStateTab4 = rememberScrollState()
                    val scrollStateTab5 = rememberScrollState()

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("main_horizontal_pager")
                    ) { page ->
                        when (page) {
                        0 -> {
                            // TAB 0: Clock & Calendar Dashboard
                            if (isExpanded) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 24.dp, vertical = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .verticalScroll(scrollStateTab0Left),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        ClockCard(
                                            timeData = timeData,
                                            is24Hour = is24Hour,
                                            isBengali = isBengali,
                                            onToggle24Hour = viewModel::toggle24Hour,
                                            onToggleLanguage = viewModel::toggleLanguage,
                                            onOpenTimeZoneSelector = { showTimeZoneDialog = true }
                                        )

                                        DateDetailsCard(
                                            dateInfo = selectedDateInfo,
                                            isBengali = isBengali
                                        )

                                        EpochAndWeekCard(
                                            timeData = timeData,
                                            isBengali = isBengali
                                        )
                                        Spacer(modifier = Modifier.height(72.dp))
                                    }

                                    Column(
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .fillMaxHeight()
                                            .verticalScroll(scrollStateTab0Right),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        CalendarCard(
                                            displayedYear = displayedYear,
                                            displayedMonth = displayedMonth,
                                            todayTime = timeData,
                                            selectedDay = selectedDay,
                                            daysWithAlarms = daysWithAlarms,
                                            daysWithNotes = daysWithNotes,
                                            isBengali = isBengali,
                                            onPreviousMonth = viewModel::onPreviousMonth,
                                            onNextMonth = viewModel::onNextMonth,
                                            onResetToToday = viewModel::onResetToToday,
                                            onSelectDay = viewModel::onSelectDay
                                        )
                                        Spacer(modifier = Modifier.height(72.dp))
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(scrollStateTab0Left)
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .widthIn(max = 640.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        ClockCard(
                                            timeData = timeData,
                                            is24Hour = is24Hour,
                                            isBengali = isBengali,
                                            onToggle24Hour = viewModel::toggle24Hour,
                                            onToggleLanguage = viewModel::toggleLanguage,
                                            onOpenTimeZoneSelector = { showTimeZoneDialog = true }
                                        )

                                        CalendarCard(
                                            displayedYear = displayedYear,
                                            displayedMonth = displayedMonth,
                                            todayTime = timeData,
                                            selectedDay = selectedDay,
                                            daysWithAlarms = daysWithAlarms,
                                            daysWithNotes = daysWithNotes,
                                            isBengali = isBengali,
                                            onPreviousMonth = viewModel::onPreviousMonth,
                                            onNextMonth = viewModel::onNextMonth,
                                            onResetToToday = viewModel::onResetToToday,
                                            onSelectDay = viewModel::onSelectDay
                                        )

                                        DateDetailsCard(
                                            dateInfo = selectedDateInfo,
                                            isBengali = isBengali
                                        )

                                        EpochAndWeekCard(
                                            timeData = timeData,
                                            isBengali = isBengali
                                        )

                                        Spacer(modifier = Modifier.height(72.dp))
                                    }
                                }
                            }
                        }

                        1 -> {
                            // TAB 1: Regular Alarms Management
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollStateTab1)
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .widthIn(max = 640.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    RegularAlarmListSection(
                                        alarms = regularAlarms,
                                        is24Hour = is24Hour,
                                        isBengali = isBengali,
                                        onToggleAlarm = viewModel::toggleRegularAlarm,
                                        onDeleteAlarm = viewModel::deleteRegularAlarm,
                                        onAddNewAlarm = { showAddRegularAlarmDialog = true }
                                    )
                                    Spacer(modifier = Modifier.height(72.dp))
                                }
                            }
                        }

                        2 -> {
                            // TAB 2: All Date Alarms
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollStateTab2)
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .widthIn(max = 640.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    DateAlarmListSection(
                                        alarms = allDateAlarms,
                                        selectedYear = selectedDay.year,
                                        selectedMonth = selectedDay.month,
                                        selectedDay = selectedDay.day,
                                        is24Hour = is24Hour,
                                        isBengali = isBengali,
                                        onToggleAlarm = viewModel::toggleDateAlarm,
                                        onDeleteAlarm = viewModel::deleteDateAlarm,
                                        onAddNewDateAlarm = { showAddDateAlarmDialog = true },
                                        isAllDatesMode = true
                                    )
                                    Spacer(modifier = Modifier.height(72.dp))
                                }
                            }
                        }

                        3 -> {
                            // TAB 3: All Notes
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollStateTab3)
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .widthIn(max = 640.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    DateNotesSection(
                                        notes = allNotes,
                                        selectedYear = selectedDay.year,
                                        selectedMonth = selectedDay.month,
                                        selectedDay = selectedDay.day,
                                        is24Hour = is24Hour,
                                        isBengali = isBengali,
                                        onAddNewNote = { showAddNoteDialog = true },
                                        onEditNote = { note -> editingNote = note },
                                        onDeleteNote = viewModel::deleteNote,
                                        allNotes = allNotes,
                                        isAllDatesMode = true,
                                        onSelectDateOnCalendar = { y, m, d ->
                                            viewModel.onSelectDay(
                                                CalendarDay(
                                                    year = y,
                                                    month = m,
                                                    day = d,
                                                    isCurrentMonth = (m == displayedMonth && y == displayedYear),
                                                    isToday = (y == timeData.year && m == timeData.month && d == timeData.dayOfMonth),
                                                    isSelected = true
                                                )
                                            )
                                            viewModel.setActiveTab(0)
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(72.dp))
                                }
                            }
                        }

                        4 -> {
                            // TAB 4: Digital Compass
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollStateTab4)
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .widthIn(max = 760.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    CompassSection(isBengali = isBengali)
                                    Spacer(modifier = Modifier.height(72.dp))
                                }
                            }
                        }

                        5 -> {
                            // TAB 5: Government Holidays & Reminders
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollStateTab5)
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .widthIn(max = 680.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    HolidaysSection(
                                        selectedCountryCode = selectedCountryCode,
                                        selectedCountryName = selectedCountryName,
                                        holidays = holidays,
                                        remindersEnabled = holidayRemindersEnabled,
                                        remindDaysBefore = holidayRemindDaysBefore,
                                        remindHour = holidayRemindHour,
                                        remindMinute = holidayRemindMinute,
                                        isLoading = holidaysLoading || countriesLoading,
                                        errorMessage = holidaysError,
                                        is24Hour = is24Hour,
                                        isBengali = isBengali,
                                        onOpenCountryPicker = {
                                            viewModel.ensureHolidaysLoaded()
                                            showCountryDialog = true
                                        },
                                        onRefresh = viewModel::refreshHolidays,
                                        onToggleRemindersEnabled = viewModel::setHolidayRemindersEnabled,
                                        onChangeLeadDays = viewModel::setHolidayRemindDaysBefore,
                                        onChangeRemindTime = viewModel::setHolidayRemindTime,
                                        onToggleSingleHoliday = viewModel::toggleSingleHolidayReminder,
                                        lastSyncedAtMillis = lastHolidaySyncAt
                                    )
                                    Spacer(modifier = Modifier.height(72.dp))
                                }
                            }
                        }
                        }
                    }
                }
            }
        }
    }

    // --- Dialogs ---

    if (showAddRegularAlarmDialog) {
        AddRegularAlarmDialog(
            isBengali = isBengali,
            onDismiss = { showAddRegularAlarmDialog = false },
            onSave = { hour, minute, label, repeatMask, vibrate ->
                viewModel.addRegularAlarm(hour, minute, label, repeatMask, vibrate)
                showAddRegularAlarmDialog = false
            }
        )
    }

    if (showAddDateAlarmDialog) {
        AddDateAlarmDialog(
            initialYear = selectedDay.year,
            initialMonth = selectedDay.month,
            initialDay = selectedDay.day,
            isBengali = isBengali,
            onDismiss = { showAddDateAlarmDialog = false },
            onSave = { year, month, day, hour, minute, title, desc, vibrate ->
                viewModel.addDateAlarm(year, month, day, hour, minute, title, desc, vibrate)
                showAddDateAlarmDialog = false
            }
        )
    }

    if (showAddNoteDialog) {
        AddEditNoteDialog(
            year = selectedDay.year,
            month = selectedDay.month,
            day = selectedDay.day,
            is24Hour = is24Hour,
            isBengali = isBengali,
            onDismiss = { showAddNoteDialog = false },
            onSave = { year, month, day, hour, minute, title, content ->
                viewModel.addNote(year, month, day, hour, minute, title, content)
                showAddNoteDialog = false
            }
        )
    }

    if (editingNote != null) {
        val noteToEdit = editingNote!!
        AddEditNoteDialog(
            year = noteToEdit.year,
            month = noteToEdit.month,
            day = noteToEdit.day,
            existingNote = noteToEdit,
            is24Hour = is24Hour,
            isBengali = isBengali,
            onDismiss = { editingNote = null },
            onSave = { year, month, day, hour, minute, title, content ->
                viewModel.updateNote(
                    existingNote = noteToEdit,
                    newYear = year,
                    newMonth = month,
                    newDay = day,
                    newHour = hour,
                    newMinute = minute,
                    newTitle = title,
                    newContent = content
                )
                editingNote = null
            }
        )
    }

    if (showAboutDialog) {
        AboutDeveloperDialog(
            isBengali = isBengali,
            onDismiss = { showAboutDialog = false }
        )
    }

    if (showPrivacyDialog) {
        PrivacyDialog(
            isBengali = isBengali,
            onDismiss = { showPrivacyDialog = false }
        )
    }

    if (showTimeZoneDialog) {
        TimeZoneSelectorDialog(
            currentTimeZoneId = selectedTimeZoneId,
            isBengali = isBengali,
            onDismiss = { showTimeZoneDialog = false },
            onSelectTimeZone = { tzId ->
                viewModel.setTimeZone(tzId)
                showTimeZoneDialog = false
            }
        )
    }

    if (showCountryDialog) {
        CountrySelectorDialog(
            availableCountries = availableCountries,
            selectedCountryCode = selectedCountryCode,
            isLoading = countriesLoading,
            errorMessage = holidaysError,
            isBengali = isBengali,
            onRetryLoad = { viewModel.loadCountriesAndSyncHolidays(forceCountries = true) },
            onSelectCountry = { code, name ->
                viewModel.selectHolidayCountry(code, name)
                showCountryDialog = false
            },
            onDismiss = { showCountryDialog = false }
        )
    }
}

@Composable
private fun EpochAndWeekCard(
    timeData: com.example.model.TimeData,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    val epochSec = timeData.timestamp / 1000
    val epochStr = DateFormatter.formatNumber(epochSec, isBengali)
    val weekStr = DateFormatter.formatNumber(timeData.weekOfYear, isBengali)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("epoch_week_card"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBengali) "বছরের সপ্তাহ নং: $weekStr" else "Week of Year: $weekStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Epoch: $epochStr",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
