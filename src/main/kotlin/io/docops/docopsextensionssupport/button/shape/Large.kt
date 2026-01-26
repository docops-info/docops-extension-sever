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
import io.docops.docopsextensionssupport.util.BackgroundHelper
import io.nayuki.qrcodegen.QrCode

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
        sb.append(createSvgHeader(width, height, scale, id))
        sb.append(createDefs(id))
        sb.append(createStyles(id))


        // Background
        sb.append("""<g transform="scale($scale)">""")
        // Distinctive Atmosphere Background
        val bgStart = if (buttons.useDark) "#0F172A" else "#F8FAFC"
        val bgEnd = if (buttons.useDark) "#020617" else "#F1F5F9"

        // Use Theme Canvas for Atmosphere
        sb.append("""<rect width="$width" height="$height" fill="url(#bg_grad_$id)" rx="${docOpsTheme.cornerRadius}"/>""")

//        sb.append("""<rect width="$width" height="$height" fill="url(#bg_grad_$id)" rx="12"/>""")

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
        val isDark = buttons.useDark

        // Drive colors from ThemeFactory
        val cardBg = docOpsTheme.canvas
        val textPrimary = docOpsTheme.primaryText
        val textSecondary = docOpsTheme.secondaryText
        val strokeColor = button.color ?: docOpsTheme.accentColor

        var win = "_top"
        buttons.theme?.let {
            if (it.newWin) {
                win = "_blank"
            }
        }

        sb.append("""<g transform="translate($x,$y)" class="modern-card-button" onclick="window.open('${button.link}', '$win')">""")

        // Main card background - High contrast border/glow for dark, subtle shadow for light
        val filterAttr = if (!isPdf) "filter=\"url(#cardShadow_$id)\"" else ""
        sb.append("""<rect x="0" y="0" width="300" height="400" rx="${docOpsTheme.cornerRadius * 2}" fill="$cardBg" stroke="$strokeColor" stroke-width="1.5" $filterAttr/>""")

        // Top Header Section (Gradient Area)
        sb.append("""<path d="M0 ${docOpsTheme.cornerRadius * 2} A${docOpsTheme.cornerRadius * 2} ${docOpsTheme.cornerRadius * 2} 0 0 1 ${docOpsTheme.cornerRadius * 2} 0 L${300 - docOpsTheme.cornerRadius * 2} 0 A${docOpsTheme.cornerRadius * 2} ${docOpsTheme.cornerRadius * 2} 0 0 1 300 ${docOpsTheme.cornerRadius * 2} L300 190 L0 190 Z" fill="url(#$gradientId)"/>""")

        // Geometric Pattern Overlay (Grouped by Button Type)
        val typeSeed = (button.type?.lowercase()?.hashCode() ?: 0)
        val patternChoice = Math.abs(typeSeed % 3)

        // Use a dynamic stroke color: white for dark mode "glow", secondary accent for light mode "blueprint"
        val patternStroke = "white"


        sb.append("""<g opacity="0.2">""")
        when (patternChoice) {
            1 -> { // Square/Box Pattern for a specific group
                sb.append("""<rect x="230" y="20" width="60" height="60" fill="none" stroke="$patternStroke" stroke-width="0.5" transform="rotate(15, 260, 50)"/>""")
                sb.append("""<rect x="250" y="70" width="40" height="40" fill="none" stroke="$patternStroke" stroke-width="0.5" transform="rotate(-10, 270, 90)"/>""")
            }
            2 -> { // Technical Lines Pattern for another group
                for (i in 0..4) {
                    val offset = i * 15
                    sb.append("""<line x1="${200 + offset}" y1="0" x2="${300}" y2="${100 - offset}" stroke="$patternStroke" stroke-width="0.5"/>""")
                }
            }
            else -> { // Circles Pattern (Default/Fallback group)
                sb.append("""<circle cx="260" cy="40" r="70" fill="none" stroke="$patternStroke" stroke-width="0.5"/>""")
                sb.append("""<circle cx="280" cy="80" r="50" fill="none" stroke="$patternStroke" stroke-width="0.5"/>""")
            }
        }
        sb.append("""</g>""")

        // Icon area
        button.embeddedImage?.let {
            if (it.qrEnabled) {
                sb.append(createDefaultIcon(button))
            } else {
                sb.append(createImageIcon(it))
            }
        }

        // Divider line (Subtle Glass effect)
        sb.append("""<line x1="20" y1="190" x2="280" y2="190" stroke="$textSecondary" stroke-width="0.5" opacity="0.2"/>""")

        // Text content
        sb.append(createTextContent(button, textPrimary, textSecondary, strokeColor))

        // Hover Effect Layer
        if (!isPdf) {
            sb.append("""<rect x="0" y="0" width="300" height="400" rx="24" fill="white" opacity="0" class="hover-overlay"/>""")
        }

        sb.append("</g>")



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

        val qrCode = QrCode.encodeText("${button.link}", QrCode.Ecc.HIGH)
        val svg = toSvgString(qrCode, 4, "#FFFFFF", "#640D5F", buttons.useDark)
        val sb = StringBuilder()
        sb.append("""<g transform="translate(75, 10)">""")
        sb.append(svg)
        sb.append("</g>")
        return sb.toString()
    }
    /**
	 * Returns a string of SVG code for an image depicting the specified QR Code, with the specified
	 * number of border modules. The string always uses Unix newlines (\n), regardless of the platform.
	 * @param qr the QR Code to render (not {@code null})
	 * @param border the number of border modules to add, which must be non-negative
	 * @param lightColor the color to use for light modules, in any format supported by CSS, not {@code null}
	 * @param darkColor the color to use for dark modules, in any format supported by CSS, not {@code null}
	 * @return a string representing the QR Code as an SVG XML document
	 * @throws NullPointerException if any object is {@code null}
	 * @throws IllegalArgumentException if the border is negative
	 */
	private fun toSvgString(qr: QrCode, border: Int, lightColor: String, darkColor: String , useDark: Boolean = false) : String {

        val fillColor = if(useDark) {
            lightColor
        } else {
            darkColor
        }
		if (border < 0)
			throw  IllegalArgumentException("Border must be non-negative");
		val brd = border
		val sb =  StringBuilder()
			.append(
                """<?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
                <svg xmlns="http://www.w3.org/2000/svg" version="1.1" viewBox="0 0 ${qr.size + brd * 2} ${qr.size + brd * 2}" height="150" width="150" stroke="none">
			    <rect width="100%" height="100%" fill="none"/>""")
			.append("""<path d=" """)
            for (y in 0 until qr.size) {
                for (x in 0 until qr.size) {
                    if (qr.getModule(x, y)) {
                        if (x != 0 || y != 0)
                            sb.append(" ")
                        sb.append("""M${x + brd},${y + brd}h1v1h-1z""")
                    }
                }
            }
		return sb
			.append("""" fill="$fillColor"/>""")
			.append("</svg>")
			.toString();
	}

    private fun createTextContent(button: Button, primary: String, secondary: String, accent: String): String {
        val sb = StringBuilder()
        val fontMain = if (isPdf) "Helvetica" else docOpsTheme.fontFamily
        val fontMono = if (isPdf) "Courier" else "'JetBrains Mono', monospace"

        sb.append("""<g transform="translate(20, 210)">""")

        // Type/Category (Monospaced style)
        sb.append(
            """<text x="0" y="20" font-family="$fontMono" font-size="${11 / docOpsTheme.fontWidthMultiplier}" font-weight="600" fill="$accent" style="text-transform: uppercase; letter-spacing: 2px;">"""
        )
        sb.append(button.type?.let { escapeXml(it) } ?: "COMPONENT")
        sb.append("</text>")

        // Title (WRAPPED)
        val titleFontSize = computeTitleFontSize(button.label)
        sb.append(
            """<text x="0" y="50" font-family="$fontMain" font-size="$titleFontSize" font-weight="800" fill="$primary" style="text-transform: uppercase; letter-spacing: -0.5px;">"""
        )
        sb.append(createWrappedTitle(button.label, maxCharsPerLine = 18, maxLines = 2))
        sb.append("</text>")

        // Description
        button.description?.let {
            if (it.isNotEmpty()) {
                // If title wraps to 2 lines, push description down a bit
                val titleLineCount = estimateLineCount(button.label, maxCharsPerLine = 18)
                val descY = if (titleLineCount > 1) 95 else 75
                sb.append(wrapDescription(it, 260, descY, secondary, fontMain))
            }
        }

        // Footer Accent (Visual Marker)
        sb.append("""<rect x="0" y="165" width="40" height="4" rx="2" fill="$accent"/>""")

        // Date / Author
        button.date?.let {
            sb.append(
                """<text x="260" y="170" text-anchor="end" font-family="$fontMain" font-size="11" font-weight="500" fill="$secondary" opacity="0.8">"""
            )
            sb.append(escapeXml(it))
            sb.append("</text>")
        }

        sb.append("</g>")
        return sb.toString()
    }

    private fun createWrappedTitle(label: String, maxCharsPerLine: Int, maxLines: Int): String {
        val clean = label.trim()
        if (clean.isEmpty()) return ""

        // Simple word wrap by character count (consistent with your description wrapping approach)
        val words = clean.split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var current = StringBuilder()

        for (w in words) {
            val candidate = if (current.isEmpty()) w else "${current} $w"
            if (candidate.length <= maxCharsPerLine || current.isEmpty()) {
                if (current.isNotEmpty()) current.append(" ")
                current.append(w)
            } else {
                lines.add(current.toString())
                current = StringBuilder(w)
                if (lines.size == maxLines) break
            }
        }
        if (lines.size < maxLines && current.isNotEmpty()) lines.add(current.toString())

        val clipped = if (lines.size > maxLines) lines.take(maxLines) else lines
        return buildString {
            clipped.forEachIndexed { idx, line ->
                val dy = if (idx == 0) "0" else "1.15em"
                append("""<tspan x="0" dy="$dy">${escapeXml(line)}</tspan>""")
            }
        }
    }

    private fun estimateLineCount(label: String, maxCharsPerLine: Int): Int {
        val len = label.trim().length
        if (len <= maxCharsPerLine) return 1
        return 2 // we only wrap to max 2 lines above
    }

    private fun computeTitleFontSize(label: String): Int {
        // Base is 22 (as before), reduce slightly for very long labels
        val base = (22 / docOpsTheme.fontWidthMultiplier).toInt()
        val len = label.trim().length
        return when {
            len > 40 -> (base - 4).coerceAtLeast(14)
            len > 28 -> (base - 2).coerceAtLeast(16)
            else -> base
        }
    }
    private fun wrapDescription(text: String, maxWidth: Int, startY: Int, color: String, font: String): String {
        val sb = StringBuilder()
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        val maxChars = maxWidth / 7

        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (testLine.length <= maxChars) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)

        sb.append("""<text y="$startY" font-family="$font" font-size="13" fill="$color" line-height="1.5">""")
        lines.take(4).forEachIndexed { index, line ->
            val dy = if (index == 0) 0 else 18
            sb.append("""<tspan x="0" dy="$dy">""")
            sb.append(escapeXml(line))
            sb.append("</tspan>")
        }
        sb.append("</text>")
        return sb.toString()
    }

    private fun createDefs(id: String): String {
        val sb = StringBuilder()
        val isDark = buttons.useDark
        sb.append("<defs>")

        // Atmosphere Gradient
        sb.append("""<radialGradient id="bg_grad_$id" cx="50%" cy="50%" r="70%">""")
        sb.append("""<stop offset="0%" stop-color="${docOpsTheme.canvas}"/>""")
        sb.append("""<stop offset="100%" stop-color="${docOpsTheme.surfaceImpact}"/>""")
        sb.append("""</radialGradient>""")

        if (!isPdf) {
            sb.append("""<filter id="cardShadow_$id" x="-20%" y="-20%" width="140%" height="140%">""")
            sb.append("""<feDropShadow dx="0" dy="8" stdDeviation="12" flood-color="black" flood-opacity="0.3"/>""")
            sb.append("</filter>")
        }

        // Button gradients
        buttons.buttons.forEach { button ->
            val gradientId = "btn_${button.id}_gradient"
            val baseColor = button.color ?: docOpsTheme.accentColor


            val stopColor = "#0f172a"
            val stopOpacity = "0.85"

            sb.append("""<linearGradient id="$gradientId" x1="0%" y1="0%" x2="100%" y2="100%">""")
            sb.append("""<stop offset="0%" stop-color="$baseColor"/>""")
            sb.append("""<stop offset="100%" stop-color="$stopColor" stop-opacity="$stopOpacity"/>""")
            sb.append("</linearGradient>")
        }
        // Refined Icon Glow - Subtle for Light, Atmospheric for Dark
        sb.append("""
            <filter id="iconGlow" x="-50%" y="-50%" width="200%" height="200%">
                <feGaussianBlur stdDeviation="4" result="blur" />
                <feComposite in="SourceGraphic" in2="blur" operator="over" />
            </filter>
        """.trimIndent())
        sb.append("</defs>")
        return sb.toString()
    }

    private fun createStyles(id: String): String {
        if (isPdf) return ""
        //language=html
        return """
            <style>
                @import url('https://fonts.googleapis.com/css2?family=Lexend:wght@400;700&amp;family=JetBrains+Mono:wght@600&amp;display=swap');
                #$id .modern-card-button {
                    transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
                    cursor: pointer;
                }                
                #$id .hover-overlay {
                    transition: opacity 0.3s ease;
                }
                #$id .modern-card-button:hover .hover-overlay {
                    opacity: 0.05;
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
