package com.example.timeregistrering.viewmodel

import com.example.timeregistrering.repository.TimeRegistrationRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

@ExperimentalCoroutinesApi
class TimeRegistrationViewModelTest {
    private val testDispatcher = TestCoroutineDispatcher()
    
    @MockK
    private lateinit var repository: TimeRegistrationRepository
    
    private lateinit var viewModel: TimeRegistrationViewModel
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = TimeRegistrationViewModel(repository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
    
    @Test
    fun `when saving time registration with valid input then success`() {
        // Given
        val date = LocalDate.now()
        val startTime = LocalTime.of(9, 0)
        val endTime = LocalTime.of(17, 0)
        val note = "Test note"
        
        coEvery { repository.saveTimeRegistration(any()) } returns Unit
        
        // When
        viewModel.saveTimeRegistration(date, startTime, endTime, note)
        
        // Then
        assert(viewModel.errorState.value is ErrorState.None)
    }
    
    @Test
    fun `when saving time registration with invalid time then error`() {
        // Given
        val date = LocalDate.now()
        val startTime = LocalTime.of(17, 0)
        val endTime = LocalTime.of(9, 0)
        val note = "Test note"
        
        // When
        viewModel.saveTimeRegistration(date, startTime, endTime, note)
        
        // Then
        assert(viewModel.errorState.value is ErrorState.InvalidTimeRange)
    }
    
    @Test
    fun `when loading weekly registrations then success`() {
        // Given
        val date = LocalDate.now()
        val weekRegistrations = listOf(
            TimeRegistration(
                date = date,
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(17, 0),
                note = "Test"
            )
        )
        
        coEvery { repository.getWeeklyRegistrations(any()) } returns weekRegistrations
        
        // When
        viewModel.loadWeeklyRegistrations(date)
        
        // Then
        assert(viewModel.weeklyRegistrations.value == weekRegistrations)
        assert(viewModel.errorState.value is ErrorState.None)
    }
    
    @Test
    fun `when loading weekly registrations fails then error`() {
        // Given
        val date = LocalDate.now()
        coEvery { repository.getWeeklyRegistrations(any()) } throws Exception("Test error")
        
        // When
        viewModel.loadWeeklyRegistrations(date)
        
        // Then
        assert(viewModel.errorState.value is ErrorState.LoadError)
    }
}
