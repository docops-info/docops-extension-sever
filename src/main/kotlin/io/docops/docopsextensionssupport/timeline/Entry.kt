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

package io.docops.docopsextensionssupport.timeline

import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.roadmap.linesToMultiLineText
import io.docops.docopsextensionssupport.roadmap.linesToUrlIfExist
import io.docops.docopsextensionssupport.roadmap.wrapText
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth

/**
 * Represents an entry with a specific date, index, and text.
 *
 * @property date The date of the entry.
 * @property index The index of the entry.
 * @property text The text of the entry.
 */
class Entry (val date: String, val index: Int, val text: String)

/**
 * Converts the given Entry to a text with spans.
 *
 * @param numChars The maximum number of characters per line.
 * @param x The x-coordinate for the text.
 * @param y The y-coordinate for the text.
 * @param clazz The CSS class for the text.
 * @param dy The line height adjustment for the text.
 * @param fillColor The fill color for the text.
 * @return The HTML text string with spans.
 */
fun Entry.toTextWithSpan(numChars: Float, x: Int, y: Int, clazz: String, dy: Int, fillColor: String): String {
    val urlMap = mutableMapOf<String,String>()
    var s = text
    if(text.contains("[[") && text.contains("]]")) {
        val regex = "(?<=\\[\\[)(.*?)(?=]])".toRegex()
        val matches = regex.findAll(s)
        matches.forEach {
                item ->
            val urlItem = item.value.split(" ")
            val url = urlItem[0]
            val display = urlItem[1]
            s = s.replace("[[${item.value}]]", "[[${display}]]")
            urlMap["[[${display}]]"] = url
        }
    }
    var text = """<text x="$x" y="$y" font-size="14px" font-family="Arial, Helvetica, sans-serif" fill='$fillColor'>"""

    //val lines = linesToUrlIfExist(wrapText(s, numChars), urlMap)
    val itemArray = itemTextWidth(s, 220, 14, "Arial")
    val lines = linesToUrlIfExist(itemArray, urlMap)
    println(lines)
    val spans = linesToMultiLineText(lines,dy, x, fillColor)
    text += spans
    text += "</text>"
    return text
}