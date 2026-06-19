package io.docops.docopsextensionssupport.steps

import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.support.wrapTextToWidth
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import java.util.UUID
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.round

class IsometricStepsSvgGenerator(private val useDark: Boolean = false) {
    private var theme: DocOpsTheme = ThemeFactory.getTheme(useDark)

    private data class Rect(val x: Double, val y: Double, val w: Double, val h: Double)

    private data class StepRender(
        val x: Double,
        val y: Double,
        val color: String,
        val order: Int,
        val title: String,
        val descLines: List<String>,
        val cardWidth: Double,
        val cardHeight: Double = 88.0,
        val cardX: Double,
        val cardY: Double,
        val delay: Int
    )

    fun createSvg(payload: IsometricSteps, scale: Double = 1.0): String {
        val config = payload.config
        val steps = payload.steps
        val n = steps.size

        theme = ThemeFactory.getThemeByName(config.theme, useDark)
        val svgId = generateSvgId(config.title)

        val w = 120.0
        val h = 60.0
        val d = 20.0

        val dx = config.dx?.toDouble() ?: floor((config.canvasWidth * 0.55) / max(1, n - 1))
        val dy = config.dy?.toDouble() ?: round(-dx * 0.62)

        val startX = config.startX?.toDouble() ?: 150.0
        val startY = config.startY?.toDouble() ?: (config.canvasHeight - 150.0)

        val minCardWidth = 372.0
        val maxCardWidth = 460.0
        val cardVerticalGap = 12.0
        val collisionPad = 8.0

        val placed = mutableListOf<Rect>()
        val renders = mutableListOf<StepRender>()

        steps.forEachIndexed { i, step ->
            val stepX = startX + i * dx
            val stepY = startY + i * dy
            val color = step.color ?: theme.chartPalette[i % theme.chartPalette.size].color

            val titleWidth = estimateTextWidth(step.title, 20)
            val preWrap = wrapTextToWidth(step.desc, (420.0 - 54.0).toFloat()).take(config.maxDescLines)
            val descWidth = preWrap.maxOfOrNull { estimateTextWidth(it, 14) } ?: 0.0
            val cardWidth = (max(titleWidth, descWidth) + 62.0).coerceIn(minCardWidth, maxCardWidth)

            val descLines = wrapTextToWidth(step.desc, (cardWidth - 54.0).toFloat()).take(config.maxDescLines)

            val cardX = stepX + config.labelOffsetX
            var cardY = stepY + config.labelOffsetY

            while (placed.any { overlaps(it, Rect(cardX, cardY, cardWidth, 88.0), collisionPad) }) {
                cardY += (88.0 + cardVerticalGap)
            }

            placed.add(Rect(cardX, cardY, cardWidth, 88.0))
            renders.add(
                StepRender(
                    x = stepX,
                    y = stepY,
                    color = color,
                    order = step.order,
                    title = step.title,
                    descLines = descLines,
                    cardWidth = cardWidth,
                    cardX = cardX,
                    cardY = cardY,
                    delay = 200 + i * 150
                )
            )
        }

        val requiredWidth = renders.maxOfOrNull { it.cardX + it.cardWidth + 24.0 } ?: config.canvasWidth.toDouble()
        val requiredHeight = renders.maxOfOrNull { it.cardY + it.cardHeight + 24.0 } ?: config.canvasHeight.toDouble()

        val canvasWidth = max(config.canvasWidth.toDouble(), requiredWidth).toInt()
        val canvasHeight = max(config.canvasHeight.toDouble(), requiredHeight).toInt()

        val stepsSvg = buildString {
            renders.forEach { r ->
                append(
                    drawStep(
                        x = r.x,
                        y = r.y,
                        w = w,
                        h = h,
                        d = d,
                        color = r.color,
                        order = r.order,
                        title = r.title,
                        descLines = r.descLines,
                        cardX = r.cardX,
                        cardY = r.cardY,
                        cardWidth = r.cardWidth,
                        delay = r.delay
                    )
                )
            }
        }

        return """
            <svg xmlns="http://www.w3.org/2000/svg"
                 width="${(canvasWidth * scale).toInt() / DISPLAY_RATIO_16_9}" height="${(canvasHeight * scale).toInt() / DISPLAY_RATIO_16_9}"
                 viewBox="0 0 $canvasWidth $canvasHeight" id="$svgId">
                <defs>
                    ${svgDefs(svgId)}
                </defs>

                <g transform="scale($scale)">
                    <rect width="$canvasWidth" height="$canvasHeight" fill="url(#${svgId}_bgSurface)"/>
                    <rect width="$canvasWidth" height="$canvasHeight" fill="url(#${svgId}_grid)"/>
                    <rect width="$canvasWidth" height="$canvasHeight" fill="url(#${svgId}_washA)"/>
                    <rect width="$canvasWidth" height="$canvasHeight" fill="url(#${svgId}_washB)"/>

                    <g transform="translate(40, 60)">
                        <text x="0" y="0" class="title">${escapeXml(config.title)}</text>
                        ${if (config.subtitle != null) """<text x="0" y="30" class="subtitle">${escapeXml(config.subtitle)}</text>""" else ""}
                        <line x1="0" y1="52" x2="${canvasWidth - 80}" y2="52" class="header-rule"/>
                    </g>

                    $stepsSvg
                </g>
            </svg>
        """.trimIndent()
    }

    private fun drawStep(
        x: Double,
        y: Double,
        w: Double,
        h: Double,
        d: Double,
        color: String,
        order: Int,
        title: String,
        descLines: List<String>,
        cardX: Double,
        cardY: Double,
        cardWidth: Double,
        delay: Int
    ): String {
        val svgColor = SVGColor(color)
        val darker = svgColor.darker() ?: color
        val lighter = svgColor.lighter() ?: color

        val accentX = cardX + 18
        val textX = cardX + 36
        val connectorEndX = cardX + 26
        val connectorEndY = cardY + 12

        val descText = descLines.mapIndexed { i, line ->
            """<text x="$textX" y="${cardY + 58 + i * 18}" class="desc">${escapeXml(line)}</text>"""
        }.joinToString("\n")

        return """
            <g>
                <g class="reveal" style="animation-delay:${delay}ms">
                    <polygon points="${x - w / 2},$y ${x - w / 2},${y + d} $x,${y + h / 2 + d} $x,${y + h / 2}" fill="$darker"/>
                    <polygon points="${x + w / 2},$y ${x + w / 2},${y + d} $x,${y + h / 2 + d} $x,${y + h / 2}" fill="$color"/>
                    <polygon points="$x,${y - h / 2} ${x + w / 2},$y $x,${y + h / 2} ${x - w / 2},$y" fill="$lighter" stroke="$lighter" stroke-width="0.5"/>

                    <circle cx="$x" cy="${y - h / 2 - 15}" r="15" fill="$color"/>
                    <text x="$x" y="${y - h / 2 - 10}" text-anchor="middle" class="badge-text">$order</text>

                    <line x1="$x" y1="$y" x2="$connectorEndX" y2="$connectorEndY" class="connector"/>

                    <rect x="${cardX + 8}" y="${cardY + 8}" width="$cardWidth" height="88" rx="14" class="label-card-depth"/>
                    <rect x="$cardX" y="$cardY" width="$cardWidth" height="88" rx="14" class="label-card"/>
                    <rect x="$accentX" y="${cardY + 12}" width="4" height="56" rx="2" fill="$color"/>

                    <text x="$textX" y="${cardY + 36}" class="step-title">${escapeXml(title)}</text>
                    $descText
                </g>
            </g>
        """.trimIndent()
    }

    private fun svgDefs(svgId: String): String {
        val bgStart = if (useDark) "#081126" else "#F8FBFF"
        val bgMid = if (useDark) "#0D1A38" else "#EDF4FF"
        val bgEnd = if (useDark) "#0A1430" else "#E8F0FF"

        val washA = if (useDark) "#2EC5FF" else "#00B7D6"
        val washB = if (useDark) "#FFB347" else "#D97706"

        val washAOpacity = if (useDark) "0.24" else "0.18"
        val washBOpacity = if (useDark) "0.18" else "0.14"

        val gridStroke = if (useDark) "rgba(191,208,255,0.14)" else "rgba(32,64,112,0.10)"
        val titleFill = if (useDark) "#EAF2FF" else "#0F223D"
        val subtitleFill = if (useDark) "#ADC0E6" else "#3F5F8A"
        val headerRule = if (useDark) "rgba(197,212,255,0.30)" else "rgba(33,79,142,0.30)"
        val cardFill = if (useDark) "rgba(255,255,255,0.10)" else "rgba(255,255,255,0.72)"
        val cardStroke = if (useDark) "rgba(214,228,255,0.28)" else "rgba(120,154,205,0.48)"
        val cardDepth = if (useDark) "rgba(147,178,232,0.10)" else "rgba(98,138,201,0.16)"
        val connector = if (useDark) "#24D1FF" else "#00B7D6"
        val descFill = if (useDark) "#C7D7F5" else "#49648E"
        val badgeFill = "#FFFFFF"

        val riseAnimation = "isoRise_$svgId"

        return """
            <linearGradient id="${svgId}_bgSurface" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stop-color="$bgStart"/>
                <stop offset="58%" stop-color="$bgMid"/>
                <stop offset="100%" stop-color="$bgEnd"/>
            </linearGradient>

            <radialGradient id="${svgId}_washA" cx="16%" cy="18%" r="50%">
                <stop offset="0%" stop-color="$washA" stop-opacity="$washAOpacity"/>
                <stop offset="100%" stop-color="$washA" stop-opacity="0"/>
            </radialGradient>

            <radialGradient id="${svgId}_washB" cx="86%" cy="16%" r="42%">
                <stop offset="0%" stop-color="$washB" stop-opacity="$washBOpacity"/>
                <stop offset="100%" stop-color="$washB" stop-opacity="0"/>
            </radialGradient>

            <pattern id="${svgId}_grid" width="32" height="32" patternUnits="userSpaceOnUse">
                <path d="M32 0H0V32" fill="none" stroke="$gridStroke" stroke-width="1"/>
            </pattern>

            <filter id="${svgId}_cardDepthBlur" x="-20%" y="-20%" width="140%" height="140%">
                <feGaussianBlur stdDeviation="1.5"/>
            </filter>

            <style>
                ${theme.fontImport}
                #$svgId .title {
                    font-family: ${theme.fontFamily};
                    font-size: 38px;
                    font-weight: 800;
                    letter-spacing: -0.01em;
                    fill: $titleFill;
                }

                #$svgId .subtitle {
                    font-family: ${theme.fontFamily};
                    font-size: 14px;
                    font-weight: 500;
                    letter-spacing: 0.02em;
                    fill: $subtitleFill;
                }

                #$svgId .header-rule {
                    stroke: $headerRule;
                    stroke-width: 1;
                }

                #$svgId .step-title {
                    font-family: ${theme.fontFamily};
                    font-size: 20px;
                    font-weight: 700;
                    fill: $titleFill;
                }

                #$svgId .desc {
                    font-family: ${theme.fontFamily};
                    font-size: 14px;
                    font-weight: 400;
                    fill: $descFill;
                }

                #$svgId .badge-text {
                    font-family: ${theme.fontFamily};
                    font-size: 13px;
                    font-weight: 800;
                    fill: $badgeFill;
                }

                #$svgId .connector {
                    stroke: $connector;
                    stroke-width: 1.5;
                    stroke-dasharray: 5 4;
                    opacity: 0.52;
                }

                #$svgId .label-card {
                    fill: $cardFill;
                    stroke: $cardStroke;
                    stroke-width: 1;
                }

                #$svgId .label-card-depth {
                    fill: $cardDepth;
                    filter: url(#${svgId}_cardDepthBlur);
                }

                #$svgId .reveal {
                    opacity: 0;
                    animation: $riseAnimation 560ms cubic-bezier(.2,.85,.2,1) forwards;
                }

                @keyframes $riseAnimation {
                    from { opacity: 0; transform: translateY(10px); }
                    to { opacity: 1; transform: translateY(0); }
                }
            </style>
        """.trimIndent()
    }

    private fun overlaps(a: Rect, b: Rect, pad: Double): Boolean {
        val ax1 = a.x - pad
        val ay1 = a.y - pad
        val ax2 = a.x + a.w + pad
        val ay2 = a.y + a.h + pad

        val bx1 = b.x - pad
        val by1 = b.y - pad
        val bx2 = b.x + b.w + pad
        val by2 = b.y + b.h + pad

        return ax1 < bx2 && ax2 > bx1 && ay1 < by2 && ay2 > by1
    }

    private fun estimateTextWidth(text: String, fontSize: Int): Double {
        return text.length * (fontSize * 0.56)
    }

    private fun generateSvgId(title: String): String {
        val slug = title.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .replace(Regex("\\s+"), "-")
            .trim('-')
            .ifBlank { "isometric-steps" }
            .take(36)

        val suffix = UUID.randomUUID().toString().replace("-", "").take(10)
        return "iso_${slug}_$suffix"
    }

    private fun escapeXml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}