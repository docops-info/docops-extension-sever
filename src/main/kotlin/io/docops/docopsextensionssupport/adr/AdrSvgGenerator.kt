package io.docops.docopsextensionssupport.adr

import io.docops.docopsextensionssupport.util.UrlUtil.urlEncode


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
            |    .participant-container {
            |      cursor: pointer;
            |    }
            |    .participant-container:hover .participant-icon {
            |      filter: drop-shadow(0px 0px 3px rgba(0, 122, 255, 0.5));
            |      transition: filter 0.3s ease;
            |    }
            |    .participant-container:hover .participant-name {
            |      fill: #007AFF;
            |      transition: fill 0.3s ease;
            |    }
            |    .group-chat-link {
            |      font-family: Arial, Helvetica, sans-serif;
            |      font-weight: 400;
            |      font-size: 12px;
            |      fill: #007AFF;
            |      text-decoration: underline;
            |      cursor: pointer;
            |    }
            |    a {
            |      cursor: pointer;
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
     * Wraps text to fit within the specified width, preserving wiki links.
     */
    private fun wrapText(text: String, maxCharsPerLine: Int): List<String> {
        // First, extract wiki links and replace them with placeholders
        val linkPattern = "\\[\\[([^\\s]+)\\s+(.*?)\\]\\]".toRegex()
        val links = mutableListOf<String>()
        var modifiedText = text

        linkPattern.findAll(text).forEach { matchResult ->
            val link = matchResult.value
            links.add(link)
            // Replace the link with a placeholder that won't be split
            modifiedText = modifiedText.replace(link, "LINK_PLACEHOLDER_${links.size - 1}")
        }

        // Now wrap the text with placeholders
        val words = modifiedText.split(" ")
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

        // Finally, replace the placeholders with the actual links
        val resultLines = lines.map { line ->
            var result = line
            for (i in links.indices) {
                result = result.replace("LINK_PLACEHOLDER_$i", links[i])
            }
            result
        }

        return resultLines
    }

    /**
     * Renders a section of text with proper formatting for bullets and links.
     */
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
                // Check if this line contains wiki links using the same pattern as AdrParser
                // Pattern to match wiki links with format [[url label]]
                // This pattern will match any characters for the URL until the first space,
                // then capture the rest as the label until the closing brackets
                val linkPattern = "\\[\\[([^\\s]+)\\s+(.*?)\\]\\]".toRegex()
                val containsWikiLink = linkPattern.containsMatchIn(wrappedLine)


                if (containsWikiLink) {
                    // Render line with links
                    renderLineWithLinks(svg, wrappedLine, textX, currentY, linkPattern)
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
    private fun renderLineWithLinks(svg: StringBuilder, line: String, x: Int, y: Int, linkPattern: Regex) {
        // Create a text element as a container
        svg.append("""<text x="$x" y="$y" class="content">""")

        var remainingText = line
        var currentX = 0

        // Process all links in the line
        while (true) {
            val matchResult = linkPattern.find(remainingText)

            if (matchResult == null) {
                // No more links, add remaining text
                if (remainingText.isNotEmpty()) {
                    svg.append("""<tspan>${escapeXml(remainingText)}</tspan>""")
                }
                break
            }

            val beforeLink = remainingText.substring(0, matchResult.range.first)
            val url = matchResult.groupValues[1]
            val label = matchResult.groupValues[2]

            // Add text before the link
            if (beforeLink.isNotEmpty()) {
                svg.append("""<tspan>${escapeXml(beforeLink)}</tspan>""")
            }

            // Add the link with proper SVG link styling
            svg.append("""<tspan>""")
            svg.append("""<a href="${escapeXml(url)}" target="_blank">""")
            svg.append("""<tspan style="fill:#007AFF; text-decoration:underline;">${escapeXml(label)}</tspan>""")
            svg.append("""</a>""")
            svg.append("""</tspan>""")

            // Update remaining text to everything after this match
            remainingText = remainingText.substring(matchResult.range.last + 1)
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
     * Detects if a participant name contains an email address.
     * @return The email address if found, null otherwise
     */
    private fun extractEmail(name: String): String? {
        // Simple regex to match email addresses
        val emailRegex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}".toRegex()
        val matchResult = emailRegex.find(name)
        return matchResult?.value
    }

    /**
     * Renders a participant with icon and name.
     * If the participant name contains an email, adds a Microsoft Teams chat link.
     * If the participant name contains a wiki link, applies the hover glow effect.
     */
    private fun renderParticipant(svg: StringBuilder, name: String, x: Int, y: Int, width: Int, status: AdrStatus): Int {
        // Check if name contains an email
        val email = extractEmail(name)

        // Check if name contains a wiki link
        val linkPattern = "\\[\\[([^\\s]+)\\s+(.*?)\\]\\]".toRegex()
        val hasLink = linkPattern.containsMatchIn(name)

        // Start participant container for hover effect if email is present or has link
        if (email != null || hasLink) {
            svg.append("""<g class="participant-container">""")
        } else {
            svg.append("""<g>""")
        }

        // Icon
        val color = STATUS_COLORS[status] ?: "#999999"
        svg.append("""<use href="#user-icon" x="${x + (width/2) - 15}" y="$y" width="30" height="30" fill="$color" class="participant-icon" />""")

        // Name (centered under icon)
        val escapedName = escapeXml(name)
        val wrappedName = wrapText(escapedName, 20)
        var currentY = y + 40

        for (line in wrappedName) {
            if (email != null) {
                // Create a Teams chat link if email is present
                val teamsUrl = "https://teams.microsoft.com/l/chat/0/0?users=${escapeXml(email)}"
                svg.append("""<a href="${teamsUrl}" target="_blank" title="Chat with ${escapeXml(email)}">""")
                svg.append("""<text x="${x + width/2}" y="$currentY" class="participant-name">$line</text>""")
                svg.append("""</a>""")
            } else {
                svg.append("""<text x="${x + width/2}" y="$currentY" class="participant-name">$line</text>""")
            }
            currentY += 15
        }

        // Close participant container
        svg.append("""</g>""")

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

            // Collect emails for group chat link
            val participantEmails = mutableListOf<String>()

            // First pass to collect emails
            for (participant in adr.participants) {
                val email = extractEmail(participant)
                if (email != null) {
                    participantEmails.add(email)
                }
            }

            // Add group chat link if there are 2+ participants with emails
            if (participantEmails.size >= 2) {
                val groupChatUrl = "https://teams.microsoft.com/l/chat/0/0?users=${participantEmails.joinToString(",")}&topicName=${adr.title.urlEncode()}"
                svg.append("""<a href="${escapeXml(groupChatUrl)}" target="_blank">""")
                svg.append("""<text x="${contentX + MAX_CARD_WIDTH - 150}" y="${currentY + 25}" class="group-chat-link">Start Group Chat</text>""")
                svg.append("""</a>""")
            }

            // Render participants
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
        return svg.toString()
    }

    /**
     * Extension function to capitalize the first letter of a string.
     */
    private fun String.capitalize(): String {
        return if (this.isEmpty()) this else this[0].uppercase() + this.substring(1)
    }
}
