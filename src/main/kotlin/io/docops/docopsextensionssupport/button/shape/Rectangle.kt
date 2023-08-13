package io.docops.docopsextensionssupport.button.shape

import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons
import io.docops.docopsextensionssupport.button.Link

class Rectangle(buttons: Buttons) : Regular(buttons) {
    override fun createShape(): String {
        val sb = StringBuilder()
        sb.append(start())
        sb.append(defs())
        sb.append(draw())
        sb.append(end())
        return sb.toString()
    }

    override fun draw() : String{
        var scale = 1.0f
        buttons.buttonDisplay?.let {
            scale = it.scale
        }
        val sb = StringBuilder("<g transform=\"scale($scale)\">")
        val rows = toRows()
        var count = 0
        rows.forEachIndexed { index, buttons ->

            sb.append(drawButtonInternal(index, buttons, count))
            count += buttons.size
            println(buttons.size)
        }
        sb.append("</g>")
        return sb.toString()
    }
    fun drawButtonInternal(index: Int, buttonList: MutableList<Button>, count: Int): String {
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
        var localCount = count
        buttonList.forEach { button: Button ->
            localCount++
            btns.append(
                """
        <g transform="translate($startX,$startY)" cursor="pointer" filter="url(#Bevel2)">
            <rect x="0" y="0" width="310" filter="url(#Bevel2)" stroke="#b2b2b2" class="glass" height="120" rx="15"
              ry="15" fill="#f0f0f0" fill-opacity='0.3'/>
            <a xlink:href="${button.link}" class="linkText" target="$win">
            <text x="115" y="16" class="glass" style="${button.buttonStyle?.labelStyle}">${button.label.escapeXml()}</text>
            </a>
            <a xlink:href="${button.link}" class="linkText" target="$win">
            <rect x="10" y="10" height="98" width="98"
                  class="mybox shape btn_${button.id}_cls" rx="18" ry="18" fill="#45618E"/>
            </a>
            <g class="glass" transform="translate(10,10)">
            <text x="49" y="68" text-anchor="middle" alignment-baseline="central"
                  font-family="Helvetica, sans-serif" font-size="60px" filter="url(#Bevel2)">
                <a xlink:href="${button.link}" target="$win" fill="#000000">$localCount</a>
            </text>
            </g>
            ${linksToText(button.links)}
        </g>
        """.trimIndent()
            )

            startX += BUTTON_WIDTH + BUTTON_PADDING

        }
        return btns.toString()
    }

    private fun linksToText(links: MutableList<Link>?): String {
        val sb = StringBuilder("""<text x="115" y="20">""")
        links?.let {
            it.forEach { link ->
                sb.append("""
            <tspan x="115" dy="14">
                <a xlink:href="${link.href}" class="linkText" target="_blank">${link.label.escapeXml()}</a>
            </tspan>
                """.trimIndent())
            }
        }
       sb.append("</text>")
        return sb.toString()
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
        const val BUTTON_HEIGHT: Int = 120
        const val BUTTON_WIDTH = 310
        const val BUTTON_PADDING = 10
        const val  BUTTON_SPACING = 10
    }
}