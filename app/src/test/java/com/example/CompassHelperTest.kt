package com.example

import android.hardware.SensorManager
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
import com.example.ui.components.CompassSection
import com.example.util.CompassHelper
import com.example.util.CompassState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CompassHelperTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `normalizeDegrees wraps negative and over-360 values`() {
        assertEquals(0f, CompassHelper.normalizeDegrees(360f), 0.001f)
        assertEquals(350f, CompassHelper.normalizeDegrees(-10f), 0.001f)
        assertEquals(45f, CompassHelper.normalizeDegrees(405f), 0.001f)
    }

    @Test
    fun `shortestAngleDelta handles 359 to 1 degree wraparound`() {
        assertEquals(2f, CompassHelper.shortestAngleDelta(359f, 1f), 0.001f)
        assertEquals(-2f, CompassHelper.shortestAngleDelta(1f, 359f), 0.001f)
        assertEquals(90f, CompassHelper.shortestAngleDelta(0f, 90f), 0.001f)
        assertEquals(-90f, CompassHelper.shortestAngleDelta(90f, 0f), 0.001f)
    }

    @Test
    fun `cardinal direction names match English and Bengali`() {
        assertEquals("N", CompassHelper.getShortCardinalDirection(0f, isBengali = false))
        assertEquals("উ", CompassHelper.getShortCardinalDirection(358f, isBengali = true))
        assertEquals("East", CompassHelper.getFullCardinalDirection(90f, isBengali = false))
        assertEquals("পূর্ব", CompassHelper.getFullCardinalDirection(92f, isBengali = true))
        assertEquals("SW", CompassHelper.getShortCardinalDirection(225f, isBengali = false))
        assertEquals("দ-প", CompassHelper.getShortCardinalDirection(225f, isBengali = true))
    }

    @Test
    fun `compassSection displays heading and lock bearing toggle`() {
        composeTestRule.setContent {
            MaterialTheme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    CompassSection(
                        isBengali = false,
                        compassStateOverride = CompassState(
                            azimuthDegrees = 90f,
                            pitchDegrees = 0f,
                            rollDegrees = 0f,
                            magneticFieldStrengthUt = 45.2f,
                            accuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
                            hasCompassSensor = true,
                            usingRotationVector = true
                        )
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("compass_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("compass_dial").assertIsDisplayed()
        composeTestRule.onNodeWithText("90°").assertIsDisplayed()
        composeTestRule.onNodeWithText("East").assertIsDisplayed()

        // Lock current bearing
        composeTestRule.onNodeWithTag("compass_lock_bearing_btn").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Clear Lock").performScrollTo().assertIsDisplayed()

        // Toggle calibration guide
        composeTestRule.onNodeWithTag("compass_calibration_btn").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("compass_calibration_guide").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `compassSection displays unavailable state honestly when no sensor`() {
        composeTestRule.setContent {
            MaterialTheme {
                CompassSection(
                    isBengali = true,
                    compassStateOverride = CompassState(
                        hasCompassSensor = false
                    )
                )
            }
        }

        composeTestRule.onNodeWithTag("compass_unavailable_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("কম্পাস সেন্সর পাওয়া যায়নি").assertIsDisplayed()
    }
}
