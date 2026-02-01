package io.docops.docopsextensionssupport.chart.gauge

import kotlin.math.PI
import kotlin.math.ceil

/**
 * Creates grid of small gauges for comparative monitoring.
 */
class MultiGaugeMaker : AbstractGaugeMaker() {
    
    override fun calculateDimensions(gaugeChart: GaugeChart) {
        val cols = gaugeChart.display.columns
        val rows = ceil(gaugeChart.gauges.size.toDouble() / cols).toInt()
        
        width = (cols * 120 + (cols + 1) * 20).toDouble()
        height = (rows * 120 + (rows + 1) * 20 + 60).toDouble()
    }
    
    override fun createGaugeContent(gaugeChart: GaugeChart): String {
        val sb = StringBuilder()
        val id = gaugeChart.display.id
        val cols = gaugeChart.display.columns
        val gaugeWidth = 120.0
        val gaugeSpacing = 20.0
        
        gaugeChart.gauges.forEachIndexed { index, gauge ->
            val row = index / cols
            val col = index % cols
            
            val x = gaugeSpacing + col * (gaugeWidth + gaugeSpacing) + gaugeWidth / 2
            val y = 80.0 + row * (gaugeWidth + gaugeSpacing) + gaugeWidth / 2
            val radius = 32.0
            
            // Track
            sb.append("""
                <circle cx="$x" cy="$y" r="$radius" 
                        fill="none" 
                        stroke="#1e293b" 
                        stroke-width="6"/>
            """.trimIndent())
            
            // Progress
            val percent = ((gauge.value - gauge.min) / (gauge.max - gauge.min)) * 100
            val circumference = 2 * PI * radius
            val offset = circumference * (100 - percent) / 100
            
            val color = getGradientForValue(gauge.value, id, gaugeChart.display.showRanges)
            
            sb.append("""
                <circle cx="$x" cy="$y" r="$radius" 
                        fill="none" 
                        stroke="$color" 
                        stroke-width="7"
                        stroke-dasharray="$circumference"
                        stroke-dashoffset="$offset"
                        transform="rotate(-90 $x $y)"
                        class="${if (gaugeChart.display.animateArc) "animated-arc" else ""}"
                        ${if (gaugeChart.display.animateArc) "style=\"--arc-length: $circumference; --arc-offset: $offset; animation-delay: ${index * 0.1}s;\"" else ""}/>
            """.trimIndent())
            
            // Value
            sb.append("""
                <text x="$x" y="${y + 8}" 
                      text-anchor="middle" 
                      class="gauge-value-medium ${if (gaugeChart.display.animateArc) "animated-digit" else ""}"
                      fill="${getColorForValue(gauge.value, gauge.color, gaugeChart.display.showRanges)}"
                      style="font-size: 28px; ${if (gaugeChart.display.animateArc) "animation-delay: ${index * 0.1 + 0.6}s;" else ""}">
                    ${formatNumber(gauge.value)}
                </text>
                <text x="$x" y="${y + 55}" 
                      text-anchor="middle" 
                      class="gauge-label" 
                      style="font-size: 8px;">
                    ${gauge.label}
                </text>
            """.trimIndent())
        }
        
        return sb.toString()
    }
}
