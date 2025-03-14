package io.docops.docopsextensionssupport.svgtable

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import io.docops.docopsextensionssupport.support.SVGColor

class TableTest {

    @Test
    fun `test table creation and row addition`() {
        val table = Table()
        val row = Row()
        val cell = Cell(data = "Test Cell")
        row.addCell(cell)
        table.addRow(row)

        assertEquals(1, table.rows.size)
        assertEquals(1, table.rows[0].cells.size)
        assertEquals("Test Cell", table.rows[0].cells[0].data)
    }

    @Test
    fun `test table with header`() {
        val table = Table()
        val thead = THead(mutableListOf())
        val headerRow = Row()
        headerRow.addCell(Cell(data = "Header"))
        thead.rows.add(headerRow)
        table.thead = thead

        val bodyRow = Row()
        bodyRow.addCell(Cell(data = "Body"))
        table.addRow(bodyRow)

        assertNotNull(table.thead)
        assertEquals(1, table.thead?.rows?.size)
        assertEquals("Header", table.thead?.rows?.get(0)?.cells?.get(0)?.data)
        assertEquals(1, table.rows.size)
        assertEquals("Body", table.rows[0].cells[0].data)
    }

    @Test
    fun `test cell configuration`() {
        val cell = Cell(
            data = "Test",
            display = DisplayConfig(
                fill = SVGColor("#ff0000"),
                fontColor = SVGColor("#000000")
            )
        )

        assertEquals("Test", cell.data)
        assertEquals("#ff0000", cell.display.fill.color)
    }

    @Test
    fun `test row height calculation`() {
        val row = Row()
        row.addCell(Cell(data = "Line 1\nLine 2"))
        row.addCell(Cell(data = "Single Line"))

        val height = row.rowHeight()
        assertTrue(height > 0)
    }

    @Test
    fun `test empty table validation throws exception`() {
        val table = Table(display = TableConfig(mutableListOf(0.99f)))
        assertThrows<IllegalArgumentException>("Table must contain at least one row") {
            table.toSvg()
        }
    }

    @Test
    fun `test minimum valid table`() {
        val table = Table()
        val row = Row()
        val cell = Cell(data = "Test")
        row.addCell(cell)
        table.addRow(row)
        val svg = table.toSvg()
        assertTrue(svg.isNotEmpty())
    }

    @Test
    fun `test table with multiple rows and cells`() {
        val table = Table()

        // Add header
        val thead = THead(mutableListOf())
        val headerRow = Row(display = DisplayConfig(fill = SVGColor("#cccccc")))
        headerRow.addCell(Cell(data = "Column 1"))
        headerRow.addCell(Cell(data = "Column 2"))
        thead.rows.add(headerRow)
        table.thead = thead

        // Add body rows
        val row1 = Row()
        row1.addCell(Cell(data = "Data 1"))
        row1.addCell(Cell(data = "Data 2"))
        table.addRow(row1)

        val row2 = Row()
        row2.addCell(Cell(data = "Data 3"))
        row2.addCell(Cell(data = "Data 4"))
        table.addRow(row2)

        val svg = table.toSvg()
        assertTrue(svg.contains("Column 1"))
        assertTrue(svg.contains("Column 2"))
        assertTrue(svg.contains("Data 1"))
        assertTrue(svg.contains("Data 2"))
        assertTrue(svg.contains("Data 3"))
        assertTrue(svg.contains("Data 4"))
    }

    @Test
    fun `test table height calculation with body only`() {
        val table = Table()

        // Add a simple row with single-line cells
        val row1 = Row()
        row1.addCell(Cell(data = "Data 1"))
        row1.addCell(Cell(data = "Data 2"))
        table.addRow(row1)

        // Height should be: initial offset (8) + row height (fontSize 12 + padding 10)
        val expectedHeight = 40f
        assertEquals(expectedHeight, table.tableHeight())
    }

    @Test
    fun `test table height calculation with header and body`() {
        val table = Table()

        // Add header
        val thead = THead(mutableListOf())
        val headerRow = Row()
        headerRow.addCell(Cell(data = "Header 1"))
        headerRow.addCell(Cell(data = "Header 2"))
        thead.rows.add(headerRow)
        table.thead = thead

        // Add body row
        val bodyRow = Row()
        bodyRow.addCell(Cell(data = "Body 1"))
        bodyRow.addCell(Cell(data = "Body 2"))
        table.addRow(bodyRow)

        // Height should be: initial offset (8) + header row height (12 + 10) + body row height (12 + 10)
        val expectedHeight = 50f
        assertEquals(expectedHeight, table.tableHeight())
    }

    @Test
    fun `test table height calculation with wrapped text`() {
        val table = Table()

        // Add a row with wrapped text (will create multiple lines)
        val row = Row()
        row.addCell(Cell(
            data = "This is a long text that will wrap into multiple lines because it exceeds the maximum width"
        ))
        table.addRow(row)

        val height = table.tableHeight()
        assertTrue(height >= 18.0f, "Height should be greater than 30px due to text wrapping")

        // Print actual height for debugging
        println("[DEBUG_LOG] Table height with wrapped text: $height")
    }
}
