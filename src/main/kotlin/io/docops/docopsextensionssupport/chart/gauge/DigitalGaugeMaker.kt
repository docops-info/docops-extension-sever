package io.docops.docopsextensionssupport.chart.gauge

import kotlin.math.PI
import kotlin.math.sin

/**
 * Creates minimalist digital indicator with large number display.
 */
class DigitalGaugeMaker : AbstractGaugeMaker() {
    
    override fun calculateDimensions(gaugeChart: GaugeChart) {
        width = 400.0
        height = 300.0
    }
    
    override fun createGaugeContent(gaugeChart: GaugeChart): String {
        if (gaugeChart.gauges.isEmpty()) return ""
        
        val gauge = gaugeChart.gauges[0]
        val id = gaugeChart.display.id
        val centerX = width / 2
        val centerY = 200.0
        val radius = 80.0
        
        val sb = StringBuilder()
        
        // Minimal arc if enabled
        if (gaugeChart.display.showArc) {
            val percent = ((gauge.value - gauge.min) / (gauge.max - gauge.min)) * 100
            val progressAngle = (percent / 100) * 180
            
            sb.append("""
                <path d="M ${centerX - radius},$centerY A $radius,$radius 0 1,1 ${centerX + radius},$centerY" 
                      fill="none" 
                      stroke="#1e293b" 
                      stroke-width="2"
                      opacity="0.4"/>
            """.trimIndent())
            
            val endX = centerX - radius + 2 * radius * (percent / 100)
            val color = getGradientForValue(gauge.value, id, gaugeChart.display.showRanges)
            
            sb.append("""
                <path d="M ${centerX - radius},$centerY A $radius,$radius 0 0,1 $endX,${centerY - radius * sin((progressAngle * PI / 180))}" 
                      fill="none" 
                      stroke="$color" 
                      stroke-width="3"
                      stroke-linecap="round"
                      opacity="0.6"/>
            """.trimIndent())
        }
        
        // Large digital value
        sb.append("""
            <text x="$centerX" y="${centerY + 10}" 
                  text-anchor="middle" 
                  class="gauge-value-large ${if (gaugeChart.display.animateArc) "animated-digit" else ""}"
                  fill="${getColorForValue(gauge.value, gauge.color, gaugeChart.display.showRanges)}"
                  style="font-size: 80px;">
                ${formatNumber(gauge.value)}
            </text>
            <text x="$centerX" y="${centerY + 35}" 
                  text-anchor="middle" 
                  class="gauge-label">
                ${gauge.unit} ${gauge.label}
            </text>
        """.trimIndent())
        
        // Status indicator
        if (gaugeChart.display.showStatus && gauge.statusText != null) {
            sb.append("""
                <circle cx="${centerX - 80}" cy="${centerY + 70}" r="4" fill="#10B981"/>
                <text x="${centerX - 70}" y="${centerY + 74}" class="gauge-label" style="font-size: 9px;">
                    ${gauge.statusText}
                </text>
            """.trimIndent())
        }
        
        return sb.toString()
    }
}
