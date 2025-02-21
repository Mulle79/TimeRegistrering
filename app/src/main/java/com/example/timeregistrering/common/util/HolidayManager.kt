package com.example.timeregistrering.util

import com.example.timeregistrering.repository.CalendarRepository
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Events
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HolidayManager @Inject constructor(
    private val calendarRepository: CalendarRepository
) {
    companion object {
        private const val DANISH_HOLIDAY_CALENDAR_ID = "da.danish#holiday@group.v.calendar.google.com"
    }

    suspend fun isHoliday(date: LocalDate): Flow<Boolean> = flow {
        try {
            val startOfDay = date.atStartOfDay(ZoneId.systemDefault())
            val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault())

            val events = calendarRepository.fetchEventsFromCalendar(
                DANISH_HOLIDAY_CALENDAR_ID,
                startOfDay.toLocalDateTime(),
                endOfDay.toLocalDateTime()
            )

            emit(events.any())
        } catch (e: Exception) {
            // Hvis vi ikke kan få fat i helligdage fra Google Calendar,
            // antager vi at det ikke er en helligdag
            emit(false)
        }
    }

    suspend fun getHolidaysForMonth(year: Int, month: Int): Flow<List<LocalDate>> = flow {
        try {
            val startDate = LocalDate.of(year, month, 1)
            val endDate = startDate.plusMonths(1)

            val events = calendarRepository.fetchEventsFromCalendar(
                DANISH_HOLIDAY_CALENDAR_ID,
                startDate.atStartOfDay(),
                endDate.atStartOfDay()
            )

            val holidays = events.map { event ->
                val eventDateTime = DateTime(event.start.date?.toInstant()?.toEpochMilli() ?: 
                                          event.start.dateTime.value)
                LocalDate.ofInstant(eventDateTime.toInstant(), ZoneId.systemDefault())
            }

            emit(holidays)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}
