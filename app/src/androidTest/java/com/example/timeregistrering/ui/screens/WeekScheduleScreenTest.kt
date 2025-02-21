package com.example.timeregistrering.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.timeregistrering.model.WeekDay
import com.example.timeregistrering.viewmodel.WeekScheduleViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class WeekScheduleScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockViewModel = mockk<WeekScheduleViewModel>(relaxed = true)

    @Test
    fun weekScheduleScreen_displaysAllWeekDays() {
        // Given
        val today = LocalDate.now()
        val weekDays = (0..6).map { dayOffset ->
            WeekDay(
                date = today.plusDays(dayOffset.toLong()),
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(17, 0)
            )
        }
        every { mockViewModel.weekDays } returns MutableStateFlow(weekDays)

        // When
        composeTestRule.setContent {
            WeekScheduleScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then
        weekDays.forEach { day ->
            composeTestRule
                .onNodeWithText(day.date.dayOfWeek.toString(), useUnmergedTree = true)
                .assertExists()
        }
    }

    @Test
    fun weekScheduleScreen_showsCorrectTotalHours() {
        // Given
        val totalHours = 40.0
        every { mockViewModel.calculateTotalHours() } returns totalHours

        // When
        composeTestRule.setContent {
            WeekScheduleScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule
            .onNodeWithText("Timer i alt: $totalHours", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun weekScheduleScreen_navigatesWeeks() {
        // When
        composeTestRule.setContent {
            WeekScheduleScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule
            .onNodeWithContentDescription("Forrige uge")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("Næste uge")
            .performClick()
    }

    @Test
    fun weekScheduleScreen_showsTimePickerOnClick() {
        // Given
        val today = LocalDate.now()
        val weekDays = listOf(
            WeekDay(
                date = today,
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(17, 0)
            )
        )
        every { mockViewModel.weekDays } returns MutableStateFlow(weekDays)

        // When
        composeTestRule.setContent {
            WeekScheduleScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule
            .onNodeWithText("09:00", useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag("time_picker_dialog")
            .assertExists()
    }

    @Test
    fun weekScheduleScreen_disablesWeekendInput() {
        // Given
        val weekend = LocalDate.now().with(java.time.DayOfWeek.SATURDAY)
        val weekDays = listOf(
            WeekDay(
                date = weekend,
                startTime = null,
                endTime = null
            )
        )
        every { mockViewModel.weekDays } returns MutableStateFlow(weekDays)
        every { mockViewModel.isWeekend(weekend) } returns true

        // When
        composeTestRule.setContent {
            WeekScheduleScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule
            .onNodeWithTag("time_input_${weekend}")
            .assertIsNotEnabled()
    }
}
