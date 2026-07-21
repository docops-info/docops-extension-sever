package io.docops.docopsextensionssupport.button.shape

import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.ButtonDisplay
import io.docops.docopsextensionssupport.button.Buttons
import io.docops.docopsextensionssupport.button.parseStyleForFontSize
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
import org.silentsoft.simpleicons.SimpleIcons
import kotlin.compareTo
import kotlin.div
import kotlin.text.toInt
import kotlin.times

class Hex(buttons: Buttons) : Regular(buttons) {

    private var isDark = buttons.theme?.useDark == true
    companion object Companion {
        const val BUTTON_HEIGHT: Int = 255
        const val BUTTON_WIDTH = 295
        const val BUTTON_PADDING = 10
    }

    init {
        docOpsTheme = ThemeFactory.getTheme(ButtonVisualDisplay(buttons.useDark, 4))
    }
    private var rows = mutableListOf<MutableList<Button>>()

    override fun height(): Float {
        if(rows.isEmpty()) {
            rows = toRows()
        }
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        // Use 255 for the internal step calculation
        val rowStep = 255
        val internalHeight = (rows.size * rowStep + 130.0f)

        // Match the physical height to the scaled internal coordinate height
        return internalHeight * scale
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

    override fun draw() : String {
        val id = "btn-${buttons.id}"
        val sb = StringBuilder()

        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        sb.append("""<g id="$id" transform="scale($scale)">""")

        var startX: Int
        var startY = 20
        var staggerIdx = 0
        rows.forEachIndexed { index, buttonsI ->
            startX = if(index == 0 || isEven(index)) {
                20
            } else {
                165
            }
            buttonsI.forEachIndexed { i, button ->
                val delay = (staggerIdx + i) * 0.05
                sb.append(createSingleHoneyComb(button, startX, startY, delay))
                startX += BUTTON_WIDTH
            }
            staggerIdx += buttonsI.size
            // Vertical step for perfect honeycomb tessellation
            startY += 255
        }
        sb.append("</g>")
        return sb.toString()
    }

    protected fun shapeDefs(): String {
        val id = buttons.id
        val accent = docOpsTheme.accentColor
        val gradientDefs = buttons.buttons.mapIndexed { index, button ->
            val color = button.color ?: accent
            """
                <linearGradient id="hexGrad_${button.id}" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stop-color="$color" />
                    <stop offset="100%" stop-color="$color" stop-opacity="0.7" />
                </linearGradient>
                """.trimIndent()
        }.joinToString("\n")
        
        val style = """
                    #btn_$id .hex-label {
                        font-weight: 800;
                        text-transform: uppercase;
                    }
                
                    #btn_$id .hex-container {
                        transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
                        transform-box: fill-box;
                        transform-origin: center;
                    }
                
                    #btn_$id .hex-container:hover {
                        transform: scale(1.05);
                    }
        """.trimIndent()
        
        return """
            <style>
                $style
            </style>
            $gradientDefs
        """.trimIndent()
    }

    override fun start(width: Float, height: Float): String {
        return """
        <svg xmlns="http://www.w3.org/2000/svg" width="${width / 1.77}" height="${height / 1.77}" viewBox="0 0 $width $height" xmlns:xlink="http://www.w3.org/1999/xlink" id="btn_${buttons.id}" zoomAndPan="magnify" preserveAspectRatio="xMidYMid meet">
    """.trimIndent()
    }

    override fun width(): Float {
        var columns = 3
        var scale = 1.0f
        buttons.theme?.let {
            columns = it.columns
            scale = it.scale
        }
        val internalWidth = (columns * BUTTON_WIDTH + 80)
        return internalWidth * scale
    }
    private fun createSingleHoneyComb(button: Button, x: Int, y: Int, delay: Double): String {
        val isActive = button.active
        val actualColor = button.color ?: docOpsTheme.accentColor
        val textColor = determineTextColor(actualColor)
        val primaryTextColor = textColor
        val secondaryTextColor = textColor

        // Reference the gradient ID instead of the solid color
        val cardFill = "url(#hexGrad_${button.id})"

        // Sharp Accents driven by Theme
        val cardStroke = "var(--accent)"
        val cardStrokeOpacity = if (isDark) "0.4" else "1.0"
        val cardStrokeWidth = if (isActive) "4" else "1"

        // Strip conflicting fill from user style
        val cleanUserStyle = button.buttonStyle?.labelStyle?.replace(Regex("fill\\s*:\\s*[^;]+;?"), "") ?: ""

        val spans = StringBuilder()
        val fontSize = button.buttonStyle?.let {
            parseStyleForFontSize(it.labelStyle, it.fontSize)
        } ?: 16


        // Use the multiplier provided directly by the theme
        val adjustedMaxWidth = 140F / docOpsTheme.fontWidthMultiplier
        val textSpans = itemTextWidth(itemText = button.label, maxWidth = adjustedMaxWidth, fontSize = fontSize)

        val verticalStep = (fontSize * docOpsTheme.fontLineHeight).toInt()
        val totalTextHeight = (textSpans.size * verticalStep)
        val startTextY = 185 - (totalTextHeight / 2) + (fontSize / 2) // Adjusted for baseline shift

        textSpans.forEachIndexed { index, s ->
            // Use the calculated verticalStep for dy instead of raw lineSpacing
            val calculatedDy = if (index > 0) verticalStep else 0

            spans.append("""<tspan x="149" text-anchor="middle" dy="$calculatedDy" style="font-family: 'Lexend', sans-serif !important; letter-spacing: 0.5px; fill: $textColor !important; $cleanUserStyle">${s.escapeXml()}</tspan>""")
        }

        var win = "_top"
        buttons.theme?.let { if (it.newWin) { win = "_blank" } }

        var img = ""
        button.embeddedImage?.let { img = getIcon(it.ref) }

        val endY = startTextY + (textSpans.size * fontSize)
        var href = """onclick="window.open('${button.link}', '$win')" """
        if(!button.enabled) { href = "" }

        var typeText = ""
        button.type?.let { typeText = it.uppercase() }

        return """
                <g transform="translate($x,$y)">
                    <g class="button-stagger" style="animation-delay: ${delay}s">
                    <g class="hex-container button-hover" $href role="button" tabindex="0">
                        <title>${descriptionOrLabel(button)}</title>
            
                        <!-- Ambient Glow -->
                        <circle cx="149" cy="170" r="120" fill="$actualColor" fill-opacity="0.1" filter="url(#cardShadow_${buttons.id})"/>

                        <!-- Hexagon Base -->
                        <polygon points="291,254 149,336 7,254 7,90 149,8 291,90" 
                                 fill="$cardFill" 
                                 stroke="$cardStroke" 
                                 stroke-opacity="$cardStrokeOpacity"
                                 stroke-width="${if(button.active) "4" else cardStrokeWidth}"
                                 ${if (!isPdf && !button.active) "filter=\"url(#cardShadow_${buttons.id})\"" else ""}/>

                        <!-- Glass highlight -->
                        <polygon points="149,15 280,95 149,175 18,95" 
                                 fill="white" fill-opacity="0.05" pointer-events="none"/>
                    <!-- Icon Wrapper -->
                    <g transform="translate(120,50) scale(0.8)">
                     $img 
                    </g>
            
                                            <!-- Main Label -->
                            <text x="149" y="$startTextY" text-anchor="middle" 
                                  fill="$primaryTextColor" 
                                  class="hex-label" 
                                  font-size="$fontSize"
                                  style="fill: $textColor !important;">$spans</text>
            
                            <!-- Accent Line -->
                            ${if (button.enabled) """
                            <line x1 ="110" y1 = "${endY+5}" x2 = "190" y2 = "${endY+5}"
                            stroke = "$secondaryTextColor" stroke-width = "2" stroke-linecap = "round" stroke-opacity = "0.5" / >
                            """.trimIndent() else ""}
                                
                                                <!-- Type Text -->
                                                <text x="149" y="${endY + 28}" text-anchor="middle" 
                                                      fill="$secondaryTextColor" 
                                                      font-size="14" 
                                                      font-weight="800"
                                                      style="letter-spacing: 2px; fill: $textColor !important; opacity: 0.6;">$typeText</text>
                                        </g>
                    </g>
                </g>
            """.trimIndent()
    }


    private fun descriptionOrLabel(button: Button): String {
        return when {
            button.description.isNullOrEmpty() -> {
                button.label
            }
            else -> {
                button.description
            }
        }
    }

    private fun getIcon(icon: String) : String {
        val logo = icon
        val simpleIcon = SimpleIcons.get(icon.replace("<", "").replace(">", ""))
        var filter = ""
        if (!isPdf) {
            filter = "filter=\"url(#Bevel2)\""
        }
        if (simpleIcon != null) {
            val ico = simpleIcon.svg
            if(ico.isNotBlank()) {
                return """
                    <svg width="128" height="128" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="${simpleIcon.path}" fill="#${simpleIcon.hex}" $filter/>
                    </svg>
                """.trimIndent()

            }
        }
        return """
            <svg width="64" height="64" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg" $filter>
                    <image width='50' height="50" xlink:href="$logo" href="$logo" />
            </svg>
            """.trimIndent()
    }
    override fun toRows(): MutableList<MutableList<Button>> {
        val rows = mutableListOf<MutableList<Button>>()
        var rowArray = mutableListOf<Button>()
        rows.add(rowArray)
        var count = 0
        buttons.buttons.forEach { s ->
            buttons.theme?.let { disp ->
                if(count == 0 || isEven(count)) {
                    if (rowArray.size == disp.columns) {
                        rowArray = mutableListOf()
                        rows.add(rowArray)
                        count++
                    }
                } else {
                    if (rowArray.size == (disp.columns - 1)) {
                        rowArray = mutableListOf()
                        rows.add(rowArray)
                        count++
                    }
                }
            }
            rowArray.add(s)

        }
        return rows
    }
    private fun isEven(value: Int) = value % 2 == 0
    //fun isOdd(value: Int) = value % 2 == 1

}
