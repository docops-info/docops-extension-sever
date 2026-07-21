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
import io.docops.docopsextensionssupport.support.determineTextColor
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
    override fun createShape(type: String): String {
        val width = width()
        val height = height()
        val sb = StringBuilder()
        sb.append(start(width, height))
        sb.append(standardDefs())
        sb.append(shapeDefs())
        sb.append(makeModernBackground(width, height))
        sb.append(draw())
        sb.append(end())
        return sb.toString()
    }

    override fun draw() : String{
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        val id = "btn-${buttons.id}"
        val sb = StringBuilder("<g id=\"$id\" transform=\"scale($scale)\">")
        val rows = toRows()
        var staggerIdx = 0
        rows.forEachIndexed { index, buttons ->
            sb.append(drawButton(index, buttons, staggerIdx))
            staggerIdx += buttons.size
        }
        sb.append("</g>")
        return sb.toString()
    }

    override fun drawButton(index: Int, buttonList: MutableList<Button>, rowStartStagger: Int): String {
        val btns = StringBuilder()
        var win = "_top"
        buttons.theme?.let {
            if (it.newWin) {
                win = "_blank"
            }
        }
        var startX = 80

        var startY = 80
        if (index > 0) {
            startY = index * 140 + 80
        }

        buttonList.forEachIndexed { i, button: Button ->
            val delay = (rowStartStagger + i) * 0.05
            val lines = wrapText(button.label, 15f)
            var lineY = 0
            if(lines.isNotEmpty()) {
                lineY = lines.size * - 6
            }
            val baseColor = button.color ?: docOpsTheme.accentColor
            val textColor = determineTextColor(baseColor)
            val labelStyle = button.buttonStyle?.labelStyle?.replace(Regex("fill\\s*:\\s*[^;]+;?"), "")

            val title = linesToMultiLineText(labelStyle,
                lines, 12, 0)

            val accentColor = button.color ?: themeColor("--accent")

            var href = """onclick="window.open('${button.link}', '$win')" """
            if(!button.enabled) {
                href = ""
            }
            btns.append("""
                <g transform="translate($startX,$startY)">
                    <g class="button-stagger" style="animation-delay: ${delay}s">
                    <g role="button" tabindex="0" class="button-hover orb-group" $href>
                        <!-- STATIC LAYER: Glow Ring -->
                        <circle r="62" cx="0" cy="0" fill="none" stroke="$accentColor" stroke-width="2" class="glow-ring" stroke-opacity="0.3"/>
                    
                        <g class="moving-group">
                        <!-- SHIFTING LAYER: The Shadow -->
                        <circle r="55" cx="0" cy="0" fill="black" opacity="0.15" filter="url(#cardShadow_${buttons.id})"/>
                        
                        <!-- SHIFTING LAYER: The Orb -->
                        <circle r="55" cx="0" cy="0" fill="url(#raisedButton_${button.id})" 
                                stroke="$accentColor" 
                                stroke-width="1.5"
                                stroke-opacity="0.2"
                        />
                        <!-- Glass Shine Reflection -->
                        ${if(!isPdf) """<circle r="50" cx="0" cy="-2" fill="url(#glassReflection)" pointer-events="none"/>""" else ""}
                        
                        <!-- Top-Left Specular Highlight Dot -->
                        <circle r="4" cx="-18" cy="-18" fill="white" fill-opacity="0.4" pointer-events="none"/>
                    </g>
                    
                    <!-- Content -->
                    <g class="moving-group">
                            <text x="0" y="$lineY" text-anchor="middle" class="orb-text" fill="$textColor" style="font-weight: 700;">
                                $title
                            </text>
                        </g>
                    </g>
                    </g>
                </g>
            """.trimIndent())

            startX += 140
        }
        return btns.toString()
    }

    override fun height(): Float {
        val size = toRows().size
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        return (size * 140.0f + 20.0f) * scale
    }

    override fun width(): Float {
        var cols = 3
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
            cols = it.columns
        }
        return (cols * 140.0f + 20.0f) * scale
    }

    protected fun shapeDefs() : String {
        val id = "btn-${buttons.id}"
        val linGrad = StringBuilder()
        buttons.buttons.forEach { b ->
            linGrad.append(createRaisedButtonGradient(b))
        }
        val style = """
                [id='$id'] .moving-group {
                    transition: transform 0.4s cubic-bezier(0.22, 1, 0.36, 1);
                }

                [id='$id'] .glow-ring {
                    stroke-dasharray: 400;
                    stroke-dashoffset: ${if(isPdf) "0" else "400"};
                    opacity: ${if(isPdf) "1" else "0.3"};
                    transition: stroke-dashoffset 0.6s ease, opacity 0.4s ease;
                }
                
                [id='$id'] .button-hover:hover .glow-ring {
                    stroke-dashoffset: 0;
                    opacity: 0.8;
                }

                [id='$id'] .orb-text {
                    font-weight: 800;
                    font-size: 12px;
                    text-transform: uppercase;
                    letter-spacing: 0.05em;
                    pointer-events: none;
                }
        """.trimIndent()

        return """
            <style>
                $style
            </style>
            <!-- Glass Reflection Gradient -->
            <linearGradient id="glassReflection" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" stop-color="white" stop-opacity="0.4"/>
                <stop offset="50%" stop-color="white" stop-opacity="0.05"/>
                <stop offset="100%" stop-color="white" stop-opacity="0"/>
            </linearGradient>
            $linGrad
        """.trimIndent()
    }
    // In the Round class, modify the gradient definition method
    private fun createRaisedButtonGradient(button: Button): String {
        val baseColor = button.color
        val darkerColor = darkenColor(baseColor!!, 0.4)

        return """
        <radialGradient id="raisedButton_${button.id}" cx="35%" cy="30%" r="65%">
            <stop offset="0%" style="stop-color:${baseColor};stop-opacity:1" />
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