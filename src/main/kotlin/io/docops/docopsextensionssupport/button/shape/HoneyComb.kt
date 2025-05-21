package io.docops.docopsextensionssupport.button.shape

import io.docops.docopsextensionssupport.adr.model.escapeXml
import io.docops.docopsextensionssupport.badge.manipulateSVG
import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.ButtonDisplay
import io.docops.docopsextensionssupport.button.Buttons
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
import org.silentsoft.simpleicons.SimpleIcons
import java.io.ByteArrayInputStream
import java.util.Base64
import javax.xml.parsers.DocumentBuilderFactory

class HoneyComb(buttons: Buttons) : Regular(buttons) {

    companion object {
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
        return (rows.size * BUTTON_HEIGHT + (rows.size * BUTTON_PADDING) + 100.0f) * scale
    }
    override fun draw() : String {
        val sb = StringBuilder()
        if(buttons.theme?.useDark == true) {
            sb.append("""<rect width="100%" height="100%" fill="#111111" />""")
        }
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
            startY += BUTTON_HEIGHT
        }
        sb.append("</g>")
        return joinXmlLines(sb.toString())
    }

    fun head(): String {
        val w = width()
         val h = height()
        return """
            <svg xmlns="http://www.w3.org/2000/svg" width="$w" height="$h" viewBox="0 0 $w $h" xmlns:xlink="http://www.w3.org/1999/xlink" id="${buttons.id}" zoomAndPan="magnify" preserveAspectRatio="xMidYMid meet">
        """.trimIndent()
    }


    override fun width(): Float {
        var columns = 3
        var scale = 1.0f
        buttons.theme?.let {
            columns = it.columns
            scale = it.scale
        }
        return (columns * BUTTON_WIDTH + columns * BUTTON_PADDING ) * scale
    }
    private fun createSingleHoneyComb(button: Button, x: Int, y: Int, theme: ButtonDisplay): String {
        val spans = StringBuilder()
        val fontSize = button.buttonStyle?.fontSize ?: 24
        val textSpans = itemTextWidth(itemText = button.label, maxWidth = 245F, fontSize = fontSize)
        val startTextY = 187 - (textSpans.size * 12)

        textSpans.forEachIndexed { index, s ->
            var dy = 0
            if(index > 0) {
                dy=fontSize
            }
            val fontColor = determineTextColor(button.color!!)
            spans.append("""<tspan x="149" text-anchor="middle" dy="$dy" style="fill:${fontColor}; font-family:system-ui,-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Oxygen,Ubuntu,Cantarell,'Open Sans','Helvetica Neue',sans-serif; font-weight:500;">${s.escapeXml()}</tspan>""")
        }
        var win = "_top"
        buttons.theme?.let {
            if (it.newWin) {
                win = "_blank"
            }
        }
        var filter = "filter=\"url(#Bevel2)\""
        var fill = "${button.color}"
        if(!isPdf) {
            filter = ""
            fill = "url(#btn_${button.id})"
        }
        val btnLook = """fill="$fill" $filter"""
        val title = descriptionOrLabel(button)
        val textColor = determineTextColor(button.color!!)
        var img = ""
        button.embeddedImage?.let {
            img = getIcon(it.ref)
        }
        val endY = textSpans.size * fontSize + 5 + startTextY - fontSize + 10
        var href = """<a xlink:href="${button.link}" href="${button.link}" target="$win" style='text-decoration: none; font-family:Arial; fill: #fcfcfc;'>"""
        var endAnchor = "</a>"
        if(!button.enabled) {
           href = ""
            endAnchor = ""
        }
        var typeText = ""
        button.type?.let {typeText = it.uppercase()}
        var l1 = ""
        var l2 = ""
        if(theme.hexLinesEnabled) {
            val lineColor = determineTextColor(button.color!!)
            l1="""<line x1="40" y1="${startTextY - (5 + fontSize)}" x2="265" y2="${startTextY - (5+fontSize)}" style="stroke:$lineColor;stroke-width:1;stroke-opacity:0.7"/>"""
            l2 = """<line x1="40" y1="$endY" x2="265" y2="$endY" style="stroke:$lineColor;stroke-width:1;stroke-opacity:0.7"/>"""
        }
        return """
        <g transform="translate($x,$y)" cursor="pointer">
        <title>$title</title>
        $href
        <polygon stroke="${theme.strokeColor}" stroke-width="3" class="bar shadowed raise btn_${button.id}_cls" $btnLook points="291.73148258233545,254.80624999999998 149.60588850376178,336.86249999999995 7.480294425188106,254.80624999999998 7.480294425188077,90.69375000000005 149.60588850376175,8.637500000000017 291.7314825823354,90.69374999999994" rx="5" ry="5" filter="drop-shadow(3px 3px 3px rgba(0,0,0,0.2))"/>
        <g transform="translate(125,50) scale(1.0)">
         $img 
        </g>
        <text x="149" y="$startTextY" text-anchor="middle" style="fill: $textColor; ${button.buttonStyle?.labelStyle}">$spans</text>
        $endAnchor
        $l1
        $l2
        <text x="149" y="${endY+24}" text-anchor="middle" style="fill: $textColor; ${button.buttonStyle?.typeStyle}; font-family:system-ui,-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Oxygen,Ubuntu,Cantarell,'Open Sans','Helvetica Neue',sans-serif; font-weight: 600; letter-spacing: 2px; font-size: 0.9em;">$typeText</text>
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
        var logo = icon
        val simpleIcon = SimpleIcons.get(icon.replace("<", "").replace(">", ""))
        if (simpleIcon != null) {
            val ico = simpleIcon.svg
            if(ico.isNotBlank()) {
                return """
                    <svg width="128" height="128" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="${simpleIcon.path}" fill="#${simpleIcon.hex}" filter="url(#Bevel2)"/>
                    </svg>
                """.trimIndent()

            }
        }
        return """
            <svg width="64" height="64" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg" filter="url(#Bevel2)">
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
