package io.docops.docopsextensionssupport.adr

import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.util.UrlUtil.urlEncode
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Generates high-fidelity, "Cyber-Brutalist" SVG diagrams for ADRs.
 * Features glassmorphism, technical backgrounds, and distinctive typography.
 */
class CyberBrutalistAdrSvgGenerator(val useDark: Boolean) {
    companion object {
        private const val DEFAULT_WIDTH = 850
        private const val DEFAULT_PADDING = 40
        private val STATUS_COLORS = mapOf(
            AdrStatus.Proposed to "#38bdf8",    // Sky Blue
            AdrStatus.Accepted to "#10b981",    // Emerald
            AdrStatus.Superseded to "#f59e0b",  // Amber
            AdrStatus.Deprecated to "#f43f5e",  // Rose
            AdrStatus.Rejected to "#9f1239"     // Rose-900
        )
    }

    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    @OptIn(ExperimentalUuidApi::class)
    fun generate(adr: Adr): String {
        val id = Uuid.random().toHexString()
        val theme = ThemeFactory.getTheme(useDark, true)
        val colors = getThemeColors(useDark)
        val statusColor = STATUS_COLORS[adr.status] ?: theme.accentColor
        val svgWidth = 850
        val lineSpacing = 20
        val panelPadding = 60

        // 1. Pre-calculate Heights
        val contextLines = adr.context.sumOf { wrapText(it, 42).size }
        val decisionLines = adr.decision.sumOf { wrapText(it, 42).size }
        val topPanelHeight = maxOf(contextLines, decisionLines) * lineSpacing + panelPadding

        val consequenceLines = adr.consequences.sumOf { wrapText(it, 85).size }
        val consequenceHeight = consequenceLines * lineSpacing + panelPadding

        // Calculate rows for participants (3 per row)
        val partRows = Math.ceil(adr.participants.size.toDouble() / 3).toInt()
        val partPanelHeight = partRows * 130 + 40

        // Calculate References height (25px per reference + padding)
        val refLinesCount = adr.references.size
        val refPanelHeight = maxOf(70, refLinesCount * 25 + 50)

        val patternStroke = if (useDark) "#1e293b" else "#e2e8f0"

        // Start panels further down to clear the header area safely
        var currentY = 180.0
        val topPanelY = currentY
        currentY += topPanelHeight + 20
        val consPanelY = currentY
        currentY += consequenceHeight + 40
        val partPanelY = currentY
        currentY += partPanelHeight + 40
        val refPanelY = currentY

        val totalHeight = (currentY + refPanelHeight + 40).toInt()
        // Collect emails for group chat link
        val participantEmails = mutableListOf<String>()

        // First pass to collect emails
        for (participant in adr.participants) {
            if (participant.email.isNotEmpty()) {
                participantEmails.add(participant.email)
            }
        }
        var groupChatUrl = ""
        if (participantEmails.size >= 2) {
             groupChatUrl = "https://teams.microsoft.com/l/chat/0/0?users=${participantEmails.joinToString(",")}&topicName=${adr.title.urlEncode()}"
        }
        return """
        <svg xmlns="http://www.w3.org/2000/svg" id="cb_adr_$id" width="$svgWidth" height="$totalHeight" viewBox="0 0 $svgWidth $totalHeight" preserveAspectRatio='xMidYMid meet'>
          <defs>
            <pattern id="hexagons_$id" width="50" height="43.4" patternUnits="userSpaceOnUse" patternTransform="scale(2)">
              <path d="M25 0 L50 14.4 L50 43.4 L25 57.8 L0 43.4 L0 14.4 Z" fill="none" stroke="$patternStroke" stroke-width="1" />
            </pattern>
            <style type="text/css">
              @import url('https://fonts.googleapis.com/css2?family=Syne:wght@800&amp;family=JetBrains+Mono:wght@400;700&amp;display=swap');
      
              #cb_adr_$id .canvas { fill: ${theme.canvas}; }
              #cb_adr_$id .bg-pattern { fill: url(#hexagons_$id); }
              #cb_adr_$id .glass-panel { fill: ${theme.glassEffect}; stroke: $statusColor; stroke-width: 1.5; }
              #cb_adr_$id .impact-panel { fill: ${theme.surfaceImpact}; stroke: ${theme.secondaryText}; stroke-width: 1; stroke-dasharray: 4; }
              #cb_adr_$id .accent-line { stroke: $statusColor; stroke-width: 4; stroke-linecap: square; filter: drop-shadow(0 0 5px $statusColor); }
      
              #cb_adr_$id .adr-title { font-family: ${theme.fontFamily}; font-weight: 800; font-size: 18px; fill: ${theme.primaryText}; text-transform: uppercase; letter-spacing: -1px; }
              #cb_adr_$id .sec-header { font-family: ${theme.fontFamily}; font-size: 14px; fill: $statusColor; text-transform: uppercase; letter-spacing: 2px; }       #cb_adr_$id .mono-text { font-family: 'JetBrains Mono', monospace; font-size: 13px; fill: ${colors["subText"]}; }
          #cb_adr_$id .status-label { font-family: 'JetBrains Mono', monospace; font-weight: 700; font-size: 12px; fill: #fff; }

          #cb_adr_$id .participant-node { cursor: pointer; }
          #cb_adr_$id .participant-node:hover .node-circle {
            stroke-width: 3;
            stroke: #818cf8;
            filter: drop-shadow(0 0 8px #6366f1);
          }
          #cb_adr_$id .participant-node:hover .node-name { fill: ${colors["text"]}; }

          #cb_adr_$id .chat-btn { transition: all 0.3s ease; cursor: pointer; }
          #cb_adr_$id .chat-btn:hover { filter: brightness(1.2); }

          @keyframes slideIn_$id { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
          #cb_adr_$id .entry { animation: slideIn_$id 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards; opacity: 0; }
        </style>
      </defs>

      <rect width="100%" height="100%" class="canvas" />
      <rect width="100%" height="100%" class="bg-pattern" opacity="${if (useDark) "0.4" else "0.1"}" />

  <!-- Header Section -->
      <g class="entry" style="animation-delay: 0s;">
        <line x1="40" y1="60" x2="40" y2="145" class="accent-line" />
        <text x="60" y="90" class="adr-title">${escapeXml(adr.title)}</text>
    
        <!-- Info Row -->
        <text x="60" y="125" class="mono-text">DATE: ${escapeXml(adr.date)} // REF: ADR-${id.take(4).uppercase()}</text>
    
        <!-- Status Badge -->
        <g transform="translate(${svgWidth - 160}, 95)">
            <rect width="120" height="26" rx="13" fill="$statusColor" fill-opacity="0.15" stroke="$statusColor" stroke-width="1"/>
            <text x="60" y="18" text-anchor="middle" class="status-label" style="fill: $statusColor">● ${adr.status.name.uppercase()}</text>
        </g>
      </g>

  <!-- 01 & 02: Dynamic Side-by-Side Panels -->
      <g class="entry" style="animation-delay: 0.1s;">
        <rect x="40" y="$topPanelY" width="370" height="$topPanelHeight" class="glass-panel" rx="8" />
        <text x="60" y="${topPanelY + 35}" class="sec-header">01_CONTEXT</text>
        ${renderLines(adr.context, 60, (topPanelY + 70).toInt(), 42, colors["text"])}

        <rect x="430" y="$topPanelY" width="370" height="$topPanelHeight" class="glass-panel" rx="8" />
        <text x="450" y="${topPanelY + 35}" class="sec-header">02_DECISION</text>
        ${renderLines(adr.decision, 450, (topPanelY + 70).toInt(), 42, colors["text"])}
      </g>

  <!-- 03: Dynamic Full-Width Consequences -->
  <g class="entry" style="animation-delay: 0.2s;">
    <rect x="40" y="$consPanelY" width="760" height="$consequenceHeight" class="impact-panel" rx="8" />
    <text x="60" y="${consPanelY + 35}" class="sec-header" style="fill:#f472b6">03_CONSEQUENCES</text>
    ${renderLines(adr.consequences, 60, (consPanelY + 70).toInt(), 85)}
  </g>

   <!-- 04: Participants Section with Multi-row support -->
  <g transform="translate(0, $partPanelY)">
    <g class="entry" style="animation-delay: 0.3s;">
      <text x="40" y="10" class="sec-header">04_PARTICIPANTS</text>
     
      <g class="chat-btn" transform="translate(620, -15)">
        <rect width="180" height="34" rx="4" fill="#6366f1" />
        <a href="${escapeXml(groupChatUrl)}" target="_blank">
        <text x="90" y="22" text-anchor="middle" class="status-label">START GROUP CHAT</text>
        </a>
      </g>

       ${renderParticipants(adr.participants, colors, useDark)}
     
    </g>
  </g>

  <!-- 05: References - Sequential positioning -->
  <g transform="translate(0, $refPanelY)">
    <g class="entry" style="animation-delay: 0.4s;">
      <rect x="40" y="0" width="760" height="$refPanelHeight" rx="4" fill="${colors["impactFill"]}" stroke="${colors["subText"]}" stroke-opacity="0.2" />
      <text x="60" y="25" class="sec-header" font-size="10">05_REFERENCES</text>
      ${renderReferences(adr.references, 60, 50)}
    </g>
  </g>
</svg>
""".trimIndent()
    }

    private fun getThemeColors(useDark: Boolean): Map<String, String> {
        return if (useDark) {
            mapOf(
                "canvas" to "#020617",
                "text" to "#ffffff",
                "subText" to "#94a3b8",
                "glassFill" to "rgba(15, 23, 42, 0.9)",
                "impactFill" to "rgba(30, 41, 59, 0.5)",
                "nodeCircle" to "#1e293b"
            )
        } else {
            mapOf(
                "canvas" to "#f8fafc",
                "text" to "#0f172a",
                "subText" to "#475569",
                "glassFill" to "rgba(255, 255, 255, 0.9)",
                "impactFill" to "rgba(226, 232, 240, 0.5)",
                "nodeCircle" to "#e2e8f0"
            )
        }
    }

    private fun renderLines(lines: List<String>, x: Int, startY: Int, limit: Int, fillColor: String? = null): String {
        val sb = StringBuilder()
        var currentY = startY
        lines.forEach { line ->
            // 1. Extract links to placeholders to prevent wrapping mid-link
            val linkPattern = "\\[\\[([^\\s]+)\\s+(.*?)\\]\\]".toRegex()
            val links = mutableListOf<String>()
            var protectedLine = line

            linkPattern.findAll(line).forEach { match ->
                val placeholder = "___LINK_${links.size}___"
                links.add(match.value)
                protectedLine = protectedLine.replace(match.value, placeholder)
            }

            // 2. Wrap the protected text
            val wrapped = wrapText(protectedLine, limit)

            wrapped.forEach { wrappedLine ->
                // 3. Restore the links into the line
                var restoredLine = wrappedLine
                links.forEachIndexed { i, originalLink ->
                    restoredLine = restoredLine.replace("___LINK_${i}___", originalLink)
                }

                sb.append(renderLineWithLinks(restoredLine, x, currentY, fillColor))
                currentY += 20
            }
        }
        return sb.toString()
    }

    private fun renderLineWithLinks(line: String, x: Int, y: Int, fillColor: String?): String {
        val linkPattern = "\\[\\[([^\\s]+)\\s+(.*?)\\]\\]".toRegex()
        var lastIndex = 0
        // Use tspan inside text to handle mixed content accurately
        val sb = StringBuilder("""<text x="$x" y="$y" class="mono-text" ${if (fillColor != null) "style=\"fill:$fillColor\"" else ""}>""")

        val matches = linkPattern.findAll(line).toList()
        if (matches.isEmpty()) {
            sb.append(escapeXml(line))
        } else {
            matches.forEach { match ->
                sb.append(escapeXml(line.substring(lastIndex, match.range.first)))

                val url = match.groupValues[1]
                val label = match.groupValues[2]

                sb.append("""<a href="${escapeXml(url)}" target="_blank">""")
                sb.append("""<tspan style="fill:#818cf8; text-decoration:underline;">${escapeXml(label)}</tspan>""")
                sb.append("""</a>""")

                lastIndex = match.range.last + 1
            }
            sb.append(escapeXml(line.substring(lastIndex)))
        }

        sb.append("</text>")
        return sb.toString()
    }

    private fun wrapText(text: String, limit: Int): List<String> {
        val words = text.split(" ")
        val result = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            if (currentLine.length + word.length + 1 > limit) {
                result.add(currentLine.toString())
                currentLine = StringBuilder(word)
            } else {
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            }
        }
        if (currentLine.isNotEmpty()) result.add(currentLine.toString())
        return result
    }


    private fun renderParticipants(participants: List<Participant>, colors: Map<String, String>, useDark: Boolean): String {
        return participants.mapIndexed { i, p ->
            val row = i / 3
            val col = i % 3
            val x = 60 + (col * 240)
            val y = 30 + (row * 130)

            val nameLines = wrapText(p.name, 25)
            val titleLines = if (p.title.isNotEmpty()) wrapText(p.title, 25) else emptyList()

            val sb = StringBuilder()

            // Wrap in Teams link if email is available
            val hasEmail = p.email.isNotEmpty()
            if (hasEmail) {
                val teamsUrl = "https://teams.microsoft.com/l/chat/0/0?users=${escapeXml(p.email)}"
                sb.append("""<a href="$teamsUrl" target="_blank" style="text-decoration: none;">""")
            }

            sb.append("""<g class="participant-node" transform="translate($x, $y)">""")
            sb.append("""<circle cx="30" cy="30" r="30" fill="${colors["nodeCircle"]}" stroke="#6366f1" stroke-width="2" class="node-circle" />""")
            sb.append("""<text x="30" y="38" text-anchor="middle" font-size="24">${if (p.emoji.isEmpty()) "👤" else p.emoji}</text>""")

            var textY = 85
            nameLines.forEach { line ->
                sb.append("""<text x="30" y="$textY" text-anchor="middle" class="mono-text node-name" style="fill:${colors["text"]}">${escapeXml(line)}</text>""")
                textY += 15
            }
            titleLines.forEach { line ->
                sb.append("""<text x="30" y="$textY" text-anchor="middle" class="mono-text" font-size="10">${escapeXml(line)}</text>""")
                textY += 12
            }
            sb.append("</g>")

            if (hasEmail) {
                sb.append("</a>")
            }

            sb.toString()
        }.joinToString("\n")
    }

    private fun renderReferences(refs: List<WikiLink>, x: Int, startY: Int): String {
        val sb = StringBuilder()
        var currentY = startY
        refs.forEach { ref ->
            // Re-using the wiki-link logic for the references section
            val linkText = "[[${ref.url} ${ref.label}]]"
            sb.append(renderLineWithLinks(linkText, x, currentY, "#818cf8"))
            currentY += 25
        }
        return sb.toString()
    }
}
