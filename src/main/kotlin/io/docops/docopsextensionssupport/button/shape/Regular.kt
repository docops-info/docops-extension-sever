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


import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons
import io.docops.docopsextensionssupport.support.determineTextColor

/**
 * Regular class is responsible for creating regular buttons shape.
 *
 * @param buttons The Buttons object containing the theme and button data.
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
            var filter = "filter=\"url(#Bevel2)\""
            var fill = "url(#btn_${button.id})"
            if(isPdf) {
                filter = ""
                fill = "${button.color}"
            }
            var btnLook = """fill="$fill" class="raise btn_${button.id}_cls" $filter"""
            var textFillColor = determineTextColor(button.color!!)
            var textClass = "glass"
            buttons.theme?.raise?.let {
                if(!it) {
                    btnLook = """fill="#fcfcfc" stroke="$fill" stroke-width="2""""
                    textFillColor = button.color!!
                    textClass = "light-shadow"
                }
            }

            btns.append(
                """
        <g transform="translate($startX,$startY)">
            <a xlink:href="${button.link}" target="$win" style='text-decoration: none; font-family:Arial; fill: #fcfcfc;'>
            <rect x="0" y="0" $btnLook width="300" height="30" rx="10" ry="10"/>
            <text x="150" y="20" text-anchor="middle" class="$textClass" style="${button.buttonStyle?.labelStyle}; fill:$textFillColor">${button.label.escapeXml()}</text>
            </a>
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
        return """<svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height" xmlns:xlink="http://www.w3.org/1999/xlink" id="${buttons.id}" zoomAndPan="magnify" preserveAspectRatio="none">"""
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
