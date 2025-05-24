/*
 * Copyright 2020 The DocOps Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.docops.docopsextensionssupport.adr



import io.docops.docopsextensionssupport.adr.model.Adr
import io.docops.docopsextensionssupport.adr.model.Status
import io.docops.docopsextensionssupport.adr.model.escapeXml
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
import java.io.File
import java.util.*

private const val SplitLength = 585

class ADRParser {

    fun parse(content: String, config: AdrParserConfig = AdrParserConfig()): Adr {
        val lines = content.lines()
        val m = mutableMapOf<String, MutableList<String>>()
        var value = mutableListOf<String>()
        val urlMap = mutableMapOf<Int, String>()
        var currKey = ""

        lines.forEach { aline ->
            val line = aline.makeUrl(urlMap, config)

            if (line.contains(":") && !line.startsWith("-") && !line.startsWith("*") && !line.startsWith("+")) {
                if (currKey.isNotEmpty()) {
                    m[currKey.uppercase()] = value
                }
                value = mutableListOf<String>()
                val split = line.split(":", limit = 2)
                val key = split[0].trim()
                currKey = key
                value.add(split[1])
            } else {
                value.add(line)
            }
        }

        if (!m.containsKey(currKey)) {
            m[currKey.uppercase()] = value
        }

        return mapToAdr(m, config, urlMap)
    }

    private fun mapToAdr(
        map: MutableMap<String, MutableList<String>>,
        config: AdrParserConfig,
        urlMap: MutableMap<Int, String>
    ): Adr {
        val title = mapTitle(map)
        val date = mapDate(map)
        val status = mapStatus(map)
        val context = processSection(map["CONTEXT"], config)
        val decision = processSection(map["DECISION"], config)
        val consequences = processSection(map["CONSEQUENCES"], config)
        val participants = mapParticipants(map, config)

        return Adr(
            title = title,
            date = date,
            status = status,
            context = context,
            decision = decision,
            consequences = consequences,
            participants = participants,
            urlMap = urlMap
        )
    }

    /**
     * Process a section of the ADR, handling empty lines and bullet points
     */
    private fun processSection(
        sectionLines: List<String>?, 
        config: AdrParserConfig
    ): MutableList<String> {
        if (sectionLines == null) return mutableListOf()

        val result = mutableListOf<String>()
        var currentParagraph = StringBuilder()
        var isFirstLineInParagraph = true

        // Process each line in the section
        for (i in sectionLines.indices) {
            val line = sectionLines[i].trim()

            // Handle empty lines as paragraph breaks
            if (line.isEmpty() || line.isBlank()) {
                if (currentParagraph.isNotEmpty()) {
                    // Process the completed paragraph
                    result.addAll(currentParagraph.toString().addLinebreaks(config.lineSize))
                    currentParagraph = StringBuilder()
                    isFirstLineInParagraph = true
                }

                // Add an empty line marker
                result.add("_nbsp;_")
                continue
            }

            // Check for bullet points at the start of lines
            if (line.startsWith("-") || line.startsWith("*") || line.startsWith("+")) {
                // If we have accumulated text, process it first
                if (currentParagraph.isNotEmpty()) {
                    result.addAll(currentParagraph.toString().addLinebreaks(config.lineSize))
                    currentParagraph = StringBuilder()
                    isFirstLineInParagraph = true
                }

                // Process the bullet point line directly
                result.addAll(line.addLinebreaks(config.lineSize))
                continue
            }

            // Regular text - append to current paragraph
            if (isFirstLineInParagraph) {
                currentParagraph.append(line)
                isFirstLineInParagraph = false
            } else {
                currentParagraph.append(" ").append(line)
            }
        }

        // Process any remaining text
        if (currentParagraph.isNotEmpty()) {
            result.addAll(currentParagraph.toString().addLinebreaks(config.lineSize))
        }

        return result
    }

    private fun mapTitle(map: MutableMap<String, MutableList<String>>): String {
        val title = map["TITLE"]
        require(title != null) { "Invalid syntax title not found" }
        return title[0].trim().escapeXml()
    }

    private fun mapDate(map: MutableMap<String, MutableList<String>>): String {
        val date = map["DATE"]
        require(date != null) { "Invalid syntax date not found" }
        return date[0].trim()
    }

    private fun mapStatus(map: MutableMap<String, MutableList<String>>): Status {
        val status = map["STATUS"]
        require(status != null) { "Invalid syntax status not found" }
        require(enumContains<Status>(status[0].trim())) {
            "$status is not a valid status not found in: ${
                Status.entries.toTypedArray().contentToString()
            }"
        }
        return Status.valueOf(status[0].trim())
    }

    private fun mapContext(map: MutableMap<String, MutableList<String>>, config: AdrParserConfig): MutableList<String> {
        val context = map["CONTEXT"]
        require(context != null) { "Invalid syntax context not found" }
        return processSection(context, config)
    }

    private fun mapDecision(
        map: MutableMap<String, MutableList<String>>,
        config: AdrParserConfig
    ): MutableList<String> {
        val decision = map["DECISION"]
        require(decision != null) { "Invalid syntax decision not found" }
        return processSection(decision, config)
    }

    private fun mapConsequences(
        map: MutableMap<String, MutableList<String>>,
        config: AdrParserConfig
    ): MutableList<String> {
        val consequences = map["CONSEQUENCES"]
        require(consequences != null) { "Invalid syntax consequences not found" }
        return processSection(consequences, config)
    }

    private fun mapParticipants(
        map: MutableMap<String, MutableList<String>>,
        config: AdrParserConfig
    ): MutableList<String> {
        val parts = map["PARTICIPANTS"]
        val list = mutableListOf<String>()
        parts?.forEach {
            list.addAll(it.addLinebreaks(config.lineSize))
        }
        return list
    }
}

/**
 * Returns `true` if enum T contains an entry with the specified name.
 */
inline fun <reified T : Enum<T>> enumContains(name: String): Boolean {
    return enumValues<T>().any { it.name == name }
}

fun String.addLinebreaks(maxLineLength: Int): MutableList<String> {
    val list = mutableListOf<String>()

    // Check if the line starts with a bullet point marker
    val trimmed = this.trim()
    val bulletPrefix = when {
        trimmed.startsWith("- ") -> "BULLET_DASH:"
        trimmed.startsWith("* ") -> "BULLET_STAR:"
        trimmed.startsWith("+ ") -> "BULLET_PLUS:"
        else -> ""
    }

    // Remove the bullet point marker for processing
    val processText = if (bulletPrefix.isNotEmpty()) {
        trimmed.substring(2).trim() // Consistently use substring(2) for all bullet types
    } else {
        this
    }

    // If the text is empty after removing the bullet, just return the bullet
    if (processText.trim().isEmpty() && bulletPrefix.isNotEmpty()) {
        list.add("$bulletPrefix")
        return list
    }

    val tok = StringTokenizer(processText, " ")
    var output = String()
    var lineLen = 0
    var isFirstLine = true

    while (tok.hasMoreTokens()) {
        val word = tok.nextToken()

        // Check if adding this word would exceed the line length
        if (lineLen + word.length > maxLineLength && lineLen > 0) {
            // Add the current line to the list
            if (isFirstLine && bulletPrefix.isNotEmpty()) {
                list.add("$bulletPrefix${output.escapeXml()}")
                isFirstLine = false
            } else {
                list.add(output.escapeXml())
            }
            output = String()
            lineLen = 0
        }

        output += ("$word ")
        lineLen += word.length + 1 // +1 for the space
    }

    // Add any remaining text
    if (output.isNotEmpty()) {
        if (isFirstLine && bulletPrefix.isNotEmpty()) {
            list.add("$bulletPrefix${output.trim().escapeXml()}")
        } else {
            list.add(output.trim().escapeXml())
        }
    }

    return list
}

fun String.makeUrl(urlMap: MutableMap<Int, String>, config: AdrParserConfig): String {
    var key = 0
    val maxEntry = urlMap.maxByOrNull { it.key }
    maxEntry?.let {
        key = it.key
    }
    var newWin = "_top"
    if(config.newWin) {
        newWin = "_blank"
    }

    var newStr = this
    if (this.contains("[[") && this.contains("]]")) {
        // Improved regex to better handle wiki-style links
        val regex = "\\[\\[(.*?)]]".toRegex()
        val matches = regex.findAll(this)
        val m = mutableMapOf<String, String>()

        matches.forEach {
            val linkContent = it.groupValues[1]

            // Split by first space to separate URL from text
            val spaceIndex = linkContent.indexOf(' ')

            if (spaceIndex > 0) {
                val url = linkContent.substring(0, spaceIndex).trim()
                val text = linkContent.substring(spaceIndex + 1).trim()

                // Create SVG link element with proper styling
                val svgLink = "<tspan><a href=\"$url\" xlink:href=\"$url\" class=\"adrlink\" target=\"$newWin\">$text</a></tspan>"
                m[svgLink] = linkContent
            } else {
                // If no space found, use the whole content as both URL and text
                val url = linkContent.trim()

                // Create SVG link element with proper styling
                val svgLink = "<tspan><a href=\"$url\" xlink:href=\"$url\" class=\"adrlink\" target=\"$newWin\">$url</a></tspan>"
                m[svgLink] = linkContent
            }
        }

        var count = key + 1
        m.forEach { (svgLink, linkContent) ->
            urlMap[count] = svgLink
            // Replace the wiki link with a placeholder that will be replaced later
            newStr = newStr.replace("[[${linkContent}]]", "_${count++}_")
        }

        // Don't escape XML here - we'll do it at the appropriate time
        return newStr
    }

    return this
}

fun generateRectPathData(width: Float, height: Float, topLetRound:Float, topRightRound:Float, bottomRightRound:Float, bottomLeftRound:Float): String {
    return """M 0 $topLetRound 
 A $topLetRound $topLetRound 0 0 1 $topLetRound 0
 L ${(width - topRightRound)} 0
 A $topRightRound $topRightRound 0 0 1 $width $topRightRound
 L $width ${(height - bottomRightRound)}
 A $bottomRightRound $bottomRightRound 0 0 1 ${(width - bottomRightRound)} $height
 L $bottomLeftRound $height
 A $bottomLeftRound $bottomLeftRound 0 0 1 0 ${(height - bottomLeftRound)}
 Z"""
}
fun main() {
    val config = AdrParserConfig(newWin = false, lineSize = 80, increaseWidthBy = 70, scale = 2.2f)
    val adr = ADRParser().parse(
        // language=text
        """
        title: Migrate Database to NoSQL Solution
status: Rejected
date: 2024-05-20
context:
- We're experiencing performance issues with our relational database
- Some of our data doesn't fit well into a relational model
- We anticipate significant growth in data volume
- We want to improve horizontal scalability
Sometime free form text

SOmetimes empty lines
sometimes wiki style likes [[https://www.apple.com Apple]]
decision:
- We will not migrate from PostgreSQL to MongoDB
- We will instead optimize our existing PostgreSQL setup
- We will implement caching strategies for performance-critical queries
- We will consider a hybrid approach for specific use cases
consequences:
- Avoid disruption of existing systems and processes
- Leverage team's existing SQL expertise
- Miss potential benefits of NoSQL for certain data patterns
- Need to invest in PostgreSQL optimization and tuning
participants:Thomas Wright (Performance Engineer), John Doe (Developer), Alice Johnson (Product Manager)
        """.trimIndent(),
        config
    )
    var svg = (AdrMaker().makeAdrSvg(adr, false, config, false))
    adr.urlMap.forEach { (t, u) ->
        svg = svg.replace("_${t}_", u)
    }
    svg = svg.replace("_nbsp;_","<tspan x=\"14\" dy=\"20\">&#160;</tspan>")
    val f = File("src/test/resources/test.svg")
    f.writeBytes(svg.toByteArray())
    println("SVG file generated at: ${f.absolutePath}")
}
