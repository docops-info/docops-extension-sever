package io.docops.docopsextensionssupport.button.shape


import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons
import io.docops.docopsextensionssupport.roadmap.wrapText
import io.docops.docopsextensionssupport.support.determineTextColor
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

    override fun drawButton(
        index: Int,
        buttonList: MutableList<Button>,
        rowStartStagger: Int
    ): String {
        val btns = StringBuilder()
        var win = "_top"
        buttons.theme?.let {
            if (it.newWin) {
                win = "_blank"
            }
        }
        var startX = 35.0
        var startY = 35.0 // Start with some top padding

        // Calculate Y position based on row index
        if (index > 0) {
            startY = 35.0 + (index * 130)
        }

        buttonList.forEachIndexed { idx, button: Button ->
            val delay = (rowStartStagger + idx) * 0.05
            val lines = wrapText(button.label, 15f)

            val baseColor = button.color ?: docOpsTheme.accentColor
            val textColor = determineTextColor(baseColor)
            val labelStyle = button.buttonStyle?.labelStyle?.replace(Regex("fill\\s*:\\s*[^;]+;?"), "")

            val title = linesToMultiLineTextInternal(labelStyle,
                lines, 12, 60)

            var href = """onclick="window.open('${button.link}', '$win')" """
            if(!button.enabled) {
                href = ""
            }

            // Aesthetic logic driven by ThemeFactory
            val accentColor = button.color ?: themeColor("--accent")

            btns.append(
                """
            <g transform="translate($startX,$startY)">
                <g class="button-stagger" style="animation-delay: ${delay}s">
                <g role="button" tabindex="0" class="button-hover circle-group" $href>
                    <title class="description">${button.description?.escapeXml() ?: ""}</title>
            
                    <!-- FIXED Shadow Layer -->
                    <circle r="50" cx="60" cy="60" fill="black" opacity="0.15" filter="url(#cardShadow_${buttons.id})"/>

                    <!-- SHIFTING Technical Ring -->
                    <circle class="tech-ring" r="58" cx="60" cy="60" fill="none" stroke="$accentColor" stroke-width="2" stroke-opacity="0.3"/>

                    <!-- SHIFTING Body -->
                    <circle class="orb-body" r="50" cx="60" cy="60" fill="url(#circleGradient_${button.id})" stroke="$accentColor" stroke-width="1.5" stroke-opacity="0.2"/>
                     
                <!-- SHIFTING Glass Layer -->
                ${if(!isPdf) """<circle class="orb-glass" r="46" cx="60" cy="60" fill="url(#glassReflection)" pointer-events="none"/>""" else ""}

                <!-- SHIFTING Content Group -->
                <g class="moving-text">
                        <!-- Technical Corner Accent -->
                        <path d="M 25 35 L 25 25 L 35 25" fill="none" stroke="$accentColor" stroke-width="2.5" opacity="0.8"/>
                    
                        <text x="60" y="60" text-anchor="middle" dominant-baseline="central" class="label-text" fill="$textColor">
                            $title
                        </text>
                        <text x="60" y="78" text-anchor="middle" class="label-text technical-id" fill="$textColor" fill-opacity="0.6">ID: 0x${idx + rowStartStagger + 100}</text>
                    </g>
                </g>
            </g>
        </g>
            """.trimIndent()
            )

            startX += 130
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
        return ((size * 130) + 60) * scale
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
        return ((cols * 130) + 60) * scale
    }

    protected fun shapeDefs(): String {
        val id = "btn-${buttons.id}"
        val accent = docOpsTheme.accentColor
        
        val gradientDefs = StringBuilder()
        buttons.buttons.forEach { button ->
            gradientDefs.append(createCircleGradient(button))
        }

        val style = """
                    [id='$id'] .orb-body, [id='$id'] .orb-glass, [id='$id'] .tech-ring {
                        transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
                    }
                    
                    [id='$id'] .button-hover:hover .orb-body, [id='$id'] .button-hover:hover .orb-glass {
                        cx: 56;
                        cy: 56;
                    }

                    [id='$id'] .tech-ring {
                        stroke-dasharray: 365;
                        stroke-dashoffset: ${if(isPdf) "0" else "365"};
                        opacity: ${if(isPdf) "1" else "0.3"};
                    }
                    [id='$id'] .button-hover:hover .tech-ring {
                        stroke-dashoffset: 0;
                        opacity: 0.8;
                    }

                    [id='$id'] .moving-text {
                        transition: transform 0.4s cubic-bezier(0.22, 1, 0.36, 1);
                    }
                    [id='$id'] .button-hover:hover .moving-text {
                        transform: translate(-4px, -4px);
                    }

                    [id='$id'] .label-text {
                        font-family: 'Lexend', sans-serif;
                        font-weight: 800;
                        font-size: 11px;
                        text-transform: uppercase;
                        letter-spacing: 0.05em;
                        pointer-events: none;
                    }
                    [id='$id'] .technical-id {
                        font-size: 7px;
                        font-weight: 400;
                    }
        """.trimIndent()

        return """
            <style>
                $style
            </style>
            <linearGradient id="glassReflection" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" stop-color="white" stop-opacity="0.2"/>
                <stop offset="100%" stop-color="white" stop-opacity="0"/>
            </linearGradient>
            $gradientDefs
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
