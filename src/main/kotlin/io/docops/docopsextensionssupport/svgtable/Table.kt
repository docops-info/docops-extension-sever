package io.docops.docopsextensionssupport.svgtable

import io.docops.docopsextensionssupport.adr.model.escapeXml
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
import kotlinx.serialization.Serializable

/**
 * Represents a table that can be rendered as SVG.
 * @property rows List of rows in the table
 */
@Serializable
class Table(
    val rows: MutableList<Row> = mutableListOf(),
    var thead: THead? = null
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
        val headSvg = thead?.toSvg() ?: ""
        val numHeaderRows = thead?.rows?.size ?: 0
        val bodySvg = TBody(rows, numHeaderRows).toSvg()
        println(getColorGradients())
        return headSvg + bodySvg
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
        private const val INITIAL_OFFSET = 8
        private const val CELL_PADDING = 5
        private const val ROW_PADDING = 10
    }
    
    fun toSvg(): String {
        require(rows.isNotEmpty()) { "Table must contain at least one row" }
        val sb = StringBuilder()
        var currentY = INITIAL_OFFSET

        rows.forEach { row ->
            sb.append("<g>")
            var currentX = INITIAL_OFFSET
            val rowColor = row.display.fill
            sb.append("<g aria-label=\"Header Row\">")
            sb.append("""<rect x="1" y="0" width="99.4%" height="${row.rowHeight()}" fill="${rowColor.color}"/>""")

            var startX = 1
            row.cells.forEachIndexed { i, cell ->
                sb.append("<g class=\"rowShade\" aria-label=\"header column ${i+1}\">")
                sb.append("""<rect x="$startX" y="0" fill="url(#${cell.display.fill.id})" width="${cell.maxWidth}" height="${row.rowHeight()+2}" stroke="#cccccc"/>""")
                sb.append(cell.toTextSpans(cell.toLines(), startX+2, currentY, style="font-family: Arial, Helvetica, sans-serif; font-weight: 700; font-size: 12px; fill: ${row.display.fontColor.color};", dy = 12))
                currentX += cell.maxWidth + CELL_PADDING
                startX += cell.maxWidth
                sb.append("</g>")
            }

            sb.append("</g></g>")
            currentY += (row.cells.maxOfOrNull { it.height() } ?: 0) + ROW_PADDING
        }

        return sb.toString()
    }
}
/**
 * Internal implementation class for SVG table rendering.
 * @property rows List of rows to render
 */
internal class TBody(private val rows: MutableList<Row>, val numHeaderRows: Int) {
    companion object {
        private const val INITIAL_OFFSET = 8
        private const val CELL_PADDING = 5
        private const val ROW_PADDING = 10
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

        var i=0
        rows.forEachIndexed { j, row ->
            var startX = 1
            sb.append("<g>")
            sb.append("<g aria-label=\"Row ${j+1}\">")
            var currentX = INITIAL_OFFSET
            sb.append("""<rect x="1" y="${currentY-2}" width="99.4%" height="${row.rowHeight()}" fill="${getColorForNumber(i)}"/>""")
            row.cells.forEachIndexed { k, cell ->
                sb.append("<g class=\"rowShade\" aria-label=\"Row ${j+1} column ${k+1}\">")
                sb.append("""<rect x="$startX" y="${currentY-2}" fill="${getColorForNumber(i)}" width="${cell.maxWidth}" height="${row.rowHeight()+2}" stroke="#cccccc"/>""")
                sb.append(cell.toTextSpans(cell.toLines(), startX+2, currentY+4))
                currentX += cell.maxWidth + CELL_PADDING
                sb.append("</g>")
                startX += cell.maxWidth
            }

            sb.append("</g></g>")
            currentY += (row.cells.maxOfOrNull { it.height() } ?: 0) + ROW_PADDING

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
        require(cell.maxWidth > 0) { "Cell must have a positive maxWidth" }
        cells.add(cell)
    }

    fun rowHeight(): Float {
        var height = 0f
        cells.forEach { cell ->
            height = maxOf(height, cell.height().toFloat())
        }
        return height + 10f
    }
}

/**
 * Represents a cell in a table row
 * @property data The content of the cell
 * @property columnNumber The column position of the cell
 * @property maxWidth Maximum width of the cell in pixels
 * @property fontName Font family to use for cell content
 * @property fontSize Font size in pixels
 */
@Serializable
class Cell(
    val data: String = "",
    val columnNumber: Int = 0,
    val maxWidth: Int = 0,
    val fontName: String = "Arial",
    val fontSize: Int = 12, val display: DisplayConfig = DisplayConfig()
) {
    private val lines: MutableList<String> by lazy {
        require(maxWidth > 0) { "maxWidth must be positive" }
        require(fontSize > 0) { "fontSize must be positive" }
        itemTextWidth(data, maxWidth = maxWidth, fontSize = fontSize, fontName = fontName)
    }

    /**
     * Calculates the height of the cell based on its content
     * @return Height in pixels
     */
    fun height(): Int = lines.size * fontSize

    /**
     * Gets the lines of text after wrapping to maxWidth
     * @return List of text lines
     */
    fun toLines(): MutableList<String> = lines

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
        startX: Int,
        startY: Int,
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
class DisplayConfig(val fill: SVGColor = SVGColor("#fcfcfc"), val fontColor: SVGColor = SVGColor("#000000"), val scale: Float = 1.0f)

fun main() {
    val table = Table()

    // Create header row and assign it to thead
    val headerCells = listOf(
        Cell(data = "Header 1", columnNumber = 1, maxWidth = 256, fontName = "Arial", fontSize = 12, DisplayConfig(fill = SVGColor("#1EE3CF"),fontColor = SVGColor("#000000"))),
        Cell(data = "Header 2", columnNumber = 2, maxWidth = 256, fontName = "Arial", fontSize = 12,DisplayConfig(fill = SVGColor("#DDDDDD"),fontColor = SVGColor("#000000"))),
        Cell(data = "Header 3", columnNumber = 3, maxWidth = 256, fontName = "Arial", fontSize = 12,DisplayConfig(fill = SVGColor("#DDDDDD"),fontColor = SVGColor("#000000"))),
        Cell(data = "Header 4", columnNumber = 4, maxWidth = 256, fontName = "Arial", fontSize = 12,DisplayConfig(fill = SVGColor("#DDDDDD"),fontColor = SVGColor("#000000")))
    )
    table.thead = THead(mutableListOf(Row(headerCells.toMutableList(), DisplayConfig(fill = SVGColor("#DDDDDD"), fontColor = SVGColor("#111111")))))

    val cells = listOf(
        Cell(data = "Cell 1  Create header row and assign it to thead Create header row and assign it to thead", columnNumber = 1, maxWidth = 256, fontName = "Arial", fontSize = 12),
        Cell(data = "Cell 2", columnNumber = 2, maxWidth = 256, fontName = "Arial", fontSize = 12),
        Cell(data = "Cell 3", columnNumber = 3, maxWidth = 256, fontName = "Arial", fontSize = 12),
        Cell(data = "Cell 4", columnNumber = 4, maxWidth = 256, fontName = "Arial", fontSize = 12)
    )
    table.addRow(Row(cells.toMutableList()))

    val cells2 = listOf(
        Cell(data = "Cell 4", columnNumber = 1, maxWidth = 256, fontName = "Arial", fontSize = 12),
        Cell(data = "Cell 5", columnNumber = 2, maxWidth = 256, fontName = "Arial", fontSize = 12),
        Cell(data = "Cell certbot automatically checks for expiring certificates and renews them if necessary. This command is typically run periodically, such as through a cron job, to ensure continuous certificate validity.", columnNumber = 3, maxWidth = 256, fontName = "Arial", fontSize = 12),
        Cell(data = "Cell 7", columnNumber = 4, maxWidth = 256, fontName = "Arial", fontSize = 12)
    )
    table.addRow(Row(cells2.toMutableList()))

    println(table.toSvg())
}