package io.docops.docopsextensionssupport.adr

import io.docops.docopsextensionssupport.util.UrlUtil.urlEncode
import kotlin.math.max
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Apple-inspired ADR SVG Generator.
 * Focuses on extreme simplicity, San Francisco-style typography, 
 * sophisticated glassmorphism, and large whitespace.
 */
class AppleInspiredAdrSvgGenerator(
    private val useDark: Boolean,
    private val themeName: String
) {
    private data class RenderedSection(
        val svg: String,
        val height: Int
    )

    private val contentWidth = 800
    private val padding = 48
    private val cardRadius = 28
    private val sectionGap = 40
    
    private val appleFont = "'SF Pro Display', 'Inter', system-ui, -apple-system, sans-serif"
    private val appleMono = "'SF Mono', 'JetBrains Mono', monospace"

    @OptIn(ExperimentalUuidApi::class)
    fun generate(adr: Adr, width: Int = 900): String {
        val id = Uuid.random().toHexString()
        val statusColor = statusColor(adr.status)
        val renderedSections = renderSections(adr, statusColor, id)

        val headerHeight = 240
        val sectionsStartY = headerHeight + 40
        val sectionsHeight = renderedSections.sumOf { it.height } +
                ((renderedSections.size - 1).coerceAtLeast(0) * sectionGap)

        val totalHeight = sectionsStartY + sectionsHeight + 100

        return """
            <svg width="$width" height="$totalHeight" viewBox="0 0 $width $totalHeight" xmlns="http://www.w3.org/2000/svg">
                <title>${escapeXml(adr.title)}</title>
                <defs>
                    ${defs(statusColor, id)}
                </defs>
                ${canvas(width, totalHeight)}
                ${header(adr, statusColor, id)}
                ${positionSections(renderedSections, sectionsStartY)}
                ${footer(width, totalHeight)}
            </svg>
        """.trimIndent()
    }

    private fun statusColor(status: AdrStatus): String {
        return when (status) {
            AdrStatus.Proposed -> "#007AFF" // System Blue
            AdrStatus.Accepted -> "#34C759" // System Green
            AdrStatus.Superseded -> "#AF52DE" // System Purple
            AdrStatus.Deprecated -> "#FF3B30" // System Red
            AdrStatus.Rejected -> "#8E8E93" // System Gray
        }
    }

    private fun defs(statusColor: String, id: String): String {
        val washStart = if (useDark) "#000000" else "#FFFFFF"
        val washEnd = if (useDark) "#1C1C1E" else "#F2F2F7"
        
        return """
            <style>
                @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&amp;display=swap');
                
                .apple-text {
                    font-family: $appleFont;
                }
                .apple-mono {
                    font-family: $appleMono;
                }
                .section-card {
                    transition: transform 0.3s ease;
                }
                .section-card:hover {
                    transform: translateY(-2px);
                }
                .link:hover {
                    text-decoration: underline;
                }
                .participant-node {
                    transition: all 0.2s ease;
                }
                .participant-node:hover {
                    opacity: 0.7;
                }
                .chat-btn:hover {
                    filter: brightness(1.1);
                }
            </style>

            <filter id="appleShadow_$id" x="-20%" y="-20%" width="140%" height="140%">
                <feDropShadow dx="0" dy="8" stdDeviation="12" flood-color="#000000" flood-opacity="${if (useDark) "0.4" else "0.08"}"/>
            </filter>

            <linearGradient id="appleBg_$id" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="$washStart"/>
                <stop offset="100%" stop-color="$washEnd"/>
            </linearGradient>
            
            <linearGradient id="statusGradient_$id" x1="0" y1="0" x2="1" y2="0">
                <stop offset="0%" stop-color="$statusColor"/>
                <stop offset="100%" stop-color="$statusColor" stop-opacity="0.8"/>
            </linearGradient>
        """.trimIndent()
    }

    private fun canvas(width: Int, height: Int): String {
        return """
            <rect width="$width" height="$height" fill="url(#appleBg_${Uuid.random().toHexString()})" />
        """.trimIndent()
    }

    private fun header(adr: Adr, statusColor: String, id: String): String {
        val textColor = if (useDark) "#FFFFFF" else "#000000"
        val mutedColor = if (useDark) "#8E8E93" else "#8E8E93"
        
        return """
            <g transform="translate($padding, 60)">
                <text x="0" y="0" class="apple-text" font-family="$appleFont" font-size="14" font-weight="600" fill="$statusColor" letter-spacing="0.05em">
                    ${escapeXml(adr.status.name.uppercase())}
                </text>
                <text x="0" y="48" class="apple-text" font-family="$appleFont" font-size="44" font-weight="700" fill="$textColor" letter-spacing="-0.02em">
                    ${escapeXml(adr.title)}
                </text>
                <text x="0" y="90" class="apple-mono" font-family="$appleMono" font-size="14" font-weight="500" fill="$mutedColor">
                    ${escapeXml(adr.date)} • ADR-${id.take(6).uppercase()}
                </text>
                
                <line x1="0" y1="130" x2="${contentWidth}" y2="130" stroke="$textColor" stroke-opacity="0.1" stroke-width="1"/>
            </g>
        """.trimIndent()
    }

    private fun renderSections(adr: Adr, statusColor: String, id: String): List<RenderedSection> {
        val sections = mutableListOf<RenderedSection>()
        
        sections.add(section("Context", adr.context, statusColor, id))
        sections.add(section("Decision", adr.decision, statusColor, id, emphasize = true))
        sections.add(section("Consequences", adr.consequences, statusColor, id))
        
        if (adr.participants.isNotEmpty()) {
            sections.add(participantsSection(adr, statusColor, id))
        }
        
        if (adr.references.isNotEmpty()) {
            sections.add(referencesSection(adr.references, statusColor, id))
        }
        
        return sections
    }

    private fun section(label: String, lines: List<String>, statusColor: String, id: String, emphasize: Boolean = false): RenderedSection {
        val textColor = if (useDark) "#F2F2F7" else "#1C1C1E"
        val cardColor = if (useDark) "#1C1C1E" else "#FFFFFF"
        val lineHeight = 28
        
        val wrappedLines = lines.flatMap { wrapText(it, 80) }
        val contentHeight = wrappedLines.size * lineHeight
        val height = 40 + contentHeight + 40
        
        val bodyText = wrappedLines.mapIndexed { index, line ->
            renderLineWithLinks(line, 32, 64 + index * lineHeight, textColor)
        }.joinToString("\n")

        val svg = """
            <g transform="translate($padding, 0)">
                <rect width="${contentWidth}" height="$height" rx="$cardRadius" fill="$cardColor" filter="url(#appleShadow_$id)"/>
                ${if (emphasize) """<rect width="6" height="$height" rx="3" fill="$statusColor" transform="translate(-12, 0)"/>""" else ""}
                <text x="32" y="32" class="apple-text" font-family="$appleFont" font-size="13" font-weight="600" fill="$statusColor" letter-spacing="0.05em">${label.uppercase()}</text>
                $bodyText
            </g>
        """.trimIndent()
        
        return RenderedSection(svg, height)
    }

    private fun renderLineWithLinks(line: String, x: Int, y: Int, textColor: String): String {
        val segments = WikiLinkParser.parse(line)
        val sb = StringBuilder("""<text x="$x" y="$y" class="apple-text" font-family="$appleFont" font-size="17" font-weight="400" fill="$textColor">""")

        segments.forEach { segment ->
            if (segment.url == null) {
                sb.append(escapeXml(segment.text))
            } else {
                sb.append("""<a href="${escapeXml(segment.url)}" target="_blank">""")
                sb.append("""<tspan fill="#007AFF" style="text-decoration: underline;">${escapeXml(segment.text)}</tspan>""")
                sb.append("""</a>""")
            }
        }
        sb.append("</text>")
        return sb.toString()
    }

    private fun participantsSection(adr: Adr, statusColor: String, id: String): RenderedSection {
        val participants = adr.participants
        val textColor = if (useDark) "#F2F2F7" else "#1C1C1E"
        val cardColor = if (useDark) "#1C1C1E" else "#FFFFFF"
        
        val rowHeight = 80
        val rows = (participants.size + 1) / 2
        val height = 40 + rows * rowHeight + 20

        val participantEmails = participants.filter { it.email.isNotEmpty() }.map { it.email }
        var groupChatUrl = ""
        if (participantEmails.size >= 2) {
            groupChatUrl = "https://teams.microsoft.com/l/chat/0/0?users=${participantEmails.joinToString(",")}&topicName=${adr.title.urlEncode()}"
        }
        
        val body = participants.mapIndexed { index, p ->
            val row = index / 2
            val col = index % 2
            val x = 32 + col * (contentWidth / 2)
            val y = 60 + row * rowHeight

            val participantContent = """
            <g transform="translate($x, $y)" class="participant-node">
                <circle cx="20" cy="20" r="20" fill="$statusColor" opacity="0.1"/>
                <text x="20" y="26" text-anchor="middle" font-size="20">${if(p.emoji.isEmpty()) "👤" else p.emoji}</text>
                <text x="50" y="18" class="apple-text" font-family="$appleFont" font-size="16" font-weight="600" fill="$textColor">${escapeXml(p.name)}</text>
                <text x="50" y="36" class="apple-text" font-family="$appleFont" font-size="13" font-weight="400" fill="$textColor" opacity="0.6">${escapeXml(p.title)}</text>
            </g>
            """.trimIndent()

            if (p.email.isNotEmpty()) {
                val teamsUrl = "https://teams.microsoft.com/l/chat/0/0?users=${escapeXml(p.email)}"
                """<a href="$teamsUrl" target="_blank" style="text-decoration: none;">$participantContent</a>"""
            } else {
                participantContent
            }
        }.joinToString("\n")

        val groupChatButton = if (groupChatUrl.isNotEmpty()) {
            """
            <g transform="translate(${contentWidth - 160}, 15)" class="chat-btn">
                <a href="${escapeXml(groupChatUrl)}" target="_blank" style="text-decoration: none;">
                    <rect width="140" height="28" rx="14" fill="#007AFF"/>
                    <text x="70" y="18" text-anchor="middle" class="apple-text" font-family="$appleFont" font-size="11" font-weight="600" fill="#FFFFFF">START GROUP CHAT</text>
                </a>
            </g>
            """.trimIndent()
        } else ""

        val svg = """
            <g transform="translate($padding, 0)">
                <rect width="${contentWidth}" height="$height" rx="$cardRadius" fill="$cardColor" filter="url(#appleShadow_$id)"/>
                <text x="32" y="32" class="apple-text" font-family="$appleFont" font-size="13" font-weight="600" fill="$statusColor" letter-spacing="0.05em">PARTICIPANTS</text>
                $groupChatButton
                $body
            </g>
        """.trimIndent()
        
        return RenderedSection(svg, height)
    }

    private fun referencesSection(references: List<WikiLink>, statusColor: String, id: String): RenderedSection {
        val textColor = if (useDark) "#F2F2F7" else "#1C1C1E"
        val cardColor = if (useDark) "#1C1C1E" else "#FFFFFF"
        val lineHeight = 32
        
        val height = 40 + references.size * lineHeight + 30
        
        val body = references.mapIndexed { index, link ->
            val y = 64 + index * lineHeight
            """
            <a href="${escapeXml(link.url)}" target="_blank" class="link">
                <text x="32" y="$y" class="apple-text" font-family="$appleFont" font-size="15" font-weight="500" fill="#007AFF">${escapeXml(link.label)}</text>
                <text x="${contentWidth - 32}" y="$y" class="apple-mono" font-family="$appleMono" font-size="12" font-weight="400" fill="$textColor" opacity="0.3" text-anchor="end">↗</text>
            </a>
            """.trimIndent()
        }.joinToString("\n")

        val svg = """
            <g transform="translate($padding, 0)">
                <rect width="${contentWidth}" height="$height" rx="$cardRadius" fill="$cardColor" filter="url(#appleShadow_$id)"/>
                <text x="32" y="32" class="apple-text" font-family="$appleFont" font-size="13" font-weight="600" fill="$statusColor" letter-spacing="0.05em">REFERENCES</text>
                $body
            </g>
        """.trimIndent()
        
        return RenderedSection(svg, height)
    }

    private fun positionSections(sections: List<RenderedSection>, startY: Int): String {
        var y = startY
        return sections.joinToString("\n") { section ->
            val positioned = """<g transform="translate(0, $y)">${section.svg}</g>"""
            y += section.height + sectionGap
            positioned
        }
    }

    private fun footer(width: Int, height: Int): String {
        val mutedColor = if (useDark) "#8E8E93" else "#8E8E93"
        return """
            <text x="${width/2}" y="${height - 40}" class="apple-text" font-family="$appleFont" font-size="12" font-weight="400" fill="$mutedColor" text-anchor="middle" opacity="0.6">
                Generated by DocOps • Apple-Inspired Design System v1.0
            </text>
        """.trimIndent()
    }

    private fun escapeXml(value: String): String = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")

    private fun wrapText(text: String, limit: Int): List<String> {
        val tokens = WikiLinkParser.tokenize(text)
        val result = mutableListOf<String>()
        var currentLine = StringBuilder()
        for (token in tokens) {
            if (currentLine.length + token.length + 1 > limit) {
                result.add(currentLine.toString())
                currentLine = StringBuilder(token)
            } else {
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(token)
            }
        }
        if (currentLine.isNotEmpty()) result.add(currentLine.toString())
        return result
    }
}
