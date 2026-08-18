package io.docops.docopsextensionssupport.chart.gauge

import io.docops.docopsextensionssupport.svgsupport.escapeXml

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
        val barY = 120.0
        val barWidth = 300.0
        val barHeight = 32.0

        val sb = StringBuilder()

        // Accessibility
        val titleId = "title_$id"
        val descId = "desc_$id"
        sb.append("<title id=\"$titleId\">${gaugeChart.title.ifEmpty { "Linear Gauge" }.escapeXml()}</title>\n")
        sb.append("<desc id=\"$descId\">Linear gauge showing ${gauge.label}: ${gauge.value} ${gauge.unit}</desc>\n")

        // Background ranges
        val totalRange = gauge.max - gauge.min
        if (gaugeChart.display.showRanges) {
            val normalWidth = ((ranges.normalEnd - gauge.min) / totalRange) * barWidth
            val cautionWidth = ((ranges.cautionEnd - ranges.normalEnd) / totalRange) * barWidth
            val criticalWidth = ((ranges.criticalEnd - ranges.cautionEnd) / totalRange) * barWidth

            sb.append("""
                <rect x="$barX" y="$barY" width="$normalWidth" height="$barHeight" rx="${theme.cornerRadius}" fill="${theme.success}" opacity="0.2"/>
                <rect x="${barX + normalWidth}" y="$barY" width="$cautionWidth" height="$barHeight" rx="${theme.cornerRadius}" fill="${theme.warning}" opacity="0.2"/>
                <rect x="${barX + normalWidth + cautionWidth}" y="$barY" width="$criticalWidth" height="$barHeight" rx="${theme.cornerRadius}" fill="${theme.danger}" opacity="0.2"/>
            """.trimIndent())
        }

        // Performance bar with animation
        val percent = ((gauge.value - gauge.min) / totalRange) * 100
        val fillWidth = (percent / 100) * barWidth
        val color = getGradientForValue(gauge.value, id, gaugeChart.display.showRanges)

        sb.append("""
            <rect x="$barX" y="${barY + 4}" ${if (!gaugeChart.display.animateArc) "width=\"$fillWidth\"" else "width=\"0\""} height="${barHeight - 8}" 
                  rx="${theme.cornerRadius}" 
                  fill="$color"
                  ${if (gaugeChart.display.animateArc) "class=\"animated-fill\" style=\"--fill-width: ${fillWidth}px;\"" else ""}
                  filter="url(#glow_$id)"/>
        """.trimIndent())

        // Target marker
        if (gaugeChart.display.showTarget && gauge.target != null) {
            val targetX = barX + ((gauge.target - gauge.min) / totalRange) * barWidth
            sb.append("""
                <rect x="$targetX" y="${barY - 4}" width="4" height="${barHeight + 8}" rx="2" fill="${theme.primaryText}" opacity="0.6"/>
                <text x="${targetX + 4}" y="${barY - 8}" class="gauge-label">TARGET</text>
            """.trimIndent())
        }

        // Range labels
        sb.append("""
            <text x="$barX" y="${barY + barHeight + 24}" class="range-label" fill="${getColorForValue(gauge.min, null, gaugeChart.display.showRanges)}">${formatNumber(gauge.min)}</text>
            <text x="${barX + barWidth/2}" y="${barY + barHeight + 24}" text-anchor="middle" class="range-label" fill="${theme.secondaryText}">${formatNumber((gauge.max + gauge.min)/2.0)}</text>
            <text x="${barX + barWidth}" y="${barY + barHeight + 24}" text-anchor="end" class="range-label" fill="${getColorForValue(gauge.max, null, gaugeChart.display.showRanges)}">${formatNumber(gauge.max)}</text>
        """.trimIndent())

        // Value
        sb.append("""
            <text x="${width/2}" y="${barY + barHeight + 56}" 
                  text-anchor="middle" 
                  class="gauge-value-medium ${if (gaugeChart.display.animateArc) "animated-digit" else ""}"
                  fill="${getColorForValue(gauge.value, gauge.color, gaugeChart.display.showRanges)}">
                ${formatNumber(gauge.value)}
            </text>
            <text x="${width/2}" y="${barY + barHeight + 80}" 
                  text-anchor="middle" 
                  class="gauge-label">
                ${gauge.label} ${gauge.unit}
            </text>
        """.trimIndent())

        return sb.toString()
    }
}
