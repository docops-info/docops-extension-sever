package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.chart.bar.BarGroup
import io.docops.docopsextensionssupport.chart.bar.BarGroupDisplay
import io.docops.docopsextensionssupport.chart.bar.Group
import io.docops.docopsextensionssupport.chart.bar.Series
import io.docops.docopsextensionssupport.chart.bar.VGroupBar
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class VGroupBarAnnualTest {

    @Test
    fun `makeVerticalBar renders expected svg for annual product sales payload`() {
        val payloadModel = BarGroup(
            title = "Annual Product Sales Report",
            yLabel = "Sales (USD)",
            xLabel = "Quarters",
            groups = mutableListOf(
                Group(
                    label = "Product A",
                    series = mutableListOf(
                        Series(label = "Q1", value = 5000.0),
                        Series(label = "Q2", value = 7000.0),
                        Series(label = "Q3", value = 8000.0),
                        Series(label = "Q4", value = 6000.0)
                    )
                ),
                Group(
                    label = "Product B",
                    series = mutableListOf(
                        Series(label = "Q1", value = 6000.0),
                        Series(label = "Q2", value = 8000.0),
                        Series(label = "Q3", value = 7000.0),
                        Series(label = "Q4", value = 9000.0)
                    )
                )
            ),
            display = BarGroupDisplay(
                vBar = true,
                paletteType = "CORPORATE"
            )
        )

        val svg = VGroupBar().makeVerticalBar(payloadModel, isPdf = false)

        // Save the chart to a file
        val outfile = File("gen/test_vgroup_bar_chart.svg")
        outfile.writeBytes(svg.toByteArray())


        assertTrue(svg.startsWith("<?xml"), "SVG should start with XML header")
        assertTrue(svg.contains("<svg"), "Should contain svg root")
        assertTrue(svg.contains("Annual Product Sales Report"), "Should render chart title")
        assertTrue(svg.contains("Quarters"), "Should render x-axis label")
        assertTrue(svg.contains("Sales (USD)"), "Should render y-axis label")

        assertTrue(svg.contains("""aria-label="Product A""""), "Should render Product A group")
        assertTrue(svg.contains("""aria-label="Product B""""), "Should render Product B group")

        assertTrue(svg.contains(">Q1<"), "Should render Q1 label")
        assertTrue(svg.contains(">Q2<"), "Should render Q2 label")
        assertTrue(svg.contains(">Q3<"), "Should render Q3 label")
        assertTrue(svg.contains(">Q4<"), "Should render Q4 label")

        assertTrue(svg.contains("url(#defColor_0)"), "Should use generated palette gradient")
        assertTrue(svg.contains("Legend"), "Should render legend")
    }

    @Test
    fun `makeVerticalBar renders classic theme correctly`() {
        val payloadModel = BarGroup(
            title = "Classic Sales Report",
            groups = mutableListOf(
                Group(label = "A", series = mutableListOf(Series(label = "X", value = 100.0)))
            ),
            display = BarGroupDisplay(theme = "classic")
        )

        val svg = VGroupBar().makeVerticalBar(payloadModel, isPdf = false)
        File("gen/test_vgroup_classic.svg").writeText(svg)

        assertTrue(svg.contains("viewBox=\"0 0 900"), "Classic should use 900 width")
        assertTrue(!svg.contains("bgWashA"), "Classic should not have modern washes")
    }
}
