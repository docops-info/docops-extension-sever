package io.docops.docopsextensionssupport.adr

import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.util.BackgroundHelper
import io.docops.docopsextensionssupport.util.UrlUtil.urlEncode
import kotlin.math.ceil
import kotlin.math.max
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


/**
 * Generates SVG diagrams for Architecture Decision Records (ADRs).
 * Creates iOS-style cards with sections for title, date, status, context, decision, consequences, and participants.
 */
class AdrSvgGenerator(val useDark: Boolean, val themeName: String = "aurora") {

    private val theme: DocOpsTheme = ThemeFactory.getThemeByName(themeName = themeName, useDark = useDark)

    companion object {

        private const val SVG_FOOTER = "</svg>"

        private const val BACK_CARD = """<rect x="0" y="0" width="100%" height="100%" class="card" rx="10" ry="10"/>"""
        // Default dimensions and spacing
        private const val DEFAULT_WIDTH = 700
        private const val DEFAULT_PADDING = 24
        private const val CARD_SPACING = 16
        private const val SECTION_SPACING = 16
        private const val TEXT_LINE_HEIGHT = 20
        private const val CARD_PADDING = 24
        private const val MAX_CARD_WIDTH = DEFAULT_WIDTH - 2*DEFAULT_PADDING
        private const val MAX_TEXT_WIDTH = MAX_CARD_WIDTH - 2*CARD_PADDING
        private const val CHARS_PER_LINE = 80   // Approximate characters per line

        // Status colors (Tint + High Contrast Text)
        private val STATUS_BG_COLORS = mapOf(
            AdrStatus.Proposed to "#FEF3C7",    // Yellow 100
            AdrStatus.Accepted to "#DCFCE7",    // Green 100
            AdrStatus.Superseded to "#F3E8FF",  // Purple 100
            AdrStatus.Deprecated to "#FEE2E2",  // Red 100
            AdrStatus.Rejected to "#FEE2E2"     // Red 100
        )

        private val STATUS_TEXT_COLORS = mapOf(
            AdrStatus.Proposed to "#92400E",    // Yellow 800
            AdrStatus.Accepted to "#166534",    // Green 800
            AdrStatus.Superseded to "#6B21A8",  // Purple 800
            AdrStatus.Deprecated to "#991B1B",  // Red 800
            AdrStatus.Rejected to "#991B1B"     // Red 800
        )

        private val STATUS_ACCENT_COLORS = mapOf(
            AdrStatus.Proposed to "#FF9500",
            AdrStatus.Accepted to "#34C759",
            AdrStatus.Superseded to "#AF52DE",
            AdrStatus.Deprecated to "#FF3B30",
            AdrStatus.Rejected to "#FF3B30"
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
            val estimatedLines = ceil(textToMeasure.length.toDouble() / CHARS_PER_LINE).toInt()
            max(1, estimatedLines)
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
        var currentLine = StringBuilder()

        for (word in words) {
            if (currentLine.length + word.length + 1 <= maxCharsPerLine) {
                if (currentLine.isNotEmpty()) {
                    currentLine.append(" ")
                }
                currentLine.append(word)
            } else {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
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
                val bulletColor = if (useDark) "#9CA3AF" else "#4B5563"
                svg.append("""<circle cx="${x + 5}" cy="${currentY - 5}" r="3" fill="$bulletColor" />""")
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
            val linkColor = if (useDark) "#60A5FA" else "#3B82F6"
            svg.append("""<tspan>""")
            svg.append("""<a href="${escapeXml(url)}" target="_blank">""")
            svg.append("""<tspan style="fill:$linkColor; text-decoration:underline;">${escapeXml(label)}</tspan>""")
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
        val statusText = status.name
        val bgColor = STATUS_BG_COLORS[status] ?: "#F3F4F6"
        val textColor = STATUS_TEXT_COLORS[status] ?: "#4B5563"

        val badgeWidth = statusText.length * 7 + 16
        svg.append("""<rect x="$x" y="${y - 14}" width="$badgeWidth" height="20" rx="6" fill="$bgColor" />""")
        svg.append("""<text x="${x + badgeWidth / 2}" y="${y}" class="status" text-anchor="middle" fill="$textColor">$statusText</text>""")

        return y + 10
    }

    /**
     * Extracts email and display name from a participant string.
     * @return A Pair of (displayName, email) where email may be null
     */
    private fun extractNameAndEmail(participant: String): Pair<String, String?> {
        // Regex to match different email formats:
        // 1. "Name <email@domain.com>"
        // 2. "Name (email@domain.com)"
        // 3. "Name email@domain.com"
        val emailRegex = "([^<]+)<([^>]+)>|(.+?)\\s+\\(([^@)]+@[^)]+)\\)|(.+?)\\s+([^\\s]+@[^\\s]+)".toRegex()
        val matchResult = emailRegex.find(participant)

        val displayName: String
        val email: String?

        when {
            matchResult != null -> {
                // Format: "Name <email@domain.com>" or "Name (email@domain.com)" or "Name email@domain.com"
                displayName = (matchResult.groupValues[1].takeIf { it.isNotBlank() } 
                    ?: matchResult.groupValues[3].takeIf { it.isNotBlank() }
                    ?: matchResult.groupValues[5].takeIf { it.isNotBlank() })?.trim() ?: participant
                email = (matchResult.groupValues[2].takeIf { it.isNotBlank() }
                    ?: matchResult.groupValues[4].takeIf { it.isNotBlank() }
                    ?: matchResult.groupValues[6].takeIf { it.isNotBlank() })?.trim()
            }
            else -> {
                // No email found, use the whole string as the display name
                displayName = participant
                email = null
            }
        }

        return Pair(displayName, email)
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
     * Creates a participant element with emoji icon and name.
     * If the participant has an email, adds a Microsoft Teams chat link but only displays the name.
     */
    private fun createParticipantElement(participant: Participant, x: Int, y: Int, width: Int, status: AdrStatus): String {
        // Check if name contains a wiki link
        val linkPattern = "\\[\\[([^\\s]+)\\s+(.*?)\\]\\]".toRegex()
        val hasLink = linkPattern.containsMatchIn(participant.name)

        val sb = StringBuilder()

        // Start participant container for hover effect if email is present or has link
        if (participant.email.isNotEmpty() || hasLink) {
            sb.append("""<g class="participant-container">""")
        } else {
            sb.append("""<g>""")
        }

        // Emoji icon (centered) - 32px diameter circles (radius 16)
        val color = if (participant.color.isNotEmpty()) participant.color else (STATUS_BG_COLORS[status] ?: "#F3F4F6")
        sb.append("""<circle cx="${x + (width/2)}" cy="$y" r="16" fill="$color" class="participant-icon" />""")
        sb.append("""<text x="${x + (width/2)}" y="${y + 5}" text-anchor="middle" font-size="16">${participant.emoji}</text>""")

        // Name (centered under icon)
        val escapedName = escapeXml(participant.name)
        val wrappedName = wrapText(escapedName, 20)
        var currentY = y + 36

        for (line in wrappedName) {
            if (participant.email.isNotEmpty()) {
                // Create a Teams chat link if email is present
                val teamsUrl = "https://teams.microsoft.com/l/chat/0/0?users=${escapeXml(participant.email)}"
                sb.append("""<a href="${teamsUrl}" target="_blank" title="Chat with ${escapeXml(participant.email)}">""")
                sb.append("""<text x="${x + width/2}" y="$currentY" class="participant-name-with-email">$line</text>""")
                sb.append("""</a>""")
            } else {
                sb.append("""<text x="${x + width/2}" y="$currentY" class="participant-name">$line</text>""")
            }
            currentY += 15
        }

        // Add title if present
        if (participant.title.isNotEmpty()) {
            sb.append("""<text x="${x + width/2}" y="$currentY" class="participant-title" text-anchor="middle">${escapeXml(participant.title)}</text>""")
            currentY += 15
        }

        // Close participant container
        sb.append("""</g>""")

        return sb.toString()
    }

    /**
     * Renders a participant with emoji icon and name.
     * If the participant has an email, adds a Microsoft Teams chat link.
     * If the participant name contains a wiki link, applies the hover glow effect.
     */
    private fun renderParticipant(svg: StringBuilder, participant: Participant, x: Int, y: Int, width: Int, status: AdrStatus): Int {
        // Create the participant element
        val participantElement = createParticipantElement(participant, x, y, width, status)
        svg.append(participantElement)

        // Calculate the height based on the number of lines in the name and if title is present
        val wrappedName = wrapText(participant.name, 20)
        var currentY = y + 36 + (wrappedName.size * 15)

        // Add extra height if title is present
        if (participant.title.isNotEmpty()) {
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
    @OptIn(ExperimentalUuidApi::class)
    fun generate(adr: Adr, width: Int = DEFAULT_WIDTH): String {
        val svg = StringBuilder()
        val id = Uuid.random().toHexString()
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
        val participantRows = ceil(adr.participants.size.toDouble() / participantsPerRow).toInt()
        val participantsHeight = if (adr.participants.isEmpty()) 0 else participantRows * 70 + 40

        // Calculate references section height
        val referencesHeight = if (adr.references.isEmpty()) 0 else 40 + (adr.references.size * 25)

        // Calculate total height
        val totalHeight = titleHeight + contextHeight + decisionHeight + consequencesHeight + 
                          participantsHeight + referencesHeight + 
                          (if (adr.references.isEmpty()) 4 else 5) * CARD_SPACING + (2 * DEFAULT_PADDING)

        val color = STATUS_BG_COLORS[adr.status] ?: "#F3F4F6"
        val accentColor = STATUS_ACCENT_COLORS[adr.status] ?: "#D1D5DB"
        // Add SVG header with dark mode support
        svg.append(makeSvgHeader(width, totalHeight, color, useDark, id, adr))

        // Add background card
        svg.append("""<rect width="100%" height="100%" fill="${theme.canvas}"/> """)
        // Title Card
        svg.append("""
            <defs>
                <clipPath id="titleClip_$id">
                    <rect x="$DEFAULT_PADDING" y="$currentY" width="$MAX_CARD_WIDTH" height="$titleHeight" rx="12" ry="12"/>
                </clipPath>
            </defs>
            <rect x="$DEFAULT_PADDING" y="$currentY" width="$MAX_CARD_WIDTH" height="$titleHeight" class="card" rx="12" ry="12"/>
            <rect x="$DEFAULT_PADDING" y="$currentY" width="$MAX_CARD_WIDTH" height="6" fill="$accentColor" clip-path="url(#titleClip_$id)"/>
        """)
        svg.append("""<text x="$contentX" y="${currentY + 32}" class="title">${escapeXml(adr.title)}</text>""")

        // Date and Status
        svg.append("""<text x="$contentX" y="${currentY + 52}" class="subtitle">Date: ${escapeXml(adr.date)}</text>""")
        renderStatusBadge(svg, adr.status, contentX + 180, currentY + 52)

        currentY += titleHeight + CARD_SPACING

        // Context Card
        svg.append("""
            <defs>
                <clipPath id="contextClip_$id">
                    <rect x="$DEFAULT_PADDING" y="$currentY" width="$MAX_CARD_WIDTH" height="$contextHeight" rx="12" ry="12"/>
                </clipPath>
            </defs>
            <rect x="$DEFAULT_PADDING" y="$currentY" width="$MAX_CARD_WIDTH" height="$contextHeight" class="card" rx="12" ry="12"/>
            <rect x="$DEFAULT_PADDING" y="$currentY" width="4" height="$contextHeight" fill="$accentColor" clip-path="url(#contextClip_$id)"/>
        """)
        svg.append("""<text x="$contentX" y="${currentY + 28}" class="section-title">Context</text>""")
        renderTextSection(svg, adr.context, contentX, currentY + 52, MAX_TEXT_WIDTH, adr = adr)

        currentY += contextHeight + CARD_SPACING

        // Decision Card
        svg.append("""
            <defs>
                <clipPath id="decisionClip_$id">
                    <rect x="$DEFAULT_PADDING" y="$currentY" width="$MAX_CARD_WIDTH" height="$decisionHeight" rx="12" ry="12"/>
                </clipPath>
            </defs>
            <rect x="$DEFAULT_PADDING" y="$currentY" width="$MAX_CARD_WIDTH" height="$decisionHeight" class="card" rx="12" ry="12"/>
            <rect x="$DEFAULT_PADDING" y="$currentY" width="4" height="$decisionHeight" fill="$accentColor" clip-path="url(#decisionClip_$id)"/>
        """)
        svg.append("""<text x="$contentX" y="${currentY + 28}" class="section-title">Decision</text>""")
        renderTextSection(svg, adr.decision, contentX, currentY + 52, MAX_TEXT_WIDTH, adr = adr)

        currentY += decisionHeight + CARD_SPACING

        // Consequences Card
        svg.append("""
            <defs>
                <clipPath id="consequencesClip_$id">
                    <rect x="$DEFAULT_PADDING" y="$currentY" width="$MAX_CARD_WIDTH" height="$consequencesHeight" rx="12" ry="12"/>
                </clipPath>
            </defs>
            <rect x="$DEFAULT_PADDING" y="$currentY" width="$MAX_CARD_WIDTH" height="$consequencesHeight" class="card" rx="12" ry="12"/>
            <rect x="$DEFAULT_PADDING" y="$currentY" width="4" height="$consequencesHeight" fill="$accentColor" clip-path="url(#consequencesClip_$id)"/>
        """)
        svg.append("""<text x="$contentX" y="${currentY + 28}" class="section-title">Consequences</text>""")
        renderTextSection(svg, adr.consequences, contentX, currentY + 52, MAX_TEXT_WIDTH, adr = adr)

        currentY += consequencesHeight + CARD_SPACING

        // Participants Card (if any)
        if (adr.participants.isNotEmpty()) {
            svg.append("""
                <defs>
                    <clipPath id="participantsClip_$id">
                        <rect x="$DEFAULT_PADDING" y="$currentY" width="$MAX_CARD_WIDTH" height="$participantsHeight" rx="12" ry="12"/>
                    </clipPath>
                </defs>
                <rect x="$DEFAULT_PADDING" y="$currentY" width="$MAX_CARD_WIDTH" height="$participantsHeight" class="card" rx="12" ry="12"/>
                <rect x="$DEFAULT_PADDING" y="$currentY" width="4" height="$participantsHeight" fill="$accentColor" clip-path="url(#participantsClip_$id)"/>
            """)
            svg.append("""<text x="$contentX" y="${currentY + 28}" class="section-title">Participants</text>""")

            var participantY = currentY + 52
            var participantX = contentX

            // Collect emails for group chat link
            val participantEmails = mutableListOf<String>()

            // First pass to collect emails
            for (participant in adr.participants) {
                if (participant.email.isNotEmpty()) {
                    participantEmails.add(participant.email)
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
                    participantY += 70
                    participantX = contentX
                }

                renderParticipant(svg, participant, participantX, participantY, participantWidth, adr.status)
                participantX += participantWidth
            }
        }

        // References Card (if any)
        if (adr.references.isNotEmpty()) {
            currentY += participantsHeight + CARD_SPACING

            svg.append("""
                <defs>
                    <clipPath id="referencesClip_$id">
                        <rect x="$DEFAULT_PADDING" y="$currentY" width="$MAX_CARD_WIDTH" height="$referencesHeight" rx="12" ry="12"/>
                    </clipPath>
                </defs>
                <rect x="$DEFAULT_PADDING" y="$currentY" width="$MAX_CARD_WIDTH" height="$referencesHeight" class="card" rx="12" ry="12"/>
                <rect x="$DEFAULT_PADDING" y="$currentY" width="4" height="$referencesHeight" fill="$accentColor" clip-path="url(#referencesClip_$id)"/>
            """)
            svg.append("""<text x="$contentX" y="${currentY + 28}" class="section-title">References</text>""")

            var linkY = currentY + 52

            // Render each reference as a link
            for (reference in adr.references) {
                svg.append("""<a href="${escapeXml(reference.url)}" target="_blank">""")
                svg.append("""<text x="$contentX" y="$linkY" class="reference-link">${escapeXml(reference.label)}</text>""")
                svg.append("""</a>""")
                linkY += 25
            }
        }

        // Close SVG
        svg.append(SVG_FOOTER)
        return svg.toString()
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun makeSvgHeader(width: Int, height: Int, color: String, darkMode: Boolean = false, id: String, adr: Adr): String {


        val styles = if (darkMode) {
            createDarkModeStyles(id, color)
        } else {
            createLightModeStyles(id, color)
        }

        return  """
        |<svg xmlns="http://www.w3.org/2000/svg" id="id_$id" width="$width" height="$height" viewBox="0 0 $width $height" preserveAspectRatio='xMidYMid meet'>
        |<title>${escapeXml(adr.title)}</title>
        |<desc>Architecture Decision Record: ${escapeXml(adr.title)} - Status: ${adr.status.name}</desc>
        |<defs>
        |  <style type="text/css">
        |  ${theme.fontImport}
        |$styles
        |  </style>
        |  <!-- Font Awesome style user icon -->
        |  <symbol id="user-icon" viewBox="0 0 448 512">
        |    <path stroke="$color" stroke-width="1" d="M224 256c70.7 0 128-57.3 128-128S294.7 0 224 0 96 57.3 96 128s57.3 128 128 128zm89.6 32h-16.7c-22.2 10.2-46.9 16-72.9 16s-50.6-5.8-72.9-16h-16.7C60.2 288 0 348.2 0 422.4V464c0 26.5 21.5 48 48 48h352c26.5 0 48-21.5 48-48v-41.6c0-74.2-60.2-134.4-134.4-134.4z"/>
        |  </symbol>
        |</defs>""".trimMargin()
    }
    /**
     * Extension function to capitalize the first letter of a string.
     */
    private fun String.capitalize(): String {
        return if (this.isEmpty()) this else this[0].uppercase() + this.substring(1)
    }

    private fun createLightModeStyles(id: String, color: String): String {
        return """
        |    #id_$id .card { 
        |      fill: rgba(255, 255, 255, 0.8); 
        |      stroke: #E5E7EB; 
        |      stroke-width: 1; 
        |      filter: drop-shadow(0 4px 6px rgba(0, 0, 0, 0.05)) drop-shadow(0 2px 4px rgba(0, 0, 0, 0.05));
        |    }
        |    #id_$id .title { 
        |      font-family: ${theme.fontFamily}; 
        |      font-weight: 700; 
        |      font-size: 24px; 
        |      fill: #111827;
        |      letter-spacing: -0.02em;
        |    }
        |    #id_$id .subtitle { 
        |      font-family: ${theme.fontFamily}; 
        |      font-weight: 500; 
        |      font-size: 12px; 
        |      fill: #6B7280; 
        |    }
        |    #id_$id .status { 
        |      font-family: ${theme.fontFamily}; 
        |      font-weight: 600; 
        |      font-size: 12px; 
        |    }
        |    #id_$id .content { 
        |      font-family: ${theme.fontFamily}; 
        |      font-weight: 400; 
        |      font-size: 14px; 
        |      fill: #4B5563; 
        |    }
        |    #id_$id .section-title { 
        |      font-family: ${theme.fontFamily}; 
        |      font-weight: 600; 
        |      font-size: 16px; 
        |      fill: #111827; 
        |      letter-spacing: -0.01em;
        |    }
        |    #id_$id .participant-title {
        |       font-family: ${theme.fontFamily};
        |       font-weight: 600;
        |       font-size: 12px;
        |       fill: #4B5563;
        |    }
        |    #id_$id .participant-name { 
        |      font-family: ${theme.fontFamily}; 
        |      font-weight: 500; 
        |      font-size: 12px; 
        |      fill: #111827; 
        |      text-anchor: middle; 
        |    }
        |    #id_$id .participant-name-with-email {
        |      font-family: ${theme.fontFamily};
        |      font-weight: 500;
        |      font-size: 12px;
        |      fill: #3B82F6;
        |      text-anchor: middle;
        |    }
        |    #id_$id .participant-name-with-email:hover {
        |      fill: #2563EB;
        |      text-decoration: underline;
        |    }
        |    #id_$id .participant-container {
        |      cursor: pointer;
        |    }
        |    #id_$id .participant-container:hover .participant-icon {
        |      filter: drop-shadow(0px 0px 4px rgba(59, 130, 246, 0.5));
        |      transition: filter 0.3s ease;
        |    }
        |    #id_$id .participant-container:hover .participant-name {
        |      fill: #3B82F6;
        |      transition: fill 0.3s ease;
        |    }
        |    #id_$id .group-chat-link {
        |      font-family: ${theme.fontFamily};
        |      font-weight: 500;
        |      font-size: 12px;
        |      fill: #3B82F6;
        |      text-decoration: underline;
        |      cursor: pointer;
        |    }
        |    #id_$id .reference-link {
        |      font-family: ${theme.fontFamily};
        |      font-weight: 500;
        |      font-size: 14px;
        |      fill: #3B82F6;
        |      text-decoration: underline;
        |      cursor: pointer;
        |    }
        |    #id_$id .reference-link:hover {
        |      fill: #2563EB;
        |    }
        |    #id_$id a {
        |      cursor: pointer;
        |    }""".trimMargin()
    }

    private fun createDarkModeStyles(id: String, color: String): String {
        return """
        |    #id_$id .card { 
        |      fill: rgba(31, 41, 55, 0.8); 
        |      stroke: #374151; 
        |      stroke-width: 1; 
        |      filter: drop-shadow(0px 4px 8px rgba(0, 0, 0, 0.3));
        |    }
        |    #id_$id .title { 
        |      font-family: ${theme.fontFamily}; 
        |      font-weight: 700; 
        |      font-size: 24px; 
        |      fill: #F9FAFB;
        |      letter-spacing: -0.02em;
        |    }
        |    #id_$id .subtitle { 
        |      font-family: ${theme.fontFamily}; 
        |      font-weight: 500; 
        |      font-size: 12px; 
        |      fill: #9CA3AF; 
        |    }
        |    #id_$id .status { 
        |      font-family: ${theme.fontFamily}; 
        |      font-weight: 600; 
        |      font-size: 12px; 
        |    }
        |    #id_$id .content { 
        |      font-family: ${theme.fontFamily}; 
        |      font-weight: 400; 
        |      font-size: 14px; 
        |      fill: #D1D5DB; 
        |    }
        |    #id_$id .section-title { 
        |      font-family: ${theme.fontFamily}; 
        |      font-weight: 600; 
        |      font-size: 16px; 
        |      fill: #F9FAFB; 
        |      letter-spacing: -0.01em;
        |    }
        |    #id_$id .participant-title {
        |       font-family: ${theme.fontFamily};
        |       font-weight: 600;
        |       font-size: 12px;
        |       fill: #D1D5DB;
        |    }
        |    #id_$id .participant-name { 
        |      font-family: ${theme.fontFamily}; 
        |      font-weight: 500; 
        |      font-size: 12px; 
        |      fill: #F9FAFB; 
        |      text-anchor: middle; 
        |    }
        |    #id_$id .participant-name-with-email {
        |      font-family: ${theme.fontFamily};
        |      font-weight: 500;
        |      font-size: 12px;
        |      fill: #60A5FA;
        |      text-anchor: middle;
        |    }
        |    #id_$id .participant-name-with-email:hover {
        |      fill: #93C5FD;
        |      text-decoration: underline;
        |    }
        |    #id_$id .participant-container {
        |      cursor: pointer;
        |    }
        |    #id_$id .participant-container:hover .participant-icon {
        |      filter: drop-shadow(0px 0px 6px rgba(96, 165, 250, 0.6));
        |      transition: filter 0.3s ease;
        |    }
        |    #id_$id .participant-container:hover .participant-name {
        |      fill: #60A5FA;
        |      transition: fill 0.3s ease;
        |    }
        |    #id_$id .group-chat-link {
        |      font-family: ${theme.fontFamily};
        |      font-weight: 500;
        |      font-size: 12px;
        |      fill: #60A5FA;
        |      text-decoration: underline;
        |      cursor: pointer;
        |    }
        |    #id_$id .reference-link {
        |      font-family: ${theme.fontFamily};
        |      font-weight: 500;
        |      font-size: 14px;
        |      fill: #60A5FA;
        |      text-decoration: underline;
        |      cursor: pointer;
        |    }
        |    #id_$id .reference-link:hover {
        |      fill: #93C5FD;
        |    }
        |    #id_$id a {
        |      cursor: pointer;
        |    }""".trimMargin()
    }

}
