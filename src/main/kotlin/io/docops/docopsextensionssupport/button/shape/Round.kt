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
        buttons.theme?.let {
            if (it.newWin) {
                win = "_blank"
            }
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
            val lines = wrapText(button.label, 15f)
            var lineY = 0
            if(lines.isNotEmpty()) {
                lineY = lines.size * - 6
            }
            val title = linesToMultiLineText(button.buttonStyle?.labelStyle,
                lines, 12, 0)

            val accentColor = if (buttons.useDark) "#3b82f6" else "#1e293b"

            var href = """onclick="window.open('${button.link}', '$win')" style="cursor: pointer;""""
            if(!button.enabled) {
                href = ""
            }
            btns.append("""
                <g transform="translate($startX,$startY)" $href class="orb-group">
                    <!-- STATIC LAYER: The Glow Ring stays behind -->
                    <circle r="62" cx="0" cy="0" fill="none" stroke="${button.color}" stroke-width="2" class="glow-ring" filter="url(#neonGlow)"/>
                    
                    <g class="moving-group">
                        <!-- SHIFTING LAYER: The Shadow -->
                        <circle r="55" cx="0" cy="0" fill="black" opacity="0.4" filter="url(#buttonShadow)"/>
                        
                        <!-- SHIFTING LAYER: The Orb -->
                        <circle r="55" cx="0" cy="0" fill="url(#raisedButton_${button.id})" 
                                stroke="${button.color}" 
                                stroke-width="1.5"
                        />
                        <!-- Glass Shine Reflection (Hidden for PDF) -->
                        ${if(!isPdf) """<circle r="50" cx="0" cy="-2" fill="url(#glassReflection)"/>""" else ""}
                        
                        <!-- Top-Left Specular Highlight Dot -->
                        <circle r="4" cx="-18" cy="-18" fill="white" fill-opacity="0.6"/>
                    </g>
                    
                    <!-- Content -->
                    <g class="moving-group">
                        <text x="0" y="$lineY" text-anchor="middle" class="orb-text" style="fill: ${if(buttons.useDark) "#ffffff" else "#1e293b"}">
                            $title
                        </text>
                    </g>
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
                .orb-group { cursor: pointer; }
                
                .orb-group .moving-group {
                    transition: transform 0.4s cubic-bezier(0.22, 1, 0.36, 1);
                }
                
                

                .glow-ring {
                    transition: stroke-dashoffset 0.6s ease, opacity 0.4s ease;
                    stroke-dasharray: 400;
                    stroke-dashoffset: ${if(isPdf) "0" else "400"};
                    opacity: ${if(isPdf) "1" else "0.2"};
                }
                
                .orb-group:hover .glow-ring {
                    stroke-dashoffset: 0;
                    opacity: 1;
                }

                .orb-text {
                    font-family: 'JetBrains Mono', monospace;
                    font-weight: 800;
                    font-size: 12px;
                    text-transform: uppercase;
                    letter-spacing: 0.05em;
                    pointer-events: none;
                }
                
                </style>
            """.trimIndent()

        if(isPdf) {
            style = ".orb-text { font-family: 'JetBrains Mono', monospace; font-weight: 800; font-size: 12px; text-transform: uppercase; }"
        }

        return """
                <defs>
                    <!-- Background Atmosphere Pattern -->
                    <pattern id="dotPattern_${buttons.id}" x="0" y="0" width="30" height="30" patternUnits="userSpaceOnUse">
                        <circle cx="2" cy="2" r="1" fill="${if(buttons.useDark) "#3b82f6" else "#cbd5e1"}" fill-opacity="0.15" />
                    </pattern>

                    <filter id="neonGlow" x="-50%" y="-50%" width="200%" height="200%">
                        <feGaussianBlur stdDeviation="4" result="blur" />
                        <feComposite in="SourceGraphic" in2="blur" operator="over" />
                    </filter>

                    <!-- Glass Reflection Gradient -->
                    <linearGradient id="glassReflection" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" stop-color="white" stop-opacity="0.4"/>
                        <stop offset="50%" stop-color="white" stop-opacity="0.05"/>
                        <stop offset="100%" stop-color="white" stop-opacity="0"/>
                    </linearGradient>

                    <filter id="buttonShadow" x="-50%" y="-50%" width="200%" height="200%">
                        <feDropShadow dx="5" dy="8" stdDeviation="6" flood-color="#000000" flood-opacity="0.5"/>
                    </filter>
        
                    $linGrad
                    $style
                </defs>
                <!-- Apply atmospheric pattern background -->
                <rect width="${width()}" height="${height()}" fill="url(#dotPattern_${buttons.id})" rx="12" pointer-events="none"/>
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