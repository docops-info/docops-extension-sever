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

import io.docops.docopsextensionssupport.util.ParsingUtils
import io.docops.docopsextensionssupport.web.CsvResponse
import java.util.*

/**
 * Configuration data class to hold timeline configuration options.
 */
data class TimelineConfig(
    val title: String? = null,
    val theme: String? = null,
    val orientation: String? = null,
    val dateFormat: String? = null,
    val useGlass: Boolean = false,
    val useDark: Boolean = false,
    val showIndex: Boolean = true,
    val customProperties: MutableMap<String, String> = mutableMapOf(),
    val enableDetailView: Boolean = false
)

/**
 * Parse result containing both configuration and entries.
 */
data class TimelineParseResult(
    val config: TimelineConfig,
    val entries: MutableList<Entry>
)

/**
 * The TimelineParser class is responsible for parsing a content string and returning a list of entries.
 */
class TimelineParser {

    /**
     * Parses the content string and returns a TimelineParseResult with configuration and entries.
     *
     * @param content The content string to be parsed.
     * @return A TimelineParseResult containing configuration and entry data.
     */
    fun parseWithConfig(content: String): TimelineParseResult {
        val configMap = ParsingUtils.extractConfiguration(content, "key:value")
        val remainingContent = extractRemainingContent(content)
        val config = parseConfig(configMap)
        val entries = parseEntries(remainingContent)
        return TimelineParseResult(config, entries)
    }

    /**
     * Parses the content string and returns a list of entries (legacy method for backward compatibility).
     *
     * @param content The content string to be parsed.
     * @return A mutable list of Entry objects.
     */
    fun parse(content: String): TimelineParseResult {
        return parseWithConfig(content)

    }

    /**
     * Extracts the content after the config section (after the --- separator).
     */
    private fun extractRemainingContent(content: String): String {
        val separatorIndex = content.indexOf("---")
        return if (separatorIndex != -1) {
            content.substring(separatorIndex + 3).trim()
        } else {
            content
        }
    }

    /**
     * Parses the configuration map into a TimelineConfig object.
     */
    private fun parseConfig(configMap: Map<String, String>): TimelineConfig {
        if (configMap.isEmpty()) {
            return TimelineConfig()
        }

        var title: String? = null
        var theme: String? = null
        var orientation: String? = null
        var dateFormat: String? = null
        var showIndex = true
        var useGlass = false
        var useDark = false
        val customProperties = mutableMapOf<String, String>()

        configMap.forEach { (key, value) ->
            when (key.lowercase()) {
                "title" -> title = value
                "theme" -> theme = value
                "orientation" -> orientation = value
                "dateformat", "date_format" -> dateFormat = value
                "showindex", "show_index" -> showIndex = value.toBooleanStrictOrNull() ?: true
                "useglass", "use_glass" -> useGlass = value.toBooleanStrictOrNull() ?: true
                "usedark", "use_dark" -> useDark = value.toBooleanStrictOrNull() ?: true
                else -> customProperties[key] = value
            }
        }

        return TimelineConfig(title, theme, orientation, dateFormat, useGlass= useGlass ,showIndex = showIndex, useDark = useDark , customProperties =  customProperties)
    }

    /**
     * Parses the entries content (same logic as original parse method).
     */
    private fun parseEntries(content: String): MutableList<Entry> {
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

fun MutableList<Entry>.toCsv(): CsvResponse {
    val headers = listOf("Index", "Date", "Text")

    val rows = this.map { entry ->
        listOf(
            entry.index.toString(),
            entry.date,
            entry.text
        )
    }

    return CsvResponse(headers, rows)
}


fun main() {
    val contentWithConfig = """
title: My Timeline
theme: dark
orientation: vertical
dateFormat: MMM dd, yyyy
showIndex: true
customColor: #FF5733
---

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

    val result = TimelineParser().parseWithConfig(contentWithConfig)
    println("Config: ${result.config}")
    println("Entries: ${result.entries}")

    // Test backward compatibility
    val entries = TimelineParser().parse(contentWithConfig)
    println("Legacy parse result: $entries")
}