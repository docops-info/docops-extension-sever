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
import io.docops.docopsextensionssupport.support.determineTextColor
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

    /**
     * Draws a button on the screen with the given index and list of buttons.
     *
     * @param index the index of the button
     * @param buttonList the list of buttons
     * @return a string representing the SVG code for the button
     */
    override fun drawButton(index: Int, buttonList: MutableList<Button>, rowStartStagger: Int): String {
        val btns = StringBuilder()
        var win = "_top"
        buttons.theme?.let {
            if (it.newWin) {
                win = "_blank"
            }
        }
        var startX = 20

        var startY = 20
        if (index > 0) {
            startY = index * BUTTON_HEIGHT + (index * BUTTON_PADDING) + 20
        }

        buttonList.forEachIndexed { i, button ->
            val delay = (rowStartStagger + i) * 0.05
            var lines = ""
            val baseColor = button.color ?: docOpsTheme.accentColor
            val accentColor = baseColor
            val textColor = determineTextColor(baseColor)

            val labelStyle = button.buttonStyle?.labelStyle?.replace(Regex("fill\\s*:\\s*[^;]+;?"), "")
            val descStyle = button.buttonStyle?.descriptionStyle?.replace(Regex("fill\\s*:\\s*[^;]+;?"), "")

            button.description?.let {
                lines += """<text class="card-text title-text" x="75" y="75" text-anchor="middle" fill="$textColor">"""
                lines += linesToMultiLineText(descStyle, wrapText(it.escapeXml(), 30f), 10, 75)
                lines+= "</text>"
            }
            var linesOrImage = lines
            if(lines.trim().isEmpty()) {
                button.embeddedImage?.let {
                    linesOrImage= """
            <image x="0" y="35" width="150" height="90" href="${it.ref}"/>"""
                }
            }
            val title = linesToMultiLineText(labelStyle, wrapText(button.label.escapeXml(), 30f), 12, 75)
            var btnDate = ""
            button.date?.let {
                btnDate = it
            }
            var authors = ""
            button.author?.let {
                authors = authorsToTSpans(it, "145", button.buttonStyle?.authorStyle)
            }
            var fill = "class=\"btn_${button.id}_cls\""
            var clz = "glass"
            if(isPdf) {
                clz = ""
            }
            var href = """window.open('${button.link}', '$win')"""
            if(!button.enabled) {
                href = ""

            }

            btns.append("""
             <g transform="translate($startX,$startY)">
                <g class="button-stagger" style="animation-delay: ${delay}s">
                <g class="button-hover" onclick="$href" role="button" tabindex="0">
                <!-- Fixed Shadow Layer -->
                <rect x="2" y="2" width="150" height="150" rx="4" fill="var(--surface)" filter="url(#cardShadow_${buttons.id})"/>
            
                <!-- Shifting Body Group -->
                <g class="moving-group">
                    <!-- Card background -->
                    <rect class="card-bg" x="0" y="0" width="150" height="150" rx="4" fill="url(#btn_${button.id})"/>
                
                    <!-- Sharp Accent Border -->
                    <rect class="accent-border" x="0" y="0" width="150" height="150" rx="4" 
                          fill="none" 
                          stroke="$accentColor" 
                          stroke-width="2.5" 
                          stroke-linecap="square"/>
                
                    <!-- Sharp Corner Accent -->
                    <path class="corner-accent" d="M 0 25 L 0 0 L 25 0" fill="none" 
                          stroke="$accentColor" 
                          stroke-width="3"/>
        
                    <!-- Content -->
                    <text class="card-text title-text" x="75" y="55" text-anchor="middle" fill="$textColor">$title</text>
                    $lines
                    <text class="card-text date-text" x="75" y="135" text-anchor="middle" fill="$textColor" opacity="0.8">${btnDate}</text>
                </g>
                </g>
                </g>
             </g>
            """.trimIndent())

            startX += BUTTON_WIDTH + BUTTON_PADDING
        }
        return btns.toString()
    }

    override fun height(): Float {
        val rows = toRows()
        val rowCount = rows.size
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        val h = 20 + rowCount * BUTTON_HEIGHT + (rowCount) * BUTTON_PADDING + 10
        return h * scale
    }

    override fun width(): Float {
        val rows = toRows()
        val maxInRow = if(rows.isEmpty()) 0 else rows.maxOf { it.size }
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        val w = 20 + maxInRow * BUTTON_WIDTH + (maxInRow) * BUTTON_PADDING + 10
        return w * scale
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

    protected fun shapeDefs(): String {
        val id = buttons.id
        val accent = docOpsTheme.accentColor
        
        val sb = StringBuilder()
        buttons.buttons.forEach {
            val svgColor = SVGColor(it.color ?: accent, "btn_${it.id}")
            sb.append("""
                <linearGradient id="btn_${it.id}" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="${svgColor.darker()}"/>
                    <stop offset="100%" stop-color="${svgColor.lighter()}"/>
                </linearGradient>""")
        }
        
        val style = """
                [id='btn-$id'] .moving-group {
                    transition: transform 0.4s cubic-bezier(0.22, 1, 0.36, 1);
                }

                [id='btn-$id'] .button-hover:hover .moving-group {
                    transform: translate(-1px, -1px);
                }

                [id='btn-$id'] .accent-border {
                    stroke-dasharray: 600;
                    stroke-dashoffset: ${if(isPdf) "0" else "600"};
                    transition: stroke-dashoffset 0.6s ease;
                }

                [id='btn-$id'] .button-hover:hover .accent-border {
                    stroke-dashoffset: 0;
                }

                [id='btn-$id'] .card-text {
                    font-family: 'Lexend', sans-serif;
                }

                [id='btn-$id'] .title-text {
                    font-weight: 900;
                    font-size: 13px;
                    text-transform: uppercase;
                    letter-spacing: 0.15em;
                }

                [id='btn-$id'] .desc-text {
                    font-family: 'JetBrains Mono', monospace;
                    font-weight: 400;
                    font-size: 10px;
                    fill: rgba(255,255,255,0.7);
                }

                [id='btn-$id'] .date-text {
                    font-family: 'JetBrains Mono', monospace;
                    font-weight: 700;
                    font-size: 11px;
                }
        """.trimIndent()
        
        return """
            <style>
                $style
            </style>
            $sb
        """.trimIndent()
    }

    companion object {
        const val BUTTON_HEIGHT = 150
        const val BUTTON_WIDTH = 150
        const val BUTTON_PADDING = 10
    }
}
