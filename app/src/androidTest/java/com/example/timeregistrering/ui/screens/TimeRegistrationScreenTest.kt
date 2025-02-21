package com.example.timeregistrering.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.timeregistrering.MainActivity
import com.example.timeregistrering.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TimeRegistrationScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun timeRegistrationScreen_initialState_showsEmptyState() {
        // Verificer at tom tilstand vises korrekt
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.no_registrations)
        ).assertExists()
    }

    @Test
    fun timeRegistrationScreen_addRegistration_showsInList() {
        // Given: Tilføj en tidsregistrering
        val startTime = LocalTime.of(9, 0)
        val endTime = LocalTime.of(17, 0)
        
        // When: Klik på tilføj knap
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.add_registration)
        ).performClick()

        // And: Udfyld tidsregistrering
        composeTestRule.onNodeWithContentDescription("start_time")
            .performTextInput(startTime.toString())
        composeTestRule.onNodeWithContentDescription("end_time")
            .performTextInput(endTime.toString())
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.save)
        ).performClick()

        // Then: Registreringen vises i listen
        composeTestRule.onNodeWithText("9:00 - 17:00").assertExists()
    }

    @Test
    fun timeRegistrationScreen_weekNavigation_showsCorrectWeek() {
        // When: Naviger til næste uge
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.next_week)
        ).performClick()

        // Then: Korrekt uge vises
        val nextWeek = LocalDate.now().plusWeeks(1)
        composeTestRule.onNodeWithText(
            nextWeek.toString()
        ).assertExists()
    }

    @Test
    fun timeRegistrationScreen_totalHours_calculatesCorrectly() {
        // Given: Tilføj flere registreringer
        // TODO: Tilføj registreringer via ViewModel

        // Then: Total timer vises korrekt
        composeTestRule.onNodeWithTag("total_hours")
            .assertTextContains("8.0")
    }

    @Test
    fun showsOfflineIndicator_WhenOffline() {
        // Given
        val viewModel = mockk<TimeRegistrationViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(
            TimeRegistrationViewModel.TimeRegistrationUiState.Success(
                isOffline = true,
                pendingSyncCount = 2
            )
        )

        // When
        composeTestRule.setContent {
            TimeRegistrationScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule.onNodeWithText("Offline").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 registreringer venter på synkronisering").assertIsDisplayed()
    }

    @Test
    fun showsErrorMessage_WhenErrorOccurs() {
        // Given
        val viewModel = mockk<TimeRegistrationViewModel>(relaxed = true)
        val errorMessage = "Kunne ikke indlæse data"
        every { viewModel.uiState } returns MutableStateFlow(
            TimeRegistrationViewModel.TimeRegistrationUiState.Error(errorMessage)
        )

        // When
        composeTestRule.setContent {
            TimeRegistrationScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun showsLoadingIndicator_WhenLoading() {
        // Given
        val viewModel = mockk<TimeRegistrationViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(
            TimeRegistrationViewModel.TimeRegistrationUiState.Loading
        )

        // When
        composeTestRule.setContent {
            TimeRegistrationScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }

    @Test
    fun showsCurrentRegistration_WhenActive() {
        // Given
        val viewModel = mockk<TimeRegistrationViewModel>(relaxed = true)
        val testRegistration = TimeRegistration(
            id = "1",
            projectId = "1",
            startTime = LocalDateTime.now(),
            description = "Test registrering"
        )
        every { viewModel.uiState } returns MutableStateFlow(
            TimeRegistrationViewModel.TimeRegistrationUiState.Success(
                currentRegistration = testRegistration,
                projects = listOf(Project("1", "Test Projekt", "Test beskrivelse"))
            )
        )

        // When
        composeTestRule.setContent {
            TimeRegistrationScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule.onNodeWithText("Test Projekt").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test registrering").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stop").assertIsDisplayed()
    }

    @Test
    fun canStartNewRegistration_WhenNoActiveRegistration() {
        // Given
        val viewModel = mockk<TimeRegistrationViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(
            TimeRegistrationViewModel.TimeRegistrationUiState.Success(
                currentRegistration = null,
                projects = listOf(Project("1", "Test Projekt", "Test beskrivelse"))
            )
        )

        // When
        composeTestRule.setContent {
            TimeRegistrationScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule.onNodeWithText("Start").assertIsDisplayed()
        
        // When clicking start
        composeTestRule.onNodeWithText("Start").performClick()
        
        // Then
        verify { viewModel.startTimeRegistration(any(), any()) }
    }

    @Test
    fun showsValidationError_WhenStartingWithoutProject() {
        // Given
        val viewModel = mockk<TimeRegistrationViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(
            TimeRegistrationViewModel.TimeRegistrationUiState.Success(
                currentRegistration = null,
                projects = emptyList()
            )
        )

        // When
        composeTestRule.setContent {
            TimeRegistrationScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("Start").performClick()

        // Then
        composeTestRule.onNodeWithText("Vælg venligst et projekt").assertIsDisplayed()
    }
}
