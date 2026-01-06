package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.web.CsvResponse
import kotlin.math.*

class CyberBrutalistBarGroupMaker(val useDark: Boolean) {

    private val titleFont = "Syne, sans-serif"
    private val monoFont = "JetBrains Mono, monospace"
    private val bgColorStart = "#1e1b4b"
    private val bgColorEnd = "#0f172a"
    private val textColor = "#f8fafc"
    private val subTextColor = "#94a3b8"
    private val accentColor = "#818cf8"
    private val secondaryColor = "#fbbf24"

    private fun getColors(): Map<String, String> {
        return if (useDark) {
            mapOf(
                "bgStart" to "#1e1b4b",
                "bgEnd" to "#0f172a",
                "text" to "#f8fafc",
                "subText" to "#94a3b8",
                "accent" to "#818cf8"
            )
        } else {
            mapOf(
                "bgStart" to "#f8fafc",
                "bgEnd" to "#e2e8f0",
                "text" to "#0f172a",
                "subText" to "#475569",
                "accent" to "#4f46e5"
            )
        }
    }
    fun makeBar(barGroup: BarGroup): Pair<String, CsvResponse> {
        val colors = getColors()
        val sb = StringBuilder()
        sb.append(makeHead(barGroup))
        sb.append(makeDefs(barGroup, colors))

        // Background - reduced height to 650
        sb.append("""<rect id="bg_${barGroup.id}" x="0" y="0" width="${barGroup.calcWidth()}" height="650" fill="url(#group_bg)" rx="16" />""")

        sb.append(makeTitle(barGroup, colors))
        sb.append(makeXLabel(barGroup, colors))
        sb.append(makeYLabel(barGroup, colors))
        
        // Grid lines
        sb.append(addGrid(barGroup, colors))
        
        var startX = 110.0
        val elements = StringBuilder()
        barGroup.groups.forEachIndexed { index, group ->
            val added = addGroup(barGroup, group, startX, index, colors)
            startX += group.series.size * 50.0 + 30.0 // Adjusted for plate padding
            elements.append(added)
        }

        sb.append("<g transform='translate(${(barGroup.calcWidth() - startX) / 2},0)'>")
        sb.append(elements.toString())
        sb.append("</g>")
        
        sb.append(addTicks(barGroup, colors))
        sb.append(addLegend(barGroup, colors))
        sb.append("</svg>")
        
        return Pair(sb.toString(), barGroup.toCsv())
    }

    private fun makeHead(barGroup: BarGroup): String {
        return """<svg xmlns="http://www.w3.org/2000/svg" width="${barGroup.calcWidth()}" height="650" viewBox="0 0 ${barGroup.calcWidth()} 650">
            <style>
                .title-text { font-family: $titleFont; font-weight: 800; text-transform: uppercase; }
                .mono-text { font-family: $monoFont; }
                .group-plate { opacity: 0; animation: fadeIn 0.8s ease-out forwards; }
                .bar-anim { transform-origin: bottom; animation: growBar 1s cubic-bezier(0.34, 1.56, 0.64, 1) forwards; }
                @keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
                @keyframes growBar { from { transform: scaleY(0); } to { transform: scaleY(1); } }
            </style>
        """.trimIndent()
    }

    private fun makeDefs(barGroup: BarGroup, colors: Map<String, String>): String {
        val sb = StringBuilder()
        val plateOpacity = if (useDark) "0.05" else "0.15"
        val plateColor = if (useDark) "#ffffff" else "#000000"

        sb.append("<defs>")
        sb.append("""
                <radialGradient id="group_bg" cx="50%" cy="50%" r="50%" fx="50%" fy="50%">
                    <stop offset="0%" style="stop-color:${colors["bgStart"]};stop-opacity:1" />
                    <stop offset="100%" style="stop-color:${colors["bgEnd"]};stop-opacity:1" />
                </radialGradient>
                <linearGradient id="plate_grad" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:$plateColor;stop-opacity:$plateOpacity" />
                    <stop offset="100%" style="stop-color:$plateColor;stop-opacity:0.01" />
                </linearGradient>
                <filter id="glow">
                <feGaussianBlur stdDeviation="2" result="blur" />
                <feComposite in="SourceGraphic" in2="blur" operator="over" />
            </filter>
        """)
        
        // Group gradients based on ChartColors
        barGroup.legendLabel().distinct().forEachIndexed { index, _ ->
            val color = ChartColors.getColorForIndex(index).color
            sb.append("""
                <linearGradient id="brut_grad_$index" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:$color;" />
                    <stop offset="100%" style="stop-color:${darkenColor(color, 0.3)};" />
                </linearGradient>
            """)
        }
        sb.append("</defs>")
        return sb.toString()
    }

    private fun makeTitle(barGroup: BarGroup, colors: Map<String, String>): String {
        return """
                <text x="40" y="60" class="title-text" font-size="24" fill="${colors["text"]}">${barGroup.title}</text>
                <text x="40" y="90" class="mono-text" font-size="12" fill="${colors["subText"]}" opacity="0.8">Comparative Resource Metrics // v2.4</text>
            """.trimIndent()
    }

    private fun makeXLabel(barGroup: BarGroup, colors: Map<String, String>): String {
        val x = barGroup.calcWidth() / 2
        val y = 630 // Shifted up from 780
        return """<text x="$x" y="$y" class="mono-text" font-size="14" text-anchor="middle" fill="${colors["subText"]}">${barGroup.xLabel ?: ""}</text>"""
    }

    private fun makeYLabel(barGroup: BarGroup, colors: Map<String, String>): String {
        val x = 20
        val y = 325 // Shifted up from 415 to stay centered on shorter Y axis
        return """<text x="$x" y="$y" class="mono-text" font-size="14" text-anchor="middle" fill="${colors["subText"]}" transform="rotate(-90, $x, $y)">${barGroup.yLabel ?: ""}</text>"""
    }

    private fun addGrid(barGroup: BarGroup, colors: Map<String, String>): String {
        val sb = StringBuilder()
        val ticks = barGroup.ticks()
        var current = ticks.getNiceMin()
        while (current <= ticks.getNiceMax()) {
            val y = 500 - barGroup.scaleUp(current) // Baseline changed from 650 to 500
            sb.append("""<line x1="80" y1="$y" x2="${barGroup.calcWidth() - 40}" y2="$y" stroke="${colors["subText"]}" stroke-width="0.5" stroke-dasharray="4,4" stroke-opacity="0.2" />""")
            current += ticks.getTickSpacing()
        }
        return sb.toString()
    }

    private fun addGroup(barGroup: BarGroup, added: Group, startX: Double, groupIndex: Int, colors: Map<String, String>): String {
        val sb = StringBuilder()
        val groupWidth = added.series.size * 50.0 + 20.0
        val plateHeight = 320.0
        val plateY = 180.0

        sb.append("""<g class="group-plate" style="animation-delay: ${0.1 * groupIndex}s">""")
        // Glass plate - now ends at the baseline
        sb.append("""<rect x="$startX" y="$plateY" width="$groupWidth" height="$plateHeight" fill="url(#plate_grad)" rx="8" />""")

        // Group label rotated - repositioned y to stay within the shortened plate
        sb.append("""<text x="${startX + 15}" y="${plateY + plateHeight - 20}" class="title-text" font-size="14" font-weight="700" fill="${colors["accent"]}" transform="rotate(-90, ${startX + 15}, ${plateY + plateHeight - 20})">${added.label}</text>""")

        var counter = startX + 40.0
        added.series.forEachIndexed { index, series ->
            val per = barGroup.scaleUp(series.value)
            val barX = counter
            val barY = 500 - per // Baseline changed from 650 to 500
            val barWidth = 30.0
            val barHeight = per
            val color = "url(#brut_grad_$index)"

            sb.append("""
                    <g class="bar-hover">
                        <rect class="bar-anim" x="$barX" y="$barY" width="$barWidth" height="$barHeight" fill="$color" rx="4" filter="url(#glow)" style="animation-delay: ${0.4 + 0.1 * index}s" />
                        <text x="${barX + barWidth / 2}" y="${barY - 10}" class="mono-text" font-size="10" text-anchor="middle" fill="${colors["text"]}">${barGroup.valueFmt(series.value)}</text>
                    </g>
                """.trimIndent())
            counter += 50.0
        }
        sb.append("</g>")
        return sb.toString()
    }

    private fun addTicks(barGroup: BarGroup, colors: Map<String, String>): String {
        val sb = StringBuilder()
        val ticks = barGroup.ticks()
        var current = ticks.getNiceMin()
        sb.append("""<g class="mono-text" font-size="10" fill="${colors["subText"]}">""")
        while (current <= ticks.getNiceMax()) {
            val y = 500 - barGroup.scaleUp(current) // Baseline changed from 650 to 500
            sb.append("""<text x="75" y="${y + 4}" text-anchor="end">${barGroup.valueFmt(current)}</text>""")
            current += ticks.getTickSpacing()
        }
        sb.append("</g>")
        return sb.toString()
    }

    private fun addLegend(group: BarGroup, colors: Map<String, String>): String {
        val sb = StringBuilder()
        val distinctLabels = group.legendLabel().distinct()
        if (distinctLabels.isEmpty()) return ""

        val itemWidth = 110.0
        val chartWidth = group.calcWidth()
        val maxAvailableWidth = chartWidth - 100.0
        
        // Calculate how many items can fit in one row
        var itemsPerRow = floor(maxAvailableWidth / itemWidth).toInt()
        if (itemsPerRow <= 0) itemsPerRow = 1
        
        val rows = ceil(distinctLabels.size.toDouble() / itemsPerRow).toInt()
        
        // Calculate actual legend width based on items or available space
        val actualItemsInFirstRow = min(distinctLabels.size, itemsPerRow)
        val legendWidth = actualItemsInFirstRow * itemWidth

        val legendX = (chartWidth - legendWidth) / 2.0
        val legendY = 560.0 // Shifted up from 725
        
        sb.append("""<g transform="translate($legendX, $legendY)">""")
        
        // Legend background plate (glassmorphism style)
        sb.append("""<rect x="-15" y="-12" width="${legendWidth + 20}" height="${rows * 25 + 10}" fill="url(#plate_grad)" rx="12" stroke="${colors["subText"]}" stroke-opacity="0.1" />""")

        distinctLabels.forEachIndexed { index, label ->
            val row = index / itemsPerRow
            val col = index % itemsPerRow
            val x = col * itemWidth
            val y = row * 25.0
            val color = "url(#brut_grad_$index)"

            sb.append("""
                    <g class="legend-item" transform="translate($x, $y)">
                        <rect width="14" height="14" fill="$color" rx="3" filter="url(#glow)" />
                        <text x="22" y="11" class="mono-text" font-size="11" fill="${colors["text"]}" style="font-weight: 500;">$label</text>
                    </g>
                """.trimIndent())
        }
        sb.append("</g>")
        return sb.toString()
    }

    private fun darkenColor(hexColor: String, factor: Double): String {
        val color = hexColor.removePrefix("#")
        val r = (color.substring(0, 2).toInt(16) * (1 - factor)).toInt()
        val g = (color.substring(2, 4).toInt(16) * (1 - factor)).toInt()
        val b = (color.substring(4, 6).toInt(16) * (1 - factor)).toInt()
        return "#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}"
    }
}
