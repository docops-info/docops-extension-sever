package io.docops.docopsextensionssupport.chart.quadrant

import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.ThemeFactory
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class MagicQuadrantSvgGenerator {

    private var theme: DocOpsTheme = ThemeFactory.getTheme(false)

    private data class DefIds(val svgId: String) {
        val bg = "bg_$svgId"
        val washA = "washA_$svgId"
        val washB = "washB_$svgId"
        val grid = "grid_$svgId"

        val qLeaders = "q_leaders_$svgId"
        val qChallengers = "q_challengers_$svgId"
        val qVisionaries = "q_visionaries_$svgId"
        val qNiche = "q_niche_$svgId"

        val bLeaders = "b_leaders_$svgId"
        val bChallengers = "b_challengers_$svgId"
        val bVisionaries = "b_visionaries_$svgId"
        val bNiche = "b_niche_$svgId"

        val boardShadow = "boardShadow_$svgId"
        val nodeGlow = "nodeGlow_$svgId"
    }

    @OptIn(ExperimentalUuidApi::class)
    fun generateMagicQuadrant(
        config: MagicQuadrantConfig,
        scale: String = "1.0",
        isPdf: Boolean = false
    ): String {
        val svgId = "mq_${Uuid.random().toHexString()}"
        val ids = DefIds(svgId)
        theme = ThemeFactory.getTheme(config)

        val scaleFactor = scale.toDoubleOrNull() ?: 1.0
        val baseWidth = 700
        val baseHeight = 700
        val width = (baseWidth * scaleFactor).toInt()
        val height = (baseHeight * scaleFactor).toInt()

        val margin = 60
        val chartWidth = baseWidth - 2 * margin
        val chartHeight = baseHeight - 2 * margin - 100
        val chartStartY = 110

        val qw = chartWidth / 2
        val qh = chartHeight / 2
        val cx = margin + qw
        val cy = chartStartY + qh

        val focusCompany = config.companies.maxByOrNull { it.size }

        return buildString {
            append("""<?xml version="1.0" encoding="UTF-8"?>""")
            append("""<svg width="$width" height="$height" viewBox="0 0 $baseWidth $baseHeight" xmlns="http://www.w3.org/2000/svg" id="$svgId">""")

            append(generateStyles(svgId, config.useDark, isPdf))
            append(generateDefs(ids, config.useDark))

            // Atmosphere
            append("""<rect width="100%" height="100%" fill="url(#${ids.bg})"/>""")
            append("""<rect width="100%" height="100%" fill="url(#${ids.grid})"/>""")
            append("""<rect width="100%" height="100%" fill="url(#${ids.washA})"/>""")
            append("""<rect width="100%" height="100%" fill="url(#${ids.washB})"/>""")

            // Board
            append("""<g transform="translate(40,28)"><g class="reveal d1"><rect x="0" y="0" width="620" height="644" rx="18" class="board"/></g></g>""")

            // Header
            append("""<g transform="translate(40,28)"><g class="reveal d1">""")
            append("""<text x="310" y="40" text-anchor="middle" class="title-text">${escapeXml(config.title)}</text>""")
            append("""<text x="310" y="60" text-anchor="middle" class="sub-text">${if (config.useDark) "Glass Analyst • Dark" else "Glass Analyst • Light"}</text>""")
            append("""</g></g>""")

            // Quadrant chart
            append("""<g transform="translate($margin,$chartStartY)">""")
            append("""<g class="reveal d2">""")
            append("""<rect x="0" y="0" width="$qw" height="$qh" fill="url(#${ids.qChallengers})"/>""")
            append("""<rect x="$qw" y="0" width="$qw" height="$qh" fill="url(#${ids.qLeaders})"/>""")
            append("""<rect x="0" y="$qh" width="$qw" height="$qh" fill="url(#${ids.qNiche})"/>""")
            append("""<rect x="$qw" y="$qh" width="$qw" height="$qh" fill="url(#${ids.qVisionaries})"/>""")
            append("""<line x1="$qw" y1="0" x2="$qw" y2="$chartHeight" class="split-line" stroke-dasharray="6 6"/>""")
            append("""<line x1="0" y1="$qh" x2="$chartWidth" y2="$qh" class="split-line" stroke-dasharray="6 6"/>""")
            append("""<rect x="0" y="0" width="$chartWidth" height="$chartHeight" rx="12" fill="none" class="chart-border"/>""")
            append("</g>")

            appendQuadrantChip(this, config.challengersLabel.uppercase(), 12, 12, "#F59F00")
            appendQuadrantChip(this, config.leadersLabel.uppercase(), chartWidth - estimateChipWidth(config.leadersLabel.uppercase()) - 12, 12, "#12B886")
            appendQuadrantChip(this, config.nichePlayersLabel.uppercase(), 12, chartHeight - 40, "#F43F5E")
            appendQuadrantChip(this, config.visionariesLabel.uppercase(), chartWidth - estimateChipWidth(config.visionariesLabel.uppercase()) - 12, chartHeight - 40, "#3B82F6")

            append("""</g>""")

            // Axis labels
            val xAxisY = baseHeight - 15
            append("""<g class="reveal d3">""")
            append("""<text x="${margin + qw / 2}" y="$xAxisY" text-anchor="middle" class="axis-label">${escapeXml(config.xAxisLabel.uppercase())}</text>""")
            if (config.xAxisLabelEnd.isNotEmpty()) {
                append("""<text x="${margin + qw + qw / 2}" y="$xAxisY" text-anchor="middle" class="axis-label">${escapeXml(config.xAxisLabelEnd.uppercase())} →</text>""")
            }

            val yAxisX = 20
            val yBottomHalfCenter = chartStartY + qh + qh / 2
            val yTopHalfCenter = chartStartY + qh / 2
            append("""<text x="$yAxisX" y="$yBottomHalfCenter" text-anchor="middle" class="axis-label" transform="rotate(-90 $yAxisX $yBottomHalfCenter)">${escapeXml(config.yAxisLabel.uppercase())}</text>""")
            if (config.yAxisLabelEnd.isNotEmpty()) {
                append("""<text x="$yAxisX" y="$yTopHalfCenter" text-anchor="middle" class="axis-label" transform="rotate(-90 $yAxisX $yTopHalfCenter)">${escapeXml(config.yAxisLabelEnd.uppercase())} →</text>""")
            }
            append("</g>")

            // Companies
            config.companies.forEachIndexed { index, company ->
                val x = margin + (company.x / 100.0 * chartWidth).roundToInt()
                val y = chartStartY + chartHeight - (company.y / 100.0 * chartHeight).roundToInt()
                val radius = max(8, min(25, company.size))
                val isFocus = focusCompany == company
                val bubbleId = getBubbleId(ids, company.x, company.y)
                val delayClass = "d${(index % 3) + 2}"

                val nodeOpen = if (company.url.isNotEmpty()) {
                    """<a href="${escapeXml(company.url)}" target="_blank"><g aria-label="${escapeXml(company.name)}">"""
                } else {
                    """<g aria-label="${escapeXml(company.name)}">"""
                }

                append("""<g transform="translate($x,$y)">""")
                append("""<g class="reveal $delayClass">""")
                append(nodeOpen)

                if (isFocus && !isPdf) {
                    append("""<circle cx="0" cy="0" r="${radius + 6}" class="focus-pulse" fill="none" stroke="var(--focus)" stroke-width="2"/>""")
                }

                append("""<circle cx="0" cy="0" r="$radius" fill="url(#$bubbleId)" class="node-core" filter="url(#${ids.nodeGlow})"/>""")
                append("""<circle cx="${-(radius * 0.25).roundToInt()}" cy="${-(radius * 0.30).roundToInt()}" r="${max(2, (radius * 0.22).roundToInt())}" class="node-specular"/>""")

                val labelY = radius + 20
                append("""<text x="0" y="$labelY" text-anchor="middle" class="company-name">${escapeXml(company.name)}</text>""")
                if (company.description.isNotEmpty()) {
                    append("""<title>${escapeXml(company.description)}</title>""")
                }

                append("</g>")
                if (company.url.isNotEmpty()) append("</a>")
                append("</g>")
                append("</g>")
            }

            append("</svg>")
        }
    }

    private fun appendQuadrantChip(sb: StringBuilder, text: String, x: Int, y: Int, color: String) {
        val width = estimateChipWidth(text)
        sb.append("""<g class="reveal d3">""")
        sb.append("""<rect x="$x" y="$y" width="$width" height="28" rx="14" class="chip-bg" stroke="$color" stroke-opacity="0.45"/>""")
        sb.append("""<text x="${x + width / 2}" y="${y + 18}" text-anchor="middle" class="quad-chip-text" fill="$color">${escapeXml(text)}</text>""")
        sb.append("</g>")
    }

    private fun estimateChipWidth(text: String): Int {
        return max(98, (text.length * 7.2).roundToInt() + 26)
    }

    private fun getBubbleId(ids: DefIds, x: Double, y: Double): String {
        return when {
            x >= 50 && y >= 50 -> ids.bLeaders
            x < 50 && y >= 50 -> ids.bChallengers
            x >= 50 && y < 50 -> ids.bVisionaries
            else -> ids.bNiche
        }
    }

    private fun generateDefs(ids: DefIds, useDark: Boolean): String {
        val bg0 = if (useDark) "#0A1026" else "#F4F8FF"
        val bg1 = if (useDark) "#101A3A" else "#EAF2FF"
        val bg2 = if (useDark) "#0B1430" else "#E5EEFF"

        val washAColor = if (useDark) "#4CB4FF" else "#4CB4FF"
        val washBColor = if (useDark) "#8E7BFF" else "#8E7BFF"
        val washAOpacity = if (useDark) "0.22" else "0.18"
        val washBOpacity = if (useDark) "0.18" else "0.14"

        val gridStroke = if (useDark) "rgba(191,208,255,0.22)" else "rgba(42,72,122,0.18)"

        val qOpacity = if (useDark) "0.22" else "0.18"

        return """
            <defs>
                <linearGradient id="${ids.bg}" x1="0" y1="0" x2="1" y2="1">
                    <stop offset="0%" stop-color="$bg0"/>
                    <stop offset="55%" stop-color="$bg1"/>
                    <stop offset="100%" stop-color="$bg2"/>
                </linearGradient>

                <radialGradient id="${ids.washA}" cx="18%" cy="20%" r="50%">
                    <stop offset="0%" stop-color="$washAColor" stop-opacity="$washAOpacity"/>
                    <stop offset="100%" stop-color="$washAColor" stop-opacity="0"/>
                </radialGradient>

                <radialGradient id="${ids.washB}" cx="88%" cy="18%" r="42%">
                    <stop offset="0%" stop-color="$washBColor" stop-opacity="$washBOpacity"/>
                    <stop offset="100%" stop-color="$washBColor" stop-opacity="0"/>
                </radialGradient>

                <pattern id="${ids.grid}" width="32" height="32" patternUnits="userSpaceOnUse">
                    <path d="M32 0H0V32" fill="none" stroke="$gridStroke" stroke-width="1"/>
                </pattern>

                <radialGradient id="${ids.qLeaders}" cx="82%" cy="20%" r="85%">
                    <stop offset="0%" stop-color="#12B886" stop-opacity="$qOpacity"/>
                    <stop offset="100%" stop-color="#12B886" stop-opacity="0"/>
                </radialGradient>
                <radialGradient id="${ids.qChallengers}" cx="18%" cy="20%" r="85%">
                    <stop offset="0%" stop-color="#F59F00" stop-opacity="$qOpacity"/>
                    <stop offset="100%" stop-color="#F59F00" stop-opacity="0"/>
                </radialGradient>
                <radialGradient id="${ids.qVisionaries}" cx="82%" cy="80%" r="85%">
                    <stop offset="0%" stop-color="#3B82F6" stop-opacity="$qOpacity"/>
                    <stop offset="100%" stop-color="#3B82F6" stop-opacity="0"/>
                </radialGradient>
                <radialGradient id="${ids.qNiche}" cx="18%" cy="80%" r="85%">
                    <stop offset="0%" stop-color="#F43F5E" stop-opacity="$qOpacity"/>
                    <stop offset="100%" stop-color="#F43F5E" stop-opacity="0"/>
                </radialGradient>

                <radialGradient id="${ids.bLeaders}" cx="35%" cy="30%" r="70%">
                    <stop offset="0%" stop-color="#4EE7BC"/>
                    <stop offset="100%" stop-color="#12B886"/>
                </radialGradient>
                <radialGradient id="${ids.bChallengers}" cx="35%" cy="30%" r="70%">
                    <stop offset="0%" stop-color="#FFD86B"/>
                    <stop offset="100%" stop-color="#F59F00"/>
                </radialGradient>
                <radialGradient id="${ids.bVisionaries}" cx="35%" cy="30%" r="70%">
                    <stop offset="0%" stop-color="#8BB7FF"/>
                    <stop offset="100%" stop-color="#3B82F6"/>
                </radialGradient>
                <radialGradient id="${ids.bNiche}" cx="35%" cy="30%" r="70%">
                    <stop offset="0%" stop-color="#FF7A92"/>
                    <stop offset="100%" stop-color="#F43F5E"/>
                </radialGradient>

                <filter id="${ids.boardShadow}" x="-40%" y="-40%" width="180%" height="180%">
                    <feGaussianBlur in="SourceAlpha" stdDeviation="${if (useDark) 5 else 4}"/>
                    <feOffset dx="0" dy="${if (useDark) 4 else 3}"/>
                    <feComponentTransfer>
                        <feFuncA type="linear" slope="${if (useDark) 0.32 else 0.18}"/>
                    </feComponentTransfer>
                    <feMerge>
                        <feMergeNode/>
                        <feMergeNode in="SourceGraphic"/>
                    </feMerge>
                </filter>

                <filter id="${ids.nodeGlow}" x="-60%" y="-60%" width="220%" height="220%">
                    <feGaussianBlur stdDeviation="${if (useDark) 4 else 3}" result="blur"/>
                    <feMerge>
                        <feMergeNode in="blur"/>
                        <feMergeNode in="SourceGraphic"/>
                    </feMerge>
                </filter>
            </defs>
        """.trimIndent()
    }

    private fun generateStyles(svgId: String, useDark: Boolean, isPdf: Boolean): String {
        val animRise = "mqRise_$svgId"
        val animPulse = "mqPulse_$svgId"

        val text = if (useDark) "#EAF1FF" else "#10213D"
        val muted = if (useDark) "#A8B8DA" else "#4E6791"
        val axis = if (useDark) "rgba(191,208,255,0.46)" else "rgba(42,72,122,0.45)"
        val glass = if (useDark) "rgba(255,255,255,0.10)" else "rgba(255,255,255,0.58)"
        val glassStroke = if (useDark) "rgba(219,230,255,0.34)" else "rgba(142,172,221,0.58)"
        val chipBg = if (useDark) "rgba(255,255,255,0.12)" else "rgba(255,255,255,0.78)"
        val focus = if (useDark) "#00C2FF" else "#00A9E8"

        return """
            <style>
                ${theme.fontImport}
                #$svgId {
                    --text: $text;
                    --muted: $muted;
                    --axis: $axis;
                    --glass: $glass;
                    --glass-stroke: $glassStroke;
                    --chip-bg: $chipBg;
                    --focus: $focus;
                }

                #$svgId .board {
                    fill: var(--glass);
                    stroke: var(--glass-stroke);
                    filter: url(#boardShadow_$svgId);
                }

                #$svgId .title-text {
                    font-family: ${theme.fontFamily};
                    font-size: ${38 / theme.fontWidthMultiplier}px;
                    font-weight: 800;
                    letter-spacing: -0.01em;
                    fill: var(--text);
                }

                #$svgId .sub-text {
                    font-family: ${theme.fontFamily};
                    font-size: 12px;
                    font-weight: 500;
                    fill: var(--muted);
                }

                #$svgId .axis-label {
                    font-family: ${theme.fontFamily};
                    font-size: 11px;
                    font-weight: 700;
                    letter-spacing: 0.12em;
                    text-transform: uppercase;
                    fill: var(--muted);
                }

                #$svgId .split-line {
                    stroke: var(--axis);
                    stroke-width: 2;
                }

                #$svgId .chart-border {
                    stroke: var(--axis);
                    stroke-width: 1.2;
                }

                #$svgId .chip-bg {
                    fill: var(--chip-bg);
                }

                #$svgId .quad-chip-text {
                    font-family: ${theme.fontFamily};
                    font-size: 12px;
                    font-weight: 700;
                    letter-spacing: 0.08em;
                }

                #$svgId .company-name {
                    font-family: ${theme.fontFamily};
                    font-size: 12px;
                    font-weight: 600;
                    fill: var(--text);
                }

                #$svgId .node-core {
                    filter: url(#nodeGlow_$svgId);
                }

                #$svgId .node-specular {
                    fill: #ffffff;
                    opacity: ${if (useDark) 0.52 else 0.62};
                }

                #$svgId .reveal {
                    opacity: 0;
                    animation: $animRise 420ms cubic-bezier(.2,.8,.2,1) forwards;
                }

                #$svgId .d1 { animation-delay: 60ms; }
                #$svgId .d2 { animation-delay: 120ms; }
                #$svgId .d3 { animation-delay: 180ms; }
                #$svgId .d4 { animation-delay: 240ms; }

                ${if (!isPdf) """
                #$svgId .focus-pulse {
                    animation: $animPulse 2.2s ease-in-out infinite;
                    transform-origin: center;
                }
                """.trimIndent() else ""}

                @keyframes $animRise {
                    from { opacity: 0; transform: translateY(8px); }
                    to { opacity: 1; transform: translateY(0); }
                }

                ${if (!isPdf) """
                @keyframes $animPulse {
                    0%,100% { opacity: ${if (useDark) ".40" else ".36"}; r: 24; }
                    50% { opacity: ${if (useDark) ".18" else ".14"}; r: 30; }
                }
                """.trimIndent() else ""}
            </style>
        """.trimIndent()
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}