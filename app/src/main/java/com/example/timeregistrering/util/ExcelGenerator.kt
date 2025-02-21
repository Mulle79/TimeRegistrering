package com.example.timeregistrering.util

import android.content.Context
import com.example.timeregistrering.model.TimeRegistration
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExcelGenerator @Inject constructor(
    private val context: Context
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun generateTimesheet(
        timeRegistrations: List<TimeRegistration>,
        fileName: String = "timeseddel.xlsx"
    ): File {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Timeseddel")

        // Opret header style
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setBorder(BorderStyle.THIN)
            setFont(workbook.createFont().apply {
                bold = true
            })
        }

        // Opret headers
        val headers = listOf(
            "Dato", "Fra kl.", "Til kl.", "Timer i alt", "Mer-arbejde",
            "Delt tjeneste", "Over-arbejde 50%", "Mangl. varsel",
            "Hverdage 17-06 og Lørdag 00-06", "Lør 06 - man 06",
            "Helligdage 00-24", "Aften- og nat-tjeneste 17-06",
            "Hverdage", "Søn- og helligdage", "Ferie", "Ferie-fri",
            "Sygdom", "Barsel", "Afspadsering", "Bemærkning"
        )
        
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).apply {
                setCellValue(header)
                cellStyle = headerStyle
            }
        }

        // Indstil kolonne bredder
        headers.indices.forEach { i ->
            sheet.setColumnWidth(i, 15 * 256) // 15 tegn bred
        }

        // Tilføj data
        timeRegistrations.sortedBy { it.startTime }.forEachIndexed { index, registration ->
            val row = sheet.createRow(index + 1)
            
            // Dato
            row.createCell(0).setCellValue(registration.startTime.format(dateFormatter))
            
            // Start tid
            row.createCell(1).setCellValue(registration.startTime.format(timeFormatter))
            
            // Slut tid
            registration.endTime?.let { endTime ->
                row.createCell(2).setCellValue(endTime.format(timeFormatter))
                
                // Beregn timer i alt
                val hours = calculateHours(registration.startTime, endTime)
                row.createCell(3).setCellValue(hours)
            }

            // Bemærkning
            if (registration.description.isNotEmpty()) {
                row.createCell(19).setCellValue(registration.description)
            }
        }

        // Gem filen
        val file = File(context.getExternalFilesDir(null), fileName)
        FileOutputStream(file).use { 
            workbook.write(it)
        }
        workbook.close()

        return file
    }

    private fun calculateHours(start: LocalDateTime, end: LocalDateTime): Double {
        val minutes = java.time.Duration.between(start, end).toMinutes()
        return minutes / 60.0
    }

    private fun CellStyle.setBorder(style: BorderStyle) {
        borderTop = style
        borderBottom = style
        borderLeft = style
        borderRight = style
    }
}
