package io.docops.docopsextensionssupport.chart.pie

import org.junit.jupiter.api.Test
import java.io.File

class PieDashboardMakerTest {

    @Test
    fun `generate pie chart with external labels`() {
        val pies = Pies(
            pies = mutableListOf(
                Pie(25.0f, "Strategy"),
                Pie(35.0f, "Execution"),
                Pie(20.0f, "Analysis"),
                Pie(10.0f, "Planning"),
                Pie(10.0f, "Feedback")
            ),
            pieDisplay = PieDisplay(theme = "modern_editorial", useDark = false)
        )

        val maker = PieDashboardMaker(useDark = false)
        val svg = maker.makePies(pies, "Operations Overview")
        
        val genDir = File("gen")
        if (!genDir.exists()) genDir.mkdirs()
        File(genDir, "pie_dashboard_light.svg").writeText(svg)
    }

    @Test
    fun `generate pie chart with external labels dark`() {
        val pies = Pies(
            pies = mutableListOf(
                Pie(25.0f, "Strategy"),
                Pie(35.0f, "Execution"),
                Pie(20.0f, "Analysis"),
                Pie(10.0f, "Planning"),
                Pie(10.0f, "Feedback")
            ),
            pieDisplay = PieDisplay(theme = "modern_editorial", useDark = true)
        )

        val maker = PieDashboardMaker(useDark = true)
        val svg = maker.makePies(pies, "Operations Overview")
        
        val genDir = File("gen")
        if (!genDir.exists()) genDir.mkdirs()
        File(genDir, "pie_dashboard_dark.svg").writeText(svg)
    }

    @Test
    fun `generate pie chart with explicit legend`() {
        val pies = Pies(
            pies = mutableListOf(
                Pie(25.0f, "Strategy"),
                Pie(35.0f, "Execution"),
                Pie(20.0f, "Analysis"),
                Pie(10.0f, "Planning"),
                Pie(10.0f, "Feedback")
            ),
            pieDisplay = PieDisplay(theme = "modern_editorial", useDark = false, showLegend = true)
        )

        val maker = PieDashboardMaker(useDark = false)
        val svg = maker.makePies(pies, "Operations Overview (With Legend)")

        val genDir = File("gen")
        if (!genDir.exists()) genDir.mkdirs()
        File(genDir, "pie_dashboard_with_legend.svg").writeText(svg)
    }

    @Test
    fun `generate pie chart for pdf`() {
        val pies = Pies(
            pies = mutableListOf(
                Pie(25.0f, "Strategy"),
                Pie(35.0f, "Execution"),
                Pie(20.0f, "Analysis")
            ),
            pieDisplay = PieDisplay(theme = "modern_editorial", useDark = false, showLegend = false)
        )

        val maker = PieDashboardMaker(useDark = false, isPdf = true)
        val svg = maker.makePies(pies, "PDF Operations Overview")

        val genDir = File("gen")
        if (!genDir.exists()) genDir.mkdirs()
        File(genDir, "pie_dashboard_pdf.svg").writeText(svg)
    }
}
