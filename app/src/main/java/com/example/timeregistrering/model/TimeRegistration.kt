package com.example.timeregistrering.model

import java.time.LocalDateTime

data class TimeRegistration(
    val id: String = "",
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val description: String = "",
    val location: Location? = null,
    val projectId: String = "",
    val userId: String = ""
)

data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

data class Project(
    val id: String,
    val name: String,
    val description: String = "",
    val color: String = "#000000"
)

data class WorkPeriod(
    val id: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val breaks: List<Break> = emptyList(),
    val totalWorkTime: Long = 0 // i minutter
)

data class Break(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val duration: Long = 0 // i minutter
)
