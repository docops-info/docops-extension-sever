package io.docops.docopsextensionssupport.button.shape


import io.docops.asciidoc.buttons.wrapText
import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons

class Slim(buttons: Buttons) : Regular(buttons) {

    override fun createShape(): String {
        val sb = StringBuilder()
        sb.append(start())
        sb.append(defs())
        sb.append(draw())
        sb.append(end())
        return sb.toString()
    }
    override fun drawButton(index: Int, buttonList: MutableList<Button>): String {
        val btns = StringBuilder()
        var win = "_top"
        buttons.buttonDisplay?.let {
            if (it.newWin) {
                win = "_blank"
            }
        }
        var startX = 10

        var startY = 10
        if (index > 0) {
            startY = index * BUTTON_HEIGHT + (index * BUTTON_PADDING) + BUTTON_SPACING
        }

        buttonList.forEach { button ->
            var lines = ""
            button.description?.let {
                lines = linesToMultiLineText(button.buttonStyle?.descriptionStyle, wrapText(it.escapeXml(), 30f), 10, 2)
            }
            val title = linesToMultiLineText(button.buttonStyle?.labelStyle,wrapText(button.label.escapeXml(), 15f), 12, 75)
            var btnDate = ""
            button.date?.let {
                btnDate = it
            }
            var authors = ""
            button.author?.let {
                authors = authorsToTSpans(it, "145", button.buttonStyle?.authorStyle)
            }
            btns.append("""
         <g transform="translate($startX,$startY)" cursor="pointer">
        <a xlink:href="${button.link}" target="$win" style='text-decoration: none; font-family:Arial; fill: #fcfcfc;'>
        <rect x="0" y="0" fill="#fcfcfc" width="$BUTTON_HEIGHT" height="$BUTTON_HEIGHT" rx="5" ry="5"  stroke="#000000" class="btn_${button.id}_cls raise">
            <title>${button.label.escapeXml()}</title>
        </rect>
        <path filter="url(#buttonBlur)" class="btn_${button.id}_cls"  d="M 0 5.0 A 5.0 5.0 0 0 1 5.0 0 L 145.0 0 A 5.0 5.0 0 0 1 150.0 5.0 L 150.0 35.0 A 0.0 0.0 0 0 1 150.0 35.0 L 0.0 35.0 A 0.0 0.0 0 0 1 0 35.0 Z"/>
        <path fill="url(#overlayGrad)" class="btn_${button.id}_cls"  d="M 0 5.0 A 5.0 5.0 0 0 1 5.0 0 L 145.0 0 A 5.0 5.0 0 0 1 150.0 5.0 L 150.0 35.0 A 0.0 0.0 0 0 1 150.0 35.0 L 0.0 35.0 A 0.0 0.0 0 0 1 0 35.0 Z"/>
        <text text-anchor="middle" x="75" y="8" class="glass">
            $title
        </text>
         <text x="0" y="38" >
            $lines
        </text>
        <path class="btn_${button.id}_cls" transform="translate(0,125)" d="M 0 0.0 A 0.0 0.0 0 0 1 0.0 0 L 150.0 0 A 0.0 0.0 0 0 1 150.0 0.0 L 150.0 20.0 A 5.0 5.0 0 0 1 144.0 25.0 L 5.0 25.0 A 5.0 5.0 0 0 1 0 20.0 Z"/>
        <text x="145" y="135" text-anchor="end">${authors}</text>
        <text x="145" y="145" style="${button.buttonStyle?.dateStyle}" text-anchor="end">${btnDate}</text>
        </a>
        </g>
            """.trimIndent())
            startX += BUTTON_WIDTH + BUTTON_PADDING
        }
        return btns.toString()
    }
    override fun height(): Float {
        val size = toRows().size
        var scale = 1.0f
        buttons.buttonDisplay?.let {
            scale = it.scale
        }
        if (size > 1) {
            return (size * BUTTON_HEIGHT + (size * 10)) * scale + 10
        }
        val h = BUTTON_HEIGHT + 30
        return h * scale
    }

    override fun width(): Float {
        var columns = 3
        var scale = 1.0f
        buttons.buttonDisplay?.let {
            columns = it.columns
            scale = it.scale
        }
        return (columns * BUTTON_WIDTH + columns * BUTTON_PADDING + columns * BUTTON_PADDING) * scale
    }

    fun authorsToTSpans(authors: List<String>, x: String, style: String?): String {
        val s = StringBuilder()
        authors.forEach {
            s.append("""
                <tspan x="$x" dy="-12" style="$style">$it</tspan>
            """.trimIndent())
        }
        return s.toString()
    }
    companion object {
        const val BUTTON_HEIGHT = 150
        const val BUTTON_WIDTH = 150
        const val BUTTON_PADDING = 10
    }
}