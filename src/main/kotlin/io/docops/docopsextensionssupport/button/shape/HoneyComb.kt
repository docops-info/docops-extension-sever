package io.docops.docopsextensionssupport.button.shape

import io.docops.docopsextensionssupport.adr.model.escapeXml
import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth

class HoneyComb(buttons: Buttons) : Regular(buttons) {

    companion object {
        const val BUTTON_HEIGHT: Int = 250
        const val BUTTON_WIDTH = 290
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
        rows.forEachIndexed { index, buttons ->
            startX = if(index == 0 || isEven(index)) {
                10
            } else {
                155
            }
            buttons.forEach {  button ->
                val x = startX
                val y = startY
                sb.append(createSingleHoneyCom(button, x, y))
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
    private fun createSingleHoneyCom(button: Button, x: Int, y: Int): String {
        val spans = StringBuilder()
        val fontSize = button.buttonStyle?.fontSize ?: 24
        val textSpans = itemTextWidth(itemText = button.label, maxWidth = 245, fontSize = fontSize)
        val startTextY = 187 - (textSpans.size * 12)

        textSpans.forEachIndexed { index, s ->
            var dy = 0
            if(index > 0) {
                dy=24
            }
            val fontColor = determineTextColor(button.color!!)
            spans.append("""<tspan x="145" dy="$dy" style="fill:${fontColor}; font-family:Arial,Helvetica, sans-serif;">${s.escapeXml()}</tspan>""")
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
        return """
        <g transform="translate($x,$y)" cursor="pointer">
        <title>$title</title>
        <a xlink:href="${button.link}" href="${button.link}" target="$win" style='text-decoration: none; font-family:Arial; fill: #fcfcfc;'>
        <polygon class="bar shadowed raise btn_${button.id}_cls" $btnLook points="291.73148258233545,254.80624999999998 149.60588850376178,336.86249999999995 7.480294425188106,254.80624999999998 7.480294425188077,90.69375000000005 149.60588850376175,8.637500000000017 291.7314825823354,90.69374999999994"/>
        <text x="145" y="$startTextY" text-anchor="middle" style="fill: $textColor; ${button.buttonStyle?.labelStyle}">$spans</text>
        </a>
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
