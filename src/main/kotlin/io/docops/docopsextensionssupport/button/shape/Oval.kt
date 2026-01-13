package io.docops.docopsextensionssupport.button.shape

import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth

/**
 * Implements an oval button shape with enhanced visual effects.
 *
 * The Oval class extends the [Regular] class to create buttons with a distinctive oval appearance.
 * Each button is rendered as a rounded rectangle with fully rounded ends, providing
 * a smooth, modern look with multiple visual effects for depth and dimension.
 *
 * Key features:
 * - Oval shape with fully rounded ends (36px radius)
 * - Multiple layered visual effects:
 *   - Base button with gradient fill
 *   - Blur filter for soft edges
 *   - Top shine gradient for highlight
 *   - Bottom shine for depth
 * - Intelligent text positioning based on content length
 * - Smooth layout with appropriate spacing
 *
 * This shape is particularly useful for:
 * - Primary action buttons that need to stand out
 * - Interfaces with a modern, glossy aesthetic
 * - Buttons that need to appear more tactile and pressable
 * - Designs where smooth, rounded shapes are preferred
 *
 * The Oval shape maintains the same row-based layout as Regular buttons
 * but with enhanced visual styling that gives a more polished appearance.
 */
class Oval(buttons: Buttons) : Regular(buttons) {

    /**
     * Renders the buttons with the oval shape and modern visual effects.
     *
     * @return The SVG string representation of the buttons
     */
    override fun draw(): String {
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        var back =""
        if(buttons.useDark) {
            back = "<rect width=\"100%\" height=\"100%\" fill=\"#1f2937\"/>"
        }
        val sb = StringBuilder("""
            $back
            <g transform="scale($scale)">
            """)
        val rows = toRows()
        var count = 0
        rows.forEachIndexed { index, buttons ->

            sb.append(drawButtonInternal(index, buttons, count))
            count += buttons.size
        }
        sb.append("</g>")
        return sb.toString()
    }

    /**
     * Renders a row of oval buttons with modern visual effects.
     *
     * @param index The index of the row
     * @param buttonList The list of buttons to render in this row
     * @param count The total count of buttons rendered so far
     * @return The SVG string representation of the buttons in this row
     */
    private fun drawButtonInternal(index: Int, buttonList: MutableList<Button>, count: Int): Any {

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

        buttonList.forEach { button: Button ->
            val text = itemTextWidth(button.label, 245F, 24)
            val tspan = StringBuilder()
            var dy = 0
            text.forEachIndexed { index, s ->
                dy = if (text.size == 1) {
                    51
                } else if(text.size == 2 && index == 0) {
                    41
                } else if(text.size == 3 && index == 0) {
                    31
                } else {
                    24
                }
                tspan.append("""<tspan x="125" dy="$dy">${s.escapeXml()}</tspan>""")
            }

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
            btns.append("""
                <g role="button" transform="translate($startX,$startY)" $href>
                    <rect id="button" x="0" y="0" width="250" height="90" rx="36" ry="36" $fill filter="url(#buttonBlur)" />
                    <rect id="buttongrad" x="0" y="0" width="250" height="90" rx="36" ry="36" fill="$overlay" />
                    <rect id="buttontop" x="10" y="5" width="230" height="40" rx="30" ry="30" fill="url(#topshineGrad)" filter="url(#topshineBlur)" />
                    <rect id="buttonbottom" x="20" y="70" width="210" height="15" rx="30" ry="7" fill="#ffffff" fill-opacity="0.3" filter="url(#bottomshine)" />
                    <text id="label" x="125" y="0" text-anchor="middle" style="${button.buttonStyle?.labelStyle}">
                        $tspan
                    </text>
                </g>
            """.trimIndent())

            startX += BUTTON_WIDTH + BUTTON_PADDING + 5
        }
        return btns.toString()
    }

    override fun width(): Float {
        var columns = 3
        var scale = 1.0f
        buttons.theme?.let {
            columns = it.columns
            scale = it.scale
        }
        // Adjusted to account for the button width (250px) plus padding
        // Each button takes BUTTON_WIDTH + BUTTON_PADDING + 5 horizontal space (line 121)
        // Adding extra padding (10px) for the initial left margin
        return (columns * (BUTTON_WIDTH + BUTTON_PADDING + 5) + 10) * scale
    }

    override fun height(): Float {
        val size = toRows().size
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        if (size > 1) {
            // Adjusted to account for the button height (90px) plus padding
            // Adding extra padding to ensure the bottom shine effect is visible
            return (size * BUTTON_HEIGHT + (size * BUTTON_PADDING) + 20) * scale
        }
        // For a single row, add extra padding for the bottom shine effect
        val h = BUTTON_HEIGHT + 40
        return h * scale
    }

    override fun defs(): String {
        val gradientDefs = buttons.buttons.mapIndexed { index, button ->
            val color = button.color ?: "#38bdf8"
            """
                <linearGradient id="btn_${button.id}" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stop-color="$color" />
                    <stop offset="100%" stop-color="$color" stop-opacity="0.7" />
                </linearGradient>
                """.trimIndent()
        }.joinToString("\n")
        val style = StringBuilder()
        buttons.buttons.forEach { button ->
            style.append("""
                #btn_${buttons.id} .btn_${button.id}_cls {
                    fill: url(#btn_${button.id});
                }
            """.trimIndent())
        }
        return """
            <defs>
            ${filters()}
            $gradientDefs
            <style>
            $style
            </style>
            </defs>
        """.trimIndent()
    }
    companion object {
        const val BUTTON_HEIGHT: Int = 90
        const val BUTTON_WIDTH = 250
        const val BUTTON_PADDING = 10
    }
}
