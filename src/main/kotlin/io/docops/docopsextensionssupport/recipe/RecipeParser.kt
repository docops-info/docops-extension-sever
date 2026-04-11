package io.docops.docopsextensionssupport.recipe


class RecipeParser {

    private val mixedFractionRegex = Regex("""(?<![\d/])(\d+)\s+(\d{1,2})/(\d{1,2})(?![\d/])""")
    private val simpleFractionRegex = Regex("""(?<![\d/])(\d{1,2})/(\d{1,2})(?![\d/])""")

    fun parse(content: String): Recipe {
        var title: String? = null
        var yield: String? = null
        var prep: String? = null
        var cook: String? = null
        var summary: String? = null
        var themeName: String = "classic"

        val tags = mutableListOf<String>()
        val ingredients = mutableListOf<String>()
        val steps = mutableListOf<String>()
        val notes = mutableListOf<String>()

        var currentSection: String? = null
        var buffer = mutableListOf<String>()

        fun flushSection() {
            val text = buffer.joinToString("\n").trim()
            if (text.isBlank() || currentSection == null) {
                buffer = mutableListOf()
                return
            }

            when (currentSection) {
                "title" -> title = text
                "yield" -> yield = text
                "theme" -> themeName = text
                "prep" -> prep = text
                "cook" -> cook = text
                "summary" -> summary = text
                "tags" -> tags.addAll(splitList(text))
                "ingredients" -> ingredients.addAll(splitListOrLines(text))
                "steps" -> steps.addAll(splitNumberedOrLines(text))
                "notes" -> notes.addAll(splitListOrLines(text))
            }
            buffer = mutableListOf()
        }

        content.lines().forEach { rawLine ->
            val line = rawLine.trim()

            when {
                line.startsWith("title=") -> {
                    flushSection()
                    currentSection = "title"
                    buffer.add(line.substringAfter("=").trim())
                }
                line.startsWith("yield=") -> {
                    flushSection()
                    currentSection = "yield"
                    buffer.add(line.substringAfter("=").trim())
                }
                line.startsWith("prep=") -> {
                    flushSection()
                    currentSection = "prep"
                    buffer.add(line.substringAfter("=").trim())
                }
                line.startsWith("cook=") -> {
                    flushSection()
                    currentSection = "cook"
                    buffer.add(line.substringAfter("=").trim())
                }
                line.startsWith("theme=") -> {
                    flushSection()
                    currentSection = "theme"
                    buffer.add(line.substringAfter("=").trim())
                }
                line.startsWith("tags=") -> {
                    flushSection()
                    currentSection = "tags"
                    buffer.add(line.substringAfter("=").trim())
                }
                line.startsWith("summary=") -> {
                    flushSection()
                    currentSection = "summary"
                    buffer.add(line.substringAfter("=").trim())
                }
                line.startsWith("ingredients=") -> {
                    flushSection()
                    currentSection = "ingredients"
                    buffer.add(line.substringAfter("=").trim())
                }
                line.startsWith("steps=") -> {
                    flushSection()
                    currentSection = "steps"
                    buffer.add(line.substringAfter("=").trim())
                }
                line.startsWith("notes=") -> {
                    flushSection()
                    currentSection = "notes"
                    buffer.add(line.substringAfter("=").trim())
                }
                else -> {
                    if (line.isNotBlank()) {
                        buffer.add(line)
                    }
                }
            }
        }

        flushSection()

        return Recipe(
            title = title,
            yield = yield,
            prep = prep,
            cook = cook,
            tags = tags,
            summary = summary,
            ingredients = ingredients,
            steps = steps,
            notes = notes,
            theme = themeName
        )
    }

    private fun splitList(text: String): List<String> =
        text.split(",", ";")
            .map { it.trim() }
            .filter { it.isNotBlank() }

    private fun splitListOrLines(text: String): List<String> =
        text.lines()
            .flatMap { line ->
                val cleaned = line.trim()
                when {
                    cleaned.startsWith("- ") -> listOf(cleaned.removePrefix("- ").trim())
                    cleaned.startsWith("* ") -> listOf(cleaned.removePrefix("* ").trim())
                    cleaned.isNotBlank() -> listOf(cleaned)
                    else -> emptyList()
                }
            }
            .map { normalizeFractions(it) }
            .filter { it.isNotBlank() }

    private fun splitNumberedOrLines(text: String): List<String> =
        text.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { line ->
                line.replace(Regex("^\\d+[.)]\\s*"), "")
                    .replace(Regex("^[-*+]\\s*"), "")
                    .trim()
            }
            .map { normalizeFractions(it) }
            .filter { it.isNotBlank() }

    private fun normalizeFractions(text: String): String {
        val withMixedFractions = mixedFractionRegex.replace(text) { match ->
            val whole = match.groupValues[1]
            val numerator = match.groupValues[2]
            val denominator = match.groupValues[3]
            val unicode = toUnicodeFraction(numerator, denominator)
            if (unicode != null) "$whole$unicode" else match.value
        }

        return simpleFractionRegex.replace(withMixedFractions) { match ->
            val numerator = match.groupValues[1]
            val denominator = match.groupValues[2]
            toUnicodeFraction(numerator, denominator) ?: match.value
        }

    }
    private fun toUnicodeFraction(numerator: String, denominator: String): String? =
        when ("$numerator/$denominator") {
            "1/2" -> "½"
            "1/3" -> "⅓"
            "2/3" -> "⅔"
            "1/4" -> "¼"
            "3/4" -> "¾"
            "1/5" -> "⅕"
            "2/5" -> "⅖"
            "3/5" -> "⅗"
            "4/5" -> "⅘"
            "1/6" -> "⅙"
            "5/6" -> "⅚"
            "1/8" -> "⅛"
            "3/8" -> "⅜"
            "5/8" -> "⅝"
            "7/8" -> "⅞"
            else -> null
        }
}
