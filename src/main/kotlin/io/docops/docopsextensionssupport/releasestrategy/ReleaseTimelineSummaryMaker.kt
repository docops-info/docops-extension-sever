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

package io.docops.docopsextensionssupport.releasestrategy

import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import kotlin.compareTo
import kotlin.text.lines
import kotlin.times
import kotlin.toString


/**
 * This class represents a Release Timeline Summary Maker.
 * It extends the ReleaseTimelineMaker class.
 * The ReleaseTimelineSummaryMaker class is responsible for generating a summary of the release timeline
 * based on the given release strategy.
 */
class ReleaseTimelineSummaryMaker : ReleaseTimelineMaker() {

    private enum class ContentFit { CLAMP, GROW }
    companion object {
        private const val CARD_WIDTH = 320f
        private const val CARD_HEIGHT = 240f
        private const val TRIANGLE_WIDTH = 80f
        private const val CARD_SPACING = 32f       // Was 20f - now 8-point grid compliant
        private const val TITLE_HEIGHT = 80f       // Was 60f - now 8-point grid compliant
        private const val MARGIN = 32f             // Was 20f - now 8-point grid compliant
        private const val TEXT_MARGIN = 24f        // Was 20f - now 8-point grid compliant
        private const val GOAL_MAX_CHARS = 45
        private const val DETAIL_MAX_CHARS = 55
        private const val MAX_BULLET_LINES = 6
        private const val GOAL_LINE_HEIGHT = 18f
        private const val DETAIL_LINE_HEIGHT = 18f
        private const val INNER_RECT_Y = 64f
        private const val INNER_RECT_BOTTOM_PADDING = 16f
        private const val DETAIL_BOTTOM_PADDING = 12f
        private const val GOAL_TO_DETAIL_GAP = 10f
    }

    private var theme = ThemeFactory.getTheme(false)
    /**
     * Generates a SVG string representation of a document using the given release strategy.
     *
     * @param releaseStrategy The release strategy to use for generating the document.
     * @param isPdf Specifies whether the document format is PDF.
     * @return The SVG string representation of the generated document.
     */
    override fun make(releaseStrategy: ReleaseStrategy, isPdf: Boolean): String {
        theme = ThemeFactory.getTheme(releaseStrategy.useDark)
        val fit = resolveContentFit(releaseStrategy)
        val cardLayouts = releaseStrategy.releases.map { release -> computeCardLayout(release, fit) }
        val maxCardHeight = cardLayouts.maxOfOrNull { it.first.cardHeight } ?: CARD_HEIGHT
        val dimensions = calculateDimensions(releaseStrategy, maxCardHeight)
        val id = releaseStrategy.id

        val str = StringBuilder()
        str.append(createSvgHeader(dimensions, id, releaseStrategy.title))
        str.append(createDefinitions(isPdf, id, releaseStrategy))
        str.append(createBackground(dimensions, releaseStrategy))
        str.append(createTitle(releaseStrategy.title, dimensions, releaseStrategy))
        str.append(createMainContent(releaseStrategy, isPdf, id, dimensions, cardLayouts, maxCardHeight))
        str.append(createSvgFooter())

        return str.toString()
    }

    private fun calculateDimensions(releaseStrategy: ReleaseStrategy, maxCardHeight: Float): Dimensions {
        // Calculate width: each card + triangle + spacing, minus last spacing, plus margins
        val singleCardWidth = CARD_WIDTH + TRIANGLE_WIDTH + CARD_SPACING
        val contentWidth = (releaseStrategy.releases.size * singleCardWidth) - CARD_SPACING + (2 * MARGIN)
        val contentHeight = TITLE_HEIGHT + maxCardHeight + (2 * MARGIN)

        return Dimensions(
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            scaledWidth = contentWidth * releaseStrategy.scale,
            scaledHeight = contentHeight * releaseStrategy.scale,
            scale = releaseStrategy.scale
        )
    }

    private fun createMainContent(
        releaseStrategy: ReleaseStrategy,
        isPdf: Boolean,
        id: String,
        dimensions: Dimensions,
        cardLayouts: List<Pair<CardGeometry, CardTextLayout>>,
        maxCardHeight: Float
    ): String {
        val str = StringBuilder()

        // Add flow connectors between cards (animated dashed lines)
        if (!isPdf && releaseStrategy.releases.size > 1) {
            str.append(createFlowConnectors(releaseStrategy, id, maxCardHeight))
        }

        releaseStrategy.releases.forEachIndexed { index, release ->
            val x = MARGIN + (index * (CARD_WIDTH + TRIANGLE_WIDTH + CARD_SPACING))
            val y = TITLE_HEIGHT
            val delay = index * 0.15
            val clipId = "detailClip_${id}_$index"

            val isGaRelease = release.type.toString().startsWith("G")
            val cardClass = if (isGaRelease) "release-card ga-release" else "release-card"

            str.append("""<g transform="translate($x,$y)" class="$cardClass">""")
            str.append("""<g class="animated-content" style="animation-delay: ${delay}s">""")
            str.append(
                createReleaseCard(
                    release = release,
                    isPdf = isPdf,
                    id = id,
                    releaseStrategy = releaseStrategy,
                    geometry = cardLayouts[index].first,
                    textLayout = cardLayouts[index].second,
                    clipId = clipId
                )
            )
            str.append("</g>")
            str.append("</g>")
        }

        return str.toString()
    }

    private fun createFlowConnectors(releaseStrategy: ReleaseStrategy, id: String, maxCardHeight: Float): String {
        val str = StringBuilder()
        val connectorY = TITLE_HEIGHT + (maxCardHeight / 2)
        val connectorColor = theme.accentColor

        for (i in 0 until releaseStrategy.releases.size - 1) {
            val startX = MARGIN + (i * (CARD_WIDTH + TRIANGLE_WIDTH + CARD_SPACING)) + CARD_WIDTH + TRIANGLE_WIDTH + 6
            val endX = MARGIN + ((i + 1) * (CARD_WIDTH + TRIANGLE_WIDTH + CARD_SPACING)) - 6

            str.append("""
                    <line x1="$startX" y1="$connectorY" x2="$endX" y2="$connectorY" 
                          stroke="$connectorColor" stroke-width="2.5" stroke-dasharray="7 6" 
                          stroke-linecap="round" opacity="0.6" class="flow-connector"/>
                """.trimIndent())
        }

        return str.toString()
    }

    private fun createReleaseCard(
        release: Release,
        isPdf: Boolean,
        id: String,
        releaseStrategy: ReleaseStrategy,
        geometry: CardGeometry,
        textLayout: CardTextLayout,
        clipId: String
    ): String {
        val gradientId = getGradientId(release, id)
        val fontColor = releaseStrategy.displayConfig.fontColor
        val dateColor = if (releaseStrategy.useDark) "#94A3B8" else "#475569"

        val innerBoxColor = if (releaseStrategy.useDark) "#020817" else "#FFFFFF"
        val innerBoxOpacity = if (releaseStrategy.useDark) "0.3" else "0.5"

        val isGaRelease = release.type.toString().startsWith("G")
        val cardFilter = if (isGaRelease) "url(#glowEffect)" else "url(#dropShadow)"

        val o = 4f
        val gaHighlight = if (isGaRelease) """
                <path d="M -$o,-$o 
                         H ${CARD_WIDTH} 
                         L ${CARD_WIDTH + TRIANGLE_WIDTH + o},${geometry.cardHeight/2} 
                         L ${CARD_WIDTH},${geometry.cardHeight + o} 
                         H -$o 
                         Z" 
                      fill="none" stroke="${theme.accentColor}" stroke-width="2.5" 
                      stroke-linejoin="round" opacity="0.6" filter="url(#glowEffect)"/>
            """.trimIndent() else ""

        return """
                $gaHighlight

                <path d="M 0,0 H ${CARD_WIDTH} V ${geometry.cardHeight} H 0 Z" 
                      fill="url(#$gradientId)" 
                      stroke="rgba(255,255,255,0.15)" 
                      stroke-width="1" 
                      filter="$cardFilter"/>

                <path d="M ${CARD_WIDTH},0 V ${geometry.cardHeight} L ${CARD_WIDTH + TRIANGLE_WIDTH},${geometry.cardHeight/2} Z" 
                      fill="url(#$gradientId)" 
                      stroke="rgba(255,255,255,0.15)" 
                      stroke-width="1"/>

                <path d="M 0,0 H ${CARD_WIDTH + TRIANGLE_WIDTH/2} L ${CARD_WIDTH},${geometry.cardHeight/2} H 0 Z" 
                      fill="url(#glass_$gradientId)" 
                      pointer-events="none"/>

                <rect x="16" y="${geometry.innerRectY}" width="${CARD_WIDTH - 32}" height="${geometry.innerRectHeight}" rx="10" 
                      fill="$innerBoxColor" fill-opacity="$innerBoxOpacity"/>

                <clipPath id="$clipId">
                    <rect x="$TEXT_MARGIN" y="${geometry.detailClipY}" width="${CARD_WIDTH - 48}" height="${geometry.detailClipHeight}" rx="6"/>
                </clipPath>

                <text x="${CARD_WIDTH/2}" y="-16" fill="$dateColor" text-anchor="middle" class="date-text">
                    ${release.date}
                </text>

                <text x="${CARD_WIDTH + TRIANGLE_WIDTH/2}" y="${geometry.cardHeight/2 + 12}" 
                      fill="white" text-anchor="middle" font-size="${if (isGaRelease) "36" else "32"}px" class="milestone-text" filter="url(#textLift)">
                    ${release.type}
                </text>

                ${createWrappedGoalText(textLayout.goalLines, TEXT_MARGIN, geometry.goalStartY, fontColor)}

                ${createDetailLines(textLayout, geometry.detailStartY, fontColor, clipId)}
            """.trimIndent()
    }

    private fun createWrappedGoalText(goalLines: List<String>, x: Float, y: Float, fontColor: String): String {
        val str = StringBuilder()
        str.append("""<text x="$x" y="$y" class="goal-text" fill="$fontColor" filter="url(#textLift)">""")

        goalLines.forEachIndexed { index, line ->
            val dyValue = if (index == 0) "0" else "${GOAL_LINE_HEIGHT.toInt()}"
            str.append("""<tspan x="$x" dy="$dyValue">${line.escapeXml()}</tspan>""")
        }

        str.append("</text>")
        return str.toString()
    }

    private fun createDetailLines(textLayout: CardTextLayout, startY: Float, fontColor: String, clipId: String): String {
        val str = StringBuilder()

        str.append("""<text x="$TEXT_MARGIN" y="$startY" class="detail-text" fill="$fontColor" filter="url(#textLift)" clip-path="url(#$clipId)">""")
        textLayout.detailLinesVisible.forEachIndexed { index, line ->
            val dyValue = if (index == 0) "0" else "${DETAIL_LINE_HEIGHT.toInt()}"
            str.append("""<tspan x="$TEXT_MARGIN" dy="$dyValue">${line.escapeXml()}</tspan>""")
        }

        if (textLayout.needsOverflowIndicator) {
            val dyValue = if (textLayout.detailLinesVisible.isEmpty()) "0" else "${DETAIL_LINE_HEIGHT.toInt()}"
            str.append("""<tspan x="$TEXT_MARGIN" dy="$dyValue">… +${textLayout.hiddenDetailCount} more</tspan>""")
        }

        str.append("</text>")
        return str.toString()
    }

    private fun createSvgHeader(dimensions: Dimensions, id: String, title: String): String {
        return """
            <svg width="${dimensions.scaledWidth}" height="${dimensions.scaledHeight}" viewBox="0 0 ${dimensions.contentWidth} ${dimensions.contentHeight}"  xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" role="img" aria-label="DocOps: Release Strategy" id="ID$id">
                <desc>Release Strategy</desc>
                <title>${title.escapeXml()}</title>
        """.trimIndent()
    }

    private fun createDefinitions(isPdf: Boolean, id: String, releaseStrategy: ReleaseStrategy): String {
        val str = StringBuilder()
        str.append("<defs>")

        // Enhanced geometric pattern for atmosphere
        str.append("""
            <pattern id="diagField_$id" width="20" height="20" patternUnits="userSpaceOnUse" patternTransform="rotate(25)">
                <line x1="0" y1="0" x2="0" y2="20" stroke="currentColor" stroke-width="1" stroke-opacity="0.08"/>
            </pattern>
            <radialGradient id="radarGlow_$id" cx="70%" cy="30%" r="60%">
                <stop offset="0%" stop-color="${theme.accentColor}" stop-opacity="0.15"/>
                <stop offset="100%" stop-color="${theme.accentColor}" stop-opacity="0"/>
            </radialGradient>
        """.trimIndent())

        // Add gradients for each release type
        releaseStrategy.displayConfig.colors.forEachIndexed { index, color ->
            val gradientId = when (index) {
                0 -> "gradientM_$id"
                1 -> "gradientR_$id"
                2 -> "gradientG_$id"
                else -> "gradient${index}_$id"
            }
            str.append(createGradient(gradientId, color))
        }

        // Add shadow filter
        str.append(createShadowFilter())

        // Add completed check icon if needed
        if (releaseStrategy.releases.any { it.completed }) {
            str.append(createCompletedCheckIcon())
        }

        str.append("</defs>")

        // Add CSS styles
        if (!isPdf) {
            str.append(createStyles(id, releaseStrategy))
        }

        return str.toString()
    }

    private fun createGradient(id: String, color: String): String {
        val svgColor = SVGColor(color, id)
        return """
            <linearGradient id="$id" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:${svgColor.color};stop-opacity:1" />
                <stop offset="100%" style="stop-color:${svgColor.darkenColor(color, 0.4)};stop-opacity:1" />
            </linearGradient>
            <linearGradient id="glass_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" style="stop-color:#ffffff;stop-opacity:0.2" />
                <stop offset="100%" style="stop-color:#ffffff;stop-opacity:0.02" />
            </linearGradient>
        """.trimIndent()
    }





    private fun createCompletedCheckIcon(): String {
        return """
            <g id="completedCheck">
                <circle cx="12" cy="12" r="12" fill="#10b981"/>
                <path d="M9 12l2 2 4-4" stroke="white" stroke-width="2" fill="none"/>
            </g>
        """.trimIndent()
    }

    private fun createShadowFilter(): String {
        return """
            <filter id="dropShadow" x="-20%" y="-20%" width="160%" height="170%">
                <feDropShadow dx="0" dy="6" stdDeviation="6" flood-color="#020617" flood-opacity="0.4"/>
            </filter>
            <filter id="glowEffect" x="-50%" y="-50%" width="200%" height="200%">
                <feGaussianBlur stdDeviation="4" result="coloredBlur"/>
                <feMerge> 
                    <feMergeNode in="coloredBlur"/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>
            <filter id="textLift" x="-10%" y="-10%" width="120%" height="120%">
                <feDropShadow dx="0" dy="1" stdDeviation="1" flood-opacity="0.5"/>
            </filter>
        """.trimIndent()
    }

    private fun createStyles(id: String, releaseStrategy: ReleaseStrategy): String {
        val totalReleases = releaseStrategy.releases.size
        val gaDelay = (totalReleases - 1) * 0.15 + 0.8 // After all cards fade in

        return """
            <style>
                @keyframes fadeInSlide {
                    from { opacity: 0; transform: translateX(-24px) translateY(8px); }
                    to { opacity: 1; transform: translateX(0) translateY(0); }
                }
                @keyframes gaPulse {
                    0%, 100% { transform: scale(1); }
                    50% { transform: scale(1.02); }
                }
                @keyframes flowLine {
                    from { stroke-dashoffset: 24; }
                    to { stroke-dashoffset: 0; }
                }
                #ID$id .release-card {
                    opacity: 1;
                }
                #ID$id .animated-content {
                    animation: fadeInSlide 0.7s cubic-bezier(0.16, 1, 0.3, 1) forwards;
                    opacity: 0;
                    transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1);
                }
                #ID$id .release-card.ga-release .animated-content {
                    animation: fadeInSlide 0.7s cubic-bezier(0.16, 1, 0.3, 1) forwards,
                               gaPulse 2s ease-in-out ${gaDelay}s infinite;
                }
                #ID$id .release-card:hover .animated-content {
                    transform: translateY(-10px);
                }
                #ID$id .milestone-text {
                    font-family: 'Inter', 'Outfit', sans-serif;
                    font-weight: 900;
                    letter-spacing: -0.02em;
                }
                #ID$id .date-text {
                    font-family: 'Inter', sans-serif;
                    font-size: 11px;
                    font-weight: 700;
                    letter-spacing: 0.12em;
                    text-transform: uppercase;
                }
                #ID$id .goal-text {
                    font-family: 'Inter', sans-serif;
                    font-size: 15px;
                    font-weight: 800;
                    letter-spacing: -0.01em;
                }
                #ID$id .detail-text {
                    font-family: 'Inter', sans-serif;
                    font-size: 13px;
                    font-weight: 600;
                    line-height: 1.5;
                }
                #ID$id .flow-connector {
                    animation: flowLine 1.5s linear infinite;
                }
                #ID$id text {
                    user-select: none;
                }
            </style>
        """.trimIndent()
    }

    private fun createBackground(dimensions: Dimensions, releaseStrategy: ReleaseStrategy): String {
        val backgroundColor = theme.canvas
        val id = releaseStrategy.id
        val deepBg = if (releaseStrategy.useDark) "#071120" else "#f8fafc"
        
        return """
            <rect width="${dimensions.contentWidth}" height="${dimensions.contentHeight}" fill="$deepBg"/>
            <rect width="${dimensions.contentWidth}" height="${dimensions.contentHeight}" fill="url(#diagField_$id)" color="${theme.accentColor}"/>
            <rect width="${dimensions.contentWidth}" height="${dimensions.contentHeight}" fill="url(#radarGlow_$id)"/>
        """.trimIndent()
    }



    private fun createTitle(title: String, dimensions: Dimensions, releaseStrategy: ReleaseStrategy): String {
        val titleFill = if (releaseStrategy.useDark) "#F8FAFC" else "#0F172A"
        val subtitleFill = if (releaseStrategy.useDark) "#94A3B8" else "#64748B"
        val accentColor = theme.accentColor
        
        return """
            <g transform="translate(${MARGIN}, 48)">
                <text fill="$titleFill" 
                      font-size="32px" 
                      font-family="'Inter', 'Outfit', sans-serif" 
                      font-weight="900"
                      style="letter-spacing: -0.02em;">
                    ${title.escapeXml()}
                </text>
                <text x="0" y="24" fill="$subtitleFill" 
                      font-size="13px" font-family="'Inter', sans-serif" 
                      font-weight="600" style="letter-spacing: 0.05em; text-transform: uppercase;">
                    Mission Control Timeline · ${java.time.LocalDate.now().year}
                </text>
                <rect y="34" width="64" height="4" fill="$accentColor" rx="2"/>
            </g>
        """.trimIndent()
    }

    private fun createMainContent(releaseStrategy: ReleaseStrategy, isPdf: Boolean, id: String, dimensions: Dimensions): String {
        val str = StringBuilder()

        // Add flow connectors between cards (animated dashed lines)
        if (!isPdf && releaseStrategy.releases.size > 1) {
            str.append(createFlowConnectors(releaseStrategy, id))
        }

        releaseStrategy.releases.forEachIndexed { index, release ->
            val x = MARGIN + (index * (CARD_WIDTH + TRIANGLE_WIDTH + CARD_SPACING))
            val y = TITLE_HEIGHT
            val delay = index * 0.15

            val isGaRelease = release.type.toString().startsWith("G")
            val cardClass = if (isGaRelease) "release-card ga-release" else "release-card"

            // Outer group: ONLY handles layout position via transform="translate()"
            // NO animation-delay here - that goes on the inner group
            str.append("""<g transform="translate($x,$y)" class="$cardClass">""")
            // Inner group: handles ALL CSS animations and transitions safely
            str.append("""<g class="animated-content" style="animation-delay: ${delay}s">""")
            str.append(createReleaseCard(release, isPdf, id, releaseStrategy))
            str.append("</g>")
            str.append("</g>")
        }

        return str.toString()
    }

    private fun createFlowConnectors(releaseStrategy: ReleaseStrategy, id: String): String {
        val str = StringBuilder()
        val connectorY = TITLE_HEIGHT + (CARD_HEIGHT / 2)
        val connectorColor = theme.accentColor

        for (i in 0 until releaseStrategy.releases.size - 1) {
            val startX = MARGIN + (i * (CARD_WIDTH + TRIANGLE_WIDTH + CARD_SPACING)) + CARD_WIDTH + TRIANGLE_WIDTH + 6
            val endX = MARGIN + ((i + 1) * (CARD_WIDTH + TRIANGLE_WIDTH + CARD_SPACING)) - 6

            str.append("""
                <line x1="$startX" y1="$connectorY" x2="$endX" y2="$connectorY" 
                      stroke="$connectorColor" stroke-width="2.5" stroke-dasharray="7 6" 
                      stroke-linecap="round" opacity="0.6" class="flow-connector"/>
            """.trimIndent())
        }

        return str.toString()
    }


    private fun createReleaseCard(release: Release, isPdf: Boolean, id: String, releaseStrategy: ReleaseStrategy): String {
        val gradientId = getGradientId(release, id)
        val fontColor = releaseStrategy.displayConfig.fontColor
        val dateColor = if (releaseStrategy.useDark) "#94A3B8" else "#475569"

        val innerBoxColor = if (releaseStrategy.useDark) "#020817" else "#FFFFFF"
        val innerBoxOpacity = if (releaseStrategy.useDark) "0.3" else "0.5"

        val isGaRelease = release.type.toString().startsWith("G")
        val cardFilter = if (isGaRelease) "url(#glowEffect)" else "url(#dropShadow)"

        // GA highlight path follows the full card + triangle shape (single continuous outline)
        val o = 4f // offset for the highlight border
        val gaHighlight = if (isGaRelease) """
            <path d="M -$o,-$o 
                     H ${CARD_WIDTH} 
                     L ${CARD_WIDTH + TRIANGLE_WIDTH + o},${CARD_HEIGHT/2} 
                     L ${CARD_WIDTH},${CARD_HEIGHT + o} 
                     H -$o 
                     Z" 
                  fill="none" stroke="${theme.accentColor}" stroke-width="2.5" 
                  stroke-linejoin="round" opacity="0.6" filter="url(#glowEffect)"/>
        """.trimIndent() else ""

        return """
            $gaHighlight

            <!-- Main Card Body with Mission Control Styling -->
            <path d="M 0,0 H ${CARD_WIDTH} V ${CARD_HEIGHT} H 0 Z" 
                  fill="url(#$gradientId)" 
                  stroke="rgba(255,255,255,0.15)" 
                  stroke-width="1" 
                  filter="$cardFilter"/>

            <!-- Arrow Triangle -->
            <path d="M ${CARD_WIDTH},0 V ${CARD_HEIGHT} L ${CARD_WIDTH + TRIANGLE_WIDTH},${CARD_HEIGHT/2} Z" 
                  fill="url(#$gradientId)" 
                  stroke="rgba(255,255,255,0.15)" 
                  stroke-width="1"/>

            <!-- Glassy Overlay (Top Half) -->
            <path d="M 0,0 H ${CARD_WIDTH + TRIANGLE_WIDTH/2} L ${CARD_WIDTH},${CARD_HEIGHT/2} H 0 Z" 
                  fill="url(#glass_$gradientId)" 
                  pointer-events="none"/>

            <!-- Inner Content Container -->
            <rect x="16" y="64" width="${CARD_WIDTH - 32}" height="${CARD_HEIGHT - 80}" rx="10" 
                  fill="$innerBoxColor" fill-opacity="$innerBoxOpacity"/>

            <text x="${CARD_WIDTH/2}" y="-16" fill="$dateColor" text-anchor="middle" class="date-text">
                ${release.date}
            </text>

            <text x="${CARD_WIDTH + TRIANGLE_WIDTH/2}" y="${CARD_HEIGHT/2 + 12}" 
                  fill="white" text-anchor="middle" font-size="${if (isGaRelease) "36" else "32"}px" class="milestone-text" filter="url(#textLift)">
                ${release.type}
            </text>

            ${createWrappedGoalText(release.goal, TEXT_MARGIN, 92.0f, CARD_WIDTH - 48, fontColor)}

            ${createDetailLines(release.lines, 120f, CARD_WIDTH - 48, fontColor)}
        """.trimIndent()
    }


    private fun createWrappedGoalText(text: String, x: Float, y: Float, maxWidth: Float, fontColor: String): String {
        val wrappedLines = wrapText(text, GOAL_MAX_CHARS)
        val str = StringBuilder()

        str.append("""<text x="$x" y="$y" class="goal-text" fill="$fontColor" filter="url(#textLift)">""")

        wrappedLines.forEachIndexed { index, line ->
            val dyValue = if (index == 0) "0" else "18"
            str.append("""<tspan x="$x" dy="$dyValue">${line.escapeXml()}</tspan>""")
        }

        str.append("</text>")

        return str.toString()
    }


    private fun createDetailLines(lines: List<String>, startY: Float, maxWidth: Float, fontColor: String): String {
        val str = StringBuilder()
        var lineCount = 0

        // Start with text element
        str.append("""<text x="$TEXT_MARGIN" y="$startY" class="detail-text" fill="$fontColor" filter="url(#textLift)">""")

        for (line in lines) {
            if (lineCount >= MAX_BULLET_LINES) {
                str.append("""<tspan x="$TEXT_MARGIN" dy="18">...</tspan>""")
                break
            }

            // Wrap the line text but handle bullets properly
            val wrappedLines = wrapText(line, DETAIL_MAX_CHARS)

            wrappedLines.forEachIndexed { index, wrappedLine ->
                if (lineCount >= MAX_BULLET_LINES) return@forEachIndexed

                // Only add chevron on the first line of each original line
                val bulletText = if (index == 0) "• $wrappedLine" else "  $wrappedLine"
                val dyValue = if (lineCount == 0) "0" else "18"

                str.append("""<tspan x="$TEXT_MARGIN" dy="$dyValue">${bulletText.escapeXml()}</tspan>""")
                lineCount++
            }
        }

        // Close the text element
        str.append("</text>")

        return str.toString()
    }


    private fun wrapText(text: String, maxCharsPerLine: Int): List<String> {
        if (text.length <= maxCharsPerLine) {
            return listOf(text)
        }

        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            // If adding this word would exceed the limit
            if (currentLine.length + word.length + (if (currentLine.isNotEmpty()) 1 else 0) > maxCharsPerLine) {
                // If current line has content, save it and start new line
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder(word)
                } else {
                    // Single word is too long, break it
                    if (word.length > maxCharsPerLine) {
                        lines.add(word.substring(0, maxCharsPerLine))
                        currentLine = StringBuilder(word.substring(maxCharsPerLine))
                    } else {
                        currentLine.append(word)
                    }
                }
            } else {
                // Add word to current line
                if (currentLine.isNotEmpty()) {
                    currentLine.append(" ")
                }
                currentLine.append(word)
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }

        return lines
    }

    private fun getGradientId(release: Release, id: String): String {
        return when {
            release.type.toString().startsWith("M") -> "gradientM_$id"
            release.type.toString().startsWith("R") -> "gradientR_$id"
            release.type.toString().startsWith("G") -> "gradientG_$id"
            else -> "gradientM_$id"
        }
    }

    private fun createSvgFooter(): String {
        return "</svg>"
    }


    // Data class to hold dimension calculations
    private data class Dimensions(
        val contentWidth: Float,
        val contentHeight: Float,
        val scaledWidth: Float,
        val scaledHeight: Float,
        val scale: Float
    )

    private data class CardTextLayout(
        val goalLines: List<String>,
        val detailLinesVisible: List<String>,
        val hiddenDetailCount: Int,
        val needsOverflowIndicator: Boolean
    )

    private data class CardGeometry(
        val cardHeight: Float,
        val innerRectY: Float,
        val innerRectHeight: Float,
        val goalStartY: Float,
        val detailStartY: Float,
        val detailClipY: Float,
        val detailClipHeight: Float
    )

    private fun resolveContentFit(releaseStrategy: ReleaseStrategy): ContentFit {
        val direct = readStringProperty(releaseStrategy, "contentFit")
            ?: readStringProperty(releaseStrategy.displayConfig, "contentFit")

        return when (direct?.trim()?.uppercase()) {
            "GROW" -> ContentFit.GROW
            else -> ContentFit.CLAMP
        }
    }

    private fun computeCardLayout(release: Release, fit: ContentFit): Pair<CardGeometry, CardTextLayout> {
        val goalLines = wrapText(release.goal, GOAL_MAX_CHARS)
        val detailWrapped = flattenDetailLines(release.lines)

        val goalStartY = 92f
        val goalBlockHeight = kotlin.math.max(1, goalLines.size) * GOAL_LINE_HEIGHT
        val detailStartY = goalStartY + goalBlockHeight + GOAL_TO_DETAIL_GAP
        val topUsedInsideInner = detailStartY - INNER_RECT_Y

        val fixedInnerRectHeight = CARD_HEIGHT - INNER_RECT_Y - INNER_RECT_BOTTOM_PADDING
        val fixedAvailableDetailHeight = kotlin.math.max(0f, fixedInnerRectHeight - topUsedInsideInner - DETAIL_BOTTOM_PADDING)
        val maxLinesInFixed = kotlin.math.max(0, (fixedAvailableDetailHeight / DETAIL_LINE_HEIGHT).toInt())

        val textLayout = when (fit) {
            ContentFit.CLAMP -> {
                val hardMax = kotlin.math.min(MAX_BULLET_LINES, maxLinesInFixed)
                if (detailWrapped.size <= hardMax) {
                    CardTextLayout(
                        goalLines = goalLines,
                        detailLinesVisible = detailWrapped,
                        hiddenDetailCount = 0,
                        needsOverflowIndicator = false
                    )
                } else {
                    val visibleCount = kotlin.math.max(0, hardMax - 1)
                    val visible = detailWrapped.take(visibleCount)
                    CardTextLayout(
                        goalLines = goalLines,
                        detailLinesVisible = visible,
                        hiddenDetailCount = detailWrapped.size - visibleCount,
                        needsOverflowIndicator = true
                    )
                }
            }
            ContentFit.GROW -> {
                val limited = detailWrapped.take(MAX_BULLET_LINES)
                CardTextLayout(
                    goalLines = goalLines,
                    detailLinesVisible = limited,
                    hiddenDetailCount = 0,
                    needsOverflowIndicator = false
                )
            }
        }

        val detailLineCount = textLayout.detailLinesVisible.size + if (textLayout.needsOverflowIndicator) 1 else 0
        val detailBlockHeight = detailLineCount * DETAIL_LINE_HEIGHT

        val innerRectHeight = when (fit) {
            ContentFit.CLAMP -> fixedInnerRectHeight
            ContentFit.GROW -> kotlin.math.max(
                fixedInnerRectHeight,
                (topUsedInsideInner + detailBlockHeight + DETAIL_BOTTOM_PADDING)
            )
        }

        val cardHeight = when (fit) {
            ContentFit.CLAMP -> CARD_HEIGHT
            ContentFit.GROW -> kotlin.math.max(CARD_HEIGHT, INNER_RECT_Y + innerRectHeight + INNER_RECT_BOTTOM_PADDING)
        }

        val geometry = CardGeometry(
            cardHeight = cardHeight,
            innerRectY = INNER_RECT_Y,
            innerRectHeight = innerRectHeight,
            goalStartY = goalStartY,
            detailStartY = detailStartY,
            detailClipY = detailStartY - 12f,
            detailClipHeight = kotlin.math.max(0f, innerRectHeight - topUsedInsideInner - 2f)
        )

        return geometry to textLayout
    }

    private fun flattenDetailLines(lines: List<String>): List<String> {
        val flattened = mutableListOf<String>()
        lines.forEach { line ->
            val wrapped = wrapText(line, DETAIL_MAX_CHARS)
            wrapped.forEachIndexed { index, segment ->
                flattened.add(if (index == 0) "• $segment" else "  $segment")
            }
        }
        return flattened
    }

    private fun readStringProperty(target: Any?, propertyName: String): String? {
        if (target == null) return null
        val getter = "get" + propertyName.replaceFirstChar { it.uppercase() }
        try {
            val m = target.javaClass.methods.firstOrNull { it.name == getter && it.parameterCount == 0 }
            val value = m?.invoke(target) as? String
            if (!value.isNullOrBlank()) return value
        } catch (_: Exception) {
        }

        try {
            val f = target.javaClass.declaredFields.firstOrNull { it.name == propertyName } ?: return null
            f.isAccessible = true
            val value = f.get(target) as? String
            if (!value.isNullOrBlank()) return value
        } catch (_: Exception) {
        }
        return null
    }
}

fun main() {
    val data = """
  {
    "title": "Release Strategy Builder",
    "releases": [
      {
        "type": "M1",
        "lines": [
          "Team will deploy application and build out infrastructure with Terraform scripts.",
          "Team will Apply API gateway pattern to establish API version infrastructure.",
          "Team will validate access to the application",
          "Team will shutdown infrastructure as security is not in place."
        ],
        "date": "July 30th, 2023",
        "selected": true,
        "goal": "Our Goal is to provision new infrastructure on our cloud EKS platform without enabling production traffic",
        "completed": true
      },
      {
        "type": "RC1",
        "lines": [
          "Team will leverage CICD pipeline to deploy latest code",
          "Team will enable OAuth security on the API Gateway",
          "Team will make the application communication private and local to the API Gateway. Then switch out the config for the new API Gateway",
          "Team will enable API throttling at the Gateway layer",
          "Team will have QA do initial testing."
        ],
        "date": "September 20th, 2023",
        "completed": true,
        "goal": "Our goal is to deploy lastest code along with security applied at the API Layer"
      },
      {
        "type": "GA",
        "lines": [
          "Team will deploy latest code.",
          "QA will test and sign off"
        ],
        "date": "September 30th",
        "selected": true,
        "goal": "Our goal is to release version 1.0 of API making it generally available to all consumers."
      }
    ],
    "style": "TLS",
    "scale": 0.5,
    "displayConfig": {
      "colors": [
        "#5f57ff",
        "#2563eb",
        "#7149c6"
      ],
      "fontColor": "#fcfcfc"
    }
  }
    """.trimIndent()

    // Generate light mode version
    val lightRelease = Json.decodeFromString<ReleaseStrategy>(data)
    lightRelease.useDark = false
    val lightStr = ReleaseTimelineSummaryMaker().make(lightRelease, isPdf = false)
    val lightFile = File("gen/release_light.svg")
    lightFile.writeText(lightStr)
    println("Generated light mode SVG: ${lightFile.absolutePath}")

    // Generate dark mode version
    val darkRelease = Json.decodeFromString<ReleaseStrategy>(data)
    darkRelease.useDark = true
    val darkStr = ReleaseTimelineSummaryMaker().make(darkRelease, isPdf = false)
    val darkFile = File("gen/release_dark.svg")
    darkFile.writeText(darkStr)
    println("Generated dark mode SVG: ${darkFile.absolutePath}")
    val rm = ReleaseRoadMapMaker().make(releaseStrategy = darkRelease, isPdf = false, animate = "ON")
    val rmFile = File("gen/release_rm.svg")
    rmFile.writeText(rm)
    println("Generated release road map SVG: ${rmFile.absolutePath}")
    val rmPdf = ReleaseRoadMapMaker().make(releaseStrategy = darkRelease, isPdf = true, animate = "ON")
    val rmPdfFile = File("gen/release_rm_pdf.svg")
    rmPdfFile.writeText(rmPdf)
    println("Generated release road map PDF: ${rmPdfFile.absolutePath}")
    val rmPdf2 = ReleaseRoadMapMaker().make(releaseStrategy = darkRelease, isPdf = true, animate = "OFF")
    val rmPdfFile2 = File("gen/release_rm_pdf2.svg")
    rmPdfFile2.writeText(rmPdf2)
    println("Generated release road map PDF: ${rmPdfFile2.absolutePath}")
}
