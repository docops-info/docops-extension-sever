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
    private var width = 960 // Standard modern width
    private var isModern = true
    
    private var theme: DocOpsTheme = ThemeFactory.getTheme(false)
    
    private var paletteType = ColorPaletteFactory.PaletteType.TABLEAU
    

    fun makeVerticalBar(barGroup: BarGroup, isPdf: Boolean): String {
        theme = if (barGroup.display.theme.isNotBlank()) {
            ThemeFactory.getThemeByName(barGroup.display.theme, barGroup.display.useDark)
        } else {
            ThemeFactory.getThemeByName("modern_editorial", barGroup.display.useDark)
        }
        isModern = !theme.name.contains("Classic") && !theme.name.contains("Pro")
        
        if(!isModern) {
            width = 900
        }

        // Get the palette type for this bar group
        paletteType = getPaletteType(barGroup.display)


        val sb = StringBuilder()
        sb.append(head(barGroup))
        if(isModern && !isPdf) {
            sb.append(makeModernDefs(barGroup, paletteType))
            sb.append(makeModernBackground())
            sb.append(makeModernHeader(barGroup))
            
            val groupSb = StringBuilder()
            var startY = 130
            barGroup.groups.forEach { t ->
                startY = makeModernGroup(startY, t, barGroup, groupSb, paletteType)
            }
            val finalGroupY = startY - 20
            sb.append(makeModernLineSeparator(barGroup, finalGroupY))
            sb.append(makeModernColumnHeader(barGroup))
            sb.append(groupSb)
            sb.append(addModernLegend(startY.toDouble(), barGroup))
        } else {
            sb.append(makeDefs(makeGradient(barGroup.display), barGroup, paletteType))
            sb.append(makeBackground(barGroup))
            sb.append(makeTitle(barGroup))
            
            val groupSb = StringBuilder()
            var startY = 90 // Increased from 80 to align with the new line separator position
            barGroup.groups.forEach { t ->
                startY = makeGroup(startY, t, barGroup, groupSb, isPdf, paletteType)
            }
            val finalGroupY = startY - 24
            sb.append(makeLineSeparator(barGroup, finalGroupY))
            sb.append(makeColumnHeader(barGroup))
            sb.append(groupSb)
            val lastBar = startY
            sb.append(addLegend(lastBar.toDouble(), barGroup))
        }
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

        val uniqueLabels = barGroup.uniqueLabels()
        // Create bars with modern styling
        group.series.forEach { it ->
            val per = barGroup.scaleUp(it.value)
            val valueColor = theme.primaryText
            val labelIdx = uniqueLabels.indexOf(it.label)

            // Use ColorPaletteFactory for colors
            var fill = if(isPdf) {
                ColorPaletteFactory.getColorCyclic(paletteType, labelIdx) ?: "#4361ee"
            } else {
                "url(#defColor_$labelIdx)"
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
        val heightAdjustment = if(isModern) {
            (numOfBars * 30) + (barGroup.groups.size * 40) + 260
        } else {
            (numOfBars * 24) + (numOfBars * 5)  + (barGroup.groups.size*24) + 80
        }
        height = heightAdjustment
        val finalHeight = height * barGroup.display.scale

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg width="${width/ DISPLAY_RATIO_16_9}" height="${finalHeight/DISPLAY_RATIO_16_9}" viewBox="0 0 $width $heightAdjustment" xmlns="http://www.w3.org/2000/svg" id="id_${barGroup.id}" role="img" aria-labelledby="title_${barGroup.id} desc_${barGroup.id}">
            <title id="title_${barGroup.id}">${barGroup.title.escapeXml()}</title>
            <desc id="desc_${barGroup.id}">Grouped horizontal bar chart for ${barGroup.title.escapeXml()}</desc>
            ${theme.fontImport}
        """.trimIndent()
    }

    private fun makeModernDefs(barGroup: BarGroup, paletteType: ColorPaletteFactory.PaletteType): String {
        val defGrad = StringBuilder()
        val uniqueLabels = barGroup.uniqueLabels()
        
        uniqueLabels.forEachIndexed { idx, _ ->
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

        val useDark = theme.name.contains("Dark")
        val washColor1 = ColorPaletteFactory.getColorCyclic(paletteType, 0) ?: "#3d6ea8"
        val washColor2 = ColorPaletteFactory.getColorCyclic(paletteType, 1) ?: "#4879b1"

        return """<defs>
                $defGrad
                <linearGradient id="bgAtmos" x1="0" y1="0" x2="1" y2="1">
                    <stop offset="0%" stop-color="${theme.canvas}"/>
                    <stop offset="100%" stop-color="${if (useDark) "#0f172a" else "#f8fafc"}"/>
                </linearGradient>
                <radialGradient id="bgWashA" cx="20%" cy="20%" r="50%">
                    <stop offset="0%" stop-color="$washColor1" stop-opacity="0.12"/>
                    <stop offset="100%" stop-color="$washColor1" stop-opacity="0"/>
                </radialGradient>
                <radialGradient id="bgWashB" cx="80%" cy="30%" r="40%">
                    <stop offset="0%" stop-color="$washColor2" stop-opacity="0.1"/>
                    <stop offset="100%" stop-color="$washColor2" stop-opacity="0"/>
                </radialGradient>

                <filter id="dropShadow" x="-20%" y="-20%" width="140%" height="140%">
                    <feDropShadow dx="0" dy="2" stdDeviation="3" flood-opacity="0.3"/>
                </filter>

                <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
                    <feGaussianBlur in="SourceGraphic" stdDeviation="4" result="blur"/>
                    <feMerge>
                        <feMergeNode in="blur"/>
                        <feMergeNode in="SourceGraphic"/>
                    </feMerge>
                </filter>

                <style>
                    #id_${barGroup.id} .bar {
                        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                        transform-box: fill-box;
                        transform-origin: left;
                    }
                    #id_${barGroup.id} .bar-group:hover .bar {
                        transform: scaleX(1.02);
                        filter: url(#glow);
                        cursor: pointer;
                    }
                    #id_${barGroup.id} .legend-item {
                        transition: transform 0.3s ease;
                        cursor: pointer;
                    }
                    #id_${barGroup.id} .legend-item:hover {
                        transform: translateX(5px);
                    }
                    #id_${barGroup.id} text {
                        font-family: ${theme.fontFamily};
                    }
                </style>
           </defs>"""
    }

    private fun makeModernBackground(): String {
        return """
            <rect width="100%" height="100%" fill="url(#bgAtmos)" rx="15" ry="15"/>
            <rect width="100%" height="100%" fill="url(#bgWashA)" rx="15" ry="15" style="mix-blend-mode: multiply;"/>
            <rect width="100%" height="100%" fill="url(#bgWashB)" rx="15" ry="15" style="mix-blend-mode: multiply;"/>
        """.trimIndent()
    }

    private fun makeModernHeader(barGroup: BarGroup): String {
        val titleColor = theme.primaryText
        val subColor = theme.secondaryText
        return """
        <g transform="translate(40, 40)">
            <text class="title" x="0" y="0" style="fill: $titleColor; font-size: 28px; font-weight: 800; letter-spacing: -0.02em;">${barGroup.title.escapeXml()}</text>
            <text x="0" y="24" style="fill: $subColor; font-size: 13px; font-weight: 500;">Grouped horizontal visualization • ${barGroup.groups.size} categories</text>
            <line x1="0" y1="40" x2="${width - 80}" y2="40" stroke="${theme.accentColor}" stroke-width="1" stroke-opacity="0.2"/>
        </g>
        """.trimIndent()
    }

    private fun makeModernLineSeparator(barGroup: BarGroup, y2: Int): String {
        return """
            <line x1="220" x2="220" y1="120" y2="$y2" stroke="${theme.accentColor}" stroke-width="1.5" stroke-opacity="0.4" stroke-linecap="round"/>
        """.trimIndent()
    }

    private fun makeModernColumnHeader(barGroup: BarGroup): String {
        val textColor = theme.secondaryText
        return """
        <g transform="translate(0, 110)">
            <text x="210" y="0" text-anchor="end" style="fill: $textColor; font-size: 14px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em;">${barGroup.xLabel?.escapeXml()}</text>
            <text x="230" y="0" text-anchor="start" style="fill: $textColor; font-size: 14px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em;">${barGroup.yLabel?.escapeXml()}</text>
        </g>
        """.trimIndent()
    }

    private fun makeModernGroup(startY: Int, group: Group, barGroup: BarGroup, builder: StringBuilder, paletteType: ColorPaletteFactory.PaletteType): Int {
        val sb = StringBuilder()
        sb.append("""<g aria-label="${group.label.escapeXml()}" transform="translate(223, $startY)">""")
        
        val groupLabelY = (group.series.size * 30) / 2
        sb.append("""
            <text x="-15" y="$groupLabelY" text-anchor="end" dominant-baseline="middle"
                  style="fill: ${theme.primaryText}; font-size: 14px; font-weight: 700;">${group.label.escapeXml()}
            </text>
        """.trimIndent())

        var currentY = 0
        val uniqueLabels = barGroup.uniqueLabels()
        val totalBarsInPreviousGroups = barGroup.groups.takeWhile { it != group }.sumOf { it.series.size }

        group.series.forEachIndexed { idx, it ->
            val per = barGroup.scaleUp(it.value)
            val labelIdx = uniqueLabels.indexOf(it.label)
            val fill = "url(#defColor_$labelIdx)"
            val delay = (totalBarsInPreviousGroups + idx) * 0.05
            
            sb.append("""
            <g class="bar-group" transform="translate(0, $currentY)">
                <rect class="bar" y="0" x="0" height="24" width="$per" rx="4" ry="4" 
                      fill="$fill" filter="url(#dropShadow)">
                    <animate attributeName="width" from="0" to="$per" dur="0.8s" begin="${delay}s" fill="freeze" calcMode="spline" keyTimes="0;1" keySplines="0.4 0 0.2 1"/>
                </rect>
                <text x="${per + 8}" y="16" dominant-baseline="middle"
                      style="font-size: 12px; font-weight: 700; fill: ${theme.primaryText};">
                    ${barGroup.valueFmt(it.value)}
                </text>
                <text x="8" y="16" dominant-baseline="middle"
                      style="font-size: 11px; font-weight: 500; fill: #ffffff; fill-opacity: 0.9;">
                    ${it.label?.escapeXml() ?: ""}
                </text>
            </g>
            """.trimIndent())
            currentY += 30
        }
        sb.append("</g>")
        builder.append(sb.toString())
        return startY + currentY + 20
    }

    private fun addModernLegend(d: Double, barGroup: BarGroup): String {
        val distinct = barGroup.legendLabel().distinct()
        if (distinct.isEmpty()) return ""

        val legendWidth = min(800, width - 80)
        val itemsPerRow = 5
        val rows = (distinct.size + itemsPerRow - 1) / itemsPerRow
        val legendHeight = 40 + (rows * 24)
        val legendX = (width - legendWidth) / 2
        val legendY = d + 20

        val sb = StringBuilder()
        sb.append("""
        <g transform="translate($legendX, $legendY)">
            <rect width="$legendWidth" height="$legendHeight" rx="12" fill="${theme.canvas}" fill-opacity="0.4" stroke="${theme.accentColor}" stroke-width="1" stroke-opacity="0.2"/>
            <text x="20" y="25" style="fill: ${theme.secondaryText}; font-size: 12px; font-weight: 700; text-transform: uppercase;">Legend</text>
        """)

        val itemWidth = (legendWidth - 40) / itemsPerRow
        distinct.forEachIndexed { index, item ->
            val row = index / itemsPerRow
            val col = index % itemsPerRow
            val itemX = 20 + (col * itemWidth)
            val itemY = 40 + (row * 24)
            val fill = "url(#defColor_$index)"

            sb.append("""
            <g class="legend-item" transform="translate($itemX, $itemY)">
                <rect width="12" height="12" rx="3" fill="$fill"/>
                <text x="20" y="10" dominant-baseline="middle" style="fill: ${theme.primaryText}; font-size: 11px; font-weight: 500;">${item.escapeXml()}</text>
            </g>
            """.trimIndent())
        }
        sb.append("</g>")
        return sb.toString()
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
    private fun makeLineSeparator(barGroup: BarGroup, y2: Int) : String{
        val axisColor = theme.accentColor
        return """
            <line x1="200" x2="200" y1="85" y2="$y2" stroke="$axisColor" stroke-width="2" stroke-linecap="round" stroke-opacity="0.3"/>
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
        barGroup.uniqueLabels().forEachIndexed { idx, _ ->
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
