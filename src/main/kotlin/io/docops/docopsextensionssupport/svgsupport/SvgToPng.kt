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

package io.docops.docopsextensionssupport.svgsupport

import java.awt.Canvas
import java.awt.Font


class SvgToPng

fun String.textWidth(fontName: String, size: Int = 12): Int {
    val font =  Font(fontName,Font.PLAIN,size)
    val c = Canvas()
    val fm = c.getFontMetrics(font)
    return fm.stringWidth(this)
}

fun itemTextWidth(itemText: String, maxWidth: Int, fontSize: Int = 12, fontName: String = "Helvetica"): MutableList<String> {
    val split = itemText.split(" ")
    val itemArray = mutableListOf<String>()
    val width = itemText.textWidth(fontName, fontSize)
    if(width > maxWidth) {
        val sb = StringBuilder()
        split.forEachIndexed { index, s ->
            val itemWidth =  "$sb $s".textWidth(fontName, fontSize)
            if(itemWidth < maxWidth) {
                sb.append("$s ")
                if(index < itemArray.size - 1) {
                    sb.append(" ")
                }
            } else {
                itemArray.add("$sb")
                sb.clear()
                sb.append("$s ")
            }
        }
        if(sb.isNotEmpty()) {
            itemArray.add(sb.toString())
        }
    } else {
        itemArray.add(itemText)
    }
    return itemArray
}
