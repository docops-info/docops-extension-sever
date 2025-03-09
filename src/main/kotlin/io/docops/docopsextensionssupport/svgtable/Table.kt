package io.docops.docopsextensionssupport.svgtable

import io.docops.docopsextensionssupport.adr.model.escapeXml
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
        val headSvg = thead?.toSvg(cellWidths) ?: ""
        val numHeaderRows = thead?.rows?.size ?: 0
        val bodySvg = TBody(rows, numHeaderRows, cellWidths).toSvg()
        return makeHead() + makeDefs() + headSvg + bodySvg + endSvg()
    }

    private fun makeHead(): String {
        val sv = StringBuilder()
        val h = tableHeight()
        sv.append("""
<?xml version="1.0" encoding="UTF-8"?>
<svg width="${1024/DISPLAY_RATIO_16_9 }" height="${h / DISPLAY_RATIO_16_9}" viewBox="0 0 1024 $h" xmlns="http://www.w3.org/2000/svg">

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
            <filter id="shadow" x="0" y="0" width="200%" height="200%">
            <feDropShadow dx="3" dy="3" stdDeviation="1" flood-color="#cccccc" flood-opacity="1"/>
        </filter>
        <filter id="Bevel2" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
            <feGaussianBlur in="SourceAlpha" stdDeviation="0.5" result="blur"/>
            <feSpecularLighting in="blur" surfaceScale="5" specularConstant="0.5" specularExponent="10" result="specOut"
                                lighting-color="white">
                <fePointLight x="-5000" y="-10000" z="0000"/>
            </feSpecularLighting>
            <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
            <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0"
                         result="litPaint"/>
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
        var totalHeight = 8f // Initial offset

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
        private const val INITIAL_OFFSET = 8.0f
        private const val CELL_PADDING = 5
        private const val ROW_PADDING = 10.0f
    }
    
    fun toSvg(cellWidths: MutableList<CellWidth>): String {
        require(rows.isNotEmpty()) { "Table must contain at least one row" }
        val sb = StringBuilder()
        var currentY = INITIAL_OFFSET
        rows.forEach { row ->
                row.cells.forEachIndexed { i, cell ->
                    cell.toLines(cellWidths[i].width)
                }
        }
        rows.forEach { row ->
            sb.append("<g>")
            var currentX = INITIAL_OFFSET.toFloat()
            val rowColor = row.display.fill
            sb.append("<g aria-label=\"Header Row\">")
            sb.append("""<rect x="1" y="0" width="99.4%" height="${row.rowHeight()}" fill="${rowColor.color}"/>""")

            var startX = 1.0
            row.cells.forEachIndexed { i, cell ->
                val fontColor = determineTextColor(cell.display.fill.color)
                val lines = cell.toTextSpans(cell.toLines(cellWidths[i].width), (startX+2).toFloat(), currentY, style="font-family: Arial, Helvetica, sans-serif; font-weight: 700; font-size: 12px; fill: ${fontColor};", dy = 12)
                sb.append("<g class=\"rowShade\" aria-label=\"header column ${i+1}\">")
                sb.append("""<rect x="$startX" y="0" fill="url(#${cell.display.fill.id})" width="${cellWidths[i].width}" height="${row.rowHeight()+2}" stroke="#cccccc"/>""")
                sb.append(lines)
                currentX += cellWidths[i].width + CELL_PADDING
                startX += cellWidths[i].width
                sb.append("</g>")
            }

            sb.append("</g></g>")
            currentY += (row.cells.maxOfOrNull { it.height() } ?: 0.0f) + ROW_PADDING
        }

        return sb.toString()
    }
}
/**
 * Internal implementation class for SVG table rendering.
 * @property rows List of rows to render
 */
internal class TBody(private val rows: MutableList<Row>, val numHeaderRows: Int, val cellWidths: MutableList<CellWidth>) {
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
        var currentY = INITIAL_OFFSET + numHeaderRows * 16
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
            sb.append("""<rect x="1" y="${currentY-2}" width="99.4%" height="${row.rowHeight()}" fill="${getColorForNumber(i)}"/>""")
            row.cells.forEachIndexed { k, cell ->
                val lines = cell.toTextSpans(cell.toLines(cellWidths[k].width), (startX+2.0).toFloat(), (currentY+4.0).toFloat())
                var cellColor = getColorForNumber(i)
                if(!cell.display.isDefault) {
                    println("Default cell")
                    cellColor = cell.display.fill.color
                }
                sb.append("<g class=\"rowShade\" aria-label=\"Row ${j+1} column ${k+1}\">")
                sb.append("""<rect x="$startX" y="${currentY-2}" fill="${cellColor}" width="${cellWidths[k].width}" height="${row.rowHeight()+2}" stroke="#cccccc"/>""")
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

    private fun getColorForNumber(number: Int): String {
        return when (number % 2) {
            0 -> "#fcfcfc" // Even number, return red
            else -> "#DDDDDD" // Odd number, return blue
        }
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
    val fontName: String = "Arial",
    val fontSize: Int = 12, val display: DisplayConfig = DisplayConfig()
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
        require(fontSize > 0) { "fontSize must be positive" }
        val lines =  itemTextWidth(data, maxWidth = width, fontSize = fontSize, fontName = fontName)
        lineSize = (lines.size * fontSize).toFloat()
        return lines
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
        style: String = "font-family: $fontName; font-size: ${fontSize}px; fill: #000000;",
        dy: Int = fontSize
    ): String {
        require(startX >= 0) { "startX must be non-negative" }
        require(startY >= 0) { "startY must be non-negative" }
        require(dy > 0) { "dy must be positive" }

        return StringBuilder().apply {
            append("<text x=\"$startX\" y=\"$startY\" style=\"$style\">")
            var downBy = 8
            lines.forEachIndexed { i, line ->
                if(i>0) {
                    downBy = dy
                }
                append("<tspan x=\"$startX\" dy=\"$downBy\">${line.escapeXml()}</tspan>")
            }
            append("</text>")
        }.toString()
    }

}

@Serializable
class DisplayConfig(val fill: SVGColor = SVGColor("#fcfcfc"), val fontColor: SVGColor = SVGColor("#000000"), val scale: Float = 1.0f) {
    val isDefault = fill.color == "#fcfcfc" && fontColor.color == "#000000" && scale == 1.0f
}

@Serializable
class TableConfig(val cellWidths: MutableList<Float> = mutableListOf())

@Serializable
class CellWidth(val index: Int, val width: Float)

fun main() {
    val table = Table(display = TableConfig(mutableListOf(0.25f,0.25f,0.25f,0.25f)))

    // Create header row and assign it to thead
    val headerCells = listOf(
        Cell(data = "Header 1",   fontName = "Arial", fontSize = 12, DisplayConfig(fill = SVGColor("#1EE3CF"),fontColor = SVGColor("#000000"))),
        Cell(data = "Header 2",   fontName = "Arial", fontSize = 12,DisplayConfig(fill = SVGColor("#DDDDDD"),fontColor = SVGColor("#000000"))),
        Cell(data = "Header 3",   fontName = "Arial", fontSize = 12,DisplayConfig(fill = SVGColor("#DDDDDD"),fontColor = SVGColor("#000000"))),
        Cell(data = "Header 4",   fontName = "Arial", fontSize = 12,DisplayConfig(fill = SVGColor("#DDDDDD"),fontColor = SVGColor("#000000")))
    )
    table.thead = THead(mutableListOf(Row(headerCells.toMutableList(), DisplayConfig(fill = SVGColor("#DDDDDD"), fontColor = SVGColor("#111111")))))

    val cells = listOf(
        Cell(data = "Cell 1  Create header row and assign it to thead Create header row and assign it to thead",   fontName = "Arial", fontSize = 12),
        Cell(data = "Cell 2",   fontName = "Arial", fontSize = 12),
        Cell(data = "Cell 3",   fontName = "Arial", fontSize = 12),
        Cell(data = "Cell 4",  fontName = "Arial", fontSize = 12)
    )
    table.addRow(Row(cells.toMutableList()))

    val cells2 = listOf(
        Cell(data = "Cell 4",  fontName = "Arial", fontSize = 12),
        Cell(data = "Cell 5",  fontName = "Arial", fontSize = 12),
        Cell(data = "Cell certbot automatically checks for expiring certificates and renews them if necessary. This command is typically run periodically, such as through a cron job, to ensure continuous certificate validity.",   fontName = "Arial", fontSize = 12, display = DisplayConfig(fill = SVGColor("#FF8BA0"))),
        Cell(data = "Cell 7",   fontName = "Arial", fontSize = 12)
    )
    table.addRow(Row(cells2.toMutableList()))

    val cells3 = listOf(
        Cell(
            data = "Row 3, Cell 1 with long text to test word wrapping and alignment",

            fontName = "Arial",
            fontSize = 12
        ),
        Cell(data = "Row 3, Cell 2",  fontName = "Arial", fontSize = 12),
        Cell(data = "Row 3, Cell 3",  fontName = "Arial", fontSize = 12),
        Cell(data = "Row 3, Cell 4",   fontName = "Arial", fontSize = 12)
    )
    table.addRow(Row(cells3.toMutableList()))

    val cells4 = listOf(
        Cell(data = "Row 4, Cell 1",   fontName = "Arial", fontSize = 12),
        Cell(
            data = "Row 4, Cell 2 with even longer text to test behavior",

            fontName = "Arial",
            fontSize = 12
        ),
        Cell(data = "Row 4, Cell 3",   fontName = "Arial", fontSize = 12),
        Cell(
            data = "Row 4, Cell 4 with unique font size",

            fontName = "Arial",
            fontSize = 14
        )
    )
    table.addRow(Row(cells4.toMutableList()))

    val cells5 = listOf(
        Cell(data = "Row 5, Cell 1",  fontName = "Arial", fontSize = 12),
        Cell(data = "Row 5, Cell 2",   fontName = "Arial", fontSize = 12),
        Cell(data = "Row 5, Cell 3",   fontName = "Arial", fontSize = 12),
        Cell(
            data = "Row 5, Cell 4 with custom text",

            fontName = "Arial",
            fontSize = 12
        )
    )
    table.addRow(Row(cells5.toMutableList()))

    println(table.toSvg())

    val prettyJson = Json { // this returns the JsonBuilder
        prettyPrint = true
    }
    val json = prettyJson.encodeToString(table)
    println(json)
}