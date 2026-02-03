package io.docops.docopsextensionssupport.chart.gauge

import kotlin.collections.get
import kotlin.compareTo
import kotlin.div
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.text.toDouble
import kotlin.times

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

        // Calculate percentage
        val percent = ((gauge.value - gauge.min) / (gauge.max - gauge.min)) * 100
        val angle = (percent / 100) * 360
        val angleRad = angle * PI / 180

        val x = centerX + outerRadius * sin(angleRad)
        val y = centerY - outerRadius * cos(angleRad)
        val largeArc = if (angle > 180) 1 else 0

        val color = getGradientForValue(gauge.value, id, gaugeChart.display.showRanges)
        val baseColor = getColorForValue(gauge.value, gauge.color, gaugeChart.display.showRanges)

        // Background circle (darker in dark mode, lighter in light mode)
        val bgColor = if (gaugeChart.display.useDark) "#1e293b" else "#e2e8f0"
        sb.append("""<circle cx="$centerX" cy="$centerY" r="$outerRadius" fill="$bgColor"/>""")

        // Inner filled circle with semi-transparent color (the shaded percentage area)
        sb.append("""
        <path d="M $centerX,$centerY L $centerX,${centerY - outerRadius} A $outerRadius,$outerRadius 0 $largeArc 1 $x,$y Z"
              fill="$baseColor"
              opacity="0.25"/>
    """.trimIndent())

        // Outer ring track (full circle, subtle)
        sb.append("""
        <circle cx="$centerX" cy="$centerY" r="$outerRadius" 
                fill="none" 
                stroke="${if (gaugeChart.display.useDark) "#334155" else "#cbd5e1"}" 
                stroke-width="8"/>
    """.trimIndent())

        // Colored progress arc (outer ring)
        val arcPath = createOuterArcPath(centerX, centerY, outerRadius, 0.0, angle)
        sb.append("""
        <path d="$arcPath" 
              fill="none" 
              stroke="$color" 
              stroke-width="8"
              stroke-linecap="round"
              filter="url(#glow_$id)"/>
    """.trimIndent())

        // Inner cutout to create the donut shape
        sb.append("""<circle cx="$centerX" cy="$centerY" r="$innerRadius" fill="${theme.canvas}"/>""")

        // Value text
        sb.append("""
        <text x="$centerX" y="${centerY + 10}" 
              text-anchor="middle" 
              class="gauge-value-large ${if (gaugeChart.display.animateArc) "animated-digit" else ""}"
              fill="$baseColor">
            ${formatNumber(gauge.value)}
        </text>
        <text x="$centerX" y="${centerY + 30}" 
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
