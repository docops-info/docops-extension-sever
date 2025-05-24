package io.docops.docopsextensionssupport.adr

import java.io.File
import kotlin.collections.filter
import kotlin.collections.find
import kotlin.collections.isNotEmpty
import kotlin.collections.joinToString
import kotlin.collections.map
import kotlin.io.readText
import kotlin.sequences.forEach
import kotlin.text.equals
import kotlin.text.indexOf
import kotlin.text.isEmpty
import kotlin.text.isNotEmpty
import kotlin.text.lines
import kotlin.text.split
import kotlin.text.startsWith
import kotlin.text.substring
import kotlin.text.substringAfter
import kotlin.text.toRegex
import kotlin.text.trim

/**
 * Enum representing the possible statuses of an Architecture Decision Record.
 */
enum class AdrStatus {
    Proposed,
    Accepted,
    Superseded,
    Deprecated,
    Rejected;

    companion object {
        /**
         * Parse a string into an AdrStatus enum value.
         * Case-insensitive matching is used.
         *
         * @param value The string value to parse
         * @return The corresponding AdrStatus enum value, or null if no match is found
         */
        fun fromString(value: String): AdrStatus? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}

/**
 * Data class representing a wiki-style link.
 */
data class WikiLink(
    val url: String,
    val label: String
)

/**
 * Data class representing an Architecture Decision Record (ADR).
 */
data class Adr(
    val title: String,
    val status: AdrStatus,
    val date: String,
    val context: List<String>,
    val decision: List<String>,
    val consequences: List<String>,
    val participants: List<String> = emptyList(),
    val links: List<WikiLink> = emptyList()
)

/**
 * Parser for Architecture Decision Record (ADR) files.
 */
class AdrParser {
    /**
     * Extract wiki links from a string and add them to the provided list.
     *
     * @param text The text to extract links from
     * @param links The list to add the extracted links to
     */
    private fun extractWikiLinks(text: String, links: MutableList<WikiLink>) {
        val linkPattern = "\\[\\[(.*?)\\s+(.*?)\\]\\]".toRegex()
        linkPattern.findAll(text).forEach { matchResult ->
            val (url, label) = matchResult.destructured
            links.add(WikiLink(url, label))
        }
    }

    /**
     * Parse an ADR file and return an Adr object.
     *
     * @param file The ADR file to parse
     * @return The parsed Adr object
     */
    fun parse(file: File): Adr {
        val content = file.readText()
        return parseContent(content)
    }

    /**
     * Parse ADR content from a string and return an Adr object.
     *
     * @param content The ADR content as a string
     * @return The parsed Adr object
     */
    fun parseContent(content: String): Adr {
        val lines = content.lines()

        var title = ""
        var statusStr = ""
        var date = ""
        val contextLines = mutableListOf<String>()
        val decisionLines = mutableListOf<String>()
        val consequencesLines = mutableListOf<String>()
        val participantsLines = mutableListOf<String>()
        val links = mutableListOf<WikiLink>()

        var currentSection: String? = null

        for (line in lines) {
            val trimmedLine = line.trim()

            when {
                trimmedLine.startsWith("title:", true) -> {
                    title = trimmedLine.substringAfter(trimmedLine.substring(0, 6), "").trim()
                    currentSection = null
                }
                trimmedLine.startsWith("status:", true) -> {
                    statusStr = trimmedLine.substringAfter(trimmedLine.substring(0, 7), "").trim()
                    currentSection = null
                }
                trimmedLine.startsWith("date:", true) -> {
                    date = trimmedLine.substringAfter(trimmedLine.substring(0, 5), "").trim()
                    currentSection = null
                }
                trimmedLine.startsWith("context:", true) -> {
                    currentSection = "context"
                    // Extract any text that appears after "context:" on the same line
                    val contextText = trimmedLine.substringAfter(trimmedLine.substring(0, trimmedLine.indexOf(":") + 1), "").trim()
                    if (contextText.isNotEmpty()) {
                        contextLines.add(contextText)
                        // Extract wiki links from the context text
                        extractWikiLinks(contextText, links)
                    }
                }
                trimmedLine.startsWith("decision:", true) -> {
                    currentSection = "decision"
                    // Extract any text that appears after "decision:" on the same line
                    val decisionText = trimmedLine.substringAfter(trimmedLine.substring(0, trimmedLine.indexOf(":") + 1), "").trim()
                    if (decisionText.isNotEmpty()) {
                        decisionLines.add(decisionText)
                        // Extract wiki links from the decision text
                        extractWikiLinks(decisionText, links)
                    }
                }
                trimmedLine.startsWith("consequences:", true) -> {
                    currentSection = "consequences"
                    // Extract any text that appears after "consequences:" on the same line
                    val consequencesText = trimmedLine.substringAfter(trimmedLine.substring(0, trimmedLine.indexOf(":") + 1), "").trim()
                    if (consequencesText.isNotEmpty()) {
                        consequencesLines.add(consequencesText)
                        // Extract wiki links from the consequences text
                        extractWikiLinks(consequencesText, links)
                    }
                }
                trimmedLine.startsWith("participants:", true) -> {
                    // For participants, we'll take the whole line as it might be a comma-separated list
                    participantsLines.add(trimmedLine.substringAfter(trimmedLine.substring(0, 13), "").trim())
                    currentSection = null
                }
                else -> {
                    // If we're in a section and the line is not empty, add it to the appropriate list
                    if (!trimmedLine.isEmpty()) {
                        when (currentSection) {
                            "context" -> contextLines.add(trimmedLine)
                            "decision" -> decisionLines.add(trimmedLine)
                            "consequences" -> consequencesLines.add(trimmedLine)
                            // If we're not in a recognized section, it might be free-form text
                            // For now, we'll ignore it, but we could add it to a general notes field if needed
                        }

                        // Extract wiki links from the line
                        extractWikiLinks(trimmedLine, links)
                    }
                }
            }
        }

        // Parse participants - they might be in a single line separated by commas
        val participants = if (participantsLines.isNotEmpty()) {
            participantsLines.joinToString(",")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() } // Filter out empty entries
        } else {
            emptyList()
        }

        // Parse status
        val status = AdrStatus.fromString(statusStr) ?: throw kotlin.IllegalArgumentException("Invalid status: $statusStr")

        return Adr(
            title = title,
            status = status,
            date = date,
            context = contextLines,
            decision = decisionLines,
            consequences = consequencesLines,
            participants = participants,
            links = links
        )
    }
}
