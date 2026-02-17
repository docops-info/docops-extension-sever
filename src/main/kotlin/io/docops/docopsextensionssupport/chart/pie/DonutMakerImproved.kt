package io.docops.docopsextensionssupport.chart.pie


import io.docops.docopsextensionssupport.chart.chartColorAsSVGColor
import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import kotlin.math.*

class DonutMakerImproved {
    private var height = 600.0
    private var width = 600.0
    private var theme: DocOpsTheme = ThemeFactory.getTheme(false)

    fun makeDonut(pieSlices: PieSlices): String {
        theme = if (pieSlices.display.theme.isNotBlank()) {
            ThemeFactory.getThemeByName(pieSlices.display.theme, pieSlices.display.useDark)
        } else {
            ThemeFactory.getTheme(pieSlices.display)
        }
        val sb = StringBuilder()

        val buffer = 120
        val baseHeight = 420
        val h = pieSlices.determineMaxLegendRows() * 12 + baseHeight + buffer
        height = h.toDouble()
        width = 600.0

        sb.append("""<?xml version="1.0" encoding="UTF-8"?>
            <svg xmlns="http://www.w3.org/2000/svg" height="${(h * pieSlices.display.scale) / DISPLAY_RATIO_16_9}" width="${(width * pieSlices.display.scale) / DISPLAY_RATIO_16_9}" viewBox='0 0 ${width * pieSlices.display.scale} ${h * pieSlices.display.scale}' id="id_${pieSlices.display.id}">
            ${theme.fontImport}
            <rect width="100%" height="100%" fill="${theme.canvas}" rx="15" ry="15"/>
        """)

        sb.append(createEnhancedDefs(pieSlices))

        sb.append("<g>")

        val titleColor = theme.primaryText
        sb.append("""
            <g transform="translate(4, 20)">
            <text style="font-size: 24px; font-family: ${theme.fontFamily}; fill: $titleColor; font-weight: 800;">${pieSlices.title.escapeXml()}</text>
            </g>
        """.trimIndent())
        sb.append("</g>")

        val donuts = pieSlices.toDonutSlices()
        sb.append(createDonutCommands(donuts, pieSlices))
        sb.append(addLegend(donuts, pieSlices))
        sb.append("</g>")

        sb.append("</svg>")
        return sb.toString()
    }

    private fun createEnhancedDefs(pieSlices: PieSlices): String {
        val defGrad = StringBuilder()
        val clrs = theme.chartPalette
        for (i in pieSlices.slices.indices) {
            val clr = clrs[i % clrs.size]
            defGrad.append(clr.createSimpleGradient(clr.color,"id_${pieSlices.display.id}_svgGradientColor_$i"))
        }

        return """
            <defs>
            $defGrad
            
            <!-- Enhanced drop shadow for depth -->
            <filter id="dropShadow" x="-30%" y="-30%" width="160%" height="160%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="4" result="blur"/>
                <feOffset in="blur" dx="0" dy="2" result="offsetBlur"/>
                <feComponentTransfer in="offsetBlur" result="shadow">
                    <feFuncA type="linear" slope="0.4"/>
                </feComponentTransfer>
                <feMerge>
                    <feMergeNode in="shadow"/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>
            
            <!-- Softer glow for hover -->
            <filter id="glow" x="-30%" y="-30%" width="160%" height="160%">
                <feGaussianBlur in="SourceGraphic" stdDeviation="6" result="blur"/>
                <feColorMatrix in="blur" type="matrix" values="1 0 0 0 0 0 1 0 0 0 0 0 1 0 0 0 0 0 20 -8" result="glow"/>
                <feMerge>
                    <feMergeNode in="glow"/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>
            
            <!-- Inner shadow for segments -->
            <filter id="innerShadow" x="-50%" y="-50%" width="200%" height="200%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="2"/>
                <feOffset dx="0" dy="1" result="offsetblur"/>
                <feFlood flood-color="#000000" flood-opacity="0.15"/>
                <feComposite in2="offsetblur" operator="in"/>
                <feMerge>
                    <feMergeNode/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>
            
            <style>
            #id_${pieSlices.display.id} .pie { 
                transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
                filter: url(#dropShadow);
            }
            #id_${pieSlices.display.id} .pie:hover { 
                filter: url(#glow) brightness(1.1);
                transform: scale(1.08);
            }
            #id_${pieSlices.display.id} text { 
                font-family: ${theme.fontFamily};
                transition: all 0.3s ease;
            }
            #id_${pieSlices.display.id} .segment-label {
                transition: opacity 0.3s ease, font-weight 0.3s ease;
            }
            #id_${pieSlices.display.id} .pie:hover + .segment-label {
                font-weight: 900;
                opacity: 1;
            }
            </style>
            </defs>
        """.trimIndent()
    }

    private fun createDonutCommands(slices: List<DonutSlice>, pieSlices: PieSlices): StringBuilder {
        val viewBox = 300.0
        val commands = getSlicesWithCommandsAndOffsets(slices, 120.0, viewBox, 50.0)
        val sb = StringBuilder()
        val accentColor = theme.accentColor

        // Calculate center based on the total SVG width (usually 600.0)
        val centerX = width / 2.0

        sb.append("""<g transform="translate($centerX, 240)">""") // Centered horizontally
        sb.append("""<svg x="-150" y="-150" width="$viewBox" height="$viewBox" viewBox="0 0 $viewBox $viewBox">""")
        sb.append("""<circle cx="${viewBox / 2}" cy="${viewBox / 2}" r="125" fill="${theme.canvas}" stroke="$accentColor" stroke-opacity="0.1" filter="url(#dropShadow)"/>""")
        sb.append("""<circle cx="${viewBox / 2}" cy="${viewBox / 2}" r="65" fill="${theme.canvas}" stroke="$accentColor" stroke-opacity="0.2"/>""")

        commands.forEachIndexed { index, it ->
            sb.append("""
                <g class="segment-group">
                    <path d="${it.commands}" fill="${it.color}" transform="rotate(${it.offset})" class="pie" style="transform-origin: center; cursor: pointer;">
                        <animate attributeName="opacity" from="0" to="1" dur="${0.5 + index * 0.1}s" fill="freeze"/>
                        <animate attributeName="stroke-width" from="0" to="0" dur="0.3s" fill="freeze"/>
                    </path>
            """.trimIndent())

            val midAngle = it.offset * -1 / 3.6 + it.percent / 2
            val midRadius = 95.0
            val labelX = viewBox / 2 + midRadius * cos(Math.toRadians(midAngle * 3.6))
            val labelY = viewBox / 2 - midRadius * sin(Math.toRadians(midAngle * 3.6))

            sb.append("""
                    <text x="$labelX" y="$labelY" text-anchor="middle" fill="white" class="segment-label" style="font-size: 11px; font-weight: 700; text-shadow: 0 2px 4px rgba(0,0,0,0.6); pointer-events: none;">${slices[index].valueFmt(it.percent)}%</text>
                </g>
            """.trimIndent())
        }

        val totalValue = slices.sumOf { it.amount }
        sb.append("""
            <text x="${viewBox / 2}" y="${viewBox / 2 - 5}" text-anchor="middle" fill="${theme.primaryText}" style="font-size: 12px; font-weight: 800; text-transform: uppercase;">Total</text>
            <text x="${viewBox / 2}" y="${viewBox / 2 + 15}" text-anchor="middle" fill="${theme.secondaryText}" style="font-size: 18px; font-weight: 800;">${slices[0].valueFmt(totalValue)}</text>
        """)
        sb.append("</svg></g>")
        return sb
    }

    private fun addLegend(donuts: MutableList<DonutSlice>, pieSlices: PieSlices): StringBuilder {
        val sb = StringBuilder()
        val legendTextColor = theme.primaryText
        val subTextColor = theme.secondaryText
        val legendBgColor = theme.canvas

        sb.append("""<g transform='translate(${(width / 2) * pieSlices.display.scale},${420 * pieSlices.display.scale})'>""")
        sb.append("""<rect x="-180" y="0" width="360" height="${donuts.size * 25 + 40}" rx="12" fill="$legendBgColor" opacity="0.1" stroke="${theme.accentColor}" stroke-width="1"/>""")
        sb.append("""<text x="0" y="25" text-anchor="middle" style="font-size: 14px; font-weight: 800; fill: $legendTextColor; text-transform: uppercase;">Legend</text>""")

        donuts.forEachIndexed { index, donutSlice ->
            val col = if (donuts.size > 4 && index >= (donuts.size + 1) / 2) 1 else 0
            val row = if (donuts.size > 4 && index >= (donuts.size + 1) / 2) index - (donuts.size + 1) / 2 else index
            val xOffset = if (donuts.size > 4) (col * 170 - 160) else -160

            sb.append("""
                <g class="legend-item" transform="translate($xOffset, ${45 + row * 25})">
                    <rect width="14" height="14" rx="4" fill="url(#id_${pieSlices.display.id}_svgGradientColor_$index)"/>
                    <text x="22" y="11" style="font-size: 12px; fill: $legendTextColor; font-weight: 500;">${donutSlice.label}</text>
                    <text x="160" y="11" text-anchor="end" style="font-size: 11px; fill: $subTextColor;">${donutSlice.valueFmt(donutSlice.amount)} (${donutSlice.valueFmt(donutSlice.percent)}%)</text>
                </g>
            """.trimIndent())
        }
        sb.append("</g>")
        return sb
    }

    // Helper methods ported for standalone operation
    private fun getSlicesWithCommandsAndOffsets(donutSlices: List<DonutSlice>, radius: Double, svgSize: Double, borderSize: Double): List<DonutSliceWithCommands> {
        var previousPercent = 0.0
        val gapDegrees = 2.0 // Gap between segments in degrees for modern spacing

        return donutSlices.map { slice ->
            val commands = getSliceCommands(slice, radius, svgSize, borderSize)
            val rotation = previousPercent * 3.6 * -1

            val d = DonutSliceWithCommands(
                slice.id,
                slice.percent,
                slice.amount,
                slice.color,
                slice.label,
                commands,
                rotation
            )

            // Add gap to prevent segments from touching
            previousPercent += slice.percent + (gapDegrees / 3.6)
            d
        }
    }

    private fun getSliceCommands(donutSlice: DonutSlice, radius: Double, svgSize: Double, borderSize: Double): String {
        val degrees = donutSlice.percent * 3.6
        val longPathFlag = if (degrees > 180) 1 else 0
        val innerRadius = radius - borderSize

        // Calculate corner radius for rounded ends (adjust this value for more/less rounding)
        val cornerRadius = borderSize * 0.20 // 20% of border width

        // Outer arc end points
        val x = cos(Math.toRadians(degrees)) * radius + svgSize / 2
        val y = sin(Math.toRadians(degrees)) * -radius + svgSize / 2

        // Inner arc end points
        val ix = cos(Math.toRadians(degrees)) * innerRadius + svgSize / 2
        val iy = sin(Math.toRadians(degrees)) * -innerRadius + svgSize / 2

        // Starting points
        val startX = svgSize / 2 + radius
        val startY = svgSize / 2
        val innerStartX = svgSize / 2 + innerRadius
        val innerStartY = svgSize / 2

        // If the segment is very small, use simple path without rounding
        if (degrees < 5) {
            return "M $startX $startY A $radius $radius 0 $longPathFlag 0 $x $y L $ix $iy A $innerRadius $innerRadius 0 $longPathFlag 1 $innerStartX $innerStartY Z"
        }

        // Calculate adjusted points for rounded corners
        // Start cap (right side)
        val startCapCenterX = (startX + innerStartX) / 2
        val startCapCenterY = (startY + innerStartY) / 2

        // End cap (arc end)
        val endCapCenterX = (x + ix) / 2
        val endCapCenterY = (y + iy) / 2

        // Offset for creating smooth rounded edges
        val outerOffsetAngle = Math.toRadians(cornerRadius / radius * 180 / Math.PI)
        val innerOffsetAngle = Math.toRadians(cornerRadius / innerRadius * 180 / Math.PI)

        // Adjusted outer arc start
        val outerArcStartX = cos(outerOffsetAngle) * radius + svgSize / 2
        val outerArcStartY = sin(outerOffsetAngle) * -radius + svgSize / 2

        // Adjusted outer arc end
        val outerArcEndX = cos(Math.toRadians(degrees) - outerOffsetAngle) * radius + svgSize / 2
        val outerArcEndY = sin(Math.toRadians(degrees) - outerOffsetAngle) * -radius + svgSize / 2

        // Adjusted inner arc start
        val innerArcStartX = cos(innerOffsetAngle) * innerRadius + svgSize / 2
        val innerArcStartY = sin(innerOffsetAngle) * -innerRadius + svgSize / 2

        // Adjusted inner arc end
        val innerArcEndX = cos(Math.toRadians(degrees) - innerOffsetAngle) * innerRadius + svgSize / 2
        val innerArcEndY = sin(Math.toRadians(degrees) - innerOffsetAngle) * -innerRadius + svgSize / 2

        // Build path with rounded corners using quadratic curves for smoothness
        return buildString {
            // Move to start position on outer arc (slightly offset)
            append("M $outerArcStartX $outerArcStartY ")

            // Outer arc
            append("A $radius $radius 0 $longPathFlag 0 $outerArcEndX $outerArcEndY ")

            // Rounded corner at end (outer to inner transition)
            append("Q $x $y $innerArcEndX $innerArcEndY ")

            // Inner arc (back to start)
            append("A $innerRadius $innerRadius 0 $longPathFlag 1 $innerArcStartX $innerArcStartY ")

            // Rounded corner at start (inner to outer transition)
            append("Q $startX $startY $outerArcStartX $outerArcStartY ")

            append("Z")
        }
    }
}

data class DonutSliceWithCommands(val id: String, val percent: Double, val amount: Double, val color: String, val label: String, val commands: String, val offset: Double)