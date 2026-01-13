package io.docops.docopsextensionssupport.chart.bar

import io.docops.docopsextensionssupport.chart.ChartColors
import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class BarGroupMaker(val useDark: Boolean) {

    private var fontColor = "#fcfcfc"
    private var theme: DocOpsTheme = ThemeFactory.getTheme(useDark)

    fun makeBar(barGroup: BarGroup, isPdf: Boolean): String {
        barGroup.display.useDark = useDark
        theme = ThemeFactory.getTheme(barGroup.display)
        if ("brutalist".equals(barGroup.display.theme, ignoreCase = true)) {
            val brutalistMaker = CyberBrutalistBarGroupMaker(useDark)
            return brutalistMaker.makeBar(barGroup).first
        }
        fontColor = determineTextColor(barGroup.display.baseColor)
        val sb = StringBuilder()
        sb.append(makeHead(barGroup))
        sb.append(makeDefs(makeGradient(barDisplay = barGroup.display), barGroup=barGroup))
        sb.append(addGrid(barGroup))
        sb.append(makeTitle(barGroup))
        sb.append(makeXLabel(barGroup))
        sb.append(makeYLabel(barGroup))
        var startX = 110.0
        val elements = StringBuilder()
        barGroup.groups.forEach { group ->
            val added = addGroup(barGroup, group, startX, isPdf)
            startX += group.series.size * 45.0 + 2
            elements.append(added)
        }

        sb.append("<g transform='translate(${(barGroup.calcWidth() - startX) / 2},0)'>")
        sb.append(elements.toString())
        sb.append("</g>")
        sb.append(addTicks(barGroup))
        sb.append(addLegend(startX + ((barGroup.calcWidth() - startX)/2), barGroup))
        sb.append(end())
        return  sb.toString()
    }

    private fun addLegend(d: Double, group: BarGroup): String {
        val sb = StringBuilder()
        val distinctLabels = group.legendLabel().distinct()

        // Skip if no distinct labels
        if (distinctLabels.isEmpty()) {
            return ""
        }

        // Calculate legend dimensions and position
        val itemWidth = 120 // Width for each legend item
        val itemHeight = 25 // Height for each legend item
        val itemsPerRow = 4 // Number of items per row
        val legendPadding = 10 // Padding around the legend

        // Calculate number of rows needed
        val rows = Math.ceil(distinctLabels.size.toDouble() / itemsPerRow).toInt()
        val legendWidth = Math.min(group.calcWidth() - 40, itemWidth * itemsPerRow + legendPadding * 2)
        val legendHeight = itemHeight * rows + legendPadding * 2

        // Position the legend below the x-axis categories
        val legendX = (group.calcWidth() - legendWidth) / 2 // Center horizontally
        val legendY = 540 // Position below the x-axis categories

        // Use colors based on ThemeFactory
        val legendBgColor = theme.canvas
        val legendBorderColor = theme.accentColor
        val legendTextColor = theme.primaryText

        // Create legend background
        sb.append("""<rect x="$legendX" y="$legendY" width="$legendWidth" height="$legendHeight" rx="10" ry="10" fill="$legendBgColor" fill-opacity="0.1" stroke="$legendBorderColor" stroke-width="1"/>""")

        // Add legend items
        distinctLabels.forEachIndexed { index, label ->
            val row = index / itemsPerRow
            val col = index % itemsPerRow

            val itemX = legendX + legendPadding + col * itemWidth
            val itemY = legendY + legendPadding + row * itemHeight

            // Use the same gradient as the corresponding bar
            val colorId = "svgGradientColor_$index"

            // Add color box
            sb.append("""<rect x="${itemX}" y="${itemY}" width="15" height="15" rx="3" ry="3" fill="url(#$colorId)"/>""")

            // Add label text
            sb.append("""<text x="${itemX + 25}" y="${itemY + 12}" font-family="${theme.fontFamily}" font-size="12" text-anchor="start" fill="$legendTextColor">$label</text>""")
        }

        return sb.toString()
    }

    private fun addGroup(barGroup: BarGroup, added: Group, startX: Double, isPdf: Boolean): String {
        val sb = StringBuilder()
        var counter = startX
        added.series.forEachIndexed { index, series ->
            val per = barGroup.scaleUp(series.value)
            var color = "url(#svgGradientColor_$index)"
            val barX = counter
            val barY = 500 - per
            val barWidth = 40.0
            val barHeight = per

            // Create glass effect bar with layered structure
            if(isPdf) {
                val svgColor = ChartColors.Companion.getColorForIndex(index)
                color = svgColor.color
                sb.append("""
                    <g class="glass-bar">
                    <!-- Base rectangle with gradient -->
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
                sb.append(
                    """
                <g class="glass-bar">
                    <!-- Base rectangle with gradient -->
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

                    <!-- Glass overlay with transparency -->
                    <rect x="$barX" 
                          y="$barY" 
                          width="$barWidth" 
                          height="$barHeight" 
                          rx="6" 
                          ry="6"
                          fill="url(#glassOverlay)"
                          filter="url(#glassBlur)">
                        <animate attributeName="height" from="0" to="$barHeight" dur="1s" fill="freeze"/>
                        <animate attributeName="y" from="500" to="$barY" dur="1s" fill="freeze"/>
                    </rect>

                    <!-- Radial highlight for realistic light effect -->
                    <ellipse cx="${barX + barWidth / 4}" 
                             cy="${barY + barHeight / 5}" 
                             rx="${barWidth / 3}" 
                             ry="${Math.min(barHeight / 6, 15.0)}"
                             fill="url(#glassRadial)"
                             opacity="0.7">
                        <animate attributeName="cy" from="510" to="${barY + barHeight / 5}" dur="1s" fill="freeze"/>
                    </ellipse>

                    <!-- Top highlight for shine -->
                    <rect x="${barX + 3}" 
                          y="${barY + 3}" 
                          width="${barWidth - 6}" 
                          height="${Math.min(barHeight / 4, 20.0)}" 
                          rx="4" 
                          ry="4"
                          fill="url(#glassHighlight)">
                        <animate attributeName="y" from="497" to="${barY + 3}" dur="1s" fill="freeze"/>
                    </rect>
                </g>
            """.trimIndent()
                )
            }

            if(series.value > 0) {
                // Value label on top of bar with color based on dark mode
                val valueLabelColor =  theme.primaryText
                sb.append("""<text x="${counter + 20}" y="${500 - per - 8}" style="${barGroup.display.barFontValueStyle}; fill: $valueLabelColor; text-anchor: middle; font-weight: bold;">${barGroup.valueFmt(series.value)}</text>""")
            }
            counter += 40.5
        }
        val textX = startX + (added.series.size / 2 * 45.0)
        sb.append(makeSeriesLabel(textX, 510.0, added.label, barGroup))
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
        val finalWidth = Math.max(300, rectWidth)

        // Ensure the title fits within the graph width
        val adjustedWidth = Math.min(finalWidth, barGroup.calcWidth() - 40)

        return """
            <g>
                <rect x="${center - adjustedWidth/2}" y="10" width="$adjustedWidth" height="40" rx="10" ry="10" fill="$titleBgColor" opacity="0.1" stroke="${theme.accentColor}" stroke-width="1"/>
                <text x="$center" y="38" style="font-family: ${theme.fontFamily}; fill: $titleTextColor; text-anchor: middle; font-size: 24px; font-weight: bold;">${barGroup.title}</text>
            </g>
        """.trimIndent()
    }
    private fun makeXLabel(barGroup: BarGroup): String {
        val center = barGroup.calcWidth()/2
        // Use #666 for axis labels like in bar.svg
        val labelColor = theme.secondaryText

        return """
            <text x="$center" y="610" 
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
        val svgHeight = 650
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg id="id_${barGroup.id}" width="${(barGroup.calcWidth() * barGroup.display.scale)/ DISPLAY_RATIO_16_9}" height="${(svgHeight * barGroup.display.scale)/DISPLAY_RATIO_16_9}" viewBox="0 0 ${barGroup.calcWidth()} $svgHeight" xmlns="http://www.w3.org/2000/svg" aria-label='Docops: Bar Group Chart'>
            ${theme.fontImport}
        """.trimIndent()
    }

    private fun makeGradient(barDisplay: BarGroupDisplay): String {
        // Create a gradient matching bar.svg format with two stops
        val baseColor = barDisplay.baseColor
        val darkerColor = darkenColor(baseColor, 0.3) // Create a darker color for the gradient end

        return """
        <linearGradient id="linearGradient_${barDisplay.id}" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" stop-color="$baseColor"/>
            <stop offset="100%" stop-color="$darkerColor"/>
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

    private fun makeDefs(gradients: String, barGroup: BarGroup): String {
        // Define color palettes exactly matching bar.svg gradients
        val modernColors = listOf(
            Triple("#4361ee", "#3a0ca3", "#4361ee"), // Bar 1: Blue to Purple
            Triple("#4cc9f0", "#4361ee", "#4cc9f0"), // Bar 2: Light Blue to Blue
            Triple("#f72585", "#b5179e", "#f72585"), // Bar 3: Pink to Purple
            Triple("#7209b7", "#560bad", "#7209b7"), // Bar 4: Purple to Dark Purple
            Triple("#f77f00", "#d62828", "#f77f00"), // Bar 5: Orange to Red
            Triple("#2a9d8f", "#264653", "#2a9d8f")  // Bar 6: Teal to Dark Blue
        )

        val defGrad = StringBuilder()
        val sz = barGroup.maxGroup().series.size

        // Create gradients matching bar.svg format
        for(i in 0 until sz) {

            val svgColor = ChartColors.Companion.getColorForIndex(i)
            defGrad.append("""
                <linearGradient id="svgGradientColor_$i" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stop-color="${svgColor.lighter()}"/>
                    <stop offset="100%" stop-color="${svgColor.darker()}"/>
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
        val maxData = barGroup.maxData() + 100

        // Define colors based on dark mode, matching bar.svg aesthetics
        val gridLineColor = theme.accentColor
        val axisColor = theme.accentColor
        val backgroundColor = theme.canvas

        // Create a cleaner grid with fewer lines
        val yGap = maxHeight / 5 // Reduced number of horizontal grid lines
        val xGap = maxWidth / (barGroup.maxGroup().series.size + 1)

        val elements = StringBuilder()

        // Use theme canvas for background
        elements.append("""<rect width='100%' height='100%' fill='${backgroundColor}' rx="15" ry="15"/>""")

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

    fun makeVGroupBar(group: BarGroup, isPdf: Boolean): String {
        val vGroupBar = VGroupBar()
        return vGroupBar.makeVerticalBar(group, isPdf)
    }
    fun makeCondensed(group: BarGroup): String {
        val vGroupBar = BarGroupCondensedMaker()
        return vGroupBar.makeBar(group)
    }
}


fun createBarGroupTestData(): BarGroup {
    val seriesA1 = Series(label = "Q1", value = 5000.0)
    val seriesA2 = Series(label = "Q2", value = 7000.0)
    val seriesA3 = Series(label = "Q3", value = 8000.0)
    val seriesA4 = Series(label = "Q4", value = 6000.0)

    val seriesB1 = Series(label = "Q1", value = 6000.0)
    val seriesB2 = Series(label = "Q2", value = 8000.0)
    val seriesB3 = Series(label = "Q3", value = 7000.0)
    val seriesB4 = Series(label = "Q4", value = 9000.0)

    val seriesC1 = Series(label = "Q1", value = 6000.0)
    val seriesC2 = Series(label = "Q2", value = 8000.0)
    val seriesC3 = Series(label = "Q3", value = 7000.0)
    val seriesC4 = Series(label = "Q4", value = 9000.0)

    val seriesD1 = Series(label = "Q1", value = 6000.0)
    val seriesD2 = Series(label = "Q2", value = 8000.0)
    val seriesD3 = Series(label = "Q3", value = 7000.0)
    val seriesD4 = Series(label = "Q4", value = 9000.0)

    val seriesE1 = Series(label = "Q1", value = 6000.0)
    val seriesE2 = Series(label = "Q2", value = 8000.0)
    val seriesE3 = Series(label = "Q3", value = 7000.0)
    val seriesE4 = Series(label = "Q4", value = 9000.0)


    val groupA = Group(
        label = "Product A",
        series = mutableListOf(seriesA1, seriesA2, seriesA3, seriesA4)
    )

    val groupB = Group(
        label = "Product B",
        series = mutableListOf(seriesB1, seriesB2, seriesB3, seriesB4)
    )

    val groupC = Group(
        label = "Product C",
        series = mutableListOf(seriesC1, seriesC2, seriesC3, seriesC4)
    )

    val groupD = Group(label = "Product D", series = mutableListOf(seriesD1, seriesD2, seriesD3, seriesD4))

    val groupE = Group(label = "Product E", series = mutableListOf(seriesE1, seriesE2, seriesE3, seriesE4))
    val barGroup = BarGroup(
        title = "Annual Product Sales Report",
        yLabel = "Sales (USD)",
        xLabel = "Quarters",
        groups = mutableListOf(groupA, groupB, groupC, groupD, groupE),
        display = BarGroupDisplay(lineColor = "#921A40", baseColor = "#F3EDED", barFontValueStyle = "font-family: Arial,Helvetica, sans-serif; font-size:9px;"
            , scale = 1.0, useDark = false)
    )

    return barGroup
}

fun main() {
    val barGroupTestData = createBarGroupTestData()


    val str = Json.encodeToString(barGroupTestData)
    println(str)
    val svg = BarGroupMaker(true).makeBar(barGroupTestData, false)
    val outfile2 = File("gen/groupbar.svg")
    outfile2.writeBytes(svg.toByteArray())
}
