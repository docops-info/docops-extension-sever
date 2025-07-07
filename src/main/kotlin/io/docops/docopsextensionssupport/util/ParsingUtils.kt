package io.docops.docopsextensionssupport.util

/**
 * Utility object for parsing text content in various formats.
 * Provides standardized methods for common parsing tasks used across the application.
 */
object ParsingUtils {

    /**
     * Parses the content to extract configuration parameters and data.
     * Configuration parameters are specified at the beginning of the content in the format "key=value",
     * followed by a separator line "---", and then the actual data.
     *
     * @param content The full content to parse
     * @return A Pair containing the configuration map and the data string
     */
    fun parseConfigAndData(content: String): Pair<Map<String, String>, String> {
        val lines = content.lines()
        val config = mutableMapOf<String, String>()
        var separatorIndex = -1

        // Find the separator line and parse configuration
        for (i in lines.indices) {
            val line = lines[i].trim()
            if (line == "---") {
                separatorIndex = i
                break
            }

            // Parse key=value pairs
            val keyValuePair = parseKeyValuePair(line, "=")
            if (keyValuePair != null) {
                config[keyValuePair.first] = keyValuePair.second
            }
        }

        // Extract data
        val data = if (separatorIndex >= 0) {
            lines.subList(separatorIndex + 1, lines.size).joinToString("\n")
        } else {
            // If no separator is found, assume the entire content is data
            content
        }

        return Pair(config, data)
    }

    /**
     * Parses a single line into a key-value pair using the specified delimiter.
     *
     * @param line The line to parse
     * @param delimiter The delimiter separating key and value (default is ":")
     * @return A Pair containing the key and value, or null if the line doesn't contain a valid key-value pair
     */
    private fun parseKeyValuePair(line: String, delimiter: String = ":"): Pair<String, String>? {
        val parts = line.split(delimiter, limit = 2)
        if (parts.size == 2) {
            val key = parts[0].trim()
            val value = parts[1].trim()
            if (key.isNotEmpty()) {
                return Pair(key, value)
            }
        }
        return null
    }

    /**
     * Parses key-value pairs from the content using the specified delimiter.
     *
     * @param content The content containing key-value pairs
     * @param delimiter The delimiter separating key and value (default is ":")
     * @return A Map containing the parsed key-value pairs
     */
    fun parseKeyValuePairs(content: String, delimiter: String = ":"): Map<String, String> {
        val result = mutableMapOf<String, String>()
        content.lines().forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.isNotEmpty()) {
                val keyValuePair = parseKeyValuePair(trimmedLine, delimiter)
                if (keyValuePair != null) {
                    result[keyValuePair.first] = keyValuePair.second
                }
            }
        }
        return result
    }

    /**
     * Parses tabular data from the content, automatically detecting the delimiter if requested.
     *
     * @param content The content containing tabular data
     * @param detectDelimiter Whether to automatically detect the delimiter (default is true)
     * @return A List of Lists representing the parsed table data
     */
    fun parseTableData(content: String, detectDelimiter: Boolean = true): List<List<String>> {
        val lines = content.lines().filter { it.trim().isNotEmpty() }
        if (lines.isEmpty()) {
            return emptyList()
        }

        // Detect delimiter if requested
        val delimiter = if (detectDelimiter) {
            detectDelimiter(lines)
        } else {
            ","
        }

        return lines.map { line ->
            when (delimiter) {
                "|" -> line.split("|").map { it.trim() }
                "\t" -> line.split("\t").map { it.trim() }
                "," -> parseCSVRow(line)
                else -> listOf(line)
            }
        }
    }

    /**
     * Detects the delimiter used in the tabular data.
     *
     * @param lines The lines of tabular data
     * @return The detected delimiter ("|", "\t", or "," by default)
     */
    private fun detectDelimiter(lines: List<String>): String {
        // Count occurrences of each delimiter in the first few lines
        var pipeCount = 0
        var tabCount = 0
        var commaCount = 0

        val linesToCheck = minOf(5, lines.size)
        for (i in 0 until linesToCheck) {
            val line = lines[i]
            pipeCount += line.count { it == '|' }
            tabCount += line.count { it == '\t' }
            commaCount += line.count { it == ',' }
        }

        // Return the delimiter with the highest count
        return when {
            pipeCount >= tabCount && pipeCount >= commaCount -> "|"
            tabCount >= pipeCount && tabCount >= commaCount -> "\t"
            else -> ","
        }
    }

    /**
     * Parses a CSV row, handling quoted values.
     *
     * @param line The CSV line to parse
     * @return A List of values from the CSV row
     */
    private fun parseCSVRow(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val char = line[i]
            when {
                char == '"' && (i == 0 || line[i-1] != '\\') -> {
                    inQuotes = !inQuotes
                }
                char == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> {
                    current.append(char)
                }
            }
            i++
        }

        result.add(current.toString().trim())
        return result
    }

    /**
     * Splits the content into sections based on the provided section markers.
     *
     * @param content The content to split into sections
     * @param sectionMarkers The markers that indicate the start of a new section
     * @return A Map where the keys are section names and the values are section contents
     */
    fun splitIntoSections(content: String, sectionMarkers: List<String>): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val lines = content.lines()

        var currentSection = ""
        var currentContent = StringBuilder()
        var foundAnyMarker = false

        for (line in lines) {
            val trimmedLine = line.trim()
            val matchedMarker = sectionMarkers.find { trimmedLine.startsWith(it) }

            if (matchedMarker != null) {
                // Save the previous section if it exists
                if (currentContent.isNotEmpty()) {
                    result[currentSection] = currentContent.toString().trim()
                }

                // For the first marker, use empty string as key
                // For subsequent markers, use the marker number (1-based index) as key
                val markerIndex = sectionMarkers.indexOf(matchedMarker)
                currentSection = if (markerIndex == 0) "" else (markerIndex + 1).toString()

                // Start a new section
                currentContent = StringBuilder()
                foundAnyMarker = true
            } else if (foundAnyMarker) {
                // Add the line to the current section
                currentContent.append(line).append("\n")
            }
        }

        // Save the last section if it exists
        if (currentContent.isNotEmpty()) {
            result[currentSection] = currentContent.toString().trim()
        }

        return result
    }

    /**
     * Extracts configuration from the content using the specified pattern.
     *
     * @param content The content containing configuration
     * @param configPattern The pattern used for configuration (default is "key=value")
     * @return A Map containing the extracted configuration
     */
    fun extractConfiguration(content: String, configPattern: String = "key=value"): Map<String, String> {
        val delimiter = when (configPattern) {
            "key=value" -> "="
            "key:value" -> ":"
            else -> "="
        }

        return parseKeyValuePairs(content, delimiter)
    }
}
