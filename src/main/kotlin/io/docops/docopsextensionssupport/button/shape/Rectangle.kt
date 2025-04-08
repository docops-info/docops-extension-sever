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
import io.docops.docopsextensionssupport.button.EmbeddedImage
import io.docops.docopsextensionssupport.button.Link
import io.docops.docopsextensionssupport.support.determineTextColor

/**
 * This class represents a rectangle shape that consists of buttons.
 * It extends the Regular class and inherits its properties and methods.
 */
class Rectangle(buttons: Buttons) : Regular(buttons) {

    /**
     * Draws the buttons using the specified theme scale and returns the generated SVG code as a string.
     *
     * @return The SVG code representing the buttons.
     */
    override fun draw() : String{
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        val sb = StringBuilder("<g transform=\"scale($scale)\">")
        val rows = toRows()
        var count = 0
        rows.forEachIndexed { index, buttons ->

            sb.append(drawButtonInternal(index, buttons, count))
            count += buttons.size
        }
        sb.append("</g>")
        return sb.toString()
    }
    private fun drawButtonInternal(index: Int, buttonList: MutableList<Button>, count: Int): String {
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
        var localCount = count

        buttonList.forEach { button: Button ->
            localCount++
            var textFillColor = "#111111"
            button.color?.let {
                textFillColor = determineTextColor(it)
            }
            var link = """<a xlink:href="${button.link}" target="$win" fill="$textFillColor">$localCount</a>"""
            if(isPdf) {
                link = "$localCount"
            }

            var imageOrLabel = """
            <rect x="10" y="10" height="98" width="98" class="btn_${button.id}_cls" rx="20" ry="20" fill="${button.color}"/>
            <g transform="translate(10,10)">
            <text x="49" y="68" text-anchor="middle" alignment-baseline="central" font-family="Helvetica, sans-serif" font-size="60px" filter="url(#Bevel2)">
                $link
            </text>
            </g>
            """.trimIndent()
            button.embeddedImage?.let {
                imageOrLabel = makeEmbedImage( it)
            }
            var href = """<a xlink:href="${button.link}" class="linkText" target="$win">"""
            var endAnchor = "</a>"
            if(!button.enabled) {
                href = ""
                endAnchor = ""
            }
            btns.append(
                """
        <g transform="translate($startX,$startY)" cursor="pointer">
            <rect x="0" y="0" width="310" stroke="#b2b2b2"  height="120" rx="15" ry="15" fill="#fcfcfc" fill-opacity='0.3'/>
            $href
            <text x="115" y="16" style="${button.buttonStyle?.labelStyle}">${button.label.escapeXml()}</text>
            $endAnchor
            $href
            $imageOrLabel
            $endAnchor
            ${linksToText(button.links, button.buttonStyle?.linkStyle)}
        </g>
        """.trimIndent()
            )

            startX += BUTTON_WIDTH + BUTTON_PADDING

        }
        return btns.toString()
    }

    private fun makeEmbedImage( buttonImage: EmbeddedImage): String {
        return """
            <image x="10" y="10" width="98" height="98" href="${buttonImage.ref}"/>""".trimIndent()

    }
    private fun linksToText(links: MutableList<Link>?, style: String?): String {
        val sb = StringBuilder("""<text x="115" y="20">""")
        var linkText = "linkText"
        buttons.theme?.let {
            if(it.useDark) {
                linkText = "linkTextDark"
            }
        }
        links?.let {
            it.forEach { link ->
                var linkElement = """<a xlink:href="${link.href}" class="$linkText" style="$style" target="_blank">${link.label.escapeXml()}</a>"""
                if(isPdf) {
                    linkElement = link.label.escapeXml()
                }
                sb.append("""
            <tspan x="115" dy="14" style="$style">
                $linkElement
            </tspan>
                """.trimIndent())
            }
        }
       sb.append("</text>")
        return sb.toString()
    }
    override fun height(): Float {
        val size = toRows().size
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        if (size > 1) {
            return (size * Slim.BUTTON_HEIGHT + (size * 10)) * scale + 10
        }
        val h = Slim.BUTTON_HEIGHT + 30
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
        const val BUTTON_HEIGHT: Int = 120
        const val BUTTON_WIDTH = 310
        const val BUTTON_PADDING = 10
        const val  BUTTON_SPACING = 10
    }
}