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
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.addLinebreaks
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
import io.docops.docopsextensionssupport.util.BackgroundHelper
import kotlin.text.toFloat
import kotlin.times

/**
 * Represents a class that extends the Regular class and implements additional functionality for drawing large buttons.
 *
 * @property buttons The Buttons instance to be used for drawing buttons.
 */
class Large(buttons: Buttons) : AbstractButtonShape(buttons) {

    private val buttonHeight = 400
    private val buttonWidth = 300
    private val buttonSpacing = 12
    private val rowSpacing = 10

    override fun createShape(type: String): String {
        val sb = StringBuilder()
        val rows = toRows()
        val columns = buttons.theme?.columns ?: 3
        val width = columns * (buttonWidth + buttonSpacing) + buttonSpacing
        val height = rows.size * (buttonHeight + rowSpacing) + rowSpacing

        val id = "large_${buttons.id}"

        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        sb.append(createSvgHeader(width, height,scale, id))
        sb.append(createDefs(id))
        sb.append(createStyles(id))


        // Background
        sb.append("""<g transform="scale($scale)">""")
        sb.append(BackgroundHelper.getBackGroundPath(useDark = buttons.useDark, id = buttons.id, width = width.toFloat() * scale, height = height.toFloat()* scale))
        //sb.append("""<path d="M 0 12.0 A 12.0 12.0 0 0 1 12.0 0 L ${width - 12} 0 A 12.0 12.0 0 0 1 $width 12.0 L $width ${height - 12} A 12.0 12.0 0 0 1 ${width - 12} $height L 12.0 $height A 12.0 12.0 0 0 1 0 ${height - 12} Z" fill="#F2F2F7" />""")

        var y = 10
        rows.forEach { row ->
            var x = 10
            row.forEach { button ->
                sb.append(createButton(button, x, y, id))
                x += buttonWidth + buttonSpacing
            }
            y += buttonHeight + rowSpacing
        }

        sb.append("</g></svg>")
        return sb.toString()
    }

    override fun height(): Float {
        val rows = toRows()
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        return (rows.size * (buttonHeight + rowSpacing) + 20) * scale
    }

    override fun width(): Float {
        var columns = 3
        var scale = 1.0f
        buttons.theme?.let {
            columns = it.columns
            scale = it.scale
        }
        return (columns * (buttonWidth + buttonSpacing) + 20) * scale
    }

    private fun createSvgHeader(width: Int, height: Int, scale: Float, id: String): String {
        val scaledWidth = (width * scale).toInt()
        val scaledHeight = (height * scale).toInt()
        return """<svg xmlns="http://www.w3.org/2000/svg" width="${scaledWidth/DISPLAY_RATIO_16_9}" height="${scaledHeight/DISPLAY_RATIO_16_9}" viewBox="0 0 $scaledWidth $scaledHeight" xmlns:xlink="http://www.w3.org/1999/xlink" id="$id" zoomAndPan="magnify" preserveAspectRatio="xMidYMid meet">"""
    }

    private fun createButton(button: Button, x: Int, y: Int, id: String): String {
        val gradientId = "btn_${button.id}_gradient"
        val sb = StringBuilder()

        // Wrap in link if href exists
        if (button.link.isNotEmpty()) {
            sb.append("""<a xlink:href="${button.link}" href="${button.link}" target="_top" style="text-decoration: none;">""")
        }

        sb.append("""<g transform="translate($x,$y)" class="modern-card-button">""")

        // Main card background
        sb.append("""<rect x="0" y="0" width="300" height="400" rx="18" ry="18" fill="white" filter="url(#cardShadow_$id)"/>""")

        // Top colored section (190px)
        sb.append("""<rect x="0" y="0" width="300" height="190" rx="18" ry="18" fill="url(#$gradientId)" class="card-top"/>""")
        sb.append("""<rect x="0" y="172" width="300" height="18" fill="url(#$gradientId)"/>""")

        // Shimmer overlay
        sb.append("""<rect x="0" y="0" width="300" height="190" rx="18" ry="18" fill="url(#shimmer_$id)" opacity="0.6"/>""")
        sb.append("""<rect x="0" y="172" width="300" height="18" fill="url(#shimmer_$id)" opacity="0.6"/>""")

        // Icon area (centered in top section)
        button.embeddedImage?.let {
            sb.append(createImageIcon(it))
        }


        // Bottom text section (210px)
        sb.append("""<rect x="0" y="190" width="300" height="210" rx="0" ry="0" fill="#FAFAFA"/>""")
        sb.append("""<rect x="0" y="382" width="300" height="18" rx="0" ry="0" fill="#FAFAFA"/>""")

        // Divider line
        sb.append("""<line x1="20" y1="190" x2="280" y2="190" stroke="url(#dividerGradient_$id)" stroke-width="1"/>""")

        // Text content
        sb.append(createTextContent(button))

        // Hover border
        sb.append("""<rect x="0" y="0" width="300" height="400" rx="18" ry="18" fill="none" stroke="#FFD700" stroke-width="3" opacity="0" class="hover-border"/>""")

        sb.append("</g>")

        if (button.link.isNotEmpty()) {
            sb.append("</a>")
        }

        return sb.toString()
    }

    private fun createImageIcon(embeddedImage: EmbeddedImage): String {
        val sb = StringBuilder()
        sb.append("""<g transform="translate(150, 95)">""")
        sb.append("""<circle cx="0" cy="0" r="50" fill="rgba(255,255,255,0.2)" filter="url(#iconGlow)"/>""")
        sb.append("""<circle cx="0" cy="0" r="45" fill="rgba(255,255,255,0.95)"/>""")

        // Embedded image
        sb.append("""<image href="${embeddedImage.ref}" x="-40" y="-40" width="80" height="80" preserveAspectRatio="xMidYMid meet"/>""")

        sb.append("</g>")
        return sb.toString()
    }

    private fun createDefaultIcon(button: Button): String {
        val sb = StringBuilder()
        sb.append("""<g transform="translate(150, 95)">""")
        sb.append("""<circle cx="0" cy="0" r="50" fill="rgba(255,255,255,0.2)" filter="url(#iconGlow)"/>""")
        sb.append("""<circle cx="0" cy="0" r="45" fill="rgba(255,255,255,0.95)"/>""")

        // Default document icon
        sb.append("""<g opacity="0.8">""")
        sb.append("""<rect x="-20" y="-25" width="40" height="50" rx="4" fill="currentColor" opacity="0.3"/>""")
        sb.append("""<rect x="-18" y="-23" width="36" height="46" rx="3" fill="none" stroke="currentColor" stroke-width="2.5"/>""")
        sb.append("""<line x1="-12" y1="-13" x2="12" y2="-13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>""")
        sb.append("""<line x1="-12" y1="-3" x2="12" y2="-3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>""")
        sb.append("""<line x1="-12" y1="7" x2="6" y2="7" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>""")
        sb.append("</g>")

        sb.append("</g>")
        return sb.toString()
    }

    private fun createTextContent(button: Button): String {
        val sb = StringBuilder()
        sb.append("""<g transform="translate(10, 190)">""")

        // Title
        sb.append("""<text x="0" y="30" font-family="'Inter var', system-ui, 'Helvetica Neue', sans-serif" font-size="18" font-weight="700" fill="#111827">""")
        sb.append(escapeXml(button.label))
        sb.append("</text>")

        // Type/Category with displayColor

        val displayColor = button.color?.let { if (it.isNotEmpty()) button.color else button.color }
        sb.append("""<text x="0" y="52" font-family="'Inter var', system-ui, 'Helvetica Neue', sans-serif" font-size="13" font-weight="600" fill="$displayColor">""")
        sb.append(button.type?.let { escapeXml(it) })
        sb.append("</text>")

        // Description (wrapped)
        button.description?.let {
            if (it.isNotEmpty()) {
                sb.append(wrapDescription(button.description, 280, 78))
            }
        }

        // Authors/Metadata
        button.author?.let {
            if (it.isNotEmpty()) {
                var authorsY = 150
                button.author.take(2).forEach { author ->
                    sb.append("""<text x="0" y="$authorsY" font-family="Arial, Helvetica, sans-serif" font-size="11" font-style="italic" font-weight="600" fill="#6B7280">""")
                    sb.append(escapeXml(author))
                    sb.append("</text>")
                    authorsY += 14
                }
            }
        }

        // Date
        button.date?.let {
            if (it.isNotEmpty()) {
                sb.append("""<text x="0" y="190" font-family="'Inter var', system-ui, 'Helvetica Neue', sans-serif" font-size="13" font-weight="700" fill="#111827">""")
                sb.append(escapeXml(button.date))
                sb.append("</text>")
            }
        }

        sb.append("</g>")
        return sb.toString()
    }

    private fun wrapDescription(text: String, maxWidth: Int, startY: Int): String {
        val sb = StringBuilder()
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        // Approximate character width (10px font, ~6px per char average)
        val charWidth = 6
        val maxChars = maxWidth / charWidth

        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (testLine.length <= maxChars) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        sb.append("""<text y="$startY" font-family="Arial, Helvetica, sans-serif" font-size="10" fill="#4B5563">""")
        lines.take(4).forEachIndexed { index, line ->
            val dy = if (index == 0) startY else 14
            val dyAttr = if (index == 0) "dy='0'" else """ dy="14""""
            sb.append("""<tspan x="0" $dyAttr>""")
            sb.append(escapeXml(line))
            sb.append("</tspan>")
        }
        sb.append("</text>")

        return sb.toString()
    }

    private fun createDefs(id: String): String {
        val sb = StringBuilder()
        sb.append("<defs>")
        sb.append(BackgroundHelper.getBackgroundGradient(buttons.useDark, buttons.id))
        // Card shadow
        sb.append("""<filter id="cardShadow_$id" x="-20%" y="-20%" width="140%" height="140%">""")
        sb.append("""<feDropShadow dx="0" dy="4" stdDeviation="8" flood-color="rgba(0,0,0,0.15)" />""")
        sb.append("""<feDropShadow dx="0" dy="2" stdDeviation="4" flood-color="rgba(0,0,0,0.1)" />""")
        sb.append("</filter>")

        // Icon glow
        sb.append("""<filter id="iconGlow">""")
        sb.append("""<feGaussianBlur stdDeviation="3" result="coloredBlur"/>""")
        sb.append("""<feMerge><feMergeNode in="coloredBlur"/><feMergeNode in="SourceGraphic"/></feMerge>""")
        sb.append("</filter>")

        // Shimmer overlay
        sb.append("""<linearGradient id="shimmer_$id" x1="0%" y1="0%" x2="0%" y2="100%">""")
        sb.append("""<stop offset="0%" stop-color="rgba(255,255,255,0.4)" />""")
        sb.append("""<stop offset="50%" stop-color="rgba(255,255,255,0.1)" />""")
        sb.append("""<stop offset="100%" stop-color="rgba(255,255,255,0)" />""")
        sb.append("</linearGradient>")

        // Divider gradient
        sb.append("""<linearGradient id="dividerGradient_$id" x1="0%" y1="0%" x2="100%" y2="0%">""")
        sb.append("""<stop offset="0%" stop-color="rgba(0,0,0,0)" />""")
        sb.append("""<stop offset="20%" stop-color="rgba(0,0,0,0.1)" />""")
        sb.append("""<stop offset="80%" stop-color="rgba(0,0,0,0.1)" />""")
        sb.append("""<stop offset="100%" stop-color="rgba(0,0,0,0)" />""")
        sb.append("</linearGradient>")

        // Button gradients for each button
        buttons.buttons.forEach { button ->
            val gradientId = "btn_${button.id}_gradient"
            val svgColor = SVGColor(button.color!!, gradientId)
            sb.append(svgColor.linearGradient)

        }

        sb.append("</defs>")
        return sb.toString()
    }

    private fun createStyles(id: String): String {
        return """
            <style>
                #$id .modern-card-button {
                    transition: all 0.3s ease;
                }
                #$id .modern-card-button:hover {
                    filter: grayscale(100%) sepia(100%);
                }
                #$id .modern-card-button:hover .hover-border {
                    opacity: 1;
                }
                #$id .card-top {
                    transition: filter 0.3s ease;
                }
                #$id .modern-card-button:hover .card-top {
                    filter: brightness(1.1);
                }
            </style>
        """.trimIndent()
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
