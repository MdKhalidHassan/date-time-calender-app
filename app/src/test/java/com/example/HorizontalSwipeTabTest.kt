package com.example

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.core.app.ApplicationProvider
import com.example.ui.DateTimeScreen
import com.example.ui.DateTimeViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HorizontalSwipeTabTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `horizontal swipe changes active tab and syncs with tab bar`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = DateTimeViewModel(app)

        composeTestRule.setContent {
            MaterialTheme {
                DateTimeScreen(viewModel = viewModel)
            }
        }

        composeTestRule.waitForIdle()
        assertEquals(0, viewModel.activeTab.value)
        composeTestRule.onNodeWithTag("tab_calendar").assertIsSelected()

        // Swipe left to move from Calendar (0) to Regular Alarms (1)
        composeTestRule.onNodeWithTag("main_horizontal_pager").performTouchInput {
            swipeLeft()
        }
        composeTestRule.waitForIdle()
        assertEquals(1, viewModel.activeTab.value)
        composeTestRule.onNodeWithTag("tab_regular_alarms").assertIsSelected()

        // Swipe right to return to Calendar (0)
        composeTestRule.onNodeWithTag("main_horizontal_pager").performTouchInput {
            swipeRight()
        }
        composeTestRule.waitForIdle()
        assertEquals(0, viewModel.activeTab.value)
        composeTestRule.onNodeWithTag("tab_calendar").assertIsSelected()

        // Tapping a tab also updates pager & activeTab cleanly
        composeTestRule.onNodeWithTag("tab_notes").performClick()
        composeTestRule.waitForIdle()
        assertEquals(3, viewModel.activeTab.value)
        composeTestRule.onNodeWithTag("tab_notes").assertIsSelected()
    }
}
