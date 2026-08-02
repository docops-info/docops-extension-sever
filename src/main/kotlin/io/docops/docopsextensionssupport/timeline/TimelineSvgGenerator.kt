package io.docops.docopsextensionssupport.timeline

import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import kotlin.math.max
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TimelineSvgGenerator {

    private var theme: DocOpsTheme = ThemeFactory.getTheme(false)
    private var isDark: Boolean = false

    @OptIn(ExperimentalUuidApi::class)
    fun generateTimeline(config: TimelineConfig, isDarkMode: Boolean = false, scale: String, theme: String = "premium"): String {
        this.isDark = isDarkMode
        this.theme = ThemeFactory.getThemeByName(theme, isDarkMode)
        return if (config.orientation == Orientation.HORIZONTAL) {
            generateTimelineHorizontal(config, scale)
        } else {
            generateTimelineVertical(config, scale)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateTimelineVertical(config: TimelineConfig, scale: String): String {
        val svgId = "timeline_${Uuid.random().toHexString()}"
        val colors = theme.chartPalette
        val scaleFactor = scale.toDoubleOrNull() ?: 1.0

        val baseWidth = 1200
        val topMargin = 140
        val bottomMargin = 100
        val itemSpacing = 160
        val centerX = baseWidth / 2
        val maxCardWidth = 420

        val itemHeights = config.events.map { calculateVerticalItemHeight(it, maxCardWidth - 64) }
        val totalHeight = topMargin + itemHeights.sum() + (config.events.size - 1).coerceAtLeast(0) * itemSpacing + bottomMargin
        val baseHeight = totalHeight.coerceAtLeast(820)

        val width = ((baseWidth * scaleFactor).toInt() / DISPLAY_RATIO_16_9).coerceAtLeast(1.0)
        val height = ((baseHeight * scaleFactor).toInt() / DISPLAY_RATIO_16_9).coerceAtLeast(1.0)

        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.append("""<svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $baseWidth $baseHeight" id="$svgId" role="img" aria-label="${escapeXml(config.title.ifBlank { "Timeline" })}">""")
        sb.append("""<!-- orientation: vertical -->""")

        appendDefs(sb, svgId)

        sb.append("""<rect width="100%" height="100%" fill="url(#${svgId}_bg)"/>""")
        sb.append("""<rect width="100%" height="100%" fill="url(#${svgId}_grid)" opacity="0.05"/>""")

        sb.append("""<g transform="translate(56,64)">""")
        sb.append("""<text class="title">${escapeXml(config.title.ifBlank { "Timeline" })}</text>""")
        if (config.subtitle.isNotBlank()) {
            sb.append("""<text y="38" class="subtitle">${escapeXml(config.subtitle)}</text>""")
        }
        sb.append("""<line x1="0" y1="64" x2="${baseWidth - 112}" y2="64" class="header-rule"/>""")
        sb.append("</g>")

        sb.append(
            """<line x1="$centerX" y1="$topMargin" x2="$centerX" y2="${baseHeight - 60}" stroke="${theme.accentColor}" stroke-width="3" stroke-dasharray="8 6" stroke-opacity="0.5"/>"""
        )

        var y = 180
        config.events.forEachIndexed { index, event ->
            val color = colors[index % colors.size].color
            val isRight = index % 2 == 0
            val itemHeight = itemHeights[index]
            appendVerticalItem(
                sb = sb,
                svgId = svgId,
                event = event,
                color = color,
                centerX = centerX,
                y = y,
                isRight = isRight,
                cardWidth = maxCardWidth,
                cardHeight = itemHeight,
                index = index
            )
            y += itemHeight + itemSpacing
        }

        sb.append("</svg>")
        return sb.toString()
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateTimelineHorizontal(config: TimelineConfig, scale: String): String {
        val svgId = "timeline_${Uuid.random().toHexString()}"
        val colors = theme.chartPalette
        val scaleFactor = scale.toDoubleOrNull() ?: 1.0

        val leftMargin = 80
        val rightMargin = 80
        val itemSpacing = 240
        val n = config.events.size.coerceAtLeast(1)

        val baseWidth = (leftMargin + rightMargin + (n - 1) * itemSpacing + 280).coerceAtLeast(1200)
        val baseHeight = 560
        val axisY = baseHeight / 2

        val width = (baseWidth * scaleFactor).toInt().coerceAtLeast(1)
        val height = (baseHeight * scaleFactor).toInt().coerceAtLeast(1)

        val sb = StringBuilder()
        sb.append("""<svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $baseWidth $baseHeight" id="$svgId" role="img" aria-label="${escapeXml(config.title.ifBlank { "Timeline" })}">""")
        sb.append("""<!-- orientation: horizontal -->""")

        appendDefs(sb, svgId)

        sb.append("""<rect width="100%" height="100%" fill="url(#${svgId}_bg)"/>""")
        sb.append("""<rect width="100%" height="100%" fill="url(#${svgId}_grid)" opacity="0.05"/>""")

        val lineStartX = leftMargin
        val lineEndX = baseWidth - rightMargin
        sb.append(
            """<rect x="$lineStartX" y="${axisY - 1.0}" width="${lineEndX - lineStartX}" height="2" fill="url(#${svgId}_line)" rx="1.0" opacity="0.6"/>"""
        )

        var x = lineStartX + 120
        config.events.forEachIndexed { index, event ->
            val color = colors[index % colors.size].color
            val above = index % 2 == 0

            val connectorEndY = if (above) axisY - 12 else axisY + 12
            sb.append("""<line x1="$x" y1="$axisY" x2="$x" y2="$connectorEndY" stroke="$color" stroke-width="2" opacity="0.82"/>""")
            sb.append("""<circle cx="$x" cy="$axisY" r="6" fill="$color"/>""")
            sb.append("""<circle cx="$x" cy="$axisY" r="6" fill="none" stroke="$color" stroke-width="2" opacity="0.6">""")
            sb.append("""<animate attributeName="r" from="6" to="20" dur="2s" begin="${index * 0.3}s" repeatCount="indefinite"/>""")
            sb.append("""<animate attributeName="opacity" from="0.6" to="0" dur="2s" begin="${index * 0.3}s" repeatCount="indefinite"/>""")
            sb.append("</circle>")

            val dateText = escapeXml(event.date)
            val dateWidth = estimateTextWidth(dateText, 14) + 24
            val datePillX = x - dateWidth / 2
            val datePillY = if (above) axisY - 38 else axisY + 14

            sb.append(
                """<rect x="$datePillX" y="$datePillY" rx="12" ry="12" width="$dateWidth" height="24" fill="${theme.canvas}" stroke="$color" stroke-width="1.5"/>"""
            )
            sb.append(
                """<text x="$x" y="${datePillY + 17}" text-anchor="middle" font-family="${theme.fontFamily}" font-size="13" font-weight="800" fill="$color">$dateText</text>"""
            )

            val maxTextWidth = 260
            val lines = wrapText(event.text, maxTextWidth)
            val bulletLines = event.bullets.sumOf { wrapText(it, maxTextWidth - 16).size }
            val bulletHeight = if (event.bullets.isNotEmpty()) bulletLines * 16 + 8 else 0
            val textHeight = lines.size * 18
            val cardWidth = maxTextWidth + 32
            val cardHeight = textHeight + 24 + bulletHeight
            val cardX = x - cardWidth / 2
            val cardY = if (above) datePillY - cardHeight - 12 else datePillY + 36

            val textColor = theme.primaryText
            val bulletColor = theme.secondaryText

            sb.append("""<g transform="translate($cardX,$cardY)">""")
            sb.append("""<g class="reveal d${(index % 6) + 1}" filter="url(#${svgId}_cardShadow)">""")
            sb.append("""<rect x="0" y="0" rx="8" ry="8" width="$cardWidth" height="$cardHeight" class="card" stroke="$color" stroke-opacity="0.3"/>""")

            var ty = 24
            lines.forEach { line ->
                appendTextWithLinks(sb, line, 16, ty, textColor, color)
                ty += 18
            }

            if (event.bullets.isNotEmpty()) {
                ty += 4
                event.bullets.forEach { bullet ->
                    val bLines = wrapText(bullet, maxTextWidth - 16)
                    bLines.forEachIndexed { i, bLine ->
                        if (i == 0) {
                            sb.append("""<circle cx="21" cy="${ty - 4}" r="2.5" fill="$bulletColor"/>""")
                            sb.append("""<text x="30" y="$ty" font-family="${theme.fontFamily}" font-size="12" fill="$bulletColor">${escapeXml(bLine)}</text>""")
                        } else {
                            sb.append("""<text x="30" y="$ty" font-family="${theme.fontFamily}" font-size="12" fill="$bulletColor">${escapeXml(bLine)}</text>""")
                        }
                        ty += 16
                    }
                }
            }

            sb.append("</g>")
            sb.append("</g>")

            x += itemSpacing
        }

        sb.append("</svg>")
        return sb.toString()
    }

    private fun appendDefs(sb: StringBuilder, svgId: String) {
        val bgStart = theme.canvas
        val bgEnd = if (isDark) "#0d1629" else "#f8fafc"
        val gridStroke = theme.accentColor
        val text = theme.primaryText
        val muted = theme.secondaryText
        val cardBg = if (isDark) "rgba(30, 41, 59, 0.85)" else "rgba(255, 255, 255, 0.90)"
        val cardStroke = theme.primaryText
        val headerRule = theme.accentColor
        val spineStart = theme.accentColor
        val spineEnd = theme.accentColor
        val shadowOpacity = if (isDark) "0.4" else "0.12"

        sb.append("<defs>")

        sb.append(
            """
            <linearGradient id="${svgId}_bg" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stop-color="$bgStart"/>
              <stop offset="100%" stop-color="$bgEnd"/>
            </linearGradient>
            """.trimIndent()
        )

        sb.append(
            """
            <pattern id="${svgId}_grid" width="32" height="32" patternUnits="userSpaceOnUse">
              <path d="M32 0H0V32" fill="none" stroke="$gridStroke" stroke-width="0.5"/>
            </pattern>
            """.trimIndent()
        )

        sb.append(
            """
            <linearGradient id="${svgId}_line" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stop-color="$spineStart" stop-opacity="0.65"/>
              <stop offset="100%" stop-color="$spineEnd" stop-opacity="0.2"/>
            </linearGradient>
            """.trimIndent()
        )

        sb.append(
            """
            <filter id="${svgId}_cardShadow" x="-30%" y="-30%" width="160%" height="160%">
              <feDropShadow dx="0" dy="12" stdDeviation="12" flood-opacity="$shadowOpacity"/>
            </filter>
            <filter id="${svgId}_nodeGlow" x="-80%" y="-80%" width="260%" height="260%">
              <feGaussianBlur stdDeviation="3" result="b"/>
              <feMerge>
                <feMergeNode in="b"/>
                <feMergeNode in="SourceGraphic"/>
              </feMerge>
            </filter>
            """.trimIndent()
        )

        sb.append(
            """
            <style>
              ${theme.fontImport}
              #$svgId {
                --text: $text;
                --muted: $muted;
                --card-bg: $cardBg;
                --card-stroke: $cardStroke;
              }

              #$svgId .title {
                font: 800 36px ${theme.fontFamily};
                letter-spacing: -0.02em;
                fill: var(--text);
              }

              #$svgId .subtitle {
                font: 400 16px ${theme.fontFamily};
                fill: var(--muted);
              }

              #$svgId .header-rule {
                stroke: $headerRule;
                stroke-opacity: 0.2;
                stroke-width: 1.5;
              }

              #$svgId .date {
                font: 700 14px ${theme.fontFamily};
              }

              #$svgId .body {
                font: 400 14px ${theme.fontFamily};
                fill: var(--muted);
              }

              #$svgId .card {
                fill: var(--card-bg);
                stroke: var(--card-stroke);
                stroke-width: 0.5;
              }

              #$svgId .reveal {
                opacity: 0;
                animation: tl-rise 540ms cubic-bezier(.2,.85,.2,1) forwards;
              }

              #$svgId .d1 { animation-delay: 70ms; }
              #$svgId .d2 { animation-delay: 170ms; }
              #$svgId .d3 { animation-delay: 270ms; }
              #$svgId .d4 { animation-delay: 370ms; }
              #$svgId .d5 { animation-delay: 470ms; }
              #$svgId .d6 { animation-delay: 570ms; }

              @keyframes tl-rise {
                from { opacity: 0; transform: translateY(10px); }
                to   { opacity: 1; transform: translateY(0); }
              }
            </style>
            """.trimIndent()
        )

        sb.append("</defs>")
    }

    private fun appendVerticalItem(
        sb: StringBuilder,
        svgId: String,
        event: TimelineEvent,
        color: String,
        centerX: Int,
        y: Int,
        isRight: Boolean,
        cardWidth: Int,
        cardHeight: Int,
        index: Int
    ) {
        val connector = 80
        val cardX = if (isRight) centerX + connector else centerX - connector - cardWidth
        val cardY = y - 46
        val textX = cardX + 32
        val dateY = cardY + 36
        val bodyStartY = cardY + 60

        val lineEndX = if (isRight) centerX + connector else centerX - connector
        val accentX = if (isRight) cardX + 16 else cardX + 16

        val bodyLines = wrapText(event.text, cardWidth - 64)

        sb.append("""<g transform="translate($centerX,$y)">""")
        sb.append("""<g class="reveal d${(index % 6) + 1}" filter="url(#${svgId}_cardShadow)">""")
        sb.append("""<line x1="0" y1="0" x2="${if (isRight) connector else -connector}" y2="0" stroke="$color" stroke-width="2" opacity="0.3"/>""")
        sb.append("""<circle cx="0" cy="0" r="8" fill="${theme.canvas}" stroke="$color" stroke-width="2.5" filter="url(#${svgId}_nodeGlow)"/>""")
        sb.append("""<circle cx="0" cy="0" r="4" fill="$color"/>""")

        val cardLocalX = if (isRight) connector else -connector - cardWidth
        sb.append("""<rect x="$cardLocalX" y="${cardY - y}" width="$cardWidth" height="$cardHeight" rx="8" class="card" stroke="$color" stroke-opacity="0.3"/>""")
        sb.append("""<rect x="${accentX - centerX}" y="${cardY - y + 16}" width="4" height="${(cardHeight - 32).coerceAtLeast(40)}" rx="2" fill="$color"/>""")
        sb.append("</g>")
        sb.append("</g>")

        sb.append("""<text x="$textX" y="$dateY" class="date" fill="$color">${escapeXml(event.date)}</text>""")

        var ty = bodyStartY
        bodyLines.forEach { line ->
            appendTextWithLinks(sb, line, textX, ty, "var(--text)", color)
            ty += 20
        }

        if (event.bullets.isNotEmpty()) {
            ty += 4
            event.bullets.forEach { bullet ->
                val bulletLines = wrapText(bullet, cardWidth - 72)
                bulletLines.forEachIndexed { i, bLine ->
                    if (i == 0) {
                        sb.append("""<circle cx="${textX + 4}" cy="${ty - 4}" r="2.5" fill="var(--muted)"/>""")
                        sb.append("""<text x="${textX + 16}" y="$ty" font-family="${theme.fontFamily}" font-size="12" fill="var(--muted)">${escapeXml(bLine)}</text>""")
                    } else {
                        sb.append("""<text x="${textX + 16}" y="$ty" font-family="${theme.fontFamily}" font-size="12" fill="var(--muted)">${escapeXml(bLine)}</text>""")
                    }
                    ty += 16
                }
            }
        }
    }

    private fun appendTextWithLinks(
        sb: StringBuilder,
        text: String,
        x: Int,
        y: Int,
        textColor: String,
        linkColor: String
    ) {
        val linkPattern = """\[\[([^\s]+)\s+([^\]]+)\]\]""".toRegex()
        var lastIndex = 0

        sb.append("""<text x="$x" y="$y" font-family="${theme.fontFamily}" font-size="14" fill="$textColor">""")

        linkPattern.findAll(text).forEach { match ->
            if (match.range.first > lastIndex) {
                val beforeText = text.substring(lastIndex, match.range.first)
                sb.append("<tspan>")
                sb.append(escapeXml(beforeText))
                sb.append("</tspan>")
            }

            val url = escapeXml(match.groupValues[1])
            val linkText = escapeXml(match.groupValues[2])
            sb.append("""<tspan fill="$linkColor" text-decoration="underline"><a href="$url" target="_blank">$linkText</a></tspan>""")
            lastIndex = match.range.last + 1
        }

        if (lastIndex < text.length) {
            val remainingText = text.substring(lastIndex)
            sb.append("<tspan>")
            sb.append(escapeXml(remainingText))
            sb.append("</tspan>")
        }

        sb.append("</text>")
    }

    private fun calculateVerticalItemHeight(item: TimelineEvent, maxWidth: Int): Int {
        val lines = wrapText(item.text, maxWidth)
        val textHeight = lines.size * 20
        val bulletLines = item.bullets.sumOf { wrapText(it, maxWidth - 16).size }
        val bulletHeight = if (item.bullets.isNotEmpty()) bulletLines * 16 + 8 else 0
        return (108 + textHeight + bulletHeight).coerceAtLeast(108)
    }

    private fun wrapText(text: String, maxWidth: Int): List<String> {
        val linkPattern = """\[\[([^\s]+)\s+([^\]]+)\]\]""".toRegex()
        val links = mutableListOf<String>()
        var protectedText = text

        linkPattern.findAll(text).forEach { match ->
            val placeholder = "___LINK_${links.size}___"
            links.add(match.value)
            protectedText = protectedText.replace(match.value, placeholder)
        }

        val words = protectedText.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = estimateTextWidth(testLine, 14)

            if (width > maxWidth) {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                    currentLine = word
                } else {
                    lines.add(word)
                }
            } else {
                currentLine = testLine
            }
        }

        if (currentLine.isNotEmpty()) lines.add(currentLine)

        return lines.map { line ->
            var restored = line
            links.forEachIndexed { idx, link -> restored = restored.replace("___LINK_${idx}___", link) }
            restored
        }
    }

    private fun estimateTextWidth(text: String, fontSize: Int): Int {
        val linkPattern = """\[\[([^\s]+)\s+([^\]]+)\]\]""".toRegex()
        val displayText = linkPattern.replace(text) { it.groupValues[2] }
        return (displayText.length * fontSize * 0.58).toInt()
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