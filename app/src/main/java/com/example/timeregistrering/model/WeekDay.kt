package com.example.timeregistrering.model

import java.time.LocalDate
import java.time.LocalTime

data class WeekDay(
    val date: LocalDate,
    val name: String,
    val isWorkDay: Boolean,
    val isHoliday: Boolean,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val totalHours: Double?
)
