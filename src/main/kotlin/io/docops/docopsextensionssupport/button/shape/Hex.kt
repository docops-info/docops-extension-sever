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
        val internalHeight = (rows.size * rowStep + 100.0f)

        // Match the physical height to the scaled internal coordinate height
        return internalHeight * scale
    }
    override fun draw() : String {

        val sb = StringBuilder()
        //isDark = buttons.theme?.useDark == true
        val bgColor = if (isDark) "#020617" else "#f1f5f9"

        // Pro Tip: Instead of a solid rect, add an "Ambient Light Source"
        // Resolve aesthetic from Factory
        val atmosphereColor = docOpsTheme.accentColor

        sb.append("""
                <circle cx="50%" cy="50%" r="400" fill="$atmosphereColor" fill-opacity="${if (isDark) "0.03" else "0.02"}" />
            """.trimIndent())

        sb.append("""<g>""")

        var startX: Int
        var startY = 10
        rows.forEachIndexed { index, buttonsI ->
            startX = if(index == 0 || isEven(index)) {
                10
            } else {
                155
            }
            buttonsI.forEach {  button ->
                val x = startX
                val y = startY
                sb.append(createSingleHoneyComb(button, x, y, buttons.theme!!))
                startX += BUTTON_WIDTH
            }
            // Vertical step for perfect honeycomb tessellation
            startY += 255
        }
        sb.append("</g>")
        return sb.toString()
    }

    override fun defs(): String {
        val atmosphereColor = docOpsTheme.accentColor
        val gradientDefs = buttons.buttons.mapIndexed { index, button ->
            val color = button.color ?: docOpsTheme.accentColor
            """
                <linearGradient id="hexGrad_${button.id}" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stop-color="$color" />
                    <stop offset="100%" stop-color="$color" stop-opacity="0.7" />
                </linearGradient>
                """.trimIndent()
        }.joinToString("\n")
        return """
                <defs>
                <style>
                    ${docOpsTheme.fontImport}
                    /* Typography driven by ThemeFactory */
                    .hex-label {
                        font-family: ${docOpsTheme.fontFamily};
                        font-weight: 800;
                        text-transform: uppercase;
                    }
                
                    @keyframes hexEntrance {
                        from { opacity: 0; transform: scale(0.9) translateY(20px); }
                        to { opacity: 1; transform: scale(1) translateY(0); }
                    }

                    .hex-container {
                        cursor: pointer;
                        transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
                        transform-box: fill-box;
                        transform-origin: center;
                        animation: hexEntrance 0.6s ease-out backwards;
                    }
                
                    .hex-container:hover {
                        transform: scale(1.05);
                    }
               
                    /* Neon Pulse Animation for Active State using Theme Accent */
                    @keyframes neonPulse {
                        0% { filter: drop-shadow(0 0 2px $atmosphereColor); opacity: 0.9; }
                        50% { filter: drop-shadow(0 0 12px $atmosphereColor); opacity: 1; }
                        100% { filter: drop-shadow(0 0 2px $atmosphereColor); opacity: 0.9; }
                    }
                </style>
             $gradientDefs
            ${
            if (!isPdf) """
            <filter id="hexShadow_${buttons.id}" x="-20%" y="-20%" width="140%" height="140%">
                <feDropShadow dx="0" dy="8" stdDeviation="12" flood-color="${if (isDark) "rgba(0,0,0,0.5)" else "rgba(99,102,241,0.12)"}"/>
            </filter>
            """.trimIndent() else ""
        }
        </defs>
        """.trimIndent()
    }

    override fun start(): String {
        // We keep the VIEWBOX at the large coordinate size, but the WIDTH/HEIGHT at the smaller physical size
        val physicalWidth = width()
        val physicalHeight = height()

        // Calculate the internal coordinate bounds (viewbox)
        val columns = buttons.theme?.columns ?: 3
        val internalW = (columns * BUTTON_WIDTH + columns * BUTTON_PADDING)
        val internalH = (rows.size * 255 + 100.0f)

        return """
        <svg xmlns="http://www.w3.org/2000/svg" width="$physicalWidth" height="$physicalHeight" viewBox="0 0 $internalW $internalH" xmlns:xlink="http://www.w3.org/1999/xlink" id="${buttons.id}" zoomAndPan="magnify" preserveAspectRatio="xMidYMid meet">
    """.trimIndent()
    }


    override fun width(): Float {
        var columns = 3
        var scale = 1.0f
        buttons.theme?.let {
            columns = it.columns
            scale = it.scale
        }
        val internalWidth = (columns * BUTTON_WIDTH + columns * BUTTON_PADDING)

        // Match the physical width to the scaled internal coordinate width
        return internalWidth * scale
    }
    private fun createSingleHoneyComb(button: Button, x: Int, y: Int, theme: ButtonDisplay): String {
        val isDark = buttons.useDark
        val isActive = button.active
        val actualColor = button.color ?: docOpsTheme.accentColor


        // Calculate a staggered delay based on coordinates or a global index

        val primaryTextColor = docOpsTheme.primaryText
        val secondaryTextColor = docOpsTheme.secondaryText

        // Reference the gradient ID instead of the solid color
        val cardFill = "url(#hexGrad_${button.id})"

        // Sharp Accents driven by Theme
        val cardStroke = if (isDark) "#ffffff" else docOpsTheme.primaryText
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

            spans.append("""<tspan x="149" text-anchor="middle" dy="$calculatedDy" style="font-family: ${docOpsTheme.fontFamily} !important; letter-spacing: 0.5px; fill: ${docOpsTheme.primaryText} !important; $cleanUserStyle">${s.escapeXml()}</tspan>""")
        }

        var win = "_top"
        buttons.theme?.let { if (it.newWin) { win = "_blank" } }

        // Determine active status: Force check if useActiveColor is on

        val additionalClass = if (isActive) "active-neon" else ""

        var img = ""
        button.embeddedImage?.let { img = getIcon(it.ref) }

        val endY = startTextY + (textSpans.size * fontSize)
        var href = """onclick="window.open('${button.link}', '$win')" """
        if(!button.enabled) { href = "" }

        var typeText = ""
        button.type?.let { typeText = it.uppercase() }

        val delay = (x / 100 * 0.1) + (y / 100 * 0.1)

        return """
                <g transform="translate($x,$y)">
                    <g class="hex-container $additionalClass" $href style="animation-delay: ${delay}s;cursor: pointer;">
                        <title>${descriptionOrLabel(button)}</title>
            
                        <!-- Layer 1: The 'Pro' Ambient Glow (Only visible in Dark Mode) -->
                        ${if (isDark) """<circle cx="149" cy="170" r="120" fill="$actualColor" fill-opacity="0.15" filter="blur(20px)"/>""" else ""}

                        <!-- Layer 2: Hexagon Base -->
                        <polygon points="291,254 149,336 7,254 7,90 149,8 291,90" 
                                 fill="$cardFill" 
                                 stroke="$cardStroke" 
                                 stroke-opacity="$cardStrokeOpacity"
                                 stroke-width="${if(button.active) "4" else cardStrokeWidth}"
                                 ${if (!isPdf && !button.active) "filter=\"url(#hexShadow_${buttons.id})\"" else ""}/>

                        <!-- Layer 3: Glass highlight -->
                        <polygon points="149,15 280,95 149,175 18,95" 
                                 fill="white" fill-opacity="${if (isDark) "0.08" else "0.03"}" pointer-events="none"/>
                    <!-- Icon Wrapper -->
                    <g transform="translate(120,50) scale(0.8)">
                     $img 
                    </g>
            
                    <!-- Main Label -->
                        <text x="149" y="$startTextY" text-anchor="middle" 
                              fill="$primaryTextColor" 
                              class="hex-label" 
                              font-size="$fontSize"
                              style="fill: $primaryTextColor !important;">$spans</text>
            
                        <!-- Sharp Accent Line: Uses secondary accent color -->
                        <line x1="110" y1="${endY + 5}" x2="190" y2="${endY + 5}" 
                              stroke="$secondaryTextColor" stroke-width="2" stroke-linecap="round" stroke-opacity="0.8"/>
            
                        <!-- Type Text: Uses secondary accent color -->
                        <text x="149" y="${endY + 28}" text-anchor="middle" 
                              fill="$secondaryTextColor" 
                              font-family="${docOpsTheme.fontFamily}" 
                              font-size="14" 
                              font-weight="800"
                              style="letter-spacing: 2px; fill: $secondaryTextColor !important; opacity: 0.9;">$typeText</text>
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
