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


import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import java.io.File
import java.util.*

/**
 * The `TimelineMaker` class is used to create a SVG timeline based on the provided parameters.
 * @constructor Creates a new instance of the `TimelineMaker` class.
 * @param useDark A boolean value indicating whether to use the dark theme.
 */
/**
 * The orientation of the timeline.
 */
enum class TimelineOrientation {
    HORIZONTAL, // Default orientation with vertical spine and entries on left/right
    VERTICAL    // New orientation with horizontal spine and entries above/below
}

/**
 * The `TimelineMaker` class is used to create a SVG timeline based on the provided parameters.
 * @constructor Creates a new instance of the `TimelineMaker` class.
 * @param useDark A boolean value indicating whether to use the dark theme.
 * @param outlineColor The color of the timeline outline.
 * @param pdf A boolean value indicating whether the output is for PDF.
 * @param id A unique identifier for the timeline.
 * @param useGlass A boolean value indicating whether to use glass effects.
 * @param orientation The orientation of the timeline (HORIZONTAL or VERTICAL).
 * @param enableDetailView A boolean value indicating whether to enable clickable items to show details.
 */
class TimelineMaker(
    val useDark: Boolean, 
    val outlineColor: String= "#38383a", 
    var pdf: Boolean = false, 
    val id: String = UUID.randomUUID().toString(), 
    val useGlass: Boolean = false,
    val orientation: TimelineOrientation = TimelineOrientation.HORIZONTAL,
    val enableDetailView: Boolean = false
) {
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
            separatorColor = "#666666"
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


    /**
     * Creates a timeline SVG based on the provided entries and parameters.
     *
     * @param entries The list of timeline entries.
     * @param title The title of the timeline.
     * @param scale The scale factor for the timeline.
     * @param isPdf Whether the output is for PDF.
     * @return The SVG string representation of the timeline.
     */
    fun makeTimelineSvg(entries: MutableList<Entry>, title: String, scale: String, isPdf: Boolean): String {
        this.pdf = isPdf
        
        return when (orientation) {
            TimelineOrientation.HORIZONTAL -> makeHorizontalTimelineSvg(entries, title, scale, isPdf)
            TimelineOrientation.VERTICAL -> makeVerticalTimelineSvg(entries, title, scale, isPdf)
        }
    }
    
    /**
     * Creates a horizontal timeline SVG (original implementation with vertical spine).
     *
     * @param entries The list of timeline entries.
     * @param title The title of the timeline.
     * @param scale The scale factor for the timeline.
     * @param isPdf Whether the output is for PDF.
     * @return The SVG string representation of the horizontal timeline.
     */
    private fun makeHorizontalTimelineSvg(entries: MutableList<Entry>, title: String, scale: String, isPdf: Boolean): String {
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
        sb.append("""<svg width="$scaledWidth" height="$scaledHeight" viewBox="0 0 $totalWidth $totalHeight" xmlns="http://www.w3.org/2000/svg"  xmlns:xlink="http://www.w3.org/1999/xlink" id="tl_$id">""".trimIndent())

        val defs = modernDefs(isPdf, id)
        sb.append(defs.first)
        val colors = defs.second

        // Add the background as a scalable SVG rectangle instead of CSS style
        val backgroundGradient = if (useDark) "#0a0a0a" else "#fafafa"
        sb.append("""
    <!-- Scalable background -->
    <rect width="$totalWidth" height="$totalHeight" fill="url(#backgroundGradient_$id)"/>
    """.trimIndent())

        // Title and content
        sb.append("""
    <text x="${totalWidth/2}" y="50" text-anchor="middle" class="timeline-title">
          ${title.escapeXml()}
    </text>
    """.trimIndent())

        // Timeline spine
        val spineX = sideMargin + cardWidth + (spineWidth / 2)
        val spineStartY = topMargin - 30
        val spineEndY = totalHeight - bottomMargin + 30

        sb.append("""
    <!-- Timeline spine -->
    <line x1="$spineX" y1="$spineStartY" x2="$spineX" y2="$spineEndY" class="timeline-spine"/>
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
    
    /**
     * Creates a vertical timeline SVG (horizontal spine with entries above/below).
     *
     * @param entries The list of timeline entries.
     * @param title The title of the timeline.
     * @param scale The scale factor for the timeline.
     * @param isPdf Whether the output is for PDF.
     * @return The SVG string representation of the vertical timeline.
     */
    private fun makeVerticalTimelineSvg(entries: MutableList<Entry>, title: String, scale: String, isPdf: Boolean): String {
        val sb = StringBuilder()
        val id = UUID.randomUUID().toString()

        // Normalize scale
        val scaleF = maxOf(0.1f, minOf(5.0f, scale.toFloatOrNull() ?: 1.0f))

        // Calculate base dimensions - wider cards with modern iOS-style look
        val cardWidth = 240 // Increased from 180 to provide more space for text
        val cardPadding = 16 // Increased padding for better text spacing
        val contentWidth = cardWidth - (cardPadding * 2)
        val fontSize = 13 // Slightly larger font for better readability
        val lineHeight = 18 // Increased line height for better text spacing

        val topMargin = 80 // Reduced from 100
        val bottomMargin = 50
        val leftMargin = 80
        val rightMargin = 80
        val spineHeight = 40 // Reduced from 60
        val cardSpacing = 25 // Reduced from 40 for more condensed layout

        // Use fixed card height like in the API timeline design
        val cardHeights = mutableListOf<Int>()
        entries.forEach { entry ->
            // Calculate height based on text content to accommodate wiki links
            val textHeight = calculateTextHeight(entry.text, contentWidth, fontSize, lineHeight)
            val headerHeight = 30 // Header height for date
            // Allow card to grow as needed based on content
            val cardHeight = maxOf(60, headerHeight + textHeight + cardPadding)
            cardHeights.add(cardHeight)
        }

        // Calculate total width based on entries
        val totalWidth = if (entries.isNotEmpty()) {
            leftMargin + (cardWidth * entries.size) + (cardSpacing * (entries.size - 1)) + rightMargin
        } else {
            600
        }

        // Calculate total height - account for cards both above and below the timeline
        // In the API design, the timeline is in the middle with cards above and below
        val maxCardHeight = cardHeight(cardHeights)
        // We need space for cards above and below, plus margins
        val totalHeight = topMargin + maxCardHeight + spineHeight + maxCardHeight + bottomMargin

        // Calculate scaled dimensions
        val scaledWidth = (totalWidth * scaleF).toInt()
        val scaledHeight = (totalHeight * scaleF).toInt()

        // SVG WITHOUT background styling in the element itself
        sb.append("""<svg width="$scaledWidth" height="$scaledHeight" viewBox="0 0 $totalWidth $totalHeight" xmlns="http://www.w3.org/2000/svg"  xmlns:xlink="http://www.w3.org/1999/xlink" id="tl_$id">""".trimIndent())

        val defs = modernDefs(isPdf, id)
        sb.append(defs.first)
        val colors = defs.second

        // Add the background as a scalable SVG rectangle instead of CSS style
        val backgroundGradient = if (useDark) "#0a0a0a" else "#fafafa"
        sb.append("""
    <!-- Scalable background -->
    <rect width="$totalWidth" height="$totalHeight" fill="url(#backgroundGradient_$id)"/>
    """.trimIndent())

        // Title and content
        sb.append("""
    <text x="${totalWidth/2}" y="50" text-anchor="middle" class="timeline-title">
          ${title.escapeXml()}
    </text>
    """.trimIndent())

        // Timeline spine (horizontal line) - position in the middle to allow cards above and below
        val spineY = topMargin + maxCardHeight + (spineHeight / 2)
        val spineStartX = leftMargin - 30
        val spineEndX = totalWidth - rightMargin + 30

        sb.append("""
    <!-- Timeline spine (horizontal) -->
    <line x1="$spineStartX" y1="$spineY" x2="$spineEndX" y2="$spineY" class="timeline-spine"/>
    """.trimIndent())

        // Generate timeline entries
        var currentX = leftMargin
        entries.forEachIndexed { index, entry ->
            val gradIndex = index % colors.size
            val cardHeight = cardHeights[index]

            sb.append(verticalTimelineEntry(
                index, entry, colors[gradIndex], gradIndex, id,
                currentX, cardHeight, totalWidth, totalHeight, cardWidth, cardPadding,
                contentWidth, fontSize, lineHeight, spineY
            ))

            currentX += cardWidth + cardSpacing
        }

        sb.append("</svg>")
        return sb.toString()
    }
    
    /**
     * Helper function to calculate the maximum card height.
     */
    private fun cardHeight(cardHeights: List<Int>): Int {
        return if (cardHeights.isNotEmpty()) cardHeights.maxOrNull()!! else 150
    }
    
    /**
     * Creates a timeline entry for the vertical timeline.
     *
     * @param index The index of the entry.
     * @param entry The entry to render.
     * @param color The color for the entry.
     * @param gradIndex The gradient index for the entry.
     * @param id The unique identifier for the timeline.
     * @param xPosition The x position of the entry.
     * @param cardHeight The height of the card.
     * @param totalWidth The total width of the timeline.
     * @param cardWidth The width of the card.
     * @param cardPadding The padding of the card.
     * @param contentWidth The width of the content.
     * @param fontSize The font size for the text.
     * @param lineHeight The line height for the text.
     * @param spineY The y position of the timeline spine.
     * @return The SVG string representation of the timeline entry.
     */
    private fun verticalTimelineEntry(
        index: Int, entry: Entry, color: String, gradIndex: Int, id: String,
        xPosition: Int, cardHeight: Int, totalWidth: Int, totalHeight: Int, cardWidth: Int,
        cardPadding: Int, contentWidth: Int, fontSize: Int, lineHeight: Int,
        spineY: Int
    ): String {
        val isAbove = index % 2 == 0 // Alternate entries above and below the timeline
        val headerHeight = 36 // Increased header height for modern iOS look
        val cardRadius = 16 // Increased radius for modern iOS-style rounded corners
        val dotX = xPosition + (cardWidth / 2)
        val dotY = spineY
        val cardY = if (isAbove) spineY - cardHeight - 10 else spineY + 10 // Reduced spacing
        
        // Create a unique ID for this entry for detail view
        val entryId = "entry_${id}_$index"
        
        // Detail view popup (only shown when clicked)
        val detailView = if (enableDetailView) {
            """
            <!-- Detail view for entry ${index + 1} -->
            <g id="detail_$entryId" style="display: none;">
                <!-- Semi-transparent overlay -->
                <rect x="0" y="0" width="$totalWidth" height="$totalHeight" 
                      fill="rgba(0,0,0,0.5)" onclick="document.getElementById('detail_$entryId').style.display='none'"/>
                
                <!-- Detail card -->
                <rect x="${totalWidth/2 - 200}" y="${totalHeight/2 - 150}" width="400" height="300" 
                      rx="16" ry="16" fill="${if (useGlass) "url(#glassGradient)" else cardBackgroundColor}"
                      stroke="${if (useGlass) "rgba(255,255,255,0.3)" else separatorColor}" stroke-width="1"/>
                
                <!-- Header -->
                <rect x="${totalWidth/2 - 200}" y="${totalHeight/2 - 150}" width="400" height="50" 
                      rx="16" ry="16" fill="url(#timeline_grad_$gradIndex)"/>
                <rect x="${totalWidth/2 - 200}" y="${totalHeight/2 - 150 + 16}" width="400" height="34" 
                      fill="url(#timeline_grad_$gradIndex)"/>
                
                <!-- Close button -->
                <circle cx="${totalWidth/2 + 180}" cy="${totalHeight/2 - 130}" r="15" 
                        fill="rgba(0,0,0,0.3)" stroke="white" stroke-width="1"
                        onclick="document.getElementById('detail_$entryId').style.display='none'"
                        style="cursor: pointer;"/>
                <text x="${totalWidth/2 + 180}" y="${totalHeight/2 - 125}" text-anchor="middle" 
                      fill="white" font-size="16" font-weight="bold"
                      onclick="document.getElementById('detail_$entryId').style.display='none'"
                      style="cursor: pointer;">×</text>
                
                <!-- Date -->
                <text x="${totalWidth/2 - 180}" y="${totalHeight/2 - 115}" 
                      class="timeline-text timeline-date" fill="white">
                      ${entry.date.escapeXml()}
                </text>
                
                <!-- Content -->
                <text x="${totalWidth/2 - 180}" y="${totalHeight/2 - 80}" 
                      class="timeline-text timeline-content">
                      ${wrapTextWithLinksToTspans(entry.text, (totalWidth/2 - 180).toInt(), (totalHeight/2 - 80).toInt(), 360, lineHeight, fontSize)}
                </text>
            </g>
            """
        } else ""
        
        // Click handler for showing detail view
        val clickHandler = if (enableDetailView) {
            """onclick="document.getElementById('detail_$entryId').style.display='block'" style="cursor: pointer;" """
        } else ""

        return """
    <!-- Timeline Entry ${index + 1} -->
    <g class="timeline-entry" id="$entryId" $clickHandler>
        <!-- Straight connector line (no dash) to match API design -->
        <line x1="$dotX" y1="${if (isAbove) cardY + cardHeight else cardY}" 
              x2="$dotX" y2="$spineY" 
              stroke="${separatorColor}" stroke-width="1.5"/>
        
        <!-- Timeline dot with concentric circles like in API design -->
        <circle cx="$dotX" cy="$dotY" r="8" 
                fill="url(#timeline_grad_$gradIndex)" 
                filter="url(#cardShadow)"/>
        <circle cx="$dotX" cy="$dotY" r="4" 
                fill="#ffffff"/>
        
        <!-- Modern iOS-style card with white background and enhanced drop shadow -->
        <rect x="$xPosition" y="$cardY" width="$cardWidth" height="$cardHeight" 
              rx="$cardRadius" ry="$cardRadius" fill="#ffffff" filter="url(#cardShadow)" class="timeline-card"/>
        
        <!-- Colored header section to match detail view -->
        <rect x="$xPosition" y="$cardY" 
              width="$cardWidth" height="$headerHeight" 
              rx="$cardRadius" ry="$cardRadius" 
              fill="url(#timeline_grad_$gradIndex)"/>
        <rect x="$xPosition" y="${cardY + cardRadius}" 
              width="$cardWidth" height="${headerHeight - cardRadius}" 
              fill="url(#timeline_grad_$gradIndex)"/>
        
        <!-- Date text -->
        <text x="${xPosition + cardWidth/2}" y="${cardY + 20}" 
              class="marker-date" text-anchor="middle" fill="white">
              ${entry.date.escapeXml()}
        </text>
        
        <!-- Event text with wiki-style links support -->
        <text x="${xPosition + cardPadding}" y="${cardY + 50}" 
              class="marker-event">
              <!-- Using a nested SVG to handle wiki links while maintaining marker-event class -->
              <tspan x="${xPosition + cardPadding}" dy="0">
                ${wrapTextWithLinksToTspans(entry.text, xPosition + cardPadding, cardY + 50, contentWidth, lineHeight, fontSize)}
              </tspan>
        </text>
        
        <!-- Small colored circle in corner like in API design -->
        <circle cx="${xPosition + cardWidth - 20}" cy="${cardY + 15}" r="3" 
                fill="url(#timeline_grad_$gradIndex)"/>
    </g>
    $detailView
    """.trimIndent()
    }

    private fun modernDefs(isPdf: Boolean, id: String): Pair<String, List<String>> {
        val colors = DEFAULT_COLORS.shuffled()

        val shadowFilter = if (useDark) {
            """
        <!-- Modern iOS-style shadow for dark mode -->
        <filter id="cardShadow" x="-50%" y="-50%" width="200%" height="200%">
            <!-- First shadow layer - closer, sharper shadow -->
            <feGaussianBlur in="SourceAlpha" stdDeviation="2"/>
            <feOffset dx="0" dy="3" result="offsetBlur1"/>
            <feFlood flood-color="#000000" flood-opacity="0.3"/>
            <feComposite in2="offsetBlur1" operator="in" result="shadow1"/>
            
            <!-- Second shadow layer - further, softer shadow for depth -->
            <feGaussianBlur in="SourceAlpha" stdDeviation="5"/>
            <feOffset dx="0" dy="8" result="offsetBlur2"/>
            <feFlood flood-color="#000000" flood-opacity="0.2"/>
            <feComposite in2="offsetBlur2" operator="in" result="shadow2"/>
            
            <feMerge> 
                <feMergeNode in="shadow2"/>
                <feMergeNode in="shadow1"/>
                <feMergeNode in="SourceGraphic"/> 
            </feMerge>
        </filter>
        """
        } else {
            """
        <!-- Modern iOS-style shadow for light mode -->
        <filter id="cardShadow" x="-50%" y="-50%" width="200%" height="200%">
            <!-- First shadow layer - closer, sharper shadow -->
            <feGaussianBlur in="SourceAlpha" stdDeviation="1.5"/>
            <feOffset dx="0" dy="2" result="offsetBlur1"/>
            <feFlood flood-color="#000000" flood-opacity="0.1"/>
            <feComposite in2="offsetBlur1" operator="in" result="shadow1"/>
            
            <!-- Second shadow layer - further, softer shadow for depth -->
            <feGaussianBlur in="SourceAlpha" stdDeviation="4"/>
            <feOffset dx="0" dy="6" result="offsetBlur2"/>
            <feFlood flood-color="#000000" flood-opacity="0.08"/>
            <feComposite in2="offsetBlur2" operator="in" result="shadow2"/>
            
            <feMerge> 
                <feMergeNode in="shadow2"/>
                <feMergeNode in="shadow1"/>
                <feMergeNode in="SourceGraphic"/> 
            </feMerge>
        </filter>
        """
        }

        // Glass-specific definitions
        val glassDefinitions = if (useGlass) {
            """
        <!-- Glass gradients -->
        <linearGradient id="glassGradient" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" style="stop-color:rgba(255,255,255,0.3);stop-opacity:1" />
            <stop offset="50%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
            <stop offset="100%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1" />
        </linearGradient>
        <radialGradient id="glassRadial" cx="30%" cy="30%" r="70%">
            <stop offset="0%" style="stop-color:rgba(255,255,255,0.4);stop-opacity:1" />
            <stop offset="70%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
            <stop offset="100%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1" />
        </radialGradient>
        <linearGradient id="highlight" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" style="stop-color:rgba(255,255,255,0.6);stop-opacity:1" />
            <stop offset="100%" style="stop-color:rgba(255,255,255,0);stop-opacity:1" />
        </linearGradient>

        <!-- Glass filters -->
        <filter id="blur" x="-50%" y="-50%" width="200%" height="200%">
            <feGaussianBlur in="SourceGraphic" stdDeviation="3" />
        </filter>
        <filter id="shadow" x="-50%" y="-50%" width="200%" height="200%">
            <feDropShadow dx="0" dy="4" stdDeviation="8" flood-color="rgba(0,0,0,0.3)"/>
        </filter>
        <filter id="innerShadow" x="-50%" y="-50%" width="200%" height="200%">
            <feOffset dx="0" dy="2"/>
            <feGaussianBlur stdDeviation="3" result="offset-blur"/>
            <feFlood flood-color="rgba(0,0,0,0.3)"/>
            <feComposite in2="offset" operator="in"/>
            <feComposite in2="SourceGraphic" operator="over"/>
        </filter>
        """
        } else {
            ""
        }

        // Add background gradient definition
        val backgroundGradient = """
    <linearGradient id="backgroundGradient_$id" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" style="stop-color:${if (useGlass) "#1d4ed8" else fillColor};stop-opacity:1" />
        <stop offset="100%" style="stop-color:${if (useGlass) "#1d4ed8" else if (useDark) "#0a0a0a" else "#fafafa"};stop-opacity:1" />
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
            #tl_$id .timeline-card {
                fill: ${if (useGlass) "url(#glassGradient)" else cardBackgroundColor};
                stroke: ${if (useGlass) "rgba(255,255,255,0.3)" else separatorColor};
                stroke-width: ${if (useGlass) "1" else "0.5"};
                filter: url(#${if (useGlass) "shadow" else "cardShadow"});
                transition: transform 0.2s ease;
            }
            #tl_$id .timeline-card:hover {
                transform: translateY(-2px);
            }
            #tl_$id .timeline-text {
                font-family: $DEFAULT_FONT_FAMILY;
                fill: ${if (useGlass) "white" else textColor};
                text-rendering: optimizeLegibility;
            }
            #tl_$id .timeline-title {
                font-size: 32px;
                font-family: $DEFAULT_FONT_FAMILY;
                font-weight: 800;
                letter-spacing: -1px;
                fill: ${if (useGlass) "white" else textColor};
            }
            #tl_$id .timeline-date {
                font-size: 13px;
                font-weight: 700;
                text-transform: uppercase;
                letter-spacing: 0.5px;
            }
            #tl_$id .timeline-content {
                font-size: 15px;
                font-weight: 400;
                line-height: 1.5;
                fill: ${if (useGlass) "rgba(255,255,255,0.9)" else textColor};
            }
            #tl_$id .timeline-spine {
                stroke: ${if (useGlass) "rgba(255,255,255,0.4)" else separatorColor};
                stroke-width: 2;
                stroke-linecap: round;
            }
            #tl_$id .timeline-dot {
                stroke: ${if (useGlass) "rgba(255,255,255,0.4)" else cardBackgroundColor};
                stroke-width: 3;
                filter: ${if (useGlass) "url(#shadow)" else "drop-shadow(0 2px 4px rgba(0,0,0,0.1))"};
            }
            #tl_$id .timeline-connector {
                stroke: ${if (useGlass) "rgba(255,255,255,0.3)" else separatorColor};
                stroke-width: 1.5;
                stroke-dasharray: 3,3;
                opacity: 0.6;
            }
            /* API timeline specific styles */
            #tl_$id .marker-date {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                font-size: 12px;
                font-weight: 500;
                fill: #374151;
            }
            #tl_$id .marker-event {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                font-size: 14px;
                font-weight: 400;
                fill: #1f2937;
            }
            $linkStyle
        </style>
        $shadowFilter
        $glassDefinitions
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
        
        // Create a unique ID for this entry for detail view
        val entryId = "entry_${id}_$index"
        
        // Detail view popup (only shown when clicked)
        val detailView = if (enableDetailView) {
            val totalHeight = 800 // Approximate height for the detail view
            """
            <!-- Detail view for entry ${index + 1} -->
            <g id="detail_$entryId" style="display: none;">
                <!-- Semi-transparent overlay -->
                <rect x="0" y="0" width="$totalWidth" height="$totalHeight" 
                      fill="rgba(0,0,0,0.5)" onclick="document.getElementById('detail_$entryId').style.display='none'"/>
                
                <!-- Detail card -->
                <rect x="${totalWidth/2 - 200}" y="${totalHeight/2 - 150}" width="400" height="300" 
                      rx="16" ry="16" fill="${if (useGlass) "url(#glassGradient)" else cardBackgroundColor}"
                      stroke="${if (useGlass) "rgba(255,255,255,0.3)" else separatorColor}" stroke-width="1"/>
                
                <!-- Header -->
                <rect x="${totalWidth/2 - 200}" y="${totalHeight/2 - 150}" width="400" height="50" 
                      rx="16" ry="16" fill="url(#timeline_grad_$gradIndex)"/>
                <rect x="${totalWidth/2 - 200}" y="${totalHeight/2 - 150 + 16}" width="400" height="34" 
                      fill="url(#timeline_grad_$gradIndex)"/>
                
                <!-- Close button -->
                <circle cx="${totalWidth/2 + 180}" cy="${totalHeight/2 - 130}" r="15" 
                        fill="rgba(0,0,0,0.3)" stroke="white" stroke-width="1"
                        onclick="document.getElementById('detail_$entryId').style.display='none'"
                        style="cursor: pointer;"/>
                <text x="${totalWidth/2 + 180}" y="${totalHeight/2 - 125}" text-anchor="middle" 
                      fill="white" font-size="16" font-weight="bold"
                      onclick="document.getElementById('detail_$entryId').style.display='none'"
                      style="cursor: pointer;">×</text>
                
                <!-- Date -->
                <text x="${totalWidth/2 - 180}" y="${totalHeight/2 - 115}" 
                      class="timeline-text timeline-date" fill="white">
                      ${entry.date.escapeXml()}
                </text>
                
                <!-- Content -->
                <text x="${totalWidth/2 - 180}" y="${totalHeight/2 - 80}" 
                      class="timeline-text timeline-content">
                      ${wrapTextWithLinksToTspans(entry.text, (totalWidth/2 - 180).toInt(), (totalHeight/2 - 80).toInt(), 360, lineHeight, fontSize)}
                </text>
            </g>
            """
        } else ""
        
        // Click handler for showing detail view
        val clickHandler = if (enableDetailView) {
            """onclick="document.getElementById('detail_$entryId').style.display='block'" style="cursor: pointer;" """
        } else ""

        return """
    <!-- Timeline Entry ${index + 1} -->
    <g class="timeline-entry" id="$entryId" $clickHandler>
        <!-- Dashed connector line -->
        <line x1="${if (isLeft) xPosition + cardWidth else xPosition}" y1="$dotY"  x2="$spineX"  y2="$dotY" class="timeline-connector"/>

        <!-- Enhanced timeline dot -->
        <circle cx="$spineX" cy="$dotY" r="8" fill="${if (useGlass) "url(#glassRadial)" else "url(#timeline_grad_$gradIndex)"}" class="timeline-dot"/>
        ${if (useGlass) """
        <!-- Circle highlight -->
        <ellipse cx="${spineX-3}" cy="${dotY-3}" rx="3" ry="2" fill="rgba(255,255,255,0.5)"/>
        """ else ""}

        <!-- Elegant card with enhanced shadow -->
        <rect x="$xPosition" y="$yPosition" width="$cardWidth" height="$cardHeight" rx="$cardRadius" ry="$cardRadius" class="timeline-card"/>

        <!-- Gradient header with subtle design -->
        <rect x="$xPosition" y="$yPosition" 
              width="$cardWidth" height="$headerHeight" 
              rx="$cardRadius" ry="$cardRadius" 
              fill="${if (useGlass) "url(#glassGradient)" else "url(#timeline_grad_$gradIndex)"}"/>
        <rect x="$xPosition" y="${yPosition + cardRadius}" 
              width="$cardWidth" height="${headerHeight - cardRadius}" 
              fill="${if (useGlass) "url(#glassGradient)" else "url(#timeline_grad_$gradIndex)"}"/>
        ${if (useGlass) """
        <!-- Card highlight -->
        <rect x="${xPosition + 5}" y="${yPosition + 5}" width="${cardWidth - 10}" height="15" rx="7" fill="url(#highlight)"/>
        """ else ""}

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
    $detailView
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
    val maker = TimelineMaker(false, "#a1d975", useGlass = true)
    val entries = TimelineParser().parse(entry)
    val svg = maker.makeTimelineSvg(entries.entries, "Literary Periods", "0.6", false)
    val f = File("gen/timeline_normal.svg")
    f.writeBytes(svg.toByteArray())

    // Test PDF output
    val makerPdf = TimelineMaker(false, "#a1d975", useGlass = true)
    val svgPdf = makerPdf.makeTimelineSvg(entries.entries, "Literary Periods", "1", true)
    val fPdf = File("gen/timeline_pdf.svg")
    fPdf.writeBytes(svgPdf.toByteArray())

    println("Test completed. Check gen/timeline_normal.svg and gen/timeline_pdf.svg")
}
