package io.docops.docopsextensionssupport.recipe

import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.support.wrapTextToWidth
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class RecipeSvgGenerator(
    private val useDark: Boolean = false
) {

    private var theme: DocOpsTheme = ThemeFactory.getTheme(useDark)

    private data class Layout(
        val summaryHeight: Int,
        val middleHeight: Int,
        val notesHeight: Int,
        val totalHeight: Int,
        val notesY: Int,
        val ingredientsHeight: Int,
        val stepsHeight: Int,
        val ingredientsLines: List<String>,
        val stepLines: List<String>,
        val noteLines: List<String>,
        val bodyPanelY: Int
    )

    private data class SectionLine(
        val number: Int? = null,
        val text: String
    )

    @OptIn(ExperimentalUuidApi::class)
    fun createSvg(recipe: Recipe, scale: Double = 1.0, isPdf: Boolean = false): String {
        val id = "recipe_${Uuid.random().toHexString()}"
        val width = 1200

        theme = ThemeFactory.getThemeByName(recipe.theme, useDark)

        val title = recipe.title ?: "Recipe"
        val yield = recipe.yield ?: "-"
        val prep = recipe.prep ?: "-"
        val cook = recipe.cook ?: "-"
        val summary = recipe.summary ?: ""
        val tags = if (recipe.tags.isNotEmpty()) recipe.tags.joinToString(" • ") else "untagged"

        val layout = measureLayout(recipe)

        val ingredientsText = if (recipe.ingredients.isNotEmpty()) {
            recipe.ingredients.joinToString("\n")
        } else {
            "No ingredients provided."
        }

        val stepsText = if (recipe.steps.isNotEmpty()) {
            recipe.steps.joinToString("\n")
        } else {
            "No steps provided."
        }

        val notesText = if (recipe.notes.isNotEmpty()) {
            recipe.notes.joinToString("\n")
        } else {
            "No notes provided."
        }

        return """
            <svg xmlns="http://www.w3.org/2000/svg"
                 width="${(width * scale).roundToInt()}" height="${(layout.totalHeight * scale).roundToInt()}"
                 viewBox="0 0 $width ${layout.totalHeight}" id="$id">
                <desc>DocOps Recipe Card</desc>
                ${if (!isPdf) svgDefs() else ""}
                <g transform="scale($scale)">
                    <rect width="$width" height="${layout.totalHeight}" fill="url(#recipe_bg)"/>
                    <rect width="$width" height="${layout.totalHeight}" fill="url(#recipe_grid)" opacity="0.9"/>

                    <rect x="60" y="60" width="1080" height="${layout.totalHeight - 120}" rx="28"
                          fill="${if (useDark) "rgba(10,14,20,0.78)" else "rgba(255,255,255,0.92)"}"
                          stroke="${if (useDark) "rgba(255,255,255,0.10)" else "rgba(15,23,42,0.12)"}"
                          stroke-width="1.5"/>

                    ${buildHeader(title, tags)}
                    ${buildMetaTable(yield, prep, cook, summary, layout.summaryHeight)}
                    ${buildBodyPanels(ingredientsText, stepsText, layout)}
                    ${buildNotesAndTags(tags, notesText, layout)}
                </g>
            </svg>
        """.trimIndent()
    }

    private fun measureLayout(recipe: Recipe): Layout {
        val summaryLines = wrapTextToWidth(recipe.summary.orEmpty(), 420f)
            .ifEmpty { listOf(recipe.summary.orEmpty()) }
            .take(4)

        val ingredientLines = recipe.ingredients.flatMap { line ->
            wrapTextToWidth(line, 430f)
        }.ifEmpty { listOf("No ingredients provided.") }

        val stepRowGap = 24
        val stepInterGap = 12
        val stepContentTop = 138
        var stepContentHeight = 0
        recipe.steps.forEach { line ->
            val clean = line
                .removePrefix("- ")
                .removePrefix("* ")
                .trim()
                .replace(Regex("^\\d+[.)]\\s*"), "")
            val wrapped = wrapTextToWidth(clean, 390f).ifEmpty { listOf("") }
            stepContentHeight += (wrapped.size * stepRowGap) + stepInterGap
        }
        if (recipe.steps.isEmpty()) {
            stepContentHeight = stepRowGap
        }

        val noteLines = recipe.notes.flatMap { line ->
            wrapTextToWidth(line, 420f)
        }.ifEmpty { listOf("No notes provided.") }

        val summaryHeight = max(180, 140 + summaryLines.size * 24)
        val ingredientsHeight = max(190, 130 + ingredientLines.size * 24)
        val stepsHeight = max(240, stepContentTop + stepContentHeight + 30)
        val notesHeight = max(180, 120 + noteLines.size * 24)

        val headerHeight = 300
        val gapAfterMeta = 30
        val gapBetweenPanels = 30
        val footerPadding = 90

        // Body panels start right after the meta table
        val bodyPanelY = headerHeight + summaryHeight + gapAfterMeta

        val middleHeight = max(ingredientsHeight, stepsHeight)
        val notesY = bodyPanelY + middleHeight + gapBetweenPanels
        val totalHeight = notesY + notesHeight + footerPadding

        return Layout(
            summaryHeight = summaryHeight,
            middleHeight = middleHeight,
            notesHeight = notesHeight,
            totalHeight = max(1320, totalHeight),
            notesY = notesY,
            ingredientsHeight = ingredientsHeight,
            stepsHeight = stepsHeight,
            ingredientsLines = ingredientLines,
            stepLines = emptyList(),
            noteLines = noteLines,
            bodyPanelY = bodyPanelY
        )
    }

    private fun svgDefs(): String = """
        <defs>
            <linearGradient id="recipe_bg" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stop-color="${if (useDark) "#0b0f14" else "#f8fafc"}"/>
                <stop offset="100%" stop-color="${if (useDark) "#111827" else "#e2e8f0"}"/>
            </linearGradient>

            <pattern id="recipe_grid" width="40" height="40" patternUnits="userSpaceOnUse">
                <path d="M 40 0 L 0 0 0 40" fill="none"
                      stroke="#ffffff"
                      stroke-opacity="${if (useDark) "0.04" else "0.07"}"
                      stroke-width="1"/>
            </pattern>

            <linearGradient id="accent_bar" x1="0" y1="0" x2="1" y2="0">
                <stop offset="0%" stop-color="${theme.accentColor}"/>
                <stop offset="100%" stop-color="${theme.secondaryText}"/>
            </linearGradient>

            <style>
                <![CDATA[
                    .recipe-title {
                        font-family: ${theme.fontFamily};
                        font-size: 54px;
                        font-weight: 700;
                    }
                    .meta-label {
                        font-family: ${theme.fontFamily};
                        font-size: 16px;
                        font-weight: 600;
                        letter-spacing: 1.5px;
                        text-transform: uppercase;
                    }
                    .meta-value {
                        font-family: ${theme.fontFamily};
                        font-size: 18px;
                    }
                    .section-title {
                        font-family: ${theme.fontFamily};
                        font-size: 18px;
                        font-weight: 600;
                        text-transform: uppercase;
                        letter-spacing: 2px;
                    }
                    .body-text {
                        font-family: ${theme.fontFamily};
                        font-size: 18px;
                    }
                    .badge-text {
                        font-family: ${theme.fontFamily};
                        font-size: 12px;
                        font-weight: 700;
                    }
                ]]>
            </style>
        </defs>
    """.trimIndent()

    private fun buildHeader(title: String, tags: String): String = """
        <g>
            <circle cx="1030" cy="170" r="170" fill="${theme.accentColor}" opacity="${if (useDark) "0.12" else "0.10"}"/>
            <circle cx="180" cy="1180" r="220" fill="${theme.secondaryText}" opacity="${if (useDark) "0.08" else "0.06"}"/>

            <text x="130" y="145"
                  class="recipe-title"
                  fill="${theme.secondaryText}"
                  font-size="18"
                  font-weight="600"
                  letter-spacing="2"
                  style="text-transform:uppercase;">recipe dossier</text>

            <text x="130" y="205"
                  class="recipe-title"
                  fill="${theme.primaryText}">${escapeXml(title)}</text>

            <rect x="860" y="130" width="220" height="44" rx="999"
                  fill="${theme.surfaceImpact}"
                  stroke="${theme.accentColor}" stroke-opacity="0.35"/>
            <text x="970" y="158" text-anchor="middle"
                  class="meta-value"
                  font-size="16"
                  font-weight="500"
                  fill="${theme.primaryText}">${escapeXml(tags)}</text>
        </g>
    """.trimIndent()

    private fun buildMetaTable(yield: String, prep: String, cook: String, summary: String, summaryHeight: Int): String {
        val summaryLines = wrapTextToWidth(summary, 420f)
            .ifEmpty { listOf(summary) }
            .take(4)

        val summaryStartY = if (summaryLines.size == 1) 460 else 450
        val summaryLineHeight = 24

        val summarySvg = summaryLines.mapIndexed { index, line ->
            val y = summaryStartY + (index * summaryLineHeight)
            """<text x="640" y="$y" class="meta-value" fill="${theme.primaryText}">${escapeXml(line)}</text>"""
        }.joinToString("\n")

        val tableHeight = summaryHeight
        val dividerY = 300 + tableHeight

        return """
            <g>
                <rect x="90" y="300" width="1020" height="$tableHeight" rx="20"
                      fill="${theme.canvas}"
                      fill-opacity="${if (useDark) "0.14" else "0.65"}"
                      stroke="${theme.accentColor}" stroke-opacity="0.12"/>

                <line x1="90" y1="345" x2="1110" y2="345" stroke="${theme.primaryText}" stroke-opacity="0.10"/>
                <line x1="90" y1="390" x2="1110" y2="390" stroke="${theme.primaryText}" stroke-opacity="0.10"/>
                <line x1="90" y1="435" x2="1110" y2="435" stroke="${theme.primaryText}" stroke-opacity="0.10"/>
                <line x1="600" y1="300" x2="600" y2="$dividerY" stroke="${theme.primaryText}" stroke-opacity="0.10"/>

                <text x="125" y="330" class="meta-label" fill="${theme.secondaryText}">yield</text>
                <text x="125" y="375" class="meta-label" fill="${theme.secondaryText}">prep</text>
                <text x="125" y="420" class="meta-label" fill="${theme.secondaryText}">cook</text>
                <text x="125" y="465" class="meta-label" fill="${theme.secondaryText}">summary</text>

                <text x="640" y="330" class="meta-value" fill="${theme.primaryText}">${escapeXml(yield)}</text>
                <text x="640" y="375" class="meta-value" fill="${theme.primaryText}">${escapeXml(prep)}</text>
                <text x="640" y="420" class="meta-value" fill="${theme.primaryText}">${escapeXml(cook)}</text>
                $summarySvg
            </g>
        """.trimIndent()
    }

    private fun buildBodyPanels(ingredientsText: String, stepsText: String, layout: Layout): String {
        val panelY = layout.bodyPanelY

        return """
        ${buildSection(
            x = 90,
            y = panelY,
            w = 500,
            h = layout.middleHeight,
            title = "ingredients",
            body = ingredientsText,
            delay = 220
        )}
        ${buildSection(
            x = 620,
            y = panelY,
            w = 490,
            h = layout.middleHeight,
            title = "steps",
            body = stepsText,
            delay = 320
        )}
    """.trimIndent()
    }

    private fun buildSection(x: Int, y: Int, w: Int, h: Int, title: String, body: String, delay: Int): String {
        val isSteps = title.equals("steps", ignoreCase = true)

        val rawLines = body.split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val contentStartX = x + 35
        val contentWidth = w - 70

        val entries = if (isSteps) {
            rawLines.mapIndexed { index, line ->
                val clean = line
                    .removePrefix("- ")
                    .removePrefix("* ")
                    .trim()
                    .replace(Regex("^\\d+[.)]\\s*"), "")
                SectionLine(number = index + 1, text = clean)
            }
        } else {
            rawLines.map { SectionLine(text = it) }
        }

        val rowGap = if (isSteps) 24 else 24
        val badgeGap = 10
        val badgeHeight = 20

        // More top padding and bottom clearance for steps
        val contentTopPadding = if (isSteps) 138 else 118
        val bottomPadding = if (isSteps) 24 else 10

        var currentY = y + contentTopPadding
        val sb = StringBuilder()

        entries.forEach { entry ->
            val hasBadge = isSteps && entry.number != null
            val badgeText = entry.number?.toString().orEmpty()
            val badgeWidth = when {
                !hasBadge -> 0
                badgeText.length >= 3 -> 40
                badgeText.length == 2 -> 30
                else -> 24
            }

            val textX = if (hasBadge) contentStartX + badgeWidth + badgeGap else contentStartX
            val availableWidth = if (hasBadge) contentWidth - badgeWidth - badgeGap else contentWidth
            val wrapWidth = if (hasBadge) (availableWidth - 6).toFloat() else availableWidth.toFloat()

            val wrapped = wrapTextToWidth(entry.text, wrapWidth)
                .ifEmpty { listOf("") }

            if (hasBadge) {
                val badgeFill = theme.accentColor
                val badgeTextColor = if (useDark) "#0b0f14" else "#ffffff"
                val badgeX = contentStartX

                sb.append(
                    """
                <g>
                    <rect x="$badgeX" y="${currentY - 14}" width="$badgeWidth" height="$badgeHeight"
                          rx="10" fill="$badgeFill" opacity="0.96"/>
                    <text x="${badgeX + badgeWidth / 2}" y="${currentY + 1}"
                          text-anchor="middle"
                          class="badge-text"
                          fill="$badgeTextColor">${entry.number}</text>
                </g>
                """.trimIndent()
                )
            }

            wrapped.forEachIndexed { index, line ->
                val lineY = currentY + (index * rowGap)
                sb.append(
                    """<text x="$textX" y="$lineY" class="body-text" fill="${theme.primaryText}">${escapeXml(line)}</text>"""
                )
            }

            currentY += (wrapped.size * rowGap) + if (hasBadge) 12 else 10
        }

        return """
        <g opacity="0">
            <animate attributeName="opacity" from="0" to="1" dur="0.7s" begin="${delay}ms" fill="freeze"/>
            <rect x="$x" y="$y" width="$w" height="$h" rx="24"
                  fill="${theme.canvas}"
                  fill-opacity="${if (useDark) "0.12" else "0.82"}"
                  stroke="${theme.accentColor}" stroke-opacity="0.12"/>
            <rect x="$x" y="$y" width="$w" height="8" fill="url(#accent_bar)" rx="4"/>
            <text x="${x + 35}" y="${y + 50}"
                  class="section-title"
                  fill="${theme.secondaryText}">$title</text>
            <line x1="${x + 30}" y1="${y + 80}" x2="${x + w - 30}" y2="${y + 80}"
                  stroke="${theme.primaryText}" stroke-opacity="0.10"/>
            <g>
                $sb
            </g>
        </g>
    """.trimIndent()
    }


    private fun buildNotesAndTags(tags: String, notes: String, layout: Layout): String {
        val notesStartX = 655
        val notesLines = notes.split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .flatMap { line -> wrapTextToWidth(line, 420f) }
            .take(12)

        val notesY = layout.notesY
        val notesLineHeight = 24

        val notesSvg = notesLines.mapIndexed { index, line ->
            val y = notesY + 95 + (index * notesLineHeight)
            """<text x="$notesStartX" y="$y" class="body-text" fill="${theme.primaryText}">${escapeXml(line)}</text>"""
        }.joinToString("\n")

        return """
            <g>
                <rect x="90" y="$notesY" width="1020" height="${layout.notesHeight}" rx="20"
                      fill="${theme.canvas}"
                      fill-opacity="${if (useDark) "0.10" else "0.72"}"
                      stroke="${theme.accentColor}" stroke-opacity="0.10"/>
                <line x1="90" y1="${notesY + 50}" x2="1110" y2="${notesY + 50}" stroke="${theme.primaryText}" stroke-opacity="0.10"/>
                <line x1="600" y1="$notesY" x2="600" y2="${notesY + layout.notesHeight}" stroke="${theme.primaryText}" stroke-opacity="0.10"/>

                <text x="125" y="${notesY + 35}" class="meta-label" fill="${theme.secondaryText}">tags</text>
                <text x="655" y="${notesY + 35}" class="meta-label" fill="${theme.secondaryText}">notes</text>

                ${buildTagPills(tags, notesY)}
                $notesSvg
            </g>
        """.trimIndent()
    }

    private fun buildTagPills(tags: String, notesY: Int): String {
        val items = tags.split("•", ",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (items.isEmpty()) {
            return """
                <rect x="125" y="${notesY + 80}" width="120" height="36" rx="999"
                      fill="${theme.surfaceImpact}" stroke="${theme.accentColor}" stroke-opacity="0.35"/>
                <text x="185" y="${notesY + 103}" text-anchor="middle"
                      class="body-text" font-size="16" fill="${theme.primaryText}">untagged</text>
            """.trimIndent()
        }

        val widths = items.map { max(72, it.length * 9 + 28) }
        var x = 125
        var y = notesY + 80
        val sb = StringBuilder()

        items.forEachIndexed { index, item ->
            val w = widths[index]
            if (x + w > 560) {
                x = 125
                y += 50
            }
            sb.append(
                """
                <rect x="$x" y="$y" width="$w" height="36" rx="999"
                      fill="${theme.surfaceImpact}" stroke="${theme.accentColor}" stroke-opacity="0.35"/>
                <text x="${x + w / 2}" y="${y + 23}" text-anchor="middle"
                      class="body-text" font-size="16" fill="${theme.primaryText}">${escapeXml(item)}</text>
                """.trimIndent()
            )
            x += w + 12
        }
        return sb.toString()
    }

    private fun escapeXml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}