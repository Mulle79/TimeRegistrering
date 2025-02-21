package com.example.timeregistrering.data.database.entity

import androidx.room.*
import java.time.LocalDateTime

@Entity(tableName = "time_registrations")
data class TimeRegistrationEntity(
    @PrimaryKey val id: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val description: String,
    val latitude: Double?,
    val longitude: Double?,
    val address: String?,
    val projectId: String,
    val userId: String
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val color: String
)

@Entity(
    tableName = "work_periods",
    foreignKeys = [
        ForeignKey(
            entity = TimeRegistrationEntity::class,
            parentColumns = ["id"],
            childColumns = ["timeRegistrationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WorkPeriodEntity(
    @PrimaryKey val id: String,
    val timeRegistrationId: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val totalWorkTime: Long
)

@Entity(
    tableName = "breaks",
    foreignKeys = [
        ForeignKey(
            entity = WorkPeriodEntity::class,
            parentColumns = ["id"],
            childColumns = ["workPeriodId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BreakEntity(
    @PrimaryKey val id: String,
    val workPeriodId: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val duration: Long
)
