package com.example.timeregistrering.viewmodel

import app.cash.turbine.test
import com.example.timeregistrering.model.WeekDay
import com.example.timeregistrering.repository.TimeRegistrationRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class WeekScheduleViewModelTest {
    
    private lateinit var viewModel: WeekScheduleViewModel
    private lateinit var repository: TimeRegistrationRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = WeekScheduleViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadWeekSchedule sets correct week days`() = runTest {
        // Given
        val today = LocalDate.now()
        val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        
        // When
        viewModel.loadWeekSchedule(weekStart)

        // Then
        viewModel.weekDays.test {
            val days = awaitItem()
            assertThat(days).hasSize(7)
            assertThat(days[0].date).isEqualTo(weekStart)
            assertThat(days[6].date).isEqualTo(weekStart.plusDays(6))
        }
    }

    @Test
    fun `updateStartTime updates correct day`() = runTest {
        // Given
        val today = LocalDate.now()
        val newTime = LocalTime.of(9, 0)
        viewModel.loadWeekSchedule(today)

        // When
        viewModel.updateStartTime(today, newTime)

        // Then
        viewModel.weekDays.test {
            val days = awaitItem()
            val updatedDay = days.find { it.date == today }
            assertThat(updatedDay?.startTime).isEqualTo(newTime)
        }
    }

    @Test
    fun `updateEndTime updates correct day`() = runTest {
        // Given
        val today = LocalDate.now()
        val newTime = LocalTime.of(17, 0)
        viewModel.loadWeekSchedule(today)

        // When
        viewModel.updateEndTime(today, newTime)

        // Then
        viewModel.weekDays.test {
            val days = awaitItem()
            val updatedDay = days.find { it.date == today }
            assertThat(updatedDay?.endTime).isEqualTo(newTime)
        }
    }

    @Test
    fun `calculateTotalHours returns correct sum`() = runTest {
        // Given
        val today = LocalDate.now()
        viewModel.loadWeekSchedule(today)
        
        // 8 timer hver dag på hverdage
        (0..4).forEach { dayOffset ->
            val date = today.plusDays(dayOffset.toLong())
            viewModel.updateStartTime(date, LocalTime.of(9, 0))
            viewModel.updateEndTime(date, LocalTime.of(17, 0))
        }

        // When
        val totalHours = viewModel.calculateTotalHours()

        // Then
        assertThat(totalHours).isEqualTo(40.0) // 5 dage × 8 timer
    }

    @Test
    fun `navigateToNextWeek updates week correctly`() = runTest {
        // Given
        val initialWeek = LocalDate.now()
        viewModel.loadWeekSchedule(initialWeek)

        // When
        viewModel.navigateToNextWeek()

        // Then
        viewModel.weekDays.test {
            val days = awaitItem()
            assertThat(days[0].date).isEqualTo(initialWeek.plusWeeks(1))
        }
    }

    @Test
    fun `navigateToPreviousWeek updates week correctly`() = runTest {
        // Given
        val initialWeek = LocalDate.now()
        viewModel.loadWeekSchedule(initialWeek)

        // When
        viewModel.navigateToPreviousWeek()

        // Then
        viewModel.weekDays.test {
            val days = awaitItem()
            assertThat(days[0].date).isEqualTo(initialWeek.minusWeeks(1))
        }
    }

    @Test
    fun `isWeekend returns correct value`() {
        // Given
        val weekday = LocalDate.now().with(java.time.DayOfWeek.WEDNESDAY)
        val weekend = LocalDate.now().with(java.time.DayOfWeek.SATURDAY)

        // Then
        assertThat(viewModel.isWeekend(weekday)).isFalse()
        assertThat(viewModel.isWeekend(weekend)).isTrue()
    }
}
