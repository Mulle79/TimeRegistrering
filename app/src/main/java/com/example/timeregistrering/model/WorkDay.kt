package com.example.timeregistrering.model

import java.time.LocalDate

data class WorkDay(
    val date: LocalDate,
    val registrations: List<TimeRegistration>,
    val totalHours: Double
)
