package io.docops.docopsextensionssupport.chart.combo

import io.docops.docopsextensionssupport.chart.NiceScale
import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9

class CombinationChartMaker(val isPdf: Boolean = false) {

    private val defaultColors = listOf(
        "#4361ee", "#4cc9f0", "#4895ef", "#560bad", "#7209b7",
        "#b5179e", "#f72585", "#3f37c9", "#3a0ca3", "#480ca8"
    )
    private var theme: DocOpsTheme = ThemeFactory.getTheme(false)
    private var isModern = false

    // Standardized dimensions from VBarMaker modern layout
    private var width = 960
    private var height = 560
    private var xAxisStart = 120
    private var xAxisEnd = 860
    private var yAxisStart = 100
    private var yAxisEnd = 460
    
    private lateinit var primaryScale: NiceScale
    private lateinit var secondaryScale: NiceScale


    fun makeChart(chart: CombinationChart): String {
        theme = ThemeFactory.getTheme(chart.display)
        isModern = !isPdf && !theme.name.contains("Classic") && !theme.name.contains("Pro")
        
        // Setup scales
        val primaryData = chart.series.filter { it.yAxis == YAxisType.PRIMARY }.flatMap { it.data }
        val primaryMax = if (primaryData.isNotEmpty()) primaryData.maxOf { it.y } else 100.0
        val primaryMin = if (primaryData.isNotEmpty()) primaryData.minOf { it.y } else 0.0
        primaryScale = NiceScale(primaryMin, primaryMax)
        primaryScale.setMaxTicks(8.0)
        
        if (chart.display.dualYAxis) {
            val secondaryData = chart.series.filter { it.yAxis == YAxisType.SECONDARY }.flatMap { it.data }
            val secondaryMax = if (secondaryData.isNotEmpty()) secondaryData.maxOf { it.y } else 100.0
            val secondaryMin = if (secondaryData.isNotEmpty()) secondaryData.minOf { it.y } else 0.0
            secondaryScale = NiceScale(secondaryMin, secondaryMax)
            secondaryScale.setMaxTicks(8.0)
        }

        // Adjust width for legend if needed (giving extra space on the right)
        if (chart.display.showLegend) {
            width = 1160
        }

        val sb = StringBuilder()

        sb.append(makeHead(chart))
        sb.append(makeDefs(chart))
        sb.append(makeModernBackground(chart))
        sb.append(makeTitle(chart))
        sb.append(makeGrid(chart))
        sb.append(makeAxes(chart))
        sb.append(makeAxisLabels(chart))
        sb.append(makeAxisTicks(chart))
        sb.append(makeData(chart))

        if (chart.display.showLegend) {
            sb.append(makeLegend(chart))
        }

        sb.append("</svg>")
        return sb.toString()
    }

    private fun makeHead(chart: CombinationChart): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg id="combo_chart_${chart.id}" width="${(width * chart.display.scale) / DISPLAY_RATIO_16_9}" 
                 height="${(height * chart.display.scale) / DISPLAY_RATIO_16_9}" 
                 viewBox="0 0 $width $height" xmlns="http://www.w3.org/2000/svg" 
                 aria-label='DocOps: Combination Chart'>
                 ${theme.fontImport}
        """.trimIndent()
    }

    private fun makeDefs(chart: CombinationChart): String {
        val sb = StringBuilder()
        sb.append("<defs>")

        val id = chart.id

        // Create gradients for bars
        chart.series.filter { it.type == ChartType.BAR }.forEachIndexed { index, series ->
            val colorIndex = index % defaultColors.size
            val color = series.color ?: defaultColors[colorIndex]
            val darkerColor = darkenColor(color, 0.3)
            val seriesId = series.name.replace(" ", "_")

            sb.append("""
                <linearGradient id="barGradient_${seriesId}_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stop-color="$color"/>
                    <stop offset="100%" stop-color="$darkerColor"/>
                </linearGradient>
            """.trimIndent())
        }

        // Create gradients for line areas
        chart.series.filter { it.type == ChartType.LINE }.forEachIndexed { index, series ->
            val colorIndex = index % defaultColors.size
            val color = series.color ?: defaultColors[colorIndex]
            val seriesId = series.name.replace(" ", "_")

            sb.append("""
                <linearGradient id="lineGradient_${seriesId}_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stop-color="$color" stop-opacity="0.3"/>
                    <stop offset="100%" stop-color="$color" stop-opacity="0.1"/>
                </linearGradient>
            """.trimIndent())
        }

        // Atmosphere glow
        sb.append("""
            <linearGradient id="bgGlow_$id" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stop-color="${if (chart.display.useDark) "#1f2937" else "#dbe8f8"}" stop-opacity="0.65"/>
                <stop offset="100%" stop-color="${theme.canvas}" stop-opacity="0"/>
            </linearGradient>
        """.trimIndent())

        // Add filters
        sb.append("""
            <filter id="dropShadow_$id">
                <feDropShadow dx="2" dy="2" stdDeviation="3" flood-color="rgba(0,0,0,0.3)"/>
            </filter>
            <filter id="glow_$id">
                <feGaussianBlur stdDeviation="4" result="coloredBlur"/>
                <feMerge>
                    <feMergeNode in="coloredBlur"/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>
            <filter id="legendGlass_$id" x="0" y="0" width="100%" height="100%">
                <feGaussianBlur in="SourceGraphic" stdDeviation="2" />
            </filter>
        """.trimIndent())

        // Add glass effect gradients and filters if enabled
        if (chart.display.useGlass) {
            sb.append("""
                <!-- Glass effect gradients -->
                <linearGradient id="glassOverlay_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:rgba(255,255,255,0.4);stop-opacity:1" />
                    <stop offset="30%" style="stop-color:rgba(255,255,255,0.2);stop-opacity:1" />
                    <stop offset="70%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
                    <stop offset="100%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1" />
                </linearGradient>

                <linearGradient id="glassHighlight_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:rgba(255,255,255,0.7);stop-opacity:1" />
                    <stop offset="60%" style="stop-color:rgba(255,255,255,0.3);stop-opacity:1" />
                    <stop offset="100%" style="stop-color:rgba(255,255,255,0);stop-opacity:1" />
                </linearGradient>

                <radialGradient id="glassRadial_$id" cx="30%" cy="30%" r="70%">
                    <stop offset="0%" style="stop-color:rgba(255,255,255,0.5);stop-opacity:1" />
                    <stop offset="70%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
                    <stop offset="100%" style="stop-color:rgba(255,255,255,0);stop-opacity:1" />
                </radialGradient>

                <filter id="glassDropShadow_$id" x="-30%" y="-30%" width="160%" height="160%">
                    <feDropShadow dx="0" dy="4" stdDeviation="8" flood-color="rgba(0,0,0,0.2)"/>
                </filter>

                <filter id="glassBlur_$id" x="-10%" y="-10%" width="120%" height="120%">
                    <feGaussianBlur in="SourceGraphic" stdDeviation="1.5" />
                </filter>

                <filter id="glassGlow_$id" x="-20%" y="-20%" width="140%" height="140%">
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
            """.trimIndent())
        }

        val textColor = theme.primaryText
        val gridColor = theme.accentColor
        val axisColor = theme.accentColor

        val revealAnim = if (isPdf) "none" else "revealBar 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) forwards"
        val pointAnim = if (isPdf) "none" else "revealPoint 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards"
        val fadeInAnim = if (isPdf) "none" else "fadeIn 0.8s ease-out forwards"

        sb.append("""
                <style>
                    #combo_chart_${chart.id} {
                        --bg: ${theme.canvas};
                        --surface: ${if (chart.display.useDark) "#161b22" else "#ffffff"};
                        --text: ${theme.primaryText};
                        --text-soft: ${theme.secondaryText};
                        --grid: ${theme.surfaceImpact};
                        --axis: ${theme.secondaryText};
                        --accent: ${theme.accentColor};
                        ${theme.chartPalette.mapIndexed { i, c -> "--bar-${i + 1}: ${c.color};" }.joinToString("\n                        ")}
                        --bar-radius: ${theme.cornerRadius};
                    }
                    #combo_chart_${chart.id} .chart-text { fill: var(--text) !important; font-family: ${theme.fontFamily}; letter-spacing: -0.5px; }
                    #combo_chart_${chart.id} .chart-grid { stroke: var(--grid); stroke-dasharray: 4,8; stroke-opacity: 0.12; }
                    #combo_chart_${chart.id} .chart-axis { stroke: var(--axis); stroke-width: 1.4; stroke-opacity: 0.35; }
                  
                    @keyframes revealBar {
                        from { transform: scaleY(0); opacity: 0; }
                        to { transform: scaleY(1); opacity: 1; }
                    }
                    @keyframes revealPoint {
                        from { r: 0; opacity: 0; }
                        to { r: 5; opacity: 1; }
                    }
                    @keyframes fadeIn {
                        from { opacity: 0; }
                        to { opacity: 1; }
                    }
                    
                    .bar-reveal {
                        animation: $revealAnim;
                        transform-origin: bottom;
                        opacity: 1;
                    }
                    .point-reveal {
                        animation: $pointAnim;
                        opacity: 1;
                    }
                    .fade-in {
                        animation: $fadeInAnim;
                        opacity: 1;
                    }
                    
                    .bar-inner { 
                        transform-box: fill-box; 
                        transform-origin: 50% 100%; 
                        transition: transform 220ms ease, filter 220ms ease; 
                    }
                    .bar-wrap:focus .bar-inner, .bar-wrap:hover .bar-inner { 
                        transform: scale(1.03); 
                        filter: saturate(1.08); 
                    }
                    .value-label { 
                        font-size: 11px; 
                        font-weight: 600; 
                        fill: var(--text) !important; 
                        opacity: ${if (isPdf) "1" else "0"}; 
                        transition: opacity 180ms ease, transform 180ms ease; 
                        pointer-events: none; 
                    }
                    .bar-wrap:focus .value-label, .bar-wrap:hover .value-label { 
                        opacity: 1; 
                        transform: translateY(-2px); 
                    }
                    
                    #combo_chart_${chart.id} .glass-bar {
                        transition: all 0.3s ease;
                    }
                    #combo_chart_${chart.id} .glass-bar:hover {
                        filter: url(#glassGlow_$id);
                        transform: scale(1.02);
                        cursor: pointer;
                    }
                </style>
            """.trimIndent())

        sb.append("</defs>")
        return sb.toString()
    }

    private fun makeModernBackground(chart: CombinationChart): String {
        return """
            <!-- Atmosphere -->
            <rect width="100%" height="100%" fill="var(--bg, ${theme.canvas})" rx="18" ry="18"/>
            <circle cx="140" cy="80" r="220" fill="url(#bgGlow_${chart.id})"/>
            <rect x="36" y="36" width="${width - 72}" height="${height - 72}" rx="18" fill="${if (chart.display.useDark) "#161b22" else "#ffffff"}" opacity="0.6"/>
        """.trimIndent()
    }

    private fun makeTitle(chart: CombinationChart): String {
        val titleColor = theme.primaryText
        return """
            <g class="fade-in">
                <text x="$xAxisStart" y="60" text-anchor="start" font-size="28" font-weight="900" 
                      fill="$titleColor" font-family="${theme.fontFamily}" style="fill: $titleColor !important;">
                    ${chart.title}
                </text>
            </g>
        """.trimIndent()
    }

    private fun makeGrid(chart: CombinationChart): String {
        if (!chart.display.showGrid) return ""

        val sb = StringBuilder()
        sb.append("""<g class="chart-grid fade-in">""")

        val niceMin = primaryScale.getNiceMin()
        val niceMax = primaryScale.getNiceMax()
        val tickSpacing = primaryScale.getTickSpacing()
        
        var currentY = niceMin
        while (currentY <= niceMax + (tickSpacing / 10.0)) {
            val yPos = yAxisEnd - (currentY - niceMin) / (niceMax - niceMin) * (yAxisEnd - yAxisStart)
            sb.append("""<line x1="$xAxisStart" y1="$yPos" x2="$xAxisEnd" y2="$yPos"/>""")
            currentY += tickSpacing
            if(tickSpacing == 0.0) break
        }

        sb.append("</g>")
        return sb.toString()
    }

    private fun makeAxes(chart: CombinationChart): String {
        val sb = StringBuilder()
        sb.append("""<g class="chart-axis fade-in">""")
        // Primary Y-axis (left)
        sb.append("""<line x1="$xAxisStart" y1="$yAxisStart" x2="$xAxisStart" y2="$yAxisEnd"/>""")

        // X-axis
        sb.append("""<line x1="$xAxisStart" y1="$yAxisEnd" x2="$xAxisEnd" y2="$yAxisEnd"/>""")

        // Secondary Y-axis (right) if dual axis is enabled
        if (chart.display.dualYAxis) {
            sb.append("""<line x1="$xAxisEnd" y1="$yAxisStart" x2="$xAxisEnd" y2="$yAxisEnd"/>""")
        }
        sb.append("""</g>""")
        return sb.toString()
    }

    private fun makeAxisLabels(chart: CombinationChart): String {
        val sb = StringBuilder()
        val textColor = theme.primaryText

        sb.append("""<g class="chart-text fade-in">""")
        // X-axis label
        if (chart.xLabel.isNotEmpty()) {
            sb.append("""
                <text x="${(xAxisStart + xAxisEnd) / 2}" y="${height - 20}" text-anchor="middle" font-size="14" font-weight="bold" style="fill: $textColor !important;">
                    ${chart.xLabel}
                </text>
            """.trimIndent())
        }

        // Primary Y-axis label
        if (chart.yLabel.isNotEmpty()) {
            sb.append("""
                <text x="30" y="${(yAxisStart + yAxisEnd) / 2}" text-anchor="middle" font-size="14" font-weight="bold" 
                      transform="rotate(-90, 30, ${(yAxisStart + yAxisEnd) / 2})" style="fill: $textColor !important;">
                    ${chart.yLabel}
                </text>
            """.trimIndent())
        }

        // Secondary Y-axis label
        if (chart.display.dualYAxis && chart.yLabelSecondary.isNotEmpty()) {
            sb.append("""
                <text x="${width - 30}" y="${(yAxisStart + yAxisEnd) / 2}" text-anchor="middle" font-size="14" font-weight="bold" 
                      transform="rotate(90, ${width - 30}, ${(yAxisStart + yAxisEnd) / 2})" style="fill: $textColor !important;">
                    ${chart.yLabelSecondary}
                </text>
            """.trimIndent())
        }
        sb.append("""</g>""")
        return sb.toString()
    }

    private fun makeAxisTicks(chart: CombinationChart): String {
        val sb = StringBuilder()
        sb.append("""<g class="chart-text fade-in">""")
        val tickColor = if (chart.display.useDark) "#d1d5db" else "#6b7280"
        val textColor = theme.primaryText

        // X-axis ticks
        val xValues = chart.series.flatMap { it.data }.map { it.x }.distinct().sorted()
        if (xValues.isNotEmpty()) {
            val availableWidth = xAxisEnd - xAxisStart
            val barSpacing = availableWidth.toDouble() / xValues.size
            xValues.forEachIndexed { index, xValue ->
                val x = xAxisStart + index * barSpacing + barSpacing / 2
                sb.append("""<line x1="$x" y1="$yAxisEnd" x2="$x" y2="${yAxisEnd + 8}" stroke="$tickColor" stroke-width="1.4" stroke-opacity="0.35"/>""")
                sb.append("""
                    <text x="$x" y="${yAxisEnd + 24}" text-anchor="middle" font-size="12" style="fill: $textColor !important;">$xValue</text>
                """.trimIndent())
            }
        }

        // Primary Y-axis ticks
        val niceMin = primaryScale.getNiceMin()
        val niceMax = primaryScale.getNiceMax()
        val tickSpacing = primaryScale.getTickSpacing()
        
        var currentY = niceMin
        while (currentY <= niceMax + (tickSpacing / 10.0)) {
            val yPos = yAxisEnd - (currentY - niceMin) / (niceMax - niceMin) * (yAxisEnd - yAxisStart)
            sb.append("""<line x1="${xAxisStart - 8}" y1="$yPos" x2="$xAxisStart" y2="$yPos" stroke="$tickColor" stroke-width="1.4" stroke-opacity="0.35"/>""")
            sb.append("""
                <text x="${xAxisStart - 12}" y="$yPos" text-anchor="end" font-size="12" dominant-baseline="middle" style="fill: $textColor !important;">
                    ${formatNumber(currentY)}
                </text>
            """.trimIndent())
            currentY += tickSpacing
            if(tickSpacing == 0.0) break
        }

        // Secondary Y-axis ticks
        if (chart.display.dualYAxis) {
             val sNiceMin = secondaryScale.getNiceMin()
             val sNiceMax = secondaryScale.getNiceMax()
             val sTickSpacing = secondaryScale.getTickSpacing()
             
             var sCurrentY = sNiceMin
             while (sCurrentY <= sNiceMax + (sTickSpacing / 10.0)) {
                 val yPos = yAxisEnd - (sCurrentY - sNiceMin) / (sNiceMax - sNiceMin) * (yAxisEnd - yAxisStart)
                 sb.append("""<line x1="$xAxisEnd" y1="$yPos" x2="${xAxisEnd + 8}" y2="$yPos" stroke="$tickColor" stroke-width="1.4" stroke-opacity="0.35"/>""")
                 sb.append("""
                     <text x="${xAxisEnd + 12}" y="$yPos" text-anchor="start" font-size="12" dominant-baseline="middle" style="fill: $textColor !important;">
                         ${formatNumber(sCurrentY)}
                     </text>
                 """.trimIndent())
                 sCurrentY += sTickSpacing
                 if(sTickSpacing == 0.0) break
             }
        }

        sb.append("""</g>""")
        return sb.toString()
    }

    private fun makeData(chart: CombinationChart): String {
        val sb = StringBuilder()
        val xValues = chart.series.flatMap { it.data }.map { it.x }.distinct().sorted()
        if (xValues.isEmpty()) return ""

        val availableWidth = xAxisEnd - xAxisStart
        val barSpacing = availableWidth.toDouble() / xValues.size
        val barWidth = barSpacing * 0.7
        
        val xPositions = xValues.mapIndexed { index, xValue -> 
            xValue to (xAxisStart + index * barSpacing + barSpacing / 2)
        }.toMap()

        val id = chart.id
        
        // Draw bars
        val barSeries = chart.series.filter { it.type == ChartType.BAR }
        barSeries.forEachIndexed { seriesIndex, series ->
            val colorIndex = seriesIndex % theme.chartPalette.size
            val seriesColor = series.color ?: theme.chartPalette[colorIndex].color
            val seriesId = series.name.replace(" ", "_")
            val gradientId = "barGradient_${seriesId}_$id"

            series.data.forEach { dataPoint ->
                val x = xPositions[dataPoint.x]!! - barWidth / 2
                val y = getYPos(dataPoint.y, series.yAxis)
                val baseY = getBaseY(series.yAxis)
                val barHeight = Math.abs(baseY - y)
                val topY = Math.min(y, baseY)

                if (isModern) {
                    val fillColor = if (series.color != null) series.color else "var(--bar-${colorIndex + 1})"
                    val cornerRadius = "var(--bar-radius)"
                    
                    if (chart.display.useGlass) {
                        sb.append("""
                            <g class="bar-wrap glass-bar bar-reveal" style="animation-delay: ${0.1 + (seriesIndex * 0.1)}s;">
                                <g class="bar-inner">
                                    <rect x="$x" y="$topY" width="$barWidth" height="$barHeight" rx="$cornerRadius" ry="$cornerRadius" 
                                          fill="$fillColor" filter="url(#glassDropShadow_$id)" stroke="rgba(255,255,255,0.3)" stroke-width="1">
                                        <title>${series.name}: ${dataPoint.x} = ${formatNumber(dataPoint.y)}</title>
                                    </rect>
                                    <rect x="$x" y="$topY" width="$barWidth" height="$barHeight" rx="$cornerRadius" ry="$cornerRadius"
                                          fill="url(#glassOverlay_$id)" filter="url(#glassBlur_$id)"/>
                                    <ellipse cx="${x + barWidth/4}" cy="${topY + barHeight/5}" rx="${barWidth/3}" ry="${Math.min(barHeight/6, 15.0)}"
                                             fill="url(#glassRadial_$id)" opacity="0.7"/>
                                    <rect x="${x + 3}" y="${topY + 3}" width="${barWidth - 6}" height="${Math.min(barHeight/4, 20.0)}" rx="4" ry="4"
                                          fill="url(#glassHighlight_$id)"/>
                                </g>
                                <text x="${x + barWidth / 2}" y="${topY - 8}" text-anchor="middle" class="value-label">
                                    ${formatNumber(dataPoint.y)}
                                </text>
                            </g>
                        """.trimIndent())
                    } else {
                        sb.append("""
                            <g class="bar-wrap bar-reveal" style="animation-delay: ${0.1 + (seriesIndex * 0.1)}s;">
                                <g class="bar-inner">
                                    <rect x="$x" y="$topY" width="$barWidth" height="$barHeight" rx="$cornerRadius" ry="$cornerRadius" 
                                          fill="$fillColor" filter="url(#dropShadow_$id)">
                                        <title>${series.name}: ${dataPoint.x} = ${formatNumber(dataPoint.y)}</title>
                                    </rect>
                                </g>
                                <text x="${x + barWidth / 2}" y="${topY - 8}" text-anchor="middle" class="value-label">
                                    ${formatNumber(dataPoint.y)}
                                </text>
                            </g>
                        """.trimIndent())
                    }
                } else {
                    if (chart.display.useGlass) {
                        sb.append("""
                            <g class="glass-bar bar-reveal">
                                <rect x="$x" y="$topY" width="$barWidth" height="$barHeight" rx="6" ry="6" 
                                      fill="url(#$gradientId)" filter="url(#glassDropShadow_$id)" stroke="rgba(255,255,255,0.3)" stroke-width="1">
                                    <title>${series.name}: ${dataPoint.x} = ${formatNumber(dataPoint.y)}</title>
                                </rect>
                                <rect x="$x" y="$topY" width="$barWidth" height="$barHeight" rx="6" ry="6"
                                      fill="url(#glassOverlay_$id)" filter="url(#glassBlur_$id)"/>
                                <ellipse cx="${x + barWidth/4}" cy="${topY + barHeight/5}" rx="${barWidth/3}" ry="${Math.min(barHeight/6, 15.0)}"
                                         fill="url(#glassRadial_$id)" opacity="0.7"/>
                                <rect x="${x + 3}" y="${topY + 3}" width="${barWidth - 6}" height="${Math.min(barHeight/4, 20.0)}" rx="4" ry="4"
                                      fill="url(#glassHighlight_$id)"/>
                            </g>
                        """.trimIndent())
                    } else {
                        sb.append("""
                            <rect x="$x" y="$topY" width="$barWidth" height="$barHeight" 
                                  fill="url(#$gradientId)" filter="url(#dropShadow_$id)" rx="3" ry="3" class="bar-reveal">
                                <title>${series.name}: ${dataPoint.x} = ${formatNumber(dataPoint.y)}</title>
                            </rect>
                        """.trimIndent())
                    }
                    
                    // Value label for non-modern
                    if (barHeight > 20) {
                        sb.append("""
                            <text x="${x + barWidth / 2}" y="${topY - 8}" text-anchor="middle" 
                                  font-size="10" class="chart-text fade-in" style="fill: ${theme.primaryText} !important;">
                                ${formatNumber(dataPoint.y)}
                            </text>
                        """.trimIndent())
                    }
                }
            }
        }

        // Draw lines
        val lineSeries = chart.series.filter { it.type == ChartType.LINE }
        lineSeries.forEachIndexed { seriesIndex, series ->
            val colorIndex = seriesIndex % defaultColors.size
            val color = series.color ?: defaultColors[colorIndex]
            val seriesId = series.name.replace(" ", "_")
            val gradientId = "lineGradient_${seriesId}_$id"

            if (series.data.isNotEmpty()) {
                val sortedData = series.data.sortedBy { it.x }
                val pathData = StringBuilder()
                val areaData = StringBuilder()

                sortedData.forEachIndexed { index, dataPoint ->
                    val x = xPositions[dataPoint.x]!!
                    val y = getYPos(dataPoint.y, series.yAxis)

                    if (index == 0) {
                        pathData.append("M $x $y")
                        areaData.append("M $x $y")
                    } else {
                        if (chart.display.smoothLines) {
                            val prevX = xPositions[sortedData[index - 1].x]!!
                            val prevY = getYPos(sortedData[index - 1].y, series.yAxis)

                            val controlX1 = prevX + (x - prevX) / 3
                            val controlY1 = prevY
                            val controlX2 = x - (x - prevX) / 3
                            val controlY2 = y

                            pathData.append(" C $controlX1 $controlY1 $controlX2 $controlY2 $x $y")
                            areaData.append(" C $controlX1 $controlY1 $controlX2 $controlY2 $x $y")
                        } else {
                            pathData.append(" L $x $y")
                            areaData.append(" L $x $y")
                        }
                    }
                }

                val lastX = xPositions[sortedData.last().x]!!
                val firstX = xPositions[sortedData.first().x]!!
                val baseY = getBaseY(series.yAxis)
                areaData.append(" L $lastX $baseY L $firstX $baseY Z")

                sb.append("""<path d="$areaData" fill="url(#$gradientId)" opacity="0.3" class="fade-in"/>""")
                sb.append("""<path d="$pathData" fill="none" stroke="$color" stroke-width="3" stroke-linejoin="round" stroke-linecap="round" class="fade-in"/>""")

                if (chart.display.showPoints) {
                    sortedData.forEachIndexed { pointIndex, dataPoint ->
                        val x = xPositions[dataPoint.x]!!
                        val y = getYPos(dataPoint.y, series.yAxis)
                        val pointStroke = if (chart.display.useDark) "#1f2937" else "#ffffff"
                        
                        sb.append("""
                            <circle cx="$x" cy="$y" r="5" fill="$color" stroke="$pointStroke" 
                                    stroke-width="2" filter="url(#dropShadow_$id)" class="point-reveal" style="animation-delay: ${pointIndex * 0.05}s">
                                <title>${series.name}: ${dataPoint.x} = ${formatNumber(dataPoint.y)}</title>
                            </circle>
                        """.trimIndent())
                        sb.append("""
                            <text x="$x" y="${y - 12}" text-anchor="middle" font-size="10" 
                                  class="chart-text fade-in" style="fill: ${theme.primaryText} !important;">
                                ${formatNumber(dataPoint.y)}
                            </text>
                        """.trimIndent())
                    }
                }
            }
        }
        return sb.toString()
    }

    private fun getYPos(value: Double, yAxisType: YAxisType): Double {
        val scale = if (yAxisType == YAxisType.PRIMARY) primaryScale else secondaryScale
        val niceMin = scale.getNiceMin()
        val niceMax = scale.getNiceMax()
        if (niceMax == niceMin) return (yAxisStart + yAxisEnd) / 2.0
        return yAxisEnd - (value - niceMin) / (niceMax - niceMin) * (yAxisEnd - yAxisStart)
    }

    private fun getBaseY(yAxisType: YAxisType): Double {
        val scale = if (yAxisType == YAxisType.PRIMARY) primaryScale else secondaryScale
        val niceMin = scale.getNiceMin()
        val niceMax = scale.getNiceMax()
        if (niceMin <= 0.0 && niceMax >= 0.0) {
            return getYPos(0.0, yAxisType)
        }
        return if (niceMin > 0.0) getYPos(niceMin, yAxisType) else getYPos(niceMax, yAxisType)
    }

    private fun makeLegend(chart: CombinationChart): String {
        val sb = StringBuilder()
        val id = chart.id
        val legendBgColor = if (chart.display.useDark) "rgba(30, 41, 59, 0.7)" else "rgba(255, 255, 255, 0.7)"
        val textColor = theme.primaryText

        val legendX = xAxisEnd + 60
        val legendY = yAxisStart
        val legendWidth = 180
        val legendHeight = 20 + (chart.series.size * 30)

        sb.append("""
            <g class="fade-in">
                <rect x="$legendX" y="$legendY" width="$legendWidth" height="$legendHeight" 
                      fill="$legendBgColor" stroke="${theme.accentColor}" stroke-width="1" stroke-opacity="0.2" 
                      rx="12" ry="12" filter="url(#legendGlass_$id)"/>
        """.trimIndent())

        chart.series.forEachIndexed { index, series ->
            val colorIndex = index % defaultColors.size
            val color = series.color ?: defaultColors[colorIndex]
            val yPos = legendY + 25 + (index * 30)

            if (series.type == ChartType.BAR) {
                sb.append("""<rect x="${legendX + 15}" y="${yPos - 10}" width="16" height="16" fill="$color" rx="4" ry="4"/>""")
            } else {
                sb.append("""
                    <line x1="${legendX + 10}" y1="$yPos" x2="${legendX + 30}" y2="$yPos" stroke="$color" stroke-width="3" stroke-linecap="round"/>
                    <circle cx="${legendX + 20}" cy="$yPos" r="4" fill="$color" stroke="${if (chart.display.useDark) "#1f2937" else "#ffffff"}" stroke-width="1.5"/>
                """.trimIndent())
            }

            val axisLabel = if (chart.display.dualYAxis && series.yAxis == YAxisType.SECONDARY) " (R)" else ""
            sb.append("""
                <text x="${legendX + 45}" y="${yPos + 5}" font-size="12" font-weight="500" class="chart-text" style="fill: $textColor !important;">${series.name}$axisLabel</text>
            """.trimIndent())
        }
        sb.append("</g>")
        return sb.toString()
    }

    private fun darkenColor(hexColor: String, factor: Double): String {
        val hex = hexColor.replace("#", "")
        if (hex.length != 6 || !hex.matches(Regex("[0-9A-Fa-f]{6}"))) return "#333333"
        val r = Integer.parseInt(hex.substring(0, 2), 16)
        val g = Integer.parseInt(hex.substring(2, 4), 16)
        val b = Integer.parseInt(hex.substring(4, 6), 16)
        val newR = (r * (1 - factor)).toInt().coerceIn(0, 255)
        val newG = (g * (1 - factor)).toInt().coerceIn(0, 255)
        val newB = (b * (1 - factor)).toInt().coerceIn(0, 255)
        return String.format("#%02x%02x%02x", newR, newG, newB)
    }

    private fun formatNumber(value: Double): String {
        if (Math.abs(value) < 0.0001) return "0"
        return if (value == value.toInt().toDouble()) value.toInt().toString() else String.format("%.1f", value)
    }
}
