package com.example.timeregistrering.util

import android.content.Context
import android.net.Uri
import com.example.timeregistrering.util.StatisticsManager.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CSVExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun exportStatistics(
        projectStatistics: List<ProjectStatistics>,
        weeklyOverview: WeeklyOverview,
        monthlyTrends: MonthlyTrends,
        outputUri: Uri
    ) {
        context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                // Projekt statistik
                writer.write("Projekt Statistik\n")
                writer.write("Projekt,Total Timer,Gns. Timer/Dag,Mest Aktive Dag,Antal Registreringer\n")
                projectStatistics.forEach { stat ->
                    writer.write("${escapeCSV(stat.project.name)},")
                    writer.write("${stat.totalHours},")
                    writer.write("${stat.averageHoursPerDay},")
                    writer.write("${stat.mostActiveDay?.name ?: ""},")
                    writer.write("${stat.registrationCount}\n")
                }
                writer.write("\n")

                // Ugentlig oversigt
                writer.write("Ugentlig Oversigt\n")
                writer.write("Uge start: ${weeklyOverview.weekStartDate.format(dateFormatter)}\n")
                writer.write("Dag,Timer\n")
                weeklyOverview.dailyHours.forEach { (day, hours) ->
                    writer.write("${day.name},${hours}\n")
                }
                writer.write("\n")
                writer.write("Total Timer,${weeklyOverview.totalHours}\n")
                writer.write("Gns. Timer/Dag,${weeklyOverview.averageHoursPerDay}\n")
                writer.write("Antal Registreringer,${weeklyOverview.completedRegistrations}\n")
                writer.write("\n")

                // Månedlige trends
                writer.write("Månedlige Trends\n")
                writer.write("År,${monthlyTrends.year}\n")
                writer.write("Måned,${monthlyTrends.month}\n")
                writer.write("Uge,Timer\n")
                monthlyTrends.weeklyHours.forEach { (week, hours) ->
                    writer.write("$week,$hours\n")
                }
                writer.write("\n")
                writer.write("Total Timer,${monthlyTrends.totalHours}\n")
                writer.write("Gns. Timer/Uge,${monthlyTrends.averageHoursPerWeek}\n")
                monthlyTrends.mostProductiveWeek?.let {
                    writer.write("Mest Produktive Uge,$it\n")
                }
            }
        }
    }

    private fun escapeCSV(text: String): String {
        return if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            "\"${text.replace("\"", "\"\"")}\""
        } else {
            text
        }
    }
}
