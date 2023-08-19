package io.docops.docopsextensionssupport.timeline

import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.roadmap.linesToMultiLineText
import io.docops.docopsextensionssupport.roadmap.linesToUrlIfExist
import io.docops.docopsextensionssupport.roadmap.wrapText

class Entry (val date: String, val index: Int, val text: String)

fun Entry.toTextWithSpan(numChars: Float, x: Int, y: Int, clazz: String, dy: Int): String {
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
    val spans = linesToMultiLineText(lines,dy, x)
    text += spans
    text += "</text>"
    return text
}