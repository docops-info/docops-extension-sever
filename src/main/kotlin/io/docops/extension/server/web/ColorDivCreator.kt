package io.docops.extension.server.web

import io.docops.asciidoc.buttons.dsl.*
import io.docops.asciidoc.buttons.theme.ButtonType
import kotlin.math.floor


class ColorDivCreator {

    fun genPanels(num: Int): Panels {
        val cm = ColorMap()
        val buttons = mutableListOf<RoundButton>()
        for (x in 0..num - 1) {
            val color = getRandomColor()
            val pbtn = RoundButton()
            pbtn.link = "http://docops.io"
            pbtn.label = color
            buttons.add(pbtn)
            cm.color(color)

        }
        val p = panels {
            theme {
                colorMap = cm
                legendOn = false
                layout {
                    columns = 6
                }
            }
            buttonType = ButtonType.ROUND
            roundButtons = buttons
        }
        return p
    }

    private fun getRandomColor(): String {
        val letters = "0123456789ABCDEF"
        var color = "#"
        for (i in 0..5) {
            color += letters[floor(Math.random() * 16).toInt()]
        }
        return color
    }
}