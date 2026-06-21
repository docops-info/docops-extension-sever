package io.docops.docopsextensionssupport.chart.bar


import io.docops.docopsextensionssupport.chart.NiceScale
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import kotlin.math.cos
import kotlin.math.sin
import kotlin.uuid.ExperimentalUuidApi


class CylinderBarMaker {

    private var theme = ThemeFactory.getTheme(false)
    @OptIn(ExperimentalUuidApi::class)
    fun makeVerticalCylinderBar(bar: Bar, isPDf: Boolean): String {
        theme = if (bar.display.theme.isNotBlank()) {
            ThemeFactory.getThemeByName(bar.display.theme, bar.display.useDark)
        } else {
            ThemeFactory.getThemeByName("modern_editorial", bar.display.useDark)
        }
        val width = 1100.0
        val height = 620.0
        val leftMargin = 100.0
        val rightMargin = 60.0
        val topMargin = 100.0
        val bottomMargin = 110.0

        val chartWidth = width - leftMargin - rightMargin
        val chartHeight = height - topMargin - bottomMargin

        val series = if (bar.display.sorted) {
            bar.series.sortedByDescending { it.value }
        } else {
            bar.series
        }

        val nice = NiceScale(0.0, series.maxOfOrNull { it.value } ?: 1.0)
        val maxValue = nice.getNiceMax()
        val tickSpacing = nice.getTickSpacing()

        val barWidth = if (series.isNotEmpty()) {
            (chartWidth / series.size) * 0.65
        } else {
            64.0
        }
        val spacing = if (series.isNotEmpty()) {
            chartWidth / series.size
        } else {
            100.0
        }

        val cylinderRadius = barWidth / 2.0
        val ellipseRy = cylinderRadius * 0.3 // Height of the ellipse

        val sb = StringBuilder()

        // Start SVG
        sb.append("""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $height" width="$width" height="$height" id="id_${bar.display.id}" role="img" aria-labelledby="title_${bar.display.id}">""")
        sb.append("\n")
        sb.append("""<title id="title_${bar.display.id}">${bar.title}</title>""")
        sb.append("\n")

        // Add defs for gradients, filters, and effects
        sb.append(generateDefs(series, bar.display, bar))
        
        // Background
        sb.append("""<rect width="$width" height="$height" fill="url(#bgWash${if(bar.display.useDark) "Dark" else ""})"/>""")
        sb.append("\n")

        sb.append("""<g class="font ${if(series.size > 12) "dense" else ""}">""")
        sb.append("\n")

        // Title & Subtitle
        sb.append("""<g transform="translate(${width/2} 45)" text-anchor="middle">""")
        sb.append("""<text class="title" x="0" y="0">${bar.title}</text>""")
        sb.append("</g>\n")


        // Y-axis label
        bar.yLabel?.let {
            if (it.isNotEmpty()) {
                sb.append("""<text class="axis-label" x="22" y="${height / 2}" text-anchor="middle" transform="rotate(-90 22 ${height / 2})">${bar.yLabel}</text>""")
                sb.append("\n")
            }
        }

        // X-axis label
        bar.xLabel?.let {
            if (it.isNotEmpty()) {
                sb.append("""<text class="axis-label" x="${width / 2}" y="${height - 22}" text-anchor="middle">${bar.xLabel}</text>""")
                sb.append("\n")
            }
        }

        // Draw Y-axis with grid lines
        val yAxisX = leftMargin
        val yAxisBottom = height - bottomMargin
        val yAxisTop = topMargin

        // Y-axis grid and labels
        var i = 0.0
        while (i <= maxValue) {
            val y = yAxisBottom - (chartHeight * (i / maxValue))
            
            if (i > 0) {
                sb.append("""<line class="grid" x1="$yAxisX" y1="$y" x2="${width - rightMargin}" y2="$y"/>""")
                sb.append("\n")
            }
            sb.append("""<text class="tick" x="${yAxisX - 12}" y="${y + 4}" text-anchor="end">${bar.valueFmt(i)}</text>""")
            sb.append("\n")
            i += tickSpacing
        }

        // Axes
        sb.append("""<line class="axis" x1="$yAxisX" y1="$yAxisTop" x2="$yAxisX" y2="$yAxisBottom"/>""")
        sb.append("\n")
        sb.append("""<line class="axis" x1="$yAxisX" y1="$yAxisBottom" x2="${width - rightMargin}" y2="$yAxisBottom"/>""")
        sb.append("\n")

        // Draw cylindrical bars
        series.forEachIndexed { index, seriesItem ->
            val barHeight = (seriesItem.value / maxValue) * chartHeight
            val x = leftMargin + (spacing * index) + (spacing - barWidth) / 2
            val y = yAxisBottom - barHeight

            // Use modern color palette if no custom color is specified
            val color = seriesItem.itemDisplay?.baseColor ?: theme.chartPalette[index % theme.chartPalette.size].color
            val gradientId = "g${index + 1}_${bar.display.id}"

            sb.append(generateCylinderBar(
                index = index,
                seriesCount = series.size,
                x = x,
                y = y,
                width = barWidth,
                height = barHeight,
                cylinderRadius = cylinderRadius,
                ellipseRy = ellipseRy,
                gradientId = gradientId,
                color = color,
                label = seriesItem.label,
                value = bar.valueFmt(seriesItem.value),
                yAxisBottom = yAxisBottom,
                textColor = theme.primaryText,
                useDark = bar.display.useDark,
                id = bar.display.id, isPDf, fontFamily = theme.fontFamily
            ))
        }

        sb.append("</g>")
        sb.append("</svg>")
        return sb.toString()
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateDefs(series: List<Series>, display: BarDisplay, bar: Bar): String {
        val sb = StringBuilder()
        sb.append("<defs>\n")

        sb.append(theme.fontImport)
        sb.append("\n")

        val useDark = display.useDark
        sb.append("""
        <style><![CDATA[
          :root {
            --bg: ${theme.canvas};
            --surface: ${if (useDark) "#0f1d2d" else "#ffffff"};
            --text-primary: ${theme.primaryText};
            --text-secondary: ${theme.secondaryText};
            --grid: ${theme.accentColor}40;
            --axis: ${theme.accentColor}80;
            --accent: ${theme.accentColor};
          }

          .font { font-family: ${theme.fontFamily}; }
          .title { fill: var(--text-primary); font-size: 28px; font-weight: 700; letter-spacing: -0.01em; }
          .subtitle { fill: var(--text-secondary); font-size: 13px; font-weight: 500; }
          .tick { fill: var(--text-secondary); font-size: 12px; }
          .axis-label { fill: var(--text-secondary); font-size: 13px; font-weight: 600; }
          .x-label { fill: var(--text-secondary); font-size: 11px; font-weight: 600; }
          .value { font-size: 12px; font-weight: 700; }

          .grid { stroke: var(--grid); stroke-width: 1; stroke-dasharray: 4 7; }
          .axis { stroke: var(--axis); stroke-width: 1.6; stroke-linecap: round; }

          .dense .x-label { font-size: 10px; }
          .dense .tick { font-size: 11px; }

          .bar-wrap:hover .rim { stroke-opacity: 0.7; }
        ]]></style>
        """.trimIndent())
        sb.append("\n")

        if(useDark) {
            sb.append("""
            <linearGradient id="bgWashDark" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stop-color="#0b1727"/>
                <stop offset="100%" stop-color="#091321"/>
            </linearGradient>
            """.trimIndent())
        } else {
            sb.append("""
            <linearGradient id="bgWash" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stop-color="#eef2f8"/>
                <stop offset="100%" stop-color="#f9fbff"/>
            </linearGradient>
            """.trimIndent())
        }
        sb.append("\n")

        sb.append("""
            <linearGradient id="highlight" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stop-color="#ffffff" stop-opacity="0" />
                <stop offset="20%" stop-color="#ffffff" stop-opacity="0.5" />
                <stop offset="45%" stop-color="#ffffff" stop-opacity="0" />
                <stop offset="100%" stop-color="#ffffff" stop-opacity="0" />
            </linearGradient>
        """.trimIndent())
        sb.append("\n")

        sb.append("""
            <filter id="cylinderShadow_${display.id}" x="-25%" y="-25%" width="170%" height="190%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="3"/>
                <feOffset dx="0" dy="2" result="off"/>
                <feComponentTransfer>
                    <feFuncA type="linear" slope="0.25"/>
                </feComponentTransfer>
                <feMerge>
                    <feMergeNode/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>
        """.trimIndent())
        sb.append("\n")

        // Generate gradients for each bar using modern colors
        series.forEachIndexed { index, seriesItem ->
            // Use modern color palette if no custom color is specified
            val color = seriesItem.itemDisplay?.baseColor ?: theme.chartPalette[index % theme.chartPalette.size].color
            val gradientId = "g${index + 1}_${display.id}"

            sb.append(generateRadialGradient(gradientId, color))
        }

        sb.append("</defs>\n")
        return sb.toString()
    }

    private fun generateRadialGradient(id: String, baseColor: String): String {
        val (lightColor, midColor, darkColor) = generateColorShades(baseColor)

        return """
            <radialGradient id="$id" cx="30%" cy="28%">
                <stop offset="0%" stop-color="$lightColor" />
                <stop offset="55%" stop-color="$midColor" />
                <stop offset="100%" stop-color="$darkColor" />
            </radialGradient>
        """.trimIndent() + "\n"
    }

    private fun generateColorShades(hexColor: String): Triple<String, String, String> {
        val hex = hexColor.removePrefix("#")
        val r = hex.substring(0, 2).toInt(16)
        val g = hex.substring(2, 4).toInt(16)
        val b = hex.substring(4, 6).toInt(16)

        // Light shade (increase brightness)
        val lightR = minOf(255, (r * 1.4).toInt())
        val lightG = minOf(255, (g * 1.4).toInt())
        val lightB = minOf(255, (b * 1.4).toInt())
        val lightColor = "#${lightR.toString(16).padStart(2, '0')}${lightG.toString(16).padStart(2, '0')}${lightB.toString(16).padStart(2, '0')}"

        // Mid shade (original color)
        val midColor = hexColor

        // Dark shade (decrease brightness)
        val darkR = (r * 0.6).toInt()
        val darkG = (g * 0.6).toInt()
        val darkB = (b * 0.6).toInt()
        val darkColor = "#${darkR.toString(16).padStart(2, '0')}${darkG.toString(16).padStart(2, '0')}${darkB.toString(16).padStart(2, '0')}"

        return Triple(lightColor, midColor, darkColor)
    }

    private fun generateCylinderBar(
        index: Int,
        seriesCount: Int,
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        cylinderRadius: Double,
        ellipseRy: Double,
        gradientId: String,
        color: String,
        label: String?,
        value: String,
        yAxisBottom: Double,
        textColor: String,
        useDark: Boolean, id: String,
        isPDf: Boolean,
        fontFamily: String
    ): String {
        val sb = StringBuilder()
        val cx = width / 2
        val highlightWidth = width * 0.25
        val highlightX = width * 0.18
        
        val stagger = index * 120
        val duration = 900
        val valueBegin = 760 + stagger

        sb.append("""<g class="bar-wrap" transform="translate($x 0)">""")
        sb.append("\n")
        sb.append("""<g filter="url(#cylinderShadow_$id)">""")
        sb.append("\n")

        // Cylinder body (main rectangle with gradient)
        sb.append("""<rect x="0" y="$y" width="$width" height="$height" fill="url(#${gradientId})" rx="2">""")
        sb.append("""<animate attributeName="y" from="$yAxisBottom" to="$y" begin="${stagger}ms" dur="${duration}ms" fill="freeze"/>""")
        sb.append("""<animate attributeName="height" from="0" to="$height" begin="${stagger}ms" dur="${duration}ms" fill="freeze"/>""")
        sb.append("""</rect>""")
        sb.append("\n")

        // Highlight stripe for glass effect
        sb.append("""<rect class="highlight-stripe" x="$highlightX" y="${y + 7}" width="$highlightWidth" height="${maxOf(0.0, height - 14)}" fill="url(#highlight)" opacity="0.6">""")
        sb.append("""<animate attributeName="y" from="${yAxisBottom + 7}" to="${y + 7}" begin="${stagger}ms" dur="${duration}ms" fill="freeze"/>""")
        sb.append("""<animate attributeName="height" from="0" to="${maxOf(0.0, height - 14)}" begin="${stagger}ms" dur="${duration}ms" fill="freeze"/>""")
        sb.append("""</rect>""")
        sb.append("\n")

        // Bottom ellipse (base of cylinder)
        val (_, _, darkColor) = generateColorShades(color)
        sb.append("""<ellipse class="base" cx="$cx" cy="$yAxisBottom" rx="$cylinderRadius" ry="$ellipseRy" fill="$darkColor" opacity="0.75"/>""")
        sb.append("\n")

        // Top ellipse (visible top of cylinder)
        val (lightColor, _, _) = generateColorShades(color)
        sb.append("""<ellipse class="top" cx="$cx" cy="$y" rx="$cylinderRadius" ry="$ellipseRy" fill="$lightColor" opacity="0.95">""")
        sb.append("""<animate attributeName="cy" from="$yAxisBottom" to="$y" begin="${stagger}ms" dur="${duration}ms" fill="freeze"/>""")
        sb.append("""</ellipse>""")
        sb.append("\n")

        // Rim highlight on top ellipse
        sb.append("""<ellipse class="rim" cx="$cx" cy="$y" rx="${cylinderRadius * 0.85}" ry="${ellipseRy * 0.85}" fill="none" stroke="var(--accent)" stroke-width="1.8" stroke-opacity="0.55">""")
        sb.append("""<animate attributeName="cy" from="$yAxisBottom" to="$y" begin="${stagger}ms" dur="${duration}ms" fill="freeze"/>""")
        sb.append("""</ellipse>""")
        sb.append("\n")

        sb.append("</g>\n")

        // Label below the bar
        val rotate = seriesCount > 8
        val rotation = if(rotate) "transform=\"rotate(-32 $cx ${yAxisBottom + 26})\"" else ""
        sb.append("""<text class="x-label" x="$cx" y="${yAxisBottom + 26}" text-anchor="middle" $rotation>$label</text>""")
        sb.append("\n")

        val opacity = if (isPDf) 1.0 else 0.0
        // Value above the bar
        sb.append("""<text class="value" x="$cx" y="${y - 20}" fill="var(--text-primary)" text-anchor="middle" opacity="$opacity">$value""")
        if(!isPDf) {
            sb.append("""<animate attributeName="opacity" from="0" to="1" begin="${valueBegin}ms" dur="260ms" fill="freeze"/>""")
        }
        sb.append("""</text>""")
        sb.append("\n")
        
        sb.append("</g>\n")
        return sb.toString()
    }
}
