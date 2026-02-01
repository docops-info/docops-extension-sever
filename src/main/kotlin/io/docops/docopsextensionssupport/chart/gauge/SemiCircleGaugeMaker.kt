package io.docops.docopsextensionssupport.chart.gauge

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Creates classic semi-circle (speedometer style) gauge.
 */
class SemiCircleGaugeMaker() : AbstractGaugeMaker() {

    override fun calculateDimensions(gaugeChart: GaugeChart) {
        width = 400.0
        height = 320.0
    }

    override fun createGaugeContent(gaugeChart: GaugeChart): String {
        if (gaugeChart.gauges.isEmpty()) return ""

        val gauge = gaugeChart.gauges[0]
        val id = gaugeChart.display.id
        val centerX = width / 2
        val centerY = 250.0
        val radius = 100.0

        val sb = StringBuilder()

        // Track (background semi-circle from left to right)
        sb.append("""
            <path d="M ${centerX - radius},$centerY A $radius,$radius 0 0,1 ${centerX + radius},$centerY" 
                  fill="none" 
                  stroke="#1e293b" 
                  stroke-width="20"
                  stroke-linecap="round"/>
        """.trimIndent())

        // Colored segments if ranges enabled
        if (gaugeChart.display.showRanges) {
            // Calculate angle ranges (0 = left, 180 = right)
            val normalAngle = (ranges.normalEnd / gauge.max) * 180.0
            val cautionAngle = (ranges.cautionEnd / gauge.max) * 180.0

            sb.append(createSegment(centerX, centerY, radius, 0.0, normalAngle, "url(#successGrad_$id)", 0.4))
            sb.append(createSegment(centerX, centerY, radius, normalAngle, cautionAngle, "url(#warningGrad_$id)", 0.4))
            sb.append(createSegment(centerX, centerY, radius, cautionAngle, 180.0, "url(#criticalGrad_$id)", 0.4))
        }

        // Progress arc
        val percent = ((gauge.value - gauge.min) / (gauge.max - gauge.min)) * 100.0
        val progressAngle = (percent / 100.0) * 180.0
        val arcPath = createArcPath(centerX, centerY, radius, 0.0, progressAngle)

// Calculate arc length correctly for animation
        val totalSemiCircleLength = PI * radius  // Total semi-circle arc length
        val progressArcLength = totalSemiCircleLength * (progressAngle / 180.0)  // Actual progress length

        val color = getGradientForValue(gauge.value, id, gaugeChart.display.showRanges)

        sb.append("""
        <path d="$arcPath" 
              fill="none" 
              stroke="$color" 
              stroke-width="20"
              stroke-linecap="round"
              class="${if (gaugeChart.display.animateArc) "animated-arc" else ""}"
              ${if (gaugeChart.display.animateArc) "style=\"--arc-length: ${progressArcLength.toInt()}; --arc-offset: 0;\"" else ""}
              filter="url(#glow_$id)"/>
        """.trimIndent())

        // Center hub
        sb.append("""
            <circle cx="$centerX" cy="$centerY" r="12" fill="#0a0e27"/>
            <circle cx="$centerX" cy="$centerY" r="8" fill="${getColorForValue(gauge.value, gauge.color, gaugeChart.display.showRanges)}"/>
        """.trimIndent())

        // Value display
        sb.append("""
    <text x="$centerX" y="${centerY + 15}" 
          text-anchor="middle" 
          class="gauge-value-large ${if (gaugeChart.display.animateArc) "animated-digit" else ""}"
          fill="${getColorForValue(gauge.value, gauge.color, gaugeChart.display.showRanges)}">
        ${formatNumber(gauge.value)}
    </text>
    <text x="$centerX" y="${centerY + 40}" 
          text-anchor="middle" 
          class="gauge-label">
        ${gauge.label} ${gauge.unit}
    </text>
""".trimIndent())

        // Range labels
        if (gaugeChart.display.showRanges) {
            sb.append("""
                <text x="${centerX - radius}" y="${centerY + 20}" 
                      class="range-label" 
                      fill="${getColorForValue(gauge.min, null, true)}">
                    ${formatNumber(gauge.min)}
                </text>
                <text x="$centerX" y="${centerY - radius + 5}" 
                      text-anchor="middle" 
                      class="range-label" 
                      fill="#94a3b8">
                    ${formatNumber((gauge.max + gauge.min) / 2.0)}
                </text>
                <text x="${centerX + radius}" y="${centerY + 20}" 
                      text-anchor="end" 
                      class="range-label" 
                      fill="${getColorForValue(gauge.max, null, true)}">
                    ${formatNumber(gauge.max)}
                </text>
            """.trimIndent())
        }

        return sb.toString()
    }

    private fun createSegment(cx: Double, cy: Double, r: Double, startAngle: Double, endAngle: Double, color: String, opacity: Double): String {
        val path = createArcPath(cx, cy, r, startAngle, endAngle)
        return """
            <path d="$path" 
                  fill="none" 
                  stroke="$color" 
                  stroke-width="18"
                  stroke-linecap="round"
                  opacity="$opacity"/>
        """.trimIndent()
    }

    /**
     * Creates an SVG arc path for a bottom semi-circle gauge (speedometer style).
     * The semi-circle spans from left (180° in polar coords) to right (0° in polar coords).
     *
     * @param cx Center X coordinate
     * @param cy Center Y coordinate (top of the semi-circle)
     * @param r Radius
     * @param startAngle Start angle (0 = left side, 180 = right side)
     * @param endAngle End angle (0 = left side, 180 = right side)
     */
    private fun createArcPath(cx: Double, cy: Double, r: Double, startAngle: Double, endAngle: Double): String {
        // For a bottom semi-circle (speedometer):
        // - Left edge is at 180° in standard polar coordinates
        // - Right edge is at 0° in standard polar coordinates
        // - We want 0° input to map to left edge, 180° input to map to right edge

        // Map our 0-180 range to polar coordinates (180-0)
        val startPolar = 180.0 - startAngle
        val endPolar = 180.0 - endAngle

        // Convert to radians
        val startRad = startPolar * PI / 180.0
        val endRad = endPolar * PI / 180.0

        // Calculate start point
        val x1 = cx + r * cos(startRad)
        val y1 = cy - r * sin(startRad)

        // Calculate end point
        val x2 = cx + r * cos(endRad)
        val y2 = cy - r * sin(endRad)

        // Determine if we need the large arc flag
        // We're always drawing less than 180° arcs for a semi-circle gauge
        val largeArc = if ((endAngle - startAngle) > 180.0) 1 else 0

        // Sweep flag: 1 for clockwise (left to right on bottom semi-circle)
        val sweepFlag = 1

        return "M $x1,$y1 A $r,$r 0 $largeArc $sweepFlag $x2,$y2"
    }
}