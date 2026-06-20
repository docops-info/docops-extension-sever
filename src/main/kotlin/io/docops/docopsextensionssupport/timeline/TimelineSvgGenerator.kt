package io.docops.docopsextensionssupport.timeline

import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import kotlin.math.max
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TimelineSvgGenerator {

    data class TimelineColor(val stroke: String, val text: String)

    private val darkModeColors = listOf(
        TimelineColor("#00d4ff", "#00d4ff"),
        TimelineColor("#4f8cff", "#4f8cff"),
        TimelineColor("#b58cff", "#b58cff"),
        TimelineColor("#ff4fd8", "#ff4fd8"),
        TimelineColor("#22d3ee", "#22d3ee")
    )

    private val lightModeColors = listOf(
        TimelineColor("#00b7d6", "#00b7d6"),
        TimelineColor("#2563eb", "#2563eb"),
        TimelineColor("#7c3aed", "#7c3aed"),
        TimelineColor("#db2777", "#db2777"),
        TimelineColor("#0891b2", "#0891b2")
    )

    @OptIn(ExperimentalUuidApi::class)
    fun generateTimeline(config: TimelineConfig, isDarkMode: Boolean = false, scale: String): String {
        return if (config.orientation == Orientation.HORIZONTAL) {
            generateTimelineHorizontal(config, isDarkMode, scale)
        } else {
            generateTimelineVertical(config, isDarkMode, scale)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateTimelineVertical(config: TimelineConfig, isDarkMode: Boolean, scale: String): String {
        val svgId = "timeline_${Uuid.random().toHexString()}"
        val colors = if (isDarkMode) darkModeColors else lightModeColors
        val scaleFactor = scale.toDoubleOrNull() ?: 1.0

        val baseWidth = 1200
        val topMargin = 130
        val bottomMargin = 100
        val itemSpacing = 150
        val centerX = baseWidth / 2
        val maxCardWidth = 428

        val itemHeights = config.events.map { calculateVerticalItemHeight(it, maxCardWidth - 40) }
        val totalHeight = topMargin + itemHeights.sum() + (config.events.size - 1).coerceAtLeast(0) * itemSpacing + bottomMargin
        val baseHeight = totalHeight.coerceAtLeast(820)

        val width = ((baseWidth * scaleFactor).toInt() / DISPLAY_RATIO_16_9).coerceAtLeast(1.0)
        val height = ((baseHeight * scaleFactor).toInt() / DISPLAY_RATIO_16_9).coerceAtLeast(1.0)

        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.append("""<svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $baseWidth $baseHeight" id="$svgId" role="img" aria-label="${escapeXml(config.title.ifBlank { "Timeline" })}">""")
        sb.append("""<!-- orientation: vertical -->""")

        appendDefs(sb, svgId, isDarkMode)

        sb.append("""<rect width="100%" height="100%" fill="url(#${svgId}_bg)"/>""")
        sb.append("""<rect width="100%" height="100%" fill="url(#${svgId}_grid)"/>""")
        sb.append("""<rect width="100%" height="100%" fill="url(#${svgId}_washA)"/>""")
        sb.append("""<rect width="100%" height="100%" fill="url(#${svgId}_washB)"/>""")

        sb.append("""<g transform="translate(56,64)">""")
        sb.append("""<text class="title">${escapeXml(config.title.ifBlank { "Timeline" })}</text>""")
        if (config.subtitle.isNotBlank()) {
            sb.append("""<text y="30" class="subtitle">${escapeXml(config.subtitle)}</text>""")
        }
        sb.append("""<line x1="0" y1="52" x2="${baseWidth - 112}" y2="52" class="header-rule"/>""")
        sb.append("</g>")

        sb.append(
            """<line x1="$centerX" y1="$topMargin" x2="$centerX" y2="${baseHeight - 60}" stroke="url(#${svgId}_spine)" stroke-width="3" stroke-dasharray="8 6"/>"""
        )

        var y = 170
        config.events.forEachIndexed { index, event ->
            val color = colors[index % colors.size]
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
    private fun generateTimelineHorizontal(config: TimelineConfig, isDarkMode: Boolean, scale: String): String {
        val svgId = "timeline_${Uuid.random().toHexString()}"
        val colors = if (isDarkMode) darkModeColors else lightModeColors
        val scaleFactor = scale.toDoubleOrNull() ?: 1.0

        val leftMargin = 80
        val rightMargin = 80
        val itemSpacing = 220
        val n = config.events.size.coerceAtLeast(1)

        val baseWidth = (leftMargin + rightMargin + (n - 1) * itemSpacing + 220).coerceAtLeast(1200)
        val baseHeight = 560
        val axisY = baseHeight / 2

        val width = (baseWidth * scaleFactor).toInt().coerceAtLeast(1)
        val height = (baseHeight * scaleFactor).toInt().coerceAtLeast(1)

        val sb = StringBuilder()
        sb.append("""<svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $baseWidth $baseHeight" id="$svgId" role="img" aria-label="${escapeXml(config.title.ifBlank { "Timeline" })}">""")
        sb.append("""<!-- orientation: horizontal -->""")

        appendDefs(sb, svgId, isDarkMode)

        sb.append("""<rect width="100%" height="100%" fill="url(#${svgId}_bg)"/>""")
        sb.append("""<rect width="100%" height="100%" fill="url(#${svgId}_grid)"/>""")
        sb.append("""<rect width="100%" height="100%" fill="url(#${svgId}_washA)"/>""")
        sb.append("""<rect width="100%" height="100%" fill="url(#${svgId}_washB)"/>""")

        val lineStartX = leftMargin
        val lineEndX = baseWidth - rightMargin
        sb.append(
            """<rect x="$lineStartX" y="${axisY - 1.5}" width="${lineEndX - lineStartX}" height="3" fill="url(#${svgId}_line)" rx="1.5" opacity="0.75"/>"""
        )

        var x = lineStartX + 100
        config.events.forEachIndexed { index, event ->
            val color = colors[index % colors.size]
            val above = index % 2 == 0

            val connectorEndY = if (above) axisY - 12 else axisY + 12
            sb.append("""<line x1="$x" y1="$axisY" x2="$x" y2="$connectorEndY" stroke="${color.stroke}" stroke-width="2" opacity="0.82"/>""")
            sb.append("""<circle cx="$x" cy="$axisY" r="6" fill="${color.stroke}"/>""")
            sb.append("""<circle cx="$x" cy="$axisY" r="6" fill="none" stroke="${color.stroke}" stroke-width="2" opacity="0.6">""")
            sb.append("""<animate attributeName="r" from="6" to="20" dur="2s" begin="${index * 0.3}s" repeatCount="indefinite"/>""")
            sb.append("""<animate attributeName="opacity" from="0.6" to="0" dur="2s" begin="${index * 0.3}s" repeatCount="indefinite"/>""")
            sb.append("</circle>")

            val dateText = escapeXml(event.date)
            val dateWidth = estimateTextWidth(dateText, 13) + 20
            val datePillX = x - dateWidth / 2
            val datePillY = if (above) axisY - 38 else axisY + 14

            sb.append(
                """<rect x="$datePillX" y="$datePillY" rx="4" ry="4" width="$dateWidth" height="24" fill="${if (isDarkMode) "#0b1528" else "#ffffff"}" opacity="0.95" stroke="${color.stroke}" stroke-width="1.5"/>"""
            )
            sb.append(
                """<text x="$x" y="${datePillY + 17}" text-anchor="middle" font-family="'Inter', -apple-system, sans-serif" font-size="13" font-weight="800" fill="${color.text}">$dateText</text>"""
            )

            val maxTextWidth = 260
            val lines = wrapText(event.text, maxTextWidth)
            val bulletLines = event.bullets.sumOf { wrapText(it, maxTextWidth - 16).size }
            val bulletHeight = if (event.bullets.isNotEmpty()) bulletLines * 16 + 8 else 0
            val textHeight = lines.size * 18
            val cardWidth = maxTextWidth + 24
            val cardHeight = textHeight + 24 + bulletHeight
            val cardX = x - cardWidth / 2
            val cardY = if (above) datePillY - cardHeight - 10 else datePillY + 34

            val textColor = if (isDarkMode) "#e8f2ff" else "#1f2f4a"
            val bulletColor = if (isDarkMode) "#9db3d6" else "#5d7598"

            sb.append("""<g transform="translate($cardX,$cardY)">""")
            sb.append("""<g class="reveal d${(index % 6) + 1}">""")
            sb.append("""<rect x="0" y="0" rx="6" ry="6" width="$cardWidth" height="$cardHeight" class="card" stroke="${color.stroke}"/>""")

            var ty = 20
            lines.forEach { line ->
                appendTextWithLinks(sb, line, 12, ty, textColor, color.text)
                ty += 18
            }

            if (event.bullets.isNotEmpty()) {
                ty += 4
                event.bullets.forEach { bullet ->
                    val bLines = wrapText(bullet, maxTextWidth - 16)
                    bLines.forEachIndexed { i, bLine ->
                        if (i == 0) {
                            sb.append("""<circle cx="17" cy="${ty - 4}" r="2.5" fill="$bulletColor"/>""")
                            sb.append("""<text x="26" y="$ty" font-family="'Inter', -apple-system, sans-serif" font-size="12" fill="$bulletColor">${escapeXml(bLine)}</text>""")
                        } else {
                            sb.append("""<text x="26" y="$ty" font-family="'Inter', -apple-system, sans-serif" font-size="12" fill="$bulletColor">${escapeXml(bLine)}</text>""")
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

    private fun appendDefs(sb: StringBuilder, svgId: String, isDarkMode: Boolean) {
        val bgStart = if (isDarkMode) "#060b16" else "#f7fbff"
        val bgEnd = if (isDarkMode) "#0d1629" else "#edf3ff"
        val gridStroke = if (isDarkMode) "rgba(171,196,239,0.12)" else "rgba(33,76,140,0.08)"
        val washA = if (isDarkMode) "#00d4ff" else "#00b7d6"
        val washB = if (isDarkMode) "#ff4fd8" else "#7c3aed"
        val text = if (isDarkMode) "#eaf3ff" else "#102542"
        val muted = if (isDarkMode) "#9eb2d4" else "#4d678d"
        val cardBg = if (isDarkMode) "rgba(16,28,49,0.80)" else "rgba(255,255,255,0.78)"
        val cardStroke = if (isDarkMode) "rgba(138,171,225,0.38)" else "rgba(112,149,205,0.45)"
        val headerRule = if (isDarkMode) "rgba(124,156,212,0.26)" else "rgba(44,88,148,0.22)"
        val spineStart = if (isDarkMode) "#00d4ff" else "#00b7d6"
        val spineEnd = if (isDarkMode) "#4f8cff" else "#2563eb"
        val shadowOpacity = if (isDarkMode) "0.28" else "0.14"

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
            <radialGradient id="${svgId}_washA" cx="14%" cy="14%" r="44%">
              <stop offset="0%" stop-color="$washA" stop-opacity="${if (isDarkMode) "0.18" else "0.15"}"/>
              <stop offset="100%" stop-color="$washA" stop-opacity="0"/>
            </radialGradient>
            """.trimIndent()
        )

        sb.append(
            """
            <radialGradient id="${svgId}_washB" cx="88%" cy="86%" r="42%">
              <stop offset="0%" stop-color="$washB" stop-opacity="${if (isDarkMode) "0.14" else "0.12"}"/>
              <stop offset="100%" stop-color="$washB" stop-opacity="0"/>
            </radialGradient>
            """.trimIndent()
        )

        sb.append(
            """
            <pattern id="${svgId}_grid" width="28" height="28" patternUnits="userSpaceOnUse">
              <path d="M28 0H0V28" fill="none" stroke="$gridStroke" stroke-width="1"/>
            </pattern>
            """.trimIndent()
        )

        sb.append(
            """
            <linearGradient id="${svgId}_spine" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stop-color="$spineStart"/>
              <stop offset="100%" stop-color="$spineEnd"/>
            </linearGradient>
            """.trimIndent()
        )

        sb.append(
            """
            <linearGradient id="${svgId}_line" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stop-color="$spineStart" stop-opacity="0.65"/>
              <stop offset="100%" stop-color="$spineEnd" stop-opacity="0.65"/>
            </linearGradient>
            """.trimIndent()
        )

        sb.append(
            """
            <filter id="${svgId}_cardShadow" x="-30%" y="-30%" width="160%" height="160%">
              <feDropShadow dx="0" dy="8" stdDeviation="10" flood-opacity="$shadowOpacity"/>
            </filter>
            <filter id="${svgId}_nodeGlow" x="-80%" y="-80%" width="260%" height="260%">
              <feGaussianBlur stdDeviation="3.4" result="b"/>
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
              #$svgId {
                --text: $text;
                --muted: $muted;
                --card-bg: $cardBg;
                --card-stroke: $cardStroke;
              }

              #$svgId .title {
                font: 800 42px Inter, system-ui, sans-serif;
                letter-spacing: -0.01em;
                fill: var(--text);
              }

              #$svgId .subtitle {
                font: 500 15px Inter, system-ui, sans-serif;
                fill: var(--muted);
              }

              #$svgId .header-rule {
                stroke: $headerRule;
              }

              #$svgId .date {
                font: 800 18px Inter, system-ui, sans-serif;
              }

              #$svgId .body {
                font: 400 13px Inter, system-ui, sans-serif;
                fill: var(--muted);
              }

              #$svgId .card {
                fill: var(--card-bg);
                stroke: var(--card-stroke);
                stroke-width: 1;
                filter: url(#${svgId}_cardShadow);
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
        color: TimelineColor,
        centerX: Int,
        y: Int,
        isRight: Boolean,
        cardWidth: Int,
        cardHeight: Int,
        index: Int
    ) {
        val connector = 86
        val cardX = if (isRight) centerX + connector else centerX - connector - cardWidth
        val cardY = y - 46
        val textX = cardX + 30
        val dateY = cardY + 36
        val bodyStartY = cardY + 60

        val lineEndX = if (isRight) centerX + connector else centerX - connector
        val accentX = if (isRight) cardX + 16 else cardX + 16

        val bodyLines = wrapText(event.text, cardWidth - 58)

        // ... existing code ...
        sb.append("""<g transform="translate($centerX,$y)">""")
        sb.append("""<g class="reveal d${(index % 6) + 1}">""")
        sb.append("""<line x1="0" y1="0" x2="${if (isRight) connector else -connector}" y2="0" stroke="${color.stroke}" stroke-width="2" opacity="0.55"/>""")
        sb.append("""<circle cx="0" cy="0" r="11" fill="var(--card-bg)" stroke="${color.stroke}" stroke-width="3" filter="url(#${svgId}_nodeGlow)"/>""")
        sb.append("""<circle cx="0" cy="0" r="5" fill="${color.stroke}"/>""")

        val cardLocalX = if (isRight) connector else -connector - cardWidth
        sb.append("""<rect x="$cardLocalX" y="${cardY - y}" width="$cardWidth" height="$cardHeight" rx="14" class="card"/>""")
        sb.append("""<rect x="${accentX - centerX}" y="${cardY - y + 16}" width="4" height="${(cardHeight - 32).coerceAtLeast(40)}" rx="2" fill="${color.stroke}"/>""")
        sb.append("</g>")
        sb.append("</g>")

        sb.append("""<text x="$textX" y="$dateY" class="date" fill="${color.text}">${escapeXml(event.date)}</text>""")

        var ty = bodyStartY
        bodyLines.forEach { line ->
            appendTextWithLinks(sb, line, textX, ty, "var(--muted)", color.text)
            ty += 20
        }

        if (event.bullets.isNotEmpty()) {
            ty += 2
            event.bullets.forEach { bullet ->
                val bulletLines = wrapText(bullet, cardWidth - 72)
                bulletLines.forEachIndexed { i, bLine ->
                    if (i == 0) {
                        sb.append("""<circle cx="${textX + 3}" cy="${ty - 4}" r="2.5" fill="var(--muted)"/>""")
                        sb.append("""<text x="${textX + 14}" y="$ty" font-family="'Inter', -apple-system, sans-serif" font-size="12" fill="var(--muted)">${escapeXml(bLine)}</text>""")
                    } else {
                        sb.append("""<text x="${textX + 14}" y="$ty" font-family="'Inter', -apple-system, sans-serif" font-size="12" fill="var(--muted)">${escapeXml(bLine)}</text>""")
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

        sb.append("""<text x="$x" y="$y" font-family="'Inter', -apple-system, sans-serif" font-size="13" fill="$textColor">""")

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
        val bulletLines = item.bullets.sumOf { wrapText(it, maxWidth - 14).size }
        val bulletHeight = if (item.bullets.isNotEmpty()) bulletLines * 16 + 6 else 0
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
            val width = estimateTextWidth(testLine, 13)

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