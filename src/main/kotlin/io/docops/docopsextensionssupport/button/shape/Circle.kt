package io.docops.docopsextensionssupport.button.shape


import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons
import io.docops.docopsextensionssupport.roadmap.wrapText
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.util.BackgroundHelper
import kotlin.compareTo
import kotlin.times

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
        const val BUTTON_HEIGHT: Int = 120  // Circle diameter (100) + padding (20)
        const val BUTTON_WIDTH = 120       // Circle diameter (100) + padding (20)
        const val BUTTON_PADDING = 20      // Padding between circles
        const val BUTTON_SPACING = 20      // Additional spacing
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
        var startX = 20
        var startY = 20 // Start with some top padding

        // Calculate Y position based on row index
        // Each circle needs 120px height (diameter 100px + 20px padding)
        if (index > 0) {
            startY = 20 + (index * 120) // 20px top padding + 120px per row
        }

        buttonList.forEachIndexed { idx, button: Button ->
            val baseColor = button.color ?: "#3498db"

            val lines = wrapText(button.label, 15f)
            var lineY = 0
            if(lines.size > 0) {
                lineY = lines.size * -6
            }
            val title = linesToMultiLineTextInternal(button.buttonStyle?.labelStyle,
                lines, 12, 60)

            var href = """onclick="window.open('${button.link}', '$win')" style="cursor: pointer;""""
            if(!button.enabled) {
                href = ""
            }

            // Define colors and styles based on dark/light mode
            val (circleBackground, circleBorder, textColor, shadowColor) = if (buttons.useDark) {
                arrayOf(
                    "rgba(255,255,255,0.2)",
                    "url(#borderGradientDark_${buttons.id})",
                    "rgba(255,255,255,0.95)",
                    "rgba(0,0,0,0.5)"
                )
            } else {
                arrayOf(
                    "rgba(0,0,0,0.1)",
                    "url(#borderGradientLight_${buttons.id})",
                    "#2d3748",
                    "rgba(255,255,255,0.8)"
                )
            }

            val glassGradientId = if (buttons.useDark) "glassGradientDark_${buttons.id}" else "circleGradient_${button.id}"
            val path = circleToPath(60f, 60f, 50f)
            btns.append(
                """
        <g role="button" transform="translate($startX,$startY)" class="circle-button" $href>
            <title class="description">${button.description?.escapeXml() ?: ""}</title>
            
            <path d="$path" fill="$circleBackground"
                    stroke="$circleBorder"
                    stroke-width="2"
                    filter="url(#dropShadow)"
                    style="backdrop-filter: blur(20px);"/>
            <!-- Glass overlay -->
            <path d="$path" fill="url(#$glassGradientId)" opacity="0.8"/>
            <text id="label_$idx" 
                  x="60" y="60"
                  text-anchor="middle"
                  dominant-baseline="central"
                  fill="$textColor"
                  style="${button.buttonStyle?.labelStyle ?: ""}">
                $title
            </text>
        </g>
       
        """.trimIndent()
            )

            startX += 120 // Increase horizontal spacing to accommodate 100px diameter + padding
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
        // Each row needs 120px (100px circle diameter + 20px padding)
        // Add extra 40px for top and bottom margins
        return ((size * 120) + 40) * scale
    }

    override fun width(): Float {
        var cols = 3
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
            cols = it.columns
        }
        // Each circle needs 120px width (100px diameter + 20px padding)
        // Add 40px for left and right margins
        return ((cols * 120) + 40) * scale
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
        val darkModeDefs = BackgroundHelper.getBackgroundGradient(useDark = buttons.useDark, buttons.id)
        // Enhanced styles for interactive states - WITHOUT transform changes
        if (!isPdf) {
            styleDefs.append("""
                <style>
                   
                    
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
                
                <!-- Gradient for the glass effect -->
            <radialGradient id="glassGradient" cx="0.3" cy="0.3" r="0.8">
                <stop offset="0%" style="stop-color:rgba(255,255,255,0.4);stop-opacity:1" />
                <stop offset="70%" style="stop-color:rgba(255,255,255,0.15);stop-opacity:1" />
                <stop offset="100%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1" />
            </radialGradient>

            <!-- Border gradient -->
            <radialGradient id="borderGradient" cx="0.3" cy="0.3" r="0.9">
                <stop offset="0%" style="stop-color:rgba(255,255,255,0.6);stop-opacity:1" />
                <stop offset="100%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
            </radialGradient>

            <!-- Shadow filter -->
            <filter id="dropShadow" x="-20%" y="-20%" width="140%" height="140%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="3"/>
                <feOffset dx="0" dy="2" result="offset" />
                <feFlood flood-color="rgba(0,0,0,0.15)"/>
                <feComposite in2="offset" operator="in"/>
                <feMerge>
                    <feMergeNode/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>

            <!-- Inner glow -->
            <filter id="innerGlow" x="-20%" y="-20%" width="140%" height="140%">
                <feGaussianBlur stdDeviation="2" result="coloredBlur"/>
                <feMerge>
                    <feMergeNode in="coloredBlur"/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>
                $darkModeDefs
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
        val svgColor = SVGColor(baseColor, "btn_${button.id}")
        val darkerColor = svgColor.darkenColor(baseColor, 0.3)
        val lighterColor = svgColor.brightenColor(baseColor, 0.2)


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


    fun circleToPath(cx: Float, cy: Float, r: Float): String {
        // M (start_x), (start_y) - Move to the starting point
        // a rx,ry x-axis-rotation large-arc-flag sweep-flag end_x,end_y - Arc command
        // We'll create two arcs to form the circle.

        // Starting point for the first arc (leftmost point of the circle)
        val startX = cx - r
        val startY = cy

        // End point for the first arc (rightmost point of the circle)
        val midX = cx + r
        val midY = cy

        // End point for the second arc (back to the starting point)
        val endX = cx - r
        val endY = cy

        return "M $startX,$startY " +
                "a $r,$r 0 1,0 ${r * 2},0 " + // First semicircle (clockwise)
                "a $r,$r 0 1,0 ${-r * 2},0"   // Second semicircle (clockwise, back to start)
    }


}
