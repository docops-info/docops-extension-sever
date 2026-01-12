package io.docops.docopsextensionssupport.gherkin

import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import org.springframework.stereotype.Service

class GherkinMaker(val useDark: Boolean) {
    // Resolve the theme once for the entire generator
    private lateinit var theme: DocOpsTheme

    private fun wrapText(text: String, maxWidthPx: Int, fontSizePx: Int): List<String> {
        val avgCharWidth = fontSizePx * 0.55 // Heuristic for sans-serif
        val maxChars = (maxWidthPx / avgCharWidth).toInt().coerceAtLeast(1)
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            if (currentLine.isEmpty()) {
                currentLine.append(word)
            } else if (currentLine.length + 1 + word.length <= maxChars) {
                currentLine.append(" ").append(word)
            } else {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
        return lines
    }

    // Approximate text wrapping based on available width and font size.
    private fun wrapByWidth(text: String, maxWidthPx: Int, fontSizePx: Int): List<String> {
        if (text.isBlank() || maxWidthPx <= 0) return listOf(text)
        // Approximate average character width factor for sans-serif fonts
        val avgCharWidth = (fontSizePx * 0.6)
        val maxChars = if (avgCharWidth <= 0) text.length else kotlin.math.max(1, kotlin.math.floor(maxWidthPx / avgCharWidth).toInt())
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (word in words) {
            if (current.isEmpty()) {
                if (word.length > maxChars) {
                    // Hard split long single word
                    var start = 0
                    while (start < word.length) {
                        val end = kotlin.math.min(start + maxChars, word.length)
                        val chunk = word.substring(start, end)
                        if (end == word.length) {
                            current.append(chunk)
                        } else {
                            lines.add(chunk)
                        }
                        start = end
                    }
                } else {
                    current.append(word)
                }
            } else {
                val candidate = current.length + 1 + word.length
                if (candidate <= maxChars) {
                    current.append(' ').append(word)
                } else {
                    lines.add(current.toString())
                    current = StringBuilder(word)
                }
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())
        return lines
    }

    // Header wrapping helpers
    private fun featureLines(featureTitle: String, theme: GherkinTheme): List<String> {
        val rectWidth = theme.layout.width - (theme.layout.padding * 2)
        val textBoxWidth = rectWidth - 60 // 50 for icon space + ~10 right margin
        return wrapByWidth("Feature: $featureTitle", textBoxWidth, theme.typography.featureSize)
    }
    private fun featureHeaderHeight(featureTitle: String, theme: GherkinTheme): Int {
        val lines = featureLines(featureTitle, theme)
        val lineHeight = theme.typography.featureSize + 6
        return kotlin.math.max(50, lines.size * lineHeight + 16)
    }
    private fun scenarioHeaderLines(title: String, theme: GherkinTheme): List<String> {
        val rectWidth = theme.layout.width - (theme.layout.padding * 2) - 20
        val textBoxWidth = rectWidth - 65 // 45 from left inside rect + ~20 right margin
        return wrapByWidth("Scenario: $title", textBoxWidth, theme.typography.scenarioSize)
    }
    private fun scenarioHeaderHeight(title: String, theme: GherkinTheme): Int {
        val lines = scenarioHeaderLines(title, theme)
        val lineHeight = theme.typography.scenarioSize + 6
        return kotlin.math.max(40, lines.size * lineHeight + 12)
    }

    private fun stepLines(step: GherkinStep, theme: GherkinTheme): List<String> {
        val keywordWidth = step.type.name.length * 8 + 20 // same heuristic as used in rendering
        val textBoxWidth = (theme.layout.width - 160) - (keywordWidth + 10)
        return wrapByWidth(step.text, textBoxWidth, theme.typography.stepSize)
    }

    private fun calculateStepHeight(step: GherkinStep, theme: GherkinTheme): Int {
        val lines = stepLines(step, theme)
        val lineHeight = 20 // px per line, aligns with existing rect height of 20 for single line
        return kotlin.math.max(20, lines.size * lineHeight)
    }

    private fun calculateExamplesHeight(examples: GherkinExamples?, theme: GherkinTheme): Int {
        if (examples == null) return 0
        val rowHeight = 25
        val headerHeight = 30
        return headerHeight + (examples.rows.size * rowHeight) + 20 // + padding
    }
    fun makeGherkin(spec: GherkinSpec): String {
        // Initialize the design system theme based on the incoming spec
        theme = ThemeFactory.getTheme(spec.theme)

        val totalHeight = calculateTotalHeight(spec, spec.theme)
        val width = spec.theme.layout.width

        return buildString {
            append(createSvgHeader(width, totalHeight))
            append(createDefinitions())
            append(createBackground(width, totalHeight))

            // Feature Header
            val (headerSvg, headerHeight) = createFeatureHeader(spec.feature, spec.theme)
            append(headerSvg)

            var yOffset = 40 + headerHeight + 30
            spec.scenarios.forEachIndexed { index, scenario ->
                append(createScenario(scenario, spec.theme, yOffset))
                yOffset += calculateScenarioHeight(scenario, spec.theme) + 40
            }

            append("</svg>")
        }
    }

    private fun createSvgHeader(width: Int, height: Int): String {
        return """<svg width="$width" height="$height" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $height">"""
    }

    private fun createDefinitions(): String {
        // Use accentColor from theme for gradients
        val accentStart = theme.accentColor
        // Generate a variation for the gradient end (or use a secondary from theme)
        val accentEnd = theme.secondaryText

        // Shadow color should match the surface impact of the theme
        val shadowColor = if (useDark) "#000000" else "#cbd5e1"
        val shadowOpacity = if (useDark) "0.5" else "0.2"


        val stopColor = if (useDark) "#000000" else "#cbd5e1"

        return """
                <defs>
                    <pattern id="dotPattern" x="0" y="0" width="20" height="20" patternUnits="userSpaceOnUse">
                        <circle cx="2" cy="2" r="1" fill="${theme.secondaryText}" opacity="0.15"/>
                    </pattern>
                    <linearGradient id="accentGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" style="stop-color:$accentStart;stop-opacity:1" />
                        <stop offset="100%" style="stop-color:$accentEnd;stop-opacity:1" />
                    </linearGradient>
                    <filter id="softGlow" x="-20%" y="-20%" width="140%" height="140%">
                        <feGaussianBlur stdDeviation="3" result="blur" />
                        <feComposite in="SourceGraphic" in2="blur" operator="over" />
                    </filter>
                    <filter id="cardShadow" x="-10%" y="-10%" width="120%" height="140%">
                        <feDropShadow dx="0" dy="8" stdDeviation="12" flood-color="$shadowColor" flood-opacity="$shadowOpacity"/>
                    </filter>
                </defs>
            """.trimIndent()
    }



    private fun createBackground(width: Int, height: Int): String {
        return """
                <rect width="$width" height="$height" fill="${theme.canvas}" rx="${theme.cornerRadius}"/>
                <rect width="$width" height="$height" fill="url(#dotPattern)" rx="${theme.cornerRadius}"/>
            """.trimIndent()
    }

    private fun createFeatureHeader(featureTitle: String, gherkinTheme: GherkinTheme): Pair<String, Int> {
        val width = gherkinTheme.layout.width - 80
        val lines = wrapText(featureTitle, width - 60, 24)
        val lineHeight = 30
        val bgHeight = (lines.size * lineHeight) + 60

        // DESIGN SYSTEM TRANSITION: Use theme instead of local useDark checks
        val bgColor = theme.glassEffect // or theme.canvas for solid
        val strokeColor = theme.accentColor
        val textColor = theme.primaryText
        val labelColor = theme.accentColor

        val sb = StringBuilder()
        sb.append("""
                <g transform="translate(40, 40)">
                    <rect width="$width" height="$bgHeight" fill="$bgColor" opacity="0.9" rx="12" stroke="$strokeColor" stroke-width="1"/>
                    <rect width="6" height="${bgHeight - 40}" x="0" y="20" fill="url(#accentGradient)" rx="3" filter="url(#softGlow)"/>
                    <text x="30" y="35" font-family="Monaco, monospace" font-size="11" font-weight="bold" fill="$labelColor" letter-spacing="2">FEATURE</text>
            """)

        lines.forEachIndexed { idx, line ->
            sb.append("""<text x="30" y="${65 + (idx * lineHeight)}" font-family="sans-serif" font-size="24" font-weight="800" fill="$textColor">$line</text>""")
        }
        sb.append("</g>")
        return Pair(sb.toString(), bgHeight)
    }

    private fun createScenario(scenario: GherkinScenario, gherkinTheme: GherkinTheme, yOffset: Int): String {
        val width = gherkinTheme.layout.width - 80
        val textColor = theme.primaryText
        val highlightColor = theme.primaryText
        val metaColor = theme.accentColor
        val bgColor = theme.glassEffect
        val strokeColor = theme.accentColor

        // 1. Wrap Scenario Title
        val titleLines = wrapText("SCENARIO: ${scenario.title.uppercase()}", width - 150, 10)
        val titleHeight = titleLines.size * 15

        // 2. Pre-calculate steps with better padding
        val stepData = scenario.steps.map { step ->
            val lines = wrapText(step.text, width - 120, 14)
            val h = lines.size * 22 // Increased line height slightly
            Triple(step, lines, h)
        }

        // 3. Dynamic Height Calculation with extra padding (40px bottom buffer)
        val scenarioHeight = 40 + titleHeight + 20 + stepData.sumOf { it.third + 18 } + 30

        return buildString {
            append("""<g transform="translate(40, $yOffset)" filter="url(#cardShadow)">""")
            append("""<rect width="$width" height="$scenarioHeight" fill="$bgColor" rx="12" stroke="$strokeColor" stroke-width="0.5"/>""")

            // Render Wrapped Scenario Title
            titleLines.forEachIndexed { idx, line ->
                append("""<text x="30" y="${35 + (idx * 15)}" font-family="Monaco, monospace" font-size="10" font-weight="bold" fill="$metaColor" letter-spacing="1.5">$line</text>""")
            }

            // Status Badge (stays at the top right)
            val statusColors = getStatusTheme(scenario.status)
            append("""
                    <g transform="translate(${width - 100}, 25)">
                        <rect width="80" height="24" rx="12" fill="${statusColors.bg}" stroke="${statusColors.stroke}" stroke-width="1"/>
                        <text x="40" y="16" font-family="Monaco, monospace" font-size="10" font-weight="bold" fill="${statusColors.text}" text-anchor="middle">${scenario.status.name}</text>
                    </g>
                """)

            // Start steps after the wrapped title
            var currentY = 40 + titleHeight + 20
            stepData.forEachIndexed { idx, (step, lines, h) ->
                val stepColor = getStepColorVibrant(step.type)
                append("""<g transform="translate(35, $currentY)">""")
                append("""<circle cx="0" cy="0" r="4" fill="$stepColor" filter="url(#softGlow)"/>""")

                lines.forEachIndexed { lIdx, line ->
                    val escapedLine = line.escapeXml()
                    val content = if (lIdx == 0) """<tspan fill="$highlightColor" font-weight="bold">${step.type.name.lowercase().capitalize()}</tspan> $escapedLine""" else escapedLine
                    append("""<text x="25" y="${5 + (lIdx * 22)}" font-family="sans-serif" font-size="14" fill="$textColor">$content</text>""")
                }

                if (idx < stepData.size - 1) {
                    val connectionHeight = h + 18
                    append("""<path d="M 0 10 L 0 $connectionHeight" stroke="$strokeColor" stroke-width="1" stroke-dasharray="2 2"/>""")
                }
                append("</g>")
                currentY += h + 18
            }
            // Render Examples if present
            scenario.examples?.let {
                append(createExamplesTable(it, width, currentY + 10))
            }
            append("</g>")
        }

    }



    private data class StatusColors(val bg: String, val stroke: String, val text: String)

    private fun getStatusTheme(status: GherkinScenarioStatus): StatusColors {
        return when (status) {
            GherkinScenarioStatus.PASSING -> if (useDark) StatusColors("#064e3b", "#059669", "#34d399") else StatusColors("#dcfce7", "#16a34a", "#15803d")
            GherkinScenarioStatus.FAILING -> if (useDark) StatusColors("#450a0a", "#dc2626", "#f87171") else StatusColors("#fee2e2", "#dc2626", "#b91c1c")
            else -> if (useDark) StatusColors("#1e293b", "#475569", "#94a3b8") else StatusColors("#f1f5f9", "#94a3b8", "#475569")
        }
    }

    private fun createStep(step: GherkinStep, theme: GherkinTheme, yOffset: Int): String {
        val stepColor = getStepColor(step.type, theme)
        val statusColor = getStatusColor(step.status, theme)
        val stepIcon = getStepIcon(step.type)
        val statusIcon = getStatusIcon(step.status)

        val lines = stepLines(step, theme)
        val lineHeight = 20
        val bgHeight = kotlin.math.max(20, lines.size * lineHeight)
        val firstLineBaseline = yOffset + 2
        // Center relative to text block rather than background rect to avoid downward shift
        val centerY = firstLineBaseline + (bgHeight - lineHeight) / 2
        val keywordWidth = step.type.name.length * 8 + 20
        val textX = theme.layout.padding + 90 + keywordWidth
        
        return buildString {
            // Step circle centered vertically on block
            append("""
                <circle cx="${theme.layout.padding + 60}" cy="$centerY" r="8" fill="$stepColor"/>
                <text x="${theme.layout.padding + 60}" y="$centerY" 
                      font-family="${theme.typography.fontFamily}" font-size="10" 
                      fill="#ffffff" text-anchor="middle" dominant-baseline="middle">$stepIcon</text>
            """.trimIndent())
            
            // Step background sized to wrapped content
            append("""
                <rect x="${theme.layout.padding + 80}" y="${yOffset - 10}" width="${theme.layout.width - 160}" height="$bgHeight" 
                      fill="$stepColor" opacity="0.1" rx="4"/>
            """.trimIndent())
            
            // Step keyword (kept at first line baseline)
            append("""
                <text x="${theme.layout.padding + 90}" y="$firstLineBaseline" 
                      font-family="Monaco, monospace" font-size="${theme.typography.stepSize}" 
                      font-weight="bold" fill="$stepColor">${step.type.name.lowercase().capitalize()}</text>
            """.trimIndent())
            
            // Step text with wrapping
            lines.forEachIndexed { idx, line ->
                val y = firstLineBaseline + (idx * lineHeight)
                append("""
                <text x="$textX" y="$y" 
                      font-family="${theme.typography.fontFamily}" font-size="${theme.typography.stepSize}" 
                      fill="#495057">$line</text>
                """.trimIndent())
            }
            
            // Status indicator centered vertically on block
            append("""
                <circle cx="${theme.layout.width - 60}" cy="$centerY" r="6" fill="$statusColor"/>
                <text x="${theme.layout.width - 60}" y="$centerY" 
                      font-family="${theme.typography.fontFamily}" font-size="8" 
                      fill="#ffffff" text-anchor="middle" dominant-baseline="middle">$statusIcon</text>
            """.trimIndent())
        }
    }

    private fun getStepColor(stepType: GherkinStepType, theme: GherkinTheme): String {
        return when (stepType) {
            GherkinStepType.GIVEN -> theme.colors.given
            GherkinStepType.WHEN -> theme.colors.`when`
            GherkinStepType.THEN -> theme.colors.then
            GherkinStepType.AND -> theme.colors.and
            GherkinStepType.BUT -> theme.colors.but
        }
    }

    private fun getStepIcon(stepType: GherkinStepType): String {
        return when (stepType) {
            GherkinStepType.GIVEN -> "✓"
            GherkinStepType.WHEN -> "⚡"
            GherkinStepType.THEN -> "🎉"
            GherkinStepType.AND -> "+"
            GherkinStepType.BUT -> "!"
        }
    }

    private fun getStatusColor(status: GherkinStepStatus, theme: GherkinTheme): String {
        return when (status) {
            GherkinStepStatus.PASSING -> theme.colors.passing
            GherkinStepStatus.FAILING -> theme.colors.failing
            GherkinStepStatus.PENDING -> theme.colors.pending
            GherkinStepStatus.SKIPPED -> theme.colors.skipped
        }
    }

    private fun getStatusColor(status: GherkinScenarioStatus, theme: GherkinTheme): String {
        return when (status) {
            GherkinScenarioStatus.PASSING -> theme.colors.passing
            GherkinScenarioStatus.FAILING -> theme.colors.failing
            GherkinScenarioStatus.PENDING -> theme.colors.pending
            GherkinScenarioStatus.SKIPPED -> theme.colors.skipped
        }
    }

    private fun getStatusIcon(status: GherkinStepStatus): String {
        return when (status) {
            GherkinStepStatus.PASSING -> "✓"
            GherkinStepStatus.FAILING -> "✗"
            GherkinStepStatus.PENDING -> "⏸"
            GherkinStepStatus.SKIPPED -> "⏭"
        }
    }

    private fun getStatusIcon(status: GherkinScenarioStatus): String {
        return when (status) {
            GherkinScenarioStatus.PASSING -> "✓"
            GherkinScenarioStatus.FAILING -> "✗"
            GherkinScenarioStatus.PENDING -> "⏸"
            GherkinScenarioStatus.SKIPPED -> "⏭"
        }
    }

    private fun getStepColorVibrant(type: GherkinStepType): String {
        return when (type) {
            GherkinStepType.GIVEN -> "#22c55e"
            GherkinStepType.WHEN -> "#3b82f6"
            GherkinStepType.THEN -> "#8b5cf6"
            else -> "#64748b"
        }
    }

    private fun calculateScenarioHeight(scenario: GherkinScenario, theme: GherkinTheme): Int {
        val width = theme.layout.width - 80
        // 1. Title height
        val titleLines = wrapText("SCENARIO: ${scenario.title.uppercase()}", width - 150, 10)
        val titleHeight = titleLines.size * 15

        // 2. Steps height
        val stepsHeight = scenario.steps.sumOf { step ->
            val lines = wrapText(step.text, width - 120, 14)
            (lines.size * 22) + 18 // Line height + gap
        }

        // 3. Examples height
        val examplesHeight = calculateExamplesHeight(scenario.examples, theme)

        // 4. Header (40) + Title Space (20) + Steps + Examples + Bottom Padding (30)
        return 40 + titleHeight + 20 + stepsHeight + examplesHeight + 30
    }

    private fun calculateTotalHeight(spec: GherkinSpec, theme: GherkinTheme): Int {
        val width = theme.layout.width - 80
        // Feature Header height
        val featureLines = wrapText(spec.feature, width - 60, 24)
        val featureHeaderHeight = (featureLines.size * 30) + 60

        // Start with top padding + feature header + gap
        var total = 40 + featureHeaderHeight + 30

        // Add each scenario height + gap between scenarios
        spec.scenarios.forEach { scenario ->
            total += calculateScenarioHeight(scenario, theme) + 40
        }

        // Extra bottom buffer
        return total + 20
    }

    private fun createExamplesTable(examples: GherkinExamples, width: Int, yOffset: Int): String {
        val cellWidth = (width - 60) / examples.headers.size
        val rowHeight = 25
        val textColor = theme.primaryText
        val headerBg = theme.canvas
        val borderColor = theme.accentColor

        return buildString {
            append("""<g transform="translate(45, $yOffset)">""")
            append("""<text x="0" y="-10" font-family="Monaco, monospace" font-size="10" font-weight="bold" fill="#3b82f6">EXAMPLES:</text>""")

            // Headers
            examples.headers.forEachIndexed { i, header ->
                append("""
                            <rect x="${i * cellWidth}" y="0" width="$cellWidth" height="$rowHeight" fill="$headerBg" stroke="$borderColor" stroke-width="0.5"/>
                            <text x="${i * cellWidth + cellWidth / 2}" y="17" font-family="sans-serif" font-size="11" font-weight="bold" fill="$textColor" text-anchor="middle">${header.escapeXml()}</text>
                        """.trimIndent())
            }

            // Rows
            examples.rows.forEachIndexed { rowIndex, row ->
                val y = (rowIndex + 1) * rowHeight
                row.forEachIndexed { colIndex, cell ->
                    append("""
                                <rect x="${colIndex * cellWidth}" y="$y" width="$cellWidth" height="$rowHeight" fill="none" stroke="$borderColor" stroke-width="0.5"/>
                                <text x="${colIndex * cellWidth + cellWidth / 2}" y="${y + 17}" font-family="sans-serif" font-size="11" fill="$textColor" text-anchor="middle">${cell.escapeXml()}</text>
                            """.trimIndent())
                }
            }
            append("</g>")
        }
    }
}
