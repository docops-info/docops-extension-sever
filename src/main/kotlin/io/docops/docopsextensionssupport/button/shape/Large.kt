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
import io.docops.docopsextensionssupport.qrcode.ErrorCorrectionLevel
import io.docops.docopsextensionssupport.qrcode.QRCodeGenerator
import io.docops.docopsextensionssupport.qrcode.buttonWaveTheme
import io.docops.docopsextensionssupport.qrcode.organicWaveTheme
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.util.BackgroundHelper

/**
 * Represents a class that extends the Regular class and implements additional functionality for drawing large buttons.
 *
 * @property buttons The Buttons instance to be used for drawing buttons.
 */
/**
 * Large card button visualization.
 * 
 * Aesthetic Direction: Option A — Refined Editorial
 * - Minimalist card with subtle depth and clear typographic hierarchy.
 * - Leans on drop shadows and subtle gradients rather than bold outlines.
 * - Brand identity is carried via a vertical accent strip on the left edge.
 * - Typography uses Lexend for a modern, approachable feel.
 */
class Large(buttons: Buttons) : AbstractButtonShape(buttons) {

    private val buttonHeight = 400
    private val buttonWidth = 300
    private val buttonSpacing = 12
    private val rowSpacing = 10

    private val headerHeight = 190
    private val cardPadding = 20
    private val textStartY = 210

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
        val svgWidth = (scaledWidth / DISPLAY_RATIO_16_9).toInt()
        val svgHeight = (scaledHeight / DISPLAY_RATIO_16_9).toInt()
        return """<svg xmlns="http://www.w3.org/2000/svg" width="$svgWidth" height="$svgHeight" viewBox="0 0 $scaledWidth $scaledHeight" xmlns:xlink="http://www.w3.org/1999/xlink" id="$id" zoomAndPan="magnify" preserveAspectRatio="xMidYMid meet">"""
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

        sb.append("""<g transform="translate($x,$y)">""")
        sb.append("""<g class="modern-card-button" role="button" tabindex="0" onclick="window.open('${button.link.escapeXml()}', '$win')" onkeydown="if(event.key==='Enter'||event.key===' '){window.open('${button.link.escapeXml()}', '$win')}">""")
        sb.append("""<title>${button.label.escapeXml()} — ${button.type?.escapeXml() ?: "Component"}</title>""")

        val radius = 12
        // Main card background - Refined Editorial (Option A)
        // rx="12" for outer, drop the stroke, use shadow
        if (isPdf) {
            // Shadow fallback for static formats (PNG/PDF) where filters often fail
            sb.append("""<rect x="0" y="4" width="$buttonWidth" height="$buttonHeight" rx="$radius" fill="black" opacity="0.15"/>""")
        }
        val filterAttr = if (!isPdf) "filter=\"url(#cardShadow_$id)\"" else ""
        sb.append("""<rect x="0" y="0" width="$buttonWidth" height="$buttonHeight" rx="$radius" fill="$cardBg" $filterAttr/>""")

        // Top Header Section (Gradient Area)
        sb.append("""<path d="M0 $radius A$radius $radius 0 0 1 $radius 0 L${buttonWidth - radius} 0 A$radius $radius 0 0 1 $buttonWidth $radius L$buttonWidth $headerHeight L0 $headerHeight Z" fill="url(#$gradientId)"/>""")

        // Brand Accent Strip (Option A) - 6px wide on the left edge, arced to match card corners
        // deltaY = radius - sqrt(radius^2 - (radius - stripWidth)^2) = 12 - sqrt(144 - 36) = 12 - sqrt(108) approx 1.61
        val y1 = 1.61
        val y2 = buttonHeight - y1
        val y3 = buttonHeight - radius
        sb.append("""<path d="M0 $radius A$radius $radius 0 0 1 6 $y1 V$y2 A$radius $radius 0 0 1 0 $y3 Z" fill="$strokeColor" fill-opacity="0.8"/>""")

        // Geometric Pattern Overlay (Grouped by Button Type)
        val typeSeed = (button.type?.lowercase()?.hashCode() ?: 0)
        val patternChoice = Math.abs(typeSeed % 3)

        // Use a dynamic stroke color: white for dark mode "glow", secondary accent for light mode "blueprint"
        val patternStroke = "white"


        sb.append("""<g opacity="0.32">""")
        when (patternChoice) {
            1 -> { // Square/Box Pattern
                sb.append("""<rect x="230" y="20" width="60" height="60" fill="none" stroke="$patternStroke" stroke-width="1.0" transform="rotate(15, 260, 50)"/>""")
                sb.append("""<rect x="250" y="70" width="40" height="40" fill="none" stroke="$patternStroke" stroke-width="1.0" transform="rotate(-10, 270, 90)"/>""")
                sb.append("""<rect x="210" y="50" width="30" height="30" fill="none" stroke="$patternStroke" stroke-width="1.0" transform="rotate(30, 225, 65)"/>""")
                sb.append("""<rect x="270" y="10" width="20" height="20" fill="none" stroke="$patternStroke" stroke-width="1.0" transform="rotate(-20, 280, 20)"/>""")
            }
            2 -> { // Technical Lines Pattern
                for (i in 0..9) {
                    val offset = i * 10
                    sb.append("""<line x1="${180 + offset}" y1="0" x2="${300}" y2="${120 - offset}" stroke="$patternStroke" stroke-width="1.0"/>""")
                }
            }
            else -> { // Circles Pattern
                sb.append("""<circle cx="260" cy="40" r="70" fill="none" stroke="$patternStroke" stroke-width="1.0"/>""")
                sb.append("""<circle cx="280" cy="80" r="50" fill="none" stroke="$patternStroke" stroke-width="1.0"/>""")
                sb.append("""<circle cx="240" cy="60" r="30" fill="none" stroke="$patternStroke" stroke-width="1.0"/>""")
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
        sb.append("""<line x1="$cardPadding" y1="$headerHeight" x2="${buttonWidth - cardPadding}" y2="$headerHeight" stroke="$textSecondary" stroke-width="0.5" opacity="0.2"/>""")

        // Text content
        sb.append(createTextContent(button, textPrimary, textSecondary, strokeColor))

        // Hover Effect Layer
        if (!isPdf) {
            sb.append("""<rect x="0" y="0" width="$buttonWidth" height="$buttonHeight" rx="12" fill="none" class="hover-overlay"/>""")
        }

        sb.append("</g></g>")



        return sb.toString()
    }

    private fun createImageIcon(embeddedImage: EmbeddedImage): String {
        val sb = StringBuilder()
        sb.append("""<g transform="translate(150, 95)">""")
        if(embeddedImage.spotlightOn) {
            sb.append("""<circle cx="0" cy="0" r="50" fill="rgba(255,255,255,0.2)" filter="url(#iconGlow)"/>""")
            sb.append("""<circle cx="0" cy="0" r="45" fill="rgba(255,255,255,0.95)"/>""")
        }
        // Embedded image
        val imageHref = if (embeddedImage.ref.startsWith("../../")) {
            // Fix relative path bug from remediation plan
            "/images/docops.svg" 
        } else {
            embeddedImage.ref
        }
        sb.append("""<image xlink:href="$imageHref" href="$imageHref" x="-40" y="-40" width="80" height="80" preserveAspectRatio="xMidYMid meet"/>""")

        sb.append("</g>")
        return sb.toString()
    }

    private fun createDefaultIcon(button: Button): String {
        val generator = QRCodeGenerator(useXml = false, 150, 150, theme = buttonWaveTheme)
        val svg = generator.generate(button.link, ErrorCorrectionLevel.M)
        val sb = StringBuilder()
        sb.append("""<g transform="translate(150, 95)">""")

        // White circular background with subtle glow (matching spotlight style)
        sb.append("""<circle cx="0" cy="0" r="80" fill="rgba(255,255,255,0.2)" filter="url(#iconGlow)"/>""")
        sb.append("""<circle cx="0" cy="0" r="75" fill="rgba(255,255,255,0.95)"/>""")

        // QR code centered within the circle
        sb.append("""<g transform="translate(-75, -75)">""")
        sb.append(svg)
        sb.append("</g>")

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

    private fun createTextContent(button: Button, primary: String, secondary: String, accent: String): String {
        val sb = StringBuilder()
        val fontMain = if (isPdf) "Helvetica" else docOpsTheme.fontFamily
        val fontMono = if (isPdf) "Courier" else "'JetBrains Mono', monospace"

        sb.append("""<g transform="translate($cardPadding, $textStartY)">""")

        // Type/Category (Monospaced style)
        sb.append(
            """<text x="0" y="20" font-family="$fontMono" font-size="${11 / docOpsTheme.fontWidthMultiplier}" font-weight="600" fill="$accent" style="text-transform: uppercase; letter-spacing: 2px;">"""
        )
        sb.append(button.type?.let { it.escapeXml() } ?: "COMPONENT")
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

        // Footer Accent (Visual Marker) - Full inner width (Phase 4.3)
        sb.append("""<rect x="0" y="165" width="260" height="2" rx="1" fill="$accent" opacity="0.3"/>""")

        // Date / Author - Small caps style (Phase 4.4)
        button.date?.let {
            sb.append(
                """<text x="260" y="170" text-anchor="end" font-family="$fontMain" font-size="10" font-weight="600" fill="$secondary" opacity="0.65" style="text-transform: uppercase; letter-spacing: 1px;">"""
            )
            sb.append(it.escapeXml())
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
                append("""<tspan x="0" dy="$dy">${line.escapeXml()}</tspan>""")
            }
        }
    }

    private fun estimateLineCount(label: String, maxCharsPerLine: Int): Int {
        val len = label.trim().length
        if (len <= maxCharsPerLine) return 1
        return 2 // we only wrap to max 2 lines above
    }

    private fun computeTitleFontSize(label: String): Int {
        val base = (22 / docOpsTheme.fontWidthMultiplier).toInt()
        val len = label.trim().length
        // Lexend is slightly wider than Arial/Archivo, tuning thresholds (Phase 3.3)
        return when {
            len > 35 -> (base - 5).coerceAtLeast(14)
            len > 25 -> (base - 3).coerceAtLeast(16)
            len > 15 -> (base - 1).coerceAtLeast(18)
            else -> base
        }
    }
    private fun wrapDescription(text: String, maxWidth: Int, startY: Int, color: String, font: String): String {
        val sb = StringBuilder()
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        val maxChars = (maxWidth / 6.5).toInt()

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

        sb.append("""<text y="$startY" font-family="$font" font-size="13" fill="$color">""")
        lines.take(4).forEachIndexed { index, line ->
            val dy = if (index == 0) 0 else 18
            sb.append("""<tspan x="0" dy="$dy">""")
            sb.append(line.escapeXml())
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
        val atmosphereStart = if (buttons.useDark) docOpsTheme.canvas else "#FAFAF7"
        val atmosphereEnd = if (buttons.useDark) docOpsTheme.surfaceImpact else "#EEF1F4"
        sb.append("""<radialGradient id="bg_grad_$id" cx="50%" cy="50%" r="70%">""")
        sb.append("""<stop offset="0%" stop-color="$atmosphereStart"/>""")
        sb.append("""<stop offset="100%" stop-color="$atmosphereEnd"/>""")
        sb.append("""</radialGradient>""")

        if (!isPdf) {
            sb.append("""<filter id="cardShadow_$id" x="-20%" y="-20%" width="140%" height="140%">""")
            sb.append("""<feGaussianBlur in="SourceAlpha" stdDeviation="12" result="blur"/>""")
            sb.append("""<feOffset dx="0" dy="8" result="offsetblur"/>""")
            sb.append("""<feFlood flood-color="black" flood-opacity="0.3"/>""")
            sb.append("""<feComposite in2="offsetblur" operator="in"/>""")
            sb.append("""<feMerge>""")
            sb.append("""<feMergeNode/>""")
            sb.append("""<feMergeNode in="SourceGraphic"/>""")
            sb.append("""</feMerge>""")
            sb.append("</filter>")
        }

        // Button gradients - Varied angles (Phase 4.2)
        buttons.buttons.forEachIndexed { index, button ->
            val gradientId = "btn_${button.id}_gradient"
            val baseColor = button.color ?: docOpsTheme.accentColor
            val stopColor = "#0f172a"
            val stopOpacity = "0.85"

            val (x1, y1, x2, y2) = when (index % 3) {
                1 -> listOf("0%", "100%", "100%", "0%")
                2 -> listOf("50%", "0%", "50%", "100%")
                else -> listOf("0%", "0%", "100%", "100%")
            }

            sb.append("""<linearGradient id="$gradientId" x1="$x1" y1="$y1" x2="$x2" y2="$y2">""")
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
                ${fontImport()}
                #$id .modern-card-button {
                    transition: transform 0.35s cubic-bezier(0.175, 0.885, 0.32, 1.275);
                    cursor: pointer;
                }
                #$id .modern-card-button:hover {
                    transform: translateY(-4px);
                    filter: drop-shadow(0 12px 16px rgba(0,0,0,0.4));
                }
                #$id .modern-card-button:focus-visible {
                    transform: translateY(-4px);
                    outline: 2px solid ${docOpsTheme.accentColor};
                    outline-offset: 4px;
                }
                #$id .hover-overlay {
                    fill: white;
                    opacity: 0;
                    transition: opacity 0.3s ease;
                }
                #$id .modern-card-button:hover .hover-overlay {
                    opacity: 0.08;
                }
            </style>
        """.trimIndent()
    }

}
