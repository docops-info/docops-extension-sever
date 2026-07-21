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
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.support.VisualDisplay

/**
 * Defines the contract for rendering different button shapes in SVG format.
 * 
 * The ButtonShape interface is a core part of the button rendering system, providing
 * a common interface for all button shape implementations. It enables the application
 * to render buttons in various shapes (Rectangle, Circle, Pill, etc.) while maintaining
 * a consistent API.
 * 
 * This interface is implemented by [AbstractButtonShape], which provides common functionality,
 * and then extended by concrete shape classes like Rectangle, Circle, Pill, etc.
 * 
 * The rendering process typically follows these steps:
 * 1. A [Buttons] collection is created with a specific [io.docops.docopsextensionssupport.button.ButtonType]
 * 2. The [Buttons.createSVGShape] method creates the appropriate ButtonShape implementation
 * 3. The [drawShape] method is called to generate the SVG representation
 * 
 * Example usage:
 * ```kotlin
 * val buttons = Buttons(buttonList, ButtonType.PILL)
 * val buttonShape = buttons.createSVGShape()
 * val svgOutput = buttonShape.drawShape()
 * ```
 */
interface ButtonShape {

    /**
     * Renders the button shape as an SVG string.
     * 
     * @param type The output type, default is "SVG". Some implementations may support "PDF" as well.
     * @return A string containing the SVG representation of the button shape
     */
    fun drawShape(type: String = "SVG"): String

    /**
     * Calculates the total height of the rendered button collection.
     * 
     * @return The height in pixels as a float value
     */
    fun height(): Float

    /**
     * Calculates the total width of the rendered button collection.
     * 
     * @return The width in pixels as a float value
     */
    fun width(): Float
}

/**
 * Abstract base class that implements the [ButtonShape] interface and provides common functionality
 * for all button shape implementations.
 *
 * AbstractButtonShape handles common aspects of button rendering such as:
 * - Converting the button collection to rows based on the theme's column setting
 * - Calculating the overall dimensions (height and width) of the rendered output
 * - Collecting gradient definitions and styles from all buttons
 * - Providing utility methods for text rendering
 *
 * Concrete shape implementations extend this class and implement the [createShape] method
 * to provide shape-specific rendering logic.
 *
 * @property buttons The [Buttons] collection to be rendered
 * @property isPdf A flag indicating whether the shape is being drawn for PDF output
 */
abstract class AbstractButtonShape(val buttons: Buttons): ButtonShape {
    protected var vbHeight = 0f
    protected var vbWidth = 0f
    /** Indicates whether the output is intended for PDF format */
    protected var isPdf = false
    protected fun fontImport(): String {
        if (isPdf) return ""
        val themeImport = docOpsTheme.fontImport.replace("&amp;", "&")
        val brandedFonts = "@import url('https://fonts.googleapis.com/css2?family=Lexend:wght@400;500;600&family=JetBrains+Mono:wght@600&family=Syne:wght@800&display=swap');"
        return if (themeImport.contains("fonts.googleapis.com")) {
            // Merge theme fonts with branded fonts if they are both Google Fonts
            // For now, just return both. Browsers handle multiple imports.
            themeImport + "\n" + brandedFonts
        } else if (themeImport.isNotEmpty()) {
            themeImport + "\n" + brandedFonts
        } else {
            brandedFonts
        }
    }


    // Resolve the global design system theme
    open protected var docOpsTheme = ThemeFactory.getTheme(ButtonVisualDisplay(buttons.useDark, buttons.visualVersion))
    
    protected val accent get() = docOpsTheme.accentColor
    protected val bg get() = if (buttons.useDark) "#172033" else "#afc0c0"
    protected val surface get() = if (buttons.useDark) "#1E293B" else "#DBF0F1"
    protected val text get() = if (buttons.useDark) "#E2E8F0" else "#1190A1"

    protected fun themeColor(variable: String): String {
        return if (isPdf) {
            when (variable) {
                "--accent" -> accent
                "--bg" -> bg
                "--surface" -> surface
                "--text" -> text
                else -> "var($variable)"
            }
        } else {
            "var($variable)"
        }
    }

    protected fun makeModernBackground(width: Float, height: Float): String {
        val id = buttons.id
        return """
        <rect width="$width" height="$height" rx="18" fill="url(#bgSurface_$id)"/>
        <rect width="$width" height="$height" rx="18" fill="url(#bgGlowA_$id)"/>
        <rect width="$width" height="$height" rx="18" fill="url(#bgGlowB_$id)"/>
        <rect width="$width" height="$height" rx="18" fill="url(#fineGrid_$id)"/>
        <rect width="$width" height="$height" rx="18" fill="url(#sonarDots_$id)"/>
        <rect width="$width" height="$height" rx="18" fill="url(#vignette_$id)"/>
        """.trimIndent()
    }

    protected fun standardDefs(): String {
        val id = "btn-${buttons.id}"
        val accent = docOpsTheme.accentColor
        val bg = if (buttons.useDark) "#172033" else "#afc0c0"
        val surface = if (buttons.useDark) "#1E293B" else "#DBF0F1"
        val text = if (buttons.useDark) "#E2E8F0" else "#1190A1"

        return """
        <defs>
            <style>
                ${fontImport()}
                [id='$id'] {
                    --accent: $accent;
                    --bg: $bg;
                    --surface: $surface;
                    --text: $text;
                    --card-radius: 12px;
                    --stagger-delay: 0.05s;
                }
                [id='$id'] text { 
                    font-family: 'Lexend', ui-sans-serif, system-ui, sans-serif; 
                }
                @keyframes riseIn_$id {
                    from { opacity: 0; transform: translateY(20px); }
                    to { opacity: 1; transform: translateY(0); }
                }
                [id='$id'] .button-stagger {
                    opacity: 0;
                    animation: riseIn_$id 0.6s cubic-bezier(0.22, 1, 0.36, 1) forwards;
                }
                [id='$id'] .button-hover {
                    transition: transform 0.2s ease, filter 0.2s ease;
                    cursor: pointer;
                }
                [id='$id'] .button-hover:hover {
                    transform: translateY(-2px) scale(1.02);
                    filter: brightness(1.1);
                }
                ${if (isPdf) "[id='$id'] .button-stagger { opacity: 1 !important; animation: none !important; }" else ""}
            </style>
            <linearGradient id="bgSurface_${buttons.id}" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stop-color="$bg"/>
                <stop offset="46%" stop-color="$surface"/>
                <stop offset="100%" stop-color="$bg"/>
            </linearGradient>
            <radialGradient id="bgGlowA_${buttons.id}" cx="18%" cy="10%" r="70%">
                <stop offset="0%" stop-color="$accent" stop-opacity="0.16"/>
                <stop offset="100%" stop-color="$accent" stop-opacity="0"/>
            </radialGradient>
            <radialGradient id="bgGlowB_${buttons.id}" cx="84%" cy="22%" r="58%">
                <stop offset="0%" stop-color="$accent" stop-opacity="0.12"/>
                <stop offset="100%" stop-color="$accent" stop-opacity="0"/>
            </radialGradient>
            <radialGradient id="vignette_${buttons.id}" cx="50%" cy="48%" r="78%">
                <stop offset="0%" stop-color="#000000" stop-opacity="0"/>
                <stop offset="100%" stop-color="#000000" stop-opacity="0.10"/>
            </radialGradient>
            <pattern id="sonarDots_${buttons.id}" x="0" y="0" width="24" height="24" patternUnits="userSpaceOnUse">
                <circle cx="2" cy="2" r="1" fill="$accent" opacity="0.12"/>
            </pattern>
            <pattern id="fineGrid_${buttons.id}" x="0" y="0" width="48" height="48" patternUnits="userSpaceOnUse">
                <path d="M48 0 H0 V48" fill="none" stroke="$accent" stroke-opacity="0.055" stroke-width="1"/>
            </pattern>
            <filter id="cardShadow_${buttons.id}" x="-20%" y="-20%" width="140%" height="140%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="4" result="blur"/>
                <feOffset in="blur" dx="0" dy="2" result="offsetblur"/>
                <feComponentTransfer><feFuncA type="linear" slope="0.15"/></feComponentTransfer>
                <feMerge><feMergeNode/><feMergeNode in="SourceGraphic"/></feMerge>
            </filter>
        </defs>
        """.trimIndent()
    }

    /**
     * Implements the [ButtonShape.drawShape] method by calling the abstract [createShape] method
     * and joining the resulting XML lines.
     *
     * @param type The output type, either "SVG" or "PDF"
     * @return A string containing the SVG representation of the button shape
     */
    override fun drawShape(type: String): String {
        if("PDF".equals(type, true) || "PNG".equals(type, true)) {
            isPdf = true
        }
        return createShape(type)
    }

    /**
     * Creates the SVG representation of the button shape.
     * 
     * This abstract method must be implemented by concrete shape classes to provide
     * the specific SVG rendering for that shape.
     *
     * @param type The output type, either "SVG" or "PDF"
     * @return A string containing the SVG representation of the button shape
     */
    abstract fun createShape(type: String): String

    /**
     * Calculates the total height of the rendered button collection.
     * 
     * The height is determined by:
     * 1. The number of rows of buttons
     * 2. The height of each button (BUTTON_HEIGHT)
     * 3. The spacing between rows
     * 4. The scale factor from the theme
     *
     * @return The height in pixels as a float value
     */
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

    /**
     * Calculates the total width of the rendered button collection.
     * 
     * The width is determined by:
     * 1. The number of columns from the theme (default 3)
     * 2. The width of each button (BUTTON_WIDTH)
     * 3. The padding between buttons
     * 4. The scale factor from the theme
     *
     * @return The width in pixels as a float value
     */
    override fun width(): Float {
        var columns = 3
        var scale = 1.0f
        buttons.theme?.let {
            columns = it.columns
            scale = it.scale
        }
        return (columns * BUTTON_WIDTH + columns * BUTTON_PADDING + columns * BUTTON_PADDING) * scale
    }

    /**
     * Organizes buttons into rows based on the theme's column setting.
     * 
     * This method creates a two-dimensional structure where:
     * - The outer list represents rows
     * - Each inner list represents buttons in that row
     * - The number of buttons per row is determined by the theme's columns property
     *
     * This row structure is used by shape implementations to position buttons in a grid layout.
     *
     * @return A nested list structure representing rows of buttons
     */
    protected open fun toRows(): MutableList<MutableList<Button>> {
        val rows = mutableListOf<MutableList<Button>>()
        var rowArray = mutableListOf<Button>()
        rows.add(rowArray)
        buttons.buttons.forEach {
            buttons.theme?.let { disp ->
                if (rowArray.size == disp.columns) {
                    rowArray = mutableListOf()
                    rows.add(rowArray)
                }
                rowArray.add(it)
            }
        }
        return rows
    }

    /**
     * Collects all gradient definitions from the buttons.
     * 
     * This method concatenates the gradient definitions from all buttons in the collection,
     * which are then included in the SVG output to provide the visual styling for the buttons.
     *
     * @return A string containing all gradient definitions
     */
    fun gradient() : String {
        val sb = StringBuilder()
        buttons.buttons.forEach {
            sb.append(it.gradient)
        }
        return sb.toString()
    }

    /**
     * Collects all gradient style definitions from the buttons.
     * 
     * This method concatenates the CSS style definitions for gradients from all buttons
     * in the collection, which are then included in the SVG output.
     *
     * @return A string containing all gradient style definitions
     */
    fun gradientStyle() : String {
        val sb = StringBuilder()
        buttons.buttons.forEach {
            sb.append(it.buttonGradientStyle)
        }
        return sb.toString()
    }
    /**
     * Contains constants used for button layout and sizing.
     */
    companion object {
        /**
         * The default height of a button in pixels.
         * This value is used as the base height for calculating the overall height of button rows.
         */
        const val BUTTON_HEIGHT: Int = 44

        /**
         * The default width of a button in pixels.
         * This value is used as the base width for calculating the overall width of button columns.
         */
        const val BUTTON_WIDTH = 300

        /**
         * The padding around buttons in pixels.
         * This value defines the space between the button content and its border.
         */
        const val BUTTON_PADDING = 10

        /**
         * The spacing between buttons in pixels.
         * This value defines the space between adjacent buttons in the layout.
         */
        const val BUTTON_SPACING = 10
    }

    /**
     * Converts a list of text lines into SVG multi-line text with tspan elements.
     * 
     * This utility method is used by shape implementations to render multi-line text
     * within buttons, such as descriptions or other content that needs to span multiple lines.
     *
     * @param style The CSS style to apply to the text
     * @param lines The list of text lines to render
     * @param dy The vertical offset between lines in pixels
     * @param x The horizontal position of the text
     * @return An SVG string containing tspan elements for each line
     */
    fun linesToMultiLineText(style: String?, lines: MutableList<String>, dy: Int, x: Int): String {
        val text = StringBuilder()
        lines.forEach {
            text.append("""<tspan x="$x" dy="$dy" style="$style">$it</tspan>""")
        }
        return text.toString()
    }
}

class ButtonVisualDisplay(
    override val useDark: Boolean,
    override val visualVersion: Int
): VisualDisplay {
}
