package io.docops.docopsextensionssupport.button.shape



import io.docops.asciidoc.utils.addLinebreaks
import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons

class Large(buttons: Buttons) : Regular(buttons) {
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
        var strokeColor = "gold"
        buttons.buttonDisplay?.let {
            if (it.newWin) {
                win = "_blank"
            }
            strokeColor = it.strokeColor
        }
        var startX = 10

        var startY = 10
        if (index > 0) {
            startY = index * 410 + 10
        }
        buttonList.forEach { button: Button ->
            btns.append(
                """
                <a xlink:href="${button.link}" href="${button.link}" target="$win" style="text-decoration: none;">
                <g transform="translate($startX,$startY)" class="basecard btn_${button.id}_cls">
                <use xlink:href="#outerBox" stroke="$strokeColor"><title class="description">${button.label.escapeXml()}</title></use>
                <use xlink:href="#topTextBox"/>
                ${determineLineText(button)}
                ${drawText(button)}
                </g>
                </a>
                """.trimIndent()
            )

            startX += 300 + BUTTON_PADDING

        }
        return btns.toString()
    }
    private fun determineLineText(button: Button): String {
        if(button.embeddedImage != null) {
            val img = button.embeddedImage
            return """<image x="0" y="0" width="300" height="191" xlink:href="${img?.ref}" href="${img?.ref}" clip-path="inset(1px round 18px 18px 0px 0px)"/>"""
        }
        else if ((button.cardLine1 == null) || (button.cardLine2 == null)) {
            return """<use xlink:href="#singleBox"  fill="url(#btn_${button.id})"/>
            """.trimMargin()
        } else {
            return """
            <text text-anchor="middle" x="150" y="67.75" filter="url(#Bevel2)"
                style="fill: ${button.color}; font: bold ${button.cardLine1.size} Arial, Helvetica, sans-serif;">${button.cardLine1.line.escapeXml()}
            </text>
            <g transform="translate(0,95.5)">
            <use xlink:href="#bottomTextBox" stroke="${button.color}" fill="url(#btn_${button.id})"/>

            <text text-anchor="middle" x="150" y="67.75" filter="url(#Bevel2)"
                  style="fill: #ffffff; font: bold ${button.cardLine2.size} Arial, Helvetica, sans-serif;" >${button.cardLine2.line.escapeXml()}
            </text>
        </g>
            """.trimIndent()
        }
    }

    private fun drawText(button: Button): String {
        var desc = mutableListOf<StringBuilder>()
        button.description?.let {
            desc = addLinebreaks(it, 35)
        }
        val descList = StringBuilder()
        desc.forEach {
            descList.append("""<tspan x="10" dy="14" style="${button.buttonStyle?.descriptionStyle}">${it.toString().escapeXml()}</tspan>""")
        }
        val authors = StringBuilder()
        button.author?.let {
            it.forEach { txt ->
                authors.append("""<tspan x="10" dy="14" style="${button.buttonStyle?.authorStyle}">${txt.escapeXml()}</tspan>""")
            }
        }
        var title  = mutableListOf<StringBuilder>()
        button.type?.let {
            title = addLinebreaks(it, 40)
        }
        val titleList = StringBuilder()
        title.forEach {
            titleList.append("""<tspan x="10" dy="14" style="${button.buttonStyle?.typeStyle}" fill="${button.color}">${it.toString().escapeXml()}</tspan>""")
        }
        var dt = ""
        button.date?.let {
            dt = "<tspan x=\"10\" dy=\"14\" style=\"${button.buttonStyle?.dateStyle}\">${it}</tspan>"
        }
        return """
            <g transform="translate(0,190)" class="title">
            <text x="10" y="20" style="font: 12px Arial, Helvetica, sans-serif;">
                <tspan style="${button.buttonStyle?.labelStyle}">${button.label.escapeXml()}</tspan>
                $titleList
                $descList
                $authors
                $dt
            </text>
        </g>
        """.trimIndent()
    }
    override fun start() : String {
        var scale = 1.0f
        val height= height()
        val width = width()
        buttons.buttonDisplay?.let { 
            scale = it.scale
        }
        return """<svg xmlns="http://www.w3.org/2000/svg" width="${width * scale}" height="${height * scale}"
     viewBox="0 0 ${width * scale} ${height * scale}" xmlns:xlink="http://www.w3.org/1999/xlink">"""
    }
    override fun height(): Float {
        if (buttons.buttons.size > 1) {
            return buttons.buttons.size * BUTTON_HEIGHT + 20 + 0.0f

        }
        return BUTTON_HEIGHT + 20 + 0.0f
    }

    override fun width(): Float {
        var columns = 3
        buttons.buttonDisplay?.let { 
            columns = it.columns
        }
        return columns * BUTTON_WIDTH + columns * BUTTON_PADDING + columns * BUTTON_PADDING + 0.0f
    }
    companion object {
        const val BUTTON_HEIGHT: Int = 410
        const val BUTTON_WIDTH = 300
        const val BUTTON_PADDING = 12
    }
}