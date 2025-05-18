package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.support.gradientFromColor
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import org.apache.catalina.manager.JspHelper.escapeXml
import java.io.File
import kotlin.math.max
import kotlin.math.min

class BarMaker {

    private var fontColor = "#fcfcfc"
    fun makeHorizontalBar(bar: Bar) : String {
        fontColor = determineTextColor(bar.display.baseColor)
        val sb = StringBuilder()
        sb.append(makeHead(bar))
        sb.append(makeDefs(bar, itemGradients(bar)))
        sb.append(addGrid(bar = bar))
        sb.append(addGroupStart(bar))
        var startX: Int = 1
        var startY: Int = bar.calcLeftPadding()
        var incY = 42
        var minY = max(0, startY)
        if(bar.display.type == "C") {
            incY = 44
        }
        bar.ticks()
        bar.series.forEachIndexed { index, barData ->
            sb.append(makeBarItem(index, barData, startX, startY, bar.seriesTotal(), bar))
            startY += incY
            minY = min(minY, startY)
        }
        sb.append(endGroup(bar))
        sb.append(addTitle(bar))
        sb.append(addTicks(bar))
        sb.append(end(bar))
        return sb.toString()
    }


    private fun end(bar: Bar) = "</svg>"
    private fun makeHead(bar: Bar): String {
        val height = 540 * bar.display.scale
        val width = bar.calcWidth() * bar.display.scale
        val backgroundColor = if (bar.display.useDark) "#1f2937" else "#f8f9fa"
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg id="id_${bar.display.id}" width="${width/ DISPLAY_RATIO_16_9}" height="${height/DISPLAY_RATIO_16_9}" viewBox="0 0 ${width} $height" xmlns="http://www.w3.org/2000/svg">
                <rect width="100%" height="100%" fill="$backgroundColor" rx="15" ry="15"/>
        """.trimIndent()
    }
    private fun addTicks( bar: Bar): String {
        val sb = StringBuilder()

        val nice = bar.ticks()
        val minV = nice.getNiceMin()
        val maxV = nice.getNiceMax()
        val tickSpacing = nice.getTickSpacing()
        var i = minV

        val textColor = if (bar.display.useDark) "#e5e7eb" else "#666"

        // Add y-axis label
        sb.append("""
            <text x="15" y="250" text-anchor="middle" transform="rotate(-90, 15, 250)" style="font-family: Arial, sans-serif; fill: $textColor; font-size: 14px; font-weight: bold;">${bar.yLabel}</text>
        """.trimIndent())

        while(i < maxV ) {
            val y = 500 - bar.scaleUp(i)
            sb.append("""
                <line x1="24" x2="30" y1="$y" y2="$y" stroke="$textColor" stroke-width="2"/>
                <text x="20" y="${y+4}" text-anchor="end" style="font-family: Arial, sans-serif; fill: $textColor; font-size: 12px; font-weight: 500;">${bar.valueFmt(i)}</text>
            """.trimIndent())

            i+=tickSpacing
        }

        // Add x-axis label
        val center = bar.centerWidth()
        sb.append("""
            <text x="$center" y="530" text-anchor="middle" style="font-family: Arial, sans-serif; fill: $textColor; font-size: 14px; font-weight: bold;">${bar.xLabel}</text>
        """.trimIndent())

        return sb.toString()
    }
    private fun makeBarItem(index: Int, barData: Series, startX: Int, startY: Int, total: Double, bar: Bar): String {
       // val per = bar.weightedPercentage(barData, 512)
        val per = bar.scaleUp(barData.value)
        var displayGradId =  bar.display.id
        //var fontColor = bar.display.barFontColor
        if(barData.itemDisplay != null) {
            displayGradId = barData.itemDisplay.id
          //  fontColor = barData.itemDisplay.barFontColor
        }
        var labelY = 0
        var shape = ""
        when (bar.display.type) {
            "C" -> {
                labelY = 18
                shape = """<path class="bar" d="M 0,6 a 20,6 0,0,0 40 0 a 20,6 0,0,0 -40 0 l 0,$per a 20,6 0,0,0 40 0 l 0,-$per" fill="url(#linearGradient_${displayGradId})" transform="translate(0,35) rotate(-90)" style="background: conic-gradient(#655 40%, yellowgreen 0);">
                    <animate attributeName="height" from="0" to="$per" dur="1s" fill="freeze"/>
                </path>"""
            }
            "R" -> {
                labelY = 19
                shape = """<rect class="bar" x="0" y="0" height="$per" width="38" rx="6" ry="6" fill="url(#linearGradient_${displayGradId})" transform="translate(0,35) rotate(-90)" filter="drop-shadow(3px 3px 2px rgba(0, 0, 0, .2))">
                    <animate attributeName="height" from="0" to="$per" dur="1s" fill="freeze"/>
                </rect>"""
            }
        }

        return """
            <g transform="translate($startX,$startY)">
                $shape

                <text x="${per-4}" y="$labelY" style="font-family: Arial,Helvetica, sans-serif; fill: ${fontColor}; font-size:11px; font-weight: bold;" text-anchor="end" >${barData.value.toInt()}</text>
                <text x="10" y="19"  style="font-family: Arial,Helvetica, sans-serif; fill: ${fontColor}; font-size:12px; text-anchor: start;" >${escapeXml(barData.label)}</text>
            </g>
        """.trimIndent()
    }

    private fun addGroupStart(bar: Bar) : String {
        var x = 0
        if(bar.calcWidth() > 512)
        {
            x = 56
        }
         return """<g transform="translate($x,0)">
            <g transform="translate(${bar.innerX()},500) rotate(-90) ">
        """.trimMargin()
    }
    private fun endGroup(bar: Bar) : String {
        val center = bar.centerWidth()
        val barY = bar.calcLeftPadding() - 15
        return """
            </g>
        </g>
        <text x="250" y="-10" style="font-family: Arial,Helvetica, sans-serif; font-size:10px; fill:$fontColor;text-anchor: middle;" transform="rotate(90)">${bar.yLabel}</text>
        <text x="$center" y="520" style="font-family: Arial,Helvetica, sans-serif; font-size:10px; fill: $fontColor;text-anchor: middle;">${bar.xLabel}</text>
        """
    }
    private fun addTitle(bar: Bar): String {
        val center = bar.centerWidth()
        val titleBgColor = if (bar.display.useDark) "#374151" else "#f0f0f0"
        val titleTextColor = if (bar.display.useDark) "#f9fafb" else "#333"
        return """
            <g>
                <rect x="${center - 150}" y="10" width="300" height="40" rx="10" ry="10" fill="$titleBgColor" opacity="0.7"/>
                <text x="$center" y="38" style="font-family: Arial,Helvetica, sans-serif; fill: $titleTextColor; text-anchor: middle; font-size: 24px; font-weight: bold; -webkit-filter: drop-shadow(2px 2px 1px rgba(0, 0, 0, .2)); filter: drop-shadow(2px 2px 1px rgba(0, 0, 0, .2));">${bar.title}</text>
            </g>
        """
    }
    private fun addGrid(bar: Bar) : String
    {
        val maxHeight = 540
        val maxWidth = bar.calcWidth()
        val maxData = bar.series.maxOf { it.value } + 100
        val oneUnit = maxHeight / maxData
        val xGap = maxWidth / (bar.series.size + 1)
        val yGap = maxHeight / 5 // Reduced number of horizontal grid lines for cleaner look
        var num = xGap
        var num2 = yGap
        val elements = StringBuilder()

        // Define colors based on dark mode
        val gridLineColor = if (bar.display.useDark) "#374151" else "#eee"
        val axisColor = if (bar.display.useDark) "#9ca3af" else "#666"

        // We don't need this background rect since we added one in makeHead
        // elements.append("""<rect width='100%' height='100%' fill='url(#backGrad_${bar.display.id})' stroke="#aaaaaa" stroke-width="1"/>""")

        // Add horizontal grid lines (reduced number for cleaner look)
        for (i in 1..4) {
            elements.append("""<line x1="30" y1="${i * yGap}" x2="${maxWidth}" y2="${i * yGap}" stroke="$gridLineColor" stroke-width="1" stroke-dasharray="5,5"/>""")
        }

        // Add vertical grid lines for each data point
        bar.series.forEach {
            val per = bar.scaleUp(it.value)
            elements.append("""<line x1="$num" y1="12" x2="$num" y2="500" stroke="$gridLineColor" stroke-width="1" stroke-dasharray="5,5"/>""")
            num += xGap
        }

        // Add main axes with better styling
        elements.append("""
            <line x1="30" x2="${bar.calcWidth()}" y1="500" y2="500" stroke="$axisColor" stroke-width="2"/>
            <line x1="30" x2="30" y1="12" y2="501" stroke="$axisColor" stroke-width="2"/>
        """.trimIndent())

        return elements.toString()
    }
    private fun makeDefs(bar: Bar, gradients: String) : String {
        val backColor = SVGColor(bar.display.baseColor, "backGrad_${bar.display.id}")
        return """<defs>
            ${backColor.linearGradient}
             <linearGradient id="grad1" x2="0%" y2="100%">
                <stop class="stop1" offset="0%" stop-color="#f6f6f5"/>
                <stop class="stop2" offset="50%" stop-color="#f2f1f0"/>
                <stop class="stop3" offset="100%" stop-color="#EEEDEB"/>
            </linearGradient>
                   $gradients

                    <!-- Drop shadow filter for bars -->
                    <filter id="dropShadow" x="-20%" y="-20%" width="140%" height="140%">
                        <feGaussianBlur in="SourceAlpha" stdDeviation="3" result="blur"/>
                        <feOffset in="blur" dx="3" dy="3" result="offsetBlur"/>
                        <feComponentTransfer in="offsetBlur" result="shadow">
                            <feFuncA type="linear" slope="0.3"/>
                        </feComponentTransfer>
                        <feMerge>
                            <feMergeNode in="shadow"/>
                            <feMergeNode in="SourceGraphic"/>
                        </feMerge>
                    </filter>

                    <!-- Glow filter for hover effect -->
                    <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
                        <feGaussianBlur in="SourceGraphic" stdDeviation="5" result="blur"/>
                        <feColorMatrix in="blur" type="matrix" values="
                            1 0 0 0 0
                            0 1 0 0 0
                            0 0 1 0 0
                            0 0 0 18 -7
                        " result="glow"/>
                        <feMerge>
                            <feMergeNode in="glow"/>
                            <feMergeNode in="SourceGraphic"/>
                        </feMerge>
                    </filter>

                    <style>
                    #id_${bar.display.id} .bar {
                        transition: all 0.3s ease;
                    }
                    #id_${bar.display.id} .bar:hover {
                        filter: url(#glow);
                        transform: scale(1.05);
                        cursor: pointer;
                    }
                    #id_${bar.display.id} .chart-title {
                        font-family: Arial, sans-serif;
                        font-size: 24px;
                        font-weight: bold;
                    }
                    #id_${bar.display.id} .axis-label {
                        font-family: Arial, sans-serif;
                        font-size: 14px;
                        fill: #666;
                    }
                    #id_${bar.display.id} .tick-label {
                        font-family: Arial, sans-serif;
                        font-size: 12px;
                        fill: #666;
                    }
                    </style>
                </defs>"""
    }

    private fun itemGradients(bar: Bar): String {
        val sb = StringBuilder()
        bar.series.forEach {
            if(it.itemDisplay != null) {
                sb.append(makeGradient(it.itemDisplay))
            } else {
                sb.append(makeGradient(bar.display))
            }
        }
        return sb.toString()
    }
    private fun makeGradient(barDisplay: BarDisplay): String {
        val gradient1 = gradientFromColor(barDisplay.baseColor)

        // Create a more vibrant and modern gradient
        val baseColor = barDisplay.baseColor
        val brighterColor = brightenColor(baseColor, 0.2)
        val darkerColor = darkenColor(baseColor, 0.2)

        return """
        <linearGradient id="linearGradient_${barDisplay.id}" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="$brighterColor"/>
            <stop class="stop2" offset="50%" stop-color="$baseColor"/>
            <stop class="stop3" offset="100%" stop-color="$darkerColor"/>
        </linearGradient>
        """.trimIndent()
    }

    // Helper function to brighten a color
    private fun brightenColor(hexColor: String, factor: Double): String {
        return adjustColor(hexColor, factor, true)
    }

    // Helper function to darken a color
    private fun darkenColor(hexColor: String, factor: Double): String {
        return adjustColor(hexColor, factor, false)
    }

    // Helper function to adjust a color's brightness
    private fun adjustColor(hexColor: String, factor: Double, brighten: Boolean): String {
        val hex = hexColor.replace("#", "")
        val r = Integer.parseInt(hex.substring(0, 2), 16)
        val g = Integer.parseInt(hex.substring(2, 4), 16)
        val b = Integer.parseInt(hex.substring(4, 6), 16)

        val adjustment = if (brighten) factor else -factor

        val newR = (r + (255 - r) * adjustment).toInt().coerceIn(0, 255)
        val newG = (g + (255 - g) * adjustment).toInt().coerceIn(0, 255)
        val newB = (b + (255 - b) * adjustment).toInt().coerceIn(0, 255)

        return String.format("#%02x%02x%02x", newR, newG, newB)
    }

    fun makeVerticalBar(bar: Bar): String {
        val vBarMaker = VBarMaker()
        return vBarMaker.makeVerticalBar(bar)
    }

    // This method is used to test the implementation
    fun testAllCharts() {
        // Create sample data
        val bar = Bar(title = "Berry Picking by Month 2024",
            yLabel = "Number of Sales",
            xLabel = "Month",
            series = mutableListOf(Series("Jan", 120.0), Series("Feb", 334.0), Series("Mar", 455.0), Series("Apr", 244.0),
                Series("May", 256.0), Series("Jun", 223.0), Series("Jul", 345.0), Series("Aug", 356.0), Series("Sep", 467.0),
                Series("Oct", 345.0), Series("Nov", 356.0), Series("Dec", 467.0)),
            display = BarDisplay(baseColor = "#4361ee", useDark = false))

        // Test vertical bar chart using the original implementation
        val verticalSvg = makeVerticalBar(bar)
        val verticalOutfile = File("gen/vertical_bars.svg")
        verticalOutfile.writeBytes(verticalSvg.toByteArray())
        println("Vertical bar chart saved to ${verticalOutfile.absolutePath}")

        // Test horizontal bar chart with our improvements
        val horizontalBar = Bar(title = "Monthly Sales Performance",
            yLabel = "Revenue ($)",
            xLabel = "Month",
            series = mutableListOf(Series("January", 120.0), Series("February", 334.0), Series("March", 455.0), 
                Series("April", 244.0), Series("May", 256.0), Series("June", 223.0)),
            display = BarDisplay(baseColor = "#4cc9f0", useDark = false, type = "R"))

        val horizontalSvg = makeHorizontalBar(horizontalBar)
        val horizontalOutfile = File("gen/horizontal_bars.svg")
        horizontalOutfile.writeBytes(horizontalSvg.toByteArray())
        println("Horizontal bar chart saved to ${horizontalOutfile.absolutePath}")

        // Test horizontal bar chart with cylinder style
        val cylinderBar = Bar(title = "Quarterly Performance",
            yLabel = "Revenue ($)",
            xLabel = "Quarter",
            series = mutableListOf(Series("Q1", 320.0), Series("Q2", 480.0), Series("Q3", 290.0), Series("Q4", 410.0)),
            display = BarDisplay(baseColor = "#f72585", useDark = false, type = "C"))

        val cylinderSvg = makeHorizontalBar(cylinderBar)
        val cylinderOutfile = File("gen/cylinder_bars.svg")
        cylinderOutfile.writeBytes(cylinderSvg.toByteArray())
        println("Cylinder bar chart saved to ${cylinderOutfile.absolutePath}")

        // Test the new vertical bar chart implementation that matches bar.svg aesthetics
        val barSvgStyle = Bar(
            title = "Quarterly Performance",
            yLabel = "Revenue ($)",
            xLabel = "Quarter",
            series = mutableListOf(
                Series("Q1", 65.0),
                Series("Q2", 85.0),
                Series("Q3", 55.0),
                Series("Q4", 78.0),
                Series("Q5", 62.0),
                Series("Q6", 90.0)
            ),
            display = BarDisplay(baseColor = "#4361ee", useDark = false)
        )

        // Use the VBarMaker directly to generate the chart
        val barSvgStyleSvg = VBarMaker().makeVerticalBar(barSvgStyle)
        val barSvgStyleOutfile = File("gen/bar_svg_style.svg")
        barSvgStyleOutfile.writeBytes(barSvgStyleSvg.toByteArray())
        println("Bar.svg style chart saved to ${barSvgStyleOutfile.absolutePath}")

        // Test dark mode for vertical bar chart
        val darkModeVerticalBar = Bar(
            title = "Quarterly Performance (Dark Mode)",
            yLabel = "Revenue ($)",
            xLabel = "Quarter",
            series = mutableListOf(
                Series("Q1", 65.0),
                Series("Q2", 85.0),
                Series("Q3", 55.0),
                Series("Q4", 78.0),
                Series("Q5", 62.0),
                Series("Q6", 90.0)
            ),
            display = BarDisplay(baseColor = "#4361ee", useDark = true)
        )

        val darkModeVerticalSvg = VBarMaker().makeVerticalBar(darkModeVerticalBar)
        val darkModeVerticalOutfile = File("gen/dark_mode_vertical_bars.svg")
        darkModeVerticalOutfile.writeBytes(darkModeVerticalSvg.toByteArray())
        println("Dark mode vertical bar chart saved to ${darkModeVerticalOutfile.absolutePath}")

        // Test dark mode for horizontal bar chart
        val darkModeHorizontalBar = Bar(
            title = "Monthly Sales Performance (Dark Mode)",
            yLabel = "Revenue ($)",
            xLabel = "Month",
            series = mutableListOf(Series("January", 120.0), Series("February", 334.0), Series("March", 455.0), 
                Series("April", 244.0), Series("May", 256.0), Series("June", 223.0)),
            display = BarDisplay(baseColor = "#4cc9f0", useDark = true, type = "R")
        )

        val darkModeHorizontalSvg = makeHorizontalBar(darkModeHorizontalBar)
        val darkModeHorizontalOutfile = File("gen/dark_mode_horizontal_bars.svg")
        darkModeHorizontalOutfile.writeBytes(darkModeHorizontalSvg.toByteArray())
        println("Dark mode horizontal bar chart saved to ${darkModeHorizontalOutfile.absolutePath}")
    }
}


fun main() {
    // Test all chart implementations
    BarMaker().testAllCharts()
}
