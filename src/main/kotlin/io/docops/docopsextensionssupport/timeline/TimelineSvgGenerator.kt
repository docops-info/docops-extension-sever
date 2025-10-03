package io.docops.extension.wasm.timeline

import io.docops.docopsextensionssupport.timeline.TimelineConfig
import io.docops.docopsextensionssupport.timeline.TimelineEvent
import kotlin.math.max
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TimelineMaker {

    val lightColors = listOf<String>("#A8FBD3","#708993", "#4FB7B3", "#E62727", "#DDDDDD", "#B19CD9", "#6EB3E0", "#F5B8B8", "#F99191", "#FFCC99",
        "#A4D6B4", "#F6E58D", "#FF9F43", "#FF1554","#3396D3", "#AE75DA", "#124170", "#FF2DD1")
    @OptIn(ExperimentalUuidApi::class)
    fun makeSvg(config: TimelineConfig, useDark: Boolean = false, scale: String = "1.0"): String {
        val cardHeight = 110
        val cardSpacing = 60
        val totalHeight = max(300, (config.events.size * (cardHeight + cardSpacing)) + 160)
        val centerX = config.width / 2
        val cardWidth = 360
        val startY = 80
        val scaleF = scale.toFloatOrNull()?.takeIf { it > 0f } ?: 1.0f
        val width = config.width * scaleF
        val height = totalHeight * scaleF

        // Resolve light background color from config (index-based).
        val lightBgColor: String = if (!useDark) {
            val idx = config.lightColorIndex ?: 0
            lightColors.getOrNull(idx) ?: lightColors.first()
        } else "#000000"

        val id = Uuid.random().toString()
        return buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<svg width="$width" height="$height" viewBox="0 0 ${config.width} $totalHeight" xmlns="http://www.w3.org/2000/svg" id="id_$id">""")
            appendLine(buildDefs(useDark, id))
            appendLine("<g>")
            if(useDark) {
                appendLine("""<rect width="100%" height="100%" fill="url(#backgroundGradient_$id)" rx="16" ry="16"/>""")
            } else {
                appendLine("""<rect width="100%" height="100%" fill="$lightBgColor" rx="16" ry="16"/>""")
            }
            // Main vertical timeline line
            appendLine("""<line class="timeline-line" x1="$centerX" y1="50" x2="$centerX" y2="${totalHeight - 50}" stroke-width="2" stroke-linecap="round"/>""")

            // Build timeline events
            config.events.forEachIndexed { index, event ->
                val yPos = startY + (index * (cardHeight + cardSpacing))
                val isRight = index % 2 == 0
                appendLine(buildTimelineEvent(event, yPos, centerX, cardWidth, cardHeight, isRight))
            }
            appendLine("</g>")
            appendLine("</svg>")
        }
    }

    private fun buildDefs(useDark: Boolean, id: String): String {
        var back = ""
        if(useDark) {
            back = """             
                <linearGradient id="backgroundGradient_$id" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:#1a1a2e;stop-opacity:1"/>
                    <stop offset="100%" style="stop-color:#16213e;stop-opacity:1"/>
                </linearGradient>
            """.trimIndent()
        }
        return    """
    <defs>
        <filter id="glass-blur_$id">
            <feGaussianBlur in="SourceGraphic" stdDeviation="10"/>
        </filter>
        
        <linearGradient id="light-glass-bg_$id" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:rgba(255,255,255,0.25);stop-opacity:1" />
            <stop offset="100%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
        </linearGradient>
        
        <linearGradient id="dark-glass-bg_$id" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
            <stop offset="100%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1" />
        </linearGradient>
        
        <filter id="glow_$id">
            <feGaussianBlur stdDeviation="4" result="coloredBlur"/>
            <feMerge>
                <feMergeNode in="coloredBlur"/>
                <feMergeNode in="SourceGraphic"/>
            </feMerge>
        </filter>
        
        <filter id="shadow">
            <feDropShadow dx="0" dy="4" stdDeviation="8" flood-opacity="0.3"/>
        </filter>
        $back
        ${buildStyles(useDark, id)}
    </defs>
    """.trimIndent()
    }

    private fun buildStyles(useDark: Boolean, id: String): String {
        return if (useDark) {
            """
            <style>
                #id_$id .timeline-line { stroke: rgba(255,255,255,0.2); }
                #id_$id .timeline-card { 
                    fill: url(#dark-glass-bg_$id);
                    stroke: rgba(255,255,255,0.15);
                    filter: url(#shadow);
                }
                #id_$id .timeline-date { 
                    fill: #e2e8f0;
                    font-weight: 700;
                }
                #id_$id .timeline-text { 
                    fill: #cbd5e0;
                    font-weight: 500;
                }
                #id_$id .timeline-link {
                    fill: #93c5fd;
                    font-weight: 600;
                }
                #id_$id .timeline-circle { 
                    fill: rgba(255,255,255,0.2);
                    stroke: rgba(147,197,253,0.8);
                    filter: url(#glow_$id);
                }
                #id_$id .connector-line { stroke: rgba(255,255,255,0.3); }
            </style>
            """.trimIndent()
        } else {
            """
            <style>
                #id_$id .timeline-line { stroke: rgba(255,255,255,0.4); }
                #id_$id .timeline-card { 
                    fill: url(#light-glass-bg_$id);
                    stroke: rgba(255,255,255,0.3);
                    filter: url(#shadow);
                }
                #id_$id .timeline-date { 
                    fill: #2d3748;
                    font-weight: 700;
                }
                #id_$id .timeline-text { 
                    fill: #1a202c;
                    font-weight: 500;
                }
                #id_$id .timeline-link {
                    fill: #4c51bf;
                    font-weight: 600;
                }
                #id_$id .timeline-circle { 
                    fill: rgba(255,255,255,0.9);
                    stroke: rgba(103,126,234,0.8);
                    filter: url(#glow_$id);
                }
                #id_$id .connector-line { stroke: rgba(255,255,255,0.5); }
            </style>
            """.trimIndent()
        }
    }

    private fun buildTimelineEvent(
        event: TimelineEvent,
        yPos: Int,
        centerX: Int,
        cardWidth: Int,
        cardHeight: Int,
        isRight: Boolean
    ): String {
        val cardX = if (isRight) centerX + 40 else centerX - cardWidth - 40
        val connectorX1 = if (isRight) centerX + 10 else centerX - 10
        val connectorX2 = if (isRight) centerX + 40 else centerX - 40
        val textX = cardX + 24

        val textWithLinks = parseLinks(event.text)
        val textLines = wrapTextWithLinks(textWithLinks, 40)

        return buildString {
            appendLine("""<g class="timeline-item">""")
            appendLine("""<circle class="timeline-circle" cx="$centerX" cy="$yPos" r="10" stroke-width="2"/>""")
            appendLine("""<line class="connector-line" x1="$connectorX1" y1="$yPos" x2="$connectorX2" y2="$yPos" stroke-width="1.5"/>""")

            val adjustedHeight = max(cardHeight, 70 + (textLines.size * 18))
            val cardY = yPos - (adjustedHeight / 2)

            appendLine("""<rect class="timeline-card" x="$cardX" y="$cardY" width="$cardWidth" height="$adjustedHeight" rx="16" stroke-width="1.5"/>""")
            appendLine("""<text class="timeline-date" x="$textX" y="${cardY + 32}" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif" font-size="22">${escapeXml(event.date)}</text>""")

            // Process text with links
            var currentY = cardY + 62
            textLines.forEach { line ->
                appendLine(renderTextLine(line, textX, currentY))
                currentY += 18
            }

            appendLine("</g>")
        }
    }

    data class TextSegment(
        val text: String,
        val isLink: Boolean = false,
        val url: String? = null
    )

    private fun parseLinks(text: String): List<TextSegment> {
        val segments = mutableListOf<TextSegment>()
        val linkPattern = """\[\[([^\s\]]+)\s+([^\]]+)\]\]""".toRegex()

        var lastIndex = 0
        linkPattern.findAll(text).forEach { match ->
            // Add text before the link
            if (match.range.first > lastIndex) {
                val beforeText = text.substring(lastIndex, match.range.first)
                if (beforeText.isNotEmpty()) {
                    segments.add(TextSegment(beforeText))
                }
            }

            // Add the link
            val url = match.groupValues[1]
            val linkText = match.groupValues[2]
            segments.add(TextSegment(linkText, isLink = true, url = url))

            lastIndex = match.range.last + 1
        }

        // Add remaining text after the last link
        if (lastIndex < text.length) {
            val afterText = text.substring(lastIndex)
            if (afterText.isNotEmpty()) {
                segments.add(TextSegment(afterText))
            }
        }

        return segments
    }

    private fun wrapTextWithLinks(segments: List<TextSegment>, maxLength: Int): List<List<TextSegment>> {
        val lines = mutableListOf<List<TextSegment>>()
        var currentLine = mutableListOf<TextSegment>()
        var currentLineLength = 0

        segments.forEach { segment ->
            val segmentLength = segment.text.length

            if (segment.isLink) {
                // Check if link fits on current line
                if (currentLineLength + segmentLength > maxLength && currentLine.isNotEmpty()) {
                    lines.add(currentLine.toList())
                    currentLine = mutableListOf()
                    currentLineLength = 0
                }
                currentLine.add(segment)
                currentLineLength += segmentLength
            } else {
                // Split regular text by words
                val words = segment.text.split(" ")
                words.forEachIndexed { index, word ->
                    val wordWithSpace = if (index > 0 || currentLineLength > 0) " $word" else word
                    val wordLength = wordWithSpace.length

                    if (currentLineLength + wordLength > maxLength && currentLine.isNotEmpty()) {
                        lines.add(currentLine.toList())
                        currentLine = mutableListOf()
                        currentLineLength = 0
                    }

                    if (currentLine.isNotEmpty() && currentLine.last().isLink.not()) {
                        // Append to last text segment
                        val lastSegment = currentLine.removeLast()
                        currentLine.add(TextSegment(lastSegment.text + wordWithSpace))
                    } else {
                        currentLine.add(TextSegment(wordWithSpace.trim()))
                    }
                    currentLineLength += wordLength
                }
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toList())
        }

        return lines
    }

    private fun renderTextLine(segments: List<TextSegment>, startX: Int, y: Int): String {
        return buildString {
            var currentX = startX

            segments.forEach { segment ->
                if (segment.isLink && segment.url != null) {
                    appendLine("""<a href="${segment.url}" target="_blank">""")
                    appendLine("""<text class="timeline-link" x="$currentX" y="$y" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif" font-size="14">${escapeXml(segment.text)}</text>""")
                    appendLine("""</a>""")
                    currentX += (segment.text.length * 8.0).toInt()
                } else {
                    appendLine("""<text class="timeline-text" x="$currentX" y="$y" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif" font-size="14">${escapeXml(segment.text)}</text>""")
                    currentX += (segment.text.length * 8.0).toInt()
                }
            }
        }
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