package io.docops.docopsextensionssupport.button.shape


import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons
import io.docops.docopsextensionssupport.roadmap.wrapText
import io.docops.docopsextensionssupport.svgsupport.escapeXml

/**
 * Implements a circular button shape with centered text.
 *
 * The Circle class extends the [Regular] class to create buttons with a circular appearance.
 * Each button is rendered as a perfect circle with text centered in the middle, providing
 * a distinctive and visually appealing alternative to rectangular buttons.
 *
 * Key features:
 * - Perfect circle shape with 50px radius
 * - Text centered in the middle of the circle
 * - Automatic text wrapping for longer labels
 * - Intelligent vertical positioning of text based on number of lines
 * - Enhanced styling with gradients, shadows, and visual effects
 * - Dynamic color support with base color from button.color attribute
 * - Compact layout with buttons arranged in a grid
 *
 * This shape is particularly useful for:
 * - Navigation menus where a distinctive shape helps with recognition
 * - Interfaces where space efficiency is important
 * - Designs that require a more modern or playful appearance
 * - Applications where circular buttons align with the overall design aesthetic
 *
 * The Circle shape maintains the same row-based layout as Regular buttons
 * but with different spacing and dimensions optimized for circular elements.
 */
class Circle(buttons: Buttons): Regular(buttons) {

    companion object {
        const val BUTTON_HEIGHT: Int = 50
        const val BUTTON_WIDTH = 300
        const val BUTTON_PADDING = 10
        const val BUTTON_SPACING = 10
    }

    override fun drawButton(
        index: Int,
        buttonList: MutableList<Button>
    ): String {
        val btns = StringBuilder()
        var win = "_top"
        buttons.theme?.let {
            if (it.newWin) {
                win = "_blank"
            }
        }
        var startX = 60
        var startY = 10
        if (index > 0) {
            startY += 110
        }

        buttonList.forEach { button: Button ->
            var fill = "class=\"btn_${button.id}_cls\""
            var baseColor = button.color ?: "#3498db"

            if(isPdf) {
                fill = "fill='${baseColor}'"
            }

            val lines = wrapText(button.label.escapeXml(), 15f)
            var lineY = 0
            if(lines.size > 0) {
                lineY = lines.size * -6
            }
            val title = linesToMultiLineTextInternal(button.buttonStyle?.labelStyle,
                lines, 12, 50)

            var href = """<a xlink:href="${button.link}" href="${button.link}" target="$win" style='text-decoration: none; font-family:Arial; fill: #fcfcfc;'>"""
            var endAnchor = "</a>"
            if(!button.enabled) {
                href = ""
                endAnchor = ""
            }

            btns.append(
                """
        $href
        <g role="button" cursor="pointer" transform="translate($startX,$startY)" class="circle-button">
            <title class="description">${button.description?.escapeXml() ?: ""}</title>
            
            <!-- Enhanced circle with gradient and shadow -->
            <circle id="button-shadow" cx="50" cy="52" r="50" 
                    fill="${darkenColor(baseColor, 0.6)}" 
                    opacity="0.3" 
                    filter="url(#circleShadowBlur)" />
            
            <circle id="button-base" cx="50" cy="50" r="50" 
                    fill="url(#circleGradient_${button.id})" 
                    stroke="${darkenColor(baseColor, 0.4)}" 
                    stroke-width="2" />
            
            <!-- Highlight for 3D effect - constrained within the main circle -->
            <circle id="button-highlight" cx="50" cy="40" r="30" 
                    fill="url(#circleHighlight_${button.id})" 
                    opacity="0.4" />
            
            <!-- Inner shadow for depth -->
            <circle id="button-inner-shadow" cx="50" cy="50" r="48" 
                    fill="none" 
                    stroke="${darkenColor(baseColor, 0.8)}" 
                    stroke-width="1" 
                    opacity="0.2" />
            
            <!-- Text with enhanced styling -->
            <text id="label" x="50" y="50" text-anchor="middle" 
                  style="fill: #ffffff; font-weight: bold; text-shadow: 1px 1px 2px rgba(0,0,0,0.5); ${button.buttonStyle?.labelStyle ?: ""}">
                $title
            </text>
        </g>
        $endAnchor
        """.trimIndent()
            )

            startX += 105
        }
        return btns.toString()
    }

    fun linesToMultiLineTextInternal(style: String?, lines: MutableList<String>, dy: Int, x: Int): String {
        val text = StringBuilder()
        var indexes = mutableListOf<Int>(0)
        if(lines.size == 2) {
            indexes = mutableListOf(-6, 12)
        }
        if(lines.size == 3) {
            indexes = mutableListOf(-12, 12, 12)
        }
        if(lines.size == 4) {
            indexes = mutableListOf(-18, 12, 12, 12)
        }
        lines.forEachIndexed { i, s ->
            text.append("""<tspan x="$x" dy="${indexes[i]}" style="$style">$s</tspan>""")
        }
        return text.toString()
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
        return ((cols * 105) + 60) * scale
    }

    override fun defs(): String {
        val gradientDefs = StringBuilder()
        val styleDefs = StringBuilder()

        // Create gradients for each button
        buttons.buttons.forEach { button ->
            val baseColor = button.color ?: "#3498db"
            gradientDefs.append(createCircleGradient(button))
            gradientDefs.append(createCircleHighlight(button))
        }

        // Enhanced styles for interactive states - WITHOUT transform changes
        if (!isPdf) {
            styleDefs.append("""
                <style>
                    .circle-button {
                        cursor: pointer;
                    }
                    
                    .circle-button:hover #button-base {
                        stroke-width: 3px;
                        filter: drop-shadow(2px 4px 8px rgba(0,0,0,0.3));
                    }
                    
                    .circle-button:hover #button-highlight {
                        opacity: 0.8;
                    }
                    
                    .circle-button:active #button-base {
                        filter: brightness(0.9);
                    }
                </style>
            """.trimIndent())
        }

        return """
            <defs>
                <!-- Enhanced shadow filter -->
                <filter id="circleShadowBlur" x="-50%" y="-50%" width="200%" height="200%">
                    <feGaussianBlur in="SourceGraphic" stdDeviation="3"/>
                </filter>
                
                <!-- Button blur filter (keeping original) -->
                <filter id="buttonBlur" x="-50%" y="-50%" width="200%" height="200%">
                    <feGaussianBlur in="SourceGraphic" stdDeviation="2"/>
                </filter>
                
                $gradientDefs
                $styleDefs
            </defs>
        """.trimIndent()
    }

    /**
     * Creates a radial gradient for the main circle button based on the dynamic color
     */
    private fun createCircleGradient(button: Button): String {
        val baseColor = button.color ?: "#3498db"
        val darkerColor = darkenColor(baseColor, 0.3)
        val lighterColor = lightenColor(baseColor, 0.2)

        return """
            <radialGradient id="circleGradient_${button.id}" cx="30%" cy="25%" r="80%">
                <stop offset="0%" style="stop-color:${lighterColor};stop-opacity:1" />
                <stop offset="50%" style="stop-color:${baseColor};stop-opacity:1" />
                <stop offset="100%" style="stop-color:${darkerColor};stop-opacity:1" />
            </radialGradient>
        """.trimIndent()
    }

    /**
     * Creates a highlight gradient for the 3D effect
     */
    private fun createCircleHighlight(button: Button): String {
        return """
            <radialGradient id="circleHighlight_${button.id}" cx="50%" cy="30%" r="60%">
                <stop offset="0%" style="stop-color:#ffffff;stop-opacity:0.8" />
                <stop offset="70%" style="stop-color:#ffffff;stop-opacity:0.3" />
                <stop offset="100%" style="stop-color:#ffffff;stop-opacity:0" />
            </radialGradient>
        """.trimIndent()
    }

    /**
     * Helper function to darken a color by a specified factor
     */
    private fun darkenColor(hexColor: String, factor: Double): String {
        val color = java.awt.Color.decode(hexColor)
        val r = (color.red * (1 - factor)).toInt().coerceAtLeast(0)
        val g = (color.green * (1 - factor)).toInt().coerceAtLeast(0)
        val b = (color.blue * (1 - factor)).toInt().coerceAtLeast(0)
        return String.format("#%02x%02x%02x", r, g, b)
    }

    /**
     * Helper function to lighten a color by a specified factor
     */
    private fun lightenColor(hexColor: String, factor: Double): String {
        val color = java.awt.Color.decode(hexColor)
        val r = (color.red + (255 - color.red) * factor).toInt().coerceAtMost(255)
        val g = (color.green + (255 - color.green) * factor).toInt().coerceAtMost(255)
        val b = (color.blue + (255 - color.blue) * factor).toInt().coerceAtMost(255)
        return String.format("#%02x%02x%02x", r, g, b)
    }
}
