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
import io.docops.docopsextensionssupport.svgsupport.addLinebreaks
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth

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
            var href = """<a xlink:href="${button.link}" href="${button.link}" target="$win" style="text-decoration: none;">"""
            var endAnchor = "</a>"
            if(!button.enabled) {
                href = ""
                endAnchor = ""
            }
            btns.append(
                """
                $href
                <g transform="translate($startX,$startY)" class="basecard btn_${button.id}_cls">
                <use xlink:href="#outerBox" stroke="$strokeColor"><title class="description">${button.label.escapeXml()}</title></use>
                <use xlink:href="#topTextBox"/>
                ${determineLineText(button)}
                ${drawText(button)}
                </g>
                $endAnchor
                """.trimIndent()
            )

            startX += 300 + BUTTON_PADDING

        }
        return btns.toString()
    }
    private fun determineLineText(button: Button): String {
        // Determine which modern gradient to use based on button color
        val modernGradient = when {
            button.color?.contains("blue", ignoreCase = true) == true -> "url(#modernBlueGradient)"
            button.color?.contains("purple", ignoreCase = true) == true -> "url(#modernPurpleGradient)"
            button.color?.contains("green", ignoreCase = true) == true -> "url(#modernGreenGradient)"
            button.color?.contains("orange", ignoreCase = true) == true || 
            button.color?.contains("yellow", ignoreCase = true) == true -> "url(#modernOrangeGradient)"
            button.color?.contains("red", ignoreCase = true) == true || 
            button.color?.contains("pink", ignoreCase = true) == true -> "url(#modernRedGradient)"
            else -> "url(#btn_${button.id})"
        }

        if(button.embeddedImage != null) {
            val img = button.embeddedImage
            return """
                <use xlink:href="#singleBox" fill="$modernGradient" filter="url(#modernShadow)"/>
                <image x="0" y="0" width="300" height="191" xlink:href="${img?.ref}" href="${img?.ref}" clip-path="inset(1px round 18px 18px 0px 0px)"/>""".trimIndent()
        }
        else if ((button.cardLine1 == null) || (button.cardLine2 == null)) {
            return """<use xlink:href="#singleBox" fill="$modernGradient" filter="url(#modernShadow)" class="modern-card"/>
            """.trimMargin()
        } else {
            var fill = modernGradient
            if(isPdf) {
                fill = "${button.color}"
            }
            return """
            <text text-anchor="middle" x="150" y="67.75" class="modern-text" style="fill: ${button.color}; font-weight: bold; font-family: 'Inter var', system-ui, 'Helvetica Neue', sans-serif; font-size: ${button.cardLine1.size};">${button.cardLine1.line.escapeXml()}
            </text>
            <g transform="translate(0,95.5)">
            <use xlink:href="#bottomTextBox" stroke="${button.color}" fill="$fill" filter="url(#softGlow)" class="modern-card"/>

            <text text-anchor="middle" x="150" y="67.75" class="modern-text" style="fill: #ffffff; font-weight: bold; font-family: 'Inter var', system-ui, 'Helvetica Neue', sans-serif; font-size: ${button.cardLine2.size};" >${button.cardLine2.line.escapeXml()}
            </text>
        </g>
            """.trimIndent()
        }
    }

    private fun drawText(button: Button): String {
        var desc = mutableListOf<String>()
        button.description?.let {
            desc= itemTextWidth(itemText = it, maxWidth = 295F, fontSize = 12, fontName = "Inter")
           // desc = addLinebreaks(it, 35)
        }
        val descList = StringBuilder()
        desc.forEach {
            descList.append("""<tspan x="10" dy="14" class="modern-text" style="${button.buttonStyle?.descriptionStyle ?: "font-size: 12px; font-weight: 400;"}">${it.toString().escapeXml()}</tspan>""")
        }
        val authors = StringBuilder()
        button.author?.let {
            it.forEach { txt ->
                authors.append("""<tspan x="10" dy="14" class="modern-text" style="${button.buttonStyle?.authorStyle ?: "font-size: 11px; font-style: italic; fill: #555;"}">${txt.escapeXml()}</tspan>""")
            }
        }
        var title  = mutableListOf<StringBuilder>()
        button.type?.let {
            title = addLinebreaks(it, 40)
        }
        val titleList = StringBuilder()
        title.forEach {
            titleList.append("""<tspan x="10" dy="14" class="modern-text" style="${button.buttonStyle?.typeStyle ?: "font-weight: 600;"}" fill="${button.color}">${it.toString().escapeXml()}</tspan>""")
        }
        var dt = ""
        button.date?.let {
            dt = "<tspan x=\"10\" dy=\"14\" class=\"modern-text\" style=\"${button.buttonStyle?.dateStyle ?: "font-size: 11px; fill: #777;"}\">${it}</tspan>"
        }
        return """
            <g transform="translate(0,190)" class="title">
            <rect x="0" y="0" width="300" height="210" rx="0" ry="0" fill="#fafafa" opacity="0.7" filter="url(#softGlow)"/>
            <text x="10" y="20" style="font-family: 'Inter var', system-ui, 'Helvetica Neue', sans-serif; font-size:12px;">
                <tspan class="modern-text" style="${button.buttonStyle?.labelStyle ?: "font-weight: 700; font-size: 14px;"}">${button.label.escapeXml()}</tspan>
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
