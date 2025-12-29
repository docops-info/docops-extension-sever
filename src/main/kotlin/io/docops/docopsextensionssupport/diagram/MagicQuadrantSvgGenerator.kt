package io.docops.docopsextensionssupport.diagram


import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.math.max
import kotlin.math.min


class MagicQuadrantSvgGenerator {

    private interface Theme {
        val background: String
        val textPrimary: String
        val textSecondary: String
        val gridLines: String
        val leaders: String
        val challengers: String
        val visionaries: String
        val nichePlayers: String
    }

    private object DarkTheme : Theme {
        override val background = "#020617"
        override val textPrimary = "#f8fafc"
        override val textSecondary = "#94a3b8"
        override val gridLines = "#1e293b"
        override val leaders = "#10b981"
        override val challengers = "#f59e0b"
        override val visionaries = "#3b82f6"
        override val nichePlayers = "#f43f5e"
    }

    private object LightTheme : Theme {
        override val background = "#f8fafc"
        override val textPrimary = "#0f172a"
        override val textSecondary = "#64748b"
        override val gridLines = "#e2e8f0"
        override val leaders = "#059669"
        override val challengers = "#d97706"
        override val visionaries = "#2563eb"
        override val nichePlayers = "#dc2626"
    }


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
    fun generateMagicQuadrant(config: MagicQuadrantConfig, isDarkMode: Boolean = false, scale: String = "1.0", isPdf: Boolean = false): String {
        val svgId = "mq_${Uuid.random().toHexString()}"
        val theme = if (isDarkMode) DarkTheme else LightTheme
        val scaleFactor = scale.toDoubleOrNull() ?: 1.0

        val baseWidth = 700
        val baseHeight = 700
        val width = (baseWidth * scaleFactor).toInt()
        val height = (baseHeight * scaleFactor).toInt()

        val margin = 60
        val chartWidth = baseWidth - 2 * margin
        val chartHeight = baseHeight - 2 * margin - 100
        val chartStartY = 110

        return buildString {
            append("""<?xml version="1.0" encoding="UTF-8"?>""")
            append("""<svg width="$width" height="$height" viewBox="0 0 $baseWidth $baseHeight" xmlns="http://www.w3.org/2000/svg" id="$svgId">""")

            append(generateStyles(svgId, theme, isPdf))
            append(appendDefs(svgId, theme))

            // Background Layers
            append("""<rect width="100%" height="100%" class="bg-rect" rx="16" ry="16"/>""")
            append("""<rect width="100%" height="100%" fill="url(#gridPattern_$svgId)" rx="16" ry="16" opacity="0.4"/>""")

            // Title
            append("""<text x="${baseWidth / 2}" y="55" text-anchor="middle" class="title-text">${escapeXml(config.title)}</text>""")

            // Quadrant backgrounds
            val qw = chartWidth / 2
            val qh = chartHeight / 2
            val cx = margin + qw
            val cy = chartStartY + qh

            appendQuadrantBackgrounds(svgId, margin, chartStartY, qw, qh)

            // Grid lines
            append("""<line x1="$cx" y1="$chartStartY" x2="$cx" y2="${chartStartY + chartHeight}" class="grid-line" stroke-dasharray="4,4"/>""")
            append("""<line x1="$margin" y1="$cy" x2="${margin + chartWidth}" y2="$cy" class="grid-line" stroke-dasharray="4,4"/>""")

            // Quadrant labels
            appendQuadrantLabels(config, margin, chartStartY, qw, qh, theme)

            // Axis labels
            append("""<text x="${baseWidth / 2}" y="${baseHeight - 15}" text-anchor="middle" class="axis-label">${escapeXml(config.xAxisLabel.uppercase())} —&gt;</text>""")
            append("""<text x="20" y="${chartStartY + chartHeight / 2}" text-anchor="middle" class="axis-label" transform="rotate(-90 20 ${chartStartY + chartHeight / 2})">${escapeXml(config.yAxisLabel.uppercase())} —&gt;</text>""")

            // Plot companies
            config.companies.forEach { company ->
                appendPlotCompany(this, svgId, company, margin, chartStartY, chartWidth, chartHeight, isPdf)
            }

            append("</svg>")
        }
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

    private fun generateStyles(id: String, theme: Theme, isPdf: Boolean): String {
        return """
        <style>
            #$id .bg-rect { fill: ${theme.background}; }
            #$id .title-text { 
                fill: ${theme.textPrimary}; 
                font-family: 'Outfit', 'Segoe UI', sans-serif; 
                font-size: 28px; 
                font-weight: 800; 
                letter-spacing: -0.5px;
            }
            #$id .grid-line { stroke: ${theme.gridLines}; stroke-width: 2; opacity: 0.6; }
            #$id .axis-label { 
                fill: ${theme.textSecondary}; 
                font-family: 'JetBrains Mono', 'Courier New', monospace; 
                font-size: 11px; 
                font-weight: 800; 
                letter-spacing: 2px;
            }
            #$id .quadrant-label { 
                font-family: 'JetBrains Mono', monospace; 
                font-size: 11px; 
                font-weight: bold; 
            }
            #$id .company-name { 
                fill: ${theme.textPrimary}; 
                font-family: 'Outfit', sans-serif; 
                font-size: 12px; 
                font-weight: 600;
            }
            ${if(!isPdf) """
            @keyframes sonar { 
                0% { r: 10; opacity: 0.6; } 
                100% { r: 25; opacity: 0; } 
            }
            .sonar-ring { animation: sonar 2.5s infinite; }
            """ else ""}
        </style>
        """.trimIndent()
    }

    private fun appendDefs(svgId: String, theme: Theme): String {
        return """
        <defs>
            <pattern id="gridPattern_$svgId" width="40" height="40" patternUnits="userSpaceOnUse">
                <path d="M 40 0 L 0 0 0 40" fill="none" stroke="${theme.gridLines}" stroke-width="1" opacity="0.3"/>
            </pattern>
            <radialGradient id="gradLeaders_$svgId" cx="100%" cy="0%"><stop offset="0%" stop-color="${theme.leaders}" stop-opacity="0.15"/><stop offset="100%" stop-opacity="0"/></radialGradient>
            <radialGradient id="gradChallengers_$svgId" cx="0%" cy="0%"><stop offset="0%" stop-color="${theme.challengers}" stop-opacity="0.15"/><stop offset="100%" stop-opacity="0"/></radialGradient>
            <radialGradient id="gradVisionaries_$svgId" cx="100%" cy="100%"><stop offset="0%" stop-color="${theme.visionaries}" stop-opacity="0.15"/><stop offset="100%" stop-opacity="0"/></radialGradient>
            <radialGradient id="gradNiche_$svgId" cx="0%" cy="100%"><stop offset="0%" stop-color="${theme.nichePlayers}" stop-opacity="0.15"/><stop offset="100%" stop-opacity="0"/></radialGradient>
            <filter id="glow_$svgId"><feGaussianBlur stdDeviation="3" result="blur"/><feComposite in="SourceGraphic" in2="blur" operator="over"/></filter>
        </defs>
        """.trimIndent()
    }

    private fun appendPlotCompany(sb: StringBuilder, svgId: String, company: QuadrantCompany, margin: Int, startY: Int, w: Int, h: Int, isPdf: Boolean) {
        val x = margin + (company.x / 100.0 * w).toInt()
        val y = startY + h - (company.y / 100.0 * h).toInt()
        val radius = 10
        val color = getQuadrantColor(company.x, company.y)

        sb.append("""<g class="company-node">""")
        if(!isPdf) {
            sb.append("""<circle cx="$x" cy="$y" r="$radius" class="sonar-ring" fill="none" stroke="$color" stroke-width="1.5"/>""")
        }
        sb.append("""<circle cx="$x" cy="$y" r="$radius" fill="$color" filter="url(#glow_$svgId)"/>""")
        sb.append("""<circle cx="$x" cy="$y" r="3" fill="#ffffff" opacity="0.4"/>""")
        sb.append("""<text x="$x" y="${y + 25}" text-anchor="middle" class="company-name">${escapeXml(company.name)}</text>""")
        sb.append("""</g>""")
    }

    private fun getQuadrantColor(x: Double, y: Double): String {
        return when {
            x >= 50 && y >= 50 -> "#10b981" // Leaders
            x < 50 && y >= 50 -> "#f59e0b"  // Challengers
            x >= 50 && y < 50 -> "#3b82f6"  // Visionaries
            else -> "#f43f5e"               // Niche
        }
    }
    private fun getQuadrantGradient(x: Double, y: Double, svgId: String): String {
        return when {
            x >= 50 && y >= 50 -> "bubbleleaders_$svgId"
            x < 50 && y >= 50 -> "bubblechallengers_$svgId"
            x >= 50 && y < 50 -> "bubblevisionaries_$svgId"
            else -> "bubblenichePlayers_$svgId"
        }
    }

    private fun appendQuadrantBackgrounds(
        svgId: String,
        margin: Int,
        chartStartY: Int,
        qw: Int,
        qh: Int
    ): String {
        return buildString {
            // Challengers (top-left)
            append("""<rect x="$margin" y="$chartStartY" width="$qw" height="$qh" fill="url(#gradChallengers_$svgId)"/>""")
            // Leaders (top-right)
            append("""<rect x="${margin + qw}" y="$chartStartY" width="$qw" height="$qh" fill="url(#gradLeaders_$svgId)"/>""")
            // Niche Players (bottom-left)
            append("""<rect x="$margin" y="${chartStartY + qh}" width="$qw" height="$qh" fill="url(#gradNiche_$svgId)"/>""")
            // Visionaries (bottom-right)
            append("""<rect x="${margin + qw}" y="${chartStartY + qh}" width="$qw" height="$qh" fill="url(#gradVisionaries_$svgId)"/>""")
        }
    }

    private fun StringBuilder.appendQuadrantLabels(
        config: MagicQuadrantConfig,
        margin: Int,
        chartStartY: Int,
        qw: Int,
        qh: Int,
        theme: Theme
    ) {
        val labelYTop = chartStartY + 25
        val labelYBottom = chartStartY + qh + qh - 15

        // Challengers
        append("""<text x="${margin + 10}" y="$labelYTop" fill="${theme.challengers}" class="quadrant-label">${escapeXml(config.challengersLabel.uppercase())}</text>""")
        // Leaders
        append("""<text x="${margin + qw + 10}" y="$labelYTop" fill="${theme.leaders}" class="quadrant-label">${escapeXml(config.leadersLabel.uppercase())}</text>""")
        // Niche
        append("""<text x="${margin + 10}" y="$labelYBottom" fill="${theme.nichePlayers}" class="quadrant-label">${escapeXml(config.nichePlayersLabel.uppercase())}</text>""")
        // Visionaries
        append("""<text x="${margin + qw + 10}" y="$labelYBottom" fill="${theme.visionaries}" class="quadrant-label">${escapeXml(config.visionariesLabel.uppercase())}</text>""")
    }

    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}