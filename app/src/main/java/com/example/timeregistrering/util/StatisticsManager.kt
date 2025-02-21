package com.example.timeregistrering.util

import com.example.timeregistrering.data.repository.ProjectRepository
import com.example.timeregistrering.data.repository.TimeRegistrationRepository
import com.example.timeregistrering.model.Project
import com.example.timeregistrering.model.TimeRegistration
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsManager @Inject constructor(
    private val timeRegistrationRepository: TimeRegistrationRepository,
    private val projectRepository: ProjectRepository
) {
    suspend fun getProjectStatistics(startDate: LocalDate, endDate: LocalDate): List<ProjectStatistics> {
        val registrations = timeRegistrationRepository.getTimeRegistrationsForPeriod(startDate, endDate)
        val projects = projectRepository.getProjects().first()
        
        return projects.map { project ->
            val projectRegistrations = registrations.filter { it.projectId == project.id }
            ProjectStatistics(
                project = project,
                totalHours = calculateTotalHours(projectRegistrations),
                averageHoursPerDay = calculateAverageHoursPerDay(projectRegistrations, startDate, endDate),
                mostActiveDay = findMostActiveDay(projectRegistrations),
                registrationCount = projectRegistrations.size
            )
        }
    }

    suspend fun getWeeklyOverview(weekStartDate: LocalDate): WeeklyOverview {
        val registrations = timeRegistrationRepository.getTimeRegistrationsForWeek(weekStartDate)
        
        val dailyHours = (0..6).associate { dayOffset ->
            val date = weekStartDate.plusDays(dayOffset.toLong())
            val dayRegistrations = registrations.filter { 
                it.startTime.toLocalDate() == date 
            }
            date.dayOfWeek to calculateTotalHours(dayRegistrations)
        }

        return WeeklyOverview(
            weekStartDate = weekStartDate,
            totalHours = dailyHours.values.sum(),
            dailyHours = dailyHours,
            averageHoursPerDay = dailyHours.values.average(),
            mostProductiveDay = dailyHours.maxByOrNull { it.value }?.key,
            completedRegistrations = registrations.count { it.endTime != null }
        )
    }

    suspend fun getMonthlyTrends(year: Int, month: Int): MonthlyTrends {
        val registrations = timeRegistrationRepository.getTimeRegistrationsForMonth(year, month)
        val weeklyHours = mutableMapOf<Int, Double>()
        var currentWeek = 1
        var weekRegistrations = mutableListOf<TimeRegistration>()

        registrations.sortedBy { it.startTime }.forEach { registration ->
            val weekOfMonth = registration.startTime.toLocalDate().get(java.time.temporal.WeekFields.ISO.weekOfMonth())
            if (weekOfMonth > currentWeek) {
                weeklyHours[currentWeek] = calculateTotalHours(weekRegistrations)
                weekRegistrations.clear()
                currentWeek = weekOfMonth
            }
            weekRegistrations.add(registration)
        }
        // Add last week
        if (weekRegistrations.isNotEmpty()) {
            weeklyHours[currentWeek] = calculateTotalHours(weekRegistrations)
        }

        return MonthlyTrends(
            year = year,
            month = month,
            weeklyHours = weeklyHours,
            totalHours = weeklyHours.values.sum(),
            averageHoursPerWeek = weeklyHours.values.average(),
            mostProductiveWeek = weeklyHours.maxByOrNull { it.value }?.key
        )
    }

    private fun calculateTotalHours(registrations: List<TimeRegistration>): Double {
        return registrations.sumOf { registration ->
            val end = registration.endTime ?: LocalDate.now().atStartOfDay()
            ChronoUnit.MINUTES.between(registration.startTime, end).toDouble() / 60
        }
    }

    private fun calculateAverageHoursPerDay(
        registrations: List<TimeRegistration>,
        startDate: LocalDate,
        endDate: LocalDate
    ): Double {
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1
        return calculateTotalHours(registrations) / totalDays
    }

    private fun findMostActiveDay(registrations: List<TimeRegistration>): DayOfWeek? {
        return registrations
            .groupBy { it.startTime.dayOfWeek }
            .maxByOrNull { (_, regs) -> calculateTotalHours(regs) }
            ?.key
    }

    data class ProjectStatistics(
        val project: Project,
        val totalHours: Double,
        val averageHoursPerDay: Double,
        val mostActiveDay: DayOfWeek?,
        val registrationCount: Int
    )

    data class WeeklyOverview(
        val weekStartDate: LocalDate,
        val totalHours: Double,
        val dailyHours: Map<DayOfWeek, Double>,
        val averageHoursPerDay: Double,
        val mostProductiveDay: DayOfWeek?,
        val completedRegistrations: Int
    )

    data class MonthlyTrends(
        val year: Int,
        val month: Int,
        val weeklyHours: Map<Int, Double>,
        val totalHours: Double,
        val averageHoursPerWeek: Double,
        val mostProductiveWeek: Int?
    )
}
