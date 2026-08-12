package io.docops.docopsextensionssupport.adr

object WikiLinkParser {
    private val linkPattern = "\\[\\[([^\\s\\]]+)\\s+([^\\]]+)\\]\\]".toRegex()

    data class LinkSegment(val text: String, val url: String? = null)

    fun parse(text: String): List<LinkSegment> {
        val segments = mutableListOf<LinkSegment>()
        var currentIndex = 0

        linkPattern.findAll(text).forEach { match ->
            if (match.range.first > currentIndex) {
                segments.add(LinkSegment(text.substring(currentIndex, match.range.first)))
            }

            val url = match.groupValues[1]
            val label = match.groupValues[2]
            segments.add(LinkSegment(label, url))

            currentIndex = match.range.last + 1
        }

        if (currentIndex < text.length) {
            segments.add(LinkSegment(text.substring(currentIndex)))
        }

        return segments
    }

    fun tokenize(text: String): List<String> {
        val tokens = mutableListOf<String>()
        var currentIndex = 0

        linkPattern.findAll(text).forEach { match ->
            if (match.range.first > currentIndex) {
                tokens.addAll(
                    text.substring(currentIndex, match.range.first)
                        .split(Regex("\\s+"))
                        .filter { it.isNotBlank() }
                )
            }

            tokens.add(match.value)
            currentIndex = match.range.last + 1
        }

        if (currentIndex < text.length) {
            tokens.addAll(
                text.substring(currentIndex)
                    .split(Regex("\\s+"))
                    .filter { it.isNotBlank() }
            )
        }

        return tokens
    }
}
