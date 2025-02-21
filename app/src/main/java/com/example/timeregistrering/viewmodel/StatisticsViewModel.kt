package com.example.timeregistrering.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timeregistrering.util.StatisticsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsManager: StatisticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                _uiState.value = StatisticsUiState.Loading

                // Hent statistik for den sidste måned
                val today = LocalDate.now()
                val monthStart = today.withDayOfMonth(1)
                val projectStats = statisticsManager.getProjectStatistics(
                    startDate = monthStart,
                    endDate = today
                )

                // Hent ugentlig oversigt
                val weekStart = today.minusDays(today.dayOfWeek.value - 1L)
                val weeklyOverview = statisticsManager.getWeeklyOverview(weekStart)

                // Hent månedlige trends
                val monthlyTrends = statisticsManager.getMonthlyTrends(
                    year = today.year,
                    month = today.monthValue
                )

                _uiState.value = StatisticsUiState.Success(
                    projectStatistics = projectStats,
                    weeklyOverview = weeklyOverview,
                    monthlyTrends = monthlyTrends
                )
            } catch (e: Exception) {
                _uiState.value = StatisticsUiState.Error(
                    "Kunne ikke indlæse statistik: ${e.message}"
                )
            }
        }
    }

    fun refresh() {
        loadStatistics()
    }

    sealed class StatisticsUiState {
        object Loading : StatisticsUiState()
        data class Success(
            val projectStatistics: List<StatisticsManager.ProjectStatistics>,
            val weeklyOverview: StatisticsManager.WeeklyOverview,
            val monthlyTrends: StatisticsManager.MonthlyTrends
        ) : StatisticsUiState()
        data class Error(val message: String) : StatisticsUiState()
    }
}
