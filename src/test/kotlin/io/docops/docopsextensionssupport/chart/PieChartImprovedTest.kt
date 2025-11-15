package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.web.DefaultCsvResponse
import org.junit.jupiter.api.Test
import java.io.File

class PieChartImprovedTest {

    @Test
    fun makePieChartTest() {
        // Create sample data for pie chart
        val pieChartData = """
            title=Glass Style Pie Chart
            width=600
            height=600
            legend=true
            percentages=true
            hover=true
            donut=true
            ---
            Segment 1|35
            Segment 2|25
            Segment 3|20
            Segment 4|15
            Segment 5|5
        """.trimIndent()

        // Generate the pie chart
        val svg = PieChartImproved().makePieSvg(pieChartData, DefaultCsvResponse, false)

        // Save the chart to a file
        val outfile = File("gen/test_pie_chart_glass.svg")
        outfile.writeBytes(svg.toByteArray())

        println("Test pie chart with glass effect saved to ${outfile.absolutePath}")
        assert(outfile.exists())
        assert(outfile.length() > 0)
    }

    @Test
    fun makeDarkModePieChartTest() {
        // Create sample data for dark mode pie chart
        val pieChartData = """
            title=Dark Mode Glass Style Pie Chart
            width=600
            height=600
            legend=true
            percentages=true
            hover=true
            donut=true
            darkMode=true
            ---
            Segment 1|35
            Segment 2|25
            Segment 3|20
            Segment 4|15
            Segment 5|5
        """.trimIndent()

        // Generate the pie chart
        val svg = PieChartImproved().makePieSvg(pieChartData, DefaultCsvResponse, false)

        // Save the chart to a file
        val outfile = File("gen/test_pie_chart_glass_dark.svg")
        outfile.writeBytes(svg.toByteArray())

        println("Test dark mode pie chart with glass effect saved to ${outfile.absolutePath}")
        assert(outfile.exists())
        assert(outfile.length() > 0)
    }
}