package com.example.timeregistrering.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Repræsenterer en enkelt tidsregistrering i systemet.
 *
 * @property id Unik identifikator for tidsregistreringen
 * @property date Datoen for registreringen
 * @property startTime Starttidspunkt for arbejdsdagen
 * @property endTime Sluttidspunkt for arbejdsdagen
 * @property note Valgfri note eller kommentar til registreringen
 * @property isHoliday Indikerer om dagen er en helligdag
 * @property isWeekend Indikerer om dagen er en weekenddag
 */
data class TimeRegistration(
    val id: Long = 0,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val note: String? = null,
    val isHoliday: Boolean = false,
    val isWeekend: Boolean = false
)
