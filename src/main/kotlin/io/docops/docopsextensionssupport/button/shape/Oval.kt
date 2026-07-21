package io.docops.docopsextensionssupport.button.shape

import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons
import io.docops.docopsextensionssupport.support.determineTextColor
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
     * Renders the buttons with the oval shape and modern visual effects.
     *
     * @return The SVG string representation of the buttons
     */
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
        var startX = 27.5
        var startY = 25.0
        if (index > 0) {
            startY = index * BUTTON_HEIGHT + (index * BUTTON_PADDING) + 25.0
        }

        buttonList.forEachIndexed { i, button: Button ->
            val delay = (rowStartStagger + i) * 0.05
            val baseColor = button.color ?: docOpsTheme.accentColor
            val textColor = determineTextColor(baseColor)
            val labelStyle = button.buttonStyle?.labelStyle?.replace(Regex("fill\\s*:\\s*[^;]+;?"), "")
            
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

            var href = """onclick="window.open('${button.link}', '$win')" """
            if(!button.enabled) {
                href = ""
            }
            btns.append("""
                <g transform="translate($startX,$startY)">
                    <g class="button-stagger" style="animation-delay: ${delay}s">
                    <g role="button" tabindex="0" class="button-hover" $href>
                    <rect id="button" x="0" y="0" width="250" height="90" rx="36" ry="36" fill="url(#btn_${button.id})" filter="url(#cardShadow_${buttons.id})" />
                    <rect id="buttontop" x="10" y="5" width="230" height="40" rx="30" ry="30" fill="url(#topshineGrad)" fill-opacity="0.15" />
                    <rect id="buttonbottom" x="20" y="70" width="210" height="15" rx="30" ry="7" fill="#ffffff" fill-opacity="0.1" />
                    <text id="label" x="125" y="0" text-anchor="middle" fill="$textColor" style="font-weight: 700; $labelStyle">
                        $tspan
                    </text>
                    </g>
                    </g>
                </g>
            """.trimIndent())

            startX += BUTTON_WIDTH + BUTTON_PADDING + 5
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
        return (columns * (BUTTON_WIDTH + BUTTON_PADDING + 5) + 40) * scale
    }

    protected fun shapeDefs(): String {
        val id = "btn-${buttons.id}"
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
        const val BUTTON_HEIGHT: Int = 90
        const val BUTTON_WIDTH = 250
        const val BUTTON_PADDING = 10
    }
}
