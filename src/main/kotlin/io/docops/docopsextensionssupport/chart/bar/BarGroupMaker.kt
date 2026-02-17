package io.docops.docopsextensionssupport.chart.bar


import io.docops.docopsextensionssupport.chart.ColorPaletteFactory
import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.web.CsvResponse
import kotlin.math.min
import kotlin.math.max
import kotlin.math.ceil

class BarGroupMaker(val useDark: Boolean) {

    private var fontColor = "#fcfcfc"
    private var theme: DocOpsTheme = ThemeFactory.getTheme(useDark)

    // Default palette type - can be made configurable via BarGroupDisplay in the future
    private var paletteType = if (useDark) {
        ColorPaletteFactory.PaletteType.URBAN_NIGHT
    } else {
        ColorPaletteFactory.PaletteType.TABLEAU
    }

    fun makeBar(barGroup: BarGroup, isPdf: Boolean): Pair<String, CsvResponse> {
        barGroup.display.useDark = useDark
        theme = if (barGroup.display.theme.isNotBlank()) {
            ThemeFactory.getThemeByName(barGroup.display.theme, barGroup.display.useDark)
        } else {
            ThemeFactory.getTheme(barGroup.display)
        }
        if ("brutalist".equals(barGroup.display.theme, ignoreCase = true)) {
            val brutalistMaker = CyberBrutalistBarGroupMaker(useDark)
            return brutalistMaker.makeBar(barGroup)
        }
        val svgColor = SVGColor(barGroup.display.baseColor)
        fontColor = determineTextColor(barGroup.display.baseColor)
        // Get the palette type for this bar group
        paletteType = getPaletteType(barGroup.display)

        val sb = StringBuilder()
        sb.append(makeHead(barGroup))
        sb.append(makeDefs(makeGradient(barDisplay = barGroup.display), barGroup=barGroup, paletteType = paletteType))
        sb.append(addGrid(barGroup))
        sb.append(makeTitle(barGroup))
        sb.append(makeXLabel(barGroup))
        sb.append(makeYLabel(barGroup))
        var startX = 110.0
        val elements = StringBuilder()
        barGroup.groups.forEach { group ->
            val added = addGroup(barGroup, group, startX, isPdf, paletteType)
            startX += group.series.size * 45.0 + 2
            elements.append(added)
        }

        sb.append("<g transform='translate(${(barGroup.calcWidth() - startX) / 2},0)'>")
        sb.append(elements.toString())
        sb.append("</g>")
        sb.append(addTicks(barGroup))
        sb.append(addLegend(startX + ((barGroup.calcWidth() - startX)/2), barGroup))
        sb.append(end())
        return  Pair(sb.toString(), barGroup.toCsv())
    }

    private fun addLegend(d: Double, group: BarGroup): String {
        val sb = StringBuilder()
        val distinctLabels = group.legendLabel().distinct()

        if (distinctLabels.isEmpty()) {
            return ""
        }

        // Improved spacing using 8pt grid
        val itemWidth = 120
        val itemHeight = 32 // Changed to 32 (4 × 8)
        val itemsPerRow = 4
        val legendPadding = 16 // Changed to 16 (2 × 8)

        val rows = ceil(distinctLabels.size.toDouble() / itemsPerRow).toInt()
        val legendWidth = min(group.calcWidth() - 40, itemWidth * itemsPerRow + legendPadding * 2)
        val legendHeight = itemHeight * rows + legendPadding * 2

        val legendX = (group.calcWidth() - legendWidth) / 2
        // Legend positioned with proper spacing below category labels
        val legendY = 560 // Moved from 575 to 560 to give more room for X-label below

        val legendBgColor = theme.canvas
        val legendBorderColor = theme.accentColor
        val legendTextColor = theme.primaryText

        // Add shadow filter for depth
        sb.append("""
        <defs>
            <filter id="legendShadow_${group.id}" x="-20%" y="-20%" width="140%" height="140%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="4"/>
                <feOffset dx="0" dy="2" result="offsetBlur"/>
                <feComponentTransfer>
                    <feFuncA type="linear" slope="0.15"/>
                </feComponentTransfer>
                <feMerge>
                    <feMergeNode/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>
        </defs>
    """.trimIndent())

        // Create depth with subtle background elevation (not opacity)
        val bgElevation = if (useDark) "#1e293b" else "#ffffff"
        sb.append("""<rect x="$legendX" y="$legendY" width="$legendWidth" height="$legendHeight" rx="12" ry="12" fill="$bgElevation" stroke="$legendBorderColor" stroke-width="1" stroke-opacity="0.3" filter="url(#legendShadow_${group.id})"/>""")

        // Add legend items
        distinctLabels.forEachIndexed { index, label ->
            val row = index / itemsPerRow
            val col = index % itemsPerRow

            val itemX = legendX + legendPadding + col * itemWidth
            val itemY = legendY + legendPadding + row * itemHeight

            val colorId = "svgGradientColor_$index"

            // Color box with proper corner radius
            sb.append("""<rect x="${itemX}" y="${itemY}" width="16" height="16" rx="4" ry="4" fill="url(#$colorId)"/>""")

            // Label text with better vertical centering
            sb.append("""<text x="${itemX + 24}" y="${itemY + 12}" font-family="${theme.fontFamily}" font-size="12" text-anchor="start" fill="$legendTextColor">$label</text>""")
        }

        return sb.toString()
    }


    private fun addGroup(barGroup: BarGroup, added: Group, startX: Double, isPdf: Boolean, paletteType: ColorPaletteFactory.PaletteType): String {
        val sb = StringBuilder()
        var counter = startX
        added.series.forEachIndexed { index, series ->
            val per = barGroup.scaleUp(series.value)
            var color = "url(#svgGradientColor_$index)"
            val barX = counter
            val barY = 500 - per
            val barWidth = 40.0
            val barHeight = per

            if(isPdf) {
                // Use ColorPaletteFactory for PDF rendering
                val baseColor = ColorPaletteFactory.getColorCyclic(paletteType, index) ?: "#4361ee"
                color = baseColor
                sb.append("""
                    <g class="glass-bar">
                    <rect x="$barX" 
                          y="$barY" 
                          width="$barWidth" 
                          height="$barHeight" 
                          rx="6" 
                          ry="6" 
                          fill="$color"
                          filter="url(#glassDropShadow)"
                          stroke="rgba(255,255,255,0.3)" stroke-width="1">
                        <animate attributeName="height" from="0" to="$barHeight" dur="1s" fill="freeze"/>
                        <animate attributeName="y" from="500" to="$barY" dur="1s" fill="freeze"/>
                    </rect>
                    </g>
                """.trimIndent())
            }
            else {
                sb.append("""
                    <g class="glass-bar">
                        <!-- Base rectangle with outer radius -->
                        <rect x="$barX" 
                              y="$barY" 
                              width="$barWidth" 
                              height="$barHeight" 
                              rx="6" 
                              ry="6" 
                              fill="$color"
                              filter="url(#glassDropShadow)"
                              stroke="rgba(255,255,255,0.3)" stroke-width="1">
                            <animate attributeName="height" from="0" to="$barHeight" dur="1s" fill="freeze"/>
                            <animate attributeName="y" from="500" to="$barY" dur="1s" fill="freeze"/>
                        </rect>

                        <!-- Glass overlay with NO gap = NO radius needed -->
                        <rect x="$barX" 
                              y="$barY" 
                              width="$barWidth" 
                              height="$barHeight" 
                              rx="6" 
                              ry="6"
                              fill="url(#glassOverlay)"
                              pointer-events="none">
                            <animate attributeName="height" from="0" to="$barHeight" dur="1s" fill="freeze"/>
                            <animate attributeName="y" from="500" to="$barY" dur="1s" fill="freeze"/>
                        </rect>

                        <!-- Top highlight with proper inner radius calculation -->
                        <!-- Gap = 3px, so inner radius = 6 - 3 = 3px -->
                        <rect x="${barX + 3}" 
                              y="${barY + 3}" 
                              width="${barWidth - 6}" 
                              height="${min(barHeight / 4, 20.0)}" 
                              rx="3" 
                              ry="3"
                              fill="url(#glassHighlight)"
                              pointer-events="none">
                            <animate attributeName="y" from="497" to="${barY + 3}" dur="1s" fill="freeze"/>
                        </rect>
                    </g>
                """.trimIndent())
            }

            if(series.value > 0) {
                // Value label on top of bar with color based on dark mode
                val valueLabelColor =  theme.primaryText
                sb.append("""<text x="${counter + 20}" y="${500 - per - 8}" style="${barGroup.display.barFontValueStyle}; fill: $valueLabelColor; text-anchor: middle; font-weight: bold;">${barGroup.valueFmt(series.value)}</text>""")
            }
            counter += 40.5
        }
        val textX = startX + (added.series.size / 2 * 45.0)
        // Changed from 510.0 to 505.0 to move labels higher and away from legend
        sb.append(makeSeriesLabel(textX, 505.0, added.label, barGroup))
        return sb.toString()
    }


    private fun makeSeriesLabel(x: Double, y: Double, label: String, barGroup: BarGroup): String {
        val sb = StringBuilder()
        // Use color based on dark mode for series labels
        val seriesLabelColor = theme.secondaryText
        sb.append("""<text x="$x" y="$y" style="${barGroup.display.barSeriesFontStyle}; fill: $seriesLabelColor;" >""")
        val str = label.split(" ")
        str.forEachIndexed { index, s ->
            sb.append("<tspan x='$x' dy='14' style=\"${barGroup.display.barSeriesFontStyle}; fill: $seriesLabelColor;\">$s</tspan>")
        }
        sb.append("</text>")
        return sb.toString()
    }

    private fun addTicks( barGroup: BarGroup): String {
        val sb = StringBuilder()

        val nice =barGroup.ticks()
        val minV = nice.getNiceMin()
        val maxV = nice.getNiceMax()
        val tickSpacing = nice.getTickSpacing()
        var i = minV
        while(i < maxV ) {
            val y = 500 - barGroup.scaleUp(i)
            sb.append("""
     <line x1="100" x2="108" y1="$y" y2="$y" stroke="${theme.accentColor}" stroke-width="3"/>
    <text x="90" y="${y+3}" text-anchor="end" style="font-family: ${theme.fontFamily}; fill: ${theme.primaryText}; font-size:10px; text-anchor:end">${barGroup.valueFmt(i)}</text>
            """.trimIndent())

            i+=tickSpacing
        }
        return sb.toString()
    }
    // Helper function to estimate text width based on font size and text length
    private fun estimateTextWidth(text: String, fontSize: Int): Int {
        // Average character width in pixels (approximate for Arial font)
        val avgCharWidth = fontSize * 0.6

        // Add some padding for bold text
        val boldFactor = 1.2

        // Calculate estimated width
        return (text.length * avgCharWidth * boldFactor).toInt()
    }

    private fun makeTitle(barGroup: BarGroup): String {
        val center = barGroup.calcWidth()/2
        // Use #f0f0f0 for title background and #333 for title text like in bar.svg
        val titleBgColor = theme.canvas
        val titleTextColor = theme.primaryText

        // Estimate the width needed for the title text (font size is 24px)
        val estimatedTextWidth = estimateTextWidth(barGroup.title, 24)

        // Add padding on both sides (40px on each side)
        val rectWidth = estimatedTextWidth + 80

        // Ensure minimum width of 300px
        val finalWidth = max(300, rectWidth)

        // Ensure the title fits within the graph width
        val adjustedWidth = min(finalWidth, barGroup.calcWidth() - 40)

        return """
            <g>
                <rect x="${center - adjustedWidth/2}" y="10" width="$adjustedWidth" height="40" rx="10" ry="10" fill="$titleBgColor" opacity="0.1" stroke="${theme.accentColor}" stroke-width="1"/>
                <text x="$center" y="38" style="font-family: ${theme.fontFamily}; fill: $titleTextColor; text-anchor: middle; font-size: 24px; font-weight: bold;">${barGroup.title}</text>
            </g>
        """.trimIndent()
    }
    private fun makeXLabel(barGroup: BarGroup): String {
        val center = barGroup.calcWidth()/2
        val labelColor = theme.secondaryText

        // Move X-label BELOW the legend (legend ends at ~620, so place at 650)
        return """
        <text x="$center" y="650" 
              style="font-family: ${theme.fontFamily}; fill: $labelColor; 
              text-anchor: middle; font-size: 14px; font-weight: bold;">
            ${barGroup.xLabel}
        </text>
    """.trimIndent()
    }

    private fun makeYLabel(barGroup: BarGroup): String {
        val labelColor = theme.secondaryText

        return """
            <text x="250" y="-40" 
                  style="font-family: ${theme.fontFamily}; fill: $labelColor; 
                  text-anchor: middle; font-size: 14px; font-weight: bold;" 
                  transform="rotate(90)">
                ${barGroup.yLabel}
            </text>
        """.trimIndent()
    }
    private fun end() = "</svg>"
    private fun makeHead(barGroup: BarGroup): String {
        // Increased from 650 to 680 to accommodate all elements
        val svgHeight = 680
        return """
        <?xml version="1.0" encoding="UTF-8"?>
        <svg id="id_${barGroup.id}" width="${(barGroup.calcWidth() * barGroup.display.scale)/ DISPLAY_RATIO_16_9}" height="${(svgHeight * barGroup.display.scale)/DISPLAY_RATIO_16_9}" viewBox="0 0 ${barGroup.calcWidth()} $svgHeight" xmlns="http://www.w3.org/2000/svg" aria-label='Docops: Bar Group Chart'>
        ${theme.fontImport}
    """.trimIndent()
    }

    private fun makeGradient(barDisplay: BarGroupDisplay): String {
        // Create a gradient matching bar.svg format with two stops
        val baseColor = barDisplay.baseColor
        val svgColor = SVGColor(baseColor)
        val darkerColor = svgColor.darkenColor(baseColor, 0.3) // Create a darker color for the gradient end

        return """
        <linearGradient id="linearGradient_${barDisplay.id}" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" stop-color="$baseColor"/>
            <stop offset="100%" stop-color="$darkerColor"/>
        </linearGradient>
        """.trimIndent()
    }



    private fun makeDefs(gradients: String, barGroup: BarGroup, paletteType: ColorPaletteFactory.PaletteType): String {
        val defGrad = StringBuilder()
        val sz = barGroup.maxGroup().series.size

        // Create gradients using the specified palette type
        for(i in 0 until sz) {
            // Get color from palette (cycles if index exceeds palette size)
            val baseColor = ColorPaletteFactory.getColorCyclic(paletteType, i) ?: "#4361ee"

            // Create lighter and darker variants for gradient
            val svgColor = SVGColor(baseColor)
            val lighterColor = svgColor.brightenColor(baseColor, 0.15)
            val darkerColor = svgColor.darkenColor(baseColor, 0.2)

            defGrad.append("""
                <linearGradient id="svgGradientColor_$i" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stop-color="$lighterColor"/>
                    <stop offset="100%" stop-color="$darkerColor"/>
                </linearGradient>
            """.trimIndent())
        }

        // Create background gradient
        val backColor = SVGColor(barGroup.display.baseColor, "backGrad_${barGroup.id}")

        return """<defs>
            $defGrad
            ${backColor.linearGradient}
            <linearGradient id="grad1" x2="0%" y2="100%">
                <stop class="stop1" offset="0%" stop-color="#f6f6f5"/>
                <stop class="stop2" offset="50%" stop-color="#f2f1f0"/>
                <stop class="stop3" offset="100%" stop-color="#EEEDEB"/>
            </linearGradient>
            $gradients

            <!-- Glass effect gradients -->
            <linearGradient id="glassOverlay" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" style="stop-color:rgba(255,255,255,0.4);stop-opacity:1" />
                <stop offset="30%" style="stop-color:rgba(255,255,255,0.2);stop-opacity:1" />
                <stop offset="70%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
                <stop offset="100%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1" />
            </linearGradient>

            <!-- Highlight gradient -->
            <linearGradient id="glassHighlight" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" style="stop-color:rgba(255,255,255,0.7);stop-opacity:1" />
                <stop offset="60%" style="stop-color:rgba(255,255,255,0.3);stop-opacity:1" />
                <stop offset="100%" style="stop-color:rgba(255,255,255,0);stop-opacity:1" />
            </linearGradient>

            <!-- Radial gradient for realistic light reflections -->
            <radialGradient id="glassRadial" cx="30%" cy="30%" r="70%">
                <stop offset="0%" style="stop-color:rgba(255,255,255,0.5);stop-opacity:1" />
                <stop offset="70%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
                <stop offset="100%" style="stop-color:rgba(255,255,255,0);stop-opacity:1" />
            </radialGradient>

            <pattern id="tenthGrid" width="10" height="10" patternUnits="userSpaceOnUse">
                <path d="M 10 0 L 0 0 0 10" fill="none" stroke="silver" stroke-width="0.5"/>
            </pattern>
            <pattern id="grid" width="100" height="100" patternUnits="userSpaceOnUse">
                <rect width="100" height="100" fill="url(#tenthGrid)"/>
                <path d="M 100 0 L 0 0 0 100" fill="none" stroke="gray" stroke-width="1"/>
            </pattern>
            <filter id="Bevel" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="3" result="blur"/>
                <feSpecularLighting in="blur" surfaceScale="5" specularConstant="0.5" specularExponent="10"
                                    result="specOut" lighting-color="white">
                    <fePointLight x="-5000" y="-10000" z="20000"/>
                </feSpecularLighting>
                <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
                <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0"
                             result="litPaint"/>
            </filter>
            <filter id="Bevel3" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="0.2" result="blur"/>
                <feSpecularLighting in="blur" surfaceScale="10" specularConstant="3.5" specularExponent="10"
                                    result="specOut" lighting-color="#ffffff">
                    <fePointLight x="-5000" y="-10000" z="0000"/>
                </feSpecularLighting>
                <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
                <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0"
                             result="litPaint"/>
            </filter>

            <!-- Enhanced drop shadow filter for glass bars -->
            <filter id="glassDropShadow" x="-30%" y="-30%" width="160%" height="160%">
                <feDropShadow dx="0" dy="4" stdDeviation="8" flood-color="rgba(0,0,0,0.2)"/>
            </filter>

            <!-- Frosted glass blur filter -->
            <filter id="glassBlur" x="-10%" y="-10%" width="120%" height="120%">
                <feGaussianBlur in="SourceGraphic" stdDeviation="1.5" />
            </filter>

            <!-- Inner shadow for depth -->
            <filter id="innerShadow" x="-50%" y="-50%" width="200%" height="200%">
                <feOffset dx="0" dy="2"/>
                <feGaussianBlur stdDeviation="2" result="offset-blur"/>
                <feFlood flood-color="rgba(0,0,0,0.2)"/>
                <feComposite in2="offset-blur" operator="in"/>
                <feComposite in2="SourceGraphic" operator="over"/>
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

            <filter id="dropShadow" filterUnits="userSpaceOnUse" width="150%" height="150%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="2"/>
                <feOffset dx="2" dy="2"/>
                <feComponentTransfer>
                    <feFuncA type="linear" slope="0.3"/>
                </feComponentTransfer>
                <feMerge>
                    <feMergeNode/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>
            <style>
                #id_${barGroup.id} .glass-bar {
                    transition: all 0.3s ease;
                }
                #id_${barGroup.id} .glass-bar:hover {
                    filter: url(#glow);
                    transform: scale(1.02);
                    cursor: pointer;
                }
            </style>
        </defs>"""
    }


    private fun addGrid(barGroup: BarGroup): String {
        val maxHeight = 540
        val maxWidth = barGroup.calcWidth()

        // Use slightly elevated colors instead of pure theme colors
        val backgroundColor = if (useDark) "#0A0E1A" else "#FAFBFD" // Not pure black/white
        val gridLineColor = theme.accentColor
        val axisColor = theme.accentColor

        val yGap = maxHeight / 5
        val xGap = maxWidth / (barGroup.maxGroup().series.size + 1)

        val elements = StringBuilder()

        // Improved background with subtle depth
        elements.append("""<rect width='100%' height='100%' fill='$backgroundColor' rx="15" ry="15"/>""")

        // Add horizontal grid lines
        for (i in 1..4) {
            elements.append("""<line x1="90" y1="${i * yGap}" x2="${maxWidth}" y2="${i * yGap}" stroke="${gridLineColor}" stroke-width="1" stroke-dasharray="5,5" stroke-opacity="0.2"/>""")
        }
        // Add vertical grid lines for each data point
        var num = xGap
        barGroup.maxGroup().series.forEach {
            elements.append("""<line x1="${num}" y1="12" x2="${num}" y2="500" stroke="${gridLineColor}" stroke-width="1" stroke-dasharray="5,5"/>""")
            num += xGap
        }

        // Add main x-axis with better styling
        elements.append("""
            <line x1="90" x2="${maxWidth}" y1="500" y2="500" stroke="${axisColor}" stroke-width="2"/>
        """.trimIndent())

        return elements.toString()
    }

    private fun makeXLine(barGroup: BarGroup): String {
        // Use #ccc for axis lines with stroke-width="2" like in bar.svg
        return """<line x1="110" x2="${barGroup.calcWidth() - 10}" y1="500" y2="500" stroke="#ccc" stroke-width="2"/>
            <g transform="translate(${barGroup.calcWidth() - 10},497.5)">
            <polygon id="ppoint" points="0,5 1.6666666666666667,2.5 0,0 5,2.5" stroke-width="2" stroke="#ccc"/>
            </g>
        """.trimMargin()
    }

    private fun makeYLine(barGroup: BarGroup): String {
        // Use #ccc for axis lines with stroke-width="2" like in bar.svg
        return """<line x1="110" x2="110" y1="12" y2="500" stroke="#ccc" stroke-width="2"/>
            <g transform="translate(107.5,16), rotate(-90)">
            <polygon id="ppoint" points="0,5 1.6666666666666667,2.5 0,0 5,2.5" stroke-width="2" stroke="#ccc"/>
            </g>
        """.trimMargin()
    }

    fun makeVGroupBar(group: BarGroup, isPdf: Boolean): Pair<String, CsvResponse> {
        val vGroupBar = VGroupBar()
        return Pair(vGroupBar.makeVerticalBar(group, isPdf), group.toCsv())
    }
    fun makeCondensed(group: BarGroup): Pair<String, CsvResponse> {
        val vGroupBar = BarGroupCondensedMaker()
        return Pair(vGroupBar.makeBar(group), group.toCsv())
    }

    // Determine palette type with fallbacks
    private fun getPaletteType(display: BarGroupDisplay): ColorPaletteFactory.PaletteType {
        return when {
            display.paletteType.isNotBlank() -> {
                // Try to parse the custom palette type
                try {
                    ColorPaletteFactory.PaletteType.valueOf(display.paletteType.uppercase())
                } catch (e: IllegalArgumentException) {
                    // Fallback to default if invalid palette name
                    getDefaultPaletteType(display.useDark)
                }
            }
            else -> getDefaultPaletteType(display.useDark)
        }
    }

    // Get the default palette type based on theme
    private fun getDefaultPaletteType(useDark: Boolean): ColorPaletteFactory.PaletteType {
        return if (useDark) {
            ColorPaletteFactory.PaletteType.URBAN_NIGHT
        } else {
            ColorPaletteFactory.PaletteType.TABLEAU
        }
    }
}




