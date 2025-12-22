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

package io.docops.docopsextensionssupport.button.shape

import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons
import io.docops.docopsextensionssupport.button.EmbeddedImage
import io.docops.docopsextensionssupport.button.Link
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.svgsupport.textWidth
import java.awt.Font
import kotlin.compareTo
import kotlin.times

/**
 * Implements a rectangular card-style button layout with enhanced visual features.
 *
 * The Rectangle class extends the [Regular] class to create a more visually rich button
 * representation that resembles a card or tile. Each button is rendered as a larger
 * rectangular card with the following unique characteristics:
 *
 * Key features:
 * - Larger button size (310×120 pixels) compared to Regular buttons
 * - Card-like appearance with rounded corners (15px radius)
 * - Support for embedded images (98×98 pixels) on the left side
 * - Numerical indicators for each button
 * - Support for multiple links within each button
 * - Enhanced text layout with label positioned to the right of the image
 *
 * This shape is particularly useful for:
 * - Buttons that need to display more information
 * - Navigation elements that benefit from visual indicators
 * - Interfaces where buttons need to be more prominent
 * - Cases where embedded images help identify button purpose
 *
 * The Rectangle shape maintains the same row-based layout as Regular buttons
 * but with larger dimensions and more complex internal structure.
 */
class Rectangle(buttons: Buttons) : Regular(buttons) {

    /**
     * Draws the buttons using the specified theme scale and returns the generated SVG code as a string.
     */
    override fun draw(): String {
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }

        val sb = StringBuilder("<g transform=\"scale($scale)\">")
        val rows = toRows()
        var count = 0

        rows.forEachIndexed { index, buttons ->
            sb.append(drawButtonInternal(index, buttons, count))
            count += buttons.size
        }
        sb.append("</g>")
        return sb.toString()
    }

    private fun drawButtonInternal(index: Int, buttonList: MutableList<Button>, count: Int): String {
        val btns = StringBuilder()
        var win = "_top"
        buttons.theme?.let {
            if (it.newWin) {
                win = "_blank"
            }
        }

        var startX = 10
        var startY = 10
        if (index > 0) {
            startY = index * CARD_HEIGHT + (index * CARD_PADDING) + CARD_SPACING
        }
        var localCount = count

        buttonList.forEach { button: Button ->
            localCount++

            val isDark = buttons.useDark
            val cardColors = getCardColors(isDark)
            val textColors = getTextColors(isDark)

            // Strip any hardcoded fill from user style to prevent dark mode invisibility
            val cleanUserStyle = button.buttonStyle?.labelStyle?.replace(Regex("fill\\s*:\\s*[^;]+;?"), "") ?: ""

            // Truncate main label if needed
            val maxLabelWidth = CARD_WIDTH - 40
            val truncatedLabel = truncateText(button.label, maxLabelWidth.toFloat(), 18)
            val showTooltip = truncatedLabel != button.label

            var href = """onclick="window.open('${button.link}', '$win')" style="cursor: pointer;""""
            if (!button.enabled) {
                href = ""
            }
            btns.append(
                """
    <g transform="translate($startX,$startY)" class="card-button" $href>
        ${if (showTooltip) """<title>${button.label.escapeXml()}</title>""" else ""}
    
        <!-- Card Background -->
        <rect x="0" y="0" width="$CARD_WIDTH" height="$CARD_HEIGHT" 
              rx="$CARD_RADIUS" ry="$CARD_RADIUS"
              fill="${cardColors.background}"
              stroke="${cardColors.border}"
              stroke-width="1"
              ${if (!isPdf) "filter=\"url(#cardShadow_${buttons.id})\"" else ""}/>
    
        <!-- Vertical Accent Bar instead of Circle -->
        <rect x="0" y="20" width="4" height="60" 
              fill="${button.color ?: "#3498db"}" rx="2"/>
    
        <!-- Main label with Syne-like fallback -->
        <text x="20" y="$MAIN_LABEL_Y"
                fill="${textColors.primary}"
              font-family="'Syne', 'Inter', sans-serif"
              font-size="18"
              font-weight="800"
              style="$cleanUserStyle">${truncatedLabel.escapeXml()}</text>
    
        <!-- Additional links as Chips -->
        ${renderAdditionalLinks(button.links, textColors, win)}
    
    </g>
    """.trimIndent()
            )

            startX += CARD_WIDTH + CARD_PADDING
        }
        return btns.toString()
    }

    private fun renderAdditionalLinks(links: MutableList<Link>?, textColors: TextColors, win: String): String {
        if (links.isNullOrEmpty()) return ""

        val sb = StringBuilder()
        var currentX = 20
        val linkY = LINKS_START_Y

        links.forEachIndexed { index, link ->
            if (index > 2) return@forEachIndexed // Limit chips to prevent overflow

            val fontSize = 10
            val label = link.label.uppercase().escapeXml()
            val textWidth = label.textWidth("Arial", fontSize) + 20

            if (currentX + textWidth > CARD_WIDTH - 10) return@forEachIndexed

            val chipHtml = if (isPdf) {
                """
                <rect x="$currentX" y="${linkY - 14}" width="$textWidth" height="20" rx="6" fill="${textColors.link}" fill-opacity="${if(buttons.useDark) "0.15" else "0.1"}"/>
                <text x="${currentX + textWidth / 2}" y="${linkY - 4}"

                  fill="${textColors.link}"
                  font-family="'Inter', 'Segoe UI', sans-serif"
                  font-size="$fontSize"
                  font-weight="600"
                  text-anchor="middle"
                  dominant-baseline="middle">
                $label
            </text>
            """.trimIndent()
            } else {
                """
             <a xlink:href="${link.href}" href="${link.href}" target="$win">
                <rect x="$currentX" y="${linkY - 14}" width="$textWidth" height="20" rx="6" fill="${textColors.link}" fill-opacity="${if(buttons.useDark) "0.15" else "0.1"}" class="link-chip"/>
                <text x="${currentX + textWidth / 2}" y="${linkY - 4}"
                      fill="${textColors.link}"
                      font-family="'Inter', 'Segoe UI', sans-serif"
                      font-size="$fontSize"
                      font-weight="600"
                      text-anchor="middle"
                      dominant-baseline="middle"
                      style="cursor: pointer;">
                    $label
                </text>
            </a>
            """.trimIndent()
            }
            sb.append(chipHtml)
            currentX += (textWidth + 8).toInt()
        }

        return sb.toString()
    }
    private fun truncateText(text: String, maxWidth: Float, fontSize: Int): String {
        val textWidthPx = text.textWidth("Arial", fontSize)
        return if (textWidthPx > maxWidth) {
            // Binary search for the right length
            var low = 0
            var high = text.length
            var result = text

            while (low <= high) {
                val mid = (low + high) / 2
                val candidate = text.substring(0, mid) + "..."
                val candidateWidth = candidate.textWidth("Arial", fontSize)

                if (candidateWidth <= maxWidth) {
                    result = candidate
                    low = mid + 1
                } else {
                    high = mid - 1
                }
            }
            result
        } else {
            text
        }
    }

    private fun getCardColors(isDark: Boolean): CardColors {
        return if (isDark) {
            CardColors(
                background = "#111827", // Deeper slate/black
                border = "rgba(255,255,255,0.08)",
                circleBorder = "rgba(255,255,255,0.08)"
            )
        } else {
            CardColors(
                background = "#f8fafc",
                border = "rgba(0,0,0,0.05)",
                circleBorder = "rgba(0,0,0,0.05)"
            )
        }
    }

    private fun getTextColors(isDark: Boolean): TextColors {
        return if (isDark) {
            TextColors(
                primary = "#ffffff",   // Pure white for maximum contrast
                secondary = "#94a3b8",
                link = "#38bdf8"       // Vibrant sky blue
            )
        } else {
            TextColors(
                primary = "#0f172a",
                secondary = "#64748b",
                link = "#0284c7"
            )
        }
    }

    override fun defs(): String {
        val isDark = buttons.useDark
        return """
        <defs>
            <style>
                @import url('https://fonts.googleapis.com/css2?family=Syne:wght@800&amp;display=swap');
                .card-button {
                    transition: transform 0.2s ease-in-out;
                    transform-box: fill-box;
                    transform-origin: center;
                }
                
                .link-chip {
                    transition: fill-opacity 0.2s;
                }
                .link-chip:hover {
                    fill-opacity: 0.2;
                }
            </style>
            <filter id="cardShadow_${buttons.id}" x="-20%" y="-20%" width="140%" height="140%">
                <feDropShadow dx="0" dy="4" stdDeviation="6" 
                              flood-color="${if (isDark) "rgba(0,0,0,0.4)" else "rgba(0,0,0,0.08)"}"/>
            </filter>
        </defs>
    """.trimIndent()
    }

    override fun height(): Float {
        val size = toRows().size
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        if (size > 1) {
            return (size * CARD_HEIGHT + (size * CARD_PADDING) + CARD_SPACING * 2) * scale
        }
        return (CARD_HEIGHT + 40) * scale
    }

    override fun width(): Float {
        var columns = 3
        var scale = 1.0f
        buttons.theme?.let {
            columns = it.columns
            scale = it.scale
        }
        return (columns * CARD_WIDTH + (columns - 1) * CARD_PADDING + 20) * scale
    }

    private data class CardColors(
        val background: String,
        val border: String,
        val circleBorder: String
    )

    private data class TextColors(
        val primary: String,
        val secondary: String,
        val link: String
    )

    companion object {
        const val CARD_HEIGHT: Int = 100
        const val CARD_WIDTH = 300
        const val CARD_PADDING = 15
        const val CARD_SPACING = 15
        const val CARD_RADIUS = 12

        // Circle positioning (Not used in new design, but kept for compatibility)
        const val CIRCLE_SIZE = 32
        const val CIRCLE_CENTER_X = 25
        const val CIRCLE_CENTER_Y = 50

        // Text positioning
        const val LABEL_START_X = 20
        const val MAIN_LABEL_Y = 38
        const val LINKS_START_Y = 82
        const val LINK_LINE_HEIGHT = 20
    }
}
