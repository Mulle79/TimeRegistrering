package com.example.timeregistrering.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.timeregistrering.model.Project
import org.junit.Rule
import org.junit.Test

class ProjectSelectorTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testProjects = listOf(
        Project("1", "Projekt 1", "Test projekt 1"),
        Project("2", "Projekt 2", "Test projekt 2"),
        Project("3", "Projekt 3", "Test projekt 3")
    )

    @Test
    fun projectSelector_DisplaysSelectedProject() {
        // Given
        val selectedProject = testProjects[0]

        // When
        composeTestRule.setContent {
            ProjectSelector(
                selectedProject = selectedProject,
                projects = testProjects,
                onProjectSelected = {}
            )
        }

        // Then
        composeTestRule
            .onNodeWithText(selectedProject.name)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun projectSelector_ShowsDropdownOnClick() {
        // Given
        composeTestRule.setContent {
            ProjectSelector(
                selectedProject = testProjects[0],
                projects = testProjects,
                onProjectSelected = {}
            )
        }

        // When
        composeTestRule
            .onNodeWithText(testProjects[0].name)
            .performClick()

        // Then
        testProjects.forEach { project ->
            composeTestRule
                .onNodeWithText(project.name)
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun projectSelector_CallsCallback_WhenProjectSelected() {
        // Given
        var selectedProjectId: String? = null
        
        composeTestRule.setContent {
            ProjectSelector(
                selectedProject = testProjects[0],
                projects = testProjects,
                onProjectSelected = { selectedProjectId = it.id }
            )
        }

        // When
        composeTestRule
            .onNodeWithText(testProjects[0].name)
            .performClick()

        composeTestRule
            .onNodeWithText(testProjects[1].name)
            .performClick()

        // Then
        assert(selectedProjectId == testProjects[1].id)
    }
}
