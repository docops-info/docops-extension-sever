package io.docops.docopsextensionssupport.button.shape

import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons

interface ButtonShape {

    fun drawShape(): String
}

abstract class AbstractButtonShape(val buttons: Buttons): ButtonShape {
    override fun drawShape(): String {
        return joinXmlLines(createShape())
    }

    private fun joinXmlLines(str: String): String {
        val sb = StringBuilder()
        str.lines().forEach {
            sb.append(it.trim())
        }
        return sb.toString()
    }
    abstract fun createShape(): String

    open fun height(): Float {
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

    open fun width(): Float {
        var columns = 3
        var scale = 1.0f
        buttons.buttonDisplay?.let {
            columns = it.columns
            scale = it.scale
        }
        return (columns * BUTTON_WIDTH + columns * BUTTON_PADDING + columns * BUTTON_PADDING) * scale
    }

    protected fun toRows(): MutableList<MutableList<Button>> {
        val rows = mutableListOf<MutableList<Button>>()
        var rowArray = mutableListOf<Button>()
        rows.add(rowArray)
        buttons.buttons.forEach {
            buttons.buttonDisplay?.let { disp ->
                if (rowArray.size == disp.columns) {
                    rowArray = mutableListOf()
                    rows.add(rowArray)
                }
                rowArray.add(it)
            }
        }
        return rows
    }

    fun gradient() : String {
        val sb = StringBuilder()
        buttons.buttons.forEach {
            sb.append(it.gradient)
        }
        return sb.toString()
    }
    fun gradientStyle() : String {
        val sb = StringBuilder()
        buttons.buttons.forEach {
            sb.append(it.buttonGradientStyle)
        }
        return sb.toString()
    }
    companion object {
        const val BUTTON_HEIGHT: Int = 30
        const val BUTTON_WIDTH = 300
        const val BUTTON_PADDING = 5
        const val  BUTTON_SPACING = 10
    }

    fun linesToMultiLineText(style: String?, lines: MutableList<String>, dy: Int, x: Int): String {
        val text = StringBuilder()
        lines.forEach {
            text.append("""<tspan x="$x" dy="$dy" style="$style">$it</tspan>""")
        }
        return text.toString()
    }
}