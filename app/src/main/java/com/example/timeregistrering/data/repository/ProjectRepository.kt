package com.example.timeregistrering.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.timeregistrering.model.Project
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Definerer en DataStore extension property for Context
private val Context.dataStore by preferencesDataStore(name = "project_preferences")

@Singleton
class ProjectRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val defaultProjectIdKey = stringPreferencesKey("default_project_id")
    
    // Eksempelprojekter - i en rigtig app ville disse komme fra en database eller API
    private val projects: List<Project> = listOf(
        Project(id = "1", name = "Projekt A", description = "Projekt A beskrivelse", color = "#FF5733"),
        Project(id = "2", name = "Projekt B", description = "Projekt B beskrivelse", color = "#33FF57"),
        Project(id = "3", name = "Projekt C", description = "Projekt C beskrivelse", color = "#3357FF")
    )
    
    fun getProjects(): Flow<List<Project>> = flow<List<Project>> {
        emit(projects)
    }
    
    suspend fun getDefaultProject(): Project? {
        val defaultProjectId: String = context.dataStore.data
            .map { preferences ->
                preferences[defaultProjectIdKey] ?: "1"
            }
            .firstOrNull() ?: "1"
            
        return projects.find { it.id == defaultProjectId } ?: projects.first()
    }
    
    suspend fun setDefaultProject(projectId: String) {
        if (projects.any { it.id == projectId }) {
            context.dataStore.edit { preferences ->
                preferences[defaultProjectIdKey] = projectId
            }
        }
    }
    
    fun getProjectById(id: String): Project? {
        return projects.find { it.id == id }
    }
}
