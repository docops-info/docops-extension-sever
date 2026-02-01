package io.docops.docopsextensionssupport.chart.gauge

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
        val radius = 80.0
        val innerRadius = gaugeChart.display.innerRadius.toDouble()
        
        val sb = StringBuilder()
        
        // Background
        sb.append("""<circle cx="$centerX" cy="$centerY" r="$radius" fill="#1e293b"/>""")
        
        // Filled portion
        val percent = ((gauge.value - gauge.min) / (gauge.max - gauge.min)) * 100
        val angle = (percent / 100) * 360
        val angleRad = angle * PI / 180
        
        val x = centerX + radius * sin(angleRad)
        val y = centerY - radius * cos(angleRad)
        val largeArc = if (angle > 180) 1 else 0
        
        val color = getGradientForValue(gauge.value, id, gaugeChart.display.showRanges)
        
        sb.append("""
            <path d="M $centerX,${centerY - radius} A $radius,$radius 0 $largeArc 1 $x,$y L $centerX,$centerY Z"
                  fill="$color"
                  filter="url(#glow_$id)"/>
        """.trimIndent())
        
        // Inner cutout
        sb.append("""<circle cx="$centerX" cy="$centerY" r="$innerRadius" fill="${theme.canvas}"/>""")
        
        // Value
        sb.append("""
            <text x="$centerX" y="${centerY + 10}" 
                  text-anchor="middle" 
                  class="gauge-value-large ${if (gaugeChart.display.animateArc) "animated-digit" else ""}"
                  fill="${getColorForValue(gauge.value, gauge.color, gaugeChart.display.showRanges)}">
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
}
