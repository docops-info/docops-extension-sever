package io.docops.docopsextensionssupport.adr

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class DecisionRailAdrSvgGenerator(
    private val useDark: Boolean,
    private val themeName: String
) {
    private data class RenderedSection(
        val svg: String,
        val height: Int
    )

    private data class InlineSegment(
        val text: String,
        val url: String? = null
    )

    private val leftRailWidth = 40
    private val contentX = 80
    private val contentWidth = 760
    private val sectionX = 108
    private val sectionCardX = 34
    private val sectionCardWidth = 698
    private val sectionGap = 64
    private val mastheadHeight = 210
    private val mastheadTitleMaxChars = 34
    private val sectionTextMaxChars = 80
    private val cardRadius = 24

    @OptIn(ExperimentalUuidApi::class)
    fun generate(adr: Adr, width: Int = 900): String {
        val id = Uuid.random().toHexString()
        val statusColor = statusColor(adr.status)
        val renderedSections = renderSections(adr, statusColor, id)

        val sectionsStartY = 320
        val sectionsHeight = renderedSections.sumOf { it.height } +
                ((renderedSections.size - 1).coerceAtLeast(0) * sectionGap)

        val height = sectionsStartY + sectionsHeight + 80

        return """
            <svg width="$width" height="$height" viewBox="0 0 $width $height" xmlns="http://www.w3.org/2000/svg">
                <title>${escapeXml(adr.title)} [${escapeXml(adr.status.name)}]</title>
                <desc>Architecture Decision Record for ${escapeXml(adr.title)} - Status: ${adr.status.name}</desc>
                <defs>
                    ${defs(statusColor)}
                </defs>
                ${canvas(width, height)}
                ${statusRail(height, statusColor)}
                ${masthead(adr, statusColor, id)}
                ${positionSections(renderedSections, sectionsStartY)}
            </svg>
        """.trimIndent()
    }

    private fun statusColor(status: AdrStatus): String {
        return when (status) {
            AdrStatus.Proposed -> "#FF9500"
            AdrStatus.Accepted -> "#34C759"
            AdrStatus.Superseded -> "#AF52DE"
            AdrStatus.Deprecated -> "#FF3B30"
            AdrStatus.Rejected -> "#FF3B30"
        }
    }

    private fun estimateHeight(adr: Adr): Int {
        val contentLines =
            adr.context.size +
                    adr.decision.size +
                    adr.consequences.size +
                    adr.participants.size +
                    adr.references.size

        return 360 + contentLines * 34 + 520
    }

    private fun defs(statusColor: String): String {
        val washStart = if (useDark) "#111827" else "#F9FAFB"
        val washEnd = if (useDark) "#080B12" else "#EEF2F7"
        
        return """
            <style>
                @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');
                
                .adr-reference-link:hover rect {
                    fill: $statusColor;
                    opacity: 0.08;
                }

                .adr-reference-link:hover text {
                    text-decoration: underline;
                }
                .adr-inline-link:hover {
                    opacity: 0.72;
                }
            </style>

            <filter id="adrSoftShadow" x="-20%" y="-20%" width="140%" height="140%">
                <feDropShadow dx="0" dy="10" stdDeviation="15" flood-color="#000000" flood-opacity="0.1"/>
                <feDropShadow dx="0" dy="4" stdDeviation="6" flood-color="#000000" flood-opacity="0.05"/>
            </filter>

            <pattern id="adrPaperGrid" width="32" height="32" patternUnits="userSpaceOnUse">
                <path d="M 32 0 L 0 0 0 32" fill="none" stroke="#E7EAF0" stroke-width="1" opacity="0.05"/>
            </pattern>

            <linearGradient id="adrCanvasWash" x1="0" y1="0" x2="1" y2="1">
                <stop offset="10%" stop-color="$washStart"/>
                <stop offset="90%" stop-color="$washEnd"/>
            </linearGradient>

            <linearGradient id="adrRailSeparatorGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#0B1220" stop-opacity="0.05"/>
                <stop offset="50%" stop-color="#0B1220" stop-opacity="0.25"/>
                <stop offset="100%" stop-color="#0B1220" stop-opacity="0.05"/>
            </linearGradient>

            <filter id="adrRailGlow">
                <feGaussianBlur stdDeviation="${if (useDark) "12" else "0"}" result="blur"/>
                <feFlood flood-color="$statusColor" flood-opacity="${if (useDark) "0.35" else "0.0"}" result="color"/>
                <feComposite in="color" in2="blur" operator="in" result="glow"/>
                <feMerge>
                    <feMergeNode in="glow"/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>
        """.trimIndent()
    }

    private fun canvas(width: Int, height: Int): String {
        return """
            <rect width="$width" height="$height" fill="url(#adrCanvasWash)"/>
            <rect width="$width" height="$height" fill="url(#adrPaperGrid)" opacity="${if (useDark) "0.12" else "0.55"}"/>
        """.trimIndent()
    }

    private fun statusRail(height: Int, statusColor: String): String {
        return """
            <rect x="0" y="0" width="40" height="$height" fill="$statusColor" filter="url(#adrRailGlow)"/>
            <rect x="0" y="0" width="1" height="$height" fill="white" opacity="0.15"/>
            <rect x="40" y="0" width="1" height="$height" fill="url(#adrRailSeparatorGradient)"/>
        """.trimIndent()
    }

    private fun masthead(adr: Adr, statusColor: String, id: String): String {
        val textColor = if (useDark) "#F9FAFB" else "#111827"
        val mutedColor = if (useDark) "#94A3B8" else "#6B7280"
        val cardColor = if (useDark) "rgba(15,23,42,0.92)" else "rgba(255,255,255,0.96)"
        val statusInitial = adr.status.name.first().uppercaseChar()
        val titleLines = wrapText(adr.title, mastheadTitleMaxChars).take(2)

        val titleSvg = titleLines.mapIndexed { index, line ->
            val y = 104 + index * 36
            """
                <text x="32" y="$y"
                      font-family="Inter, SF Pro Display, system-ui, sans-serif"
                      font-size="30"
                      font-weight="700"
                      letter-spacing="-0.02em"
                      fill="$textColor">
                    ${escapeXml(line)}
                </text>
            """.trimIndent()
        }.joinToString("\n")

        val metaY = if (titleLines.size > 1) 178 else 154

        return """
            <g transform="translate($contentX,64)">
                <defs>
                    <clipPath id="mastheadClip_$id">
                        <rect x="0" y="0" width="$contentWidth" height="$mastheadHeight" rx="$cardRadius"/>
                    </clipPath>
                </defs>
                <rect x="0" y="0" width="$contentWidth" height="$mastheadHeight" rx="$cardRadius" fill="$cardColor" filter="url(#adrSoftShadow)"/>
                
                <text x="690" y="142"
                      text-anchor="middle"
                      font-family="Inter, SF Pro Display, system-ui, sans-serif"
                      font-size="92"
                      font-weight="900"
                      fill="$statusColor"
                      opacity="0.10">
                    $statusInitial
                </text>

                <rect x="0" y="0" width="$contentWidth" height="8" fill="$statusColor" clip-path="url(#mastheadClip_$id)"/>

                <text x="32" y="54"
                      font-family="Inter, SF Pro Display, system-ui, sans-serif"
                      font-size="13"
                      font-weight="800"
                      letter-spacing="0.15em"
                      fill="$statusColor">
                    ${escapeXml(adr.status.name.uppercase())}
                </text>

                $titleSvg

                <text x="32" y="$metaY"
                      font-family="Inter, SF Mono, ui-monospace, monospace"
                      font-size="12"
                      font-weight="500"
                      fill="$mutedColor">
                    ${escapeXml(adr.date)}
                </text>
            </g>
        """.trimIndent()
    }


    private fun escapeXml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun renderSections(adr: Adr, statusColor: String, id: String): List<RenderedSection> {
        val sections = mutableListOf<RenderedSection>()

        sections.add(section("01", "CONTEXT", adr.context, statusColor, id = id))

        sections.add(section("02", "DECISION", adr.decision, statusColor, emphasize = true, id = id))

        sections.add(section("03", "CONSEQUENCES", adr.consequences, statusColor, id = id))

        if (adr.participants.isNotEmpty()) {
            sections.add(
                section(
                    "04",
                    "PARTICIPANTS",
                    adr.participants.map { participant ->
                        listOf(participant.name, participant.title)
                            .filter { it.isNotBlank() }
                            .joinToString(" — ")
                    },
                    statusColor,
                    id = id
                )
            )
        }

        if (adr.references.isNotEmpty()) {
            sections.add(
                referenceSection(
                    "05",
                    "REFERENCES",
                    adr.references,
                    statusColor,
                    id = id
                )
            )
        }

        return sections
    }

    private fun positionSections(sections: List<RenderedSection>, startY: Int): String {
        var y = startY

        return sections.joinToString("\n") { section ->
            val positioned = """
                <g transform="translate(0,$y)">
                    ${section.svg}
                </g>
            """.trimIndent()

            y += section.height + sectionGap
            positioned
        }
    }

    private fun section(
        number: String,
        label: String,
        lines: List<String>,
        statusColor: String,
        emphasize: Boolean = false,
        references: Boolean = false,
        id: String
    ): RenderedSection {
        val textColor = if (useDark) "#D1D5DB" else "#1F2937"
        val labelColor = if (useDark) "#94A3B8" else "#4B5563"
        val ruleColor = if (useDark) "#334155" else "#E5E7EB"
        val cardColor = if (useDark) "rgba(15,23,42,0.92)" else "rgba(255,255,255,0.96)"
        val bodySize = 15
        val bodyWeight = if (emphasize) 600 else 400
        val lineHeight = 24
        val paragraphGap = if (references) 12 else 8

        val wrappedParagraphs = lines.ifEmpty { listOf("") }.map { line ->
            wrapText(line, sectionTextMaxChars)
        }

        val bodyHeight = wrappedParagraphs.sumOf { paragraph ->
            paragraph.size * lineHeight + paragraphGap
        }

        val cardHeight = 56 + bodyHeight + 24
        val totalSectionHeight = 28 + cardHeight

        var currentY = 68
        val body = wrappedParagraphs.flatMap { paragraph ->
            val textLines = paragraph.map { wrappedLine ->
                val text = renderInlineTextLine(
                    value = wrappedLine,
                    x = 82,
                    y = currentY,
                    fontSize = bodySize,
                    fontWeight = bodyWeight,
                    textColor = textColor,
                    linkColor = statusColor
                )
                currentY += lineHeight
                text
            }
            currentY += paragraphGap
            textLines
        }.joinToString("\n")

        val svg = """
            <g id="section-$number" transform="translate($sectionX,0)">
                <defs>
                    <clipPath id="sectionClip-${number}_$id">
                        <rect x="$sectionCardX" y="28" width="$sectionCardWidth" height="$cardHeight" rx="$cardRadius"/>
                    </clipPath>
                </defs>
                <circle cx="0" cy="0" r="15" fill="$statusColor"/>
                <text x="0" y="5"
                      text-anchor="middle"
                      font-family="Inter, system-ui, sans-serif"
                      font-size="12"
                      font-weight="800"
                      fill="white">$number</text>

                <text x="34" y="5"
                      font-family="Inter, system-ui, sans-serif"
                      font-size="13"
                      font-weight="800"
                      letter-spacing="0.15em"
                      fill="$labelColor">$label</text>

                <line x1="180" y1="0" x2="716" y2="0" stroke="$ruleColor" stroke-width="1"/>

                <rect x="$sectionCardX" y="28" width="$sectionCardWidth" height="$cardHeight" rx="$cardRadius" fill="$cardColor" filter="url(#adrSoftShadow)"/>
                ${if (emphasize) """<rect x="$sectionCardX" y="28" width="2" height="$cardHeight" fill="$statusColor" clip-path="url(#sectionClip-${number}_$id)"/>""" else ""}
                <rect x="$sectionCardX" y="28" width="5" height="$cardHeight" fill="$statusColor" opacity="0.15" clip-path="url(#sectionClip-${number}_$id)"/>

                $body
            </g>
        """.trimIndent()

        return RenderedSection(svg, totalSectionHeight)
    }


    private fun referenceSection(
        number: String,
        label: String,
        references: List<WikiLink>,
        statusColor: String,
        id: String
    ): RenderedSection {
        val textColor = if (useDark) "#D1D5DB" else "#1F2937"
        val mutedColor = if (useDark) "#94A3B8" else "#6B7280"
        val labelColor = if (useDark) "#94A3B8" else "#4B5563"
        val ruleColor = if (useDark) "#334155" else "#E5E7EB"
        val cardColor = if (useDark) "rgba(15,23,42,0.92)" else "rgba(255,255,255,0.96)"

        val referenceBlockHeight = 58
        val cardHeight = 54 + references.size.coerceAtLeast(1) * referenceBlockHeight + 24
        val totalSectionHeight = 28 + cardHeight

        val body = references.mapIndexed { index, link ->
            val y = 70 + index * referenceBlockHeight
            val numberLabel = "[${index + 1}]"
            val wrappedLabel = wrapText(link.label, 56).firstOrNull().orEmpty()
            val wrappedUrl = wrapText(link.url, 72).firstOrNull().orEmpty()

            """
                <a href="${escapeXml(link.url)}" target="_blank" rel="noopener noreferrer">
                    <g class="adr-reference-link" style="cursor: pointer;">
                        <rect x="62" y="${y - 24}" width="628" height="46" rx="10" fill="transparent"/>
                        <text x="62" y="$y"
                              font-family="Inter, SF Mono, ui-monospace, monospace"
                              font-size="12"
                              font-weight="800"
                              fill="$statusColor">
                            ${escapeXml(numberLabel)}
                        </text>
                        <text x="102" y="$y"
                              font-family="Inter, system-ui, sans-serif"
                              font-size="15"
                              font-weight="600"
                              fill="$textColor">
                            ${escapeXml(wrappedLabel)}
                        </text>
                        <text x="102" y="${y + 22}"
                              font-family="Inter, SF Mono, ui-monospace, monospace"
                              font-size="12"
                              font-weight="400"
                              fill="$mutedColor">
                            ${escapeXml(wrappedUrl)}
                        </text>
                    </g>
                </a>
            """.trimIndent()
        }.joinToString("\n")

        val svg = """
            <g id="section-$number" transform="translate($sectionX,0)">
                <defs>
                    <clipPath id="sectionClip-${number}_$id">
                        <rect x="$sectionCardX" y="28" width="$sectionCardWidth" height="$cardHeight" rx="$cardRadius"/>
                    </clipPath>
                </defs>
                <circle cx="0" cy="0" r="15" fill="$statusColor"/>
                <text x="0" y="5"
                      text-anchor="middle"
                      font-family="Inter, system-ui, sans-serif"
                      font-size="12"
                      font-weight="800"
                      fill="white">$number</text>

                <text x="34" y="5"
                      font-family="Inter, system-ui, sans-serif"
                      font-size="13"
                      font-weight="800"
                      letter-spacing="0.15em"
                      fill="$labelColor">$label</text>

                <line x1="180" y1="0" x2="716" y2="0" stroke="$ruleColor" stroke-width="1"/>

                <rect x="$sectionCardX" y="28" width="$sectionCardWidth" height="$cardHeight" rx="$cardRadius" fill="$cardColor" filter="url(#adrSoftShadow)"/>
                <rect x="$sectionCardX" y="28" width="5" height="$cardHeight" fill="$statusColor" opacity="0.15" clip-path="url(#sectionClip-${number}_$id)"/>

                $body
            </g>
        """.trimIndent()

        return RenderedSection(svg, totalSectionHeight)
    }

    private fun parseInlineWikiLinks(value: String): List<InlineSegment> {
        val pattern = Regex("\\[\\[([^\\s\\]]+)\\s+([^\\]]+)\\]\\]")
        val segments = mutableListOf<InlineSegment>()
        var currentIndex = 0

        pattern.findAll(value).forEach { match ->
            if (match.range.first > currentIndex) {
                segments.add(
                    InlineSegment(
                        text = value.substring(currentIndex, match.range.first)
                    )
                )
            }

            val url = match.groupValues[1]
            val label = match.groupValues[2]

            segments.add(
                InlineSegment(
                    text = label,
                    url = url
                )
            )

            currentIndex = match.range.last + 1
        }

        if (currentIndex < value.length) {
            segments.add(
                InlineSegment(
                    text = value.substring(currentIndex)
                )
            )
        }

        return segments.filter { it.text.isNotEmpty() }
    }
    private fun renderInlineTextLine(
        value: String,
        x: Int,
        y: Int,
        fontSize: Int,
        fontWeight: Int,
        textColor: String,
        linkColor: String
    ): String {
        val segments = parseInlineWikiLinks(value)

        var currentX = x

        return segments.joinToString("\n") { segment ->
            val estimatedSegmentWidth = estimateSvgTextWidth(segment.text, fontSize)

            val svg = if (segment.url == null) {
                """
                    <text x="$currentX" y="$y"
                          font-family="Inter, system-ui, sans-serif"
                          font-size="$fontSize"
                          font-weight="$fontWeight"
                          fill="$textColor">
                        ${escapeXml(segment.text)}
                    </text>
                """.trimIndent()
            } else {
                """
                    <a href="${escapeXml(segment.url)}" target="_blank" rel="noopener noreferrer">
                        <text x="$currentX" y="$y"
                              class="adr-inline-link"
                              font-family="Inter, system-ui, sans-serif"
                              font-size="$fontSize"
                              font-weight="$fontWeight"
                              fill="$linkColor"
                              style="text-decoration: underline; cursor: pointer;">
                            ${escapeXml(segment.text)}
                        </text>
                    </a>
                """.trimIndent()
            }

            currentX += estimatedSegmentWidth
            svg
        }
    }

    private fun estimateSvgTextWidth(value: String, fontSize: Int): Int {
        return (value.length * fontSize * 0.56).toInt()
    }

    private fun wrapText(value: String, maxChars: Int): List<String> {
        if (value.length <= maxChars) {
            return listOf(value)
        }

        val tokens = tokenizePreservingWikiLinks(value)
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (token in tokens) {
            if (currentLine.isEmpty()) {
                currentLine.append(token)
            } else if (currentLine.length + token.length + 1 <= maxChars) {
                currentLine.append(" ").append(token)
            } else {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(token)
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }

        return lines
    }

    private fun tokenizePreservingWikiLinks(value: String): List<String> {
        val pattern = Regex("\\[\\[[^\\]]+\\]\\]")
        val tokens = mutableListOf<String>()
        var currentIndex = 0

        pattern.findAll(value).forEach { match ->
            if (match.range.first > currentIndex) {
                tokens.addAll(
                    value.substring(currentIndex, match.range.first)
                        .split(Regex("\\s+"))
                        .filter { it.isNotBlank() }
                )
            }

            tokens.add(match.value)
            currentIndex = match.range.last + 1
        }

        if (currentIndex < value.length) {
            tokens.addAll(
                value.substring(currentIndex)
                    .split(Regex("\\s+"))
                    .filter { it.isNotBlank() }
            )
        }

        return tokens
    }
}