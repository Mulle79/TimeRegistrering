package com.example.timeregistrering.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "time_registrations")
data class TimeRegistration(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val note: String? = null,
    val isHoliday: Boolean = false,
    val isWeekend: Boolean = false
)
