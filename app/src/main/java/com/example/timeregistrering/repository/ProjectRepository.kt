package com.example.timeregistrering.repository

import com.example.timeregistrering.data.database.dao.ProjectDao
import com.example.timeregistrering.data.database.entity.ProjectEntity
import com.example.timeregistrering.model.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao
) {
    fun getAllProjects(): Flow<List<Project>> =
        projectDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun getProject(id: String): Project? =
        projectDao.getById(id)?.toDomain()

    suspend fun createProject(name: String, description: String = "", color: String = "#000000"): Project {
        val project = Project(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            color = color
        )
        projectDao.insert(project.toEntity())
        return project
    }

    suspend fun updateProject(project: Project) {
        projectDao.update(project.toEntity())
    }

    suspend fun deleteProject(project: Project) {
        projectDao.delete(project.toEntity())
    }

    private fun ProjectEntity.toDomain() = Project(
        id = id,
        name = name,
        description = description,
        color = color
    )

    private fun Project.toEntity() = ProjectEntity(
        id = id,
        name = name,
        description = description,
        color = color
    )
}
