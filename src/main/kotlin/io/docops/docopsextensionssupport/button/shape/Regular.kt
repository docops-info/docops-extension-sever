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


import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9


/**
 * Implements the standard button shape with rounded rectangles.
 *
 * The Regular class is the base implementation for button shapes, providing a standard
 * appearance with rounded rectangle buttons arranged in rows. It serves as the parent
 * class for many other shape implementations that customize or extend this basic layout.
 *
 * Key features:
 * - Renders buttons as rounded rectangles with a 10px radius
 * - Arranges buttons in rows based on the theme's column setting
 * - Supports hover effects and visual styling through CSS
 * - Provides SVG filters for bevel and shadow effects
 * - Handles button scaling based on theme settings
 *
 * This class implements the complete rendering pipeline:
 * 1. Creates the SVG container with appropriate dimensions
 * 2. Defines styles, filters, and gradients in the defs section
 * 3. Draws the buttons in a grid layout
 * 4. Closes the SVG container
 *
 * @param buttons The Buttons collection to be rendered
 */
open class Regular(buttons: Buttons) : AbstractButtonShape(buttons) {

    /**
     * Creates a shape based on the given type.
     *
     * @param type the type of shape to create
     * @return the shape as a string representation
     */
    override fun createShape(type: String): String {
        val width = width()
        val height = height()
        val sb = StringBuilder()
        sb.append(start(width, height))
        sb.append(standardDefs())
        sb.append(makeModernBackground(width, height))
        sb.append(draw())
        sb.append(end())
        return sb.toString()
    }
    open fun draw() : String{
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        val sb = StringBuilder("<g id=\"btn-${buttons.id}\" transform=\"scale($scale)\">")
        val rows = toRows()
        var staggerIdx = 0
        rows.forEachIndexed { index, buttons ->
            sb.append(drawButton(index, buttons, staggerIdx))
            staggerIdx += buttons.size
        }
        sb.append("</g>")
        return sb.toString()
    }

    open fun drawButton(index: Int, buttonList: MutableList<Button>, rowStartStagger: Int): String {
        val btns = StringBuilder()
        var win = "_top"
        buttons.theme?.let {
            if (it.newWin) {
                win = "_blank"
            }
        }
        var startX = 20
        var startY = 20
        if (index > 0) {
            startY = index * BUTTON_HEIGHT + (index * BUTTON_PADDING) + 20
        }

        buttonList.forEachIndexed { i, button ->
            val delay = (rowStartStagger + i) * 0.05
            btns.append("""<g transform="translate($startX,$startY)">""")
            btns.append("""<g class="button-stagger" style="animation-delay: ${delay}s">""")
            btns.append(drawSingleButton(button, win))
            btns.append("</g>")
            btns.append("</g>")
            startX += BUTTON_WIDTH + BUTTON_PADDING
        }
        return btns.toString()
    }

    private fun drawSingleButton(button: Button, win: String): String {
        val accentColor = button.color ?: "var(--accent)"
        val isPdfMode = isPdf
        val buttonOpacity = if (button.enabled) 1.0 else 0.55
        
        // Accessibility and Interaction
        val groupClass = if (isPdfMode) "" else """class="button-hover""""
        val role = if (isPdfMode) "" else "role=\"button\""
        val tabIndex = if (isPdfMode) "" else "tabindex=\"0\""
        val cursor = if (button.enabled) "pointer" else "not-allowed"
        val interaction = if (button.enabled && !isPdfMode) {
            """onclick="window.open('${button.link}', '$win')" onkeydown="if(event.key==='Enter'||event.key===' '){event.preventDefault();window.open('${button.link}', '$win')}" style="cursor: $cursor;" """
        } else {
            """style="cursor: $cursor;" """
        }

        val cleanUserStyle = button.buttonStyle?.labelStyle?.replace(Regex("(?:fill|font-family|font-size|color)\\s*:\\s*[^;]+;?"), "") ?: ""

        val sb = StringBuilder()
        sb.append("""<g $groupClass $role $tabIndex $interaction opacity="$buttonOpacity">""")
        sb.append("""<title>${button.label.escapeXml()}</title>""")
        
        // Main Button Body
        sb.append("""<rect x="0" y="0" width="$BUTTON_WIDTH" height="$BUTTON_HEIGHT" rx="12" fill="var(--surface)" stroke="var(--accent)" stroke-opacity="0.2" stroke-width="1" filter="url(#cardShadow_${buttons.id})"/>""")
        
        // Brand Accent Strip
        sb.append(drawAccentStrip(accentColor))
        
        // Active State
        if (button.active) {
            sb.append(drawActiveState(accentColor))
        }
        
        // Label
        sb.append("""<text x="${BUTTON_WIDTH / 2}" y="${BUTTON_HEIGHT / 2 + 5}" text-anchor="middle" fill="var(--text)" style="font-size: 14px; font-weight: 600; $cleanUserStyle">""")
        sb.append(button.label.escapeXml())
        sb.append("</text>")
        
        sb.append("</g>")
        return sb.toString()
    }

    private fun drawAccentStrip(accentColor: String): String {
        val r = docOpsTheme.cornerRadius.toDouble()
        val sw = 4.0
        val height = BUTTON_HEIGHT.toDouble()
        if (r > 0) {
            val dy = r - Math.sqrt(Math.max(0.0, r * r - (r - sw) * (r - sw)))
            val y1 = dy
            val y2 = height - dy
            val y3 = height - r
            return """<path d="M0 $r A$r $r 0 0 1 $sw $y1 V$y2 A$r $r 0 0 1 0 $y3 Z" fill="$accentColor" fill-opacity="0.8"/>"""
        } else {
            return """<rect x="0" y="0" width="$sw" height="$height" fill="$accentColor" fill-opacity="0.8"/>"""
        }
    }

    private fun drawActiveState(accentColor: String): String {
        val r = Math.max(0.0, docOpsTheme.cornerRadius - 2.0)
        val opacity = if (buttons.useDark) "0.15" else "0.12"
        return """
            <rect x="2" y="2" width="${BUTTON_WIDTH - 4}" height="${BUTTON_HEIGHT - 4}" rx="$r" fill="$accentColor" opacity="$opacity"/>
            <rect x="4" y="${BUTTON_HEIGHT - 2}" width="${BUTTON_WIDTH - 8}" height="2" fill="$accentColor"/>
        """.trimIndent()
    }


    protected open fun start(width: Float, height: Float) : String {
        val svgWidth = String.format("%.1f", width / DISPLAY_RATIO_16_9)
        val svgHeight = String.format("%.1f", height / DISPLAY_RATIO_16_9)
        return """<svg xmlns="http://www.w3.org/2000/svg" width="$svgWidth" height="$svgHeight" viewBox="0 0 $width $height" xmlns:xlink="http://www.w3.org/1999/xlink" id="btn_${buttons.id}" zoomAndPan="magnify" preserveAspectRatio="xMidYMid meet">"""
    }

    protected fun end() = """</svg>"""

    override fun height(): Float {
        val rows = toRows()
        val rowCount = rows.size
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        val h = 20 + rowCount * BUTTON_HEIGHT + (rowCount) * BUTTON_PADDING + 10
        return h * scale
    }

    override fun width(): Float {
        val rows = toRows()
        val maxInRow = if(rows.isEmpty()) 0 else rows.maxOf { it.size }
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        val w = 20 + maxInRow * BUTTON_WIDTH + (maxInRow) * BUTTON_PADDING + 10
        return w * scale
    }
    companion object {
        const val BUTTON_PADDING = 10
    }
}
