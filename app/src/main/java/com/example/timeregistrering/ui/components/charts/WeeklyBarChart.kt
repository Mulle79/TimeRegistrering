package com.example.timeregistrering.ui.components.charts

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.timeregistrering.util.StatisticsManager.WeeklyOverview
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

@Composable
fun WeeklyBarChart(
    weeklyOverview: WeeklyOverview,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            BarChart(context).apply {
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
            val entries = weeklyOverview.dailyHours.entries.mapIndexed { index, entry ->
                BarEntry(index.toFloat(), entry.value.toFloat())
            }

            val labels = weeklyOverview.dailyHours.keys.map { day ->
                day.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

            val dataSet = BarDataSet(entries, "Timer per dag").apply {
                color = Color.rgb(64, 89, 128)
                valueTextSize = 12f
            }

            chart.data = BarData(dataSet)
            chart.invalidate()
        }
    )
}
