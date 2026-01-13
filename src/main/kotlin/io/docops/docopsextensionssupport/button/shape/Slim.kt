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
import io.docops.docopsextensionssupport.roadmap.wrapText
import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.util.BackgroundHelper
import kotlin.compareTo
import kotlin.times

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
            val accentColor = button.color ?: docOpsTheme.accentColor

            button.description?.let {
                lines += """<text class="card-text title-text" x="75" y="75" text-anchor="middle">"""
                lines += linesToMultiLineText(button.buttonStyle?.descriptionStyle, wrapText(it.escapeXml(), 30f), 10, 75)
                lines+= "</text>"
            }
            var linesOrImage = lines
            if(lines.trim().isEmpty()) {
                button.embeddedImage?.let {
                    linesOrImage= """
            <image x="0" y="35" width="150" height="90" href="${it.ref}"/>"""
                }
            }
            val title = linesToMultiLineText(button.buttonStyle?.labelStyle,wrapText(button.label.escapeXml(), 30f), 12, 75)
            var btnDate = ""
            button.date?.let {
                btnDate = it
            }
            var authors = ""
            button.author?.let {
                authors = authorsToTSpans(it, "145", button.buttonStyle?.authorStyle)
            }
            var fill = "class=\"btn_${button.id}_cls\""
            var overlay = "url(#overlayGrad)"
            var clz = "glass"
            if(isPdf) {
                fill = "fill='${button.color}'"
                overlay = "${button.color}"
                clz = ""
            }
            var href = """window.open('${button.link}', '$win')"""
            if(!button.enabled) {
                href = ""

            }

            btns.append("""
             <g transform="translate($startX,$startY)" class="card-group" onclick="$href" style="cursor: pointer;">
                <!-- Fixed Shadow Layer -->
                <rect x="2" y="2" width="150" height="150" rx="${docOpsTheme.cornerRadius / 3}" fill="${docOpsTheme.surfaceImpact}" filter="url(#cardShadow)"/>
            
                <!-- Shifting Body Group -->
                <g class="moving-group">
                    <!-- Card background -->
                    <rect class="card-bg" x="0" y="0" width="150" height="150" rx="${docOpsTheme.cornerRadius / 3}" fill="url(#btn_${button.id})"/>
                
                    <!-- Sharp Accent Border -->
                    <rect class="accent-border" x="0" y="0" width="150" height="150" rx="${docOpsTheme.cornerRadius / 3}" 
                          fill="none" 
                          stroke="$accentColor" 
                          stroke-width="2.5" 
                          stroke-linecap="square"/>
                
                    <!-- Sharp Corner Accent -->
                    <path class="corner-accent" d="M 0 25 L 0 0 L 25 0" fill="none" 
                          stroke="$accentColor" 
                          stroke-width="3"/>
        
                    <!-- Content -->
                    <text class="card-text title-text" x="75" y="55" text-anchor="middle">$title</text>
                    $lines
                    <text class="card-text date-text" x="75" y="135" text-anchor="middle" fill="$accentColor">${btnDate}</text>
                </g>
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
        // Add padding on left and right, plus padding between buttons
        // Left padding + (columns * button width) + ((columns - 1) * padding between buttons) + right padding
        return (BUTTON_PADDING + columns * BUTTON_WIDTH + (columns - 1) * BUTTON_PADDING + BUTTON_PADDING) * scale
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

    override fun defs(): String {
        var strokeColor: String = "gold"
        buttons.theme?.let {
            strokeColor = it.strokeColor
        }

        // Dark mode styles
        val darkModeStyles = if (buttons.useDark) {
            """
            #btn_${buttons.id} .dark-mode {
                filter: brightness(0.8) contrast(1.2);
            }
            #btn_${buttons.id} .dark-text {
                fill: #e5e7eb !important;
                text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.8);
            }
            #btn_${buttons.id} .dark-shadow {
                text-shadow: 1px 1px 2px rgba(255, 255, 255, 0.2);
            }
            #btn_${buttons.id} .slim-card.dark-mode:hover {
                filter: brightness(1.2) sepia(30%);
            }
            """
        } else {
            ""
        }

        val darkModeDefs = BackgroundHelper.getBackgroundGradient(useDark = buttons.useDark, buttons.id)

        var style = """
                 <style>
                $darkModeStyles
            
                #btn_${buttons.id} .card-group {
                    cursor: pointer;
                }

                #btn_${buttons.id} .moving-group {
                    transition: transform 0.4s cubic-bezier(0.22, 1, 0.36, 1);
                }

                #btn_${buttons.id} .card-group:hover .moving-group {
                    transform: translate(-1px, -1px);
                }

                #btn_${buttons.id} .accent-border {
                    stroke-dasharray: 600;
                    stroke-dashoffset: ${if(isPdf) "0" else "600"};
                    transition: stroke-dashoffset 0.6s ease;
                }

                #btn_${buttons.id} .card-group:hover .accent-border {
                    stroke-dashoffset: 0;
                }

                #btn_${buttons.id} .card-text {
                    font-family: 'Inter', 'JetBrains Mono', monospace, sans-serif;
                }

               
                #btn_${buttons.id} .title-text {
                    font-weight: 900;
                    font-size: 13px;
                    fill: #ffffff;
                    text-transform: uppercase;
                    letter-spacing: 0.15em;
                }

                #btn_${buttons.id} .desc-text {
                    font-family: 'JetBrains Mono', monospace;
                    font-weight: 400;
                    font-size: 10px;
                    fill: rgba(255,255,255,0.7);
                }

                #btn_${buttons.id} .date-text {
                    font-family: 'JetBrains Mono', monospace;
                    font-weight: 700;
                    font-size: 11px;
                    fill: ${if (buttons.useDark) "#60a5fa" else "#ffffff"};
                }
                </style>
            """.trimIndent()

        if(isPdf) {
            style = """

            """.trimIndent()
        }
        val sb = StringBuilder()
        buttons.buttons.forEach {
            val svgColor = SVGColor(it.color!!, "btn_${it.id}")
            sb.append("""
                <linearGradient id="btn_${it.id}" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="${svgColor.darker()}"/>
                    <stop offset="100%" stop-color="${svgColor.lighter()}"/>
                </linearGradient>""")
        }
        return """
            <defs>
            $darkModeDefs
          
                <!-- Atmospheric Pattern -->
                <pattern id="dotPattern" x="0" y="0" width="20" height="20" patternUnits="userSpaceOnUse">
                    <circle cx="2" cy="2" r="1" fill="${if(buttons.useDark) "#ffffff" else "#000000"}" fill-opacity="0.05" />
                </pattern>
                
            <!-- Modern shadow filter -->
            <filter id="cardShadow" x="-50%" y="-50%" width="200%" height="200%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="8"/>
                <feOffset dx="0" dy="8" result="offsetblur"/>
                <feComponentTransfer>
                    <feFuncA type="linear" slope="0.3"/>
                </feComponentTransfer>
                <feMerge>
                    <feMergeNode/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>
            $sb
           $style
            </defs>
        """.trimIndent()
    }

    companion object {
        const val BUTTON_HEIGHT = 150
        const val BUTTON_WIDTH = 150
        const val BUTTON_PADDING = 10
    }
}
