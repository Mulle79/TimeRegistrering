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

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun loginScreen_initialState_showsGoogleSignInButton() {
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.sign_in_with_google)
        ).assertExists()
    }

    @Test
    fun loginScreen_errorState_showsErrorMessage() {
        // Given: En fejltilstand
        composeTestRule.activity.runOnUiThread {
            // TODO: Indsæt fejltilstand via ViewModel
        }

        // Then: Fejlbesked vises
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.login_error)
        ).assertExists()
    }

    @Test
    fun loginScreen_loadingState_showsProgressIndicator() {
        // Given: Loading tilstand
        composeTestRule.activity.runOnUiThread {
            // TODO: Sæt loading tilstand via ViewModel
        }

        // Then: Progress indicator vises
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.loading)
        ).assertExists()
    }
}
