package io.docops.docopsextensionssupport.timeline

import java.util.*

class TimelineParser {

    fun parse(content: String): MutableList<Entry> {
        val entries = mutableListOf<Entry>()
        val m = mutableMapOf<String, MutableList<String>>()
        var value = mutableListOf<String>()
        var currKey = ""

        val groups = group(content)
        groups.forEachIndexed {i, lines ->
            lines.forEach { line ->
                val key: String
                if (line.contains(":")) {
                    if (currKey.isNotEmpty()) {
                        m[currKey.uppercase()] = value
                    }
                    value = mutableListOf<String>()
                    val split = line.split(":", limit = 2)
                    key = split[0].trim()
                    currKey = key
                    value.add(split[1])
                } else {
                    value.add(line)
                }
            }
            if (!m.containsKey(currKey)) {
                m[currKey.uppercase()] = value
            }

            entries.add(Entry(date = mapDate(m), index = i+1, text = mapText(m)))

        }

        return entries
    }

    private fun group(content: String): MutableList<MutableList<String>> {
        val group = mutableListOf<MutableList<String>>()
        var newList: MutableList<String> = mutableListOf()
        content.lines().forEachIndexed { index, s ->
            if (s.trim().startsWith("-")) {
                newList = mutableListOf()
                group.add(newList)
            } else {
                newList.add(s)
            }
        }
        return group
    }

    private fun mapDate(map: MutableMap<String, MutableList<String>>): String {
        val date = map["DATE"]
        require(date != null) { "Invalid syntax date not found" }
        return date[0].trim()
    }


    private fun mapText(map: MutableMap<String, MutableList<String>>): String {
        val text = map["TEXT"]
        require(text != null) { "Invalid syntax context not found" }
        val sb = StringBuilder()
        text.forEach {
            sb.append(" ${it.trim()}")
        }
        return sb.toString()
    }
}

fun main() {
    val entry = TimelineParser().parse(
        """
-
date: July 23rd, 2023
text: DocOps extension Server releases a new feature, Timeline Maker
for asciidoctorj. With a simple text markup block you can
create very powerful timeline images. Enjoy!
-
date: August 15th, 2023
text: DocOps.io revamping website with updated documentation. All 
our work will be updated with latest documentation for Panels,
for extension server are the various plug-ing for asciidoctorj.
"""
    )
    println(entry)
}