package io.docops.docopsextensionssupport.button.shape

import io.docops.docopsextensionssupport.adr.model.escapeXml
import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth


class Oval(buttons: Buttons) : Regular(buttons) {

    override fun draw(): String {
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        var back =""
        if(buttons.useDark) {
            back = "<rect width=\"100%\" height=\"100%\" fill=\"#1f2937\"/>"
        }
        val sb = StringBuilder("""
            $back
            <g transform="scale($scale)">
            """)
        val rows = toRows()
        var count = 0
        rows.forEachIndexed { index, buttons ->

            sb.append(drawButtonInternal(index, buttons, count))
            count += buttons.size
        }
        sb.append("</g>")
        return sb.toString()
    }

    private fun drawButtonInternal(index: Int, buttonList: MutableList<Button>, count: Int): Any {

        val btns = StringBuilder()
        var win = "_top"
        buttons.theme?.let {
            if (it.newWin) {
                win = "_blank"
            }
        }
        var startX = 10

        var startY = 10
        if (index > 0) {
            startY = index * BUTTON_HEIGHT + (index * BUTTON_PADDING) + BUTTON_SPACING
        }
        var background = "#fcfcfc"
        if(buttons.useDark) {
            background = "none"
        }
        buttonList.forEach { button: Button ->
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
                var fill = "url(#btn_${button.id})"
                if(isPdf) {
                    fill = "${button.color}"
                }
                tspan.append("""<tspan x="125" dy="$dy" style="fill:$fill;">${s.escapeXml()}</tspan>""")
            }
            var href = """<a xlink:href="${button.link}" href="${button.link}" xlink:show="new" xlink:type="simple" target="$win" style="text-decoration: none; font-family:Arial; fill: #fcfcfc;">"""
            var endAnchor = "</a>"
            if(!button.enabled) {
                href = ""
                endAnchor = ""
            }
            btns.append("""
                <g transform="translate($startX,$startY)">
                <rect class="bar" width="250" height="90" ry="36" rx="36" fill="$background" stroke="url(#btn_${button.id})" stroke-width='5' filter="url(#Bevel2)"/>
            $href
            <text x="135" y="0" text-anchor="middle" class="filtered" style="font-size: 24px; font-family: Helvetica, Arial, sans-serif; font-weight: bold; fill:url(#btn_${button.id});" lengthAdjust="spacing">
                $tspan
            </text>
            $endAnchor
    </g>
            """.trimIndent())

            startX += BUTTON_WIDTH + BUTTON_PADDING + 5
        }
        return btns.toString()
    }

    override fun width(): Float {
        var columns = 3
        var scale = 1.0f
        buttons.theme?.let {
            columns = it.columns
            scale = it.scale
        }
        return (columns * BUTTON_WIDTH + columns * BUTTON_PADDING + columns * BUTTON_PADDING) * scale
    }

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
    companion object {
        const val BUTTON_HEIGHT: Int = 90
        const val BUTTON_WIDTH = 250
        const val BUTTON_PADDING = 10
    }
}