package io.docops.docopsextensionssupport.svgtable

import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
import kotlinx.serialization.Serializable

/**
 * Represents a table that can be rendered as SVG.
 * @property rows List of rows in the table
 */
@Serializable
class Table(
    val rows: MutableList<Row> = mutableListOf(),
    var thead: THead? = null,
    val display: TableConfig = TableConfig()
) {


    /**
     * Adds a new row to the table
     * @param row The row to add
     * @throws IllegalArgumentException if the row is invalid
     */
    fun addRow(row: Row) {
        require(row.cells.isNotEmpty()) { "Row must contain at least one cell" }
        rows.add(row)
    }

    /**
     * Converts the table to SVG format
     * @return SVG representation of the table
     */
    fun toSvg(): String {
        val cellWidths = mutableListOf<CellWidth>()
        if (display.cellWidths.isEmpty()) {
            val cols = rows[0].cells.size
            val size = 1024/cols
            for (i in 0..cols step 1) {
                cellWidths.add(CellWidth(i, size.toFloat()))
            }
        } else {
            display.cellWidths.forEachIndexed { i, size ->
                val sz = 1024 * size
                cellWidths.add(CellWidth(i, sz))
            }
        }
        val headSvg = thead?.toSvg(cellWidths) ?: Pair("", 0f)
        val bodySvg = TBody(rows, maxOf(headSvg.second,18f), cellWidths).toSvg()
        return makeHead(display, cellWidths) + makeDefs() + """<g transform="scale(${display.scale})">""" + headSvg.first + bodySvg + "</g>" +endSvg()
    }

    private fun makeHead(display: TableConfig, cellWidths: MutableList<CellWidth>): String {
        val sv = StringBuilder()
        val h = tableHeight()
        val w =  tableWidth(cellWidths)
        sv.append("""
<?xml version="1.0" encoding="UTF-8"?>
<svg width="${w/DISPLAY_RATIO_16_9 * display.scale}" height="${h / DISPLAY_RATIO_16_9 * display.scale}" viewBox="0 0 ${w * display.scale} ${h * display.scale}" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">

        """.trimIndent())
        return sv.toString()
    }
    private fun endSvg(): String {
        return "</svg>"
    }

    private fun makeDefs(): String {
        return """
            <defs>
            ${getColorGradients()}
            <linearGradient id="headerGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop class="stop1" offset="0%" stop-color="#2563eb" stop-opacity="1"/>
                <stop class="stop2" offset="100%" stop-color="#9333ea" stop-opacity="1"/>
            </linearGradient>

            <linearGradient id="backgroundGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop class="stop3" offset="0%" stop-color="#fafafa" stop-opacity="1"/>
                <stop class="stop2" offset="50%" stop-color="#f9fafb" stop-opacity="1"/>
                <stop class="stop1" offset="100%" stop-color="#f3f4f6" stop-opacity="1"/>
            </linearGradient>

            <filter id="shadowFilter" x="-10%" y="-10%" width="120%" height="120%">
                <feDropShadow dx="0" dy="2" stdDeviation="2" flood-color="#000000" flood-opacity="0.1"/>
            </filter>

            <style>.rowShade {
                pointer-events: bounding-box;
            }

            .rowShade:hover {
                filter: grayscale(100%) sepia(100%);
            }</style>
            </defs>
        """.trimMargin()
    }
    /**
     * Calculates the total height of the table including header and body
     * @return Total height in pixels
     */
    fun tableHeight(): Float {
        var totalHeight = 30f // Initial offset

        // Add thead height if present
        thead?.rows?.forEach { row ->
            totalHeight += row.rowHeight()
        }

        // Add body rows height
        rows.forEach { row ->
            totalHeight += row.rowHeight()
        }

        return totalHeight
    }
    fun tableWidth(cellWidths: MutableList<CellWidth>): Float {
        val w = cellWidths.map { it.width }.sum()
        return w
    }
}

fun Table.getColorGradients() : String {
    val sb = StringBuilder()
    thead?.let {
        it.rows.forEachIndexed { i, row ->
            sb.append(row.display.fill.linearGradient)
            row.cells.forEach { cell ->
                sb.append(cell.display.fill.linearGradient)
            }
        }
    }
    return sb.toString()
}
@Serializable
class THead(val rows: MutableList<Row>, val display: DisplayConfig = DisplayConfig()) {
    companion object {
        private const val INITIAL_OFFSET = 8f
        private const val CELL_PADDING = 5
        private const val ROW_PADDING = 10.0f
        private const val HEADER_ROW_HEIGHT = 30.0f
    }

    fun toSvg(cellWidths: MutableList<CellWidth>): Pair<String, Float> {
        require(rows.isNotEmpty()) { "Table must contain at least one row" }
        val sb = StringBuilder()
        var currentY = INITIAL_OFFSET
        var maxLines = 0
        rows.forEach { row ->
                row.cells.forEachIndexed { i, cell ->
                    val x = cell.toLines(cellWidths[i].width-4)
                    maxLines = maxOf(maxLines, x.size)
                }
        }
        val rowHeight = maxLines * 14.0f + 10.0f
        rows.forEach { row ->
            sb.append("<g aria-label=\"Header\">")
            var currentX = INITIAL_OFFSET
            val rowColor = row.display.fill
            sb.append("<g aria-label=\"Header Row\">")
            sb.append("""<rect x="1" y="0" width="100%" height="$HEADER_ROW_HEIGHT" fill="url(#headerGradient)" rx="4" ry="4"/>""")

            var startX = 1.0
            row.cells.forEachIndexed { i, cell ->
                var cellColor = getColorForNumber(i)
                if(!cell.display.isDefault) {
                    cellColor = cell.display.fill.color
                }
                val fontColor = determineTextColor(cellColor)
                val fontStyle = cell.display.parseFontStyle()
                val lines = cell.toTextSpans(cell.toLines(cellWidths[i].width), (startX+2).toFloat(), currentY, style="font-family: 'Segoe UI', Arial, sans-serif; font-weight: 600; font-size: ${fontStyle.size}px; fill: ${fontColor}; letter-spacing: 0.5px;", dy = fontStyle.size)
                sb.append("<g class=\"rowShade\" aria-label=\"header column ${i+1} cellcolor $cellColor $fontColor\">")
                sb.append("""<rect x="$startX" y="0" fill="$cellColor" width="${cellWidths[i].width}" height="${maxOf(rowHeight,18f)}" stroke="#e5e7eb" stroke-width="1" rx="4" ry="4"/>""")
                sb.append(lines)
                currentX += cellWidths[i].width + CELL_PADDING
                startX += cellWidths[i].width
                sb.append("</g>")
            }

            sb.append("</g></g>")
            currentY += (row.cells.maxOfOrNull { it.height() } ?: 0.0f) + ROW_PADDING
        }

        return Pair(sb.toString(), rowHeight)
    }
}
/**
 * Internal implementation class for SVG table rendering.
 * @property rows List of rows to render
 */
internal class TBody(private val rows: MutableList<Row>, val headerHeight: Float, val cellWidths: MutableList<CellWidth>) {
    companion object {
        private const val INITIAL_OFFSET = 8.0f
        private const val CELL_PADDING = 5.0f
        private const val ROW_PADDING = 10.0f
    }

    /**
     * Adds a new row to the body
     * @param row The row to add
     * @throws IllegalArgumentException if the row is invalid
     */
    fun addRow(row: Row) {
        require(row.cells.isNotEmpty()) { "Row must contain at least one cell" }
        rows.add(row)
    }

    /**
     * Converts the table body to SVG format
     * @return SVG representation of the table body
     */
    fun toSvg(): String {
        require(rows.isNotEmpty()) { "Table must contain at least one row" }

        val sb = StringBuilder()
        var currentY =  headerHeight
        rows.forEach { row ->
            row.cells.forEachIndexed { i, cell ->
                cell.toLines(cellWidths[i].width)
            }
        }
        var i=0
        rows.forEachIndexed { j, row ->
            var startX = 1.0
            sb.append("<g>")
            sb.append("<g aria-label=\"Row ${j+1}\">")
            var currentX = INITIAL_OFFSET
            sb.append("""<rect x="1" y="${currentY-2}" width="100%" height="${row.rowHeight()}" fill="url(#backgroundGradient)" filter="url(#shadowFilter)" rx="4" ry="4"/>""")
            row.cells.forEachIndexed { k, cell ->
                var cellColor = "#f9fafb"
                var fontColor = "374151"
                if(!cell.display.isDefault) {
                    cellColor = cell.display.fill.color
                    fontColor = determineTextColor(cellColor)
                }

                val lines = cell.toTextSpans(cell.toLines(cellWidths[k].width+2), (startX+2.0).toFloat(), (currentY+4.0).toFloat(), style = "font-family: 'Segoe UI', Arial, sans-serif; font-weight: normal; font-size: 12px; fill: $fontColor; letter-spacing: 0.2px;",)
                sb.append("<g class=\"rowShade\" aria-label=\"Row ${j+1} column ${k+1}\">")
                sb.append("""<rect x="$startX" y="${currentY-2}" fill="$cellColor" width="${cellWidths[k].width}" height="${row.rowHeight()+2}" stroke="#e5e7eb" stroke-width="1" rx="4" ry="4"/>""")
                sb.append(lines)
                currentX += cellWidths[k].width + CELL_PADDING
                sb.append("</g>")
                startX += cellWidths[k].width
            }

            sb.append("</g></g>")
            currentY += (row.cells.maxOfOrNull { it.height() } ?: 0.0f) + ROW_PADDING
            i++
        }

        return sb.toString()
    }




}
private fun getColorForNumber(number: Int): String {
    return when (number % 2) {
        0 -> "#fcfcfc" // Even number, return red
        else -> "#DDDDDD" // Odd number, return blue
    }
}
/**
 * Represents a row in the table
 * @property cells List of cells in the row
 */
@Serializable
class Row(val cells: MutableList<Cell> = mutableListOf(), val display: DisplayConfig = DisplayConfig()) {
    /**
     * Adds a new cell to the row
     * @param cell The cell to add
     * @throws IllegalArgumentException if the cell is invalid
     */
    fun addCell(cell: Cell) {
        cells.add(cell)
    }

    fun rowHeight(): Float {
        var height = 0f
        cells.forEach { cell ->
            height = maxOf(height, cell.height())
        }
        return height + 10f
    }
}

/**
 * Represents a cell in a table row
 * @property data The content of the cell
 * @property fontName Font family to use for cell content
 * @property fontSize Font size in pixels
 */
@Serializable
class Cell(
    val data: String = "",
    val display: DisplayConfig = DisplayConfig()
) {
    private var lineSize: Float = 0.0f


    /**
     * Calculates the height of the cell based on its content
     * @return Height in pixels
     */
    fun height(): Float = lineSize

    /**
     * Gets the lines of text after wrapping to maxWidth
     * @return List of text lines
     */
    fun toLines(width: Float): MutableList<String>  {

        val urlMap = mutableMapOf<String,String>()
        var s = data
        if(data.contains("[[") && data.contains("]]")) {
            val regex = "(?<=\\[\\[)(.*?)(?=]])".toRegex()
            val matches = regex.findAll(s)
            matches.forEach {
                    item ->
                val urlItem = item.value.split(" ")
                val url = urlItem[0]
                var display = "__"
                if(urlItem.size > 1) {
                     display = urlItem[1]
                }
                s = s.replace("[[${item.value}]]", "[[${display}]]")
                urlMap["[[${display}]]"] = url
            }
        }

        val fontStyle = display.parseFontStyle()
        val itemArray =  itemTextWidth(s, maxWidth = width)
        val lines = linesToUrl(itemArray, urlMap)
        lineSize = (lines.size * fontStyle.size)
        return lines
    }

    private fun linesToUrl(lines: MutableList<String>, urlMap: MutableMap<String, String>): MutableList<String> {
        val newLines = mutableListOf<String>()
        lines.forEach { input ->
            var line = input
            if (input.contains("[[") && input.contains("]]")) {
                val regex = "(?<=\\[\\[)(.*?)(?=]])".toRegex()
                val matches = regex.findAll(input)
                matches.forEach {
                    val output = urlMap["[[${it.value}]]"]
                    val url = """<a xlink:href="$output" target="_blank" style="fill: #0000EE; text-decoration: underline;">${it.value}</a>"""
                    line = line.replace("[[${it.value}]]", url)
                }
            }
            newLines.add(line)
        }
        return newLines
    }
    /**
     * Converts cell content to SVG text spans
     * @param lines The lines of text to render
     * @param startX X coordinate for the text
     * @param startY Y coordinate for the text
     * @param style CSS style for the text (defaults to cell's font settings)
     * @param dy Vertical offset between lines (defaults to fontSize)
     * @return SVG text element with tspan children
     */
    fun toTextSpans(
        lines: MutableList<String>,
        startX: Float,
        startY: Float,
        style: String = "font-family: 'Segoe UI', Arial, sans-serif; font-size: 12px; fill: #374151; letter-spacing: 0.2px;",
        dy: Float = 12f
    ): String {
        require(startX >= 0) { "startX must be non-negative" }
        require(startY >= 0) { "startY must be non-negative" }

        return StringBuilder().apply {
            append("<text x=\"$startX\" y=\"$startY\" style=\"${style}\">")
            var downBy = 8
            lines.forEachIndexed { i, line ->
                if(i>0) {
                    downBy = dy.toInt()
                }
                append("<tspan x=\"$startX\" dy=\"$downBy\">${line}</tspan>")
            }
            append("</text>")
        }.toString()
    }


}

@Serializable
class DisplayConfig(
    val fill: SVGColor = SVGColor("#fcfcfc"),
    val fontColor: SVGColor = SVGColor("#374151"),
    val scale: Float = 1.0f,
    val style: String = "font-family: 'Segoe UI', Arial, sans-serif; font-weight: normal; font-size: 12px; fill: #374151; letter-spacing: 0.2px;"
) {
    val isDefault = fill.color == "#fcfcfc" && fontColor.color == "#374151" && scale == 1.0f

    fun parseFontStyle(): ParsedFont {
        val fontSizeRegex = """font-size:\s*([\d.]+)px""".toRegex()
        val fontFamilyRegex = """font-family:\s*([^,;]+)""".toRegex()

        val fontSize = fontSizeRegex.find(style)?.groupValues?.get(1) ?: "12"
        val fontFamily = fontFamilyRegex.find(style)?.groupValues?.get(1) ?: "Arial"

        return ParsedFont(name = fontFamily, size= fontSize.toFloat())
    }
}

@Serializable
class ParsedFont(val name: String, val size: Float)
@Serializable
class TableConfig(val cellWidths: MutableList<Float> = mutableListOf(), val scale: Float = 1.0f)

@Serializable
class CellWidth(val index: Int, val width: Float)

fun main() {
    val table = Table(display = TableConfig(mutableListOf(0.25f,0.25f,0.25f,0.25f)))

    // Create header row and assign it to thead
    val headerCells = listOf(
        Cell(data = "Header 1", DisplayConfig(fill = SVGColor("#1EE3CF"),fontColor = SVGColor("#000000"))),
        Cell(data = "Header 2", DisplayConfig(fill = SVGColor("#DDDDDD"),fontColor = SVGColor("#000000"))),
        Cell(data = "Header 3", DisplayConfig(fill = SVGColor("#DDDDDD"),fontColor = SVGColor("#000000"))),
        Cell(data = "Header 4", DisplayConfig(fill = SVGColor("#DDDDDD"),fontColor = SVGColor("#000000")))
    )
    table.thead = THead(mutableListOf(Row(headerCells.toMutableList(), DisplayConfig(fill = SVGColor("#DDDDDD"), fontColor = SVGColor("#111111")))))

    val cells = listOf(
        Cell(data = "Cell 1  Create header row and assign it to thead Create header row and assign it to thead",   ),
        Cell(data = "Cell 2",   ),
        Cell(data = "Cell 3",   ),
        Cell(data = "Cell 4",  )
    )
    table.addRow(Row(cells.toMutableList()))

    val cells2 = listOf(
        Cell(data = "Cell 4",  ),
        Cell(data = "Cell 5",  ),
        Cell(data = "Cell certbot automatically checks for expiring certificates and renews them if necessary. This command is typically run periodically, such as through a cron job, to ensure continuous certificate validity.",    display = DisplayConfig(fill = SVGColor("#FF8BA0"))),
        Cell(data = "Cell 7",   )
    )
    table.addRow(Row(cells2.toMutableList()))

    val cells3 = listOf(
        Cell(
            data = "Row 3, Cell 1 with [[https://example.com Example Link]] to test link parsing"
        ),
        Cell(data = "Row 3, Cell 2"),
        Cell(data = "Row 3, Cell 3"),
        Cell(data = "Row 3, Cell 4")
    )
    table.addRow(Row(cells3.toMutableList()))

    val cells4 = listOf(
        Cell(data = "Row 4, Cell 1"),
        Cell(
            data = "Row 4, Cell 2 with [[https://example.com Link1]] and [[https://docops.io DocOps]] multiple links"
        ),
        Cell(data = "Row 4, Cell 3"),
        Cell(
            data = "Row 4, Cell 4 with unique font size"
        )
    )
    table.addRow(Row(cells4.toMutableList()))

    val cells5 = listOf(
        Cell(data = "Row 5, Cell 1"),
        Cell(data = "Row 5, Cell 2"),
        Cell(data = "Row 5, Cell 3"),
        Cell(
            data = "Row 5, Cell 4 with link without label: [[https://github.com/docops-info]]"
        )
    )
    table.addRow(Row(cells5.toMutableList()))

    println(table.toSvg())

    /*val prettyJson = Json { // this returns the JsonBuilder
        prettyPrint = true
    }
    val json = prettyJson.encodeToString(table)
    println(json)*/
}
