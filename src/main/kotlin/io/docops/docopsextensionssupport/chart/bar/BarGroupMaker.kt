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
    private var isModern = false
    private var width = 800
    private var height = 680
    private var xAxisStart = 110
    private var xAxisEnd = 800
    private var yAxisStart = 12
    private var yAxisEnd = 500
    private var barWidth = 40.0
    private var barSpacing = 45.0
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
            ThemeFactory.getThemeByName("modern_editorial", barGroup.display.useDark)
        }
        isModern = !theme.name.contains("Classic") && !theme.name.contains("Pro")
        if (isModern) {
            width = 960
            height = 700
            xAxisStart = 120
            xAxisEnd = 860
            yAxisStart = 120
            yAxisEnd = 470
            barWidth = 28.0
            barSpacing = 42.0
        } else {
            width = barGroup.calcWidth()
            height = 680
            xAxisStart = 110
            xAxisEnd = width - 10
            yAxisStart = 12
            yAxisEnd = 500
            barWidth = 40.0
            barSpacing = 45.0
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
        if (isModern && !isPdf) {
            sb.append(makeModernDefs(barGroup, paletteType))
            sb.append(makeModernBackground())
            sb.append(makeModernHeader(barGroup))
            sb.append(addModernPlotArea(barGroup))
            
            var currentX = xAxisStart + 110.0 // Starting offset for the first group
            if (barGroup.groups.size == 1) {
                currentX = (xAxisStart + xAxisEnd) / 2.0 - (barGroup.groups[0].series.size * barSpacing) / 2.0
            } else {
                // Distribute groups across the plot area
                val availableWidth = xAxisEnd - xAxisStart
                val groupAreaWidth = availableWidth / barGroup.groups.size
                currentX = xAxisStart + (groupAreaWidth - (barGroup.groups[0].series.size * barSpacing)) / 2.0
            }

            barGroup.groups.forEachIndexed { groupIdx, group ->
                sb.append(addModernGroup(barGroup, group, currentX, groupIdx))
                currentX += (xAxisEnd - xAxisStart) / barGroup.groups.size.toDouble()
            }
            
            sb.append(addModernLegend(barGroup))
        } else {
            sb.append(makeDefs(makeGradient(barDisplay = barGroup.display), barGroup = barGroup, paletteType = paletteType))
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

            sb.append("<g transform='translate(${(width - startX) / 2},0)'>")
            sb.append(elements.toString())
            sb.append("</g>")
            sb.append(addTicks(barGroup))
            sb.append(addLegend(startX + ((width - startX) / 2), barGroup))
        }
        sb.append(end())
        return Pair(sb.toString(), barGroup.toCsv())
    }

    private fun makeModernDefs(barGroup: BarGroup, paletteType: ColorPaletteFactory.PaletteType): String {
        val defs = StringBuilder()
        val sz = barGroup.maxGroup().series.size

        // Chart Palette - use standard colors if paletteType is not specific
        val colors = if (barGroup.display.paletteType.isNotBlank()) {
            (0 until sz).map { ColorPaletteFactory.getColorCyclic(paletteType, it) ?: "#4361ee" }
        } else {
            theme.chartPalette
        }

        val paletteVars = colors.mapIndexed { i, color ->
            "--q${i + 1}:$color;"
        }.joinToString("\n")

        defs.append("""
        <defs>
            <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stop-color="${theme.canvas}"/>
                <stop offset="100%" stop-color="${if (useDark) "#111a24" else "#f0f2f5"}"/>
            </linearGradient>
            <radialGradient id="washA" cx="16%" cy="12%" r="46%">
                <stop offset="0%" stop-color="${colors.getOrElse(0) { "#39bae6" }}" stop-opacity="0.18"/>
                <stop offset="100%" stop-color="${colors.getOrElse(0) { "#39bae6" }}" stop-opacity="0"/>
            </radialGradient>
            <radialGradient id="washB" cx="84%" cy="18%" r="40%">
                <stop offset="0%" stop-color="${colors.getOrElse(1 % colors.size) { "#ff8f40" }}" stop-opacity="0.16"/>
                <stop offset="100%" stop-color="${colors.getOrElse(1 % colors.size) { "#ff8f40" }}" stop-opacity="0"/>
            </radialGradient>

            <filter id="soft" x="-30%" y="-30%" width="160%" height="160%">
                <feDropShadow dx="0" dy="2" stdDeviation="3" flood-color="#000000" flood-opacity="0.45"/>
            </filter>

            <style>
                @import url('https://fonts.googleapis.com/css2?family=IBM+Plex+Sans:wght@400;500;600;700&amp;display=swap');

                :root{
                --ink:${theme.primaryText};
                --muted:${theme.secondaryText};
                --line:${theme.accentColor}40;
                --axis:${theme.accentColor}80;
                --panel:${if (useDark) "#151e28" else "#ffffff"};
                --panel-stroke:${theme.accentColor}30;

                $paletteVars
                }

                text{font-family:'IBM Plex Sans', sans-serif; fill:var(--ink);}
                .title{font-size:30px;font-weight:700;letter-spacing:-0.01em;}
                .sub{font-size:12px;font-weight:500;fill:var(--muted);}
                .axis-label{font-size:13px;font-weight:600;fill:var(--muted);}
                .tick{font-size:11px;font-weight:500;fill:var(--muted);}
                .group-label{font-size:14px;font-weight:700;fill:var(--ink);}
                .value{font-size:11px;font-weight:600;fill:var(--ink);opacity:.9}
                .legend-text{font-size:12px;font-weight:600;fill:var(--muted);}
                .grid{stroke:var(--line);stroke-width:1;stroke-dasharray:4 8;}
                .axis{stroke:var(--axis);stroke-width:1.4;stroke-linecap:round;}

                .bar-wrap .bar-inner{
                transform-box: fill-box;
                transform-origin: 50% 100%;
                animation: rise .7s cubic-bezier(.2,.8,.2,1) both;
                }
                .bar-wrap:hover .bar-inner{transform:scaleY(1.03);}
                
                @keyframes rise{
                from{transform:scaleY(0);}
                to{transform:scaleY(1);}
                }
            </style>
        </defs>
        """.trimIndent())
        return defs.toString()
    }

    private fun makeModernBackground(): String {
        return """
        <rect width="$width" height="$height" fill="url(#bg)"/>
        <rect width="$width" height="$height" fill="url(#washA)"/>
        <rect width="$width" height="$height" fill="url(#washB)"/>
        """.trimIndent()
    }

    private fun makeModernHeader(barGroup: BarGroup): String {
        return """
        <g transform="translate(64 52)">
            <text class="title" x="0" y="0">${barGroup.title}</text>
            <text class="sub" x="0" y="24">Grouped bars • Theme: ${barGroup.display.theme}</text>
            <line x1="0" y1="38" x2="${width - 128}" y2="38" stroke="${theme.accentColor}40" stroke-width="1"/>
        </g>
        """.trimIndent()
    }

    private fun addModernPlotArea(barGroup: BarGroup): String {
        val sb = StringBuilder()
        val nice = barGroup.ticks()
        val minV = nice.getNiceMin()
        val maxV = nice.getNiceMax()
        val tickSpacing = nice.getTickSpacing()
        
        sb.append("<g>")
        
        // Grid lines and Y ticks
        var i = minV
        while (i <= maxV) {
            val ratio = (i - minV) / (maxV - minV)
            val y = yAxisEnd - (ratio * (yAxisEnd - yAxisStart))
            
            if (i > minV) {
                sb.append("""<line class="grid" x1="$xAxisStart" y1="$y" x2="$xAxisEnd" y2="$y"/>""")
            }
            
            sb.append("""<text class="tick" x="${xAxisStart - 12}" y="${y + 4}" text-anchor="end">${barGroup.valueFmt(i)}</text>""")
            i += tickSpacing
        }

        // Axes
        sb.append("""<line class="axis" x1="$xAxisStart" y1="$yAxisStart" x2="$xAxisStart" y2="$yAxisEnd"/>""")
        sb.append("""<line class="axis" x1="$xAxisStart" y1="$yAxisEnd" x2="$xAxisEnd" y2="$yAxisEnd"/>""")

        // Axis labels
        if (!barGroup.xLabel.isNullOrBlank()) {
            sb.append("""<text class="axis-label" x="${(xAxisStart + xAxisEnd) / 2}" y="${yAxisEnd + 44}" text-anchor="middle">${barGroup.xLabel}</text>""")
        }
        if (!barGroup.yLabel.isNullOrBlank()) {
            sb.append("""<text class="axis-label" x="44" y="${(yAxisStart + yAxisEnd) / 2}" text-anchor="middle" transform="rotate(-90 44 ${(yAxisStart + yAxisEnd) / 2})">${barGroup.yLabel}</text>""")
        }
        
        sb.append("</g>")
        return sb.toString()
    }

    private fun addModernGroup(barGroup: BarGroup, group: Group, startX: Double, groupIdx: Int): String {
        val sb = StringBuilder()
        val nice = barGroup.ticks()
        val minV = nice.getNiceMin()
        val maxV = nice.getNiceMax()
        
        sb.append("""<g aria-label="${group.label}">""")
        
        group.series.forEachIndexed { index, series ->
            val ratio = (series.value - minV) / (maxV - minV)
            val barHeight = ratio * (yAxisEnd - yAxisStart)
            val barX = startX + (index * barSpacing)
            val barY = yAxisEnd - barHeight
            
            val delayClass = "d${groupIdx * barGroup.maxGroup().series.size + index + 1}"
            
            // Add custom delay for this bar if it doesn't exist in style yet
            // Actually I should probably just use inline style for delay to be safe
            val delay = (groupIdx * group.series.size + index) * 0.07
            
            sb.append("""
            <g class="bar-wrap" transform="translate($barX $barY)">
                <g class="bar-inner" style="animation-delay: ${delay}s">
                    <rect width="$barWidth" height="$barHeight" rx="${theme.cornerRadius}" fill="var(--q${index + 1})" filter="url(#soft)"/>
                </g>
                <text class="value" x="${barWidth / 2}" y="-8" text-anchor="middle">${barGroup.valueFmt(series.value)}</text>
            </g>
            """.trimIndent())
        }
        
        val groupLabelX = startX + (group.series.size * barSpacing) / 2.0 - (barSpacing - barWidth) / 2.0
        sb.append("""<text class="group-label" x="$groupLabelX" y="${yAxisEnd + 75}" text-anchor="middle">${group.label}</text>""")
        
        sb.append("</g>")
        return sb.toString()
    }

    private fun addModernLegend(barGroup: BarGroup): String {
        val sb = StringBuilder()
        val distinctLabels = barGroup.legendLabel().distinct()
        if (distinctLabels.isEmpty()) return ""

        val itemsPerRow = 4
        val rows = ceil(distinctLabels.size / itemsPerRow.toDouble()).toInt().coerceAtLeast(1)
        val legendWidth = 480.0
        val legendHeight = 38.0 + (rows * 22.0)
        val legendX = (width - legendWidth) / 2.0
        val legendY = height - legendHeight - 20.0

        sb.append("""
        <g transform="translate($legendX $legendY)">
            <rect x="0" y="0" width="$legendWidth" height="$legendHeight" rx="12" fill="var(--panel)" stroke="var(--panel-stroke)"/>
            <text x="14" y="22" class="legend-text" style="font-weight:700;">Categories</text>
        """)

        val itemWidth = (legendWidth - 28) / itemsPerRow
        distinctLabels.forEachIndexed { index, label ->
            val col = index % itemsPerRow
            val row = index / itemsPerRow
            val itemX = 14.0 + (col * itemWidth)
            val itemY = 34.0 + (row * 22.0)
            
            sb.append("""
            <rect x="$itemX" y="$itemY" width="12" height="12" rx="3" fill="var(--q${index + 1})"/>
            <text class="legend-text" x="${itemX + 18}" y="${itemY + 10}">$label</text>
            """)
        }

        sb.append("</g>")
        return sb.toString()
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
        return """
        <?xml version="1.0" encoding="UTF-8"?>
        <svg id="id_${barGroup.id}" width="${(width * barGroup.display.scale) / DISPLAY_RATIO_16_9}" height="${(height * barGroup.display.scale) / DISPLAY_RATIO_16_9}" viewBox="0 0 $width $height" xmlns="http://www.w3.org/2000/svg" aria-label='Docops: Bar Group Chart'>
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




