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

/**
 * The Pill class represents a type of button that has a pill-shaped appearance.
 * It inherits from the Regular class.
 *
 * @param buttons The Buttons object that contains the theme and other properties for the button.
 */
class Pill(buttons: Buttons) : Regular(buttons) {


    /**
     * Generates the SVG code for rendering a list of buttons.
     *
     * @param index The index of the first button.
     * @param buttonList The list of buttons to be rendered.
     * @return The SVG code for rendering the buttons.
     */
    override fun drawButton(index: Int, buttonList: MutableList<Button>): String {
        val btns = StringBuilder()
        var win = "_top"
        buttons.theme?.let {
            if (it.newWin) {
                win = "_blank"
            }
        }

        var startX = 0

        var startY = 0
        if (index > 0) {
            startY = index * BUTTON_HEIGHT + (index * 10)
        }
        buttonList.forEach { button: Button ->
            var fill = "class=\"btn_${button.id}_cls\""
            var overlay = "url(#overlayGrad)"
            if(isPdf) {
                fill = "fill='${button.color}'"
                overlay = "${button.color}"
            }
            var href = """<a xlink:href="${button.link}" href="${button.link}" target="$win" style="text-decoration: none;">"""
            var endAnchor = "</a>"
            if(!button.enabled) {
                href = ""
                endAnchor = ""
            }
            btns.append(
                """
                $href
                <g role="button" cursor="pointer" transform="translate($startX, $startY)">
                    <rect id="button" x="5" y="5" width="$BUTTON_WIDTH" height="$BUTTON_HEIGHT" ry="26" rx="26" $fill filter="url(#buttonBlur)" />
                    <rect id="buttongrad" x="5" y="5" width="$BUTTON_WIDTH" height="$BUTTON_HEIGHT" ry="26" rx="26" fill="$overlay"/>
                    <rect id="buttontop" x="15" y="10.5" width="280" height="25" ry="24" rx="24" fill="url(#topshineGrad)" filter="url(#topshineBlur)"/>
                    <rect id="buttonbottom" x="25" y="50" width="260" height="7" fill="#ffffff" ry="24" rx="24" fill-opacity="0.3" filter="url(#bottomshine)"/>
                    <text id="label" x="150" y="43" text-anchor="middle" style="${button.buttonStyle?.labelStyle}">${button.label.escapeXml()}</text>
                </g>
                $endAnchor
                """
            )

            startX += BUTTON_WIDTH + BUTTON_PADDING

        }
        return btns.toString()
    }
    override fun height(): Float {
        val size = toRows().size
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        if (size > 1) {
            return (size * BUTTON_HEIGHT + (size * 10)) * scale + 10
        }
        val h = BUTTON_HEIGHT + 30
        return h * scale
    }

    override fun width(): Float {
        var columns = 3
        var scale = 1.0f
        buttons.theme?.let {
            columns = it.columns
            scale = it.scale
        }
        return (columns * BUTTON_WIDTH + columns * BUTTON_PADDING + columns * BUTTON_PADDING) * scale
    }
    companion object {
        const val BUTTON_HEIGHT: Int = 56
        const val BUTTON_WIDTH = 300
        const val BUTTON_PADDING = 12
    }
}