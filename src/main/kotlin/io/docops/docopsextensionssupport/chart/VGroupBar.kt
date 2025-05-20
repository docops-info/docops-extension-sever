package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.adr.model.escapeXml
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.support.gradientFromColor
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.textWidth
import java.awt.Font

class VGroupBar {
    private var height  = 600
    private var fontDisplayColor = "#111111"
    private val width = 900 // Increased from 800 to provide more space for labels
    fun makeVerticalBar(barGroup: BarGroup): String {
        if(barGroup.display.useDark) {
            fontDisplayColor = "#fcfcfc"
        }
        val sb = StringBuilder()
        sb.append(head(barGroup))
        sb.append(makeDefs(makeGradient(barGroup.display), barGroup))
        sb.append(makeBackground(barGroup))
        sb.append(makeTitle(barGroup))
        sb.append(makeLineSeparator(barGroup))
        sb.append(makeColumnHeader(barGroup))
        var startY = 90 // Increased from 80 to align with the new line separator position
        barGroup.groups.forEach { t ->
            startY = makeGroup(startY, t, barGroup, sb)
        }
        val lastBar = startY
        sb.append(addLegend(lastBar.toDouble(), barGroup))
        sb.append(tail())
        return sb.toString()
    }

    private fun makeGroup(startY: Int, group: Group, barGroup: BarGroup, builder: StringBuilder): Int {
        val sb = StringBuilder()
        sb.append("""<g aria-label="${group.label}" transform="translate(203,$startY)">""")
        var currentY = 0
        val bars = (group.series.size * 24) /2 + 6

        // Improve group label styling
        val labelColor = if (barGroup.display.useDark) "#e5e7eb" else "#666"
        sb.append("""
            <text x="-10" y="$bars" text-anchor="end"
                  style="fill: $labelColor; font-family: Arial, sans-serif; font-size: 14px; font-weight: bold;">${group.label}
            </text>
        """.trimIndent())

        // Create bars with modern styling
        group.series.forEachIndexed { idx, it ->
            val per = barGroup.scaleUp(it.value)
            val valueColor = if (barGroup.display.useDark) "#f9fafb" else "#333"

            // Add bar with rounded corners, animation, and hover effect
            sb.append("""
            <g class="bar-group">
                <rect class="bar" y="$currentY" x="0.0" height="24" width="$per" rx="6" ry="6" 
                      fill="url(#defColor_$idx)" filter="url(#dropShadow)">
                    <animate attributeName="width" from="0" to="$per" dur="1s" fill="freeze"/>
                </rect>
                <text x="${per+5}" y="${currentY + 16}" 
                      style="font-family: Arial, sans-serif; font-size: 12px; font-weight: bold; fill: $valueColor;">
                    ${barGroup.valueFmt(it.value)}
                </text>
                <text x="5" y="${currentY + 16}" 
                      style="font-family: Arial, sans-serif; font-size: 12px; fill: $labelColor;">
                    ${it.label ?: ""}
                </text>
            </g>
            """.trimIndent())
            currentY += 30 // Increase spacing between bars
        }
        sb.append("</g>")
        builder.append(sb.toString())
        return startY+currentY+24
    }

    private fun head(barGroup: BarGroup): String {
        height = barGroup.calcHeight()
        val numOfBars = barGroup.groups.sumOf{it.series.size}
        val heightAdjustment = (numOfBars * 24) + (numOfBars * 5)  + (barGroup.groups.size*24) + 80
        height = heightAdjustment
        val finalHeight = height * barGroup.display.scale

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg width="${width/ DISPLAY_RATIO_16_9}" height="${finalHeight/DISPLAY_RATIO_16_9}" viewBox="0 0 $width $heightAdjustment" xmlns="http://www.w3.org/2000/svg" id="id_${barGroup.id}">
        """.trimIndent()
    }
    private fun tail(): String {
        return "</svg>"
    }
    private fun makeBackground(barGroup: BarGroup): String {
        val backgroundColor = if(barGroup.display.useDark) "#1f2937" else "#f8f9fa"
        return """
            <rect width="100%" height="100%" fill="$backgroundColor" rx="15" ry="15"/>
        """.trimIndent()
    }
    private fun makeTitle(barGroup: BarGroup): String {
        val titleBgColor = if (barGroup.display.useDark) "#374151" else "#f0f0f0"
        val titleTextColor = if (barGroup.display.useDark) "#f9fafb" else "#333"

        // Calculate the width of the title text
        val titleWidth = barGroup.title.textWidth("Arial", 24, Font.BOLD)

        // Add padding to ensure the text fits comfortably
        val padding = 40
        val rectWidth = titleWidth + padding * 2

        return """
        <g>
            <rect x="${width/2 - rectWidth/2}" y="10" width="$rectWidth" height="40" rx="10" ry="10" fill="$titleBgColor" opacity="0.7"/>
            <text x="${width/2}" y="38" style="font-family: Arial, sans-serif; fill: $titleTextColor; text-anchor: middle; font-size: 24px; font-weight: bold; -webkit-filter: drop-shadow(2px 2px 1px rgba(0, 0, 0, .2)); filter: drop-shadow(2px 2px 1px rgba(0, 0, 0, .2));">${barGroup.title.escapeXml()}</text>
        </g>
        """.trimIndent()
    }
    private fun makeLineSeparator(barGroup: BarGroup) : String{
        val axisColor = if (barGroup.display.useDark) "#9ca3af" else "#666"
        return """
            <line x1="200" x2="200" y1="85" y2="$height" stroke="$axisColor" stroke-width="2" stroke-linecap="round"/>
        """.trimIndent()
    }

    private fun makeColumnHeader(barGroup: BarGroup) : String {
        val textColor = if (barGroup.display.useDark) "#e5e7eb" else "#666"
        return """
        <g>
            <text x="180" y="75" text-anchor="end" style="font-family: Arial, sans-serif; fill: $textColor; font-size: 16px; font-weight: bold;">${barGroup.xLabel?.escapeXml()}</text>
            <text x="220" y="75" text-anchor="start" style="font-family: Arial, sans-serif; fill: $textColor; font-size: 16px; font-weight: bold;">${barGroup.yLabel?.escapeXml()}</text>
        </g>
        """.trimIndent()
    }
    private fun addLegend(d: Double, group: BarGroup): String {
        val legendBgColor = if (group.display.useDark) "#374151" else "#f0f0f0"
        val legendTextColor = if (group.display.useDark) "#e5e7eb" else "#666"
        val legendBorderColor = if (group.display.useDark) "#4b5563" else "#ddd"

        val sb = StringBuilder()
        val distinct = group.legendLabel().distinct()

        // Calculate legend dimensions
        val legendWidth = min(700, width - 40) // Increased from 600 to accommodate more items per row
        val itemsPerRow = 5 // Increased from 4 to 5 to better utilize the wider legend
        val rowCount = (distinct.size + itemsPerRow - 1) / itemsPerRow // Ceiling division
        val rowHeight = 20 // Height per row
        val topPadding = 40 // Space for the "Legend" title and padding
        val bottomPadding = 10 // Padding at the bottom
        val legendHeight = topPadding + (rowCount * rowHeight) + bottomPadding
        val legendX = (width - legendWidth) / 2
        val legendY = d + 20 // Increased from 10 to 20 to provide more space

        sb.append("<g transform='translate(0, $d)'>")

        // Modern legend background with rounded corners and drop shadow
        sb.append("""
            <rect x="$legendX" y="10" width="$legendWidth" height="$legendHeight" rx="15" ry="15" 
                  fill="$legendBgColor" stroke="$legendBorderColor" stroke-width="1" opacity="0.9"
                  filter="url(#dropShadow)"/>
            <text x="${width/2}" y="30" text-anchor="middle" 
                  style="font-family: Arial, sans-serif; fill: $legendTextColor; font-size: 16px; font-weight: bold;">Legend</text>
        """.trimIndent())

        // Create a more organized legend layout
        val y = 50
        val startX = legendX + 30
        val itemWidth = legendWidth / itemsPerRow

        distinct.forEachIndexed { index, item ->
            val row = index / itemsPerRow
            val col = index % itemsPerRow

            val itemX = legendX + 30 + (col * itemWidth)
            val itemY = 50 + (row * 20)

            val color = "url(#defColor_$index)"
            sb.append("""
                <g class="legend-item">
                    <rect x="$itemX" y="$itemY" width="12" height="12" rx="2" ry="2" fill="$color"/>
                    <text x="${itemX + 18}" y="${itemY + 10}" 
                          style="font-family: Arial, sans-serif; fill: $legendTextColor; font-size: 12px;">$item</text>
                </g>
            """.trimIndent())
        }

        sb.append("</g>")
        return sb.toString()
    }

    private fun min(a: Int, b: Int): Int {
        return if (a < b) a else b
    }
    private fun makeDefs(gradients: String, barGroup: BarGroup): String {
        val defGrad = StringBuilder()
        STUNNINGPIE.forEachIndexed { idx, item->
            // Create more vibrant gradients for each color
            val baseColor = item
            val brighterColor = brightenColor(baseColor, 0.2)
            val darkerColor = darkenColor(baseColor, 0.2)

            defGrad.append("""
                <linearGradient id="defColor_$idx" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stop-color="$brighterColor"/>
                    <stop offset="50%" stop-color="$baseColor"/>
                    <stop offset="100%" stop-color="$darkerColor"/>
                </linearGradient>
            """.trimIndent())
        }

        val backColor = SVGColor(barGroup.display.baseColor, "backGrad_${barGroup.id}")
        val darkBackColor = SVGColor("#1f2937", "backGrad_dark_${barGroup.id}")

        return """<defs>
                $defGrad
                ${backColor.linearGradient}
                ${darkBackColor.linearGradient}
                $gradients

                <!-- Drop shadow filter -->
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
                    #id_${barGroup.id} .bar {
                        transition: all 0.3s ease;
                    }
                    #id_${barGroup.id} .bar:hover {
                        filter: url(#glow);
                        transform: scale(1.05);
                        cursor: pointer;
                    }
                    #id_${barGroup.id} .legend-item {
                        transition: all 0.3s ease;
                        cursor: pointer;
                    }
                    #id_${barGroup.id} .legend-item:hover {
                        transform: translateX(5px);
                        font-weight: bold;
                    }
                    #id_${barGroup.id} .shadowed {
                        filter: drop-shadow(3px 3px 2px rgba(0, 0, 0, .3));
                    }
                </style>
           </defs>"""
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
    private fun makeGradient(barDisplay: BarGroupDisplay): String {
        val gradient1 = SVGColor(barDisplay.baseColor, "linearGradient_${barDisplay.id}")
        return gradient1.linearGradient
    }
}
