package io.docops.docopsextensionssupport.diagram


import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.math.max
import kotlin.math.min


class MagicQuadrantSvgGenerator {

    private val lightModeColors = QuadrantColors(
        background = listOf("#f0f9ff", "#e0f2fe"),
        leaders = "#10b981",
        challengers = "#f59e0b",
        visionaries = "#3b82f6",
        nichePlayers = "#ef4444",
        gridLines = "#475569",
        text = "#374151",
        lightText = "#e2e8f0"
    )

    private val darkModeColors = QuadrantColors(
        background = listOf("#0f172a", "#1e293b"),
        leaders = "#10b981",
        challengers = "#f59e0b",
        visionaries = "#3b82f6",
        nichePlayers = "#ef4444",
        gridLines = "#475569",
        text = "#e2e8f0",
        lightText = "#94a3b8"
    )

    data class QuadrantColors(
        val background: List<String>,
        val leaders: String,
        val challengers: String,
        val visionaries: String,
        val nichePlayers: String,
        val gridLines: String,
        val text: String,
        val lightText: String
    )

    @OptIn(ExperimentalUuidApi::class)
    fun generateMagicQuadrant(config: MagicQuadrantConfig, isDarkMode: Boolean = false, scale: String = "1.0"): String {
        val svgId = "mq_${Uuid.random().toHexString()}"
        val colors = if (isDarkMode) darkModeColors else lightModeColors
        val scaleFactor = scale.toDoubleOrNull() ?: 1.0

        val baseWidth = 700
        val baseHeight = 700
        val width = (baseWidth * scaleFactor).toInt()
        val height = (baseHeight * scaleFactor).toInt()

        val margin = 50
        val chartWidth = baseWidth - 2 * margin
        val chartHeight = baseHeight - 2 * margin - 100 // Space for title and labels
        val chartStartY = 100

        val sb = StringBuilder()

        // SVG header
        sb.append("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.append("""<svg width="$width" height="$height" viewBox="0 0 $baseWidth $baseHeight" xmlns="http://www.w3.org/2000/svg" id="$svgId">""")


        // Add defs with gradients and filters
        appendDefs(sb, svgId, colors)

        // Background - simplified to avoid BackgroundHelper issues
        sb.append("""<rect width="100%" height="100%" fill="url(#bgGradient_$svgId)" rx="12" ry="12"/>""")

        // Title
        sb.append("""<text x="${baseWidth / 2}" y="40" text-anchor="middle" font-family="Inter, system-ui, sans-serif" font-size="24" font-weight="bold" fill="${colors.text}">${escapeXml(config.title)}</text>""")

        // Quadrant backgrounds with gradients
        val quadrantWidth = chartWidth / 2
        val quadrantHeight = chartHeight / 2
        val centerX = margin + quadrantWidth
        val centerY = chartStartY + quadrantHeight

        appendQuadrantBackground(sb, svgId, margin, chartStartY, quadrantWidth, quadrantHeight, colors)

        // Grid lines
        sb.append("""<line x1="$centerX" y1="$chartStartY" x2="$centerX" y2="${chartStartY + chartHeight}" stroke="${colors.gridLines}" stroke-width="2" opacity="0.8"/>""")
        sb.append("""<line x1="$margin" y1="$centerY" x2="${margin + chartWidth}" y2="$centerY" stroke="${colors.gridLines}" stroke-width="2" opacity="0.8"/>""")

        // Quadrant labels
        appendQuadrantLabels(sb, svgId, margin, chartStartY, quadrantWidth, quadrantHeight, colors, isDarkMode, config)

        // Axis labels
        sb.append("""<text x="${baseWidth / 2}" y="${baseHeight - 20}" text-anchor="middle" font-family="Inter, system-ui, sans-serif" font-size="18" font-weight="bold" fill="${colors.text}">${escapeXml(config.xAxisLabel)}</text>""")
        sb.append("""<text x="25" y="${chartStartY + chartHeight / 2}" text-anchor="middle" font-family="Inter, system-ui, sans-serif" font-size="18" font-weight="bold" fill="${colors.text}" transform="rotate(-90 25 ${chartStartY + chartHeight / 2})">${escapeXml(config.yAxisLabel)}</text>""")

        // Plot companies
        config.companies.forEach { company ->
            plotCompany(sb, svgId, company, margin, chartStartY, chartWidth, chartHeight, colors, isDarkMode)
        }

        sb.append("</svg>")
        return sb.toString()
    }



    private fun appendDefs(sb: StringBuilder, svgId: String, colors: QuadrantColors) {
        sb.append("<defs>")

        // Background gradient
        sb.append("""
            <linearGradient id="bgGradient_$svgId" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:${colors.background[0]};stop-opacity:1" />
                <stop offset="100%" style="stop-color:${colors.background[1]};stop-opacity:1" />
            </linearGradient>
        """.trimIndent())

        // Quadrant gradients
        sb.append("""
            <radialGradient id="leadersGradient_$svgId" cx="75%" cy="25%">
                <stop offset="0%" style="stop-color:${colors.leaders};stop-opacity:0.4" />
                <stop offset="100%" style="stop-color:${colors.leaders};stop-opacity:0.1" />
            </radialGradient>
            <radialGradient id="challengersGradient_$svgId" cx="25%" cy="25%">
                <stop offset="0%" style="stop-color:${colors.challengers};stop-opacity:0.4" />
                <stop offset="100%" style="stop-color:${colors.challengers};stop-opacity:0.1" />
            </radialGradient>
            <radialGradient id="visionariesGradient_$svgId" cx="75%" cy="75%">
                <stop offset="0%" style="stop-color:${colors.visionaries};stop-opacity:0.4" />
                <stop offset="100%" style="stop-color:${colors.visionaries};stop-opacity:0.1" />
            </radialGradient>
            <radialGradient id="nicheGradient_$svgId" cx="25%" cy="75%">
                <stop offset="0%" style="stop-color:${colors.nichePlayers};stop-opacity:0.4" />
                <stop offset="100%" style="stop-color:${colors.nichePlayers};stop-opacity:0.1" />
            </radialGradient>
        """.trimIndent())

        // Company bubble gradients
        listOf("leaders", "challengers", "visionaries", "nichePlayers").forEach { type ->
            val color = when(type) {
                "leaders" -> colors.leaders
                "challengers" -> colors.challengers
                "visionaries" -> colors.visionaries
                else -> colors.nichePlayers
            }
            sb.append("""
                <radialGradient id="bubble${type}_$svgId">
                    <stop offset="0%" style="stop-color:$color;stop-opacity:0.8" />
                    <stop offset="100%" style="stop-color:$color;stop-opacity:1" />
                </radialGradient>
            """.trimIndent())
        }

        // Filters
        sb.append("""
            <filter id="glow_$svgId">
                <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
                <feMerge>
                    <feMergeNode in="coloredBlur"/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>
            <filter id="bubbleShadow_$svgId">
                <feDropShadow dx="2" dy="2" stdDeviation="4" flood-opacity="0.3"/>
            </filter>
            <filter id="labelShadow_$svgId">
                <feDropShadow dx="0" dy="2" stdDeviation="4" flood-opacity="0.2"/>
            </filter>
        """.trimIndent())

        sb.append("</defs>")
    }

    private fun appendQuadrantBackground(
        sb: StringBuilder,
        svgId: String,
        margin: Int,
        chartStartY: Int,
        quadrantWidth: Int,
        quadrantHeight: Int,
        colors: QuadrantColors
    ) {
        // Challengers (top-left)
        sb.append("""<rect x="$margin" y="$chartStartY" width="$quadrantWidth" height="$quadrantHeight" fill="url(#challengersGradient_$svgId)"/>""")

        // Leaders (top-right)
        sb.append("""<rect x="${margin + quadrantWidth}" y="$chartStartY" width="$quadrantWidth" height="$quadrantHeight" fill="url(#leadersGradient_$svgId)"/>""")

        // Niche Players (bottom-left)
        sb.append("""<rect x="$margin" y="${chartStartY + quadrantHeight}" width="$quadrantWidth" height="$quadrantHeight" fill="url(#nicheGradient_$svgId)"/>""")

        // Visionaries (bottom-right)
        sb.append("""<rect x="${margin + quadrantWidth}" y="${chartStartY + quadrantHeight}" width="$quadrantWidth" height="$quadrantHeight" fill="url(#visionariesGradient_$svgId)"/>""")
    }

    private fun appendQuadrantLabels(
        sb: StringBuilder,
        svgId: String,
        margin: Int,
        chartStartY: Int,
        quadrantWidth: Int,
        quadrantHeight: Int,
        colors: QuadrantColors,
        isDarkMode: Boolean, config: MagicQuadrantConfig
    ) {
        val labelBg = if (isDarkMode) "#1e293b" else "#ffffff"
        val labelOpacity = if (isDarkMode) "0.9" else "0.95"

        // Challengers label
        val challengersX = margin + quadrantWidth / 2
        val challengersY = chartStartY + 30
        val challengersText = config.challengersLabel.uppercase()
        val challengersWidth = estimateTextWidth(challengersText, 14) + 20
        sb.append("""<rect x="${challengersX - 75}" y="${challengersY - 15}" width="150" height="30" rx="15" fill="$labelBg" opacity="$labelOpacity" filter="url(#labelShadow_$svgId)"/>""")
        sb.append("""<text x="$challengersX" y="${challengersY + 5}" text-anchor="middle" font-family="Inter, system-ui, sans-serif" font-size="14" font-weight="600" fill="${colors.challengers}">$challengersText</text>""")

        // Leaders label
        val leadersX = margin + quadrantWidth + quadrantWidth / 2
        val leadersY = chartStartY + 30
        val leadersText = config.leadersLabel.uppercase()
        val leadersWidth = estimateTextWidth(leadersText, 14) + 20
        sb.append("""<rect x="${leadersX - 60}" y="${leadersY - 15}" width="120" height="30" rx="15" fill="$labelBg" opacity="$labelOpacity" filter="url(#labelShadow_$svgId)"/>""")
        sb.append("""<text x="$leadersX" y="${leadersY + 5}" text-anchor="middle" font-family="Inter, system-ui, sans-serif" font-size="14" font-weight="600" fill="${colors.leaders}">$leadersText</text>""")

        // Niche Players label
        val nicheX = margin + quadrantWidth / 2
        val nicheY = chartStartY + quadrantHeight + quadrantHeight - 20
        val nicheText = config.nichePlayersLabel.uppercase()
        val nicheWidth = estimateTextWidth(nicheText, 14) + 20
        sb.append("""<rect x="${nicheX - 85}" y="${nicheY - 15}" width="170" height="30" rx="15" fill="$labelBg" opacity="$labelOpacity" filter="url(#labelShadow_$svgId)"/>""")
        sb.append("""<text x="$nicheX" y="${nicheY + 5}" text-anchor="middle" font-family="Inter, system-ui, sans-serif" font-size="14" font-weight="600" fill="${colors.nichePlayers}">$nicheText</text>""")

        // Visionaries label
        val visionariesX = margin + quadrantWidth + quadrantWidth / 2
        val visionariesY = chartStartY + quadrantHeight + quadrantHeight - 20
        val visionariesText = config.visionariesLabel.uppercase()
        val visionariesWidth = estimateTextWidth(visionariesText, 14) + 20
        sb.append("""<rect x="${visionariesX - 75}" y="${visionariesY - 15}" width="150" height="30" rx="15" fill="$labelBg" opacity="$labelOpacity" filter="url(#labelShadow_$svgId)"/>""")
        sb.append("""<text x="$visionariesX" y="${visionariesY + 5}" text-anchor="middle" font-family="Inter, system-ui, sans-serif" font-size="14" font-weight="600" fill="${colors.visionaries}">$visionariesText</text>""")
    }

    // Add helper method to estimate text width for dynamic sizing
    private fun estimateTextWidth(text: String, fontSize: Int): Int {
        // Rough estimation: average character width is about 0.6 * fontSize
        return (text.length * fontSize * 0.6).toInt()
    }

    private fun plotCompany(
        sb: StringBuilder,
        svgId: String,
        company: QuadrantCompany,
        margin: Int,
        chartStartY: Int,
        chartWidth: Int,
        chartHeight: Int,
        colors: QuadrantColors,
        isDarkMode: Boolean
    ) {
        // Convert 0-100 scale to chart coordinates
        val x = margin + (company.x / 100.0 * chartWidth).toInt()
        val y = chartStartY + chartHeight - (company.y / 100.0 * chartHeight).toInt() // Flip Y axis

        // Determine quadrant color and gradient
        val bubbleGradient = getQuadrantGradient(company.x, company.y, svgId)
        val radius = max(8, min(25, company.size))
        var href =""
        if (company.url.isNotEmpty()) {
            href = """onclick="window.open('${escapeXml(company.url)}', '_blank')" style="cursor:pointer""""
        }
        // Wrap in group for potential linking
        sb.append("""<g aria-label="${escapeXml(company.name)}" $href>""")
        // Company bubble with glow effect
        sb.append("""<circle cx="$x" cy="$y" r="$radius" fill="url(#$bubbleGradient)" filter="url(#glow_$svgId)"/>""")
        sb.append("""<circle cx="$x" cy="$y" r="${radius - 4}" fill="#ffffff" opacity="0.9">""")
        sb.append("""<animate attributeName="r" begin="0s" dur="0.5s" values="$radius; ${radius + 4}; $radius" calcMode="linear"/>""")
        // Optional description as title element for tooltip
        if (company.description.isNotEmpty()) {
            sb.append("""<title>${escapeXml(company.description)}</title>""")
        }
        sb.append("""</circle>""")
        // Company name label
        val textY = y + radius + 20
        val textColor = colors.text
        sb.append("""<text x="$x" y="$textY" text-anchor="middle" font-family="Inter, system-ui, sans-serif" font-size="12" font-weight="600" fill="$textColor">${escapeXml(company.name)}</text>""")



        sb.append("""</g>""")
    }

    private fun getQuadrantGradient(x: Double, y: Double, svgId: String): String {
        return when {
            x >= 50 && y >= 50 -> "bubbleleaders_$svgId"
            x < 50 && y >= 50 -> "bubblechallengers_$svgId"
            x >= 50 && y < 50 -> "bubblevisionaries_$svgId"
            else -> "bubblenichePlayers_$svgId"
        }
    }

    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}