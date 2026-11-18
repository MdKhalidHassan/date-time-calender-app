package com.example

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.ui.dialogs.AddDateAlarmDialog
import com.example.ui.dialogs.AddEditNoteDialog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DateSelectionDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `addDateAlarmDialog allows changing date directly via quick chip`() {
        var savedYear = -1
        var savedMonth = -1
        var savedDay = -1

        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

        composeTestRule.setContent {
            MaterialTheme {
                AddDateAlarmDialog(
                    initialYear = 2026,
                    initialMonth = 0, // January
                    initialDay = 15,
                    isBengali = false,
                    onDismiss = {},
                    onSave = { y, m, d, _, _, _, _, _ ->
                        savedYear = y
                        savedMonth = m
                        savedDay = d
                    }
                )
            }
        }

        composeTestRule.onNodeWithTag("date_alarm_selected_date_text")
            .assertIsDisplayed()
            .assertTextEquals("15 January 2026")
        composeTestRule.onNodeWithTag("date_alarm_pick_date_btn").assertIsDisplayed()

        // Tap Tomorrow quick chip
        composeTestRule.onNodeWithText("Tomorrow").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("date_alarm_title_input").performScrollTo().performTextInput("Trip")
        composeTestRule.onNodeWithTag("save_date_alarm_btn").performClick()

        assertEquals(tomorrow.get(Calendar.YEAR), savedYear)
        assertEquals(tomorrow.get(Calendar.MONTH), savedMonth)
        assertEquals(tomorrow.get(Calendar.DAY_OF_MONTH), savedDay)
    }

    @Test
    fun `addEditNoteDialog displays correct month and allows changing date directly`() {
        var savedYear = -1
        var savedMonth = -1
        var savedDay = -1
        var savedCalled = false

        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

        composeTestRule.setContent {
            MaterialTheme {
                AddEditNoteDialog(
                    year = 2026,
                    month = 2, // March
                    day = 10,
                    is24Hour = false,
                    isBengali = false,
                    onDismiss = {},
                    onSave = { y, m, d, _, _, _, _ ->
                        savedCalled = true
                        savedYear = y
                        savedMonth = m
                        savedDay = d
                    }
                )
            }
        }

        composeTestRule.onNodeWithTag("note_selected_date_text")
            .assertIsDisplayed()
            .assertTextEquals("10 March 2026")
        composeTestRule.onNodeWithTag("note_pick_date_btn").assertIsDisplayed()

        // Tap Tomorrow quick chip
        composeTestRule.onNodeWithText("Tomorrow").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("note_title_input").performScrollTo().performTextInput("Reminder Note")
        composeTestRule.onNodeWithTag("save_note_btn").performClick()

        assertTrue(savedCalled)
        assertEquals(tomorrow.get(Calendar.YEAR), savedYear)
        assertEquals(tomorrow.get(Calendar.MONTH), savedMonth)
        assertEquals(tomorrow.get(Calendar.DAY_OF_MONTH), savedDay)
    }
}
