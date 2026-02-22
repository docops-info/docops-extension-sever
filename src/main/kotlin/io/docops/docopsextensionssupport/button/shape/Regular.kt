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
            val accentColor = button.color ?: docOpsTheme.accentColor

            // Signal: neutral surfaces; accent is a cue (border/focus/active), not the fill.
            val bodyFill = if (buttons.useDark) docOpsTheme.surfaceImpact else "#FFFFFF"

            // Signal: label uses primary text color for authority and readability.
            val textFillColor = docOpsTheme.primaryText

            val font = docOpsTheme.fontFamily
            val isPdfMode = isPdf

            val borderWidth = if (button.active) 2.0 else 1.0
            val buttonOpacity = if (button.enabled) 1.0 else 0.55

            var href = """onclick="window.open('${button.link}', '$win')" style="cursor: pointer;""""
            if (!button.enabled) {
                href = ""
            }

            val groupClass = if (isPdfMode) "" else """class="btn-group""""
            val groupStyle = "" // Signal: keep buttons stable; page-level motion should orchestrate reveals.

            // Strip conflicting fill from user style (we control label color via tokens)
            val cleanUserStyle =
                button.buttonStyle?.labelStyle?.replace(Regex("fill\\s*:\\s*[^;]+;?"), "") ?: ""

            btns.append(
                """
                    <g transform="translate($startX,$startY)">
                        <g $groupClass $groupStyle $href opacity="$buttonOpacity">
                            <!-- Shadow Layer (subtle elevation, not decoration) -->
                            <rect x="2" y="3" width="300" height="40" rx="${docOpsTheme.cornerRadius}" fill="${docOpsTheme.surfaceImpact}" opacity="0.18"/>
                            
                            <!-- Main Button Body -->
                            <rect x="0" y="0" width="300" height="40" rx="${docOpsTheme.cornerRadius}"
                                  fill="$bodyFill" stroke="$accentColor" stroke-width="$borderWidth" class="btn-main"/>
                            
                            <!-- Active/Current: understated inner highlight so it reads even without hover -->
                            ${
                    if (button.active) {
                        """<rect x="2" y="2" width="296" height="36" rx="${docOpsTheme.cornerRadius - 2}"
                               fill="$accentColor" opacity="${if (buttons.useDark) "0.10" else "0.06"}"/>"""
                    } else ""
                }

                            <text x="150" y="26" text-anchor="middle" fill="$textFillColor"
                                  style="font-family: $font !important;$cleanUserStyle">
                                ${button.label.escapeXml()}
                            </text>
                        </g>
                    </g>
                """.trimIndent()
            )

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

    protected open fun defs(): String {
        val customStyle = if (isPdf) "" else """
            .btn-group { cursor: pointer; }
            .btn-main { transition: fill 220ms cubic-bezier(0.16, 1, 0.3, 1), transform 220ms cubic-bezier(0.16, 1, 0.3, 1); }
            .btn-group:hover .btn-main { transform: translateY(-1px); }
            .btn-group:hover { }
        """.trimIndent()

        var style = """
             <style>
            $customStyle

            #${buttons.id} .bar:hover {
                filter: grayscale(100%) sepia(100%);
            }
            #${buttons.id} .shadowed {
                -webkit-filter: drop-shadow(3px 3px 2px rgba(0, 0, 0, .3));
                filter: drop-shadow(3px 3px 2px rgba(0, 0, 0, .3));
            }
            </style>
        """.trimIndent()

        if (isPdf) {
            style = """

            """.trimIndent()
        }

        return """
            <defs>
            
            ${uses()}
           $style
            </defs>
        """.trimIndent()
    }
    companion object {
        const val BUTTON_PADDING = 10
    }
}
