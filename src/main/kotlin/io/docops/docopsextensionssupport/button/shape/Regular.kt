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
import io.docops.docopsextensionssupport.util.BackgroundHelper


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
        val sb = StringBuilder()
        sb.append(start())
        sb.append(defs())
        sb.append(draw())
        sb.append(end())
        return sb.toString()
    }
    open fun draw() : String{
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        val sb = StringBuilder("<g transform=\"scale($scale)\">")
        val rows = toRows()
        rows.forEachIndexed { index, buttons ->
            sb.append(drawButton(index, buttons))
        }
        sb.append("</g>")
        return sb.toString()
    }
    open fun drawButton(index: Int, buttonList: MutableList<Button>): String {
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
            startY = index * BUTTON_HEIGHT + (index * BUTTON_PADDING) + BUTTON_SPACING
        }
        buttonList.forEachIndexed { btnIdx, button ->
            val accentColor = button.color ?: "#6366F1"
            val bodyFill = if (buttons.useDark) "#1A1A1A" else "#F8F8F8"

            // Fix: Ensure high contrast for text
            val textFillColor = if (buttons.useDark) {
                accentColor // Neon glow on dark
            } else {
                // On light background, use the accent color directly for the label
                // but ensure it's not too light.
                accentColor
            }

            val animDelay = (index * 0.1) + (btnIdx * 0.05)

            val isPdfMode = isPdf

            var href = """onclick="window.open('${button.link}', '$win')" style="cursor: pointer;""""
            if(!button.enabled) {
                href = ""
            }

            // PDF and Print don't support CSS animations or complex hover interactions
            val groupStyle = if (isPdfMode) "" else """style="animation: slideIn 0.5s ease-out ${animDelay}s both;""""
            val groupClass = if (isPdfMode) "" else """class="btn-group""""
            // Strip conflicting fill from user style
            val cleanUserStyle = button.buttonStyle?.labelStyle?.replace(Regex("fill\\s*:\\s*[^;]+;?"), "") ?: ""

            btns.append("""
            <g transform="translate($startX,$startY)">
                <g $groupClass $groupStyle $href>
                    <!-- Shadow/Accent Layer -->
                    <rect x="4" y="4" width="300" height="32" rx="2" fill="$accentColor" opacity="0.3"/>
                    <!-- Main Button Body -->
                    <rect x="0" y="0" width="300" height="32" rx="2" fill="$bodyFill" stroke="$accentColor" stroke-width="1.5" class="btn-main"/>
                    ${if (!isPdfMode) """<rect x="0" y="0" width="300" height="16" rx="2" fill="white" opacity="0.05"/>""" else ""}
                    
                    <text x="150" y="21" text-anchor="middle" fill="$textFillColor" 
                        style="!important;$cleanUserStyle}">
                        ${button.label.escapeXml()}
                    </text>
                </g>
            </g>
            """.trimIndent())

            startX += BUTTON_WIDTH + BUTTON_PADDING
        }
        return btns.toString()
    }


    protected open fun start() : String {
        val height= height()
        val width = width()
        return """<svg xmlns="http://www.w3.org/2000/svg" width="${width/ DISPLAY_RATIO_16_9}" height="${height/ DISPLAY_RATIO_16_9}" viewBox="0 0 $width $height" xmlns:xlink="http://www.w3.org/1999/xlink" id="btn_${buttons.id}" zoomAndPan="magnify" preserveAspectRatio="xMidYMid meet">"""
    }

    protected fun end() = """</svg>"""
    protected open fun defs() : String{
        var strokeColor: String = "gold"
        buttons.theme?.let {
            strokeColor = it.strokeColor
        }

        val customStyle = if (isPdf) "" else """
            @keyframes slideIn {
                from { opacity: 0; transform: translateY(15px); }
                to { opacity: 1; transform: translateY(0); }
            }
            .btn-group { transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1); cursor: pointer; }
            .btn-group:hover { transform: translate(2px, 2px) !important; }
            .btn-main { transition: fill 0.2s ease; }
            .btn-group:hover .btn-main { fill: ${if (buttons.useDark) "#252525" else "#FFFFFF"} !important; }
        """.trimIndent()

        var style = """
             <style>
            $customStyle
            ${glass()}
            ${lightShadow()}
            ${raise(strokeColor = strokeColor)}
            ${baseCard()}
            ${gradientStyle()}
            ${myBox()}
            ${keyFrame()}
            ${linkText()}
            ${modernText()}
            ${modernCard()}
            #${buttons.id} .bar:hover {
                filter: grayscale(100%) sepia(100%);
            }
            #${buttons.id} .shadowed {
                -webkit-filter: drop-shadow(3px 3px 2px rgba(0, 0, 0, .3));
                filter: drop-shadow(3px 3px 2px rgba(0, 0, 0, .3));
            }
            </style>
        """.trimIndent()
        if(isPdf) {
            style = """

            """.trimIndent()
        }

        return """
            <defs>
            ${filters()}
            ${naturalShadow()}
            ${gradient()}
            ${modernGradients()}
            ${uses()}
           $style
            </defs>
        """.trimIndent()
    }
    companion object {
        const val BUTTON_PADDING = 10
    }
}
