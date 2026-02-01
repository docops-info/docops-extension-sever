package io.docops.docopsextensionssupport.chart.gauge

/**
 * Creates horizontal bullet graph style gauge.
 */
class LinearGaugeMaker : AbstractGaugeMaker() {
    
    override fun calculateDimensions(gaugeChart: GaugeChart) {
        width = 400.0
        height = 280.0
    }
    
    override fun createGaugeContent(gaugeChart: GaugeChart): String {
        if (gaugeChart.gauges.isEmpty()) return ""
        
        val gauge = gaugeChart.gauges[0]
        val id = gaugeChart.display.id
        
        val barX = 50.0
        val barY = 150.0
        val barWidth = 300.0
        val barHeight = 32.0
        
        val sb = StringBuilder()
        
        // Background ranges
        if (gaugeChart.display.showRanges) {
            val normalWidth = (ranges.normalEnd / gauge.max) * barWidth
            val cautionWidth = ((ranges.cautionEnd - ranges.normalEnd) / gauge.max) * barWidth
            val criticalWidth = ((ranges.criticalEnd - ranges.cautionEnd) / gauge.max) * barWidth
            
            sb.append("""
                <rect x="$barX" y="$barY" width="$normalWidth" height="$barHeight" rx="4" fill="#059669" opacity="0.2"/>
                <rect x="${barX + normalWidth}" y="$barY" width="$cautionWidth" height="$barHeight" rx="4" fill="#D97706" opacity="0.2"/>
                <rect x="${barX + normalWidth + cautionWidth}" y="$barY" width="$criticalWidth" height="$barHeight" rx="4" fill="#DC2626" opacity="0.2"/>
            """.trimIndent())
        }
        
        // Performance bar
        val percent = ((gauge.value - gauge.min) / (gauge.max - gauge.min)) * 100
        val fillWidth = (percent / 100) * barWidth
        val color = getGradientForValue(gauge.value, id, gaugeChart.display.showRanges)
        
        sb.append("""
            <rect x="$barX" y="${barY + 4}" width="$fillWidth" height="${barHeight - 8}" 
                  rx="4" 
                  fill="$color" 
                  filter="url(#glow_$id)"/>
        """.trimIndent())
        
        // Target marker
        if (gaugeChart.display.showTarget && gauge.target != null) {
            val targetX = barX + (gauge.target!! / gauge.max) * barWidth
            sb.append("""
                <rect x="$targetX" y="${barY - 4}" width="4" height="${barHeight + 8}" fill="#f8fafc" opacity="0.8"/>
                <text x="${targetX + 4}" y="${barY - 10}" class="gauge-label" style="font-size: 9px;">TARGET</text>
            """.trimIndent())
        }
        
        // Range labels
        sb.append("""
            <text x="$barX" y="${barY + barHeight + 25}" class="range-label" fill="${getColorForValue(gauge.min, null, gaugeChart.display.showRanges)}">${formatNumber(gauge.min)}</text>
            <text x="${barX + barWidth/2}" y="${barY + barHeight + 25}" text-anchor="middle" class="range-label" fill="#94a3b8">${formatNumber((gauge.max + gauge.min)/2)}</text>
            <text x="${barX + barWidth}" y="${barY + barHeight + 25}" text-anchor="end" class="range-label" fill="${getColorForValue(gauge.max, null, gaugeChart.display.showRanges)}">${formatNumber(gauge.max)}</text>
        """.trimIndent())
        
        // Value
        sb.append("""
            <text x="${width/2}" y="${barY + barHeight + 60}" 
                  text-anchor="middle" 
                  class="gauge-value-medium ${if (gaugeChart.display.animateArc) "animated-digit" else ""}"
                  fill="${getColorForValue(gauge.value, gauge.color, gaugeChart.display.showRanges)}">
                ${formatNumber(gauge.value)}
            </text>
            <text x="${width/2}" y="${barY + barHeight + 80}" 
                  text-anchor="middle" 
                  class="gauge-label">
                ${gauge.label} (${gauge.unit})
            </text>
        """.trimIndent())
        
        return sb.toString()
    }
}
