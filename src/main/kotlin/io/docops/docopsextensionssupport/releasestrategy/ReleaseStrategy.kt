package io.docops.docopsextensionssupport.releasestrategy

import kotlinx.serialization.Serializable
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFClientAnchor
import org.apache.poi.xssf.usermodel.XSSFDrawing
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream


/**
 * An enumeration representing different release stages.
 */
enum class ReleaseEnum {
    M1, M2, M3, M4, M5, M6, M7, M8, M9,
    RC1, RC2, RC3, RC4, RC5, RC6, RC7, RC8, RC9,
    GA;

    fun color(releaseEnum: ReleaseEnum): String {
        return when (releaseEnum) {
            in arrayOf(M1,M2,M3,M4,M5,M6,M7,M8,M9) -> {
                "#6cadde"
            }
            in arrayOf(RC1,RC2,RC3,RC4,RC5,RC6,RC7,RC8,RC9) -> {
                "#C766A0"
            }
            GA -> {
                "#3dd915"
            }

            else -> "#136e33"
        }
    }
    
    fun clazz(releaseEnum: ReleaseEnum): String{
        return when (releaseEnum) {
            in arrayOf(M1,M2,M3,M4,M5,M6,M7,M8,M9) -> {
                "bev"
            }
            in arrayOf(RC1,RC2,RC3,RC4,RC5,RC6,RC7,RC8,RC9) -> {
                "bev2"
            }
            GA -> {
                "bev3"
            }

            else -> "bev"
        }
    }
    fun speed(releaseEnum: ReleaseEnum): String {
        return when (releaseEnum) {
            in arrayOf(M1,M2,M3,M4,M5,M6,M7,M8,M9) -> {
                "40s"
            }
            in arrayOf(RC1,RC2,RC3,RC4,RC5,RC6,RC7,RC8,RC9) -> {
                "36s"
            }
            GA -> {
                "30s"
            }

            else -> "40s"
        }
    }
}

/**
 * This class represents a selected strategy.
 *
 * @property releaseEnum The release enumeration string associated with the strategy.
 * @property selected A boolean flag indicating whether the strategy is selected or not. The default value is false.
 */
class SelectedStrategy(val releaseEnum: String, val selected: Boolean = false)
/**
 * Represents a release.
 *
 * @property type The type of the release.
 * @property lines The lines describing the release.
 * @property date The date of the release in the format "yyyy-MM-dd".
 * @property selected Whether the release is selected or not.
 * @property goal The goal of the release.
 * @property completed Whether the release is completed or not.
 */
@Serializable
class Release(
    val type: ReleaseEnum,
    val lines: MutableList<String>,
    val date: String,
    val selected: Boolean = false,
    val goal: String,
    val completed: Boolean = false
)

/**
 * A class that represents the display configuration for a program.
 *
 * @property fontColor The font color to be used in the display. Defaults to "#fcfcfc".
 * @property milestoneColor The color to be used for milestones in the display. Defaults to "#fcfcfc".
 * @property colors The list of colors to be used in the display. Defaults to ["#fc86be", "#dc93f6", "#aeb1ed"].
 * @property circleColors The list of colors to be used for circles in the display. Defaults to ["#fc86be", "#dc93f6", "#aeb1ed"].
 * @property carColors The list of colors to be used for cars in the display. Defaults to ["#fcfcfc", "#000000", "#ff0000"].
 */
@Serializable
class DisplayConfig (val fontColor: String = "#fcfcfc", val milestoneColor: String= "#fcfcfc", val colors : List<String> = mutableListOf("#fc86be", "#dc93f6", "#aeb1ed"), val circleColors : List<String> = mutableListOf("#fc86be", "#dc93f6", "#aeb1ed"), val carColors : List<String> = mutableListOf("#fcfcfc", "#000000", "#ff0000"))
/**
 * Represents a release strategy.
 *
 * @param title The title of the release strategy.
 * @param releases The list of releases in the strategy.
 * @param style The style of the strategy. Default value is "TL".
 * @param scale The scale of the strategy. Default value is 1.0f.
 * @param numChars The number of characters in the strategy. Default value is 35.
 * @param displayConfig The display configuration for the strategy.
 * @param useDark Specifies if the dark theme should be used. Default value is false.
 */
@Serializable
class ReleaseStrategy (val title: String, val releases: MutableList<Release>, val style: String = "TL", val scale: Float = 1.0f, val numChars: Int= 35, val displayConfig: DisplayConfig = DisplayConfig(), var useDark: Boolean = false)

fun ReleaseStrategy.styles(): MutableMap<String, String> = mutableMapOf("TL" to "Timeline", "TLS" to "Timeline Summary",  "R" to "Roadmap", "TLG" to "Timeline Grouped")

fun ReleaseStrategy.excel(output: String): ByteArray {
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("Release Strategy $title")
    val style: CellStyle = workbook.createCellStyle()
    style.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GOLD.index)
    style.fillPattern = FillPatternType.SOLID_FOREGROUND

    val header: Row = sheet.createRow(0)
    header.createCell(0).setCellValue("Date")
    header.createCell(1).setCellValue("Type")
    header.createCell(2).setCellValue("Goal")
    header.createCell(3).setCellValue("Content")

    for (c in 0..3) {
        header.getCell(c).cellStyle = style
    }


    val dataRow: Row = sheet.createRow(1)
    dataRow.createCell(0).setCellValue("")
    dataRow.createCell(1).setCellValue("")
    dataRow.createCell(2).setCellValue("")
    dataRow.createCell(3).setCellValue("")

    //sheet.setColumnWidth(3, 60)
    releases.forEachIndexed { index, release ->
        val row = sheet.createRow(index+2)
        row.createCell(0).setCellValue(release.date)
        row.createCell(1).setCellValue(release.type.toString())
        row.createCell(2).setCellValue(release.goal)
        val content = row.createCell(3)
        val cellStyle: CellStyle = content.sheet.workbook.createCellStyle()
        cellStyle.wrapText = true

        content.setCellStyle(cellStyle)
        content.setCellValue(release.lines.joinToString())
    }
    sheet.autoSizeColumn(3)
    sheet.autoSizeColumn(0)

    val outputStream: ByteArrayOutputStream = ByteArrayOutputStream()
    workbook.write(outputStream)
    workbook.close()
    return outputStream.toByteArray()
}
fun ReleaseStrategy.grouped(): Map<Char, List<Release>> {
    return releases.groupBy { it.type.toString()[0] }
}
