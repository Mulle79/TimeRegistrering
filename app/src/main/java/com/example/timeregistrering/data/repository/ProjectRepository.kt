package com.example.timeregistrering.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.timeregistrering.model.Project
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "projects")

@Singleton
class ProjectRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val defaultProjectIdKey = stringPreferencesKey("default_project_id")

    private val projects = listOf(
        Project(
            id = "default",
            name = "Standard Projekt",
            description = "Standard projekt for timeregistrering"
        ),
        Project(
            id = "overtime",
            name = "Overarbejde",
            description = "Projekt for overarbejde"
        ),
        Project(
            id = "vacation",
            name = "Ferie",
            description = "Ferie og fridage"
        ),
        Project(
            id = "sick",
            name = "Sygdom",
            description = "Sygedage"
        )
    )

    fun getProjects(): Flow<List<Project>> = kotlinx.coroutines.flow.flow {
        emit(projects)
    }

    suspend fun getDefaultProject(): Project? {
        val defaultProjectId = context.dataStore.data
            .map { preferences ->
                preferences[defaultProjectIdKey] ?: "default"
            }
            .collect { id ->
                return projects.find { it.id == id }
            }
        return projects.first()
    }

    suspend fun setDefaultProject(projectId: String) {
        context.dataStore.edit { preferences ->
            preferences[defaultProjectIdKey] = projectId
        }
    }

    suspend fun getProjectById(projectId: String): Project? {
        return projects.find { it.id == projectId }
    }
}
