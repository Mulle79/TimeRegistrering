package com.example.timeregistrering.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timeregistrering.model.TimeRegistration
import com.example.timeregistrering.model.UiState
import com.example.timeregistrering.model.WeekDay
import com.example.timeregistrering.repository.TimeRegistrationRepository
import com.example.timeregistrering.util.DanishHolidayUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*
import javax.inject.Inject

@HiltViewModel
class WeekScheduleViewModel @Inject constructor(
    private val repository: TimeRegistrationRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _selectedWeek = savedStateHandle.getStateFlow("selected_week", LocalDate.now())
    private val _weekSchedule = MutableStateFlow<UiState<List<WeekDay>>>(UiState.Loading)
    val weekSchedule: StateFlow<UiState<List<WeekDay>>> = _weekSchedule

    private val weekCache = mutableMapOf<LocalDate, List<WeekDay>>()

    val totalHours: StateFlow<Double> = weekSchedule
        .map { state ->
            when (state) {
                is UiState.Success -> state.data.sumOf { it.totalHours ?: 0.0 }
                else -> 0.0
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val currentWeekText: String
        get() {
            val weekNumber = _selectedWeek.value.get(WeekFields.ISO.weekOfWeekBasedYear())
            val year = _selectedWeek.value.year
            return "Uge $weekNumber, $year"
        }

    init {
        loadWeekSchedule()
    }

    fun restoreState() {
        loadWeekSchedule()
    }

    fun saveState() {
        savedStateHandle["selected_week"] = _selectedWeek.value
    }

    fun loadWeekSchedule() {
        viewModelScope.launch {
            try {
                _weekSchedule.value = UiState.Loading
                
                val weekStart = _selectedWeek.value
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

                // Check cache first
                weekCache[weekStart]?.let {
                    _weekSchedule.value = UiState.Success(it)
                    return@launch
                }
                
                val weekDays = (0..6).map { dayOffset ->
                    val date = weekStart.plusDays(dayOffset.toLong())
                    val registrations = repository.getTimeRegistrationsForDate(date)
                        .first()
                        .sortedBy { it.startTime }
                    
                    WeekDay(
                        date = date,
                        name = "${date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("da"))} ${
                            date.format(DateTimeFormatter.ofPattern("dd/MM"))
                        }",
                        isWorkDay = date.dayOfWeek !in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                        isHoliday = isHoliday(date),
                        startTime = registrations.firstOrNull()?.startTime?.toLocalTime(),
                        endTime = registrations.lastOrNull()?.endTime?.toLocalTime(),
                        totalHours = calculateTotalHours(registrations)
                    )
                }

                // Update cache
                weekCache[weekStart] = weekDays
                if (weekCache.size > 4) { // Keep only last 4 weeks in cache
                    weekCache.entries.firstOrNull()?.let { weekCache.remove(it.key) }
                }
                
                _weekSchedule.value = UiState.Success(weekDays)
            } catch (e: Exception) {
                _weekSchedule.value = UiState.Error("Kunne ikke indlæse ugeskema: ${e.message}")
            }
        }
    }

    fun previousWeek() {
        _selectedWeek.value = _selectedWeek.value.minusWeeks(1)
        loadWeekSchedule()
    }

    fun nextWeek() {
        _selectedWeek.value = _selectedWeek.value.plusWeeks(1)
        loadWeekSchedule()
    }

    fun updateStartTime(date: LocalDate, time: LocalTime) {
        viewModelScope.launch {
            try {
                validateTime(date, time, null)
                val startDateTime = LocalDateTime.of(date, time)
                repository.startTimeRegistration(
                    projectId = "default",
                    startTime = startDateTime
                )
                loadWeekSchedule()
            } catch (e: Exception) {
                throw IllegalArgumentException("Kunne ikke opdatere starttid: ${e.message}")
            }
        }
    }

    fun updateEndTime(date: LocalDate, time: LocalTime) {
        viewModelScope.launch {
            try {
                val currentState = weekSchedule.value
                if (currentState is UiState.Success) {
                    val dayData = currentState.data.find { it.date == date }
                    validateTime(date, dayData?.startTime, time)
                }
                
                val endDateTime = LocalDateTime.of(date, time)
                repository.updateEndTime(date, endDateTime)
                loadWeekSchedule()
            } catch (e: Exception) {
                throw IllegalArgumentException("Kunne ikke opdatere sluttid: ${e.message}")
            }
        }
    }

    private fun validateTime(date: LocalDate, startTime: LocalTime?, endTime: LocalTime?) {
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw IllegalArgumentException("Sluttid skal være efter starttid")
        }

        // Tjek for overlap med eksisterende registreringer
        val currentState = weekSchedule.value
        if (currentState is UiState.Success) {
            val dayData = currentState.data.find { it.date == date }
            if (dayData?.startTime != null && dayData.endTime != null) {
                if (startTime != null && startTime.isBefore(dayData.endTime)) {
                    throw IllegalArgumentException("Ny starttid overlapper med eksisterende registrering")
                }
                if (endTime != null && endTime.isAfter(dayData.startTime)) {
                    throw IllegalArgumentException("Ny sluttid overlapper med eksisterende registrering")
                }
            }
        }
    }

    private fun calculateTotalHours(registrations: List<TimeRegistration>): Double {
        return registrations.sumOf { registration ->
            registration.endTime?.let { endTime ->
                val duration = Duration.between(registration.startTime, endTime)
                duration.toMinutes() / 60.0
            } ?: 0.0
        }
    }

    private fun isHoliday(date: LocalDate): Boolean {
        return DanishHolidayUtil.isHoliday(date)
    }
}
