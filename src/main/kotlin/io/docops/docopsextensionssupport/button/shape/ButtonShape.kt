/*
 * Copyright (c) 2023. The DocOps Consortium
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.docops.docopsextensionssupport.button.shape

import io.docops.docopsextensionssupport.button.Button
import io.docops.docopsextensionssupport.button.Buttons

/**
 * Represents a shape for a button.
 */
interface ButtonShape {

    fun drawShape(type: String = "SVG"): String

    fun height () : Float

    fun width () : Float
}

/**
 * AbstractButtonShape is an abstract class that implements the ButtonShape interface. It provides common functionality for drawing button shapes.
 *
 * @property buttons The Buttons instance associated with the button shape.
 * @property isPdf A flag indicating whether the shape is being drawn for a PDF.
 */
abstract class AbstractButtonShape(val buttons: Buttons): ButtonShape {
    protected var isPdf = false
    override fun drawShape(type: String): String {
        if("PDF".equals(type, true)) {
            isPdf = true
        }
        return joinXmlLines(createShape(type))
    }

    private fun joinXmlLines(str: String): String {
        val sb = StringBuilder()
        str.lines().forEach {
            sb.append(it.trim())
        }
        return sb.toString()
    }
    abstract fun createShape(type: String): String

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

    override fun width(): Float {
        var columns = 3
        var scale = 1.0f
        buttons.theme?.let {
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
            buttons.theme?.let { disp ->
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