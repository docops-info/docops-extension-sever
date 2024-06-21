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

package io.docops.docopsextensionssupport.roadmap

import kotlin.math.max


/**
 * The RoadMapParser class is responsible for parsing the content of a road map and
 * grouping the items into different categories.
 */
class RoadMapParser {

    /**
     * Parses the given content and returns a RoadMaps object.
     *
     * @param content the content to parse
     * @return a RoadMaps object representing the parsed content
     */
    fun parse(content: String): RoadMaps {
        return group(content = content)

    }

    private fun group(content: String): RoadMaps {
        val nowTitle = mutableMapOf<Int, String>()
        val nextTitle = mutableMapOf<Int, String>()
        val laterTitle = mutableMapOf<Int, String>()
        val doneTitle = mutableMapOf<Int, String>()
        val now = mutableListOf<MutableList<String>>()
        val next = mutableListOf<MutableList<String>>()
        val later = mutableListOf<MutableList<String>>()
        val done = mutableListOf<MutableList<String>>()
        var newList: MutableList<String> = mutableListOf()
        val urlMap = mutableMapOf<String,String>()
        var sCounter = 0
        var nextCounter = 0
        var laterCounter = 0
        var doneCounter = 0
        content.lines().forEachIndexed { index, input ->
            var s = input
            if(input.contains("[[") && input.contains("]]")) {
                val regex = "(?<=\\[\\[)(.*?)(?=]])".toRegex()
                val matches = regex.findAll(s)
                matches.forEach {
                    item ->
                    val urlItem = item.value.split(" ")
                    val url = urlItem[0]
                    val display = urlItem[1]
                    s = input.replace("[[${item.value}]]", "[[${display}]]")
                    urlMap["[[${display}]]"] = url

                }
            }
            if (s.trim().startsWith("- now")) {
                val t = s.split("- now")
                if(t.size ==2) {
                    nowTitle[sCounter++] = t[1]
                }
                newList = mutableListOf()
                now.add(newList)
            } else if(s.trim().startsWith("- next")) {
                val t = s.split("- next")
                if(t.size ==2) {
                    nextTitle[nextCounter++] = t[1]
                }
                newList = mutableListOf()
                next.add(newList)
            }
            else if(s.trim().startsWith("- later")) {
                val t = s.split("- later")
                if(t.size ==2) {
                    laterTitle[laterCounter++] = t[1]
                }
                newList = mutableListOf()
                later.add(newList)
            }
            else if(s.trim().startsWith("- done")) {
                val t = s.split("- done")
                if(t.size ==2) {
                    doneTitle[doneCounter++] = t[1]
                }
                newList = mutableListOf()
                done.add(newList)
            }
            else {
                newList.add(s)
            }
        }
        return RoadMaps(
            now = now,
            next = next,
            later = later,
            done=done,
            urlMap= urlMap, nowTitle = nowTitle,
            nextTitle = nextTitle,
            laterTitle = laterTitle,
            doneTitle = doneTitle)
    }
}
data class RoadMaps(
    val now: MutableList<MutableList<String>>,
    val next: MutableList<MutableList<String>>,
    val later: MutableList<MutableList<String>>,
    val urlMap: MutableMap<String, String>,
    val done: MutableList<MutableList<String>>,
    val nowTitle: MutableMap<Int, String>,
    val nextTitle: MutableMap<Int, String>,
    val laterTitle: MutableMap<Int, String>,
    val doneTitle: MutableMap<Int, String>,
) {
    fun nowList(): MutableList<String> {
        val list = mutableListOf<String>()
        now.forEach {
            list.addAll(it)
        }
        return list
    }
    fun nextList() : MutableList<String> {
        val list = mutableListOf<String>()
        next.forEach { list.addAll(it) }
        return list
    }
    fun laterList() : MutableList<String> {
        val list = mutableListOf<String>()
        later.forEach { list.addAll(it) }
        return list
    }
    fun doneList() : MutableList<String> {
        val list = mutableListOf<String>()
        done.forEach { list.addAll(it) }
        return list
    }
    fun maxListSize() : Int {
        return max(doneList().size,max(nowList().size, max(nextList().size,laterList().size)))
    }
}

fun RoadMaps.maxLength() : Int {
    return max(now.size, max(next.size,later.size))
}