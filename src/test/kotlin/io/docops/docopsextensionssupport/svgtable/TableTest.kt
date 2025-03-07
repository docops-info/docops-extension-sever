package io.docops.docopsextensionssupport.svgtable

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TableTest {

    @Test
    fun `test table creation and row addition`() {
        val table = Table()
        val row = Row()
        val cell = Cell(data = "Test", maxWidth = 100)
        
        row.addCell(cell)
        table.addRow(row)
        
        assertEquals(1, table.rows.size)
        assertEquals(1, table.rows[0].cells.size)
        assertEquals("Test", table.rows[0].cells[0].data)
    }

    @Test
    fun `test input validation`() {
        val table = Table()
        val row = Row()
        
        // Test empty row validation
        assertThrows<IllegalArgumentException> {
            table.addRow(row)
        }
        
        // Test cell with invalid maxWidth
        assertThrows<IllegalArgumentException> {
            row.addCell(Cell(data = "Test", maxWidth = 0))
        }
        
        // Test cell with invalid fontSize
        assertThrows<IllegalArgumentException> {
            Cell(data = "Test", maxWidth = 100, fontSize = 0).toLines()
        }
    }

    @Test
    fun `test SVG generation`() {
        val table = Table()
        val row = Row()
        row.addCell(Cell(data = "Test", maxWidth = 100))
        table.addRow(row)
        
        val svg = table.toSvg()
        
        assertTrue(svg.contains("<g>"))
        assertTrue(svg.contains("<text"))
        assertTrue(svg.contains("<tspan"))
        assertTrue(svg.contains("Test"))
        assertTrue(svg.contains("font-family: Arial"))
    }

    @Test
    fun `test cell text wrapping`() {
        val cell = Cell(
            data = "This is a long text that should wrap",
            maxWidth = 50,
            fontSize = 12
        )
        
        val lines = cell.toLines()
        assertTrue(lines.size > 1)
        assertTrue(lines.all { it.length <= 50 })
    }

    @Test
    fun `test XML escaping in cell content`() {
        val cell = Cell(
            data = "Test & <example> \"quoted\" 'text'",
            maxWidth = 100
        )
        
        val svg = cell.toTextSpans(cell.toLines(), 0, 0)
        
        assertTrue(svg.contains("&amp;"))
        assertTrue(svg.contains("&lt;example&gt;"))
        assertTrue(svg.contains("&quot;quoted&quot;"))
        assertTrue(svg.contains("&apos;text&apos;"))
    }

    @Test
    fun `test complex table structure`() {
        val table = Table()
        val row1 = Row()
        val row2 = Row()
        
        row1.addCell(Cell(data = "Header 1", maxWidth = 100))
        row1.addCell(Cell(data = "Header 2", maxWidth = 100))
        
        row2.addCell(Cell(data = "Data 1", maxWidth = 100))
        row2.addCell(Cell(data = "Data 2", maxWidth = 100))
        
        table.addRow(row1)
        table.addRow(row2)
        
        val svg = table.toSvg()
        assertTrue(svg.contains("Header 1"))
        assertTrue(svg.contains("Header 2"))
        assertTrue(svg.contains("Data 1"))
        assertTrue(svg.contains("Data 2"))
        
        // Verify structure
        assertEquals(2, table.rows.size)
        assertEquals(2, table.rows[0].cells.size)
        assertEquals(2, table.rows[1].cells.size)
    }
}