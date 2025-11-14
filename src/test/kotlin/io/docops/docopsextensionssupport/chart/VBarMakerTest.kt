package io.docops.docopsextensionssupport.chart

import org.junit.jupiter.api.Test
import java.io.File

class VBarMakerTest {

    @Test
    fun makeVBarTest() {

        // Create sample data with long labels to test overlapping
        val bar = Bar(
            title = "Quarterly Performance with Long Labels",
            yLabel = "Revenue in Thousands of Dollars ($)",
            xLabel = "Quarters with Extended Descriptions",
            series = mutableListOf(
                Series("Q1 - First Quarter", 65.0),
                Series("Q2 - Second Quarter", 85.0),
                Series("Q3 - Third Quarter", 55.0),
                Series("Q4 - Fourth Quarter", 78.0),
                Series("Q5 - Fifth Quarter", 62.0),
                Series("Q6 - Sixth Quarter", 90.0)
            ),
            display = BarDisplay(baseColor = "#4361ee", useDark = false)
        )

        // Generate the vertical bar chart
        val svg = VBarMaker().makeVerticalBar(bar, false)

        // Save the chart to a file
        val outfile = File("gen/test_vertical_bar_chart.svg")
        outfile.writeBytes(svg.toByteArray())

        println("Test vertical bar chart saved to ${outfile.absolutePath}")
        assert(outfile.exists())
        assert(outfile.length() > 0)
    }
}