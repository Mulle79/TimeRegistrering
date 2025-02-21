package com.example.timeregistrering.util

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.timeregistrering.util.StatisticsManager.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint

@Singleton
class PDFExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val paint = Paint().apply {
        textSize = 12f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    private val boldPaint = Paint().apply {
        textSize = 14f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val titlePaint = Paint().apply {
        textSize = 18f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    fun exportStatistics(
        projectStatistics: List<ProjectStatistics>,
        weeklyOverview: WeeklyOverview,
        monthlyTrends: MonthlyTrends,
        outputUri: Uri
    ) {
        val document = PdfDocument()
        var currentPage = 1
        var yPosition = 50f

        // Opret første side
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPage).create()
        var page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Titel
        canvas.drawText("Statistik Rapport", 50f, yPosition, titlePaint)
        yPosition += 40f

        // Projekt statistik
        canvas.drawText("Projekt Statistik", 50f, yPosition, boldPaint)
        yPosition += 30f

        projectStatistics.forEach { stat ->
            if (yPosition > 750f) {
                // Ny side hvis vi er tæt på bunden
                document.finishPage(page)
                currentPage++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPage).create()
                page = document.startPage(pageInfo)
                yPosition = 50f
            }

            canvas.drawText("${stat.project.name}:", 50f, yPosition, paint)
            canvas.drawText("${String.format("%.1f", stat.totalHours)} timer total", 250f, yPosition, paint)
            yPosition += 20f
            canvas.drawText("Gns. ${String.format("%.1f", stat.averageHoursPerDay)} timer/dag", 250f, yPosition, paint)
            yPosition += 30f
        }

        // Ugentlig oversigt
        if (yPosition > 700f) {
            document.finishPage(page)
            currentPage++
            pageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPage).create()
            page = document.startPage(pageInfo)
            yPosition = 50f
        }

        canvas.drawText("Ugentlig Oversigt", 50f, yPosition, boldPaint)
        yPosition += 30f

        weeklyOverview.dailyHours.forEach { (day, hours) ->
            canvas.drawText("${day.name}:", 50f, yPosition, paint)
            canvas.drawText("${String.format("%.1f", hours)} timer", 250f, yPosition, paint)
            yPosition += 20f
        }

        yPosition += 20f
        canvas.drawText("Total: ${String.format("%.1f", weeklyOverview.totalHours)} timer", 50f, yPosition, paint)
        yPosition += 20f
        canvas.drawText("Gennemsnit: ${String.format("%.1f", weeklyOverview.averageHoursPerDay)} timer/dag", 50f, yPosition, paint)

        // Månedlige trends
        if (yPosition > 700f) {
            document.finishPage(page)
            currentPage++
            pageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPage).create()
            page = document.startPage(pageInfo)
            yPosition = 50f
        }

        yPosition += 40f
        canvas.drawText("Månedlige Trends", 50f, yPosition, boldPaint)
        yPosition += 30f

        monthlyTrends.weeklyHours.forEach { (week, hours) ->
            canvas.drawText("Uge $week:", 50f, yPosition, paint)
            canvas.drawText("${String.format("%.1f", hours)} timer", 250f, yPosition, paint)
            yPosition += 20f
        }

        yPosition += 20f
        canvas.drawText("Total: ${String.format("%.1f", monthlyTrends.totalHours)} timer", 50f, yPosition, paint)
        yPosition += 20f
        canvas.drawText("Gennemsnit: ${String.format("%.1f", monthlyTrends.averageHoursPerWeek)} timer/uge", 50f, yPosition, paint)

        // Afslut sidste side
        document.finishPage(page)

        // Gem dokumentet
        context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
            document.writeTo(outputStream)
        }
        document.close()
    }
}
