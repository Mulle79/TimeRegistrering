package com.example.timeregistrering.common.excel

import android.content.Context
import android.net.Uri
import com.example.timeregistrering.model.TimeRegistration
import com.example.timeregistrering.util.StatisticsManager.*
import dagger.hilt.android.qualifiers.ApplicationContext
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExcelManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val holidayManager: HolidayManager,
    private val fileEncryptionManager: FileEncryptionManager
) {
    companion object {
        private val HEADER_STYLE = createHeaderStyle()
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
    }

    /**
     * Genererer en Excel fil med timeregistreringer
     */
    suspend fun generateExcelFile(registrations: List<TimeRegistration>): File {
        return withContext(Dispatchers.IO) {
            val workbook = XSSFWorkbook()
            
            try {
                // Opret temp fil til Excel data
                val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.xlsx")
                
                // Generer Excel indhold
                createExcelContent(workbook, registrations)
                
                // Gem til temp fil
                FileOutputStream(tempFile).use { 
                    workbook.write(it)
                }
                
                // Opret krypteret destination fil
                var counter = 0
                var file: File
                do {
                    val fileName = if (counter == 0) {
                        "timeregistrering.xlsx"
                    } else {
                        "timeregistrering_${counter}.xlsx"
                    }
                    file = File(context.getExternalFilesDir(null), fileName)
                    counter++
                } while (file.exists())
                
                // Krypter filen
                fileEncryptionManager.encryptFile(tempFile, file)
                
                // Slet temp filen sikkert
                fileEncryptionManager.secureDelete(tempFile)
                
                file
            } finally {
                workbook.close()
            }
        }
    }

    private fun createExcelContent(workbook: Workbook, registrations: List<TimeRegistration>) {
        val sheet = workbook.createSheet("Timeseddel")

        // Opret header
        createHeader(sheet)

        // Udfyld data
        var rowNum = 1
        for (registration in registrations) {
            createDataRow(sheet, rowNum++, registration)
        }

        // Tilføj formler
        addFormulas(sheet, rowNum)
    }

    private fun createHeader(sheet: Sheet) {
        val row = sheet.createRow(0)
        val headers = listOf(
            "Dato", "Fra kl.", "Til kl.", "Timer i alt", "Mer-arbejde",
            "Delt tjeneste", "Over-arbejde 50%", "Mangl. varsel",
            "Hverdage 17-06 og Lørdag 00-06", "Lør 06 - man 06",
            "Helligdage 00-24", "Aften- og nat-tjeneste 17-06",
            "Hverdage", "Søn- og helligdage", "Ferie", "Ferie-fri",
            "Sygdom", "Barsel", "Afspadsering", "Bemærkning"
        )
        
        headers.forEachIndexed { index, header ->
            val cell = row.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = HEADER_STYLE
            sheet.setColumnWidth(index, 15 * 256) // 15 tegn bred
        }
    }

    private fun createDataRow(sheet: Sheet, rowNum: Int, registration: TimeRegistration) {
        val row = sheet.createRow(rowNum)
        
        // Grundlæggende data
        row.createCell(0).setCellValue(registration.date.format(DATE_FORMATTER))
        row.createCell(1).setCellValue(registration.startTime.format(TIME_FORMATTER))
        row.createCell(2).setCellValue(registration.endTime.format(TIME_FORMATTER))
        
        // Timer i alt formel
        val totalHoursCell = row.createCell(3)
        totalHoursCell.cellFormula = 
            "IF(AND(B${rowNum+1}<>\"\",C${rowNum+1}<>\"\")," +
            "(C${rowNum+1}-B${rowNum+1})*24,0)"
        
        // Mer-arbejde formel
        val overtimeCell = row.createCell(4)
        overtimeCell.cellFormula = 
            "IF(D${rowNum+1}>7.4,D${rowNum+1}-7.4,0)"
        
        // Delt tjeneste
        row.createCell(5)
        
        // Over-arbejde 50%
        val overtime50Cell = row.createCell(6)
        overtime50Cell.cellFormula =
            "IF(D${rowNum+1}>11,D${rowNum+1}-11,0)"
        
        // Manglende varsel
        row.createCell(7)
        
        // Aften/nat og weekend tillæg
        val eveningNightCell = row.createCell(8)
        eveningNightCell.cellFormula =
            "IF(OR(AND(B${rowNum+1}>=17,B${rowNum+1}<=6)," +
            "AND(C${rowNum+1}>=17,C${rowNum+1}<=6)),D${rowNum+1},0)"
        
        // Weekend tillæg
        val weekendCell = row.createCell(9)
        weekendCell.cellFormula =
            "IF(WEEKDAY(A${rowNum+1},2)>=6,D${rowNum+1},0)"
        
        // Helligdage
        row.createCell(10)
        
        // Aften- og nattjeneste
        val eveningNightShiftCell = row.createCell(11)
        eveningNightShiftCell.cellFormula =
            "IF(AND(B${rowNum+1}>=17,C${rowNum+1}<=6),D${rowNum+1},0)"
        
        // Resterende kolonner
        (12..19).forEach { row.createCell(it) }
        
        // Bemærkning
        if (registration.note != null) {
            row.createCell(19).setCellValue(registration.note)
        }
    }

    private fun addFormulas(sheet: Sheet, lastRow: Int) {
        val sumRow = sheet.createRow(lastRow)
        sumRow.createCell(0).setCellValue("Total")
        
        // Summer alle kolonner med tal
        (3..18).forEach { col ->
            val cell = sumRow.createCell(col)
            val colLetter = ('A' + col).toString()
            cell.cellFormula = "SUM(${colLetter}2:${colLetter}$lastRow)"
        }
    }

    private companion object {
        private fun createHeaderStyle(): CellStyle {
            val workbook = XSSFWorkbook()
            val style = workbook.createCellStyle()
            val font = workbook.createFont()
            
            font.bold = true
            style.setFont(font)
            style.fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            style.fillPattern = FillPatternType.SOLID_FOREGROUND
            style.borderBottom = BorderStyle.THIN
            style.borderTop = BorderStyle.THIN
            style.borderLeft = BorderStyle.THIN
            style.borderRight = BorderStyle.THIN
            
            return style
        }
    }

    fun exportStatistics(
        projectStatistics: List<ProjectStatistics>,
        weeklyOverview: WeeklyOverview,
        monthlyTrends: MonthlyTrends,
        outputUri: Uri
    ) {
        val workbook = XSSFWorkbook()
        
        // Projekt statistik
        createProjectStatisticsSheet(workbook, projectStatistics)
        
        // Ugentlig oversigt
        createWeeklyOverviewSheet(workbook, weeklyOverview)
        
        // Månedlige trends
        createMonthlyTrendsSheet(workbook, monthlyTrends)

        // Gem workbook
        context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
            workbook.write(outputStream)
        }
        workbook.close()
    }

    private fun createProjectStatisticsSheet(
        workbook: Workbook,
        statistics: List<ProjectStatistics>
    ) {
        val sheet = workbook.createSheet("Projekt Statistik")
        var rowNum = 0

        // Header
        val headerRow = sheet.createRow(rowNum++)
        headerRow.createCell(0).setCellValue("Projekt")
        headerRow.createCell(1).setCellValue("Total Timer")
        headerRow.createCell(2).setCellValue("Gns. Timer/Dag")
        headerRow.createCell(3).setCellValue("Mest Aktive Dag")
        headerRow.createCell(4).setCellValue("Antal Registreringer")

        // Data
        statistics.forEach { stat ->
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(stat.project.name)
            row.createCell(1).setCellValue(stat.totalHours)
            row.createCell(2).setCellValue(stat.averageHoursPerDay)
            row.createCell(3).setCellValue(stat.mostActiveDay?.name ?: "")
            row.createCell(4).setCellValue(stat.registrationCount.toDouble())
        }

        // Auto-size kolonner
        (0..4).forEach { sheet.autoSizeColumn(it) }
    }

    private fun createWeeklyOverviewSheet(
        workbook: Workbook,
        overview: WeeklyOverview
    ) {
        val sheet = workbook.createSheet("Ugentlig Oversigt")
        var rowNum = 0

        // Uge information
        val titleRow = sheet.createRow(rowNum++)
        titleRow.createCell(0).setCellValue("Uge start: ${overview.weekStartDate.format(dateFormatter)}")

        rowNum++ // Tom række

        // Daglig oversigt header
        val headerRow = sheet.createRow(rowNum++)
        headerRow.createCell(0).setCellValue("Dag")
        headerRow.createCell(1).setCellValue("Timer")

        // Daglig data
        overview.dailyHours.forEach { (day, hours) ->
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(day.name)
            row.createCell(1).setCellValue(hours)
        }

        rowNum++ // Tom række

        // Totaler
        val totalRow = sheet.createRow(rowNum++)
        totalRow.createCell(0).setCellValue("Total Timer")
        totalRow.createCell(1).setCellValue(overview.totalHours)

        val avgRow = sheet.createRow(rowNum++)
        avgRow.createCell(0).setCellValue("Gns. Timer/Dag")
        avgRow.createCell(1).setCellValue(overview.averageHoursPerDay)

        val regRow = sheet.createRow(rowNum)
        regRow.createCell(0).setCellValue("Antal Registreringer")
        regRow.createCell(1).setCellValue(overview.completedRegistrations.toDouble())

        // Auto-size kolonner
        (0..1).forEach { sheet.autoSizeColumn(it) }
    }

    private fun createMonthlyTrendsSheet(
        workbook: Workbook,
        trends: MonthlyTrends
    ) {
        val sheet = workbook.createSheet("Månedlige Trends")
        var rowNum = 0

        // Måned information
        val titleRow = sheet.createRow(rowNum++)
        titleRow.createCell(0).setCellValue("${trends.year} - Måned ${trends.month}")

        rowNum++ // Tom række

        // Ugentlig oversigt header
        val headerRow = sheet.createRow(rowNum++)
        headerRow.createCell(0).setCellValue("Uge")
        headerRow.createCell(1).setCellValue("Timer")

        // Ugentlig data
        trends.weeklyHours.forEach { (week, hours) ->
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue("Uge $week")
            row.createCell(1).setCellValue(hours)
        }

        rowNum++ // Tom række

        // Totaler
        val totalRow = sheet.createRow(rowNum++)
        totalRow.createCell(0).setCellValue("Total Timer")
        totalRow.createCell(1).setCellValue(trends.totalHours)

        val avgRow = sheet.createRow(rowNum++)
        avgRow.createCell(0).setCellValue("Gns. Timer/Uge")
        avgRow.createCell(1).setCellValue(trends.averageHoursPerWeek)

        trends.mostProductiveWeek?.let {
            val bestRow = sheet.createRow(rowNum)
            bestRow.createCell(0).setCellValue("Mest Produktive Uge")
            bestRow.createCell(1).setCellValue("Uge $it")
        }

        // Auto-size kolonner
        (0..1).forEach { sheet.autoSizeColumn(it) }
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
}

data class TimeRegistration(
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val note: String? = null
)
