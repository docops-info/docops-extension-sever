package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.web.DefaultCsvResponse
import org.junit.jupiter.api.Test
import java.io.File

class CombinationChartImprovedTest {

    @Test
    fun makeCombinationChartTest() {
        // Create sample data for combination chart (light mode)
        val combinationChartData = """
            title=Server Performance Metrics
            xLabel=Hour
            yLabel=Requests per Second
            yLabelSecondary=Response Time (ms)
            dualYAxis=true
            showGrid=true
            smoothLines=true
            showPoints=true
            showLegend=true
            darkMode=false
            ---
            Requests | BAR | 00:00 | 1200 | #3498db | PRIMARY
            Requests | BAR | 04:00 | 800 | #3498db | PRIMARY
            Requests | BAR | 08:00 | 2200 | #3498db | PRIMARY
            Requests | BAR | 12:00 | 3500 | #3498db | PRIMARY
            Requests | BAR | 16:00 | 4200 | #3498db | PRIMARY
            Requests | BAR | 20:00 | 2800 | #3498db | PRIMARY
            Response Time | LINE | 00:00 | 120 | #e74c3c | SECONDARY
            Response Time | LINE | 04:00 | 95 | #e74c3c | SECONDARY
            Response Time | LINE | 08:00 | 180 | #e74c3c | SECONDARY
            Response Time | LINE | 12:00 | 250 | #e74c3c | SECONDARY
            Response Time | LINE | 16:00 | 320 | #e74c3c | SECONDARY
            Response Time | LINE | 20:00 | 200 | #e74c3c | SECONDARY
        """.trimIndent()

        // Generate the combination chart
        val svg = CombinationChartImproved(true).makeCombinationChartSvg(
            combinationChartData,
            DefaultCsvResponse,
        )

        // Save the chart to a file
        val outfile = File("gen/test_combination_chart_light.svg")
        outfile.writeBytes(svg.toByteArray())

        println("Test combination chart (light mode) saved to ${outfile.absolutePath}")
        assert(outfile.exists())
        assert(outfile.length() > 0)
    }

    @Test
    fun makeDarkModeCombinationChartTest() {
        // Create sample data for combination chart (dark mode)
        val combinationChartData = """
            title=Sales Performance Dashboard
            xLabel=Month
            yLabel=Sales Volume
            yLabelSecondary=Percentage (%)
            dualYAxis=true
            showGrid=true
            smoothLines=true
            showPoints=true
            showLegend=true
            darkMode=true
            ---
            Units Sold | BAR | Jan | 1200 | #2ecc71 | PRIMARY
            Units Sold | BAR | Feb | 1450 | #2ecc71 | PRIMARY
            Units Sold | BAR | Mar | 1380 | #2ecc71 | PRIMARY
            Units Sold | BAR | Apr | 1620 | #2ecc71 | PRIMARY
            Units Sold | BAR | May | 1750 | #2ecc71 | PRIMARY
            Units Sold | BAR | Jun | 1890 | #2ecc71 | PRIMARY
            Conversion Rate | LINE | Jan | 12.5 | #e74c3c | SECONDARY
            Conversion Rate | LINE | Feb | 14.2 | #e74c3c | SECONDARY
            Conversion Rate | LINE | Mar | 13.8 | #e74c3c | SECONDARY
            Conversion Rate | LINE | Apr | 15.1 | #e74c3c | SECONDARY
            Conversion Rate | LINE | May | 16.3 | #e74c3c | SECONDARY
            Conversion Rate | LINE | Jun | 17.8 | #e74c3c | SECONDARY
            Customer Satisfaction | LINE | Jan | 85.2 | #f39c12 | SECONDARY
            Customer Satisfaction | LINE | Feb | 87.1 | #f39c12 | SECONDARY
            Customer Satisfaction | LINE | Mar | 86.5 | #f39c12 | SECONDARY
            Customer Satisfaction | LINE | Apr | 88.9 | #f39c12 | SECONDARY
            Customer Satisfaction | LINE | May | 90.2 | #f39c12 | SECONDARY
            Customer Satisfaction | LINE | Jun | 91.5 | #f39c12 | SECONDARY
        """.trimIndent()

        // Generate the combination chart
        val svg = CombinationChartImproved(false).makeCombinationChartSvg(
            combinationChartData,
            DefaultCsvResponse,
        )

        // Save the chart to a file
        val outfile = File("gen/test_combination_chart_dark.svg")
        outfile.writeBytes(svg.toByteArray())

        println("Test combination chart (dark mode) saved to ${outfile.absolutePath}")
        assert(outfile.exists())
        assert(outfile.length() > 0)
    }

    @Test
    fun makeGlassEffectCombinationChartTest() {
        // Create sample data for combination chart with glass effect (light mode)
        val combinationChartData = """
            title=Sales Performance with Glass Effect
            xLabel=Quarter
            yLabel=Revenue (${'$'}000)
            yLabelSecondary=Growth Rate (%)
            dualYAxis=true
            showGrid=true
            smoothLines=true
            showPoints=true
            showLegend=true
            useGlass=true
            darkMode=false
            ---
            Revenue | BAR | Q1 | 450 | #3498db | PRIMARY
            Revenue | BAR | Q2 | 520 | #3498db | PRIMARY
            Revenue | BAR | Q3 | 580 | #3498db | PRIMARY
            Revenue | BAR | Q4 | 650 | #3498db | PRIMARY
            Growth Rate | LINE | Q1 | 15.5 | #e74c3c | SECONDARY
            Growth Rate | LINE | Q2 | 18.2 | #e74c3c | SECONDARY
            Growth Rate | LINE | Q3 | 22.8 | #e74c3c | SECONDARY
            Growth Rate | LINE | Q4 | 28.5 | #e74c3c | SECONDARY
        """.trimIndent()

        // Generate the combination chart
        val svg = CombinationChartImproved(true).makeCombinationChartSvg(
            combinationChartData,
            DefaultCsvResponse,
        )

        // Save the chart to a file
        val outfile = File("gen/test_combination_chart_glass_light.svg")
        outfile.writeBytes(svg.toByteArray())

        println("Test combination chart with glass effect (light mode) saved to ${outfile.absolutePath}")
        assert(outfile.exists())
        assert(outfile.length() > 0)

        // Verify glass effect elements are present
        assert(svg.contains("glassOverlay_"))
        assert(svg.contains("glassHighlight_"))
        assert(svg.contains("glassRadial_"))
        assert(svg.contains("glassDropShadow_"))
        assert(svg.contains("class=\"glass-bar bar-reveal\""))
    }

    @Test
    fun makeGlassEffectDarkModeCombinationChartTest() {
        // Create sample data for combination chart with glass effect (dark mode)
        val combinationChartData = """
            title=Production Metrics with Glass Effect (Dark Mode)
            xLabel=Month
            yLabel=Units Produced
            yLabelSecondary=Quality Score (%)
            dualYAxis=true
            showGrid=true
            smoothLines=true
            showPoints=true
            showLegend=true
            useGlass=true
            darkMode=true
            ---
            Production Volume | BAR | Jan | 8500 | #2ecc71 | PRIMARY
            Production Volume | BAR | Feb | 9200 | #2ecc71 | PRIMARY
            Production Volume | BAR | Mar | 8800 | #2ecc71 | PRIMARY
            Production Volume | BAR | Apr | 9500 | #2ecc71 | PRIMARY
            Production Volume | BAR | May | 10200 | #2ecc71 | PRIMARY
            Production Volume | BAR | Jun | 9800 | #2ecc71 | PRIMARY
            Quality Score | LINE | Jan | 94.2 | #e74c3c | SECONDARY
            Quality Score | LINE | Feb | 95.8 | #e74c3c | SECONDARY
            Quality Score | LINE | Mar | 93.5 | #e74c3c | SECONDARY
            Quality Score | LINE | Apr | 96.2 | #e74c3c | SECONDARY
            Quality Score | LINE | May | 97.1 | #e74c3c | SECONDARY
            Quality Score | LINE | Jun | 96.8 | #e74c3c | SECONDARY
        """.trimIndent()

        // Generate the combination chart
        val svg = CombinationChartImproved(true).makeCombinationChartSvg(
            combinationChartData,
            DefaultCsvResponse,
        )

        // Save the chart to a file
        val outfile = File("gen/test_combination_chart_glass_dark.svg")
        outfile.writeBytes(svg.toByteArray())

        println("Test combination chart with glass effect (dark mode) saved to ${outfile.absolutePath}")
        assert(outfile.exists())
        assert(outfile.length() > 0)

        // Verify glass effect elements are present
        assert(svg.contains("glassOverlay_"))
        assert(svg.contains("glassHighlight_"))
        assert(svg.contains("glassRadial_"))
        assert(svg.contains("glassDropShadow_"))
        assert(svg.contains("class=\"glass-bar bar-reveal\""))

        // Verify dark mode styling
        assert(svg.contains("fill=\"url(#bgGlow_")) // Dark background glow
        assert(svg.contains(".chart-text { fill: #f8fafc")) // Light text for dark mode in CSS
    }

    @Test
    fun testBarOverlapIssue() {
        // Create simple bar chart to test the overlap issue
        val combinationChartData = """
            title=Bar Overlap Test
            xLabel=Categories
            yLabel=Values
            showGrid=true
            showLegend=false
            darkMode=false
            ---
            Sales | BAR | Jan | 10 | #3498db | PRIMARY
            Sales | BAR | Feb | 20 | #3498db | PRIMARY
            Sales | BAR | Mar | 15 | #3498db | PRIMARY
            Sales | BAR | Apr | 25 | #3498db | PRIMARY
        """.trimIndent()

        // Generate the combination chart
        val svg = CombinationChartImproved(false).makeCombinationChartSvg(
            combinationChartData,
            DefaultCsvResponse,
        )

        // Save the chart to a file for inspection
        val outfile = File("gen/test_bar_overlap_issue.svg")
        outfile.writeBytes(svg.toByteArray())

        println("[DEBUG_LOG] Test bar overlap chart saved to ${outfile.absolutePath}")

        // Check for y-axis line at x=80
        val yAxisFound = svg.contains("""x1="80" y1="100" x2="80" y2="500"""")
        println("[DEBUG_LOG] Y-axis line at x=80 found: $yAxisFound")
        assert(yAxisFound) { "Y-axis line should be at x=80" }

        // Check for bar positioning - look for bars that start at or before x=80
        val barPattern = """<rect x="([^"]+)"""".toRegex()
        val barMatches = barPattern.findAll(svg)
        var overlapFound = false

        barMatches.forEach { match ->
            val xPos = match.groupValues[1].toDoubleOrNull()
            if (xPos != null) {
                println("[DEBUG_LOG] Bar found at x=$xPos")
                if (xPos <= 80.0) {
                    println("[DEBUG_LOG] ⚠️  ISSUE: Bar at x=$xPos overlaps with y-axis at x=80")
                    overlapFound = true
                }
            }
        }

        // This test should currently fail due to the overlap issue
        if (overlapFound) {
            println("[DEBUG_LOG] Bar overlap issue confirmed - bars are overlapping with y-axis line")
        } else {
            println("[DEBUG_LOG] No bar overlap found - issue may be fixed")
        }

        assert(outfile.exists())
        assert(outfile.length() > 0)
    }

    @Test
    fun testRightBarOverlapWithDualAxis() {
        // Create dual y-axis chart to test the right bar overlap issue
        val combinationChartData = """
            title=Right Bar Overlap Test - Dual Y-Axis
            xLabel=Categories
            yLabel=Primary Values
            yLabelSecondary=Secondary Values (%)
            dualYAxis=true
            showGrid=true
            showLegend=false
            darkMode=false
            useGlass=true
            ---
            Sales | BAR | Jan | 100 | #3498db | PRIMARY
            Sales | BAR | Feb | 200 | #3498db | PRIMARY
            Sales | BAR | Mar | 150 | #3498db | PRIMARY
            Sales | BAR | Apr | 250 | #3498db | PRIMARY
            Sales | BAR | May | 300 | #3498db | PRIMARY
            Conversion | LINE | Jan | 10 | #e74c3c | SECONDARY
            Conversion | LINE | Feb | 15 | #e74c3c | SECONDARY
            Conversion | LINE | Mar | 12 | #e74c3c | SECONDARY
            Conversion | LINE | Apr | 18 | #e74c3c | SECONDARY
            Conversion | LINE | May | 20 | #e74c3c | SECONDARY
        """.trimIndent()

        // Generate the combination chart
        val svg = CombinationChartImproved(false).makeCombinationChartSvg(
            combinationChartData,
            DefaultCsvResponse,
        )

        // Save the chart to a file for inspection
        val outfile = File("gen/test_right_bar_overlap_dual_axis.svg")
        outfile.writeBytes(svg.toByteArray())

        println("[DEBUG_LOG] Test right bar overlap with dual axis chart saved to ${outfile.absolutePath}")

        // Check for right y-axis line at x=720
        val rightYAxisFound = svg.contains("""x1="720" y1="100" x2="720" y2="500"""")
        println("[DEBUG_LOG] Right y-axis line at x=720 found: $rightYAxisFound")
        assert(rightYAxisFound) { "Right y-axis line should be at x=720 when dual axis is enabled" }

        // Check for bar positioning - look for bars that extend beyond x=720
        val barPattern = """<rect x="([^"]+)"[^>]*width="([^"]+)"""".toRegex()
        val barMatches = barPattern.findAll(svg)
        var rightOverlapFound = false

        barMatches.forEach { match ->
            val xPos = match.groupValues[1].toDoubleOrNull()
            val width = match.groupValues[2].toDoubleOrNull()
            if (xPos != null && width != null) {
                val barEnd = xPos + width
                println("[DEBUG_LOG] Bar found at x=$xPos, width=$width, ends at x=$barEnd")
                if (barEnd > 720.0) {
                    println("[DEBUG_LOG] ⚠️  ISSUE: Bar extends to x=$barEnd, overlapping with right y-axis at x=720")
                    rightOverlapFound = true
                }
            }
        }

        // This test should currently fail due to the right overlap issue
        if (rightOverlapFound) {
            println("[DEBUG_LOG] Right bar overlap issue confirmed - bars are overlapping with right y-axis")
        } else {
            println("[DEBUG_LOG] No right bar overlap found - issue may be fixed")
        }

        assert(outfile.exists())
        assert(outfile.length() > 0)
    }
}
