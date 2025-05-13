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


import io.docops.docopsextensionssupport.adr.model.escapeXml
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
        buttonList.forEach { button: Button ->
            var fill = "class=\"btn_${button.id}_cls\""
            var overlay = "url(#overlayGrad)"
            if(isPdf) {
                fill = "fill='${button.color}'"
                overlay = "${button.color}"
            }
            var textFillColor = determineTextColor(button.color!!)
            var textClass = "glass"
            buttons.theme?.raise?.let {
                if(!it) {
                    textFillColor = button.color!!
                    textClass = "light-shadow"
                }
            }
            var href = """<a xlink:href="${button.link}" href="${button.link}" target="$win" style='text-decoration: none; font-family:Arial; fill: #fcfcfc;'>"""
            var endAnchor = "</a>"
            if(!button.enabled) {
                href = ""
                endAnchor = ""
            }
            btns.append(
                """
        $href
        <g role="button" cursor="pointer" transform="translate($startX,$startY)">
            <rect id="button" x="0" y="0" width="$BUTTON_WIDTH" height="$BUTTON_HEIGHT" rx="10" ry="10" $fill filter="url(#buttonBlur)" />
            <rect id="buttongrad" x="0" y="0" width="$BUTTON_WIDTH" height="$BUTTON_HEIGHT" rx="10" ry="10" fill="$overlay"/>
            <rect id="buttontop" x="5" y="2" width="${BUTTON_WIDTH - 10}" height="${BUTTON_HEIGHT/2}" rx="8" ry="8" fill="url(#topshineGrad)" filter="url(#topshineBlur)"/>
            <rect id="buttonbottom" x="10" y="${BUTTON_HEIGHT - 8}" width="${BUTTON_WIDTH - 20}" height="5" rx="2" ry="2" fill="#ffffff" fill-opacity="0.3" filter="url(#bottomshine)"/>
            <text id="label" x="${BUTTON_WIDTH/2}" y="${BUTTON_HEIGHT/2 + 5}" text-anchor="middle" class="$textClass" style="${button.buttonStyle?.labelStyle}; fill:$textFillColor">${button.label.escapeXml()}</text>
        </g>
        $endAnchor
        """.trimIndent()
            )

            startX += BUTTON_WIDTH + BUTTON_PADDING

        }
        return btns.toString()
    }
    protected open fun start() : String {
        val height= height()
        val width = width()
        return """<svg xmlns="http://www.w3.org/2000/svg" width="${width/ DISPLAY_RATIO_16_9}" height="${height/ DISPLAY_RATIO_16_9}" viewBox="0 0 $width $height" xmlns:xlink="http://www.w3.org/1999/xlink" id="${buttons.id}" zoomAndPan="magnify" preserveAspectRatio="xMidYMid meet">"""
    }

    protected fun end() = """</svg>"""
    protected open fun defs() : String{
        var strokeColor: String = "gold"
        buttons.theme?.let {
            strokeColor = it.strokeColor
        }
        var style = """
             <style>
            ${glass()}
            ${lightShadow()}
            ${raise(strokeColor = strokeColor)}
            ${baseCard()}
            ${gradientStyle()}
            ${myBox()}
            ${keyFrame()}
            ${linkText()}
            .bar:hover {
                filter: grayscale(100%) sepia(100%);
            }
            .shadowed {
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
            ${uses()}
           $style
            </defs>
        """.trimIndent()
    }
    companion object {
        const val BUTTON_PADDING = 10
    }
}
