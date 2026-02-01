package io.docops.docopsextensionssupport.chart.gauge

/**
 * Creates dashboard with mixed gauge types in a grid layout.
 */
class DashboardGaugeMaker : AbstractGaugeMaker() {

    override fun calculateDimensions(gaugeChart: GaugeChart) {
        val (cols, rows) = parseLayout(gaugeChart.display.layout)
        width = (cols * 400).toDouble()
        height = (rows * 320 + 60).toDouble()
    }

    override fun createGaugeContent(gaugeChart: GaugeChart): String {
        val sb = StringBuilder()
        val (cols, rows) = parseLayout(gaugeChart.display.layout)

        gaugeChart.gauges.forEachIndexed { index, gaugeData ->
            val row = index / cols
            val col = index % cols

            val x = col * 400
            val y = row * 320 + 60

            // Create mini gauge chart for this cell
            val miniChart = GaugeChart(
                type = gaugeData.type ?: GaugeType.SEMI_CIRCLE,
                title = "",
                gauges = listOf(gaugeData),
                display = gaugeChart.display.copy(animateArc = false)
            )

            val maker = GaugeMakerFactory.createMaker(miniChart)
            val miniSvg = maker.makeGauge(miniChart)

            // Extract content without XML declaration and outer SVG wrapper
            val content = extractSvgInnerContent(miniSvg)

            sb.append("""<g transform="translate($x, $y)">""")
            sb.append(content)
            sb.append("</g>")
        }

        return sb.toString()
    }

    private fun parseLayout(layout: String): Pair<Int, Int> {
        val parts = layout.split("x")
        return if (parts.size == 2) {
            Pair(parts[0].toIntOrNull() ?: 2, parts[1].toIntOrNull() ?: 2)
        } else {
            Pair(2, 2)
        }
    }

    /**
     * Extracts the inner content of an SVG, removing:
     * - XML declaration (<?xml...?>)
     * - Opening <svg> tag
     * - Closing </svg> tag
     *
     * This allows the content to be embedded within the parent SVG.
     */
    private fun extractSvgInnerContent(svg: String): String {
        var content = svg

        // Remove XML declaration if present
        val xmlDeclEnd = content.indexOf("?>")
        if (xmlDeclEnd != -1) {
            content = content.substring(xmlDeclEnd + 2).trim()
        }

        // Find the end of the opening <svg> tag
        val svgTagEnd = content.indexOf(">")
        if (svgTagEnd == -1) return ""

        // Find the closing </svg> tag
        val svgCloseStart = content.lastIndexOf("</svg>")
        if (svgCloseStart == -1) return ""

        // Extract everything between the opening and closing svg tags
        return content.substring(svgTagEnd + 1, svgCloseStart).trim()
    }
}