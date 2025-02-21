package com.example.timeregistrering.ui.components.charts

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.timeregistrering.util.StatisticsManager.MonthlyTrends
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@Composable
fun MonthlyLineChart(
    monthlyTrends: MonthlyTrends,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setDrawGridBackground(false)
                
                // X-akse
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                }
                
                // Y-akse
                axisLeft.apply {
                    setDrawGridLines(true)
                    axisMinimum = 0f
                }
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val entries = monthlyTrends.weeklyHours.entries
                .sortedBy { it.key }
                .mapIndexed { index, entry ->
                    Entry(index.toFloat(), entry.value.toFloat())
                }

            val labels = monthlyTrends.weeklyHours.keys
                .sorted()
                .map { "Uge $it" }

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

            val dataSet = LineDataSet(entries, "Timer per uge").apply {
                color = Color.rgb(64, 89, 128)
                setCircleColor(Color.rgb(64, 89, 128))
                lineWidth = 2f
                circleRadius = 4f
                valueTextSize = 12f
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}
