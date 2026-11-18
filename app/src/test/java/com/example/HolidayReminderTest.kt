package com.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.alarm.AlarmScheduler
import com.example.data.model.HolidayEntity
import com.example.data.repository.HolidayRepository
import com.example.network.PublicHolidayDto
import com.example.ui.components.HolidaysSection
import com.example.ui.components.calculateDaysUntilHoliday
import com.example.ui.components.countryCodeToFlagEmoji
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HolidayReminderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `parseHolidayDto parses ISO date to zero-based Calendar month`() {
        val dto = PublicHolidayDto(
            date = "2027-12-25",
            localName = "Christmas Day",
            name = "Christmas Day",
            countryCode = "US",
            fixed = true,
            global = true,
            types = listOf("Public")
        )
        val entity = HolidayRepository.parseHolidayDto(dto, "US", "United States")
        assertNotNull(entity)
        assertEquals(2027, entity!!.year)
        assertEquals(11, entity.month) // December = 11
        assertEquals(25, entity.day)
        assertEquals("Public", entity.typesCsv)
        assertTrue(entity.isGlobal)

        val invalid = HolidayRepository.parseHolidayDto(dto.copy(date = "invalid-date"), "US", "United States")
        assertNull(invalid)
    }

    @Test
    fun `calculateHolidayTriggerMillis subtracts lead days accurately`() {
        val holiday = HolidayEntity(
            id = "US_2028-07-04_Independence Day",
            countryCode = "US",
            countryName = "United States",
            dateIso = "2028-07-04",
            year = 2028,
            month = Calendar.JULY,
            day = 4,
            name = "Independence Day",
            localName = "Independence Day",
            typesCsv = "Public",
            isGlobal = true,
            reminderEnabled = true
        )

        val triggerMillis = AlarmScheduler.calculateHolidayTriggerMillis(
            holiday = holiday,
            daysBefore = 3,
            hour = 9,
            minute = 30
        )

        val cal = Calendar.getInstance().apply { timeInMillis = triggerMillis }
        assertEquals(2028, cal.get(Calendar.YEAR))
        assertEquals(Calendar.JULY, cal.get(Calendar.MONTH))
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, cal.get(Calendar.MINUTE))
        assertEquals(0, cal.get(Calendar.SECOND))
    }

    @Test
    fun `countryCodeToFlagEmoji and calculateDaysUntilHoliday work as expected`() {
        assertEquals("🇺🇸", countryCodeToFlagEmoji("US"))
        assertEquals("🇯🇵", countryCodeToFlagEmoji("jp"))
        assertEquals("🏳️", countryCodeToFlagEmoji("INVALID"))

        val today = Calendar.getInstance()
        val futureCal = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 5) }
        val futureHoliday = HolidayEntity(
            id = "h_future",
            countryCode = "US",
            countryName = "United States",
            dateIso = "2099-01-01",
            year = futureCal.get(Calendar.YEAR),
            month = futureCal.get(Calendar.MONTH),
            day = futureCal.get(Calendar.DAY_OF_MONTH),
            name = "Future Holiday",
            localName = "Future Holiday",
            typesCsv = "Public",
            isGlobal = true
        )
        assertEquals(5L, calculateDaysUntilHoliday(futureHoliday, today))
    }

    @Test
    fun `isSyncStale and findRemovedHolidays handle updated government schedules`() {
        val now = 1_700_000_000_000L
        val freshTime = now - (2 * 60 * 60 * 1000L) // 2 hours ago
        val staleTime = now - HolidayRepository.STALE_SYNC_INTERVAL_MS - 1000L

        assertEquals(false, HolidayRepository.isSyncStale(freshTime, "US", "US", now))
        assertEquals(true, HolidayRepository.isSyncStale(staleTime, "US", "US", now))
        assertEquals(true, HolidayRepository.isSyncStale(freshTime, "US", "GB", now))
        assertEquals(true, HolidayRepository.isSyncStale(0L, "US", "US", now))

        val h1 = HolidayEntity("US_2027-01-01_New Year", "US", "United States", "2027-01-01", 2027, 0, 1, "New Year", "New Year", "Public", true)
        val hCancelled = HolidayEntity("US_2027-02-15_Old Holiday", "US", "United States", "2027-02-15", 2027, 1, 15, "Old Holiday", "Old Holiday", "Public", true)
        val hAdded = HolidayEntity("US_2027-06-19_New Holiday", "US", "United States", "2027-06-19", 2027, 5, 19, "New Holiday", "New Holiday", "Public", true)

        val removed = HolidayRepository.findRemovedHolidays(listOf(h1, hCancelled), listOf(h1, hAdded))
        assertEquals(listOf(hCancelled), removed)
    }

    @Test
    fun `holidaysSection displays upcoming holidays and triggers reminder controls`() {
        val futureCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 4) }
        val pastCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -10) }

        val futureHoliday = HolidayEntity(
            id = "US_future_holiday",
            countryCode = "US",
            countryName = "United States",
            dateIso = "2099-05-01",
            year = futureCal.get(Calendar.YEAR),
            month = futureCal.get(Calendar.MONTH),
            day = futureCal.get(Calendar.DAY_OF_MONTH),
            name = "Memorial Holiday",
            localName = "Memorial Holiday",
            typesCsv = "Public",
            isGlobal = true,
            reminderEnabled = true
        )
        val pastHoliday = HolidayEntity(
            id = "US_past_holiday",
            countryCode = "US",
            countryName = "United States",
            dateIso = "2020-01-01",
            year = pastCal.get(Calendar.YEAR),
            month = pastCal.get(Calendar.MONTH),
            day = pastCal.get(Calendar.DAY_OF_MONTH),
            name = "Old Past Holiday",
            localName = "Old Past Holiday",
            typesCsv = "Public",
            isGlobal = true,
            reminderEnabled = true
        )

        var countryPickerOpened = false
        var selectedLeadDays = -1
        var toggledSingleId: String? = null
        var toggledSingleState = true

        composeTestRule.setContent {
            MaterialTheme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    HolidaysSection(
                        selectedCountryCode = "US",
                        selectedCountryName = "United States",
                        holidays = listOf(pastHoliday, futureHoliday),
                        remindersEnabled = true,
                        remindDaysBefore = 1,
                        remindHour = 9,
                        remindMinute = 0,
                        isLoading = false,
                        errorMessage = null,
                        is24Hour = false,
                        isBengali = false,
                        onOpenCountryPicker = { countryPickerOpened = true },
                        onRefresh = {},
                        onToggleRemindersEnabled = {},
                        onChangeLeadDays = { selectedLeadDays = it },
                        onChangeRemindTime = { _, _ -> },
                        onToggleSingleHoliday = { h, state ->
                            toggledSingleId = h.id
                            toggledSingleState = state
                        }
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("holidays_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("United States (US)").assertIsDisplayed()
        composeTestRule.onNodeWithTag("next_holiday_banner").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("holiday_item_US_future_holiday").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("holiday_item_US_past_holiday").assertDoesNotExist()

        composeTestRule.onNodeWithTag("select_country_btn").performScrollTo().performClick()
        assertTrue(countryPickerOpened)

        composeTestRule.onNodeWithTag("holiday_lead_days_3").performScrollTo().performClick()
        assertEquals(3, selectedLeadDays)

        composeTestRule.onNodeWithTag("holiday_toggle_US_future_holiday").performScrollTo().performClick()
        assertEquals("US_future_holiday", toggledSingleId)
        assertEquals(false, toggledSingleState)
    }

    @Test
    fun `holidaysSection shows error banner and retry button when offline with no cache`() {
        var retried = false
        composeTestRule.setContent {
            MaterialTheme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    HolidaysSection(
                        selectedCountryCode = "GB",
                        selectedCountryName = "United Kingdom",
                        holidays = emptyList(),
                        remindersEnabled = false,
                        remindDaysBefore = 1,
                        remindHour = 9,
                        remindMinute = 0,
                        isLoading = false,
                        errorMessage = "No internet",
                        is24Hour = false,
                        isBengali = true,
                        onOpenCountryPicker = {},
                        onRefresh = { retried = true },
                        onToggleRemindersEnabled = {},
                        onChangeLeadDays = {},
                        onChangeRemindTime = { _, _ -> },
                        onToggleSingleHoliday = { _, _ -> }
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("holidays_error_banner").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("holidays_retry_btn").performScrollTo().performClick()
        assertTrue(retried)
    }
}
