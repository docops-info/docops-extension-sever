package io.docops.docopsextensionssupport.chart.bar

import io.docops.docopsextensionssupport.chart.ColorPaletteFactory
import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.svgsupport.textWidth

class VGroupBar {
    private var height  = 600
    private var fontDisplayColor = "#111111"
    private val width = 900 // Increased from 800 to provide more space for labels
    
    private var theme: DocOpsTheme = ThemeFactory.getTheme(false)
    

    fun makeVerticalBar(barGroup: BarGroup, isPdf: Boolean): String {
        theme = if (barGroup.display.theme.isNotBlank()) {
            ThemeFactory.getThemeByName(barGroup.display.theme, barGroup.display.useDark)
        } else {
            ThemeFactory.getTheme(barGroup.display)
        }

        // Get the palette type for this bar group
        val paletteType = getPaletteType(barGroup.display)


        val sb = StringBuilder()
        sb.append(head(barGroup))
        sb.append(makeDefs(makeGradient(barGroup.display), barGroup, paletteType))
        sb.append(makeBackground(barGroup))
        sb.append(makeTitle(barGroup))
        sb.append(makeLineSeparator(barGroup))
        sb.append(makeColumnHeader(barGroup))
        var startY = 90 // Increased from 80 to align with the new line separator position
        barGroup.groups.forEach { t ->
            startY = makeGroup(startY, t, barGroup, sb, isPdf, paletteType)
        }
        val lastBar = startY
        sb.append(addLegend(lastBar.toDouble(), barGroup))
        sb.append(tail())
        return sb.toString()
    }

    private fun makeGroup(startY: Int, group: Group, barGroup: BarGroup, builder: StringBuilder, isPdf: Boolean, paletteType: ColorPaletteFactory.PaletteType): Int {
        val sb = StringBuilder()
        sb.append("""<g aria-label="${group.label}" transform="translate(203,$startY)">""")
        var currentY = 0
        val bars = (group.series.size * 24) /2 + 6

        // Improve group label styling
        val labelColor = theme.secondaryText
        sb.append("""
            <text x="-10" y="$bars" text-anchor="end"
                  style="fill: $labelColor; font-family: ${theme.fontFamily}; font-size: 14px; font-weight: bold;">${group.label}
            </text>
        """.trimIndent())

        // Create bars with modern styling
        group.series.forEachIndexed { idx, it ->
            val per = barGroup.scaleUp(it.value)
            val valueColor = theme.primaryText

            // Use ColorPaletteFactory for colors
            var fill = if(isPdf) {
                ColorPaletteFactory.getColorCyclic(paletteType, idx) ?: "#4361ee"
            } else {
                "url(#defColor_$idx)"
            }
            sb.append("""
            <g class="bar-group">
                <rect class="bar" y="$currentY" x="0.0" height="24" width="$per" rx="6" ry="6" 
                      fill="$fill" filter="url(#dropShadow)">
                    <animate attributeName="width" from="0" to="$per" dur="1s" fill="freeze"/>
                </rect>
                <text x="${per+5}" y="${currentY + 16}" 
                      style="font-family: ${theme.fontFamily}; font-size: 12px; font-weight: bold; fill: $valueColor;">
                    ${barGroup.valueFmt(it.value)}
                </text>
                <text x="5" y="${currentY + 16}" 
                      style="font-family: ${theme.fontFamily}; font-size: 12px; fill: $labelColor;">
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
            ${theme.fontImport}
        """.trimIndent()
    }
    private fun tail(): String {
        return "</svg>"
    }
    private fun makeBackground(barGroup: BarGroup): String {
        val backgroundColor = theme.canvas
        return """
            <rect width="100%" height="100%" fill="$backgroundColor" rx="15" ry="15"/>
        """.trimIndent()
    }
    private fun makeTitle(barGroup: BarGroup): String {
        val titleBgColor = theme.canvas
        val titleTextColor = theme.primaryText

        // Calculate the width of the title text
        val titleWidth = barGroup.title.textWidth("Arial", 24, 1)

        // Add padding to ensure the text fits comfortably
        val padding = 40
        val rectWidth = titleWidth + padding * 2

        return """
        <g>
            <rect x="${width/2 - rectWidth/2}" y="10" width="$rectWidth" height="40" rx="10" ry="10" fill="$titleBgColor" opacity="0.1" stroke="${theme.accentColor}" stroke-width="1"/>
            <text x="${width/2}" y="38" style="font-family: ${theme.fontFamily}; fill: $titleTextColor; text-anchor: middle; font-size: 24px; font-weight: bold;">${barGroup.title.escapeXml()}</text>
        </g>
        """.trimIndent()
    }
    private fun makeLineSeparator(barGroup: BarGroup) : String{
        val axisColor = theme.accentColor
        return """
            <line x1="200" x2="200" y1="85" y2="$height" stroke="$axisColor" stroke-width="2" stroke-linecap="round" stroke-opacity="0.3"/>
        """.trimIndent()
    }

    private fun makeColumnHeader(barGroup: BarGroup) : String {
        val textColor = theme.secondaryText
        return """
        <g>
            <text x="180" y="75" text-anchor="end" style="font-family: ${theme.fontFamily}; fill: $textColor; font-size: 16px; font-weight: bold;">${barGroup.xLabel?.escapeXml()}</text>
            <text x="220" y="75" text-anchor="start" style="font-family: ${theme.fontFamily}; fill: $textColor; font-size: 16px; font-weight: bold;">${barGroup.yLabel?.escapeXml()}</text>
        </g>
        """.trimIndent()
    }
    private fun addLegend(d: Double, group: BarGroup): String {
        val legendBgColor = theme.canvas
        val legendTextColor = theme.primaryText
        val legendBorderColor = theme.accentColor

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
                  fill="$legendBgColor" fill-opacity="0.1" stroke="$legendBorderColor" stroke-width="1"
                  filter="url(#dropShadow)"/>
            <text x="${width/2}" y="30" text-anchor="middle" 
                  style="font-family: ${theme.fontFamily}; fill: $legendTextColor; font-size: 16px; font-weight: bold;">Legend</text>
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

            val itemColor = "url(#defColor_$index)"
            sb.append("""
                <g class="legend-item">
                    <rect x="$itemX" y="$itemY" width="12" height="12" rx="2" ry="2" fill="$itemColor"/>
                    <text x="${itemX + 18}" y="${itemY + 10}" 
                          style="font-family: ${theme.fontFamily}; fill: $legendTextColor; font-size: 12px;">$item</text>
                </g>
            """.trimIndent())
        }

        sb.append("</g>")
        return sb.toString()
    }

    private fun min(a: Int, b: Int): Int {
        return if (a < b) a else b
    }
    // Helper method to determine palette type (similar to BarGroupMaker)
    private fun getPaletteType(display: BarGroupDisplay): ColorPaletteFactory.PaletteType {
        return when {
            display.paletteType.isNotBlank() -> {
                try {
                    ColorPaletteFactory.PaletteType.valueOf(display.paletteType.uppercase())
                } catch (e: IllegalArgumentException) {
                    getDefaultPaletteType(display.useDark)
                }
            }
            else -> getDefaultPaletteType(display.useDark)
        }
    }

    private fun getDefaultPaletteType(useDark: Boolean): ColorPaletteFactory.PaletteType {
        return if (useDark) {
            ColorPaletteFactory.PaletteType.URBAN_NIGHT
        } else {
            ColorPaletteFactory.PaletteType.TABLEAU
        }
    }

    private fun makeDefs(gradients: String, barGroup: BarGroup, paletteType: ColorPaletteFactory.PaletteType): String {
        val defGrad = StringBuilder()

        // Create gradients using the specified palette type
        barGroup.groups.flatMap { it.series }.forEachIndexed { idx, _ ->
            val baseColor = ColorPaletteFactory.getColorCyclic(paletteType, idx) ?: "#4361ee"
            val svgColor = SVGColor(baseColor)
            val lighterColor = svgColor.brightenColor(baseColor, 0.15)
            val darkerColor = svgColor.darkenColor(baseColor, 0.2)

            defGrad.append("""
                <linearGradient id="defColor_$idx" x1="0%" y1="0%" x2="100%" y2="0%">
                    <stop offset="0%" stop-color="$lighterColor"/>
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




    private fun makeGradient(barDisplay: BarGroupDisplay): String {
        val gradient1 = SVGColor(barDisplay.baseColor, "linearGradient_${barDisplay.id}")
        return gradient1.linearGradient
    }
}
