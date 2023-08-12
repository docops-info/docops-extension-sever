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
        if (buttons.buttons.size > 1) {
            return (buttons.buttons.size * BUTTON_HEIGHT + (buttons.buttons.size * 10)) * buttons.buttonDisplay.scale
        }
        val h = BUTTON_HEIGHT + 20
        return h * buttons.buttonDisplay.scale
    }

    open fun width(): Float {
        return (buttons.buttonDisplay.columns * BUTTON_WIDTH + buttons.buttonDisplay.columns * BUTTON_PADDING + buttons.buttonDisplay.columns * BUTTON_PADDING) * buttons.buttonDisplay.scale
    }

    protected fun toRows(): MutableList<MutableList<Button>> {
        val rows = mutableListOf<MutableList<Button>>()
        var rowArray = mutableListOf<Button>()
        rows.add(rowArray)
        buttons.buttons.forEach {
            if (rowArray.size == buttons.buttonDisplay.columns) {
                rowArray = mutableListOf()
                rows.add(rowArray)
            }
            rowArray.add(it)
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
}