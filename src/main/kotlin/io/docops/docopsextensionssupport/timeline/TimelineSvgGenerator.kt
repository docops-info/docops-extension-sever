package io.docops.docopsextensionssupport.timeline

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TimelineSvgGenerator {

    // Define 15 colors that cycle for timeline items
    private val lightModeColors = listOf(
        TimelineColor("#3b82f6", "#2563eb"), // Blue
        TimelineColor("#8b5cf6", "#7c3aed"), // Purple
        TimelineColor("#a855f7", "#9333ea"), // Violet
        TimelineColor("#ec4899", "#db2777"), // Pink
        TimelineColor("#f59e0b", "#d97706"), // Amber
        TimelineColor("#10b981", "#059669"), // Emerald
        TimelineColor("#06b6d4", "#0891b2"), // Cyan
        TimelineColor("#6366f1", "#4f46e5"), // Indigo
        TimelineColor("#ef4444", "#dc2626"), // Red
        TimelineColor("#14b8a6", "#0d9488"), // Teal
        TimelineColor("#f97316", "#ea580c"), // Orange
        TimelineColor("#84cc16", "#65a30d"), // Lime
        TimelineColor("#8b5cf6", "#7c3aed"), // Purple (variant)
        TimelineColor("#ec4899", "#db2777"), // Pink (variant)
        TimelineColor("#3b82f6", "#2563eb")  // Blue (variant)
    )

    private val darkModeColors = listOf(
        TimelineColor("#60a5fa", "#3b82f6"), // Light Blue
        TimelineColor("#a78bfa", "#8b5cf6"), // Light Purple
        TimelineColor("#c084fc", "#a855f7"), // Light Violet
        TimelineColor("#f472b6", "#ec4899"), // Light Pink
        TimelineColor("#fbbf24", "#f59e0b"), // Light Amber
        TimelineColor("#34d399", "#10b981"), // Light Emerald
        TimelineColor("#22d3ee", "#06b6d4"), // Light Cyan
        TimelineColor("#818cf8", "#6366f1"), // Light Indigo
        TimelineColor("#f87171", "#ef4444"), // Light Red
        TimelineColor("#2dd4bf", "#14b8a6"), // Light Teal
        TimelineColor("#fb923c", "#f97316"), // Light Orange
        TimelineColor("#a3e635", "#84cc16"), // Light Lime
        TimelineColor("#a78bfa", "#8b5cf6"), // Light Purple (variant)
        TimelineColor("#f472b6", "#ec4899"), // Light Pink (variant)
        TimelineColor("#60a5fa", "#3b82f6")  // Light Blue (variant)
    )

    data class TimelineColor(val stroke: String, val text: String)

    @OptIn(ExperimentalUuidApi::class)
    fun generateTimeline(config: TimelineConfig, isDarkMode: Boolean = false, scale: String): String {
        val svgId = "id_${Uuid.random().toHexString()}"
        val colors = if (isDarkMode) darkModeColors else lightModeColors

        // Calculate dimensions
        val maxTextWidth = 380
        val itemSpacing = 150
        val topMargin = 80
        val bottomMargin = 80
        val width = 1000
        val centerX = width / 2

        // Calculate heights for each item
        val itemHeights = config.events.map { item ->
            calculateItemHeight(item, maxTextWidth)
        }

        val totalHeight = topMargin + itemHeights.sum() + (config.events.size - 1) * itemSpacing + bottomMargin
        val height = totalHeight.coerceAtLeast(400)

        val sb = StringBuilder()

        // SVG header
        sb.append("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.append("""<svg width="$width" height="$height" viewBox="0 0 $width $height" xmlns="http://www.w3.org/2000/svg" id="$svgId">""")

        // Add metadata
       // appendMetadata(sb, items)

        // Add defs with gradients and filters
        appendDefs(sb, svgId, isDarkMode, colors)

        // Background
        sb.append("""<rect width="100%" height="100%" fill="url(#bgGradient_$svgId)"/>""")

        // Decorative circles
        if (isDarkMode) {
            sb.append("""<circle cx="100" cy="100" r="150" fill="#3b82f6" opacity="0.08"/>""")
            sb.append("""<circle cx="${width - 100}" cy="${height - 100}" r="200" fill="#ec4899" opacity="0.08"/>""")
        } else {
            sb.append("""<circle cx="100" cy="100" r="150" fill="#3b82f6" opacity="0.04"/>""")
            sb.append("""<circle cx="${width - 100}" cy="${height - 100}" r="200" fill="#ec4899" opacity="0.04"/>""")
        }

        // Calculate timeline line length
        val lineStartY = topMargin
        val lineEndY = height - bottomMargin

        // Main timeline line
        sb.append("""<line x1="$centerX" y1="$lineStartY" x2="$centerX" y2="$lineEndY" stroke="url(#lineGradient_$svgId)" stroke-width="3" stroke-linecap="round" opacity="0.7"/>""")

        // Generate timeline items
        var currentY = topMargin + 40
        config.events.forEachIndexed { index, item ->
            val isRight = index % 2 == 0
            val color = colors[index % colors.size]
            val itemHeight = itemHeights[index]

            appendTimelineItem(sb, svgId, item, color, centerX, currentY, isRight, maxTextWidth, itemHeight, index, isDarkMode)

            currentY += itemHeight + itemSpacing
        }

        sb.append("</svg>")

        return sb.toString()
    }

    private fun appendDefs(sb: StringBuilder, svgId: String, isDarkMode: Boolean, colors: List<TimelineColor>) {
        sb.append("<defs>")

        // Background gradient
        if (isDarkMode) {
            sb.append("""
                <linearGradient id="bgGradient_$svgId" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:#0f172a;stop-opacity:1" />
                    <stop offset="100%" style="stop-color:#1e293b;stop-opacity:1" />
                </linearGradient>
            """.trimIndent())
        } else {
            sb.append("""
                <linearGradient id="bgGradient_$svgId" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:#f0f9ff;stop-opacity:1" />
                    <stop offset="100%" style="stop-color:#e0f2fe;stop-opacity:1" />
                </linearGradient>
            """.trimIndent())
        }

        // Timeline line gradient
        sb.append("""
            <linearGradient id="lineGradient_$svgId" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" style="stop-color:#3b82f6;stop-opacity:0.6" />
                <stop offset="50%" style="stop-color:#8b5cf6;stop-opacity:0.8" />
                <stop offset="100%" style="stop-color:#ec4899;stop-opacity:0.6" />
            </linearGradient>
        """.trimIndent())

        // Card gradients
        if (isDarkMode) {
            sb.append("""
                <linearGradient id="cardGradient1_$svgId" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:#1e293b;stop-opacity:0.95" />
                    <stop offset="100%" style="stop-color:#334155;stop-opacity:0.95" />
                </linearGradient>
                <linearGradient id="cardGradient2_$svgId" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:#1e293b;stop-opacity:0.95" />
                    <stop offset="100%" style="stop-color:#2d1b4e;stop-opacity:0.95" />
                </linearGradient>
            """.trimIndent())
        } else {
            sb.append("""
                <linearGradient id="cardGradient1_$svgId" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:#ffffff;stop-opacity:0.95" />
                    <stop offset="100%" style="stop-color:#f8fafc;stop-opacity:0.95" />
                </linearGradient>
                <linearGradient id="cardGradient2_$svgId" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:#fdf4ff;stop-opacity:0.95" />
                    <stop offset="100%" style="stop-color:#fae8ff;stop-opacity:0.95" />
                </linearGradient>
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
            <filter id="cardShadow_$svgId">
                <feDropShadow dx="0" dy="4" stdDeviation="12" flood-opacity="0.1"/>
            </filter>
        """.trimIndent())

        sb.append("</defs>")
    }

    private fun appendTimelineItem(
        sb: StringBuilder,
        svgId: String,
        item: TimelineEvent,
        color: TimelineColor,
        centerX: Int,
        y: Int,
        isRight: Boolean,
        maxWidth: Int,
        itemHeight: Int,
        index: Int,
        isDarkMode: Boolean
    ) {
        val cardX = if (isRight) centerX + 80 else 40
        val lineEndX = if (isRight) centerX + 80 else centerX - 80
        val textColor = if (isDarkMode) "#e2e8f0" else "#475569"
        val cardGradient = if (index % 4 < 2) "cardGradient1_$svgId" else "cardGradient2_$svgId"

        sb.append("""<g class="timeline-item">""")

        // Connecting line
        sb.append("""<line x1="$centerX" y1="$y" x2="$lineEndX" y2="$y" stroke="${color.stroke}" stroke-width="2" opacity="0.4"/>""")

        // Node circle with glow
        sb.append("""<circle cx="$centerX" cy="$y" r="12" fill="${if (isDarkMode) "#1e293b" else "#ffffff"}" stroke="${color.stroke}" stroke-width="3" filter="url(#glow_$svgId)"/>""")
        sb.append("""<circle cx="$centerX" cy="$y" r="6" fill="${color.stroke}" opacity="0.9"/>""")

        // Animated pulse
        sb.append("""<circle cx="$centerX" cy="$y" r="12" fill="none" stroke="${color.stroke}" stroke-width="2" opacity="0.2">""")
        sb.append("""<animate attributeName="r" from="12" to="24" dur="2s" begin="${index * 0.5}s" repeatCount="indefinite"/>""")
        sb.append("""<animate attributeName="opacity" from="0.2" to="0" dur="2s" begin="${index * 0.5}s" repeatCount="indefinite"/>""")
        sb.append("""</circle>""")

        // Card
        val cardY = y - 40
        sb.append("""<rect x="$cardX" y="$cardY" width="$maxWidth" height="$itemHeight" rx="12" fill="url(#$cardGradient)" stroke="${color.stroke}" stroke-width="2" opacity="1" filter="url(#cardShadow_$svgId)"/>""")

        // Content
        val textX = cardX + 20
        var textY = cardY + 30

        // Date
        sb.append("""<text x="$textX" y="$textY" font-family="'Inter', -apple-system, sans-serif" font-size="20" font-weight="700" fill="${color.text}">${escapeXml(item.date)}</text>""")
        textY += 25

        // Text content with wiki-style link support
        val lines = wrapText(item.text, maxWidth - 40)
        lines.forEach { line ->
            appendTextWithLinks(sb, line, textX, textY, textColor, color.text, isDarkMode)
            textY += 17
        }

        sb.append("</g>")
    }

    private fun appendTextWithLinks(
        sb: StringBuilder,
        text: String,
        x: Int,
        y: Int,
        textColor: String,
        linkColor: String,
        isDarkMode: Boolean
    ) {
        // Parse wiki-style links [[url text]]
        val linkPattern = """\[\[([^\s]+)\s+([^\]]+)\]\]""".toRegex()
        var currentX = x
        var lastIndex = 0

        linkPattern.findAll(text).forEach { match ->
            // Add text before link
            if (match.range.first > lastIndex) {
                val beforeText = text.substring(lastIndex, match.range.first)
                sb.append("""<text x="$currentX" y="$y" font-family="'Inter', -apple-system, sans-serif" font-size="13" fill="$textColor">$beforeText</text>""")
                currentX += estimateTextWidth(beforeText, 13)
            }

            // Add link
            val url = match.groupValues[1]
            val linkText = match.groupValues[2]
            sb.append("""<a href="$url" target="_blank">""")
            sb.append("""<text x="$currentX" y="$y" font-family="'Inter', -apple-system, sans-serif" font-size="13" fill="$linkColor" text-decoration="underline">$linkText</text>""")
            sb.append("""</a>""")
            currentX += estimateTextWidth(linkText, 13)

            lastIndex = match.range.last + 1
        }

        // Add remaining text
        if (lastIndex < text.length) {
            val remainingText = text.substring(lastIndex)
            sb.append("""<text x="$currentX" y="$y" font-family="'Inter', -apple-system, sans-serif" font-size="13" fill="$textColor">$remainingText</text>""")
        }
    }

    private fun calculateItemHeight(item: TimelineEvent, maxWidth: Int): Int {
        val lines = wrapText(item.text, maxWidth - 40)
        val textHeight = lines.size * 17
        return 80 + textHeight.coerceAtLeast(20)
    }

    private fun wrapText(text: String, maxWidth: Int): List<String> {
        val words = text.split(" ")
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

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        return lines
    }

    private fun estimateTextWidth(text: String, fontSize: Int): Int {
        // Rough estimation: average character width is about 0.6 * fontSize
        return (text.length * fontSize * 0.6).toInt()
    }



    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

}