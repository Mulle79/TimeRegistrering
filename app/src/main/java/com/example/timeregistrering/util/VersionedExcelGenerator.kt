package com.example.timeregistrering.util

import android.content.Context
import android.graphics.Color
import com.example.timeregistrering.model.TimeRegistration
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VersionedExcelGenerator @Inject constructor(
    private val context: Context
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val fileFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")

    fun generateTimesheet(
        timeRegistrations: List<TimeRegistration>,
        baseFileName: String = "timeseddel"
    ): File {
        // Valider input data
        validateTimeRegistrations(timeRegistrations)

        val workbook = XSSFWorkbook()
        configureWorkbookStyles(workbook)
        
        val sheet = workbook.createSheet("Timeseddel")
        setupSheetFormatting(sheet)

        // Opret styles
        val styles = createStyles(workbook)

        // Tilføj headers
        addHeaders(sheet, styles.headerStyle)

        // Tilføj data med formler
        addDataWithFormulas(sheet, timeRegistrations, styles)

        // Tilføj summeringsformler
        addSummaryFormulas(sheet, timeRegistrations.size + 1)

        // Beskyt arket
        sheet.protectSheet("password")

        // Gem filen med versionering
        val version = getNextVersion(baseFileName)
        val fileName = "${baseFileName}_${LocalDateTime.now().format(fileFormatter)}_v$version.xlsx"
        
        val file = File(context.getExternalFilesDir(null), fileName)
        
        // Backup eksisterende fil hvis den findes
        if (file.exists()) {
            val backupFile = File(file.parentFile, "${file.nameWithoutExtension}_backup.xlsx")
            file.copyTo(backupFile, overwrite = true)
        }

        FileOutputStream(file).use { 
            workbook.write(it)
        }
        workbook.close()

        return file
    }

    private fun validateTimeRegistrations(timeRegistrations: List<TimeRegistration>) {
        timeRegistrations.forEach { registration ->
            require(registration.startTime != null) {
                "Starttid mangler for registrering"
            }
            registration.endTime?.let { endTime ->
                require(endTime.isAfter(registration.startTime)) {
                    "Sluttid skal være efter starttid"
                }
            }
        }
    }

    private fun configureWorkbookStyles(workbook: XSSFWorkbook) {
        workbook.createCellStyle().apply {
            setFont(workbook.createFont().apply {
                fontName = "Calibri"
                fontHeightInPoints = 11
            })
        }
    }

    private fun setupSheetFormatting(sheet: Sheet) {
        // Frys første række
        sheet.createFreezePane(0, 1)
        
        // Indstil kolonne bredder
        sheet.setColumnWidth(0, 12 * 256) // Dato
        sheet.setColumnWidth(1, 8 * 256)  // Fra kl.
        sheet.setColumnWidth(2, 8 * 256)  // Til kl.
        sheet.setColumnWidth(3, 10 * 256) // Timer i alt
        
        // Indstil andre kolonner
        (4..18).forEach { i ->
            sheet.setColumnWidth(i, 15 * 256)
        }
        sheet.setColumnWidth(19, 30 * 256) // Bemærkning
    }

    private fun createStyles(workbook: XSSFWorkbook): ExcelStyles {
        return ExcelStyles(
            headerStyle = workbook.createCellStyle().apply {
                setFont(workbook.createFont().apply {
                    bold = true
                    fontName = "Calibri"
                    fontHeightInPoints = 11
                })
                fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                setBorder(BorderStyle.THIN)
                alignment = HorizontalAlignment.CENTER
                wrapText = true
            },
            dataStyle = workbook.createCellStyle().apply {
                setFont(workbook.createFont().apply {
                    fontName = "Calibri"
                    fontHeightInPoints = 11
                })
                setBorder(BorderStyle.THIN)
                alignment = HorizontalAlignment.CENTER
            },
            timeStyle = workbook.createCellStyle().apply {
                setFont(workbook.createFont().apply {
                    fontName = "Calibri"
                    fontHeightInPoints = 11
                })
                setBorder(BorderStyle.THIN)
                alignment = HorizontalAlignment.CENTER
                dataFormat = workbook.createDataFormat().getFormat("HH:mm")
            },
            formulaStyle = workbook.createCellStyle().apply {
                setFont(workbook.createFont().apply {
                    fontName = "Calibri"
                    fontHeightInPoints = 11
                })
                setBorder(BorderStyle.THIN)
                alignment = HorizontalAlignment.CENTER
                dataFormat = workbook.createDataFormat().getFormat("0.00")
            }
        )
    }

    private fun addHeaders(sheet: Sheet, headerStyle: CellStyle) {
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
    }

    private fun addDataWithFormulas(
        sheet: Sheet,
        timeRegistrations: List<TimeRegistration>,
        styles: ExcelStyles
    ) {
        timeRegistrations.sortedBy { it.startTime }.forEachIndexed { index, registration ->
            val rowNum = index + 1
            val row = sheet.createRow(rowNum)
            
            // Dato
            row.createCell(0).apply {
                setCellValue(registration.startTime.format(dateFormatter))
                cellStyle = styles.dataStyle
            }
            
            // Start tid
            row.createCell(1).apply {
                setCellValue(registration.startTime.format(timeFormatter))
                cellStyle = styles.timeStyle
            }
            
            // Slut tid
            registration.endTime?.let { endTime ->
                row.createCell(2).apply {
                    setCellValue(endTime.format(timeFormatter))
                    cellStyle = styles.timeStyle
                }
                
                // Timer i alt med korrekt formel
                row.createCell(3).apply {
                    // Konverter tidspunkter til decimaltimer
                    setCellFormula(
                        "IF(B${rowNum+1}<>\"\",IF(A${rowNum+1}<>\"\",(" +
                        "(HOUR(B${rowNum+1})+MINUTE(B${rowNum+1})/60)-" +
                        "(HOUR(A${rowNum+1})+MINUTE(A${rowNum+1})/60)),\"\"),\"\")"
                    )
                    cellStyle = styles.formulaStyle
                }
            }

            // Bemærkning
            if (registration.description.isNotEmpty()) {
                row.createCell(19).apply {
                    setCellValue(registration.description)
                    cellStyle = styles.dataStyle
                }
            }
        }
    }

    private fun addSummaryFormulas(sheet: Sheet, lastRow: Int) {
        val summaryRow = sheet.createRow(lastRow)
        
        // Total timer
        summaryRow.createCell(3).apply {
            setCellFormula("SUM(D2:D$lastRow)")
            cellStyle = sheet.getRow(0).getCell(3).cellStyle
        }
    }

    private fun getNextVersion(baseFileName: String): Int {
        val existingFiles = context.getExternalFilesDir(null)
            ?.listFiles { file -> file.name.startsWith(baseFileName) }
            ?.size ?: 0
        return existingFiles + 1
    }

    private fun CellStyle.setBorder(style: BorderStyle) {
        borderTop = style
        borderBottom = style
        borderLeft = style
        borderRight = style
    }

    private data class ExcelStyles(
        val headerStyle: CellStyle,
        val dataStyle: CellStyle,
        val timeStyle: CellStyle,
        val formulaStyle: CellStyle
    )
}
