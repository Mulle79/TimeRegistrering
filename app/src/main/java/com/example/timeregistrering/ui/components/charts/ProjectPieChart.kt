package com.example.timeregistrering.ui.components.charts

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.timeregistrering.util.StatisticsManager.ProjectStatistics
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

@Composable
fun ProjectPieChart(
    statistics: List<ProjectStatistics>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                setUsePercentValues(true)
                legend.isEnabled = true
                setDrawEntryLabels(false)
                setHoleColor(Color.TRANSPARENT)
            }
        },
        update = { chart ->
            val entries = statistics.map { stat ->
                PieEntry(stat.totalHours.toFloat(), stat.project.name)
            }

            val colors = listOf(
                Color.rgb(64, 89, 128),
                Color.rgb(149, 165, 124),
                Color.rgb(217, 184, 162),
                Color.rgb(191, 134, 134),
                Color.rgb(179, 48, 80)
            )

            val dataSet = PieDataSet(entries, "Projekter").apply {
                setColors(colors)
                valueFormatter = PercentFormatter(chart)
                valueTextSize = 12f
                valueTextColor = Color.WHITE
            }

            chart.data = PieData(dataSet)
            chart.invalidate()
        }
    )
}
