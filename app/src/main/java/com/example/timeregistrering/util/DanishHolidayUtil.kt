package com.example.timeregistrering.util

import java.time.LocalDate
import java.time.Month
import java.time.temporal.ChronoUnit

object DanishHolidayUtil {
    fun isHoliday(date: LocalDate): Boolean {
        return when {
            // Faste helligdage
            isNewYear(date) -> true
            isConstitutionDay(date) -> true
            isChristmas(date) -> true
            
            // Påske-relaterede helligdage
            isEasterRelatedHoliday(date) -> true
            
            // Store bededag (afskaffet fra 2024, men nogle overenskomster har stadig dagen)
            isPrayerDay(date) -> true
            
            else -> false
        }
    }

    private fun isNewYear(date: LocalDate): Boolean {
        return date.month == Month.JANUARY && date.dayOfMonth == 1
    }

    private fun isConstitutionDay(date: LocalDate): Boolean {
        return date.month == Month.JUNE && date.dayOfMonth == 5
    }

    private fun isChristmas(date: LocalDate): Boolean {
        return date.month == Month.DECEMBER && (date.dayOfMonth in 24..26)
    }

    private fun isEasterRelatedHoliday(date: LocalDate): Boolean {
        val easter = calculateEaster(date.year)
        return when (date) {
            easter.minusDays(3) -> true  // Skærtorsdag
            easter.minusDays(2) -> true  // Langfredag
            easter -> true               // Påskedag
            easter.plusDays(1) -> true   // 2. påskedag
            easter.plusDays(39) -> true  // Kristi himmelfartsdag
            easter.plusDays(49) -> true  // Pinsedag
            easter.plusDays(50) -> true  // 2. pinsedag
            else -> false
        }
    }

    private fun isPrayerDay(date: LocalDate): Boolean {
        if (date.year >= 2024) return false
        val easter = calculateEaster(date.year)
        return date == easter.plusDays(26)
    }

    // Beregn påskedag for et givet år (Meeus/Jones/Butcher algoritme)
    private fun calculateEaster(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        
        return LocalDate.of(year, month, day)
    }
}
