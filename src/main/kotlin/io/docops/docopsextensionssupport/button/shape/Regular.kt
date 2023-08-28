package io.docops.docopsextensionssupport.button.shape


import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons

open class Regular(buttons: Buttons) : AbstractButtonShape(buttons) {

    override fun createShape(type: String): String {
        val sb = StringBuilder()
        sb.append(start())
        sb.append(defs())
        sb.append(draw())
        sb.append(end())
        return sb.toString()
    }
    open fun draw() : String{
        var scale = 1.0f
        buttons.theme?.let {
            scale = it.scale
        }
        val sb = StringBuilder("<g transform=\"scale($scale)\">")
        val rows = toRows()
        rows.forEachIndexed { index, buttons ->
            sb.append(drawButton(index, buttons))
        }
        sb.append("</g>")
        return sb.toString()
    }
    open fun drawButton(index: Int, buttonList: MutableList<Button>): String {
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
        buttonList.forEach { button: Button ->
            var filter = "url(#Bevel2)"
            if(isPdf) {
                filter = ""
            }
            btns.append(
                """
        <g transform="translate($startX,$startY)" cursor="pointer">
            <a xlink:href="${button.link}" target="$win" style='text-decoration: none; font-family:Arial; fill: #fcfcfc;'>
            <rect x="0" y="0" fill="${button.color}" width="300" height="30" class="raise btn_${button.id}_cls" filter="$filter" rx="10" ry="10"/>
            <text x="150" y="20" text-anchor="middle" class="glass" style="${button.buttonStyle?.labelStyle}">${button.label.escapeXml()}</text>
            </a>
        </g>
        """.trimIndent()
            )

            startX += BUTTON_WIDTH + BUTTON_PADDING

        }
        return btns.toString()
    }
    protected open fun start() : String {
        val height= height()
        val width = width()
        return """<svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height" xmlns:xlink="http://www.w3.org/1999/xlink" id="${buttons.id}">"""
    }

    protected fun end() = """</svg>"""
    protected fun defs() : String{
        var strokeColor: String = "gold"
        buttons.theme?.let {
            strokeColor = it.strokeColor
        }
        return """
            <defs>
            ${filters()}
            ${gradient()}
            ${uses()}
            <style>
            ${glass()}
            ${raise(strokeColor = strokeColor)}
            ${baseCard()}
            ${gradientStyle()}
            ${myBox()}
            ${keyFrame()}
            ${linkText()}
            </style>
            </defs>
        """.trimIndent()
    }

}
