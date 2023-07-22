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

class SelectedStrategy(val releaseEnum: String, val selected: Boolean = false)
@Serializable
class Release(
    val type: ReleaseEnum,
    val lines: MutableList<String>,
    val date: String,
    val selected: Boolean = false,
    val goal: String
)
@Serializable
class ReleaseStrategy (val title: String, val releases: MutableList<Release>, val style: String = "TL", val scale: Float = 1.0f, val numChars: Int= 35)

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
