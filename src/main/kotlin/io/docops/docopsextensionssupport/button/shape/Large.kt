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
 * Large card button visualization.
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
        val width = width()
        val height = height()
        val sb = StringBuilder()
        sb.append(start(width, height))
        sb.append(standardDefs())
        sb.append(shapeDefs())
        sb.append(makeModernBackground(width, height))
        sb.append(draw())
        sb.append(end())
        return sb.toString()
    }

    open fun draw(): String {
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        val id = "btn-${buttons.id}"
        val sb = StringBuilder("<g id=\"$id\" transform=\"scale($scale)\">")
        val rows = toRows()
        var staggerIdx = 0
        rows.forEachIndexed { index, buttons ->
            sb.append(drawButton(index, buttons, staggerIdx))
            staggerIdx += buttons.size
        }
        sb.append("</g>")
        return sb.toString()
    }

    protected open fun start(width: Float, height: Float) : String {
        val svgWidth = String.format("%.1f", width / DISPLAY_RATIO_16_9)
        val svgHeight = String.format("%.1f", height / DISPLAY_RATIO_16_9)
        return """<svg xmlns="http://www.w3.org/2000/svg" width="$svgWidth" height="$svgHeight" viewBox="0 0 $width $height" xmlns:xlink="http://www.w3.org/1999/xlink" id="btn_${buttons.id}" zoomAndPan="magnify" preserveAspectRatio="xMidYMid meet">"""
    }

    protected fun end() = """</svg>"""

    fun drawButton(index: Int, buttonList: MutableList<Button>, rowStartStagger: Int): String {
        val btns = StringBuilder()
        var win = "_top"
        buttons.theme?.let {
            if (it.newWin) {
                win = "_blank"
            }
        }
        var startX = 26
        var startY = 25
        if (index > 0) {
            startY = index * (buttonHeight + rowSpacing) + 25
        }

        buttonList.forEachIndexed { i, button ->
            val delay = (rowStartStagger + i) * 0.05
            btns.append("""<g transform="translate($startX,$startY)">""")
            btns.append("""<g class="button-stagger" style="animation-delay: ${delay}s">""")
            btns.append(createButtonInternal(button, win))
            btns.append("</g>")
            btns.append("</g>")
            startX += buttonWidth + buttonSpacing
        }
        return btns.toString()
    }

    private fun createButtonInternal(button: Button, win: String): String {
        val gradientId = "btn_${button.id}_gradient"
        val id = buttons.id
        val sb = StringBuilder()
        
        val accentColor = button.color ?: themeColor("--accent")

        sb.append("""<g class="button-hover modern-card-button" role="button" tabindex="0" onclick="window.open('${button.link.escapeXml()}', '$win')" onkeydown="if(event.key==='Enter'||event.key===' '){window.open('${button.link.escapeXml()}', '$win')}">""")
        sb.append("""<title>${button.label.escapeXml()} — ${button.type?.escapeXml() ?: "Component"}</title>""")

        val radius = 12
        val filterAttr = if (!isPdf) "filter=\"url(#cardShadow_$id)\"" else ""
        sb.append("""<rect x="0" y="0" width="$buttonWidth" height="$buttonHeight" rx="$radius" fill="${themeColor("--surface")}" $filterAttr/>""")

        // Top Header Section
        sb.append("""<path d="M0 $radius A$radius $radius 0 0 1 $radius 0 L${buttonWidth - radius} 0 A$radius $radius 0 0 1 $buttonWidth $radius L$buttonWidth $headerHeight L0 $headerHeight Z" fill="url(#$gradientId)"/>""")

        // Brand Accent Strip
        val y1 = 1.61
        val y2 = buttonHeight - y1
        val y3 = buttonHeight - radius
        sb.append("""<path d="M0 $radius A$radius $radius 0 0 1 6 $y1 V$y2 A$radius $radius 0 0 1 0 $y3 Z" fill="$accentColor" fill-opacity="0.8"/>""")

        // Geometric Pattern Overlay
        val typeSeed = (button.type?.lowercase()?.hashCode() ?: 0)
        val patternChoice = Math.abs(typeSeed % 3)
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
                for (it in 0..9) {
                    val offset = it * 10
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

        // Divider line
        sb.append("""<line x1="$cardPadding" y1="$headerHeight" x2="${buttonWidth - cardPadding}" y2="$headerHeight" stroke="${themeColor("--text")}" stroke-width="0.5" opacity="0.2"/>""")

        // Text content
        sb.append(createTextContent(button))

        sb.append("</g>")
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
        sb.append("""<image xlink:href="${embeddedImage.ref}" href="${embeddedImage.ref}" x="-40" y="-40" width="80" height="80" preserveAspectRatio="xMidYMid meet"/>""")

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

    private fun createTextContent(button: Button): String {
        val sb = StringBuilder()
        val accent = button.color ?: themeColor("--accent")

        sb.append("""<g transform="translate($cardPadding, $textStartY)">""")

        // Type/Category
        sb.append(
            """<text x="0" y="20" font-size="11" font-weight="600" fill="$accent" style="text-transform: uppercase; letter-spacing: 2px;">"""
        )
        sb.append(button.type?.let { it.escapeXml() } ?: "COMPONENT")
        sb.append("</text>")

        // Title
        val titleFontSize = computeTitleFontSize(button.label)
        sb.append(
            """<text x="0" y="50" font-size="$titleFontSize" font-weight="800" fill="${themeColor("--text")}" style="text-transform: uppercase; letter-spacing: -0.5px;">"""
        )
        sb.append(createWrappedTitle(button.label, maxCharsPerLine = 18, maxLines = 2))
        sb.append("</text>")

        // Description
        button.description?.let {
            if (it.isNotEmpty()) {
                val titleLineCount = estimateLineCount(button.label, maxCharsPerLine = 18)
                val descY = if (titleLineCount > 1) 95 else 75
                sb.append(wrapDescription(it, 260, descY))
            }
        }

        // Footer Accent
        sb.append("""<rect x="0" y="165" width="260" height="2" rx="1" fill="$accent" opacity="0.3"/>""")

        // Date / Author
        button.date?.let {
            sb.append(
                """<text x="260" y="170" text-anchor="end" font-size="10" font-weight="600" fill="${themeColor("--text")}" opacity="0.65" style="text-transform: uppercase; letter-spacing: 1px;">"""
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

        // Simple word wrap by character count
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
        return 2 
    }

    private fun computeTitleFontSize(label: String): Int {
        val base = (22 / docOpsTheme.fontWidthMultiplier).toInt()
        val len = label.trim().length
        return when {
            len > 35 -> (base - 5).coerceAtLeast(14)
            len > 25 -> (base - 3).coerceAtLeast(16)
            len > 15 -> (base - 1).coerceAtLeast(18)
            else -> base
        }
    }
    private fun wrapDescription(text: String, maxWidth: Int, startY: Int): String {
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

        sb.append("""<text y="$startY" font-size="13" fill="${themeColor("--text")}" opacity="0.8">""")
        lines.take(4).forEachIndexed { index, line ->
            val dy = if (index == 0) 0 else 18
            sb.append("""<tspan x="0" dy="$dy">""")
            sb.append(line.escapeXml())
            sb.append("</tspan>")
        }
        sb.append("</text>")
        return sb.toString()
    }

    private fun shapeDefs(): String {
        val sb = StringBuilder()
        val id = "btn-${buttons.id}"
        
        val style = """
                [id='$id'] .modern-card-button {
                    transition: transform 0.35s cubic-bezier(0.175, 0.885, 0.32, 1.275);
                }
                [id='$id'] .button-hover:hover {
                    transform: translateY(-4px);
                }
        """.trimIndent()

        sb.append("<style>$style</style>")

        // Button gradients
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
        sb.append("""
            <filter id="iconGlow" x="-50%" y="-50%" width="200%" height="200%">
                <feGaussianBlur stdDeviation="4" result="blur" />
                <feComposite in="SourceGraphic" in2="blur" operator="over" />
            </filter>
        """.trimIndent())
        return sb.toString()
    }

    override fun height(): Float {
        val rows = toRows()
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        return (rows.size * (buttonHeight + rowSpacing) + 40) * scale
    }

    override fun width(): Float {
        var columns = 3
        var scale = 1.0f
        buttons.theme?.let {
            columns = it.columns
            scale = it.scale
        }
        return (columns * (buttonWidth + buttonSpacing) + 40) * scale
    }
}
