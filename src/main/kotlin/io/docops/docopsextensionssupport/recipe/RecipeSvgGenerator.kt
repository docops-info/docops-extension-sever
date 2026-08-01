package io.docops.docopsextensionssupport.recipe

import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.FoodTheme
import io.docops.docopsextensionssupport.support.PremiumDarkTheme
import io.docops.docopsextensionssupport.support.PremiumTheme
import io.docops.docopsextensionssupport.support.SpringTheme
import io.docops.docopsextensionssupport.support.SummerTheme
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.support.wrapTextToWidth
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class RecipeSvgGenerator(
    private val useDark: Boolean = false
) {

    private var theme: DocOpsTheme = ThemeFactory.getTheme(useDark)
    @OptIn(ExperimentalUuidApi::class)
    private val id = "recipe_${Uuid.random().toHexString()}"
    private fun avgCharWidth() = 7.5f * theme.fontWidthMultiplier
    private fun defId(suffix: String) = "${id}_$suffix"
    private fun defUrl(suffix: String) = "url(#${defId(suffix)})"

    private data class Layout(
        val summaryHeight: Int,
        val middleHeight: Int,
        val notesHeight: Int,
        val tagsHeight: Int,
        val totalHeight: Int,
        val notesY: Int,
        val tagsY: Int,
        val ingredientsHeight: Int,
        val stepsHeight: Int,
        val ingredientsLines: List<List<String>>,
        val stepLines: List<List<String>>,
        val noteLines: List<List<String>>,
        val bodyPanelY: Int,
        val sideMargin: Int,
        val columnGap: Int
    )

    private data class SectionLine(
        val number: Int? = null,
        val text: String
    )

    fun createSvg(recipe: Recipe, scale: Double = 1.0, isPdf: Boolean = false): String {
        val width = 680

        theme = ThemeFactory.getThemeByName(recipe.theme, useDark)

        val title = recipe.title ?: "Recipe"
        val yield = recipe.yield ?: "-"
        val prep = recipe.prep ?: "-"
        val cook = recipe.cook ?: "-"
        val summary = recipe.summary ?: ""
        val tags = if (recipe.tags.isNotEmpty()) recipe.tags.joinToString(" · ") else "untagged"

        val layout = measureLayout(recipe)

        return """
            <svg xmlns="http://www.w3.org/2000/svg"
                 width="${(width * scale).roundToInt()}" height="${(layout.totalHeight * scale).roundToInt()}"
                 viewBox="0 0 $width ${layout.totalHeight}" id="$id">
                <title>$title — Recipe Card</title>
                <desc>A styled recipe card with warm tones and decorative borders</desc>
                ${if (!isPdf) svgDefs() else ""}
                <g transform="scale($scale)">
                    <!-- Background -->
                    <rect width="$width" height="${layout.totalHeight}" fill="${theme.canvas}"/>
                    ${when(theme) {
                        is FoodTheme -> """<rect width="$width" height="${layout.totalHeight}" fill="url(#${defId("linen")})"/>"""
                        is SpringTheme -> """<rect width="$width" height="${layout.totalHeight}" fill="url(#${defId("sp_dot")})"/>"""
                        is SummerTheme -> """<rect width="$width" height="${layout.totalHeight}" fill="url(#${defId("su_wave")})"/>"""
                        else -> ""
                    }}
                    
                    <!-- Decorative Borders -->
                    ${when (theme) {
                        is PremiumTheme, is PremiumDarkTheme -> """<rect x="16" y="16" width="${width - 32}" height="${layout.totalHeight - 32}" rx="${theme.cornerRadius}" fill="${theme.canvas}" filter="url(#${defId("shadow")})"/>"""
                        else -> if (theme !is PremiumTheme && theme !is PremiumDarkTheme) """<rect x="14" y="14" width="${width - 28}" height="${layout.totalHeight - 28}" rx="${theme.cornerRadius}" fill="none" stroke="${theme.accentColor}" stroke-width="1.2" stroke-opacity="0.5" stroke-dasharray="8 4"/>""" else ""
                    }}

                    ${buildCornerOrnaments(width, layout.totalHeight)}
                    ${buildHeader(title, tags, layout.sideMargin)}
                    ${buildMetaInfoStrip(yield, prep, cook, summary, layout)}
                    ${buildBodyPanels(recipe.ingredients, recipe.steps, layout)}
                    ${buildNotesAndTags(recipe.tags, recipe.notes, layout)}
                </g>
            </svg>
        """.trimIndent()
    }

    private fun buildCornerOrnaments(w: Int, h: Int): String {
        if (theme is PremiumTheme || theme is PremiumDarkTheme) return ""
        return when(theme) {
            is SpringTheme -> buildSpringCorners(w, h)
            is SummerTheme -> buildSummerCorners(w, h)
            else -> buildDefaultCorners(w, h)
        }
    }

    private fun buildDefaultCorners(w: Int, h: Int): String {
        val ornamentOpacity = if (useDark) "0.45" else "0.55"
        val circleOpacity = if (useDark) "0.3" else "0.4"
        return """
            <g fill="none" stroke="${theme.accentColor}" stroke-width="1" stroke-opacity="$ornamentOpacity">
                <!-- Top Left -->
                <path d="M14 50 L14 14 L50 14"/>
                <circle cx="14" cy="14" r="3.5" fill="${theme.accentColor}" fill-opacity="$circleOpacity" stroke="none"/>
                <!-- Top Right -->
                <path d="M${w-14} 50 L${w-14} 14 L${w-50} 14"/>
                <circle cx="${w-14}" cy="14" r="3.5" fill="${theme.accentColor}" fill-opacity="$circleOpacity" stroke="none"/>
                <!-- Bottom Left -->
                <path d="M14 ${h-50} L14 ${h-14} L50 ${h-14}"/>
                <circle cx="14" cy="${h-14}" r="3.5" fill="${theme.accentColor}" fill-opacity="$circleOpacity" stroke="none"/>
                <!-- Bottom Right -->
                <path d="M${w-14} ${h-50} L${w-14} ${h-14} L${w-50} ${h-14}"/>
                <circle cx="${w-14}" cy="${h-14}" r="3.5" fill="${theme.accentColor}" fill-opacity="$circleOpacity" stroke="none"/>
            </g>
        """.trimIndent()
    }

    private fun buildSpringCorners(w: Int, h: Int): String {
        fun flower(x: Int, y: Int) = """
            <g transform="translate($x, $y)">
                <circle cx="12" cy="12" r="4" fill="#E8A0B8" fill-opacity="0.6"/>
                <ellipse cx="12" cy="5" rx="3.5" ry="5" fill="#F2B8CC" fill-opacity="0.7"/>
                <ellipse cx="12" cy="19" rx="3.5" ry="5" fill="#F2B8CC" fill-opacity="0.7"/>
                <ellipse cx="5" cy="12" rx="5" ry="3.5" fill="#F2B8CC" fill-opacity="0.7"/>
                <ellipse cx="19" cy="12" rx="5" ry="3.5" fill="#F2B8CC" fill-opacity="0.7"/>
                <circle cx="12" cy="12" r="2.5" fill="#FDE8F0"/>
            </g>
        """.trimIndent()
        return """
            <g>
                ${flower(10, 10)}
                ${flower(w - 34, 10)}
                ${flower(10, h - 34)}
                ${flower(w - 34, h - 34)}
            </g>
        """.trimIndent()
    }

    private fun buildSummerCorners(w: Int, h: Int): String {
        fun sun(x: Int, y: Int) = """
            <g transform="translate($x, $y)">
                <circle cx="13" cy="13" r="6" fill="#F5D840" fill-opacity="0.75"/>
                <g stroke="#F5C820" stroke-width="1.5" stroke-linecap="round">
                    <line x1="13" y1="3" x2="13" y2="0"/>
                    <line x1="13" y1="23" x2="13" y2="26"/>
                    <line x1="3" y1="13" x2="0" y2="13"/>
                    <line x1="23" y1="13" x2="26" y2="13"/>
                    <line x1="6" y1="6" x2="4" y2="4" stroke-width="1.2"/>
                    <line x1="20" y1="6" x2="22" y2="4" stroke-width="1.2"/>
                    <line x1="6" y1="20" x2="4" y2="22" stroke-width="1.2"/>
                    <line x1="20" y1="20" x2="22" y2="22" stroke-width="1.2"/>
                </g>
            </g>
        """.trimIndent()
        return """
            <g>
                ${sun(10, 10)}
                ${sun(w - 36, 10)}
                ${sun(10, h - 36)}
                ${sun(w - 36, h - 36)}
            </g>
        """.trimIndent()
    }

    private fun measureLayout(recipe: Recipe): Layout {
        val width = 680
        val sideMargin = if (theme is PremiumTheme || theme is PremiumDarkTheme) 32 else 44
        val columnGap = if (theme is PremiumTheme || theme is PremiumDarkTheme) 24 else 20
        val contentWidth = width - (sideMargin * 2)
        val columnWidth = (contentWidth - columnGap) / 2

        val isPremium = theme is PremiumTheme || theme is PremiumDarkTheme
        val textMultiplier = if (isPremium) 1.05f else 1.0f

        val allSummaryLines = wrapTextToWidth(recipe.summary.orEmpty(), contentWidth.toFloat() - 32, avgCharWidth() * textMultiplier)
            .ifEmpty { listOf(recipe.summary.orEmpty()) }
        
        val summaryLines = if (allSummaryLines.size > 3) {
            val lines = allSummaryLines.take(3).toMutableList()
            lines[2] = lines[2].let { line ->
                if (line.length > 3) line.dropLast(3) + "..." else line + "..."
            }
            lines
        } else {
            allSummaryLines
        }

        val ingredientRowGap = 24
        val ingredientLinesNested = mutableListOf<List<String>>()
        var totalIngredientLines = 0
        recipe.ingredients.forEach { line ->
            val wrapped = wrapTextToWidth(line, (columnWidth - 64).toFloat(), avgCharWidth() * textMultiplier)
            ingredientLinesNested.add(wrapped)
            totalIngredientLines += wrapped.size
        }
        if (recipe.ingredients.isEmpty()) {
            ingredientLinesNested.add(listOf("No ingredients provided."))
            totalIngredientLines = 1
        }
        val ingredientsHeight = max(240, 60 + totalIngredientLines * ingredientRowGap)

        val stepRowGap = 24
        val stepLinesNested = mutableListOf<List<String>>()
        var totalStepLines = 0
        recipe.steps.forEach { line ->
            val clean = line
                .removePrefix("- ")
                .removePrefix("* ")
                .trim()
                .replace(Regex("^\\d+[.)]\\s*"), "")
            val wrapped = wrapTextToWidth(clean, (columnWidth - 70).toFloat(), avgCharWidth() * textMultiplier).ifEmpty { listOf("") }
            stepLinesNested.add(wrapped)
            totalStepLines += wrapped.size
        }
        if (recipe.steps.isEmpty()) {
            stepLinesNested.add(listOf("No steps provided."))
            totalStepLines = 1
        }
        val stepsHeight = max(240, 60 + totalStepLines * stepRowGap)

        // Notes height calculation
        val noteLinesNested = recipe.notes.map { note ->
            wrapTextToWidth(note, contentWidth.toFloat() - 44, avgCharWidth() * textMultiplier)
        }
        val noteLineHeight = 24
        val noteGap = 12
        var currentNotesHeight = 0
        if (recipe.notes.isNotEmpty()) {
            currentNotesHeight = 54 // Title and padding
            noteLinesNested.forEachIndexed { i, lines ->
                currentNotesHeight += lines.size * noteLineHeight
                if (i < noteLinesNested.size - 1) currentNotesHeight += noteGap
            }
            currentNotesHeight += 12 // Bottom buffer
        }
        val notesHeight = currentNotesHeight

        // Tags height calculation with wrapping
        var tagRows = 0
        if (recipe.tags.isNotEmpty()) {
            tagRows = 1
            var currentX = 0
            recipe.tags.forEach { tag ->
                val tw = (tag.length * 8) + 34 // 30 (paddings) + 4 (buffer)
                if (currentX + tw > contentWidth) {
                    tagRows++
                    currentX = tw + 10
                } else {
                    currentX += tw + 10
                }
            }
        }
        val tagsHeight = if (recipe.tags.isNotEmpty()) tagRows * 36 + (tagRows - 1) * 8 else 0

        val summaryHeight = 20 + summaryLines.size * 22
        val middleHeight = max(ingredientsHeight, stepsHeight)

        val headerHeight = 200
        val metaStripHeight = 72
        val gap = if (theme is PremiumTheme || theme is PremiumDarkTheme) 16 else 14
        val summaryPadding = if (theme is PremiumTheme || theme is PremiumDarkTheme) 24 else 20
        
        val bodyPanelY = headerHeight + metaStripHeight + gap + summaryHeight + summaryPadding
        val notesY = bodyPanelY + middleHeight + (if (theme is PremiumTheme || theme is PremiumDarkTheme) 32 else 30)
        
        val tagsY = if (notesHeight > 0) notesY + notesHeight + (if (theme is PremiumTheme || theme is PremiumDarkTheme) 24 else 20) else notesY
        
        val footerPadding = 60
        val finalContentY = if (tagsHeight > 0) tagsY + tagsHeight else if (notesHeight > 0) notesY + notesHeight else notesY
        val totalHeight = finalContentY + footerPadding

        return Layout(
            summaryHeight = summaryHeight,
            middleHeight = middleHeight,
            notesHeight = notesHeight,
            tagsHeight = tagsHeight,
            totalHeight = max(800, totalHeight),
            notesY = notesY,
            tagsY = tagsY,
            ingredientsHeight = ingredientsHeight,
            stepsHeight = stepsHeight,
            ingredientsLines = ingredientLinesNested,
            stepLines = stepLinesNested,
            noteLines = noteLinesNested,
            bodyPanelY = bodyPanelY,
            sideMargin = sideMargin,
            columnGap = columnGap
        )
    }

    private fun svgDefs(): String = """
        <defs>
            ${theme.fontImport}
            <filter id="${defId("shadow")}" x="-20%" y="-20%" width="140%" height="140%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="3"/>
                <feOffset dx="0" dy="2" result="offsetblur"/>
                <feComponentTransfer>
                    <feFuncA type="linear" slope="0.1"/>
                </feComponentTransfer>
                <feMerge>
                    <feMergeNode/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>
            <pattern id="${defId("linen")}" width="8" height="8" patternUnits="userSpaceOnUse">
                <line x1="0" y1="8" x2="8" y2="0" stroke="${theme.accentColor}" stroke-width="0.4" stroke-opacity="0.18"/>
                <line x1="-2" y1="2" x2="2" y2="-2" stroke="${theme.accentColor}" stroke-width="0.4" stroke-opacity="0.1"/>
            </pattern>
            <pattern id="${defId("dots")}" width="12" height="12" patternUnits="userSpaceOnUse">
                <circle cx="6" cy="6" r="1" fill="${theme.accentColor}" fill-opacity="0.12"/>
            </pattern>
            <pattern id="${defId("sp_dot")}" width="18" height="18" patternUnits="userSpaceOnUse">
                <circle cx="9" cy="9" r="1.2" fill="${theme.accentColor}" fill-opacity="0.13"/>
            </pattern>
            <pattern id="${defId("sp_stripe")}" width="10" height="10" patternUnits="userSpaceOnUse">
                <line x1="0" y1="10" x2="10" y2="0" stroke="#A8D4A0" stroke-width="0.5" stroke-opacity="0.2"/>
            </pattern>
            <pattern id="${defId("su_wave")}" width="20" height="10" patternUnits="userSpaceOnUse">
                <path d="M0 5 Q5 0 10 5 Q15 10 20 5" fill="none" stroke="${theme.accentColor}" stroke-width="0.5" stroke-opacity="0.12"/>
            </pattern>
            <pattern id="${defId("su_check")}" width="16" height="16" patternUnits="userSpaceOnUse">
                <line x1="8" y1="0" x2="8" y2="16" stroke="${theme.accentColor}" stroke-width="0.5" stroke-opacity="0.08"/>
                <line x1="0" y1="8" x2="16" y2="8" stroke="${theme.accentColor}" stroke-width="0.5" stroke-opacity="0.08"/>
            </pattern>

            <style>
                <![CDATA[
                    #$id {
                        --primary: ${theme.primaryText};
                        --secondary: ${theme.secondaryText};
                        --accent: ${theme.accentColor};
                        --canvas: ${theme.canvas};
                    }
                    #$id .recipe-title {
                        font-family: ${theme.fontFamily};
                        font-size: ${if (theme is PremiumTheme || theme is PremiumDarkTheme) "36px" else "34px"};
                        font-weight: 700;
                        letter-spacing: -0.5px;
                    }
                    #$id .recipe-subtitle {
                        font-family: ${theme.fontFamily};
                        font-size: 12px;
                        font-weight: 400;
                        letter-spacing: 2px;
                        opacity: 0.85;
                    }
                    #$id .meta-label {
                        font-family: ${theme.fontFamily};
                        font-size: 12px;
                        font-weight: 700;
                        letter-spacing: 1.5px;
                        text-transform: uppercase;
                    }
                    #$id .meta-value {
                        font-family: ${theme.fontFamily};
                        font-size: 17px;
                        font-weight: 600;
                    }
                    #$id .section-title {
                        font-family: ${theme.fontFamily};
                        font-size: 12px;
                        font-weight: 700;
                        text-transform: uppercase;
                        letter-spacing: 3.5px;
                    }
                    #$id .body-text {
                        font-family: ${theme.fontFamily};
                        font-size: 14px;
                        line-height: 1.6;
                    }
                    #$id .summary-text {
                        font-family: ${theme.fontFamily};
                        font-size: 14px;
                        font-style: italic;
                    }
                ]]>
            </style>
        </defs>
    """.trimIndent()

    private fun buildHeader(title: String, tags: String, sideMargin: Int): String {
        val (titleColor, subtitleColor, tagColor) = when(theme) {
            is SpringTheme -> Triple("#FFF0F5", "#7A2848", "#FCE4EE")
            is SummerTheme -> Triple("#FFFFFF", "#A8D0F8", "#E8F4FF")
            is PremiumTheme, is PremiumDarkTheme -> Triple("var(--primary)", "var(--secondary)", "var(--secondary)")
            else -> Triple("#FFF8EE", "#FDEBD0", "#FDDDB8")
        }
        val headerRectFill = when(theme) {
            is SpringTheme -> "#EDA0BA"
            is SummerTheme -> theme.accentColor
            is PremiumTheme, is PremiumDarkTheme -> theme.canvas
            else -> theme.secondaryText
        }
        val headerPattern = when(theme) {
            is SpringTheme -> defId("sp_stripe")
            is SummerTheme -> defId("su_check")
            is PremiumTheme, is PremiumDarkTheme -> ""
            else -> defId("dots")
        }
        val headerWidth = 680 - (sideMargin * 2)
        val headerY = if (theme is PremiumTheme || theme is PremiumDarkTheme) 32 else 30
        
        return """
        <g>
            <rect x="$sideMargin" y="$headerY" width="$headerWidth" height="148" rx="${theme.cornerRadius}" fill="$headerRectFill" fill-opacity="0.92"/>
            ${if (headerPattern.isNotEmpty()) """<rect x="$sideMargin" y="$headerY" width="$headerWidth" height="148" rx="${theme.cornerRadius}" fill="url(#$headerPattern)"/>""" else ""}
            
            <text x="340" y="${headerY + 50}" text-anchor="middle" class="recipe-subtitle" fill="$subtitleColor" letter-spacing="5" opacity="0.9">RECIPE DOSSIER</text>
            <text x="340" y="${headerY + 95}" text-anchor="middle" class="recipe-title" fill="$titleColor" letter-spacing="-0.5">${escapeXml(title)}</text>
            <text x="340" y="${headerY + 126}" text-anchor="middle" font-family="${theme.fontFamily}" font-size="12" fill="$tagColor" letter-spacing="2" opacity="0.85">${escapeXml(tags)}</text>

            <!-- Decorative divider under header -->
            ${buildDecorativeDivider(headerY + 158, sideMargin)}
        </g>
        """.trimIndent()
    }

    private fun buildDecorativeDivider(y: Int, sideMargin: Int): String {
        return when(theme) {
            is SpringTheme -> """
                <g transform="translate(0, $y)">
                    <path d="M60 0 Q120 -8 180 0 Q240 8 300 0 Q360 -8 420 0 Q480 8 540 0 Q600 -8 620 0" fill="none" stroke="#80C080" stroke-width="1.2" stroke-opacity="0.5" stroke-linecap="round"/>
                    <circle cx="120" cy="-4" r="3" fill="#80C080" fill-opacity="0.45"/>
                    <circle cx="300" cy="-4" r="3" fill="#80C080" fill-opacity="0.45"/>
                    <circle cx="480" cy="-4" r="3" fill="#80C080" fill-opacity="0.45"/>
                </g>
            """.trimIndent()
            is SummerTheme -> """
                <g transform="translate(0, $y)">
                    <path d="M44 0 Q100 -8 160 0 Q220 8 280 0 Q340 -8 400 0 Q460 8 520 0 Q580 -8 636 0" fill="none" stroke="${theme.accentColor}" stroke-width="1" stroke-opacity="0.35"/>
                    <circle cx="160" cy="-4" r="3" fill="#F5D840" fill-opacity="0.65"/>
                    <circle cx="340" cy="-4" r="3" fill="${theme.accentColor}" fill-opacity="0.5"/>
                    <circle cx="520" cy="-4" r="3" fill="#F5D840" fill-opacity="0.65"/>
                </g>
            """.trimIndent()
            is PremiumTheme, is PremiumDarkTheme -> """
                <g transform="translate(0, $y)">
                    <line x1="$sideMargin" y1="0" x2="${680 - sideMargin}" y2="0" stroke="var(--accent)" stroke-width="1" stroke-opacity="0.1"/>
                </g>
            """.trimIndent()
            else -> """
                <g transform="translate(0, $y)">
                    <line x1="80" y1="0" x2="300" y2="0" stroke="var(--accent)" stroke-width="0.8" stroke-opacity="0.4"/>
                    <circle cx="340" cy="0" r="4" fill="var(--accent)" fill-opacity="0.5"/>
                    <line x1="380" y1="0" x2="600" y2="0" stroke="var(--accent)" stroke-width="0.8" stroke-opacity="0.4"/>
                    <path d="M320 0 Q330 -5 340 -4 Q350 -5 360 0" fill="none" stroke="var(--accent)" stroke-width="0.8" stroke-opacity="0.4"/>
                </g>
            """.trimIndent()
        }
    }

    private fun buildMetaInfoStrip(yield: String, prep: String, cook: String, summary: String, layout: Layout): String {
        val (stripFill, stripStroke, labelColor, valueColor) = when(theme) {
            is SpringTheme -> listOf("#F0FAF0", "#90C890", "#3A7040", "#1E4820")
            is SummerTheme -> listOf("#E8F4FF", "#5090C8", "#1058A0", "#0A3870")
            is PremiumTheme, is PremiumDarkTheme -> listOf("var(--canvas)", "var(--accent)", "var(--secondary)", "var(--primary)")
            else -> listOf("var(--accent)", "var(--accent)", "var(--secondary)", "var(--primary)")
        }
        val stripFillOpacity = if (theme is SpringTheme || theme is SummerTheme) "0.8" else if (theme is PremiumTheme || theme is PremiumDarkTheme) "1.0" else "0.1"
        val stripStrokeOpacity = if (theme is SpringTheme || theme is SummerTheme) "0.5" else if (theme is PremiumTheme || theme is PremiumDarkTheme) "0.1" else "0.3"
        
        val contentWidth = 680 - (layout.sideMargin * 2)
        val textMultiplier = if (theme is PremiumTheme || theme is PremiumDarkTheme) 1.05f else 1.0f
        val allSummaryLines = wrapTextToWidth(summary, contentWidth.toFloat() - 32, avgCharWidth() * textMultiplier)
            .ifEmpty { listOf(summary) }
        
        val summaryLines = if (allSummaryLines.size > 3) {
            val lines = allSummaryLines.take(3).toMutableList()
            lines[2] = lines[2].let { line ->
                if (line.length > 3) line.dropLast(3) + "..." else line + "..."
            }
            lines
        } else {
            allSummaryLines
        }
        
        val summarySvg = summaryLines.mapIndexed { index, line ->
            val y = 296 + (index * 22)
            val summaryColor = when(theme) {
                is SpringTheme -> "#2A3828"
                is SummerTheme -> "#0A3870"
                else -> "var(--secondary)"
            }
            """<text x="340" y="$y" text-anchor="middle" class="summary-text" fill="$summaryColor" fill-opacity="0.85">${escapeXml(line)}</text>"""
        }.joinToString("\n")

        val itemWidth = contentWidth / 3.0
        
        return """
        <g>
            <rect x="${layout.sideMargin}" y="202" width="$contentWidth" height="72" rx="${theme.cornerRadius}" fill="$stripFill" fill-opacity="$stripFillOpacity" stroke="$stripStroke" stroke-width="0.5" stroke-opacity="$stripStrokeOpacity"/>
            
            <g transform="translate(${layout.sideMargin}, 202)">
                <!-- Yield -->
                <text x="${itemWidth / 2}" y="26" text-anchor="middle" class="meta-label" fill="$labelColor">YIELD</text>
                <text x="${itemWidth / 2}" y="50" text-anchor="middle" class="meta-value" fill="$valueColor">${escapeXml(yield)}</text>
                
                <line x1="$itemWidth" y1="12" x2="$itemWidth" y2="60" stroke="$stripStroke" stroke-width="0.5" stroke-opacity="0.35"/>
                
                <!-- Prep -->
                <text x="${itemWidth + itemWidth / 2}" y="26" text-anchor="middle" class="meta-label" fill="$labelColor">PREP TIME</text>
                <text x="${itemWidth + itemWidth / 2}" y="50" text-anchor="middle" class="meta-value" fill="$valueColor">${escapeXml(prep)}</text>
                
                <line x1="${itemWidth * 2}" y1="12" x2="${itemWidth * 2}" y2="60" stroke="$stripStroke" stroke-width="0.5" stroke-opacity="0.35"/>
                
                <!-- Cook -->
                <text x="${itemWidth * 2 + itemWidth / 2}" y="26" text-anchor="middle" class="meta-label" fill="$labelColor">COOK TIME</text>
                <text x="${itemWidth * 2 + itemWidth / 2}" y="50" text-anchor="middle" class="meta-value" fill="$valueColor">${escapeXml(cook)}</text>
            </g>
            
            $summarySvg
            
            <line x1="${layout.sideMargin}" y1="${285 + layout.summaryHeight}" x2="${680 - layout.sideMargin}" y2="${285 + layout.summaryHeight}" stroke="var(--accent)" stroke-width="0.5" stroke-opacity="0.3"/>
        </g>
        """.trimIndent()
    }

    private fun buildBodyPanels(ingredients: List<String>, steps: List<String>, layout: Layout): String {
        val panelY = layout.bodyPanelY
        val contentWidth = 680 - (layout.sideMargin * 2)
        val columnWidth = (contentWidth - layout.columnGap) / 2
        
        return """
        <g transform="translate(${layout.sideMargin}, $panelY)">
            ${buildSection(0, 0, columnWidth, layout.middleHeight, "INGREDIENTS", layout.ingredientsLines, false)}
            <g transform="translate(${columnWidth + layout.columnGap}, 0)">
                ${buildSection(0, 0, columnWidth, layout.middleHeight, "STEPS", layout.stepLines, true)}
            </g>
        </g>
        """.trimIndent()
    }

    private fun buildSection(x: Int, y: Int, w: Int, h: Int, title: String, itemLines: List<List<String>>, isSteps: Boolean): String {
        val accentColor = when(theme) {
            is SpringTheme -> if (!isSteps) "#3A7040" else "#8C3055"
            is SummerTheme -> if (!isSteps) "#F5C820" else theme.accentColor
            is PremiumTheme, is PremiumDarkTheme -> theme.accentColor
            else -> if (!isSteps) "#4A8A38" else theme.accentColor
        }
        val sb = StringBuilder()
        
        sb.append("""
            <rect x="$x" y="$y" width="$w" height="$h" rx="${theme.cornerRadius}" fill="${theme.canvas}" fill-opacity="${if (theme is PremiumTheme || theme is PremiumDarkTheme) "1.0" else "0.4"}" stroke="${theme.accentColor}" stroke-width="1" stroke-opacity="0.5"/>
            <rect x="$x" y="$y" width="6" height="$h" rx="3" fill="$accentColor" fill-opacity="0.7"/>
            <text x="${x + 28}" y="${y + 32}" class="section-title" fill="$accentColor">${escapeXml(title)}</text>
            <line x1="${x + 28}" y1="${y + 39}" x2="${x + w - 40}" y2="${y + 39}" stroke="$accentColor" stroke-width="1.2" stroke-opacity="0.6"/>
        """.trimIndent())
        
        var currentY = y + 66
        val rowGap = 24
        
        itemLines.forEachIndexed { itemIndex, lines ->
            val itemNum = itemIndex + 1
            lines.forEachIndexed { lineIndex, line ->
                if (isSteps) {
                    val stepAccent = when(theme) {
                        is SpringTheme -> "#D47898"
                        is SummerTheme -> theme.accentColor
                        else -> theme.accentColor
                    }
                    if (lineIndex == 0) {
                        sb.append("""
                            <circle cx="${x + 36}" cy="${currentY - 5}" r="11" fill="$stepAccent" fill-opacity="0.85"/>
                            <text x="${x + 36}" y="${currentY}" text-anchor="middle" font-size="11" fill="#FFF5EE" font-weight="700" font-family="${theme.fontFamily}">${itemNum}</text>
                        """.trimIndent())
                    }
                    sb.append("""
                        <text x="${x + 54}" y="${currentY}" class="body-text" fill="var(--primary)">${escapeXml(line)}</text>
                    """.trimIndent())
                } else {
                    val bulletColor = when(theme) {
                        is SpringTheme -> "#80C080"
                        is SummerTheme -> "#F5C820"
                        else -> theme.accentColor
                    }
                    if (lineIndex == 0) {
                        sb.append("""
                            <circle cx="${x + 34}" cy="${currentY - 5}" r="2.5" fill="$bulletColor" fill-opacity="0.7"/>
                        """.trimIndent())
                    }
                    sb.append("""
                        <text x="${x + 48}" y="${currentY}" class="body-text" fill="var(--primary)">${escapeXml(line)}</text>
                    """.trimIndent())
                }
                currentY += rowGap
            }
        }
        
        return sb.toString()
    }

    private fun buildNotesAndTags(tags: List<String>, notes: List<String>, layout: Layout): String {
        if (notes.isEmpty() && tags.isEmpty()) return ""
        
        val sb = StringBuilder()
        val contentWidth = 680 - (layout.sideMargin * 2)
        
        if (notes.isNotEmpty()) {
            val h = layout.notesHeight
            val notesY = layout.notesY
            val (noteRectFill, noteRectStroke, noteTitleColor, noteLineColor) = when(theme) {
                is SpringTheme -> listOf("#F5FAF0", "#80C080", "#3A7040", "#80C080")
                is SummerTheme -> listOf("#E8F4FF", "#5090C8", "#1058A0", "#5090C8")
                is PremiumTheme, is PremiumDarkTheme -> listOf("var(--canvas)", "var(--accent)", "var(--secondary)", "var(--accent)")
                else -> listOf(theme.accentColor, theme.accentColor, "var(--secondary)", theme.accentColor)
            }
            val noteRectOpacity = if (theme is SpringTheme || theme is SummerTheme) "0.7" else if (theme is PremiumTheme || theme is PremiumDarkTheme) "1.0" else "0.05"
            val noteRectStrokeDash = if (theme is SpringTheme || theme is SummerTheme) "5 5" else if (theme is PremiumTheme || theme is PremiumDarkTheme) "" else "6 4"
            
            sb.append("""
                <g transform="translate(${layout.sideMargin}, $notesY)">
                    <rect width="$contentWidth" height="$h" rx="${theme.cornerRadius}" fill="$noteRectFill" fill-opacity="$noteRectOpacity" stroke="$noteRectStroke" stroke-width="0.8" stroke-opacity="0.45" stroke-dasharray="$noteRectStrokeDash"/>
                    <text x="28" y="28" class="section-title" fill="$noteTitleColor">COOK'S NOTES</text>
                    <line x1="28" y1="35" x2="150" y2="35" stroke="$noteLineColor" stroke-width="1" stroke-opacity="0.5"/>
            """.trimIndent())
            
            var currentNoteY = 60
            layout.noteLines.forEachIndexed { i, lines ->
                lines.forEach { line ->
                    val noteTextColor = when(theme) {
                        is SpringTheme -> "#2A3828"
                        is SummerTheme -> "#0A3870"
                        else -> "var(--primary)"
                    }
                    sb.append("""<text x="28" y="$currentNoteY" class="summary-text" fill="$noteTextColor" fill-opacity="0.9">· ${escapeXml(line)}</text>""")
                    currentNoteY += 24
                }
                if (i < layout.noteLines.size - 1) currentNoteY += 12
            }
            sb.append("</g>")
        }
        
        if (tags.isNotEmpty()) {
            val tagsY = layout.tagsY
            sb.append("""<g transform="translate(${layout.sideMargin}, $tagsY)">""")
            var tx = 0
            var ty = 0
            tags.forEach { tag ->
                val tw = (tag.length * 8) + 34
                if (tx + tw > contentWidth) {
                    tx = 0
                    ty += 44
                }
                val (tagFill, tagStroke, tagTextColor) = when(theme) {
                    is SpringTheme -> Triple("#F2B8CC", "#D47898", "#7A2848")
                    is SummerTheme -> Triple(theme.accentColor, theme.accentColor, "#0A3870")
                    is PremiumTheme, is PremiumDarkTheme -> {
                        val secondary = if (theme.chartPalette.size > 1) theme.chartPalette[1].color else "#8B5CF6"
                        Triple(secondary, "none", "var(--primary)")
                    }
                    else -> Triple(theme.accentColor, theme.accentColor, "var(--primary)")
                }
                val tagFillOpacity = if (theme is SummerTheme) "0.12" else if (theme is PremiumTheme || theme is PremiumDarkTheme) "0.1" else "0.35"
                val tagStrokeOpacity = if (theme is PremiumTheme || theme is PremiumDarkTheme) "0.0" else "0.4"
                
                sb.append("""
                    <rect x="$tx" y="$ty" width="$tw" height="32" rx="16" fill="$tagFill" fill-opacity="$tagFillOpacity" stroke="$tagStroke" stroke-width="0.8" stroke-opacity="$tagStrokeOpacity"/>
                    <text x="${tx + tw/2}" y="${ty + 21}" text-anchor="middle" font-family="${theme.fontFamily}" font-size="12" fill="$tagTextColor" font-weight="700">${escapeXml(tag)}</text>
                """.trimIndent())
                tx += tw + 10
            }
            sb.append("</g>")
        }
        
        val finalY = if (layout.tagsHeight > 0) layout.tagsY + layout.tagsHeight + 10 
                     else if (layout.notesHeight > 0) layout.notesY + layout.notesHeight + 10
                     else layout.notesY
        
        // Decorative divider
        sb.append(buildBottomDivider(finalY, layout.sideMargin))
        
        return sb.toString()
    }

    private fun buildBottomDivider(y: Int, sideMargin: Int): String {
        return when(theme) {
            is SpringTheme -> """
                <g transform="translate(0, $y)">
                    <path d="M44 14 Q160 6 280 14 Q340 18 400 14 Q520 6 636 14" fill="none" stroke="#90C890" stroke-width="0.8" stroke-opacity="0.4"/>
                    <circle cx="170" cy="10" r="2.5" fill="#F2B8CC" fill-opacity="0.6"/>
                    <circle cx="340" cy="11" r="2.5" fill="#80C080" fill-opacity="0.6"/>
                    <circle cx="510" cy="10" r="2.5" fill="#F2B8CC" fill-opacity="0.6"/>
                </g>
            """.trimIndent()
            is SummerTheme -> """
                <g transform="translate(0, $y)">
                    <path d="M44 14 Q120 6 200 14 Q280 22 360 14 Q440 6 520 14 Q580 20 636 14" fill="none" stroke="${theme.accentColor}" stroke-width="0.8" stroke-opacity="0.35"/>
                    <rect x="333" y="8" width="14" height="14" rx="1" transform="rotate(45 340 15)" fill="#F5D840" fill-opacity="0.55"/>
                </g>
            """.trimIndent()
            is PremiumTheme, is PremiumDarkTheme -> """
                <g transform="translate(0, $y)">
                    <line x1="$sideMargin" y1="14" x2="${680 - sideMargin}" y2="14" stroke="var(--accent)" stroke-width="1" stroke-opacity="0.1"/>
                </g>
            """.trimIndent()
            else -> """
                <g transform="translate(0, $y)">
                    <line x1="44" y1="14" x2="280" y2="14" stroke="${theme.accentColor}" stroke-width="0.6" stroke-opacity="0.35"/>
                    <g transform="translate(318, 6)" fill="${theme.accentColor}" fill-opacity="0.45">
                        <path d="M0 8 Q6 0 12 8 Q18 0 24 8" fill="none" stroke="${theme.accentColor}" stroke-width="1" stroke-opacity="0.5"/>
                        <circle cx="12" cy="8" r="2"/>
                    </g>
                    <line x1="400" y1="14" x2="636" y2="14" stroke="${theme.accentColor}" stroke-width="0.6" stroke-opacity="0.35"/>
                </g>
            """.trimIndent()
        }
    }

    private fun buildFooter(layout: Layout): String {
        return """
        <g transform="translate(340, ${layout.totalHeight - 30})">
            <text text-anchor="middle" font-family="${theme.fontFamily}" font-size="10" fill="var(--secondary)" letter-spacing="2" fill-opacity="0.6">RECIPE DOSSIER</text>
        </g>
        """.trimIndent()
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