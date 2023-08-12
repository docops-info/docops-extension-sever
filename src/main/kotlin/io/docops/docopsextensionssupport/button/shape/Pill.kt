package io.docops.docopsextensionssupport.button.shape

import io.docops.asciidoc.buttons.PillButtonRenderer
import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons

class Pill(buttons: Buttons) : Regular(buttons) {
    override fun createShape(): String {
        val sb = StringBuilder()
        sb.append(start())
        sb.append(defs())
        sb.append(draw())
        sb.append(end())
        return sb.toString()
    }

    override fun draw(): String {
        var scale = 1.0f
        buttons.buttonDisplay?.let {
            scale = it.scale
        }
        val sb = StringBuilder("<g transform=\"scale($scale)\">")
        val rows = toRows()
        rows.forEachIndexed { index, buttons ->
            sb.append(drawPills(index, buttons))
        }
        sb.append("</g>")
        return sb.toString()
    }
    fun drawPills(index: Int, buttonList: MutableList<Button>): String {
        val btns = StringBuilder()
        var win = "_top"
        buttons.buttonDisplay?.let {
            if (it.newWin) {
                win = "_blank"
            }
        }

        var startX = 0

        var startY = 0
        if (index > 0) {
            startY = index * BUTTON_HEIGHT + (index * 10)
        }
        buttonList.forEach { button: Button ->

            btns.append(
                """
                <a xlink:href="${button.link}" href="${button.link}" target="$win" style="text-decoration: none;">
                <g role="button" cursor="pointer" transform="translate($startX, $startY)">
                    <rect id="button" x="5" y="5" width="$BUTTON_WIDTH" height="$BUTTON_HEIGHT" ry="26" rx="26" class="btn_${button.id}_cls" filter="url(#buttonBlur)" />
            
                    <rect id="buttongrad" x="5" y="5" width="$BUTTON_WIDTH" height="$BUTTON_HEIGHT" ry="26" rx="26" fill="url(#overlayGrad)"/>
                    <text id="label" x="150" y="43" text-anchor="middle" style="${button.buttonStyle?.labelStyle}">${button.label.escapeXml()}</text>
            
                    <rect id="buttontop" x="15" y="10.5" width="280" height="25" ry="24" rx="24" fill="url(#topshineGrad)" filter="url(#topshineBlur)"/>
                    <rect id="buttonbottom" x="25" y="50" width="260" height="7" fill="#ffffff" ry="24" rx="24" fill-opacity="0.3" filter="url(#bottomshine)"/>
                </g>
                </a>
                """
            )

            startX += BUTTON_WIDTH + BUTTON_PADDING

        }
        return btns.toString()
    }
    override fun height(): Float {
        var scale = 1.0f
        buttons.buttonDisplay?.let {
            scale = it.scale
        }
        if (buttons.buttons.size > 1) {
            return (buttons.buttons.size * BUTTON_HEIGHT + (buttons.buttons.size * 10)) * scale
        }
        return (BUTTON_HEIGHT + 20) * scale
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
    companion object {
        const val BUTTON_HEIGHT: Int = 56
        const val BUTTON_WIDTH = 300
        const val BUTTON_PADDING = 12
    }
}