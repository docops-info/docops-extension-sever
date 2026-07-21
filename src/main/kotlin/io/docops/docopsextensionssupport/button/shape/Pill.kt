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
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.support.determineTextColor

/**
 * Implements a pill-shaped button with enhanced visual effects and rounded ends.
 *
 * The Pill class extends the [Regular] class to create buttons with a distinctive pill shape
 * characterized by fully rounded ends and a taller profile. These buttons feature multiple
 * visual effects including gradients, blurs, and highlights to create a glossy, three-dimensional
 * appearance.
 *
 * Key features:
 * - Pill shape with fully rounded ends (26px radius)
 * - Taller profile (56px height) compared to Regular buttons
 * - Multiple layered visual effects:
 *   - Base button with gradient fill
 *   - Blur filter for soft edges
 *   - Top shine gradient for highlight
 *   - Bottom shine for depth
 * - Centered text label
 * - Smooth layout with appropriate spacing
 *
 * This shape is particularly useful for:
 * - Primary action buttons that need to stand out
 * - Interfaces with a modern, glossy aesthetic
 * - Buttons that need to appear more tactile and pressable
 * - Designs where rounded, friendly shapes are preferred
 *
 * The Pill shape maintains the same row-based layout as Regular buttons
 * but with enhanced visual styling that gives a more polished appearance.
 *
 * @param buttons The Buttons collection to be rendered
 */
class Pill(buttons: Buttons) : Regular(buttons) {


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

    override fun draw(): String {
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

        var startX = 20
        var startY = 20
        if (index > 0) {
            startY = index * BUTTON_HEIGHT + (index * BUTTON_PADDING) + 20
        }
        buttonList.forEachIndexed { i, button: Button ->
            val delay = (rowStartStagger + i) * 0.05
            val baseColor = button.color ?: docOpsTheme.accentColor
            val textColor = determineTextColor(baseColor)
            val labelStyle = button.buttonStyle?.labelStyle?.replace(Regex("fill\\s*:\\s*[^;]+;?"), "")
            var href = """onclick="window.open('${button.link}', '$win')" """
            if(!button.enabled) {
                href = ""
            }

            btns.append(
                """
                <g transform="translate($startX, $startY)">
                    <g class="button-stagger" style="animation-delay: ${delay}s">
                    <g role="button" tabindex="0" class="button-hover" $href>
                    <rect id="button" x="0" y="0" width="$BUTTON_WIDTH" height="$BUTTON_HEIGHT" ry="26" rx="26" fill="url(#btn_${button.id})" filter="url(#cardShadow_${buttons.id})" />
                    <rect id="buttontop" x="10" y="5" width="280" height="25" ry="24" rx="24" fill="url(#topshineGrad)" fill-opacity="0.15"/>
                    <rect id="buttonbottom" x="20" y="44" width="260" height="7" fill="#ffffff" ry="24" rx="24" fill-opacity="0.1"/>
                    <text id="label" x="150" y="33" text-anchor="middle" fill="$textColor" style="font-weight: 700; $labelStyle">${button.label.escapeXml()}</text>
                    </g>
                    </g>
                </g>
                """
            )

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
            return (size * BUTTON_HEIGHT + (size * BUTTON_PADDING) + 40) * scale
        }
        val h = BUTTON_HEIGHT + 40
        return h * scale
    }

    override fun width(): Float {
        var columns = 3
        var scale = 1.0f
        buttons.theme?.let {
            columns = it.columns
            scale = it.scale
        }
        return (columns * BUTTON_WIDTH + (columns - 1) * BUTTON_PADDING + 40) * scale
    }

    protected fun shapeDefs(): String {
        val accent = docOpsTheme.accentColor
        val gradientDefs = buttons.buttons.mapIndexed { index, button ->
            val color = button.color ?: accent
            """
                <linearGradient id="btn_${button.id}" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stop-color="$color" />
                    <stop offset="100%" stop-color="$color" stop-opacity="0.7" />
                </linearGradient>
                """.trimIndent()
        }.joinToString("\n")
        
        return """
            <linearGradient id="topshineGrad" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" stop-color="#ffffff" stop-opacity="0.4" />
                <stop offset="100%" stop-color="#ffffff" stop-opacity="0" />
            </linearGradient>
            $gradientDefs
        """.trimIndent()
    }

    companion object {
        const val BUTTON_HEIGHT: Int = 56
        const val BUTTON_WIDTH = 300
        const val BUTTON_PADDING = 12
    }
}
