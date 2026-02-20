package io.docops.docopsextensionssupport.scorecard


/**
 * Parser for ScoreCard content blocks.
 * Expected simple INI-like sections as currently used in ScorecardController default content.
 * This parser is intentionally minimal to unblock rendering; it can be extended later.
 */
class ScoreCardParser {

    fun parse(payload: String, useDark: Boolean, scale: String): ScoreCard {
        val lines = payload.lines()
        var title = ""
        var subtitle = ""
        var initiativeTitle = "BEFORE"
        var outcomeTitle = "AFTER"
        var visualVersion = 1
        var theme = "classic"
        val beforeSections: MutableList<BeforeSection> = mutableListOf()
        val afterSections: MutableList<AfterSection> = mutableListOf()
        val outcomeItems: MutableList<ScoreCardItem> = mutableListOf()

        var currentSection: Section? = null
        var inBefore = false
        var inAfter = false

        var currentItemsTitle: String? = null

        fun startBeforeSectionIfNeeded() {
            if (currentSection !is BeforeSection) {
                currentSection = BeforeSection()
                beforeSections.add(currentSection as BeforeSection)
            }
        }
        fun startAfterSectionIfNeeded() {
            if (currentSection !is AfterSection) {
                currentSection = AfterSection()
                afterSections.add(currentSection as AfterSection)
            }
        }

        lines.forEach { raw ->
            val line = raw.trim()
            if (line.isBlank()) return@forEach
            when {
                // section toggles
                line == "[before]" -> { inBefore = true; inAfter = false; currentSection = null }
                line == "[after]" -> { inAfter = true; inBefore = false; currentSection = null }

                // items blocks
                line == "[before.items]" -> { inBefore = true; inAfter = false; /* do not start a section yet; wait for === or items */ currentSection = null }
                line == "[after.items]" -> { inAfter = true; inBefore = false; /* do not start a section yet; wait for === or items */ currentSection = null }

                // group headings within items
                line.startsWith("=== ") -> {
                    val heading = line.removePrefix("=== ").trim()
                    if (inBefore) {
                        // Always start a NEW BeforeSection for each heading
                        currentSection = BeforeSection().also {
                            it.title = heading
                            beforeSections.add(it)
                        }
                    } else if (inAfter) {
                        // Always start a NEW AfterSection for each heading
                        currentSection = AfterSection().also {
                            it.title = heading
                            afterSections.add(it)
                        }
                    }
                    currentItemsTitle = heading
                }

                // section titles within before/after (header titles for the column, not groups)
                inBefore && line.startsWith("title=") -> {
                    initiativeTitle = line.removePrefix("title=").trim()
                }
                inAfter && line.startsWith("title=") -> {
                    outcomeTitle = line.removePrefix("title=").trim()
                }

                // top-level attributes
                line.startsWith("title=") -> title = line.removePrefix("title=").trim()
                line.startsWith("subtitle=") -> subtitle = line.removePrefix("subtitle=").trim()
                line.startsWith("visualVersion=") -> visualVersion = line.removePrefix("visualVersion=").trim().toIntOrNull() ?: 1
                line.startsWith("theme=") -> theme = line.removePrefix("theme=").trim()
                line == "---" -> { currentSection = null; currentItemsTitle = null }

                // item lines
                line.contains("|") && (inBefore || inAfter) -> {
                    val parts = line.split("|").map { it.trim() }
                    val display = if (parts.isNotEmpty()) parts[0] else line
                    val desc = if (parts.size > 1) parts[1] else ""
                    val item = ScoreCardItem(displayText = display, description = desc)
                    if (inBefore) {
                        if (currentSection !is BeforeSection) {
                            // Create a default section if none active yet
                            currentSection = BeforeSection().also {
                                it.title = currentItemsTitle ?: initiativeTitle
                                beforeSections.add(it)
                            }
                        }
                        currentSection!!.items.add(item)
                        if (currentSection!!.title.isBlank()) currentSection!!.title = currentItemsTitle ?: initiativeTitle
                    } else if (inAfter) {
                        if (currentSection !is AfterSection) {
                            currentSection = AfterSection().also {
                                it.title = currentItemsTitle ?: outcomeTitle
                                afterSections.add(it)
                            }
                        }
                        currentSection!!.items.add(item)
                        if (currentSection!!.title.isBlank()) currentSection!!.title = currentItemsTitle ?: outcomeTitle
                    }
                }
                else -> { /* ignore */ }
            }
        }

        // Column titles must come from [before] and [after] sections only per spec
        val combinedTitle = if (subtitle.isNotBlank()) {
            if (title.isNotBlank()) "$title — $subtitle" else subtitle
        } else title

        return ScoreCard(
            title = combinedTitle.ifBlank { "ScoreCard" },
            beforeTitle = initiativeTitle.ifBlank { "BEFORE" },
            beforeSections = beforeSections,
            afterTitle = outcomeTitle.ifBlank { "AFTER" },
            afterSections = afterSections,
            useDark = useDark,
            visualVersion = visualVersion,
            theme = theme
        )
    }
}