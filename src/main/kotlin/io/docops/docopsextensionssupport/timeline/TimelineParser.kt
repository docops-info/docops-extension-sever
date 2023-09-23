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

import java.util.*

/**
 * The TimelineParser class is responsible for parsing a content string and returning a list of entries.
 */
class TimelineParser {

    /**
     * Parses the content string and returns a list of entries.
     *
     * @param content The content string to be parsed.
     * @return A mutable list of Entry objects.
     */
    fun parse(content: String): MutableList<Entry> {
        val entries = mutableListOf<Entry>()
        val m = mutableMapOf<String, MutableList<String>>()
        var value = mutableListOf<String>()
        var currKey = ""

        val groups = group(content)
        groups.forEachIndexed {i, lines ->
            lines.forEach { line ->
                val key: String
                if (line.startsWith("date:") || line.startsWith("text:")) {
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