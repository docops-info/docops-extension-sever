package io.docops.docopsextensionssupport.scorecard

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.max
import kotlin.text.toInt

/**
 * Maker to generate an iOS-style two-column ScoreCard SVG based on ScoreCard model.
 * This follows the general pattern of other Makers in the project: pure-SVG generation as a String.
 */
class ScoreCardMaker {
    fun make(scorecard: ScoreCard): String {
        val useDark = scorecard.theme.useDark
        val scale = scorecard.theme.scale
        val baseWidth = 1024
        val margin = 40
        val gutter = 44
        val cardWidth = (baseWidth - (margin * 2) - gutter) / 2

        // Theme Configuration
        val theme = if (useDark) DarkTheme() else LightTheme()

        val titleLines = wrapByCharsForTitle(scorecard.title, baseWidth - 80, 32)
        val titleLineHeight = 38
        val topY = 120.0 + (titleLines.size - 1) * titleLineHeight

        // Build Cards
        val beforeCard = buildCard(cardWidth, scorecard.beforeSections, theme, scorecard.id,
            headerTitle = scorecard.beforeTitle.ifBlank { "BEFORE" },
            isBefore = true)

        val afterCard = buildCard(cardWidth, scorecard.afterSections, theme, scorecard.id,
            headerTitle = scorecard.afterTitle.ifBlank { "AFTER" },
            isBefore = false)

        val cardHeightMax = max(beforeCard.height, afterCard.height)
        val baseHeight = cardHeightMax + topY + margin
        val canvasWidth = (baseWidth * scale).toInt()
        val canvasHeight = (baseHeight * scale).toInt()

        return buildString {
            append("""<svg width="$canvasWidth" height="$canvasHeight" viewBox="0 0 $baseWidth $baseHeight" xmlns="http://www.w3.org/2000/svg">""")
            append(generateDefs(theme, scorecard.id))

            // Background
            append("""<rect width="100%" height="100%" fill="${theme.bg}"/>""")
            append("""<rect width="100%" height="100%" fill="url(#grid_${scorecard.id})" opacity="0.4"/>""")

            // Header Title
            append("""<g transform="translate($margin, 60)">""")
            append("""<rect width="4" height="40" fill="${theme.accentPrimary}" rx="2"/>""")
            titleLines.forEachIndexed { i, line ->
                append("""<text x="20" y="${32 + i * titleLineHeight}" class="main-title_${scorecard.id}">${escape(line)}</text>""")
            }
            append("</g>")

            // BEFORE Card Layout
            append("""<g transform="translate($margin, $topY)">""")
            append("""<g class="anim-panel_${scorecard.id} delay-1_${scorecard.id}">${beforeCard.svg}</g>""")
            append("</g>")

            // Transition Arrow
            append("""
                <g transform="translate(${(margin + cardWidth + gutter / 2 - 20)}, ${topY + cardHeightMax / 2})">
                    <path d="M0,0 L40,0 L32,-8 M40,0 L32,8" stroke="${theme.panelStroke}" stroke-width="4" fill="none" stroke-linecap="square" opacity="0.5"/>
                </g>
            """.trimIndent())

            // AFTER Card Layout
            append("""<g transform="translate(${margin + cardWidth + gutter}, $topY)">""")
            append("""<g class="anim-panel_${scorecard.id} delay-2_${scorecard.id}">${afterCard.svg}</g>""")
            append("</g>")

            append("""<text x="$margin" y="${baseHeight - 20}" class="meta-text_${scorecard.id}">SCORECARD_REF: ${scorecard.id.take(4).uppercase()} // SCALE: $scale // THEME: ${if (useDark) "NEURAL_DARK" else "NEURAL_LIGHT"}</text>""")
            append("</svg>")
        }
    }

    private fun generateDefs(theme: ScoreCardThemeColors, id: String) = """
        <defs>
            <pattern id="grid_$id" width="40" height="40" patternUnits="userSpaceOnUse">
                <path d="M 40 0 L 0 0 0 40" fill="none" stroke="${theme.gridColor}" stroke-width="1"/>
            </pattern>
            <style>
                @import url('https://fonts.googleapis.com/css2?family=Syne:wght@800&amp;family=JetBrains+Mono:wght@400;700&amp;display=swap');
                .main-title_$id { font-family: 'Syne', sans-serif; font-size: 32px; fill: ${theme.titleFill}; text-transform: uppercase; letter-spacing: -1px; }
                .sec-header_$id { font-family: 'Syne', sans-serif; font-size: 14px; letter-spacing: 3px; text-transform: uppercase; font-weight: 800; }
                .item-text_$id { font-family: 'JetBrains Mono', monospace; font-size: 13px; fill: ${theme.itemTextFill}; }
                .item-desc_$id { font-family: 'JetBrains Mono', monospace; font-size: 11px; fill: ${theme.itemDescFill}; }
                .meta-text_$id { font-family: 'JetBrains Mono', monospace; font-size: 10px; fill: ${theme.itemDescFill}; opacity: 0.5; }
                
                @keyframes slideUp_$id { 
                    from { opacity: 0; transform: translateY(30px); } 
                    to { opacity: 1; transform: translateY(0); } 
                }
                .anim-panel_$id { animation: slideUp_$id 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards; opacity: 0; }
                .delay-1_$id { animation-delay: 0.1s; }
                .delay-2_$id { animation-delay: 0.3s; }
            </style>
        </defs>
    """.trimIndent()

    private fun buildCard(width: Int, sections: List<Section>, theme: ScoreCardThemeColors, id: String, headerTitle: String, isBefore: Boolean): BuiltCard {
        val accent = if (isBefore) theme.accentBefore else theme.accentAfter
        var currentY = 60
        val innerPadding = 24
        val contentWidth = width - innerPadding * 2 - 30

        val body = buildString {
            sections.forEach { section ->
                if (section.items.isEmpty()) return@forEach
                append("""<text x="$innerPadding" y="${currentY + 20}" class="sec-header_$id" style="fill: $accent">${escape(section.title)}</text>""")
                currentY += 45

                section.items.forEach { item ->
                    val lines = wrapByChars(item.displayText, contentWidth)
                    val descLines = if (!item.description.isNullOrBlank()) wrapByChars("// ${item.description}", contentWidth) else emptyList()

                    if (isBefore) {
                        append("""<circle cx="${innerPadding + 8}" cy="${currentY + 10}" r="4" fill="$accent"/>""")
                    } else {
                        append("""<path d="M${innerPadding},${currentY + 10} L${innerPadding + 6},${currentY + 16} L${innerPadding + 16},${currentY + 6}" stroke="$accent" stroke-width="2.5" fill="none" stroke-linecap="round"/>""")
                    }

                    lines.forEachIndexed { i, line ->
                        append("""<text x="${innerPadding + 28}" y="${currentY + 14 + i * 18}" class="item-text_$id">${escape(line)}</text>""")
                    }
                    currentY += lines.size * 18 + 4

                    descLines.forEachIndexed { i, line ->
                        append("""<text x="${innerPadding + 28}" y="${currentY + 10 + i * 14}" class="item-desc_$id">${escape(line)}</text>""")
                    }
                    currentY += descLines.size * 14 + 12
                }
                currentY += 20
            }
        }

        val totalHeight = max(currentY + innerPadding, 200)
        val svg = """
            <rect width="$width" height="$totalHeight" fill="${theme.panelFill}" stroke="${accent}" stroke-width="1.5" rx="4"/>
            <rect width="$width" height="40" fill="$accent" fill-opacity="0.1" rx="4"/>
            <text x="$innerPadding" y="26" class="sec-header_$id" style="fill: $accent">0${if (isBefore) 1 else 2}_${escape(headerTitle)}</text>
            $body
        """.trimIndent()

        return BuiltCard(svg, totalHeight)
    }

    private data class BuiltCard(val svg: String, val height: Int)

    private fun wrapByWords(text: String, maxChars: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (word in words) {
            if (current.isEmpty()) {
                current.append(word)
            } else if (current.length + 1 + word.length <= maxChars) {
                current.append(" ").append(word)
            } else {
                lines.add(current.toString())
                current = StringBuilder(word)
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())
        return lines
    }

    private fun wrapByChars(text: String, maxWidthPx: Int): List<String> {
        val pxPerChar = 7.8
        return wrapByWords(text, (maxWidthPx / pxPerChar).toInt().coerceAtLeast(10))
    }

    private fun wrapByCharsForTitle(text: String, maxWidthPx: Int, fontSize: Int): List<String> {
        val pxPerChar = fontSize * 0.65
        return wrapByWords(text, (maxWidthPx / pxPerChar).toInt().coerceAtLeast(8))
    }

    private fun escape(text: String): String = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

    private interface ScoreCardThemeColors {
        val bg: String; val panelFill: String; val panelStroke: String; val gridColor: String
        val titleFill: String; val itemTextFill: String; val itemDescFill: String
        val accentPrimary: String; val accentBefore: String; val accentAfter: String
    }

    private class DarkTheme : ScoreCardThemeColors {
        override val bg = "#020617"; override val panelFill = "#0f172a"; override val panelStroke = "#1e293b"; override val gridColor = "#1e293b"
        override val titleFill = "#f8fafc"; override val itemTextFill = "#cbd5e1"; override val itemDescFill = "#64748b"
        override val accentPrimary = "#38bdf8"; override val accentBefore = "#f43f5e"; override val accentAfter = "#10b981"
    }

    private class LightTheme : ScoreCardThemeColors {
        override val bg = "#f8fafc"; override val panelFill = "#ffffff"; override val panelStroke = "#e2e8f0"; override val gridColor = "#e2e8f0"
        override val titleFill = "#0f172a"; override val itemTextFill = "#1e293b"; override val itemDescFill = "#94a3b8"
        override val accentPrimary = "#0284c7"; override val accentBefore = "#e11d48"; override val accentAfter = "#059669"
    }
}
