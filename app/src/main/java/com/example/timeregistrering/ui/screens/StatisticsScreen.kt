package com.example.timeregistrering.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.timeregistrering.R
import com.example.timeregistrering.ui.components.ErrorMessage
import com.example.timeregistrering.ui.components.LoadingIndicator
import com.example.timeregistrering.util.StatisticsManager.ProjectStatistics
import com.example.timeregistrering.util.StatisticsManager.WeeklyOverview
import com.example.timeregistrering.util.StatisticsManager.MonthlyTrends
import com.example.timeregistrering.viewmodel.StatisticsViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Statistik",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        when (val state = uiState) {
            is StatisticsViewModel.StatisticsUiState.Loading -> {
                item { LoadingIndicator() }
            }
            is StatisticsViewModel.StatisticsUiState.Error -> {
                item { ErrorMessage(message = state.message) }
            }
            is StatisticsViewModel.StatisticsUiState.Success -> {
                // Projekt statistik
                item {
                    ProjectStatisticsSection(
                        statistics = state.projectStatistics,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }

                // Projekt fordeling pie chart
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Projekt Fordeling",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            ProjectPieChart(
                                statistics = state.projectStatistics
                            )
                        }
                    }
                }

                // Ugentlig oversigt
                item {
                    WeeklyOverviewSection(
                        overview = state.weeklyOverview,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }

                // Ugentlig timer bar chart
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Timer per Dag",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            WeeklyBarChart(
                                weeklyOverview = state.weeklyOverview
                            )
                        }
                    }
                }

                // Månedlige trends
                item {
                    MonthlyTrendsSection(
                        trends = state.monthlyTrends,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }

                // Månedlig udvikling line chart
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Månedlig Udvikling",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            MonthlyLineChart(
                                monthlyTrends = state.monthlyTrends
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectStatisticsSection(
    statistics: List<ProjectStatistics>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Projekt Statistik",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        statistics.forEach { stat ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stat.project.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatisticItem(
                            label = "Total timer",
                            value = String.format("%.1f", stat.totalHours)
                        )
                        StatisticItem(
                            label = "Gns. timer/dag",
                            value = String.format("%.1f", stat.averageHoursPerDay)
                        )
                        StatisticItem(
                            label = "Antal reg.",
                            value = stat.registrationCount.toString()
                        )
                    }
                    stat.mostActiveDay?.let {
                        Text(
                            text = "Mest aktive dag: ${
                                it.getDisplayName(TextStyle.FULL, Locale.getDefault())
                            }",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyOverviewSection(
    overview: WeeklyOverview,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Ugentlig Oversigt",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Uge ${overview.weekStartDate.format(DateTimeFormatter.ofPattern("w, yyyy"))}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Daglig oversigt
                overview.dailyHours.forEach { (day, hours) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = day.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = String.format("%.1f timer", hours),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatisticItem(
                        label = "Total timer",
                        value = String.format("%.1f", overview.totalHours)
                    )
                    StatisticItem(
                        label = "Gns. timer/dag",
                        value = String.format("%.1f", overview.averageHoursPerDay)
                    )
                    StatisticItem(
                        label = "Antal reg.",
                        value = overview.completedRegistrations.toString()
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyTrendsSection(
    trends: MonthlyTrends,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Månedlige Trends",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = LocalDate.of(trends.year, trends.month, 1)
                        .format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Ugentlig oversigt
                trends.weeklyHours.forEach { (week, hours) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Uge $week",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = String.format("%.1f timer", hours),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatisticItem(
                        label = "Total timer",
                        value = String.format("%.1f", trends.totalHours)
                    )
                    StatisticItem(
                        label = "Gns. timer/uge",
                        value = String.format("%.1f", trends.averageHoursPerWeek)
                    )
                    trends.mostProductiveWeek?.let {
                        StatisticItem(
                            label = "Bedste uge",
                            value = "Uge $it"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProjectPieChart(
    statistics: List<ProjectStatistics>
) {
    // Implement ProjectPieChart
}

@Composable
private fun WeeklyBarChart(
    weeklyOverview: WeeklyOverview
) {
    // Implement WeeklyBarChart
}

@Composable
private fun MonthlyLineChart(
    monthlyTrends: MonthlyTrends
) {
    // Implement MonthlyLineChart
}
