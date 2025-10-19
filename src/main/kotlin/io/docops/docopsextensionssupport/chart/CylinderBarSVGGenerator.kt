package io.docops.docopsextensionssupport.chart


import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.util.BackgroundHelper
import kotlin.math.abs
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid



class CylinderBarMaker {

    @OptIn(ExperimentalUuidApi::class)
    fun makeVerticalCylinderBar(bar: Bar): String {
        val width = 800.0
        val height = 600.0
        val leftMargin = 80.0
        val rightMargin = 50.0
        val topMargin = 100.0
        val bottomMargin = 100.0

        val chartWidth = width - leftMargin - rightMargin
        val chartHeight = height - topMargin - bottomMargin

        val series = if (bar.display.sorted) {
            bar.series.sortedByDescending { it.value }
        } else {
            bar.series
        }

        val maxValue = series.maxOfOrNull { it.value } ?: 1.0
        val barWidth = if (series.isNotEmpty()) {
            (chartWidth / series.size) * 0.6
        } else {
            50.0
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
        sb.append("""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $height" width="${width / DISPLAY_RATIO_16_9}" height="${height / DISPLAY_RATIO_16_9}" id="id_${bar.display.id}">""")
        sb.append("\n")

        // Add defs for gradients, filters, and effects
        sb.append(generateDefs(series, bar.display))

        // Background
        val textColor = if (bar.display.useDark) "#e0e0e0" else "#111111"
        val bg = BackgroundHelper.getBackGroundPath(useDark = bar.display.useDark, bar.display.id, width = width.toFloat(), height=height.toFloat())
        sb.append(bg)
        sb.append("\n")

        // Title
        sb.append("""<text x="${width / 2}" y="50" text-anchor="middle" font-family="Arial, sans-serif" font-size="24" font-weight="bold" fill="$textColor">${bar.title}</text>""")
        sb.append("\n")

        // Y-axis label
        bar.yLabel?.let {
            if (it.isNotEmpty()) {
                sb.append("""<text x="20" y="${height / 2}" font-family="Arial, sans-serif" text-anchor="middle" font-size="14" fill="$textColor" transform="rotate(-90 20 ${height / 2})">${bar.yLabel}</text>""")
                sb.append("\n")
            }
        }

        // X-axis label
        bar.xLabel?.let {
            if (it.isNotEmpty()) {
                sb.append("""<text x="${width / 2}" y="${height - 20}" font-family="Arial, sans-serif" text-anchor="middle" font-size="14" fill="$textColor">${bar.xLabel}</text>""")
                sb.append("\n")
            }
        }

        // Draw Y-axis with grid lines
        val yAxisX = leftMargin
        val yAxisBottom = height - bottomMargin
        val yAxisTop = topMargin

        sb.append("""<line x1="$yAxisX" y1="$yAxisTop" x2="$yAxisX" y2="$yAxisBottom" stroke="$textColor" stroke-width="2"/>""")
        sb.append("\n")

        // Y-axis grid and labels
        val numYTicks = 5
        for (i in 0..numYTicks) {
            val y = yAxisBottom - (chartHeight * i / numYTicks)
            val value = (maxValue * i / numYTicks)
            val gridColor = if (bar.display.useDark) "#444444" else "#e0e0e0"

            sb.append("""<line x1="$yAxisX" y1="$y" x2="${width - rightMargin}" y2="$y" stroke="$gridColor" stroke-width="1" opacity="0.5"/>""")
            sb.append("\n")
            sb.append("""<text x="${yAxisX - 10}" y="${y + 5}" font-family="Arial, sans-serif" text-anchor="end" font-size="12" fill="$textColor">${bar.valueFmt(value)}</text>""")
            sb.append("\n")
        }

        // Draw X-axis
        sb.append("""<line x1="$yAxisX" y1="$yAxisBottom" x2="${width - rightMargin}" y2="$yAxisBottom" stroke="$textColor" stroke-width="2"/>""")
        sb.append("\n")

        // Draw cylindrical bars
        series.forEachIndexed { index, seriesItem ->
            val barHeight = (seriesItem.value / maxValue) * chartHeight
            val x = leftMargin + (spacing * index) + (spacing - barWidth) / 2
            val y = yAxisBottom - barHeight

            // Use modern color palette if no custom color is specified
            val color = seriesItem.itemDisplay?.baseColor ?: ChartColors.getColorForIndex(index).color
            val gradientId = "cylinderGradient${index}_${bar.display.id}"

            sb.append(generateCylinderBar(
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
                textColor = textColor,
                useDark = bar.display.useDark,
                id = bar.display.id
            ))
        }

        sb.append("</svg>")
        return sb.toString()
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateDefs(series: List<Series>, display: BarDisplay): String {
        val sb = StringBuilder()
        sb.append("<defs>\n")

        sb.append(BackgroundHelper.getBackgroundGradient(display.useDark, display.id))
        // Generate gradients for each bar using modern colors
        series.forEachIndexed { index, seriesItem ->
            // Use modern color palette if no custom color is specified
            val color = seriesItem.itemDisplay?.baseColor ?: ChartColors.getColorForIndex(index).color
            val gradientId = "cylinderGradient${index}_${display.id}"

            sb.append(generateRadialGradient(gradientId, color))
        }

        // Highlight gradient for glass effect
        sb.append("""
            <linearGradient id="highlight" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" style="stop-color:#ffffff;stop-opacity:0" />
                <stop offset="20%" style="stop-color:#ffffff;stop-opacity:0.6" />
                <stop offset="40%" style="stop-color:#ffffff;stop-opacity:0" />
                <stop offset="100%" style="stop-color:#ffffff;stop-opacity:0" />
            </linearGradient>
        """.trimIndent())
        sb.append("\n")

        // Shadow filter
        sb.append("""
            <filter id="cylinderShadow_${display.id}">
                <feGaussianBlur in="SourceAlpha" stdDeviation="3"/>
                <feOffset dx="2" dy="2" result="offsetblur"/>
                <feComponentTransfer>
                    <feFuncA type="linear" slope="0.3"/>
                </feComponentTransfer>
                <feMerge>
                    <feMergeNode/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>
        """.trimIndent())
        sb.append("\n")

        sb.append("</defs>\n")
        return sb.toString()
    }

    private fun generateRadialGradient(id: String, baseColor: String): String {
        val (lightColor, midColor, darkColor) = generateColorShades(baseColor)

        return """
            <radialGradient id="$id" cx="30%" cy="30%">
                <stop offset="0%" style="stop-color:$lightColor;stop-opacity:1" />
                <stop offset="50%" style="stop-color:$midColor;stop-opacity:0.8" />
                <stop offset="100%" style="stop-color:$darkColor;stop-opacity:0.9" />
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
        useDark: Boolean, id: String
    ): String {
        val sb = StringBuilder()
        val cx = x + width / 2
        val highlightWidth = width * 0.3
        val highlightX = x + width * 0.15

        sb.append("<g filter=\"url(#cylinderShadow_$id)\">\n")

        // Cylinder body (main rectangle with gradient)
        // Animate both y and height for upward growth effect
        sb.append("""<rect x="$x" y="$y" width="$width" height="$height" fill="url(#${gradientId})" rx="2">""")
        sb.append("""<animate attributeName="y" from="$yAxisBottom" to="$y" dur="1s" fill="freeze"/>""")
        sb.append("""<animate attributeName="height" from="0" to="$height" dur="1s" fill="freeze"/>""")
        sb.append("""</rect>""")
        sb.append("\n")

        // Highlight stripe for glass effect (also animate)
        sb.append("""<rect x="$highlightX" y="${y + 5}" width="$highlightWidth" height="${height - 10}" fill="url(#highlight)" opacity="0.7">""")
        sb.append("""<animate attributeName="y" from="${yAxisBottom + 5}" to="${y + 5}" dur="1s" fill="freeze"/>""")
        sb.append("""<animate attributeName="height" from="0" to="${height - 10}" dur="1s" fill="freeze"/>""")
        sb.append("""</rect>""")
        sb.append("\n")

        // Bottom ellipse (base of cylinder) - stays at bottom, no animation needed
        val (_, _, darkColor) = generateColorShades(color)
        sb.append("""<ellipse cx="$cx" cy="$yAxisBottom" rx="$cylinderRadius" ry="$ellipseRy" fill="$darkColor" opacity="0.8"/>""")
        sb.append("\n")

        // Top ellipse (visible top of cylinder) - animate y position
        val (lightColor, _, _) = generateColorShades(color)
        sb.append("""<ellipse cx="$cx" cy="$y" rx="$cylinderRadius" ry="$ellipseRy" fill="$lightColor" opacity="0.9">""")
        sb.append("""<animate attributeName="cy" from="$yAxisBottom" to="$y" dur="1s" fill="freeze"/>""")
        sb.append("""</ellipse>""")
        sb.append("\n")

        // Top ellipse highlight overlay - also animate
        sb.append("""<ellipse cx="$cx" cy="${y - 2}" rx="$cylinderRadius" ry="$ellipseRy" fill="$lightColor" opacity="0.5">""")
        sb.append("""<animate attributeName="cy" from="${yAxisBottom - 2}" to="${y - 2}" dur="1s" fill="freeze"/>""")
        sb.append("""</ellipse>""")
        sb.append("\n")

        // Rim highlight on top ellipse - also animate
        sb.append("""<ellipse cx="$cx" cy="$y" rx="${cylinderRadius * 0.85}" ry="${ellipseRy * 0.8}" fill="none" stroke="#ffffff" stroke-width="2" opacity="0.4">""")
        sb.append("""<animate attributeName="cy" from="$yAxisBottom" to="$y" dur="1s" fill="freeze"/>""")
        sb.append("""</ellipse>""")
        sb.append("\n")

        sb.append("</g>\n")

        // Label below the bar
        sb.append("""<text x="$cx" y="${yAxisBottom + 20}" font-family="Arial, sans-serif" text-anchor="middle" font-size="12" font-weight="bold" fill="$textColor">$label</text>""")
        sb.append("\n")

        // Value above the bar
        val valueColor = if (useDark) "#ffffff" else "#111111"
        sb.append("""<text x="$cx" y="${y - 20}" font-family="Arial, sans-serif" text-anchor="middle" font-size="14" font-weight="bold" fill="$valueColor" opacity="0">${value}""")
        sb.append("""<animate attributeName="opacity" from="0" to="1" begin="0.8s" dur="0.5s" fill="freeze"/>""")
        sb.append("""</text>""")
        sb.append("\n")
        return sb.toString()
    }
}
