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



import io.docops.asciidoc.utils.addLinebreaks
import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons

/**
 * Represents a class that extends the Regular class and implements additional functionality for drawing large buttons.
 *
 * @property buttons The Buttons instance to be used for drawing buttons.
 */
class Large(buttons: Buttons) : Regular(buttons) {


    /**
     * Draws a button based on the provided index and button list.
     *
     * @param index The index of the button in the list.
     * @param buttonList The list of buttons to draw.
     * @return The HTML representation of the drawn buttons.
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
        var startX = 10

        var startY = 10
        if (index > 0) {
            startY = index * 410 + 10
        }
        buttonList.forEach { button: Button ->
            btns.append(
                """
                <a xlink:href="${button.link}" href="${button.link}" target="$win" style="text-decoration: none;">
                <g transform="translate($startX,$startY)" class="basecard btn_${button.id}_cls">
                <use xlink:href="#outerBox" stroke="$strokeColor"><title class="description">${button.label.escapeXml()}</title></use>
                <use xlink:href="#topTextBox"/>
                ${determineLineText(button)}
                ${drawText(button)}
                </g>
                </a>
                """.trimIndent()
            )

            startX += 300 + BUTTON_PADDING

        }
        return btns.toString()
    }
    private fun determineLineText(button: Button): String {
        if(button.embeddedImage != null) {
            val img = button.embeddedImage
            return """
                <use xlink:href="#singleBox"  fill="url(#btn_${button.id})"/>
                <image x="0" y="0" width="300" height="191" xlink:href="${img?.ref}" href="${img?.ref}" clip-path="inset(1px round 18px 18px 0px 0px)"/>""".trimIndent()
        }
        else if ((button.cardLine1 == null) || (button.cardLine2 == null)) {
            return """<use xlink:href="#singleBox"  fill="url(#btn_${button.id})"/>
            """.trimMargin()
        } else {
            return """
            <text text-anchor="middle" x="150" y="67.75" filter="url(#Bevel2)" style="fill: ${button.color}; font-weight: bold; font-family: Arial, Helvetica, sans-serif;font-size: ${button.cardLine1.size};">${button.cardLine1.line.escapeXml()}
            </text>
            <g transform="translate(0,95.5)">
            <use xlink:href="#bottomTextBox" stroke="${button.color}" fill="url(#btn_${button.id})"/>

            <text text-anchor="middle" x="150" y="67.75" filter="url(#Bevel2)" style="fill: #ffffff; font-weight: bold; font-family: Arial, Helvetica, sans-serif;font-size: ${button.cardLine2.size};" >${button.cardLine2.line.escapeXml()}
            </text>
        </g>
            """.trimIndent()
        }
    }

    private fun drawText(button: Button): String {
        var desc = mutableListOf<StringBuilder>()
        button.description?.let {
            desc = addLinebreaks(it, 35)
        }
        val descList = StringBuilder()
        desc.forEach {
            descList.append("""<tspan x="10" dy="14" style="${button.buttonStyle?.descriptionStyle}">${it.toString().escapeXml()}</tspan>""")
        }
        val authors = StringBuilder()
        button.author?.let {
            it.forEach { txt ->
                authors.append("""<tspan x="10" dy="14" style="${button.buttonStyle?.authorStyle}">${txt.escapeXml()}</tspan>""")
            }
        }
        var title  = mutableListOf<StringBuilder>()
        button.type?.let {
            title = addLinebreaks(it, 40)
        }
        val titleList = StringBuilder()
        title.forEach {
            titleList.append("""<tspan x="10" dy="14" style="${button.buttonStyle?.typeStyle}" fill="${button.color}">${it.toString().escapeXml()}</tspan>""")
        }
        var dt = ""
        button.date?.let {
            dt = "<tspan x=\"10\" dy=\"14\" style=\"${button.buttonStyle?.dateStyle}\">${it}</tspan>"
        }
        return """
            <g transform="translate(0,190)" class="title">
            <text x="10" y="20" style="font: 12px Arial, Helvetica, sans-serif;">
                <tspan style="${button.buttonStyle?.labelStyle}">${button.label.escapeXml()}</tspan>
                $titleList
                $descList
                $authors
                $dt
            </text>
        </g>
        """.trimIndent()
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
        const val BUTTON_HEIGHT: Int = 410
        const val BUTTON_WIDTH = 300
        const val BUTTON_PADDING = 12
    }
}