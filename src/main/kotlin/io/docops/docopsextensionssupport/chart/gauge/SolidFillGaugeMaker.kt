package io.docops.docopsextensionssupport.chart.gauge

import io.docops.docopsextensionssupport.svgsupport.escapeXml
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Creates solid filled donut gauge.
 */
class SolidFillGaugeMaker : AbstractGaugeMaker() {

    override fun calculateDimensions(gaugeChart: GaugeChart) {
        width = 400.0
        height = 320.0
    }

    override fun createGaugeContent(gaugeChart: GaugeChart): String {
        if (gaugeChart.gauges.isEmpty()) return ""

        val gauge = gaugeChart.gauges[0]
        val id = gaugeChart.display.id
        val centerX = width / 2
        val centerY = 200.0
        val outerRadius = 80.0
        val innerRadius = gaugeChart.display.innerRadius.toDouble()

        val sb = StringBuilder()

        // Accessibility
        val titleId = "title_$id"
        val descId = "desc_$id"
        sb.append("<title id=\"$titleId\">${gaugeChart.title.ifEmpty { "Gauge Chart" }.escapeXml()}</title>\n")
        sb.append("<desc id=\"$descId\">Gauge showing ${gauge.label}: ${gauge.value} ${gauge.unit}</desc>\n")

        // Calculate percentage
        val percent = ((gauge.value - gauge.min) / (gauge.max - gauge.min)) * 100
        val angle = (percent / 100) * 360
        val angleRad = angle * PI / 180

        val x = centerX + outerRadius * sin(angleRad)
        val y = centerY - outerRadius * cos(angleRad)
        val largeArc = if (angle > 180) 1 else 0

        val color = getGradientForValue(gauge.value, id, gaugeChart.display.showRanges)
        val baseColor = getColorForValue(gauge.value, gauge.color, gaugeChart.display.showRanges)

        // Background circle (subtle track)
        sb.append("""<circle cx="$centerX" cy="$centerY" r="$outerRadius" fill="${theme.surfaceLow}" opacity="0.4"/>""")

        // Inner filled circle with semi-transparent color (the shaded percentage area)
        sb.append("""
        <path d="M $centerX,$centerY L $centerX,${centerY - outerRadius} A $outerRadius,$outerRadius 0 $largeArc 1 $x,$y Z"
              fill="$baseColor"
              opacity="0.2"/>
    """.trimIndent())

        // Outer ring track (full circle, subtle)
        sb.append("""
        <circle cx="$centerX" cy="$centerY" r="$outerRadius" 
                fill="none" 
                stroke="${theme.surfaceLow}" 
                stroke-width="8"
                opacity="0.8"/>
    """.trimIndent())

        // Colored progress arc (outer ring)
        val arcPath = createOuterArcPath(centerX, centerY, outerRadius, 0.0, angle)
        val totalCircleLength = 2 * PI * outerRadius
        val progressArcLength = totalCircleLength * (percent / 100.0)

        sb.append("""
        <path d="$arcPath" 
              fill="none" 
              stroke="$color" 
              stroke-width="8"
              stroke-linecap="round"
              class="${if (gaugeChart.display.animateArc) "animated-arc" else ""}"
              ${if (gaugeChart.display.animateArc) "style=\"--arc-length: ${progressArcLength.toInt()}; --arc-offset: 0;\"" else ""}
              filter="url(#glow_$id)"/>
    """.trimIndent())

        // Inner cutout to create the donut shape
        sb.append("""<circle cx="$centerX" cy="$centerY" r="$innerRadius" fill="${theme.canvas}"/>""")

        // Value text
        sb.append("""
        <text x="$centerX" y="${centerY + 16}" 
              text-anchor="middle" 
              class="gauge-value-large ${if (gaugeChart.display.animateArc) "animated-digit" else ""}"
              fill="$baseColor">
            ${formatNumber(gauge.value)}
        </text>
        <text x="$centerX" y="${centerY + 40}" 
              text-anchor="middle" 
              class="gauge-label">
            ${gauge.label} ${gauge.unit}
        </text>
    """.trimIndent())

        return sb.toString()
    }

    /**
     * Creates an SVG arc path starting from the top (12 o'clock) and sweeping clockwise.
     */
    private fun createOuterArcPath(cx: Double, cy: Double, r: Double, startAngle: Double, endAngle: Double): String {
        // Start angle in radians (0 = top, 12 o'clock)
        val startRad = startAngle * PI / 180.0
        val endRad = endAngle * PI / 180.0

        // Calculate start point (top of circle)
        val x1 = cx + r * sin(startRad)
        val y1 = cy - r * cos(startRad)

        // Calculate end point
        val x2 = cx + r * sin(endRad)
        val y2 = cy - r * cos(endRad)

        val largeArc = if (endAngle - startAngle > 180) 1 else 0

        return "M $x1,$y1 A $r,$r 0 $largeArc 1 $x2,$y2"
    }
}
