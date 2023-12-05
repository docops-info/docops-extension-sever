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

import io.docops.asciidoc.buttons.wrapText
import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons

/**
 * The `Round` class is a subclass of the `Regular` class and represents a round button layout.
 * It provides the functionality to draw round buttons based on the given data.
 *
 * @property buttons The `Buttons` object containing the button data.
 */
class Round(buttons: Buttons) : Regular(buttons) {
    /**
     * Draws a series of buttons based on the given index and list of buttons.
     *
     * @param index The starting index.
     * @param buttonList The list of buttons.
     * @return The HTML code representing the buttons.
     */
    override fun drawButton(index: Int, buttonList: MutableList<Button>): String {
        val btns = StringBuilder()
        var win = "_top"
        var strokeColor = "gold"
        buttons.theme?.let {
            if (it.newWin) {
                win = "_blank"
            }
            strokeColor = it.strokeColor
        }
        var startX = 70

        var startY = 65
        if (index > 0) {
            startY = index * BUTTON_HEIGHT + BUTTON_SPACING + 55
        }

        buttonList.forEach { button: Button ->
            val lines = wrapText(button.label.escapeXml(), 15f)
            var lineY = 0
            if(lines.size > 0) {
                lineY = lines.size * - 6
            }
            val title = linesToMultiLineText(button.buttonStyle?.labelStyle,
                lines, 12, 0)
            var filter = "filter=\"url(#Bevel2)\""
            var fill = "${button.color}"
            if(isPdf) {
                filter = ""
                fill = "url(#btn_${button.id})"
            }
            btns.append(
                """
        <g transform="translate($startX,$startY)" cursor="pointer">
        <a xlink:href="${button.link}" target="$win">
            <use x="0" y="0" xlink:href="#myCircle" class="raise btn_${button.id}_cls" fill="$fill" stroke-width="2" stroke="$strokeColor" stroke-dasharray="2000" stroke-dashoffset="2000">
                <animate id="p2" attributeName="stroke-dashoffset" begin="mouseover" end="mouseout" values="2037;0;2037" dur="5.5s" calcMode="linear" repeatCount="indefinite" />
                <title class="description">${button.description?.escapeXml()}</title>
            </use>
            <text $filter x="0" y="$lineY" text-anchor="middle">
                $title
            </text>
        </a>
    </g>
     
        """.trimIndent()
            )

            startX += BUTTON_WIDTH + BUTTON_PADDING

        }
        return btns.toString()
    }

    override fun height(): Float {
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        val size = toRows().size
        return ((size * 125) +(size * 5)) * scale
    }

    override fun width(): Float {
        var cols = 3
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
            cols = it.columns
        }
        return ((cols * 125) +(cols * 7)) * scale
    }
    companion object {
        const val BUTTON_HEIGHT: Int = 125
        const val BUTTON_WIDTH = 125
        const val BUTTON_PADDING = 0
    }
}