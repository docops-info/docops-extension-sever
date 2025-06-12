package io.docops.docopsextensionssupport.adr

import io.docops.docopsextensionssupport.svgsupport.addSvgMetadata


/**
 * Generates SVG diagrams for Architecture Decision Records (ADRs).
 * Creates iOS-style cards with sections for title, date, status, context, decision, consequences, and participants.
 */
class AdrSvgGenerator {
    companion object {
        // SVG header and footer
        private val SVG_HEADER = """<?xml version="1.0" encoding="UTF-8" standalone="no"?>
            |<svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 %d %d" preserveAspectRatio='xMidYMid meet'>
            |<defs>
            |  <style type="text/css">
            |    .card { 
            |      fill: #ffffff; 
            |      stroke: #e1e1e1; 
            |      stroke-width: 1; 
            |      filter: drop-shadow(0px 2px 4px rgba(0, 0, 0, 0.1));
            |    }
            |    .title { 
            |      font-family: Arial, Helvetica,  sans-serif; 
            |      font-weight: 600; 
            |      font-size: 20px; 
            |      fill: #000000; 
            |    }
            |    .subtitle { 
            |      font-family: Arial, Helvetica,  sans-serif; 
            |      font-weight: 500; 
            |      font-size: 16px; 
            |      fill: #666666; 
            |    }
            |    .status { 
            |      font-family: Arial, Helvetica,  sans-serif; 
            |      font-weight: 500; 
            |      font-size: 14px; 
            |      fill: #ffffff; 
            |    }
            |    .content { 
            |      font-family: Arial, Helvetica,  sans-serif; 
            |      font-weight: 400; 
            |      font-size: 14px; 
            |      fill: #333333; 
            |    }
            |    .section-title { 
            |      font-family: Arial, Helvetica,  sans-serif; 
            |      font-weight: 600; 
            |      font-size: 16px; 
            |      fill: #333333; 
            |    }
            |    .participant-name { 
            |      font-family: Arial, Helvetica,  sans-serif; 
            |      font-weight: 400; 
            |      font-size: 12px; 
            |      fill: #333333; 
            |      text-anchor: middle; 
            |    }
            |  </style>
            |  <!-- Font Awesome style user icon -->
            |  <symbol id="user-icon" viewBox="0 0 448 512">
            |    <path stroke="%s" stroke-width="1" d="M224 256c70.7 0 128-57.3 128-128S294.7 0 224 0 96 57.3 96 128s57.3 128 128 128zm89.6 32h-16.7c-22.2 10.2-46.9 16-72.9 16s-50.6-5.8-72.9-16h-16.7C60.2 288 0 348.2 0 422.4V464c0 26.5 21.5 48 48 48h352c26.5 0 48-21.5 48-48v-41.6c0-74.2-60.2-134.4-134.4-134.4z"/>
            |  </symbol>
            |</defs>""".trimMargin()

        private const val SVG_FOOTER = "</svg>"

        private const val BACK_CARD = """<rect x="0" y="0" width="100%" height="100%" class="card" rx="10" ry="10"/>"""
        // Default dimensions and spacing
        private const val DEFAULT_WIDTH = 700
        private const val DEFAULT_PADDING = 20
        private const val CARD_SPACING = 20
        private const val SECTION_SPACING = 15
        private const val TEXT_LINE_HEIGHT = 20
        private const val CARD_PADDING = 15
        private const val MAX_CARD_WIDTH = DEFAULT_WIDTH - 2*DEFAULT_PADDING  // DEFAULT_WIDTH - 2*DEFAULT_PADDING
        private const val MAX_TEXT_WIDTH = MAX_CARD_WIDTH - 2*CARD_PADDING  // MAX_CARD_WIDTH - 2*CARD_PADDING
        private const val CHARS_PER_LINE = 80   // Approximate characters per line

        // Status colors
        private val STATUS_COLORS = mapOf(
            AdrStatus.Proposed to "#FF9500",    // Orange
            AdrStatus.Accepted to "#34C759",    // Green
            AdrStatus.Superseded to "#AF52DE",  // Purple
            AdrStatus.Deprecated to "#FF3B30",  // Red
            AdrStatus.Rejected to "#FF3B30"     // Red
        )
    }

    /**
     * Escapes special XML characters in text.
     */
    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;")
    }

    /**
     * Estimates the height needed for text based on content and width.
     */
    private fun estimateTextHeight(text: List<String>, maxWidth: Int): Int {
        val linesCount = text.sumOf { line ->
            val isBullet = line.trim().startsWith("-") || line.trim().startsWith("*")
            val textToMeasure = if (isBullet) line.trim().substring(1).trim() else line

            // Estimate lines needed based on character count
            val estimatedLines = Math.ceil(textToMeasure.length.toDouble() / CHARS_PER_LINE).toInt()
            Math.max(1, estimatedLines)
        }

        return linesCount * TEXT_LINE_HEIGHT
    }

    /**
     * Wraps text to fit within the specified width.
     */
    private fun wrapText(text: String, maxCharsPerLine: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = kotlin.text.StringBuilder()

        for (word in words) {
            if (currentLine.length + word.length + 1 <= maxCharsPerLine) {
                if (currentLine.isNotEmpty()) {
                    currentLine.append(" ")
                }
                currentLine.append(word)
            } else {
                lines.add(currentLine.toString())
                currentLine = kotlin.text.StringBuilder(word)
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }

        return lines
    }

    /**
     * Renders a section of text with proper formatting for bullets and links.
     */
    private fun renderTextSection(svg: StringBuilder, text: List<String>, x: Int, y: Int, maxWidth: Int, adr: Adr): Int {
        var currentY = y

        for (line in text) {
            val isBullet = line.trim().startsWith("-") || line.trim().startsWith("*")
            val textToRender = if (isBullet) line.trim().substring(1).trim() else line
            val textX = if (isBullet) x + 15 else x  // Indent bullet points

            // Add bullet if needed
            if (isBullet) {
                svg.append("""<circle cx="${x + 5}" cy="${currentY - 5}" r="3" fill="#333333" />""")
            }

            // Wrap text if needed
            val wrappedLines = wrapText(textToRender, CHARS_PER_LINE)

            for (wrappedLine in wrappedLines) {
                // Check if this line contains wiki links
                val containsWikiLink = adr.links.any { link ->
                    wrappedLine.contains("[[${link.url} ${link.label}]]")
                }

                if (containsWikiLink) {
                    // Render line with links
                    renderLineWithLinks(svg, wrappedLine, textX, currentY, adr.links)
                } else {
                    // Render normal text
                    val escapedLine = escapeXml(wrappedLine)
                    svg.append("""<text x="$textX" y="$currentY" class="content">$escapedLine</text>""")
                }

                currentY += TEXT_LINE_HEIGHT
            }
        }

        return currentY
    }

    /**
     * Renders a line of text that contains wiki-style links.
     */
    private fun renderLineWithLinks(svg: StringBuilder, line: String, x: Int, y: Int, links: List<WikiLink>) {
        // Create a text element as a container
        svg.append("""<text x="$x" y="$y" class="content">""")

        var currentText = line
        var currentX = 0

        // Process each link in the line
        for (link in links) {
            val linkPattern = "\\[\\[${Regex.escape(link.url)}\\s+${Regex.escape(link.label)}\\]\\]"
            val parts = currentText.split(linkPattern.toRegex(), 2)

            if (parts.size > 1) {
                // Add text before the link
                if (parts[0].isNotEmpty()) {
                    svg.append("""<tspan x="${x + currentX}">${escapeXml(parts[0])}</tspan>""")
                    // Estimate width of text (very rough approximation)
                    currentX += parts[0].length * 7
                }

                // Add the link
                svg.append("""<tspan x="${x + currentX}">""")
                svg.append("""<a href="${escapeXml(link.url)}" target="_blank" style="fill:#007AFF">${escapeXml(link.label)}</a>""")
                svg.append("""</tspan>""")

                // Estimate width of link text
                currentX += link.label.length * 7

                // Update remaining text
                currentText = parts[1]
            }
        }

        // Add any remaining text
        if (currentText.isNotEmpty()) {
            svg.append("""<tspan x="${x + currentX}">${escapeXml(currentText)}</tspan>""")
        }

        svg.append("""</text>""")
    }

    /**
     * Renders a status badge with appropriate color.
     */
    private fun renderStatusBadge(svg: StringBuilder, status: AdrStatus, x: Int, y: Int): Int {
        val statusText = status.name.lowercase().capitalize()
        val color = STATUS_COLORS[status] ?: "#999999"

        svg.append("""<rect x="$x" y="${y - 15}" width="80" height="20" rx="10" ry="10" fill="$color" />""")
        svg.append("""<text x="${x + 40}" y="${y}" class="status" text-anchor="middle">$statusText</text>""")

        return y + 10
    }

    /**
     * Renders a participant with icon and name.
     */
    private fun renderParticipant(svg: StringBuilder, name: String, x: Int, y: Int, width: Int, status: AdrStatus): Int {
        // Icon
        val color = STATUS_COLORS[status] ?: "#999999"
        svg.append("""<use href="#user-icon" x="${x + (width/2) - 15}" y="$y" width="30" height="30" fill="$color" />""")

        // Name (centered under icon)
        val escapedName = escapeXml(name)
        val wrappedName = wrapText(escapedName, 20)
        var currentY = y + 40

        for (line in wrappedName) {
            svg.append("""<text x="${x + width/2}" y="$currentY" class="participant-name">$line</text>""")
            currentY += 15
        }

        return currentY + 10
    }

    /**
     * Generates an SVG diagram for an Architecture Decision Record.
     *
     * @param adr The ADR to visualize
     * @param width Optional width of the SVG (default: 800)
     * @return String containing the complete SVG content
     */
    fun generate(adr: Adr, width: Int = DEFAULT_WIDTH): String {
        val svg = kotlin.text.StringBuilder()
        var currentY = DEFAULT_PADDING
        val contentX = DEFAULT_PADDING + CARD_PADDING

        // Calculate heights for each section
        val titleHeight = 60
        val contextHeight = estimateTextHeight(adr.context, MAX_TEXT_WIDTH) + 40
        val decisionHeight = estimateTextHeight(adr.decision, MAX_TEXT_WIDTH) + 40
        val consequencesHeight = estimateTextHeight(adr.consequences, MAX_TEXT_WIDTH) + 40

        // Calculate participants section height (3 per row)
        val participantsPerRow = 3
        val participantWidth = (MAX_CARD_WIDTH - 2 * CARD_PADDING) / participantsPerRow
        val participantRows = Math.ceil(adr.participants.size.toDouble() / participantsPerRow).toInt()
        val participantsHeight = if (adr.participants.isEmpty()) 0 else participantRows * 80 + 40

        // Calculate total height
        val totalHeight = titleHeight + contextHeight + decisionHeight + consequencesHeight + 
                          participantsHeight + (4 * CARD_SPACING) + (2 * DEFAULT_PADDING)

        val color = STATUS_COLORS[adr.status] ?: "#999999"
        // Add SVG header
        svg.append(String.format(SVG_HEADER, width, totalHeight, width, totalHeight, color))

        // Add background card
        svg.append(BACK_CARD)

        // Title Card
        svg.append("""<rect x="$DEFAULT_PADDING" y="$currentY" width="$MAX_CARD_WIDTH" height="$titleHeight" class="card" rx="10" ry="10"/>""")
        svg.append("""<text x="$contentX" y="${currentY + 30}" class="title">${escapeXml(adr.title)}</text>""")

        // Date and Status
        svg.append("""<text x="$contentX" y="${currentY + 50}" class="subtitle">Date: ${escapeXml(adr.date)}</text>""")
        renderStatusBadge(svg, adr.status, contentX + 200, currentY + 50)

        currentY += titleHeight + CARD_SPACING

        // Context Card
        svg.append("""<rect x="$DEFAULT_PADDING" y="$currentY" width="$MAX_CARD_WIDTH" height="$contextHeight" class="card" rx="10" ry="10"/>""")
        svg.append("""<text x="$contentX" y="${currentY + 25}" class="section-title">Context</text>""")
        renderTextSection(svg, adr.context, contentX, currentY + 50, MAX_TEXT_WIDTH, adr = adr)

        currentY += contextHeight + CARD_SPACING

        // Decision Card
        svg.append("""<rect x="$DEFAULT_PADDING" y="$currentY" width="$MAX_CARD_WIDTH" height="$decisionHeight" class="card" rx="10" ry="10"/>""")
        svg.append("""<text x="$contentX" y="${currentY + 25}" class="section-title">Decision</text>""")
        renderTextSection(svg, adr.decision, contentX, currentY + 50, MAX_TEXT_WIDTH, adr = adr)

        currentY += decisionHeight + CARD_SPACING

        // Consequences Card
        svg.append("""<rect x="$DEFAULT_PADDING" y="$currentY" width="$MAX_CARD_WIDTH" height="$consequencesHeight" class="card" rx="10" ry="10"/>""")
        svg.append("""<text x="$contentX" y="${currentY + 25}" class="section-title">Consequences</text>""")
        renderTextSection(svg, adr.consequences, contentX, currentY + 50, MAX_TEXT_WIDTH, adr = adr)

        currentY += consequencesHeight + CARD_SPACING

        // Participants Card (if any)
        if (adr.participants.isNotEmpty()) {
            svg.append("""<rect x="$DEFAULT_PADDING" y="$currentY" width="$MAX_CARD_WIDTH" height="$participantsHeight" class="card" rx="10" ry="10"/>""")
            svg.append("""<text x="$contentX" y="${currentY + 25}" class="section-title">Participants</text>""")

            var participantY = currentY + 50
            var participantX = contentX

            for ((index, participant) in adr.participants.withIndex()) {
                if (index > 0 && index % participantsPerRow == 0) {
                    participantY += 80
                    participantX = contentX
                }

                renderParticipant(svg, participant, participantX, participantY, participantWidth, adr.status)
                participantX += participantWidth
            }
        }

        // Close SVG
        svg.append(SVG_FOOTER)
        val result = addSvgMetadata(svg.toString())
        return result
    }

    /**
     * Extension function to capitalize the first letter of a string.
     */
    private fun String.capitalize(): String {
        return if (this.isEmpty()) this else this[0].uppercase() + this.substring(1)
    }
}
