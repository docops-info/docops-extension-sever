package io.docops.docopsextensionssupport.scorecard

import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Maker to generate an iOS-style two-column ScoreCard SVG based on ScoreCard model.
 * This follows the general pattern of other Makers in the project: pure-SVG generation as a String.
 */
class ScoreCardMaker {
    private val log = KotlinLogging.logger {}

    fun make(scorecard: ScoreCard): String {
        val useDark = scorecard.theme.useDark
        val scale = scorecard.theme.scale
        // Overall canvas settings adjusted for wider layout
        val baseWidth = 1024
        val margin = 40
        val gutter = 40
        // Two cards side by side within width
        val cardWidth = ((baseWidth - (margin * 2) - gutter) / 2)
        // Title wrapping configuration
        val titleFontSize = 20
        val titleLineHeight = 24
        val titleMaxWidth = baseWidth - (margin * 2)
        val titleLines = wrapByCharsForTitle(scorecard.title, titleMaxWidth, titleFontSize)
        val titleBlockHeight = titleLines.size * titleLineHeight
        val titleStartY = 28
        val titleEndY = titleStartY + titleBlockHeight

        val topY = titleEndY + 20 // start cards after title block
        val leftX = margin
        val rightX = margin + cardWidth + gutter

        // Theme colors
        val bg = if (useDark) "#0f172a" else "#f8f9fa"
        val panelFill = if (useDark) "#111827" else "white"
        val panelStroke = if (useDark) "#334155" else "#ddd"
        val sectionHeaderTextFill = if (useDark) "#e5e7eb" else "#333"
        val itemTextFill = if (useDark) "#cbd5e1" else "#666"
        val titleFill = if (useDark) "#e5e7eb" else "#333"

        // Compute dynamic card heights based on wrapped content
        val beforeCard = buildCard(cardWidth, scorecard.beforeSections, "url(#redGrad)",
            headerTitle = scorecard.beforeTitle.ifBlank { scorecard.beforeSections.firstOrNull()?.title ?: scorecard.beforeTitle },
            bulletColor = "#ff4444", checkmark = false,
            panelFill = panelFill, panelStroke = panelStroke, sectionHeaderTextFill = sectionHeaderTextFill, itemTextFill = itemTextFill)
        val afterCard = buildCard(cardWidth, scorecard.afterSections, "url(#greenGrad)",
            headerTitle = (scorecard.afterTitle.ifBlank { scorecard.afterSections.firstOrNull()?.title ?: scorecard.afterTitle }),
            bulletColor = "#22cc44", checkmark = true,
            panelFill = panelFill, panelStroke = panelStroke, sectionHeaderTextFill = sectionHeaderTextFill, itemTextFill = itemTextFill)
        val cardHeightLeft = beforeCard.height
        val cardHeightRight = afterCard.height
        val cardHeightMax = maxOf(cardHeightLeft, cardHeightRight)
        val baseHeight = cardHeightMax + topY + margin
        val canvasWidth = (baseWidth * scale).toInt()
        val canvasHeight = (baseHeight * scale).toInt()

        val sb = StringBuilder()
        sb.append("""
            <svg width="$canvasWidth" height="$canvasHeight" viewBox="0 0 $baseWidth $baseHeight" xmlns="http://www.w3.org/2000/svg">
                <defs>
                    <linearGradient id="redGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" style="stop-color:#ff6b6b;stop-opacity:1" />
                        <stop offset="100%" style="stop-color:#ee5253;stop-opacity:1" />
                    </linearGradient>
                    <linearGradient id="greenGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" style="stop-color:#4ecdc4;stop-opacity:1" />
                        <stop offset="100%" style="stop-color:#26a69a;stop-opacity:1" />
                    </linearGradient>
                    <filter id="shadow"><feDropShadow dx="2" dy="2" stdDeviation="3" flood-opacity="0.25"/></filter>
                </defs>
                <rect width="$baseWidth" height="$baseHeight" fill="$bg"/>
        """.trimIndent())
        // Render wrapped title lines centered
        titleLines.forEachIndexed { idx: Int, line: String ->
            val y = titleStartY + (idx * titleLineHeight)
            sb.append("""
                <text x="${baseWidth/2}" y="$y" text-anchor="middle" font-family="Arial, sans-serif" font-size="$titleFontSize" font-weight="bold" fill="$titleFill">${escape(line)}</text>
            """.trimIndent())
        }

        // BEFORE card
        sb.append(beforeCard.svg.replaceFirst("<g", "<g transform=\"translate($leftX, $topY)\""))

        // AFTER card
        sb.append(afterCard.svg.replaceFirst("<g", "<g transform=\"translate($rightX, $topY)\""))

        // Transition arrow between cards (vertically centered to tallest card)
        sb.append("""
            <g transform="translate(${(leftX + cardWidth + rightX)/2 - 12}, ${topY + cardHeightMax/2})">
                <path d="M0,0 L30,0 L25,-8 M30,0 L25,8" stroke="#4ecdc4" stroke-width="3" fill="none" stroke-linecap="round"/>
            </g>
        """.trimIndent())

        sb.append("</svg>")
        return sb.toString()
    }

    // Helper structure for prebuilt card
    private data class BuiltCard(val svg: String, val height: Int)

    // Build a card group SVG with dynamic height and wrapped items, returned untranslated (caller sets translate)
    private fun buildCard(
        width: Int,
        sections: List<Section>,
        headerFill: String,
        headerTitle: String,
        bulletColor: String,
        checkmark: Boolean,
        panelFill: String,
        panelStroke: String,
        sectionHeaderTextFill: String,
        itemTextFill: String
    ): BuiltCard {
        val headerHeight = 60
        val innerPadding = 20
        val contentX = innerPadding
        val contentWidth = width - innerPadding * 2 - 20 // 20 for bullet area
        val lineHeight = 16
        val itemGap = 6

        var currentY = headerHeight + innerPadding
        val sb = StringBuilder()
        sb.append("<g>")
        // We will compute height; draw rect later with computed height via string replace
        // Header visuals
        // We'll append the rects after knowing height; for now collect body and track Y
        val body = StringBuilder()
        sections.forEach { section ->
            if (section.items.isEmpty()) return@forEach
            // Section header
            body.append("""
                <g transform="translate($contentX, $currentY)">
                    <text x="0" y="20" font-family="Arial, sans-serif" font-size="16" font-weight="bold" fill="$sectionHeaderTextFill">${escape(section.title)}</text>
                </g>
            """.trimIndent())
            currentY += 35
            // Items block under header
            val itemsStartY = currentY
            val itemsBuilt = buildItems(section.items, contentX, itemsStartY, bulletColor, checkmark, contentWidth, lineHeight, itemTextFill)
            body.append(itemsBuilt.svg)
            currentY = itemsBuilt.nextY + 10 // bottom spacing after items
        }
        val totalHeight = maxOf(currentY + innerPadding, headerHeight + innerPadding * 2)

        // Now draw the card container and header, then inject body
        sb.append("""
            <rect x="0" y="0" width="$width" height="$totalHeight" rx="12" fill="$panelFill" stroke="$panelStroke" stroke-width="2" filter="url(#shadow)"/>
            <rect x="0" y="0" width="$width" height="$headerHeight" rx="12" fill="$headerFill"/>
            <rect x="0" y="${headerHeight - 12}" width="$width" height="12" fill="$panelFill"/>
            <text x="${width/2}" y="35" text-anchor="middle" font-family="Arial, sans-serif" font-size="18" font-weight="bold" fill="white">${escape(headerTitle)}</text>
        """.trimIndent())
        sb.append(body.toString())
        sb.append("</g>")
        return BuiltCard(sb.toString(), totalHeight)
    }

    private data class BuiltItems(val svg: String, val nextY: Int)

    private fun buildItems(
        items: List<ScoreCardItem>,
        xStart: Int,
        yStart: Int,
        bulletColor: String,
        checkmark: Boolean,
        maxTextWidth: Int,
        lineHeight: Int,
        itemTextFill: String
    ): BuiltItems {
        val sb = StringBuilder()
        var y = yStart
        val itemGap = 6
        items.forEach { item ->
            val text = listOfNotNull(item.displayText.takeIf { it.isNotBlank() }?.let { escape(it) },
                item.description?.takeIf { it.isNotBlank() }?.let { escape(it) })
                .joinToString(" — ")
            val lines = wrapByChars(text, maxTextWidth)
            // bullet or checkmark at first line baseline
            if (checkmark) {
                sb.append("""
                    <path d="M${xStart + 5},${y + 8} L${xStart + 8},${y + 11} L${xStart + 13},${y + 6}" stroke="$bulletColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
                """.trimIndent())
            } else {
                sb.append("""
                    <circle cx="${xStart + 8}" cy="${y + 8}" r="4" fill="$bulletColor"/>
                """.trimIndent())
            }
            lines.forEachIndexed { idx, line ->
                val yy = y + 12 + idx * lineHeight
                sb.append("""
                    <text x="${xStart + 20}" y="$yy" font-family="Arial, sans-serif" font-size="12" fill="$itemTextFill">$line</text>
                """.trimIndent())
            }
            y += 12 + (lines.size * lineHeight) + itemGap
        }
        return BuiltItems(sb.toString(), y)
    }

    // Very simple character-based wrapper using approximate chars-per-line for 12px font
    private fun wrapByChars(text: String, maxWidthPx: Int): List<String> {
        // Approx width per character at 12px Arial ~6.5px
        val pxPerChar = 6.5
        val maxChars = maxOf(10, (maxWidthPx / pxPerChar).toInt())
        return wrapByWords(text, maxChars)
    }

    // Title wrapper uses different font size; approximate px-per-char by size*0.6
    private fun wrapByCharsForTitle(text: String, maxWidthPx: Int, fontSize: Int): List<String> {
        val pxPerChar = fontSize * 0.6 // rough approximation for Arial
        val maxChars = maxOf(8, (maxWidthPx / pxPerChar).toInt())
        return wrapByWords(text, maxChars)
    }

    private fun wrapByWords(text: String, maxChars: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        words.forEach { w ->
            if (current.isEmpty()) {
                current.append(w)
            } else if (current.length + 1 + w.length <= maxChars) {
                current.append(' ').append(w)
            } else {
                lines.add(current.toString())
                current = StringBuilder(w)
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())
        return lines
    }

    private fun escape(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
}
