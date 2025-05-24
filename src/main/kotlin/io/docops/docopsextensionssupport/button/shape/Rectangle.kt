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
import io.docops.docopsextensionssupport.button.EmbeddedImage
import io.docops.docopsextensionssupport.button.Link
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.escapeXml

/**
 * Implements a rectangular card-style button layout with enhanced visual features.
 *
 * The Rectangle class extends the [Regular] class to create a more visually rich button
 * representation that resembles a card or tile. Each button is rendered as a larger
 * rectangular card with the following unique characteristics:
 *
 * Key features:
 * - Larger button size (310×120 pixels) compared to Regular buttons
 * - Card-like appearance with rounded corners (15px radius)
 * - Support for embedded images (98×98 pixels) on the left side
 * - Numerical indicators for each button
 * - Support for multiple links within each button
 * - Enhanced text layout with label positioned to the right of the image
 *
 * This shape is particularly useful for:
 * - Buttons that need to display more information
 * - Navigation elements that benefit from visual indicators
 * - Interfaces where buttons need to be more prominent
 * - Cases where embedded images help identify button purpose
 *
 * The Rectangle shape maintains the same row-based layout as Regular buttons
 * but with larger dimensions and more complex internal structure.
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
            <rect id="button" x="10" y="10" height="98" width="98" class="btn_${button.id}_cls" rx="20" ry="20" filter="url(#buttonBlur)"/>
            <rect id="buttongrad" x="10" y="10" height="98" width="98" rx="20" ry="20" fill="url(#overlayGrad)"/>
            <rect id="buttontop" x="15" y="12" height="40" width="88" rx="18" ry="18" fill="url(#topshineGrad)" filter="url(#topshineBlur)"/>
            <rect id="buttonbottom" x="20" y="100" height="5" width="78" rx="2" ry="2" fill="#ffffff" fill-opacity="0.3" filter="url(#bottomshine)"/>
            <g transform="translate(10,10)">
            <text x="49" y="68" text-anchor="middle" alignment-baseline="central" font-family="Helvetica, sans-serif" font-size="60px" class="glass">
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
            <rect id="mainButton" x="0" y="0" width="310" height="120" rx="15" ry="15" class="btn_${button.id}_cls" filter="url(#buttonBlur)"/>
            <rect id="mainButtongrad" x="0" y="0" width="310" height="120" rx="15" ry="15" fill="url(#overlayGrad)" fill-opacity="0.7"/>
            <rect id="mainButtontop" x="5" y="2" width="300" height="50" rx="13" ry="13" fill="url(#topshineGrad)" filter="url(#topshineBlur)"/>
            <rect id="mainButtonbottom" x="10" y="112" width="290" height="5" rx="2" ry="2" fill="#ffffff" fill-opacity="0.3" filter="url(#bottomshine)"/>
            $href
            <text x="115" y="16" class="glass" style="${button.buttonStyle?.labelStyle}">${button.label.escapeXml()}</text>
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
            <rect id="imageFrame" x="10" y="10" height="98" width="98" rx="20" ry="20" fill="#fcfcfc" filter="url(#buttonBlur)"/>
            <rect id="imageFrameGrad" x="10" y="10" height="98" width="98" rx="20" ry="20" fill="url(#overlayGrad)" fill-opacity="0.3"/>
            <image id="embeddedImage" x="10" y="10" width="98" height="98" href="${buttonImage.ref}" filter="url(#naturalShadow)"/>
            <rect id="imageFrameTop" x="15" y="12" height="30" width="88" rx="18" ry="18" fill="url(#topshineGrad)" fill-opacity="0.5" filter="url(#topshineBlur)"/>""".trimIndent()

    }
    private fun linksToText(links: MutableList<Link>?, style: String?): String {
        val sb = StringBuilder("""<text id="linkText" x="115" y="20" class="glass">""")
        var linkClass = "linkText"
        buttons.theme?.let {
            if(it.useDark) {
                linkClass = "linkTextDark"
            }
        }
        links?.let {
            it.forEach { link ->
                var linkElement = """<a xlink:href="${link.href}" href="${link.href}" class="$linkClass" style="$style" target="_blank">${link.label.escapeXml()}</a>"""
                if(isPdf) {
                    linkElement = link.label.escapeXml()
                }
                sb.append("""
            <tspan x="115" dy="14" style="$style" class="glass">
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
