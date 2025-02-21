package com.example.timeregistrering.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import java.time.LocalTime

class TimeInputTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun timeInput_DisplaysCurrentTime() {
        // Given
        val testTime = LocalTime.of(14, 30)

        // When
        composeTestRule.setContent {
            TimeInput(
                time = testTime,
                onTimeChanged = {},
                label = "Test tid"
            )
        }

        // Then
        composeTestRule
            .onNodeWithText("14:30")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun timeInput_ShowsDialog_WhenClicked() {
        // Given
        composeTestRule.setContent {
            TimeInput(
                time = LocalTime.of(14, 30),
                onTimeChanged = {},
                label = "Test tid"
            )
        }

        // When
        composeTestRule
            .onNodeWithText("14:30")
            .performClick()

        // Then
        composeTestRule
            .onNodeWithText("Vælg tidspunkt")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun timeInput_UpdatesTime_WhenNewTimeSelected() {
        // Given
        var selectedTime: LocalTime? = null
        
        composeTestRule.setContent {
            TimeInput(
                time = LocalTime.of(14, 30),
                onTimeChanged = { selectedTime = it },
                label = "Test tid"
            )
        }

        // When
        composeTestRule
            .onNodeWithText("14:30")
            .performClick()

        // Simulate selecting a new time (15:45)
        composeTestRule
            .onAllNodesWithText("▲")
            .get(0) // Timer
            .performClick()

        composeTestRule
            .onAllNodesWithText("▲")
            .get(1) // Minutter
            .performClick()
            .performClick()
            .performClick()
            .performClick()
            .performClick()
            .performClick()
            .performClick()
            .performClick()
            .performClick()
            .performClick()
            .performClick()
            .performClick()
            .performClick()
            .performClick()
            .performClick()

        composeTestRule
            .onNodeWithText("OK")
            .performClick()

        // Then
        assert(selectedTime?.hour == 15)
        assert(selectedTime?.minute == 45)
    }
}
