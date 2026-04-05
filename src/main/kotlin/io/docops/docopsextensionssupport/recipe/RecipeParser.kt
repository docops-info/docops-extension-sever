package io.docops.docopsextensionssupport.recipe


class RecipeParser {

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
            .filter { it.isNotBlank() }
}
