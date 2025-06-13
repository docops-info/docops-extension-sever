/*
 * Copyright (c) 2023. The DocOps Consortium
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.docops.docopsextensionssupport.timeline


import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.hexToHsl
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import java.io.File
import java.util.*

/**
 * The `TimelineMaker` class is used to create a SVG timeline based on the provided parameters.
 * @constructor Creates a new instance of the `TimelineMaker` class.
 * @param useDark A boolean value indicating whether to use the dark theme.
 */
class TimelineMaker(val useDark: Boolean, val outlineColor: String, var pdf: Boolean = false, val id: String = UUID.randomUUID().toString()) {
    private var textColor: String = "#000000"
    private var fillColor = ""
    private var cardBackgroundColor = ""
    private var separatorColor = ""

    init {
        if(useDark) {
            textColor = "#ffffff"
            fillColor = "#000000"
            cardBackgroundColor = "#1c1c1e"
            separatorColor = "#38383a"
        } else {
            textColor = "#1d1d1f"
            fillColor = "#f2f2f7"
            cardBackgroundColor = "#ffffff"
            separatorColor = "#e5e5e5"
        }
    }

    companion object {
        val DEFAULT_COLORS = mutableListOf(
            "#007AFF", "#FF3B30", "#FF9500", "#34C759", "#5856D6",
            "#00C7BE", "#FF2D92", "#A2845E", "#8E8E93", "#AF52DE"
        )
        val DEFAULT_HEIGHT: Float = 660.0F
        const val DEFAULT_FONT_FAMILY = "-apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', Roboto, Arial, sans-serif"
    }

    data class WikiLink(val url: String, val label: String)
    data class TextSegment(val text: String, val isLink: Boolean = false, val url: String = "")

    private fun parseWikiLinks(text: String): List<TextSegment> {
        val segments = mutableListOf<TextSegment>()
        val linkPattern = "\\[\\[(.*?)\\s+(.*?)\\]\\]".toRegex()
        var lastIndex = 0

        linkPattern.findAll(text).forEach { matchResult ->
            // Add text before the link
            if (matchResult.range.first > lastIndex) {
                val beforeText = text.substring(lastIndex, matchResult.range.first)
                if (beforeText.isNotEmpty()) {
                    segments.add(TextSegment(beforeText))
                }
            }

            // Add the link
            val (url, label) = matchResult.destructured
            segments.add(TextSegment(label, true, url))
            lastIndex = matchResult.range.last + 1
        }

        // Add remaining text after the last link
        if (lastIndex < text.length) {
            val remainingText = text.substring(lastIndex)
            if (remainingText.isNotEmpty()) {
                segments.add(TextSegment(remainingText))
            }
        }

        // If no links were found, return the original text as a single segment
        if (segments.isEmpty()) {
            segments.add(TextSegment(text))
        }

        return segments
    }

    private fun calculateTextHeight(text: String, maxWidth: Int, fontSize: Int, lineHeight: Int): Int {
        val segments = parseWikiLinks(text)
        val lines = mutableListOf<String>()
        var currentLine = ""
        val avgCharWidth = fontSize * 0.6 // Approximate character width

        for (segment in segments) {
            val words = segment.text.split(" ")
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (testLine.length * avgCharWidth <= maxWidth) {
                    currentLine = testLine
                } else {
                    if (currentLine.isNotEmpty()) {
                        lines.add(currentLine)
                        currentLine = word
                    } else {
                        lines.add(word)
                    }
                }
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        return lines.size * lineHeight + 20 // Add padding
    }

    private fun wrapTextWithLinksToTspans(text: String, x: Int, y: Int, maxWidth: Int, lineHeight: Int, fontSize: Int = 16): String {
        val segments = parseWikiLinks(text)
        val lines = mutableListOf<List<TextSegment>>()
        var currentLine = mutableListOf<TextSegment>()
        var currentLineWidth = 0.0 // Changed to Double
        val avgCharWidth = fontSize * 0.6

        for (segment in segments) {
            val words = segment.text.split(" ")
            for (word in words) {
                val wordWidth = word.length * avgCharWidth

                if (currentLineWidth + wordWidth <= maxWidth) {
                    if (currentLine.isNotEmpty() && currentLine.last().text.isNotEmpty()) {
                        // Add space to previous segment or create new segment for space
                        val lastSegment = currentLine.last()
                        if (lastSegment.isLink == segment.isLink && lastSegment.url == segment.url) {
                            currentLine[currentLine.size - 1] = lastSegment.copy(text = "${lastSegment.text} $word")
                        } else {
                            currentLine.add(segment.copy(text = word))
                        }
                    } else {
                        currentLine.add(segment.copy(text = word))
                    }
                    currentLineWidth += wordWidth
                } else {
                    if (currentLine.isNotEmpty()) {
                        lines.add(currentLine)
                        currentLine = mutableListOf(segment.copy(text = word))
                        currentLineWidth = wordWidth
                    } else {
                        currentLine.add(segment.copy(text = word))
                        currentLineWidth = wordWidth
                    }
                }
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        return lines.mapIndexed { lineIndex, lineSegments ->
            val dy = if (lineIndex == 0) "0" else lineHeight.toString()
            val tspans = lineSegments.joinToString("") { segment ->
                if (segment.isLink && !pdf) {
                    """<tspan fill="#007AFF" text-decoration="underline" style="cursor: pointer;" onclick="window.open('${segment.url.escapeXml()}', '_blank')">${segment.text.escapeXml()}</tspan>"""
                } else {
                    """<tspan>${segment.text.escapeXml()}</tspan>"""
                }
            }

            """<tspan x="$x" dy="$dy">$tspans</tspan>"""
        }.joinToString("\n")
    }


    private fun modernEntry(index: Int, entry: Entry, color: String, chars: String, gradIndex: Int, id: String): String {
        val isLeft = index % 2 == 0
        val cardWidth = 320
        val cardPadding = 20
        val contentWidth = cardWidth - (cardPadding * 2)
        val fontSize = 16
        val lineHeight = 20

        // Calculate dynamic height based on text content
        val textHeight = calculateTextHeight(entry.text, contentWidth, fontSize, lineHeight)
        val headerHeight = 40
        val cardHeight = maxOf(100, headerHeight + textHeight + 20) // Minimum 100px

        val yPosition = 120 + (index * (cardHeight + 40)) // Add spacing between cards
        val xPosition = if (isLeft) 50 else 650

        // iOS-style rounded rectangle
        val cardRadius = 16

        // Timeline connector dot
        val dotX = 500
        val dotY = yPosition + (cardHeight / 2)

        return """
        <!-- Timeline Entry ${index + 1} -->
        <g class="timeline-entry">
            <!-- Connector line to timeline -->
            <line x1="${if (isLeft) xPosition + cardWidth else xPosition}" 
                  y1="$dotY" 
                  x2="$dotX" 
                  y2="$dotY" 
                  stroke="$separatorColor" 
                  stroke-width="2"/>
            
            <!-- Timeline dot -->
            <circle cx="$dotX" cy="$dotY" r="8" 
                    fill="url(#timeline_grad_$gradIndex)" 
                    stroke="$cardBackgroundColor" 
                    stroke-width="3"/>
            
            <!-- Card background -->
            <rect x="$xPosition" y="$yPosition" 
                  width="$cardWidth" height="$cardHeight" 
                  rx="$cardRadius" ry="$cardRadius" 
                  class="timeline-card"/>
            
            <!-- Date header -->
            <rect x="$xPosition" y="$yPosition" 
                  width="$cardWidth" height="$headerHeight" 
                  rx="$cardRadius" ry="$cardRadius" 
                  fill="url(#timeline_grad_$gradIndex)"/>
            <rect x="$xPosition" y="${yPosition + 25}" 
                  width="$cardWidth" height="15" 
                  fill="url(#timeline_grad_$gradIndex)"/>
            
            <!-- Date text -->
            <text x="${xPosition + cardPadding}" y="${yPosition + 28}" 
                  class="timeline-text timeline-date" 
                  fill="white">
                ${entry.date.escapeXml()}
            </text>
            
            <!-- Content text with links -->
            <text x="${xPosition + cardPadding}" y="${yPosition + headerHeight + 25}" 
                  class="timeline-text timeline-content">
                ${wrapTextWithLinksToTspans(entry.text, xPosition + cardPadding, yPosition + headerHeight + 25, contentWidth, lineHeight, fontSize)}
            </text>
        </g>
        """.trimIndent()
    }

    private fun modernRoad(width: Int): String {
        return """
        <!-- Main timeline spine -->
        <line x1="500" y1="100" x2="500" y2="800" 
              stroke="$separatorColor" 
              stroke-width="4" 
              stroke-linecap="round"/>
        """.trimIndent()
    }


    fun makeTimelineSvg(source: String, title: String, scale: String, isPdf: Boolean, chars: String): String {
        this.pdf = isPdf
        val entries = TimelineParser().parse(source)
        val sb = StringBuilder()
        val id = UUID.randomUUID().toString()

        // Normalize scale
        val scaleF = maxOf(0.1f, minOf(5.0f, scale.toFloatOrNull() ?: 1.0f))

        // Calculate base dimensions
        val cardWidth = 300
        val cardPadding = 18
        val contentWidth = cardWidth - (cardPadding * 2)
        val fontSize = 14
        val lineHeight = 20

        val sideMargin = 50
        val spineWidth = 80
        val cardSpacing = 35
        val topMargin = 100
        val bottomMargin = 50

        val totalWidth = sideMargin + cardWidth + spineWidth + cardWidth + sideMargin

        // Pre-calculate card heights
        val cardHeights = mutableListOf<Int>()
        entries.forEach { entry ->
            val textHeight = calculateTextHeight(entry.text, contentWidth, fontSize, lineHeight)
            val headerHeight = 45
            val cardHeight = maxOf(90, headerHeight + textHeight + cardPadding)
            cardHeights.add(cardHeight)
        }

        val totalHeight = if (entries.isNotEmpty()) {
            topMargin + cardHeights.sum() + (cardSpacing * maxOf(0, entries.size - 1)) + bottomMargin
        } else {
            300
        }

        // Calculate scaled dimensions
        val scaledWidth = (totalWidth * scaleF).toInt()
        val scaledHeight = (totalHeight * scaleF).toInt()

        // SVG WITHOUT background styling in the element itself
        sb.append("""
    <svg width="$scaledWidth" height="$scaledHeight" 
         viewBox="0 0 $totalWidth $totalHeight"
         xmlns="http://www.w3.org/2000/svg" 
         xmlns:xlink="http://www.w3.org/1999/xlink">
    """.trimIndent())

        val defs = modernDefs(isPdf, id)
        sb.append(defs.first)
        val colors = defs.second

        // Add the background as a scalable SVG rectangle instead of CSS style
        val backgroundGradient = if (useDark) "#0a0a0a" else "#fafafa"
        sb.append("""
    <!-- Scalable background -->
    <rect width="$totalWidth" height="$totalHeight" 
          fill="url(#backgroundGradient)"/>
    """.trimIndent())

        // Title and content
        sb.append("""
    <text x="${totalWidth/2}" y="50" 
          text-anchor="middle" 
          class="timeline-title">
          ${title.escapeXml()}
    </text>
    """.trimIndent())

        // Timeline spine
        val spineX = sideMargin + cardWidth + (spineWidth / 2)
        val spineStartY = topMargin - 30
        val spineEndY = totalHeight - bottomMargin + 30

        sb.append("""
    <!-- Timeline spine -->
    <line x1="$spineX" y1="$spineStartY" x2="$spineX" y2="$spineEndY" 
          class="timeline-spine"/>
    """.trimIndent())

        // Generate timeline entries
        var currentY = topMargin
        entries.forEachIndexed { index, entry ->
            val gradIndex = index % colors.size
            val cardHeight = cardHeights[index]

            sb.append(refinedEntry(
                index, entry, colors[gradIndex], gradIndex, id,
                currentY, cardHeight, totalWidth, cardWidth, cardPadding,
                contentWidth, fontSize, lineHeight, sideMargin, spineX
            ))

            currentY += cardHeight + cardSpacing
        }

        sb.append("</svg>")
        return sb.toString()
    }

    private fun modernDefs(isPdf: Boolean, id: String): Pair<String, List<String>> {
        val colors = DEFAULT_COLORS.shuffled()

        val shadowFilter = if (useDark) {
            """
        <filter id="cardShadow" x="-50%" y="-50%" width="200%" height="200%">
            <feGaussianBlur in="SourceAlpha" stdDeviation="3"/>
            <feOffset dx="0" dy="6" result="offset"/>
            <feFlood flood-color="#000000" flood-opacity="0.4"/>
            <feComposite in2="offset" operator="in"/>
            <feMerge> 
                <feMergeNode/>
                <feMergeNode in="SourceGraphic"/> 
            </feMerge>
        </filter>
        """
        } else {
            """
        <filter id="cardShadow" x="-50%" y="-50%" width="200%" height="200%">
            <feGaussianBlur in="SourceAlpha" stdDeviation="4"/>
            <feOffset dx="0" dy="4" result="offset"/>
            <feFlood flood-color="#000000" flood-opacity="0.15"/>
            <feComposite in2="offset" operator="in"/>
            <feMerge> 
                <feMergeNode/>
                <feMergeNode in="SourceGraphic"/> 
            </feMerge>
        </filter>
        """
        }

        // Add background gradient definition
        val backgroundGradient = """
    <linearGradient id="backgroundGradient" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" style="stop-color:$fillColor;stop-opacity:1" />
        <stop offset="100%" style="stop-color:${if (useDark) "#0a0a0a" else "#fafafa"};stop-opacity:1" />
    </linearGradient>
    """

        // Use SVGColor for enhanced gradients
        val gradientDefs = colors.mapIndexed { index, color ->
            val svgColor = SVGColor(color, "timeline_grad_$index", "to bottom right", 1.0, 0.95, 0.85)
            svgColor.linearGradient
        }.joinToString("\n")

        val linkStyle = if (!isPdf) {
            """
        .timeline-link {
            fill: #007AFF;
            text-decoration: underline;
            cursor: pointer;
            transition: fill 0.2s ease;
        }
        .timeline-link:hover {
            fill: #0056CC;
        }
        """
        } else ""

        val defs = """
    <defs>
        <style>
            .timeline-card {
                fill: $cardBackgroundColor;
                stroke: $separatorColor;
                stroke-width: 0.5;
                filter: url(#cardShadow);
                transition: transform 0.2s ease;
            }
            .timeline-card:hover {
                transform: translateY(-2px);
            }
            .timeline-text {
                font-family: $DEFAULT_FONT_FAMILY;
                fill: $textColor;
                text-rendering: optimizeLegibility;
            }
            .timeline-title {
                font-size: 32px;
                font-weight: 800;
                letter-spacing: -1px;
                fill: $textColor;
            }
            .timeline-date {
                font-size: 13px;
                font-weight: 700;
                text-transform: uppercase;
                letter-spacing: 0.5px;
            }
            .timeline-content {
                font-size: 15px;
                font-weight: 400;
                line-height: 1.5;
                fill: $textColor;
            }
            .timeline-spine {
                stroke: $separatorColor;
                stroke-width: 2;
                stroke-linecap: round;
            }
            .timeline-dot {
                stroke: $cardBackgroundColor;
                stroke-width: 3;
                filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1));
            }
            .timeline-connector {
                stroke: $separatorColor;
                stroke-width: 1.5;
                stroke-dasharray: 3,3;
                opacity: 0.6;
            }
            $linkStyle
        </style>
        $shadowFilter
        $backgroundGradient
        $gradientDefs
    </defs>
    """.trimIndent()

        return Pair(defs, colors)
    }

    private fun refinedEntry(
        index: Int, entry: Entry, color: String, gradIndex: Int, id: String,
        yPosition: Int, cardHeight: Int, totalWidth: Int, cardWidth: Int,
        cardPadding: Int, contentWidth: Int, fontSize: Int, lineHeight: Int,
        sideMargin: Int, spineX: Int
    ): String {
        val isLeft = index % 2 == 0
        val xPosition = if (isLeft) sideMargin else totalWidth - sideMargin - cardWidth
        val headerHeight = 45
        val cardRadius = 16
        val dotY = yPosition + (cardHeight / 2)

        return """
    <!-- Timeline Entry ${index + 1} -->
    <g class="timeline-entry">
        <!-- Dashed connector line -->
        <line x1="${if (isLeft) xPosition + cardWidth else xPosition}" 
              y1="$dotY" 
              x2="$spineX" 
              y2="$dotY" 
              class="timeline-connector"/>
        
        <!-- Enhanced timeline dot -->
        <circle cx="$spineX" cy="$dotY" r="8" 
                fill="url(#timeline_grad_$gradIndex)" 
                class="timeline-dot"/>
        
        <!-- Elegant card with enhanced shadow -->
        <rect x="$xPosition" y="$yPosition" 
              width="$cardWidth" height="$cardHeight" 
              rx="$cardRadius" ry="$cardRadius" 
              class="timeline-card"/>
        
        <!-- Gradient header with subtle design -->
        <rect x="$xPosition" y="$yPosition" 
              width="$cardWidth" height="$headerHeight" 
              rx="$cardRadius" ry="$cardRadius" 
              fill="url(#timeline_grad_$gradIndex)"/>
        <rect x="$xPosition" y="${yPosition + cardRadius}" 
              width="$cardWidth" height="${headerHeight - cardRadius}" 
              fill="url(#timeline_grad_$gradIndex)"/>
        
        <!-- Refined date text -->
        <text x="${xPosition + cardPadding}" y="${yPosition + 28}" 
              class="timeline-text timeline-date" 
              fill="white">
              ${entry.date.escapeXml()}
        </text>
        
        <!-- Content with enhanced typography -->
        <text x="${xPosition + cardPadding}" y="${yPosition + headerHeight + 25}" 
              class="timeline-text timeline-content">
              ${wrapTextWithLinksToTspans(entry.text, xPosition + cardPadding, yPosition + headerHeight + 25, contentWidth, lineHeight, fontSize)}
        </text>
    </g>
    """.trimIndent()
    }


}
fun main() {
    // Test with the content from the issue description
    val entry = """
-
date: 1660-1798
text: The Enlightenment/Neoclassical Period
Literature focused on reason, logic, and scientific thought. Major writers include [[https://en.wikipedia.org/wiki/Alexander_Pope Alexander Pope]] and [[https://en.wikipedia.org/wiki/Jonathan_Swift Jonathan Swift]].
-
date: 1798-1832
text: Romanticism
Emphasized emotion, individualism, and the glorification of nature. Key figures include [[https://en.wikipedia.org/wiki/William_Wordsworth William Wordsworth]] and [[https://en.wikipedia.org/wiki/Lord_Byron Lord Byron]].
-
date: 1837-1901
text: Victorian Era
Literature reflected the social, economic, and cultural changes of the Industrial Revolution. Notable authors include [[https://en.wikipedia.org/wiki/Charles_Dickens Charles Dickens]] and [[https://en.wikipedia.org/wiki/George_Eliot George Eliot]].
-
date: 1914-1945
text: Modernism
Characterized by a break with traditional forms and a focus on experimentation. Important writers include [[https://en.wikipedia.org/wiki/James_Joyce James Joyce]] and [[https://en.wikipedia.org/wiki/Virginia_Woolf Virginia Woolf]].
-
date: 1945-present
text: Postmodernism
Challenges the distinction between high and low culture and emphasizes fragmentation and skepticism. Key authors include [[https://en.wikipedia.org/wiki/Thomas_Pynchon Thomas Pynchon]] and [[https://en.wikipedia.org/wiki/Toni_Morrison Toni Morrison]].
    """.trimIndent()

    // Test normal output
    val maker = TimelineMaker(false, "#a1d975")
    val svg = maker.makeTimelineSvg(entry, "Literary Periods", "0.6", false, "30")
    val f = File("gen/timeline_normal.svg")
    f.writeBytes(svg.toByteArray())

    // Test PDF output
    val makerPdf = TimelineMaker(false, "#a1d975")
    val svgPdf = makerPdf.makeTimelineSvg(entry, "Literary Periods", "1", true, "30")
    val fPdf = File("gen/timeline_pdf.svg")
    fPdf.writeBytes(svgPdf.toByteArray())

    println("Test completed. Check gen/timeline_normal.svg and gen/timeline_pdf.svg")
}
