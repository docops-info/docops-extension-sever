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

import io.docops.docopsextensionssupport.svgsupport.escapeXml
import kotlinx.serialization.json.Json
import java.io.File


/**
 * This class represents a Release Timeline Summary Maker.
 * It extends the ReleaseTimelineMaker class.
 * The ReleaseTimelineSummaryMaker class is responsible for generating a summary of the release timeline
 * based on the given release strategy.
 */
class ReleaseTimelineSummaryMaker : ReleaseTimelineMaker() {

    companion object {
        private const val CARD_WIDTH = 400f
        private const val CARD_HEIGHT = 200f
        private const val TRIANGLE_WIDTH = 100f
        private const val CARD_SPACING = 20f
        private const val TITLE_HEIGHT = 60f
        private const val MARGIN = 20f
        private const val TEXT_MARGIN = 20f
        private const val GOAL_MAX_CHARS = 58  // Increased for longer goal text
        private const val DETAIL_MAX_CHARS = 80  // Increased for wider bullet wrapping
        private const val GOAL_HEIGHT = 45f  // Space reserved for goal text
        private const val MAX_BULLET_LINES = 8  // Maximum bullet lines before showing "..."
    }

    /**
     * Generates a SVG string representation of a document using the given release strategy.
     *
     * @param releaseStrategy The release strategy to use for generating the document.
     * @param isPdf Specifies whether the document format is PDF.
     * @return The SVG string representation of the generated document.
     */
    override fun make(releaseStrategy: ReleaseStrategy, isPdf: Boolean): String {
        val dimensions = calculateDimensions(releaseStrategy)
        val id = releaseStrategy.id

        val str = StringBuilder()
        str.append(createSvgHeader(dimensions, id, releaseStrategy.title))
        str.append(createDefinitions(isPdf, id, releaseStrategy))
        str.append(createBackground(dimensions, releaseStrategy))
        str.append(createTitle(releaseStrategy.title, dimensions, releaseStrategy))
        str.append(createMainContent(releaseStrategy, isPdf, id, dimensions))
        str.append(createSvgFooter())

        return str.toString()
    }

    private fun calculateDimensions(releaseStrategy: ReleaseStrategy): Dimensions {
        // Calculate width: each card + triangle + spacing, minus last spacing, plus margins
        val singleCardWidth = CARD_WIDTH + TRIANGLE_WIDTH + CARD_SPACING
        val contentWidth = (releaseStrategy.releases.size * singleCardWidth) - CARD_SPACING + (2 * MARGIN)
        val contentHeight = TITLE_HEIGHT + CARD_HEIGHT + (2 * MARGIN)

        return Dimensions(
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            scaledWidth = contentWidth * releaseStrategy.scale,
            scaledHeight = contentHeight * releaseStrategy.scale,
            scale = releaseStrategy.scale
        )
    }

    private fun createSvgHeader(dimensions: Dimensions, id: String, title: String): String {
        return """
            <svg width="${dimensions.scaledWidth}" height="${dimensions.scaledHeight}" 
                 viewBox="0 0 ${dimensions.contentWidth} ${dimensions.contentHeight}" 
                 xmlns="http://www.w3.org/2000/svg" 
                 xmlns:xlink="http://www.w3.org/1999/xlink" 
                 role="img" 
                 aria-label="DocOps: Release Strategy" 
                 id="ID$id">
                <desc>https://docops.io/extension</desc>
                <title>${title.escapeXml()}</title>
        """.trimIndent()
    }

    private fun createDefinitions(isPdf: Boolean, id: String, releaseStrategy: ReleaseStrategy): String {
        val str = StringBuilder()
        str.append("<defs>")

        // Enhanced geometric pattern for atmosphere
        str.append("""
            <pattern id="meshPattern_$id" width="100" height="100" patternUnits="userSpaceOnUse">
                <path d="M 100 0 L 0 0 0 100" fill="none" stroke="currentColor" stroke-width="0.2" opacity="0.05"/>
                <circle cx="50" cy="50" r="1" fill="currentColor" opacity="0.1"/>
            </pattern>
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
        return """
            <linearGradient id="$id" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:$color;stop-opacity:1" />
                <stop offset="100%" style="stop-color:${darkenColor(color)};stop-opacity:1" />
            </linearGradient>
            <linearGradient id="glass_$id" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:#ffffff;stop-opacity:0.2" />
                <stop offset="100%" style="stop-color:#ffffff;stop-opacity:0.05" />
            </linearGradient>
        """.trimIndent()
    }

    private fun darkenColor(color: String): String {
        return try {
            val r = color.substring(1, 3).toInt(16)
            val g = color.substring(3, 5).toInt(16)
            val b = color.substring(5, 7).toInt(16)
            String.format("#%02x%02x%02x", (r * 0.8).toInt(), (g * 0.8).toInt(), (b * 0.8).toInt())
        } catch (e: Exception) {
            color
        }
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
            <filter id="dropShadow" x="-10%" y="-10%" width="120%" height="120%">
                <feDropShadow dx="2" dy="2" stdDeviation="2" flood-opacity="0.3"/>
            </filter>
            <filter id="glowEffect" x="-50%" y="-50%" width="200%" height="200%">
                <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
                <feMerge> 
                    <feMergeNode in="coloredBlur"/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>
        """.trimIndent()
    }

    private fun createStyles(id: String, releaseStrategy: ReleaseStrategy): String {
        return """
            <style>
                @keyframes fadeInCustom {
                    from { opacity: 0; }
                    to { opacity: 1; }
                }
                #ID$id .release-card {
                    animation: fadeInCustom 0.8s ease-out forwards;
                    opacity: 0;
                }
                /* Use a separate class for the motion to avoid coordinate conflicts */
                #ID$id .animated-content {
                    transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1);
                }
                #ID$id .release-card:hover .animated-content {
                    transform: translateY(-8px);
                }
                #ID$id .milestone-text {
                    font-family: 'Outfit', sans-serif;
                    font-weight: 800;
                }
                #ID$id .date-text {
                    font-family: 'Outfit', sans-serif;
                    font-size: 11px;
                    letter-spacing: 0.1em;
                    text-transform: uppercase;
                }
                #ID$id .goal-text {
                    font-family: 'Plus Jakarta Sans', sans-serif;
                    font-size: 13px;
                    font-weight: 700;
                }
                /* Remove transform-box: fill-box; as it breaks relative translation in many SVG renderers */
                #ID$id text {
                    user-select: none;
                }
            </style>
        """.trimIndent()
    }


    private fun createBackground(dimensions: Dimensions, releaseStrategy: ReleaseStrategy): String {
        val backgroundColor = if (releaseStrategy.useDark) "#0f172a" else "#f8fafc"
        val patternColor = if (releaseStrategy.useDark) "#38bdf8" else "#020617"
        val id = releaseStrategy.id
        return """
            <rect width="${dimensions.contentWidth}" height="${dimensions.contentHeight}" fill="$backgroundColor"/>
            <rect width="${dimensions.contentWidth}" height="${dimensions.contentHeight}" fill="url(#meshPattern_${releaseStrategy.id})" color="$patternColor"/>
            <linearGradient id="bg_gradient_$id" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="$backgroundColor" stop-opacity="0.8"/>
                <stop offset="100%" stop-color="$backgroundColor" stop-opacity="0.95"/>
            </linearGradient>
            <rect width="${dimensions.contentWidth}" height="${dimensions.contentHeight}" fill="url(#bg_gradient_$id)"/>
        """.trimIndent()
    }



    private fun createTitle(title: String, dimensions: Dimensions, releaseStrategy: ReleaseStrategy): String {
        val titleFill = if (releaseStrategy.useDark) "#fcfcfc" else "#000000"
        return """
            <text x="${dimensions.contentWidth / 2}" y="30" 
                  fill="$titleFill" 
                  text-anchor="middle" 
                  font-size="18px" 
                  font-family="'Plus Jakarta Sans', 'Outfit', sans-serif" 
                  font-weight="800"
                  class="milestone-text">
                ${title.escapeXml()}
            </text>
        """.trimIndent()
    }

    private fun createMainContent(releaseStrategy: ReleaseStrategy, isPdf: Boolean, id: String, dimensions: Dimensions): String {
        val str = StringBuilder()

        releaseStrategy.releases.forEachIndexed { index, release ->
            val x = MARGIN + (index * (CARD_WIDTH + TRIANGLE_WIDTH + CARD_SPACING))
            val y = TITLE_HEIGHT
            val delay = index * 0.15

            // The outer group handles the SVG layout coordinates (Stable)
            str.append("""<g transform="translate($x,$y)" style="animation-delay: ${delay}s" class="release-card">""")
            // An inner group can safely handle CSS transforms for hover effects
            str.append("""<g class="animated-content">""")
            str.append(createReleaseCard(release, isPdf, id, releaseStrategy))
            str.append("</g>")
            str.append("</g>")
        }

        return str.toString()
    }


    private fun createReleaseCard(release: Release, isPdf: Boolean, id: String, releaseStrategy: ReleaseStrategy): String {
        val gradientId = getGradientId(release, id)
        val fontColor = releaseStrategy.displayConfig.fontColor
        val dateColor = if (releaseStrategy.useDark) "#94a3b8" else "#475569"

        val goalMaxWidth = if (release.completed) CARD_WIDTH - 80f else CARD_WIDTH - 50f
        val goalLines = wrapText(release.goal, GOAL_MAX_CHARS)
        val actualGoalHeight = (goalLines.size * 20f) + 10f

        // IMPORTANT: No translate() inside here. Everything starts from 0,0 relative to the parent <g>
        return """
            <!-- Main Card Body -->
            <path d="M 0,0 H ${CARD_WIDTH} V ${CARD_HEIGHT} H 0 Z" 
                  fill="url(#$gradientId)" 
                  stroke="rgba(255,255,255,0.2)" 
                  stroke-width="1" 
                  filter="url(#dropShadow)"/>
            
            <path d="M 0,0 H ${CARD_WIDTH} V ${CARD_HEIGHT} H 0 Z" 
                  fill="url(#glass_$gradientId)" 
                  pointer-events="none"/>

            <!-- Arrow Triangle (Relative to 0,0 of this card) -->
            <path d="M ${CARD_WIDTH},0 V ${CARD_HEIGHT} L ${CARD_WIDTH + TRIANGLE_WIDTH},${CARD_HEIGHT/2} Z" 
                  fill="url(#$gradientId)" 
                  stroke="rgba(255,255,255,0.2)" 
                  stroke-width="1"/>
            
            <path d="M ${CARD_WIDTH},0 V ${CARD_HEIGHT} L ${CARD_WIDTH + TRIANGLE_WIDTH},${CARD_HEIGHT/2} Z" 
                  fill="url(#glass_$gradientId)" 
                  pointer-events="none"/>
            
            <text x="${CARD_WIDTH/2}" y="-15" fill="${if (releaseStrategy.useDark) "#94a3b8" else "#475569"}" text-anchor="middle" class="date-text">
                ${release.date}
            </text>
            
            <text x="${CARD_WIDTH + TRIANGLE_WIDTH/2}" y="${CARD_HEIGHT/2 + 10}" 
                  fill="white" text-anchor="middle" font-size="28px" class="milestone-text">
                ${release.type}
            </text>
            
            ${createWrappedGoalText(release.goal, TEXT_MARGIN, 35.0f, goalMaxWidth, fontColor)}
            
            ${createDetailLines(release.lines, 35f + actualGoalHeight + 15f, CARD_WIDTH - (2 * TEXT_MARGIN), fontColor)}
        """.trimIndent()
    }


    private fun createWrappedGoalText(text: String, x: Float, y: Float, maxWidth: Float, fontColor: String): String {
        val wrappedLines = wrapText(text, GOAL_MAX_CHARS)
        val str = StringBuilder()

        str.append("""<text x="$x" y="$y" class="goal-text" fill="$fontColor">""")

        wrappedLines.forEachIndexed { index, line ->
            // Increased dy from 16 to 20 for better line height
            val dyValue = if (index == 0) "0" else "20"
            str.append("""<tspan x="$x" dy="$dyValue">${line.escapeXml()}</tspan>""")
        }

        str.append("</text>")

        return str.toString()
    }


    private fun createDetailLines(lines: List<String>, startY: Float, maxWidth: Float, fontColor: String): String {
        val str = StringBuilder()
        var lineCount = 0

        // Start with text element
        str.append("""<text x="$TEXT_MARGIN" y="$startY" class="detail-text" fill="$fontColor">""")

        for (line in lines) {
            if (lineCount >= MAX_BULLET_LINES) {
                str.append("""<tspan x="$TEXT_MARGIN" dy="16">...</tspan>""")
                break
            }

            // Wrap the line text but handle bullets properly
            val wrappedLines = wrapText(line, DETAIL_MAX_CHARS)

            wrappedLines.forEachIndexed { index, wrappedLine ->
                if (lineCount >= MAX_BULLET_LINES) return@forEachIndexed

                // Only add chevron on the first line of each original line
                val bulletText = if (index == 0) "» $wrappedLine" else "  $wrappedLine"
                // Increased dy from 12 to 16 for cleaner vertical rhythm
                val dyValue = if (lineCount == 0) "0" else "16"

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
