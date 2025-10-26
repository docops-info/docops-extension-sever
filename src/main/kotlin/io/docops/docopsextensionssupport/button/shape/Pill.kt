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


    /**
     * Generates the SVG code for rendering a list of buttons.
     *
     * @param index The index of the first button.
     * @param buttonList The list of buttons to be rendered.
     * @return The SVG code for rendering the buttons.
     */
    override fun drawButton(index: Int, buttonList: MutableList<Button>): String {
        val btns = StringBuilder()
        var win = "_top"
        buttons.theme?.let {
            if (it.newWin) {
                win = "_blank"
            }
        }

        var startX = 0

        var startY = 0
        if (index > 0) {
            startY = index * BUTTON_HEIGHT + (index * 10)
        }
        buttonList.forEach { button: Button ->
            var fill = "class=\"btn_${button.id}_cls\""
            var overlay = "url(#overlayGrad)"
            if(isPdf) {
                fill = "fill='${button.color}'"
                overlay = "${button.color}"
            }
            var href = """onclick="window.open('${button.link}', '$win')" style="cursor: pointer;""""
            if(!button.enabled) {
                href = ""
            }

            btns.append(
                """
                <g role="button" transform="translate($startX, $startY)" $href>
                    <rect id="button" x="5" y="5" width="$BUTTON_WIDTH" height="$BUTTON_HEIGHT" ry="26" rx="26" $fill filter="url(#buttonBlur)" />
                    <rect id="buttongrad" x="5" y="5" width="$BUTTON_WIDTH" height="$BUTTON_HEIGHT" ry="26" rx="26" fill="$overlay"/>
                    <rect id="buttontop" x="15" y="10.5" width="280" height="25" ry="24" rx="24" fill="url(#topshineGrad)" filter="url(#topshineBlur)"/>
                    <rect id="buttonbottom" x="25" y="50" width="260" height="7" fill="#ffffff" ry="24" rx="24" fill-opacity="0.3" filter="url(#bottomshine)"/>
                    <text id="label" x="150" y="43" text-anchor="middle" style="${button.buttonStyle?.labelStyle}">${button.label.escapeXml()}</text>
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
        const val BUTTON_HEIGHT: Int = 56
        const val BUTTON_WIDTH = 300
        const val BUTTON_PADDING = 12
    }
}
