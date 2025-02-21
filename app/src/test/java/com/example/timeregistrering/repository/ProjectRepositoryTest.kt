package com.example.timeregistrering.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferencesKey
import androidx.datastore.preferences.core.Preferences
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ProjectRepositoryTest {
    private lateinit var context: Context
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: ProjectRepository

    @Before
    fun setup() {
        context = mockk()
        dataStore = mockk()
        every { context.dataStore } returns dataStore
        repository = ProjectRepository(context)
    }

    @Test
    fun `test get projects returns predefined list`() = runBlocking {
        // When
        val projects = repository.getProjects().first()

        // Then
        assertTrue(projects.isNotEmpty())
        assertTrue(projects.any { it.id == "default" })
        assertTrue(projects.any { it.id == "overtime" })
        assertTrue(projects.any { it.id == "vacation" })
        assertTrue(projects.any { it.id == "sick" })
    }

    @Test
    fun `test get default project returns first project when no default is set`() = runBlocking {
        // Given
        every { dataStore.data } returns flowOf(mockk())

        // When
        val defaultProject = repository.getDefaultProject()

        // Then
        assertNotNull(defaultProject)
        assertEquals("default", defaultProject?.id)
    }

    @Test
    fun `test get project by id returns correct project`() = runBlocking {
        // When
        val project = repository.getProjectById("overtime")

        // Then
        assertNotNull(project)
        assertEquals("overtime", project?.id)
        assertEquals("Overarbejde", project?.name)
    }

    @Test
    fun `test get project by invalid id returns null`() = runBlocking {
        // When
        val project = repository.getProjectById("invalid_id")

        // Then
        assertNull(project)
    }
}
