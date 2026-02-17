package io.docops.docopsextensionssupport.chart.pie

import io.docops.docopsextensionssupport.chart.ChartColors
import io.docops.docopsextensionssupport.chart.chartColorAsSVGColor
import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.svgsupport.formatDecimal
import java.io.File
import kotlin.math.*

class PieSliceMakerImproved {
    private var height = 600.0
    private var width = 600.0
    private var theme: DocOpsTheme = ThemeFactory.getTheme(false)

    fun makePie(pieSlices: PieSlices): String {
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

        val titleColor = theme.primaryText
        sb.append("""<text x="${width / 2}" y="35" text-anchor="middle" style="font-size: 12px; font-family: ${theme.fontFamily}; fill: $titleColor; font-weight: 800; text-transform: uppercase;">${pieSlices.title.escapeXml()}</text>""")

        val total = pieSlices.sum()
        val centerX = width / 2.0
        val centerY = 220.0 // Adjusted for vertical balance
        val radius = 160.0
        var currentAngle = 0.0

        sb.append("<g class='chart-area'>")
        pieSlices.slices.forEachIndexed { index, slice ->
            val angleSize = (slice.amount / total) * 360.0
            val endAngle = currentAngle + angleSize
            val color = slice.displayColor(index, pieSlices.display.id)

            val pathData = createPiePath(centerX, centerY, radius, currentAngle, endAngle)

            // Percentage positioning (halfway out the radius)
            val midAngle = currentAngle + (angleSize / 2.0)
            val labelRadius = radius * 0.65
            val labelX = centerX + labelRadius * cos(Math.toRadians(midAngle))
            val labelY = centerY + labelRadius * sin(Math.toRadians(midAngle))

            sb.append("""
                <g class="pie-segment">
                    <path d="$pathData" fill="$color" filter="url(#dropShadow)" stroke="rgba(255,255,255,0.1)" stroke-width="1">
                        <animate attributeName="opacity" from="0" to="1" dur="${0.5 + index * 0.1}s" fill="freeze"/>
                        <title>${slice.label}: ${slice.amount}</title>
                    </path>
                    <text x="$labelX" y="$labelY" font-family="${theme.fontFamily}" font-size="11" font-weight="800" text-anchor="middle" dominant-baseline="middle" fill="white" style="text-shadow: 0 1px 2px rgba(0,0,0,0.8); pointer-events: none;">
                        ${formatDecimal((slice.amount/total)*100, 1)}%
                    </text>
                </g>
            """.trimIndent())
            currentAngle = endAngle
        }
        sb.append("</g>")

        sb.append(addLegend(pieSlices))
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
            <filter id="dropShadow" x="-20%" y="-20%" width="140%" height="140%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="3" result="blur"/>
                <feOffset dx="2" dy="2" result="offsetBlur"/>
                <feComponentTransfer in="offsetBlur" result="shadow"><feFuncA type="linear" slope="0.3"/></feComponentTransfer>
                <feMerge><feMergeNode/><feMergeNode in="SourceGraphic"/></feMerge>
            </filter>
            <style>
            .pie-segment { transition: all 0.3s ease; transform-origin: center; }
            .pie-segment:hover { transform: scale(1.03); }
            text { font-family: ${theme.fontFamily}; }
            </style>
            </defs>
        """.trimIndent()
    }

    private fun addLegend(pieSlices: PieSlices): String {
        val sb = StringBuilder()
        val total = pieSlices.sum()
        val legendX = width / 2.0
        val legendY = 420.0

        sb.append("""<g transform='translate($legendX, $legendY)'>""")
        sb.append("""<rect x="-180" y="0" width="360" height="${(pieSlices.slices.size + 1) / 2 * 25 + 40}" rx="12" fill="${theme.canvas}" opacity="0.1" stroke="${theme.accentColor}" stroke-width="1"/>""")

        pieSlices.slices.forEachIndexed { index, slice ->
            val col = if (pieSlices.slices.size > 4 && index >= (pieSlices.slices.size + 1) / 2) 1 else 0
            val row = if (pieSlices.slices.size > 4 && index >= (pieSlices.slices.size + 1) / 2) index - (pieSlices.slices.size + 1) / 2 else index
            val xOffset = if (pieSlices.slices.size > 4) (col * 170 - 160) else -160
            val percent = (slice.amount / total) * 100

            sb.append("""
                <g class="legend-item" transform="translate($xOffset, ${35 + row * 25})">
                    <rect width="14" height="14" rx="4" fill="${slice.displayColor(index, pieSlices.display.id)}"/>
                    <text x="22" y="11" style="font-size: 12px; fill: ${theme.primaryText}; font-weight: 500;">${slice.label}</text>
                    <text x="160" y="11" text-anchor="end" style="font-size: 11px; fill: ${theme.secondaryText};">${slice.amount.toInt()} (${formatDecimal(percent, 1)}%)</text>
                </g>
            """.trimIndent())
        }
        sb.append("</g>")
        return sb.toString()
    }

    private fun createPiePath(cx: Double, cy: Double, r: Double, startAngle: Double, endAngle: Double): String {
        val x1 = cx + r * cos(Math.toRadians(startAngle))
        val y1 = cy + r * sin(Math.toRadians(startAngle))
        val x2 = cx + r * cos(Math.toRadians(endAngle))
        val y2 = cy + r * sin(Math.toRadians(endAngle))
        val largeArc = if (endAngle - startAngle > 180) 1 else 0
        // Key logic: Move to center (cx, cy), line to outer edge, arc to second point, then auto-close Z back to center
        return "M $cx $cy L $x1 $y1 A $r $r 0 $largeArc 1 $x2 $y2 Z"
    }
}


