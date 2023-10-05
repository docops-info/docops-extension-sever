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
 * The Slim class represents a slim version of a button display. It extends the Regular class.
 *
 * @property buttons The buttons object that provides the buttons to be displayed.
 */
class Slim(buttons: Buttons) : Regular(buttons) {

    /**
     * Draws a button on the screen with the given index and list of buttons.
     *
     * @param index the index of the button
     * @param buttonList the list of buttons
     * @return a string representing the SVG code for the button
     */
    override fun drawButton(index: Int, buttonList: MutableList<Button>): String {
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

        buttonList.forEach { button ->

            var lines = ""
            button.description?.let {
                lines += "<text x=\"0\" y=\"38\" >"
                lines += linesToMultiLineText(button.buttonStyle?.descriptionStyle, wrapText(it.escapeXml(), 30f), 10, 2)
                lines+= "</text>"
            }
            var linesOrImage = lines
            if(lines.trim().isEmpty()) {
                button.embeddedImage?.let {
                    linesOrImage= """
            <image x="0" y="35" width="150" height="90" href="${it.ref}"/>"""
                }
            }
            val title = linesToMultiLineText(button.buttonStyle?.labelStyle,wrapText(button.label.escapeXml(), 15f), 12, 75)
            var btnDate = ""
            button.date?.let {
                btnDate = it
            }
            var authors = ""
            button.author?.let {
                authors = authorsToTSpans(it, "145", button.buttonStyle?.authorStyle)
            }
            btns.append("""
         <g transform="translate($startX,$startY)" cursor="pointer">
        <a xlink:href="${button.link}" target="$win" style='text-decoration: none; font-family:Arial; fill: #fcfcfc;'>
        <rect x="0" y="0" fill="#fcfcfc" width="$BUTTON_HEIGHT" height="$BUTTON_HEIGHT" rx="5" ry="5"  stroke="#000000" class="btn_${button.id}_cls raise">
            <title>${button.label.escapeXml()}</title>
        </rect>
        <path fill="url(#overlayGrad)" class="btn_${button.id}_cls"  d="M 0 5.0 A 5.0 5.0 0 0 1 5.0 0 L 145.0 0 A 5.0 5.0 0 0 1 150.0 5.0 L 150.0 35.0 A 0.0 0.0 0 0 1 150.0 35.0 L 0.0 35.0 A 0.0 0.0 0 0 1 0 35.0 Z"/>
        <text text-anchor="middle" x="75" y="8" class="glass">
            $title
        </text>
        $linesOrImage
        <text x="145" y="135" text-anchor="end">${authors}</text>
        <text x="145" y="145" style="${button.buttonStyle?.dateStyle}" text-anchor="end">${btnDate}</text>
        </a>
        </g>
            """.trimIndent())
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

    fun authorsToTSpans(authors: List<String>, x: String, style: String?): String {
        val s = StringBuilder()
        authors.forEach {
            s.append("""
                <tspan x="$x" dy="-12" style="$style">$it</tspan>
            """.trimIndent())
        }
        return s.toString()
    }
    companion object {
        const val BUTTON_HEIGHT = 150
        const val BUTTON_WIDTH = 150
        const val BUTTON_PADDING = 10
    }
}