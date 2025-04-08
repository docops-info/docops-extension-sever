package io.docops.docopsextensionssupport.button.shape


import io.docops.docopsextensionssupport.adr.model.escapeXml
import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons
import io.docops.docopsextensionssupport.roadmap.wrapText

class Circle(buttons: Buttons): Regular(buttons) {

    companion object {
        const val BUTTON_HEIGHT: Int = 50
        const val BUTTON_WIDTH = 300
        const val BUTTON_PADDING = 10
        const val  BUTTON_SPACING = 10
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
        var startX = 10

        var startY = 10
        if (index > 0) {
            startY += 110
        }
        buttonList.forEach { button: Button ->
            var filter = "filter=\"url(#Bevel2)\""
            var fill = "url(#btn_${button.id})"
            if(isPdf) {
                filter = ""
                fill = "${button.color}"
            }
            val lines = wrapText(button.label.escapeXml(), 15f)
            var lineY = 0
            if(lines.size > 0) {
                lineY = lines.size * - 6
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
        <g transform="translate($startX,$startY)" filter="url(#naturalShadow)">
            $href
            <circle cx="50" cy="50" r="50" fill="$fill" class="btn_${button.id}_cls bar" $filter/>
            <text x="50" y="50" text-anchor="middle" class="glass" style="${button.buttonStyle?.labelStyle}">
            $title
            </text>
            $endAnchor
        </g>
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
            indexes = mutableListOf(0, 12)
        }
        if(lines.size == 3) {
            indexes = mutableListOf(-12, 12, 12)
        }
        if(lines.size == 4) {
            indexes = mutableListOf(-12, 12, 12,12)
        }
        lines.forEachIndexed {i , s ->
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
        return (((cols * 125)+ (cols * 5)) + (cols * 7)) * scale
    }
}