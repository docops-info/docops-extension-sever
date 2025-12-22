package io.docops.docopsextensionssupport.button.shape

import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.ButtonDisplay
import io.docops.docopsextensionssupport.button.Buttons
import io.docops.docopsextensionssupport.button.parseStyleForFontSize
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
import org.silentsoft.simpleicons.SimpleIcons

class Hex(buttons: Buttons) : Regular(buttons) {

    private var isDark = buttons.theme?.useDark == true
    companion object Companion {
        const val BUTTON_HEIGHT: Int = 255
        const val BUTTON_WIDTH = 295
        const val BUTTON_PADDING = 10
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

        // Return a smaller physical height to match the original "shrink" behavior (approx 56% of internal)
        return (internalHeight * 0.56f) * scale
    }
    override fun draw() : String {
        val sb = StringBuilder()
        //isDark = buttons.theme?.useDark == true
        val bgColor = if (isDark) "#020617" else "#f1f5f9"

        sb.append("""<rect width="100%" height="100%" fill="$bgColor" />""")

        // Add atmospheric background element
        val atmosphereColor = if (isDark) "#38bdf8" else "#818cf8"
        sb.append("""<circle cx="90%" cy="10%" r="250" fill="$atmosphereColor" fill-opacity="0.05" />""")

        sb.append("""<g transform="scale(${buttons.theme?.scale})">""")

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
        return """
            <defs>
            <style>
                @import url('https://fonts.googleapis.com/css2?family=Syne:wght@800&amp;display=swap');
                .hex-container {
                    cursor: pointer;
                    transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
                    transform-box: fill-box;
                    transform-origin: center;
                }
               
                .hex-label {
                    font-family: 'Syne', sans-serif;
                    font-weight: 800;
                    text-transform: uppercase;
                }
                
                /* Neon Pulse Animation for Active State */
                @keyframes neonPulse {
                    0% { filter: drop-shadow(0 0 2px ${if (isDark) "#38bdf8" else "#6366f1"}); opacity: 0.9; }
                    50% { filter: drop-shadow(0 0 12px ${if (isDark) "#38bdf8" else "#6366f1"}); opacity: 1; }
                    100% { filter: drop-shadow(0 0 2px ${if (isDark) "#38bdf8" else "#6366f1"}); opacity: 0.9; }
                }
                
                
            </style>
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

        // Return a smaller physical width to match original behavior (approx 56% of internal)
        return (internalWidth * 0.56f) * scale
    }
    private fun createSingleHoneyComb(button: Button, x: Int, y: Int, theme: ButtonDisplay): String {
        val isDark = theme.useDark
        val primaryTextColor = if (isDark) "#f8fafc" else "#1e1b4b"
        val secondaryTextColor = if (isDark) "#38bdf8" else "#6366f1"
        // Design Decision: In light mode, use a subtle tint of the user color for the fill
        // instead of the raw saturated color. This keeps it professional and "designed".
        val cardFill = if (isDark) {
            "rgba(30, 41, 59, 0.9)"
        } else {
            // Apply a very light tint of the user color (10% opacity) or stay white
            "#ffffff"
        }
        val isActive = button.enabled
        val actualColor = button.color!!

        val cardStroke = actualColor
        val cardStrokeWidth = if (isDark) "1" else "2"


        // Strip conflicting fill from user style
        val cleanUserStyle = button.buttonStyle?.labelStyle?.replace(Regex("fill\\s*:\\s*[^;]+;?"), "") ?: ""

        val spans = StringBuilder()
        val fontSize = button.buttonStyle?.labelStyle?.let { style ->
            parseStyleForFontSize(style, button.buttonStyle?.fontSize ?: 16)
        } ?: button.buttonStyle?.fontSize ?: 16 // Defaulting to 16px


        // Syne is wide, so we treat our 295px wide button as if it only has 170px
        // worth of "Helvetica space" to force the wrap early enough.
        val adjustedMaxWidth = 140F
        val textSpans = itemTextWidth(itemText = button.label, maxWidth = adjustedMaxWidth, fontSize = fontSize)

        // Centering logic remains the same
        val lineSpacing = 4
        val totalTextHeight = (textSpans.size * fontSize) + ((textSpans.size - 1) * lineSpacing)
        val startTextY = 185 - (totalTextHeight / 2)

        textSpans.forEachIndexed { index, s ->
            val calculatedDy = if (index > 0) fontSize + lineSpacing else 0
            // Added letter-spacing for that "Designed" look
            spans.append("""<tspan x="149" text-anchor="middle" dy="$calculatedDy" style="font-family: 'Syne', sans-serif !important; letter-spacing: 0.5px; $cleanUserStyle">${s.escapeXml()}</tspan>""")
        }

        var win = "_top"
        buttons.theme?.let { if (it.newWin) { win = "_blank" } }

        // Determine active status: Force check if useActiveColor is on
        
        val additionalClass = if (isActive) "active-neon" else ""

        var img = ""
        button.embeddedImage?.let { img = getIcon(it.ref) }

        val endY = startTextY + (textSpans.size * fontSize)
        var href = """onclick="window.open('${button.link}', '$win')" style="cursor: pointer;""""
        if(!button.enabled) { href = "" }

        var typeText = ""
        button.type?.let { typeText = it.uppercase() }

        return """
    <g transform="translate($x,$y)" class="hex-container $additionalClass" $href>
        <title>${descriptionOrLabel(button)}</title>
        
        <!-- Hexagon Base: Light Mode uses the user color for the stroke -->
        <polygon points="291,254 149,336 7,254 7,90 149,8 291,90" 
                 fill="$cardFill" 
                 stroke="$cardStroke" 
                 stroke-width="${if(button.active) "4" else cardStrokeWidth}"
                 ${if (!isPdf && !button.active) "filter=\"url(#hexShadow_${buttons.id})\"" else ""}/>

        <!-- Icon Wrapper: Optionally tint with user color in light mode -->
        <g transform="translate(120,50) scale(0.8)">
         $img 
        </g>
        
        <!-- Main Label with Syne and override protection -->
        <text x="149" y="$startTextY" text-anchor="middle" 
              fill="$primaryTextColor" 
              class="hex-label" 
              font-size="$fontSize"
              style="fill: $primaryTextColor !important;">$spans</text>
        
        <!-- Sharp Accent Line -->
        <line x1="110" y1="${endY + 5}" x2="190" y2="${endY + 5}" 
              stroke="$actualColor" stroke-width="3" stroke-linecap="round" stroke-opacity="0.8"/>
        
        <!-- Type Text -->
        <text x="149" y="${endY + 28}" text-anchor="middle" 
              fill="$secondaryTextColor" 
              font-family="'Syne', sans-serif" 
              font-size="10" 
              font-weight="800"
              style="letter-spacing: 2px;">$typeText</text>
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
