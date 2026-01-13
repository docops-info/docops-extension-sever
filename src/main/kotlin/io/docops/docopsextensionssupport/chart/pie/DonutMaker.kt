package io.docops.docopsextensionssupport.chart.pie

import io.docops.docopsextensionssupport.chart.ChartColors
import io.docops.docopsextensionssupport.chart.chartColorAsSVGColor
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import java.io.File
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.sin

class DonutMaker : PieSliceMaker(){

    fun makeDonut(pieSlices: PieSlices) : String {
        val sb= StringBuffer()

        // Create custom SVG start with dark mode support
        val buffer = 120
        val baseHeight = 420
        val h = pieSlices.determineMaxLegendRows() * 12 + baseHeight + buffer
        height = h.toDouble()
        // Increase width to ensure the pie chart is not cut off
        width = 600.0

        // Add dark mode background if enabled
        val backgroundColor = if (pieSlices.display.useDark) "#1f2937" else "#ffffff"

        sb.append("""<?xml version="1.0" encoding="UTF-8"?>
            <svg xmlns="http://www.w3.org/2000/svg" height="${(h * pieSlices.display.scale)/ DISPLAY_RATIO_16_9}" width="${(width * pieSlices.display.scale)/DISPLAY_RATIO_16_9}" viewBox='0 0 ${width * pieSlices.display.scale} ${h * pieSlices.display.scale}' id="id_${pieSlices.display.id}">
            <rect width="100%" height="100%" fill="$backgroundColor" rx="15" ry="15"/>
        """)

        // Create custom defs with enhanced styling
        sb.append(createEnhancedDefs(pieSlices))

        sb.append("<g>")
        sb.append("<g transform=\"translate(4,20)\">")

        // Use dark mode text color if enabled
        val titleColor = if (pieSlices.display.useDark) "#f9fafb" else "#333333"
        sb.append("""<text x="${(width * pieSlices.display.scale)/2}" y="10" text-anchor="middle" style="font-size: 24px; font-family: Arial, Helvetica, sans-serif; fill: $titleColor;">${pieSlices.title.escapeXml()}</text>""")
        sb.append("</g>")
        val donuts = pieSlices.toDonutSlices()
        sb.append(createDonutCommands(donuts, pieSlices))
        sb.append(addLegend(donuts, pieSlices))
        sb.append("</g>")

        sb.append(endSvg())
        return sb.toString()
    }

    private fun createEnhancedDefs(pieSlices: PieSlices) : String {
        val defGrad = StringBuilder()
        val clrs = chartColorAsSVGColor()
        val sz = pieSlices.slices.size
        for(i in 0 until sz) {
            defGrad.append(clrs[i].linearGradient)
        }

        //language=svg
        return """
            <defs>
            $defGrad

            <!-- Drop shadow filter -->
            <filter id="dropShadow" x="-20%" y="-20%" width="140%" height="140%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="3" result="blur"/>
                <feOffset in="blur" dx="2" dy="2" result="offsetBlur"/>
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
            #id_${pieSlices.display.id} .pie {
                transition: all 0.3s ease;
                filter: drop-shadow(3px 3px 2px rgba(0, 0, 0, 0.2));
            }
            #id_${pieSlices.display.id} .pie:hover {
                filter: url(#glow);
                transform: scale(1.05);
            }
            #id_${pieSlices.display.id} .legend-item {
                transition: all 0.3s ease;
                cursor: pointer;
            }
            #id_${pieSlices.display.id} .legend-item:hover {
                font-weight: bold;
                transform: translateX(5px);
            }
            </style>
            </defs>
        """.trimIndent()
    }

    private fun createDonutCommands(slices: List<DonutSlice>, pieSlices: PieSlices): StringBuilder {
        val viewBox = 300.0
        val commands = getSlicesWithCommandsAndOffsets(slices, 120.0, viewBox, 50.0)
        val sb = StringBuilder()

        // Add a background circle for better aesthetics
        val bgColor = if (pieSlices.display.useDark) "#2d3748" else "#f8f9fa"

        sb.append("""<g transform="translate(10, 10)">""")
        sb.append("""<svg viewBox="0 0 $viewBox $viewBox">""")
        sb.append("""<circle cx="${viewBox/2}" cy="${viewBox/2}" r="125" fill="$bgColor" filter="url(#dropShadow)"/>""")

        // Add a center circle for better donut aesthetics
        val centerColor = if (pieSlices.display.useDark) "#1f2937" else "#ffffff"
        sb.append("""<circle cx="${viewBox/2}" cy="${viewBox/2}" r="65" fill="$centerColor"/>""")

        commands.forEachIndexed { index, it ->
            // Determine text color based on background
            val textColor = determineTextColor(ChartColors.Companion.modernColors[index].color)

            // Add animation to the donut slices
            sb.append("""
                <path d="${it.commands}" fill="${it.color}" transform="rotate(${it.offset})" class="pie" style="transform-origin: center; cursor: pointer;">
                    <animate attributeName="opacity" from="0" to="1" dur="${0.5 + index * 0.1}s" fill="freeze"/>
                </path>
            """.trimIndent())

            // Add percentage in the center of each slice
            val midAngle = it.offset * -1 / 3.6 + it.percent / 2
            val midRadius = 95.0
            val labelX = viewBox/2 + midRadius * cos(toRadians(midAngle * 3.6))
            val labelY = viewBox/2 - midRadius * sin(toRadians(midAngle * 3.6))

            sb.append("""
                <text x="$labelX" y="$labelY" text-anchor="middle" fill="$textColor" style="font-size: 12px; font-weight: bold; font-family: Arial, Helvetica, sans-serif;">
                    ${slices[index].valueFmt(it.percent)}%
                </text>
            """.trimIndent())
        }

        // Add total in the center
        val totalValue = slices.sumOf { it.amount }
        val totalTextColor = if (pieSlices.display.useDark) "#f9fafb" else "#333333"

        sb.append("""
            <text x="${viewBox/2}" y="${viewBox/2 - 10}" text-anchor="middle" fill="$totalTextColor" style="font-size: 16px; font-weight: bold; font-family: Arial, Helvetica, sans-serif;">Total</text>
            <text x="${viewBox/2}" y="${viewBox/2 + 15}" text-anchor="middle" fill="$totalTextColor" style="font-size: 20px; font-weight: bold; font-family: Arial, Helvetica, sans-serif;">${slices[0].valueFmt(totalValue)}</text>
        """.trimIndent())

        sb.append("</svg>")
        sb.append("</g>")
        return sb
    }

    private fun addLegend(donuts: MutableList<DonutSlice>, pieSlices: PieSlices): StringBuilder {
        val sb = StringBuilder()

        // Determine colors based on dark mode
        val legendTextColor = if (pieSlices.display.useDark) "#e5e7eb" else "#333333"
        val legendBgColor = if (pieSlices.display.useDark) "#374151" else "#f8f9fa"

        // Create a more modern legend with rounded rectangle background below the chart
        sb.append("""<g transform='translate(${(width / 2) * pieSlices.display.scale},${330 * pieSlices.display.scale})'>""")
        sb.append("""<rect x="-150" y="10" width="300" height="${donuts.size * 20 + 20}" rx="10" ry="10" fill="$legendBgColor" opacity="0.8" filter="url(#dropShadow)"/>""")
        sb.append("""<text x="0" y="30" text-anchor="middle" style="font-size: 14px; font-weight: bold; font-family: Arial, Helvetica, sans-serif; fill: $legendTextColor;">Legend</text>""")

        donuts.forEachIndexed { index, donutSlice ->
            // Calculate position for a two-column layout if there are more than 3 items
            val col = if (donuts.size > 3 && index >= (donuts.size + 1) / 2) 1 else 0
            val row = if (donuts.size > 3 && index >= (donuts.size + 1) / 2) index - (donuts.size + 1) / 2 else index
            val xOffset = col * 150 - 130

            sb.append("""
                <g class="legend-item">
                    <rect x="${xOffset}" y="${40 + row * 20}" width="15" height="15" rx="3" ry="3" fill="url(#svgGradientColor_$index)"/>
                    <text x="${xOffset + 20}" y="${52 + row * 20}" text-anchor="start" style="font-size: 12px; font-family: Arial, Helvetica, sans-serif; fill: $legendTextColor;">
                        ${donutSlice.label} (${donutSlice.valueFmt(donutSlice.amount)}) | ${donutSlice.valueFmt(donutSlice.percent)}%
                    </text>
                </g>
            """.trimIndent())
        }

        sb.append("</g>")
        return sb
    }

    fun getSlicesWithCommandsAndOffsets(
        donutSlices: List<DonutSlice>,
        radius: Double,
        svgSize: Double,
        borderSize: Double
    ): List<DonutSliceWithCommands> {
        var previousPercent = 0.0
        return donutSlices.map { slice ->
            val sliceWithCommands = DonutSliceWithCommands(
                id = slice.id,
                percent = slice.percent,
                amount = slice.amount,
                color = slice.color,
                label = slice.label,
                commands = getSliceCommands(slice, radius, svgSize, borderSize),
                offset = previousPercent * 3.6 * -1
            )
            previousPercent += slice.percent
            sliceWithCommands
        }
    }

    fun getSliceCommands(
        donutSlice: DonutSlice,
        radius: Double,
        svgSize: Double,
        borderSize: Double
    ): String {
        val degrees = percentToDegrees(donutSlice.percent)
        val longPathFlag = if (degrees > 180) 1 else 0
        val innerRadius = radius - borderSize

        val commands = mutableListOf<String>()
        commands.add("M ${svgSize / 2 + radius} ${svgSize / 2}")
        commands.add(
            "A $radius $radius 0 $longPathFlag 0 ${getCoordFromDegrees(degrees, radius, svgSize)}"
        )
        commands.add(
            "L ${getCoordFromDegrees(degrees, innerRadius, svgSize)}"
        )
        commands.add(
            "A $innerRadius $innerRadius 0 $longPathFlag 1 ${svgSize / 2 + innerRadius} ${svgSize / 2}"
        )
        return commands.joinToString(" ")
    }

    fun getCoordFromDegrees(angle: Double, radius: Double, svgSize: Double): String {
        val x = cos(toRadians(angle))
        val y = sin(toRadians(angle))
        val coordX = x * radius + svgSize / 2
        val coordY = y * -radius + svgSize / 2
        return "$coordX $coordY"
    }

    fun percentToDegrees(percent: Double): Double {
        return percent * 3.6
    }
}

data class DonutSliceWithCommands(
    val id: String,
    val percent: Double,
    val amount: Double,
    val color: String,
    val label: String,
    val offset: Double,
    val commands: String
)

// Main function to test the DonutMaker implementation
fun main() {
    // Create sample data for light mode
    val pieSlices = PieSlices(
        title = "Quarterly Revenue Distribution",
        mutableListOf(
            PieSlice(label = "Q1", amount = 45000.0),
            PieSlice(label = "Q2", amount = 62000.0),
            PieSlice(label = "Q3", amount = 38000.0),
            PieSlice(label = "Q4", amount = 78000.0),
            PieSlice(label = "Q5", amount = 51000.0)
        ),
        SliceDisplay(donut = true, useDark = false)
    )

    // Generate the donut chart
    val maker = DonutMaker()
    val svg = maker.makeDonut(pieSlices)

    // Save the chart to a file
    val outfile = File("gen/modern_donut_chart.svg")
    outfile.writeBytes(svg.toByteArray())
    println("Modern donut chart saved to ${outfile.absolutePath}")

    // Create sample data for dark mode
    val darkPieSlices = PieSlices(
        title = "Quarterly Revenue Distribution (Dark Mode)",
        mutableListOf(
            PieSlice(label = "Q1", amount = 45000.0),
            PieSlice(label = "Q2", amount = 62000.0),
            PieSlice(label = "Q3", amount = 38000.0),
            PieSlice(label = "Q4", amount = 78000.0),
            PieSlice(label = "Q5", amount = 51000.0)
        ),
        SliceDisplay(donut = true, useDark = true)
    )

    // Generate the dark mode donut chart
    val darkSvg = maker.makeDonut(darkPieSlices)

    // Save the dark mode chart to a file
    val darkOutfile = File("gen/dark_mode_donut_chart.svg")
    darkOutfile.writeBytes(darkSvg.toByteArray())
    println("Dark mode donut chart saved to ${darkOutfile.absolutePath}")
}
