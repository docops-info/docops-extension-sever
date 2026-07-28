package io.docops.docopsextensionssupport.chart.pie

import io.docops.docopsextensionssupport.chart.ChartColors
import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class PieMakerImproved {

    private val MODERN_COLORS = ChartColors.CYBER_PALETTE
    private var theme = ThemeFactory.getTheme(false)

    fun makePies(pies: Pies): String {
        theme = if (pies.pieDisplay.theme.isNotBlank()) {
            ThemeFactory.getThemeByName(pies.pieDisplay.theme, pies.pieDisplay.useDark)
        } else {
            ThemeFactory.getTheme(pies.pieDisplay)
        }

        val pieCount = pies.pies.size
        val pieWidth = 88
        val totalPieWidth = pieCount * pieWidth
        val width = totalPieWidth + 64
        val height = pies.maxRows().coerceAtMost(2) * 10 + 92
        val leftMargin = (width - totalPieWidth) / 2

        val sb = StringBuilder()
        sb.append(makeHead(width, height, pies))
        sb.append("<defs>")
        sb.append(filtersAndAtmosphere(pies, theme))
        sb.append(makePieGradients(pies))
        sb.append("</defs>")
        sb.append(makeBackground(pies, width, height))
        sb.append(makeRail(width, height, pieCount))

        pies.pies.forEachIndexed { index, pie ->
            val x = leftMargin + (index * pieWidth)
            val delay = index * 0.08

            sb.append("""<g transform="translate($x, 16)">""")
            sb.append("""<g class="pie-card" style="animation-delay: ${delay}s;">""")
            sb.append(makePieSvg(pie, index, theme, pies.pieDisplay.id))
            sb.append(makeLabel(pie, index, theme))
            sb.append("</g></g>")
        }

        sb.append("</svg></svg>")
        return sb.toString()
    }

    private fun makeHead(width: Int, height: Int, pies: Pies): String {
        val shadowPadding = 24
        val paddedWidth = width + shadowPadding * 2
        val paddedHeight = height + shadowPadding * 2

        val outerHeight = (1 + pies.pieDisplay.scale) * paddedHeight
        val outerWidth = (1 + pies.pieDisplay.scale) * paddedWidth

        return """
            <svg xmlns="http://www.w3.org/2000/svg"
                 height="${outerHeight / DISPLAY_RATIO_16_9}"
                 width="${outerWidth / DISPLAY_RATIO_16_9}"
                 viewBox="-$shadowPadding -$shadowPadding $paddedWidth $paddedHeight"
                 id="id_${pies.pieDisplay.id}">
                <svg xmlns="http://www.w3.org/2000/svg"
                     width="$width"
                     height="$height"
                     viewBox="0 0 $width $height">
        """.trimIndent()
    }

    private fun makeBackground(pies: Pies, width: Int, height: Int): String {
        val id = pies.pieDisplay.id

        return """
            <rect class="pie-bg-base" width="$width" height="$height" rx="${theme.cornerRadius}" pointer-events="none"/>
            <rect class="pie-bg-atmosphere" width="$width" height="$height" rx="${theme.cornerRadius}" pointer-events="none"/>
            <rect class="pie-bg-grid" width="$width" height="$height" rx="${theme.cornerRadius}" pointer-events="none"/>
            <circle cx="${width - 40}" cy="18" r="74" fill="url(#pieOrbitalWash_$id)" pointer-events="none"/>
        """.trimIndent()
    }

    private fun makeRail(width: Int, height: Int, pieCount: Int): String {
        if (pieCount == 0) {
            return ""
        }

        return """
            <rect class="pie-rail"
                  x="24"
                  y="10"
                  width="${width - 48}"
                  height="${height - 20}"
                  rx="24"
                  pointer-events="none"/>
            <path class="pie-rail-score"
                  d="M 36 ${height - 20} C ${width * 0.28} ${height - 44}, ${width * 0.72} ${height + 10}, ${width - 36} ${height - 28}"
                  fill="none"
                  pointer-events="none"/>
        """.trimIndent()
    }

    private fun makePieSvg(pie: Pie, index: Int, theme: DocOpsTheme, id: String): String {
        val percent = pie.percent.coerceIn(0f, 100f)
        val displayPercent = formatPercent(percent)
        val gradId = "grad_${id}_$index"
        val glowId = "pieGlow_$id"
        val glassBlurId = "pieGlassBlur_$id"
        val needleGlowId = "pieNeedleGlow_$id"
        val cap = progressCap(percent)

        return """
            <svg class="pie-unit" width="56" height="56" x="16" y="4" viewBox="0 0 36 36">
                <title>${pie.label.escapeXml()}: $displayPercent%</title>

                <circle class="pie-shadow-disc"
                        cx="18"
                        cy="18"
                        r="15.8"
                        fill="var(--pie-canvas)"
                        stroke="var(--pie-text)"
                        stroke-opacity="0.06"
                        stroke-width="0.6"/>

                <circle class="pie-glass-foundation"
                        cx="18"
                        cy="18"
                        r="14.2"
                        fill="var(--pie-glass)"
                        filter="url(#$glassBlurId)"/>

                <circle class="pie-inner-sheen"
                        cx="18"
                        cy="18"
                        r="11.5"
                        fill="url(#pieInnerSheen_$id)"
                        opacity="0.7"/>

                <path class="pie-tick-ring"
                      d="M18 4 a 14 14 0 0 1 0 28 a 14 14 0 0 1 0 -28"
                      fill="none"
                      pathLength="100"/>

                <path class="pie-progress-track"
                      d="M18 4 a 14 14 0 0 1 0 28 a 14 14 0 0 1 0 -28"
                      fill="none"
                      pathLength="100"/>

                <path class="pie-progress"
                      d="M18 4 a 14 14 0 0 1 0 28 a 14 14 0 0 1 0 -28"
                      fill="none"
                      stroke="url(#$gradId)"
                      stroke-dasharray="$percent 100"
                      pathLength="100"
                      filter="url(#$glowId)"
                      style="animation-delay: ${0.18 + index * 0.08}s;"/>

                ${makeCapSvg(percent, cap.first, cap.second, gradId, needleGlowId)}

                <text class="pie-percent-halo"
                      x="18"
                      y="18.65"
                      text-anchor="middle">$displayPercent%</text>

                <text class="pie-percent"
                      x="18"
                      y="18.65"
                      text-anchor="middle"
                      style="animation-delay: ${0.45 + index * 0.08}s;">$displayPercent%</text>
            </svg>
        """.trimIndent()
    }

    private fun makeCapSvg(percent: Float, x: Double, y: Double, gradId: String, needleGlowId: String): String {
        if (percent < 4f) {
            return ""
        }

        return """
            <circle class="pie-progress-cap"
                    cx="${formatCoordinate(x)}"
                    cy="${formatCoordinate(y)}"
                    r="1.65"
                    fill="url(#$gradId)"
                    filter="url(#$needleGlowId)"/>
        """.trimIndent()
    }

    private fun makeLabel(pie: Pie, index: Int, theme: DocOpsTheme): String {
        val fontSize = 8.5f / theme.fontWidthMultiplier
        val lines = wrapLabel(pie.label, maxChars = 12, maxLines = 2)
        val labelSvg = lines.mapIndexed { i, line ->
            val dy = if (i == 0) 0 else 9
            """<tspan x="44" dy="$dy">${line.escapeXml()}</tspan>"""
        }.joinToString("")

        return """
            <text class="pie-label"
                  x="44"
                  y="70"
                  text-anchor="middle"
                  style="font-size: ${formatCoordinate(fontSize.toDouble())}px; animation-delay: ${0.55 + index * 0.08}s;">
                $labelSvg
            </text>
        """.trimIndent()
    }

    private fun filtersAndAtmosphere(pies: Pies, theme: DocOpsTheme): String {
        val id = pies.pieDisplay.id
        val darkMode = pies.pieDisplay.useDark
        val bgGridOpacity = if (darkMode) "0.16" else "0.34"
        val atmosphereOpacity = if (darkMode) "0.22" else "0.18"
        val railOpacity = if (darkMode) "0.18" else "0.38"

        return """
            <style>
                ${theme.fontImport}

                #id_$id {
                    --pie-canvas: ${theme.canvas};
                    --pie-text: ${theme.primaryText};
                    --pie-muted: ${theme.secondaryText};
                    --pie-accent: ${theme.accentColor};
                    --pie-glass: ${theme.glassEffect};
                    --pie-surface: ${theme.surfaceImpact};
                    --pie-bg-grid-opacity: $bgGridOpacity;
                    --pie-atmosphere-opacity: $atmosphereOpacity;
                    --pie-rail-opacity: $railOpacity;
                }

                @keyframes pieCanvasEnter {
                    from {
                        opacity: 0;
                    }
                    to {
                        opacity: 1;
                    }
                }

                @keyframes pieCardEnter {
                    from {
                        transform: translateY(6px) scale(0.92);
                        opacity: 0;
                    }
                    to {
                        transform: translateY(0) scale(1);
                        opacity: 1;
                    }
                }

                @keyframes pieRingDraw {
                    from {
                        stroke-dashoffset: 100;
                    }
                    to {
                        stroke-dashoffset: 0;
                    }
                }

                @keyframes pieTextRise {
                    from {
                        transform: translateY(2px);
                        opacity: 0;
                    }
                    to {
                        transform: translateY(0);
                        opacity: 1;
                    }
                }

                @keyframes pieLabelRise {
                    from {
                        transform: translateY(3px);
                        opacity: 0;
                    }
                    to {
                        transform: translateY(0);
                        opacity: 1;
                    }
                }

                #id_$id .pie-bg-base {
                    fill: var(--pie-canvas);
                    animation: pieCanvasEnter 0.35s ease-out both;
                }

                #id_$id .pie-bg-atmosphere {
                    fill: url(#pieAtmosphere_$id);
                    opacity: var(--pie-atmosphere-opacity);
                    animation: pieCanvasEnter 0.55s ease-out both;
                }

                #id_$id .pie-bg-grid {
                    fill: url(#pieGrid_$id);
                    opacity: var(--pie-bg-grid-opacity);
                    animation: pieCanvasEnter 0.7s ease-out both;
                }

                #id_$id .pie-rail {
                    fill: url(#pieRailWash_$id);
                    stroke: var(--pie-text);
                    stroke-opacity: 0.055;
                    stroke-width: 1;
                    opacity: var(--pie-rail-opacity);
                }

                #id_$id .pie-rail-score {
                    stroke: var(--pie-accent);
                    stroke-opacity: 0.18;
                    stroke-width: 1;
                    stroke-dasharray: 3 9;
                }

                #id_$id .pie-card {
                    opacity: 0;
                    transform-box: fill-box;
                    transform-origin: center;
                    animation: pieCardEnter 0.64s cubic-bezier(0.22, 1, 0.36, 1) both;
                }

                #id_$id .pie-card:hover .pie-unit {
                    transform: translateY(-2px) scale(1.06);
                }

                #id_$id .pie-card:hover .pie-label {
                    fill: var(--pie-text) !important;
                    opacity: 1;
                }

                #id_$id .pie-card:hover .pie-progress {
                    filter: url(#pieGlow_$id) brightness(1.12);
                }

                #id_$id .pie-unit {
                    overflow: visible;
                    cursor: pointer;
                    transition: transform 0.34s cubic-bezier(0.175, 0.885, 0.32, 1.275);
                    transform-box: fill-box;
                    transform-origin: center;
                }

                #id_$id .pie-shadow-disc {
                    opacity: 0.96;
                }

                #id_$id .pie-glass-foundation {
                    opacity: 0.34;
                }

                #id_$id .pie-tick-ring {
                    stroke: var(--pie-text);
                    stroke-width: 0.65;
                    stroke-opacity: 0.16;
                    stroke-dasharray: 1.1 6.04;
                    transform: rotate(-90deg);
                    transform-origin: 18px 18px;
                }

                #id_$id .pie-progress-track {
                    stroke: var(--pie-text);
                    stroke-opacity: 0.105;
                    stroke-width: 3.25;
                    stroke-linecap: round;
                    transform: rotate(-90deg);
                    transform-origin: 18px 18px;
                }

                #id_$id .pie-progress {
                    stroke-width: 3.85;
                    stroke-linecap: round;
                    stroke-dashoffset: 100;
                    transform: rotate(-90deg);
                    transform-origin: 18px 18px;
                    animation: pieRingDraw 1.08s cubic-bezier(0.22, 1, 0.36, 1) both;
                }

                #id_$id .pie-progress-cap {
                    opacity: 0.96;
                    pointer-events: none;
                }

                #id_$id .pie-percent,
                #id_$id .pie-percent-halo {
                    font-family: 'JetBrains Mono', ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
                    font-size: 9.9px;
                    font-weight: 900;
                    dominant-baseline: middle;
                    letter-spacing: -0.72px;
                    pointer-events: none;
                }

                #id_$id .pie-percent {
                    fill: var(--pie-text) !important;
                    opacity: 0;
                    animation: pieTextRise 0.42s cubic-bezier(0.22, 1, 0.36, 1) both;
                }

                #id_$id .pie-percent-halo {
                    fill: var(--pie-canvas);
                    stroke: var(--pie-canvas);
                    stroke-width: 1.8;
                    stroke-linejoin: round;
                    opacity: 0.68;
                }

                #id_$id .pie-label {
                    font-family: ${theme.fontFamily};
                    font-weight: 800;
                    text-transform: uppercase;
                    letter-spacing: 0.36px;
                    fill: var(--pie-muted) !important;
                    opacity: 0;
                    animation: pieLabelRise 0.4s cubic-bezier(0.22, 1, 0.36, 1) both;
                    transition: fill 0.25s ease, opacity 0.25s ease;
                    pointer-events: none;
                }
            </style>

            <radialGradient id="pieAtmosphere_$id" cx="52%" cy="14%" r="82%">
                <stop offset="0%" stop-color="${theme.accentColor}" stop-opacity="1"/>
                <stop offset="48%" stop-color="${theme.surfaceImpact}" stop-opacity="0.42"/>
                <stop offset="100%" stop-color="${theme.canvas}" stop-opacity="0"/>
            </radialGradient>

            <radialGradient id="pieOrbitalWash_$id" cx="50%" cy="50%" r="50%">
                <stop offset="0%" stop-color="${theme.accentColor}" stop-opacity="${if (darkMode) "0.22" else "0.16"}"/>
                <stop offset="100%" stop-color="${theme.accentColor}" stop-opacity="0"/>
            </radialGradient>

            <linearGradient id="pieRailWash_$id" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="${theme.primaryText}" stop-opacity="${if (darkMode) "0.06" else "0.08"}"/>
                <stop offset="40%" stop-color="${theme.glassEffect}" stop-opacity="${if (darkMode) "0.08" else "0.13"}"/>
                <stop offset="100%" stop-color="${theme.accentColor}" stop-opacity="${if (darkMode) "0.07" else "0.10"}"/>
            </linearGradient>

            <linearGradient id="pieInnerSheen_$id" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="white" stop-opacity="${if (darkMode) "0.08" else "0.30"}"/>
                <stop offset="48%" stop-color="white" stop-opacity="${if (darkMode) "0.02" else "0.10"}"/>
                <stop offset="100%" stop-color="${theme.primaryText}" stop-opacity="${if (darkMode) "0.06" else "0.045"}"/>
            </linearGradient>

            <pattern id="pieGrid_$id" x="0" y="0" width="16" height="16" patternUnits="userSpaceOnUse">
                <path d="M 16 0 L 0 0 0 16"
                      fill="none"
                      stroke="${theme.primaryText}"
                      stroke-opacity="0.28"
                      stroke-width="0.75"/>
                <circle cx="0" cy="0" r="0.72" fill="${theme.accentColor}" fill-opacity="0.26"/>
            </pattern>

            <filter id="pieGlassBlur_$id" x="-20%" y="-20%" width="140%" height="140%">
                <feGaussianBlur in="SourceGraphic" stdDeviation="1.35"/>
            </filter>

            <filter id="pieGlow_$id" x="-35%" y="-35%" width="170%" height="170%">
                <feGaussianBlur stdDeviation="2.15" result="blur"/>
                <feColorMatrix in="blur"
                               type="matrix"
                               values="1 0 0 0 0
                                       0 1 0 0 0
                                       0 0 1 0 0
                                       0 0 0 10 -4"
                               result="glow"/>
                <feMerge>
                    <feMergeNode in="glow"/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>

            <filter id="pieNeedleGlow_$id" x="-80%" y="-80%" width="260%" height="260%">
                <feGaussianBlur stdDeviation="1.35" result="blur"/>
                <feComposite in="SourceGraphic" in2="blur" operator="over"/>
            </filter>
        """.trimIndent()
    }

    private fun makePieGradients(pies: Pies): String {
        val sb = StringBuilder()
        val id = pies.pieDisplay.id

        pies.pies.forEachIndexed { i, _ ->
            val color = getColorForIndex(i)
            sb.append(
                """
                    <linearGradient id="grad_${id}_$i" x1="0%" y1="0%" x2="100%" y2="100%">
                        <stop offset="0%" stop-color="$color" stop-opacity="0.68"/>
                        <stop offset="42%" stop-color="$color" stop-opacity="1"/>
                        <stop offset="100%" stop-color="$color" stop-opacity="0.86"/>
                    </linearGradient>
                """.trimIndent()
            )
        }

        return sb.toString()
    }

    private fun getColorForIndex(index: Int): String {
        return when {
            theme.chartPalette.isNotEmpty() -> theme.chartPalette[index % theme.chartPalette.size].color
            MODERN_COLORS.isNotEmpty() -> MODERN_COLORS[index % MODERN_COLORS.size].color
            else -> theme.accentColor
        }
    }

    private fun progressCap(percent: Float): Pair<Double, Double> {
        val radius = 14.0
        val angle = ((percent / 100.0) * 360.0 - 90.0) * PI / 180.0
        val x = 18.0 + radius * cos(angle)
        val y = 18.0 + radius * sin(angle)

        return x to y
    }

    private fun formatPercent(value: Float): String {
        return if (value % 1f == 0f) {
            value.toInt().toString()
        } else {
            "%.1f".format(value)
        }
    }

    private fun formatCoordinate(value: Double): String {
        return "%.2f".format(value)
    }

    private fun wrapLabel(label: String, maxChars: Int, maxLines: Int): List<String> {
        val clean = label.trim()
        if (clean.isEmpty()) {
            return listOf("")
        }

        val words = clean.split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var current = StringBuilder()

        words.forEach { word ->
            val candidate = if (current.isEmpty()) word else "$current $word"

            if (candidate.length <= maxChars || current.isEmpty()) {
                if (current.isNotEmpty()) {
                    current.append(" ")
                }
                current.append(word)
            } else {
                lines.add(current.toString())
                current = StringBuilder(word)
            }
        }

        if (current.isNotEmpty()) {
            lines.add(current.toString())
        }

        if (lines.size <= maxLines) {
            return lines
        }

        val clipped = lines.take(maxLines).toMutableList()
        val lastIndex = clipped.lastIndex
        clipped[lastIndex] = clipped[lastIndex].take((maxChars - 1).coerceAtLeast(1)).trimEnd() + "…"

        return clipped
    }
}