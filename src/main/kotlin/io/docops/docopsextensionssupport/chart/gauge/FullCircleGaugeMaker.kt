package io.docops.docopsextensionssupport.chart.gauge

import kotlin.math.PI

/**
 * Creates full 360-degree circular gauge.
 */
class FullCircleGaugeMaker : AbstractGaugeMaker() {
    
    override fun calculateDimensions(gaugeChart: GaugeChart) {
        width = 400.0
        height = 300.0
    }
    
    override fun createGaugeContent(gaugeChart: GaugeChart): String {
        if (gaugeChart.gauges.isEmpty()) return ""
        
        val gauge = gaugeChart.gauges[0]
        val id = gaugeChart.display.id
        val centerX = width / 2
        val centerY = height / 2 + 10
        val radius = 70.0
        
        val sb = StringBuilder()
        
        // Track
        sb.append("""
            <circle cx="$centerX" cy="$centerY" r="$radius" 
                    fill="none" 
                    stroke="#1e293b" 
                    stroke-width="16"/>
        """.trimIndent())
        
        // Progress
        val percent = ((gauge.value - gauge.min) / (gauge.max - gauge.min)) * 100
        val circumference = 2 * PI * radius
        val offset = circumference * (100 - percent) / 100
        
        val color = getGradientForValue(gauge.value, id, gaugeChart.display.showRanges)
        
        sb.append("""
            <circle cx="$centerX" cy="$centerY" r="$radius" 
                    fill="none" 
                    stroke="$color" 
                    stroke-width="18"
                    stroke-linecap="round"
                    stroke-dasharray="$circumference"
                    stroke-dashoffset="$offset"
                    transform="rotate(-90 $centerX $centerY)"
                    class="${if (gaugeChart.display.animateArc) "animated-arc" else ""}"
                    ${if (gaugeChart.display.animateArc) "style=\"--arc-length: $circumference; --arc-offset: $offset;\"" else ""}
                    filter="url(#glow_$id)"/>
        """.trimIndent())
        
        // Pulse ring
        sb.append("""
            <circle cx="$centerX" cy="$centerY" r="${radius - 2}" 
                    fill="none" 
                    stroke="${getColorForValue(gauge.value, gauge.color, gaugeChart.display.showRanges)}" 
                    stroke-width="1" 
                    opacity="0.3"/>
        """.trimIndent())
        
        // Value
        sb.append("""
            <text x="$centerX" y="${centerY + 10}" 
                  text-anchor="middle" 
                  class="gauge-value-medium ${if (gaugeChart.display.animateArc) "animated-digit" else ""}"
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
