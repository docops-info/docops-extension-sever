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
            button.description?.let {
                lines += "<text x=\"0\" y=\"38\" >"
                lines += linesToMultiLineText(button.buttonStyle?.descriptionStyle, wrapText(it.escapeXml(), 30f), 10, 4)
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
            var fill = "class=\"btn_${button.id}_cls\""
            var overlay = "url(#overlayGrad)"
            var clz = "glass"
            if(isPdf) {
                fill = "fill='${button.color}'"
                overlay = "${button.color}"
                clz = ""
            }
            var href = """window.open('${button.link}', '$win')"""
            var endAnchor = "</a>"
            if(!button.enabled) {
                href = ""
                endAnchor = ""
            }
            btns.append("""
         <g transform="translate($startX,$startY)" class="slim-card" cursor="pointer" onclick="$href">

               
        <!-- Faint thin border -->
        <rect x="0" y="0" width="150" height="150" rx="17.5" ry="17.5"
              fill="none" stroke="rgba(255,255,255,0.08)" stroke-width="1"
              $fill/>

        <!-- Inner shadow to add depth -->
        <rect x="0" y="0" width="150" height="150" rx="18" ry="18"
              fill="transparent" filter="url(#inner-shadow)"/>


        <!-- Subtle noise overlay for organic texture -->
        <rect x="0" y="0" width="150" height="150" rx="18" ry="18" fill="url(#noise)" opacity="0.04" clip-path="url(#rect-clip)"/>

        <!-- Slight colored tint (like iOS accent) -->
        <rect x="0" y="0" width="150" height="150" rx="18" ry="18"
              fill="rgba(100,140,255,0.06)" style="mix-blend-mode:overlay">
            <title>Netflix</title>
        </rect>
        
        <text text-anchor="middle" x="75" y="8" class="$clz">
            $title
        </text>
        $linesOrImage
        <text x="145" y="135" text-anchor="end">${authors}</text>
        <text x="145" y="145" style="${button.buttonStyle?.dateStyle}" text-anchor="end">${btnDate}</text>
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
            #${buttons.id} .dark-mode {
                filter: brightness(0.8) contrast(1.2);
            }
            #${buttons.id} .dark-text {
                fill: #e5e7eb !important;
                text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.8);
            }
            #${buttons.id} .dark-shadow {
                text-shadow: 1px 1px 2px rgba(255, 255, 255, 0.2);
            }
            #${buttons.id} .slim-card.dark-mode:hover {
                filter: brightness(1.2) sepia(30%);
            }
            """
        } else {
            ""
        }

        val darkModeDefs = if (buttons.useDark) {
            """
                <linearGradient id="glassBorder_${buttons.id}" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" style="stop-color:rgba(255,255,255,0.3);stop-opacity:1"/>
            <stop offset="50%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1"/>
            <stop offset="100%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1"/>
        </linearGradient>
        <filter id="glassDropShadow_${buttons.id}" x="-20%" y="-20%" width="140%" height="140%">
            <feGaussianBlur in="SourceAlpha" stdDeviation="8" result="blur"/>
            <feOffset in="blur" dx="0" dy="8" result="offsetBlur"/>
            <feFlood flood-color="rgba(0,0,0,0.15)" result="shadowColor"/>
            <feComposite in="shadowColor" in2="offsetBlur" operator="in" result="shadow"/>
            <feMerge>
                <feMergeNode in="shadow"/>
                <feMergeNode in="SourceGraphic"/>
            </feMerge>
        </filter>
        <linearGradient id="glassOverlay_${buttons.id}" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" style="stop-color:rgba(255,255,255,0.25);stop-opacity:1"/>
            <stop offset="30%" style="stop-color:rgba(255,255,255,0.15);stop-opacity:1"/>
            <stop offset="70%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1"/>
            <stop offset="100%" style="stop-color:rgba(255,255,255,0.02);stop-opacity:1"/>
        </linearGradient>
        <linearGradient id="backgroundGradient_${buttons.id}" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:#1a1a2e;stop-opacity:1"/>
            <stop offset="100%" style="stop-color:#16213e;stop-opacity:1"/>
        </linearGradient>
            """.trimIndent()
        } else {""}

        var style = """
             <style>
            ${glass()}
            ${lightShadow()}
            ${raise(strokeColor = strokeColor)}
            ${baseCard()}
            ${gradientStyle()}
            ${myBox()}
            ${keyFrame()}
            ${linkText()}
            ${modernText()}
            ${modernCard()}
            $darkModeStyles
            
            /* Slim-specific hover effects */
            #${buttons.id} .slim-card {
                transition: all 0.3s ease;
                cursor: pointer;
            }
            
            #${buttons.id} .slim-card:hover {
                filter: brightness(1.1) saturate(1.2);
                box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
            }
            
            #${buttons.id} .slim-card:hover rect {
                stroke-width: 2;
                stroke: rgba(255, 255, 255, 0.3);
            }
            
            #${buttons.id} .slim-card:hover text {
                filter: brightness(1.2);
            }
            
            /* Enhanced glass effect on hover */
            #${buttons.id} .glass:hover {
                text-shadow: 0 0 10px rgba(255, 255, 255, 0.8);
            }
            
            </style>
        """.trimIndent()

        if(isPdf) {
            style = """

            """.trimIndent()
        }

        return """
            <defs>
            $darkModeDefs
            ${filters()}
            ${naturalShadow()}
            ${gradient()}
            ${modernGradients()}
            ${uses()}
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
