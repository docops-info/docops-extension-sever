package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.support.ThemeFactory
import kotlin.math.max

/**
 * Maker to generate an iOS-style two-column ScoreCard SVG based on ScoreCard model.
 * This follows the general pattern of other Makers in the project: pure-SVG generation as a String.
 */
class ScoreCardMaker(val useDark: Boolean, val scale: Float = 1.0f) {

    private var theme = ThemeFactory.getThemeByName("premium", useDark)
    fun make(scorecard: ScoreCard): String {
        println(scorecard.theme)
        theme = ThemeFactory.getThemeByName(scorecard.theme, useDark)
        val isPremium = theme.name.contains("Premium")
        val baseWidth = 1024
        val margin = if (isPremium) 32 else 40
        val gutter = if (isPremium) 32 else 44
        val cardWidth = (baseWidth - (margin * 2) - gutter) / 2

        // Theme Configuration

        val titleFontSize = if (isPremium) 36 else 26
        val titleLines = wrapByCharsForTitle(scorecard.title, baseWidth - (margin * 2), titleFontSize)
        val titleLineHeight = if (isPremium) 44 else 32
        val topY = (if (isPremium) 120.0 else 110.0) + (titleLines.size - 1) * titleLineHeight

        // Build Cards
        val beforeCard = buildCard(cardWidth, scorecard.beforeSections,  scorecard.id,
            headerTitle = scorecard.beforeTitle.ifBlank { "BEFORE" },
            isBefore = true)

        val afterCard = buildCard(cardWidth, scorecard.afterSections, scorecard.id,
            headerTitle = scorecard.afterTitle.ifBlank { "AFTER" },
            isBefore = false)

        val cardHeightMax = max(beforeCard.height, afterCard.height)
        val baseHeight = cardHeightMax + topY + margin
        val canvasWidth = (baseWidth * scale).toInt()
        val canvasHeight = (baseHeight * scale).toInt()

        return buildString {
            append("""<svg width="$canvasWidth" height="$canvasHeight" viewBox="0 0 $baseWidth $baseHeight" xmlns="http://www.w3.org/2000/svg">""")
            append(generateDefs( scorecard.id))

            // Background
            append("""<rect width="100%" height="100%" fill="${theme.canvas}"/>""")
            val gridOpacity = if (isPremium) "0.05" else "0.4"
            append("""<rect width="100%" height="100%" fill="url(#grid_${scorecard.id})" opacity="$gridOpacity"/>""")

            // Header Title
            append("""<g transform="translate($margin, 60)">""")
            val titleIndicatorWidth = if (isPremium) 6 else 4
            val titleIndicatorHeight = if (isPremium) 48 else 40
            append("""<rect x="0" y="8" width="$titleIndicatorWidth" height="$titleIndicatorHeight" fill="${theme.accentColor}" rx="${if (isPremium) 3 else 2}"/>""")
            titleLines.forEachIndexed { i, line ->
                append("""<text x="${if (isPremium) 24 else 16}" y="${(if (isPremium) 38 else 28) + i * titleLineHeight}" class="main-title_${scorecard.id}">${escape(line)}</text>""")
            }
            append("</g>")


            // BEFORE Card Layout
            append("""<g transform="translate($margin, $topY)">""")
            if (isPremium) {
                append("""<g filter="url(#premiumShadow_${scorecard.id})">""")
            }
            append("""<g class="anim-panel_${scorecard.id} delay-1_${scorecard.id}">${beforeCard.svg}</g>""")
            if (isPremium) {
                append("</g>")
            }
            append("</g>")

            // Transition Arrow
            val arrowY = topY + cardHeightMax / 2
            val arrowLength = if (isPremium) 24 else 36
            val arrowX = margin + cardWidth + (gutter - arrowLength) / 2
            append("""
                <g transform="translate($arrowX, $arrowY)">
                    <path d="M0,0 L$arrowLength,0 L${arrowLength - 8},-8 M$arrowLength,0 L${arrowLength - 8},8" stroke="${theme.accentColor}" stroke-width="${if (isPremium) 2 else 4}" fill="none" stroke-linecap="${if (isPremium) "round" else "square"}" opacity="${if (isPremium) 0.8 else 0.5}"/>
                </g>
            """.trimIndent())

            // AFTER Card Layout
            append("""<g transform="translate(${margin + cardWidth + gutter}, $topY)">""")
            if (isPremium) {
                append("""<g filter="url(#premiumShadow_${scorecard.id})">""")
            }
            append("""<g class="anim-panel_${scorecard.id} delay-2_${scorecard.id}">${afterCard.svg}</g>""")
            if (isPremium) {
                append("</g>")
            }
            append("</g>")

            append("""<text x="$margin" y="${baseHeight - 20}" class="meta-text_${scorecard.id}">SCORECARD_REF: ${scorecard.id.take(4).uppercase()} // SCALE: $scale // THEME: ${theme.name}</text>""")
            append("</svg>")
        }
    }

    private fun generateDefs(id: String): String {
        val isPremium = theme.name.contains("Premium")
        val shadowColor = if (useDark) "#000000" else "#0F172A"
        val shadowOpacity = if (useDark) "0.6" else "0.12"
        val titleFontSize = if (isPremium) 36 else 16
        val titleWeight = if (isPremium) 800 else 800
        val titleCaps = if (isPremium) "none" else "uppercase"
        
        return """
            <defs>
                <pattern id="grid_$id" width="40" height="40" patternUnits="userSpaceOnUse">
                    <path d="M 40 0 L 0 0 0 40" fill="none" stroke="${theme.accentColor}" stroke-width="1"/>
                </pattern>
                <filter id="premiumShadow_$id" x="-20%" y="-20%" width="140%" height="140%">
                    <feDropShadow dx="0" dy="12" stdDeviation="12" flood-color="$shadowColor" flood-opacity="$shadowOpacity"/>
                </filter>
                <style>
                    ${theme.fontImport}
                    .main-title_$id { font-family: ${theme.fontFamily}; font-size: ${titleFontSize / theme.fontWidthMultiplier}px; fill: ${theme.primaryText}; text-transform: $titleCaps; letter-spacing: -0.5px; font-weight: $titleWeight; }
                    .sec-header_$id { font-family: ${theme.fontFamily}; font-size: ${(if (isPremium) 16 else 14) / theme.fontWidthMultiplier}px; letter-spacing: ${if (isPremium) 0 else 2}px; text-transform: ${if (isPremium) "none" else "uppercase"}; font-weight: 700; }
                    .item-text_$id { font-family: ${theme.fontFamily}; font-size: ${if (isPremium) 14 else 13}px; fill: ${theme.primaryText}; font-weight: ${if (isPremium) 500 else 400}; }
                    .item-desc_$id { font-family: ${theme.fontFamily}; font-size: ${if (isPremium) 12 else 11}px; fill: ${theme.secondaryText}; font-weight: 400; }
                    .meta-text_$id { font-family: ${theme.fontFamily}, monospace; font-size: 10px; fill: ${theme.secondaryText}; opacity: 0.5; }
                
                @keyframes slideUp_$id { 
                    from { opacity: 0; transform: translateY(30px); } 
                    to { opacity: 1; transform: translateY(0); } 
                }
                .anim-panel_$id { animation: slideUp_$id 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards; opacity: 1; }
                .delay-1_$id { animation-delay: 0.1s; }
                .delay-2_$id { animation-delay: 0.3s; }
            </style>
        </defs>
    """.trimIndent()
    }

    private fun buildCard(width: Int, sections: List<Section>, id: String, headerTitle: String, isBefore: Boolean): BuiltCard {
        val isPremium = theme.name.contains("Premium")
        val accent = if (isPremium) {
            if (isBefore) "#64748b" else "#3B82F6"
        } else {
            if (isBefore) theme.accentColor else "#10b981"
        }
        var currentY = 60
        val innerPadding = if (isPremium) 32 else 24
        val contentWidth = width - innerPadding * 2 - 30

        val body = buildString {
            sections.forEach { section ->
                if (section.items.isEmpty()) return@forEach
                append("""<text x="$innerPadding" y="${currentY + 20}" class="sec-header_$id" style="fill: ${if (isPremium) theme.primaryText else accent}">${escape(section.title)}</text>""")
                currentY += if (isPremium) 48 else 45

                section.items.forEach { item ->
                    val lines = wrapByChars(item.displayText, contentWidth)
                    val descLines = if (!item.description.isNullOrBlank()) wrapByChars(if (isPremium) item.description else "// ${item.description}", contentWidth) else emptyList()

                    if (isBefore) {
                        append("""<circle cx="${innerPadding + 8}" cy="${currentY + 10}" r="${if (isPremium) 3 else 4}" fill="$accent"/>""")
                    } else {
                        if (isPremium) {
                            append("""<path d="M${innerPadding},${currentY + 10} L${innerPadding + 6},${currentY + 16} L${innerPadding + 16},${currentY + 6}" stroke="$accent" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>""")
                        } else {
                            append("""<path d="M${innerPadding},${currentY + 10} L${innerPadding + 6},${currentY + 16} L${innerPadding + 16},${currentY + 6}" stroke="$accent" stroke-width="2.5" fill="none" stroke-linecap="round"/>""")
                        }
                    }

                    lines.forEachIndexed { i, line ->
                        append("""<text x="${innerPadding + 28}" y="${currentY + 14 + i * (if (isPremium) 20 else 18)}" class="item-text_$id">${escape(line)}</text>""")
                    }
                    currentY += lines.size * (if (isPremium) 20 else 18) + (if (isPremium) 6 else 4)

                    descLines.forEachIndexed { i, line ->
                        append("""<text x="${innerPadding + 28}" y="${currentY + (if (isPremium) 12 else 10) + i * (if (isPremium) 16 else 14)}" class="item-desc_$id">${escape(line)}</text>""")
                    }
                    currentY += descLines.size * (if (isPremium) 16 else 14) + (if (isPremium) 16 else 12)
                }
                currentY += 20
            }
        }

        val totalHeight = max(currentY + innerPadding, 200)
        val rx = if (isPremium) 8 else 4
        
        val headerText = if (isPremium) {
            escape(headerTitle)
        } else {
            "0${if (isBefore) 1 else 2}_${escape(headerTitle)}"
        }

        val svg = """
            ${if (isPremium) """
            <linearGradient id="cardGrad_${id}_${isBefore}" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="${theme.canvas}"/>
                <stop offset="100%" stop-color="${if (useDark) "#1e293b" else "#f8fafc"}"/>
            </linearGradient>
            """.trimIndent() else ""}
            <rect width="$width" height="$totalHeight" fill="${if (isPremium) "url(#cardGrad_${id}_${isBefore})" else theme.canvas}" stroke="${if (isPremium) "none" else accent}" stroke-width="1.5" rx="$rx"/>
            <rect width="$width" height="40" fill="$accent" fill-opacity="${if (isPremium) 0.08 else 0.1}" rx="$rx"/>
            <text x="$innerPadding" y="26" class="sec-header_$id" style="fill: $accent">$headerText</text>
            $body
            ${if (isPremium) """<rect width="$width" height="$totalHeight" fill="none" stroke="${theme.primaryText}" stroke-opacity="0.05" stroke-width="1" rx="$rx"/>""" else ""}
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
