package io.docops.docopsextensionssupport.timeline

import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.roadmap.linesToMultiLineText
import io.docops.docopsextensionssupport.roadmap.linesToUrlIfExist
import io.docops.docopsextensionssupport.roadmap.wrapText

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
    var s = text.escapeXml()
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
    var text = """<text x="$x" y="$y" class="$clazz" >"""
    val lines = linesToUrlIfExist(wrapText(s, numChars), urlMap)
    val spans = linesToMultiLineText(lines,dy, x, fillColor)
    text += spans
    text += "</text>"
    return text
}