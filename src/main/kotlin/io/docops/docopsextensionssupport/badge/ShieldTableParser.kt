package io.docops.docopsextensionssupport.badge

/**
 * Parser for shield table data in various formats
 */
class ShieldTableParser {

    companion object {
        private val PIPE_DELIMITER = Regex("\\|")
        private val COMMA_DELIMITER = Regex(",")
        private val TAB_DELIMITER = Regex("\t")
    }

    /**
     * Parse shield data from tabular text content
     * @return Pair of shield data list and configuration (or null if no config found)
     */
    fun parseShieldTable(content: String): Pair<List<ShieldData>, ShieldTableConfig?> {
        val lines = content.trim().split('\n')
            .map { it.trim() }
            .filter { it.isNotBlank() }

        // Look for configuration section
        val configSection = extractConfigSection(content)
        val config = configSection?.let { parseShieldConfig(it) }

        // Filter out config section if present
        val dataLines = if (configSection != null) {
            lines.filter { !it.contains("{") || !it.contains("}") }
        } else {
            lines
        }

        val shields = dataLines.map { line ->
            val row = parseRow(line)
            row.toShieldData()
        }

        return Pair(shields, config)
    }

    /**
     * Extract configuration section from content
     */
    private fun extractConfigSection(content: String): String? {
        // Look for a JSON-like configuration section
        val configRegex = Regex("\\{[^{}]*\\}")
        val match = configRegex.find(content)
        return match?.value
    }

    /**
     * Parse a single row and determine the delimiter
     */
    private fun parseRow(line: String): ShieldTableRow {
        val columns = when {
            line.contains('|') -> line.split(PIPE_DELIMITER)
            line.contains('\t') -> line.split(TAB_DELIMITER)
            line.contains(',') -> parseCSVRow(line)
            else -> listOf(line)
        }

        return ShieldTableRow(columns.map { it.trim() })
    }

    /**
     * Parse CSV row handling quoted values
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
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> {
                    current.append(char)
                }
            }
            i++
        }

        result.add(current.toString())
        return result
    }

    /**
     * Parse shield configuration from JSON-like format
     */
    fun parseShieldConfig(jsonContent: String): ShieldTableConfig? {
        // Simple JSON-like parsing for shield configuration
        return try {
            // This is a simplified parser - in production you might want to use a proper JSON library
            val config = ShieldTableConfig()

            if (jsonContent.contains("\"style\"")) {
                val styleMatch = Regex("\"style\"\\s*:\\s*\"([^\"]+)\"").find(jsonContent)
                styleMatch?.let {
                    config.defaultStyle = ShieldStyle.entries.find { style ->
                        style.value == it.groupValues[1]
                    } ?: ShieldStyle.FLAT
                }
            }

            if (jsonContent.contains("\"theme\"")) {
                val themeMatch = Regex("\"theme\"\\s*:\\s*\"([^\"]+)\"").find(jsonContent)
                themeMatch?.let {
                    config.theme = it.groupValues[1]
                }
            }

            if (jsonContent.contains("\"spacing\"")) {
                val spacingMatch = Regex("\"spacing\"\\s*:\\s*(\\d+)").find(jsonContent)
                spacingMatch?.let {
                    config.spacing = it.groupValues[1].toIntOrNull() ?: 15
                }
            }

            if (jsonContent.contains("\"arrangement\"")) {
                val arrangementMatch = Regex("\"arrangement\"\\s*:\\s*\"([^\"]+)\"").find(jsonContent)
                arrangementMatch?.let {
                    val arrangementValue = it.groupValues[1].uppercase()
                    try {
                        config.arrangement = ShieldArrangement.valueOf(arrangementValue)
                    } catch (e: IllegalArgumentException) {
                        // Default to GRID if invalid arrangement
                    }
                }
            }

            if (jsonContent.contains("\"backgroundStyle\"")) {
                val backgroundStyleMatch = Regex("\"backgroundStyle\"\\s*:\\s*\"([^\"]+)\"").find(jsonContent)
                backgroundStyleMatch?.let {
                    config.backgroundStyle = it.groupValues[1]
                }
            }

            if (jsonContent.contains("\"animationEnabled\"")) {
                val animationEnabledMatch = Regex("\"animationEnabled\"\\s*:\\s*(true|false)").find(jsonContent)
                animationEnabledMatch?.let {
                    config.animationEnabled = it.groupValues[1].toBoolean()
                }
            }

            config
        } catch (e: Exception) {
            null
        }
    }
}
