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
import io.docops.docopsextensionssupport.roadmap.wrapText
import io.docops.docopsextensionssupport.support.SVGColor
import java.awt.Color


/**
 * The `Round` class is a subclass of the `Regular` class and represents a round button layout.
 * It provides the functionality to draw round buttons based on the given data.
 *
 * @property buttons The `Buttons` object containing the button data.
 */
class Round(buttons: Buttons) : Regular(buttons) {
    class ItemCount(var counter: Int) {
        fun inc() {
            counter++
        }
    }
    override fun draw() : String{
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        val sb = StringBuilder("<g transform=\"scale($scale)\">")
        val rows = toRows()
        val itemNumber = ItemCount(0)
        rows.forEachIndexed { index, buttons ->
            sb.append(drawButtons(index, buttons, itemNumber))
        }
        sb.append("</g>")
        if(buttons.useDark) {
            val rect = StringBuilder("""
                $sb
            """.trimIndent())
            return rect.toString()
        } else {
            return sb.toString()
        }
    }
    /**
     * Draws a series of buttons based on the given index and list of buttons.
     *
     * @param index The starting index.
     * @param buttonList The list of buttons.
     * @return The HTML code representing the buttons.
     */
     fun drawButtons(index: Int, buttonList: MutableList<Button>, itemNumber: ItemCount): String {
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
            if(index == 1) {
                startY = index * BUTTON_HEIGHT + BUTTON_SPACING + 60
            } else {
                startY = index * BUTTON_HEIGHT + BUTTON_SPACING + 60 + (index * 5)
            }
        }

        buttonList.forEach {button: Button ->
            val lines = wrapText(button.label.escapeXml(), 15f)
            var lineY = 0
            if(lines.size > 0) {
                lineY = lines.size * - 6
            }
            val title = linesToMultiLineText(button.buttonStyle?.labelStyle,
                lines, 12, 0)
            var filter = "filter=\"url(#Bevel2)\""
            var fill = "${button.color}"
            var stroke = "url(#nnneon-grad${itemNumber.counter}-${buttons.id})"
            if(isPdf) {
                filter = ""
                stroke = "${button.color}"
                fill = "url(#btn_${button.id})"
            }
            var href = """<a xlink:href="${button.link}" target="$win">"""
            var endAnchor = "</a>"
            if(!button.enabled) {
                href = ""
                endAnchor = ""
            }
            btns.append("""
            <g transform="translate($startX,$startY)" cursor="pointer">
            $href
            <g stroke-width="16" stroke="$stroke" fill="#fcfcfc" cursor="pointer" class="raise bar">
                <title class="description">${button.description?.escapeXml()}</title>
                <circle r="55" cx="0" cy="0" filter="url(#nnneon-filter2)" opacity="0.25"/>
                <circle r="55" cx="0" cy="0" fill="url(#raisedButton_${button.id})" 
                        filter="url(#buttonShadow)"
                        stroke="${darkenColor(button.color!!, 0.4)}" 
                        stroke-width="1"
                />
            </g>
            <text  x="0" y="$lineY" text-anchor="middle" style="fill: ${button.color}">
                $title
            </text>
            $endAnchor
            </g>
            """.trimIndent())

            startX += BUTTON_WIDTH + BUTTON_PADDING + 5
            itemNumber.inc()

        }
        return btns.toString()
    }

    override fun height(): Float {
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        val size = toRows().size
        return (((size * 125) + (size * 5)) + size * 5) * scale
    }

    override fun width(): Float {
        var cols = 3
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
            cols = it.columns
        }
        return (((cols * 125)+ (cols * 5)) + (cols * 7)) * scale
    }
    companion object {
        const val BUTTON_HEIGHT: Int = 125
        const val BUTTON_WIDTH = 125
        const val BUTTON_PADDING = 0
    }
    override fun defs() : String {
        var strokeColor = "gold"
        buttons.theme?.let {
            strokeColor = it.strokeColor
        }
        val linGrad = StringBuilder()
        buttons.buttons.forEachIndexed {
            i, b ->
            val grad = SVGColor(b.color!!, "nnneon-grad$i-${buttons.id}")
            val gradientDef = createRaisedButtonGradient(b)
            linGrad.append(gradientDef)
            linGrad.append(grad.linearGradient)
        }
        var style = """
             <style>
            .raise {
                pointer-events: bounding-box;
                opacity: 1;
                filter: drop-shadow(3px 5px 2px rgb(0 0 0 / 0.4));
            }

            .raise:hover {
                stroke: gold;
                stroke-width: 3px;
                opacity: 0.9;
            }
            .bar:hover {
                filter: grayscale(100%) sepia(100%);
            }
            </style>
        """.trimIndent()
        if(isPdf) {
            style = ""
        }
        return """
            <defs>
                <filter id="nnneon-filter" x="-100%" y="-100%" width="400%" height="400%" filterUnits="objectBoundingBox" primitiveUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feGaussianBlur stdDeviation="17 8" x="0%" y="0%" width="100%" height="100%" in="SourceGraphic" result="blur"/>
                </filter>
                <filter id="nnneon-filter2" x="-100%" y="-100%" width="400%" height="400%" filterUnits="objectBoundingBox" primitiveUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                   
                </filter>
                <filter id="buttonShadow" x="-50%" y="-50%" width="200%" height="200%">
                    <feGaussianBlur in="SourceAlpha" stdDeviation="2"/>
                    <feOffset dx="1" dy="3" result="offset"/>
                    <feFlood flood-color="#000000" flood-opacity="0.2"/>
                    <feComposite in2="offset" operator="in" result="shadow"/>
                    <feGaussianBlur in="SourceAlpha" stdDeviation="1"/>
                    <feOffset dx="0" dy="1" result="innerOffset"/>
                    <feFlood flood-color="#000000" flood-opacity="0.1"/>
                    <feComposite in2="innerOffset" operator="in" result="innerShadow"/>
        
                    <feMerge>
                        <feMergeNode in="shadow"/>
                        <feMergeNode in="SourceGraphic"/>
                        <feMergeNode in="innerShadow"/>
                    </feMerge>
                </filter>
        
           
                $linGrad
           $style
            </defs>
        """.trimIndent()
    }
    // In the Round class, modify the gradient definition method
    private fun createRaisedButtonGradient(button: Button): String {
        val baseColor = button.color
        val darkerColor = darkenColor(baseColor!!, 0.3) // Create a darker shade
        val lighterColor = lightenColor(baseColor, 0.2) // Create a lighter shade

        return """
    <radialGradient id="raisedButton_${button.id}" cx="30%" cy="25%" r="80%">
        <stop offset="0%" style="stop-color:#ffffff;stop-opacity:0.4" />
        <stop offset="40%" style="stop-color:${baseColor};stop-opacity:1" />
        <stop offset="100%" style="stop-color:${darkerColor};stop-opacity:1" />
    </radialGradient>
    """
    }

    // Helper functions to create color variations
    private fun darkenColor(hexColor: String, factor: Double): String {
        val color = Color.decode(hexColor)
        val r = (color.red * (1 - factor)).toInt().coerceAtLeast(0)
        val g = (color.green * (1 - factor)).toInt().coerceAtLeast(0)
        val b = (color.blue * (1 - factor)).toInt().coerceAtLeast(0)
        return String.format("#%02x%02x%02x", r, g, b)
    }

    private fun lightenColor(hexColor: String, factor: Double): String {
        val color = Color.decode(hexColor)
        val r = (color.red + (255 - color.red) * factor).toInt().coerceAtMost(255)
        val g = (color.green + (255 - color.green) * factor).toInt().coerceAtMost(255)
        val b = (color.blue + (255 - color.blue) * factor).toInt().coerceAtMost(255)
        return String.format("#%02x%02x%02x", r, g, b)
    }

}