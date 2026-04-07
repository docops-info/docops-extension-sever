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

                    ${buildHeader(title, tags)}
                    ${buildMetaTable(yield, prep, cook, summary, layout.summaryHeight)}
                    ${buildBodyPanels(ingredientsText, stepsText, layout)}
                    ${buildNotesAndTags(tags, notesText, layout)}
                </g>
            </svg>
        """.trimIndent()
    }

    private fun measureLayout(recipe: Recipe): Layout {
        val summaryLines = wrapTextToWidth(recipe.summary.orEmpty(), 600f)
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
            wrapTextToWidth(line, 940f)
        }.ifEmpty { listOf("No notes provided.") }

        val summaryHeight = max(200, 160 + summaryLines.size * 28)
        val ingredientsHeight = max(190, 130 + ingredientLines.size * 24)
        val stepsHeight = max(240, stepContentTop + stepContentHeight + 30)

        val noteLineCount = noteLines.size
        val notesExtraBottomPadding = if (noteLineCount > 2) (noteLineCount - 2) * 20 else 0
        val notesHeight = max(220, 160 + noteLineCount * 28 + notesExtraBottomPadding)

        val headerHeight = 280
        val gapAfterMeta = 40
        val gapBetweenPanels = 40
        val footerPadding = 100

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
                <stop offset="0%" stop-color="${if (useDark) "#0f172a" else "#f8fafc"}"/>
                <stop offset="100%" stop-color="${if (useDark) "#020617" else "#f1f5f9"}"/>
            </linearGradient>

            <pattern id="recipe_grid" width="60" height="60" patternUnits="userSpaceOnUse">
                <path d="M 60 0 L 0 0 0 60" fill="none"
                      stroke="${theme.accentColor}"
                      stroke-opacity="${if (useDark) "0.05" else "0.08"}"
                      stroke-width="1"/>
            </pattern>

            <linearGradient id="accent_bar" x1="0" y1="0" x2="1" y2="0">
                <stop offset="0%" stop-color="${theme.accentColor}"/>
                <stop offset="100%" stop-color="${theme.secondaryText}"/>
            </linearGradient>
            
            <filter id="glass" x="-20%" y="-20%" width="140%" height="140%">
                <feGaussianBlur in="SourceGraphic" stdDeviation="5" result="blur"/>
                <feComposite in="SourceGraphic" in2="blur" operator="over"/>
            </filter>
            
            <filter id="drop_shadow" x="-20%" y="-20%" width="140%" height="140%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="8"/>
                <feOffset dx="4" dy="8" result="offsetblur"/>
                <feFlood flood-color="0,0,0" flood-opacity="${if (useDark) "0.5" else "0.12"}"/>
                <feComposite in2="offsetblur" operator="in"/>
                <feMerge>
                    <feMergeNode/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>

            <style>
                <![CDATA[
                    :root {
                        --primary: ${theme.primaryText};
                        --secondary: ${theme.secondaryText};
                        --accent: ${theme.accentColor};
                        --canvas: ${theme.canvas};
                    }
                    .recipe-title {
                        font-family: ${theme.fontFamily};
                        font-size: 64px;
                        font-weight: 800;
                        letter-spacing: -1.5px;
                    }
                    .recipe-subtitle {
                        font-family: ${theme.fontFamily};
                        font-size: 16px;
                        font-weight: 600;
                        letter-spacing: 4px;
                        text-transform: uppercase;
                        opacity: 0.6;
                    }
                    .meta-label {
                        font-family: ${theme.fontFamily};
                        font-size: 14px;
                        font-weight: 700;
                        letter-spacing: 1.5px;
                        text-transform: uppercase;
                        opacity: 0.7;
                    }
                    .meta-value {
                        font-family: ${theme.fontFamily};
                        font-size: 20px;
                        font-weight: 500;
                    }
                    .section-title {
                        font-family: ${theme.fontFamily};
                        font-size: 22px;
                        font-weight: 700;
                        text-transform: uppercase;
                        letter-spacing: 3px;
                    }
                    .body-text {
                        font-family: ${theme.fontFamily};
                        font-size: 18px;
                        line-height: 1.6;
                    }
                    .badge-text {
                        font-family: ${theme.fontFamily};
                        font-size: 12px;
                        font-weight: 800;
                    }
                    .animate-stagger {
                        opacity: 0;
                        animation: fadeIn 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
                    }
                    @keyframes fadeIn {
                        from { opacity: 0; transform: translateY(20px); }
                        to { opacity: 1; transform: translateY(0); }
                    }
                ]]>
            </style>
        </defs>
    """.trimIndent()

    private fun buildHeader(title: String, tags: String): String {
        val tagWidth = max(220, (tags.length * 8.5).toInt() + 40)
        val tagX = 1080 - tagWidth
        return """
        <g>
            <circle cx="1100" cy="100" r="300" fill="${theme.accentColor}" opacity="0.05"/>
            <circle cx="100" cy="1200" r="400" fill="${theme.accentColor}" opacity="0.03"/>
            
            <g class="animate-stagger" style="animation-delay: 100ms">
                <text x="130" y="140"
                      class="recipe-subtitle"
                      fill="var(--secondary)">Recipe Dossier</text>
    
                <text x="130" y="210"
                      class="recipe-title"
                      fill="var(--primary)"
                      filter="${if (!useDark) "" else "url(#drop_shadow)"}">${escapeXml(title)}</text>
            </g>

            <g class="animate-stagger" style="animation-delay: 200ms">
                <rect x="$tagX" y="135" width="$tagWidth" height="40" rx="20"
                      fill="${theme.surfaceImpact}"
                      stroke="var(--accent)" stroke-opacity="0.2"/>
                <text x="${tagX + tagWidth / 2}" y="161" text-anchor="middle"
                      class="meta-value"
                      font-size="14"
                      font-weight="600"
                      fill="var(--primary)">${escapeXml(tags)}</text>
            </g>
            
            <line x1="130" y1="260" x2="1080" y2="260" stroke="var(--accent)" stroke-width="2" stroke-opacity="0.3"/>
        </g>
    """.trimIndent()
    }

    private fun buildMetaTable(yield: String, prep: String, cook: String, summary: String, summaryHeight: Int): String {
        val summaryLines = wrapTextToWidth(summary, 600f)
            .ifEmpty { listOf(summary) }
            .take(4)

        val summaryStartY = 338
        val summaryLineHeight = 28

        val summarySvg = summaryLines.mapIndexed { index, line ->
            val y = summaryStartY + (index * summaryLineHeight)
            """<text x="130" y="$y" class="body-text" fill="var(--primary)" opacity="0.9">${escapeXml(line)}</text>"""
        }.joinToString("\n")

        val items = listOf(
            "Yield" to yield,
            "Prep Time" to prep,
            "Cook Time" to cook
        )
        
        val metaItemsSvg = items.mapIndexed { index, pair ->
            val xPos = 780
            val yPos = 330 + (index * 60)
            """
            <g class="animate-stagger" style="animation-delay: ${300 + index * 50}ms">
                <text x="$xPos" y="$yPos" class="meta-label" fill="var(--secondary)">${pair.first}</text>
                <text x="$xPos" y="${yPos + 30}" class="meta-value" fill="var(--primary)">${escapeXml(pair.second)}</text>
            </g>
            """.trimIndent()
        }.joinToString("\n")

        return """
            <g class="animate-stagger" style="animation-delay: 250ms">
                <rect x="90" y="290" width="1050" height="$summaryHeight" rx="24"
                      fill="${theme.canvas}"
                      fill-opacity="${if (useDark) "0.3" else "0.95"}"
                      filter="url(#drop_shadow)"/>
                
                <text x="130" y="320" class="meta-label" fill="var(--accent)">Description</text>
                $summarySvg
                
                <line x1="740" y1="310" x2="740" y2="${270 + summaryHeight}" stroke="var(--primary)" stroke-opacity="0.1" stroke-dasharray="4 4"/>
                
                $metaItemsSvg
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
            w = 520,
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
                    """<text x="$textX" y="$lineY" class="body-text" fill="var(--primary)">${escapeXml(line)}</text>"""
                )
            }

            currentY += (wrapped.size * rowGap) + if (hasBadge) 12 else 10
        }

        return """
        <g class="animate-stagger" style="animation-delay: ${delay}ms">
            <rect x="$x" y="$y" width="$w" height="$h" rx="24"
                  fill="${theme.canvas}"
                  fill-opacity="${if (useDark) "0.2" else "0.98"}"
                  filter="url(#drop_shadow)"/>
            
            <rect x="$x" y="$y" width="6" height="$h" fill="var(--accent)" rx="3"/>
            
            <text x="${x + 35}" y="${y + 50}"
                  class="section-title"
                  fill="var(--accent)">$title</text>
            
            <line x1="${x + 35}" y1="${y + 70}" x2="${x + 100}" y2="${y + 70}"
                  stroke="var(--accent)" stroke-width="3"/>
                  
            <g>
                $sb
            </g>
        </g>
    """.trimIndent()
    }


    private fun buildNotesAndTags(tags: String, notes: String, layout: Layout): String {
        val notesStartX = 130
        val notesLines = notes.split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .flatMap { line -> wrapTextToWidth(line, 940f) }
            .take(12)

        val notesY = layout.notesY
        val notesLineHeight = 28

        val notesSvg = notesLines.mapIndexed { index, line ->
            val y = notesY + 110 + (index * notesLineHeight)
            """<text x="$notesStartX" y="$y" class="body-text" fill="var(--primary)" opacity="0.8">${escapeXml(line)}</text>"""
        }.joinToString("\n")

        return """
            <g class="animate-stagger" style="animation-delay: 500ms">
                <rect x="90" y="$notesY" width="1050" height="${layout.notesHeight}" rx="24"
                      fill="${theme.canvas}"
                      fill-opacity="${if (useDark) "0.2" else "0.96"}"
                      filter="url(#drop_shadow)"/>
                
                <text x="130" y="${notesY + 50}" class="section-title" fill="var(--accent)">Notes &amp; Tags</text>
                <line x1="130" y1="${notesY + 65}" x2="200" y2="${notesY + 65}" stroke="var(--accent)" stroke-width="3"/>

                ${buildTagPills(tags, notesY + 80)}
                <g transform="translate(0, 80)">
                    $notesSvg
                </g>
            </g>
        """.trimIndent()
    }

    private fun buildTagPills(tags: String, startY: Int): String {
        val items = tags.split("•", ",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (items.isEmpty()) return ""

        val widths = items.map { max(80, it.length * 10 + 32) }
        var x = 130
        var y = startY
        val sb = StringBuilder()

        items.forEachIndexed { index, item ->
            val w = widths[index]
            if (x + w > 1050) {
                x = 130
                y += 56
            }
            sb.append(
                """
                <g class="animate-stagger" style="animation-delay: ${600 + index * 30}ms">
                    <rect x="$x" y="$y" width="$w" height="38" rx="19"
                          fill="var(--accent)" fill-opacity="0.15" stroke="var(--accent)" stroke-opacity="0.3"/>
                    <text x="${x + w / 2}" y="${y + 24}" text-anchor="middle"
                          class="badge-text" fill="var(--primary)">${escapeXml(item)}</text>
                </g>
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