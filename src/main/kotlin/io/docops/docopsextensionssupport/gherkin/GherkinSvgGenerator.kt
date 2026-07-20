package io.docops.docopsextensionssupport.gherkin

import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import org.springframework.stereotype.Service
import java.util.Locale
import java.util.Locale.getDefault

class GherkinMaker(val useDark: Boolean, val isPdf: Boolean = false) {
    // Resolve the theme once for the entire generator
    private lateinit var theme: DocOpsTheme
    private var isModern = false

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
    fun makeGherkin(spec: GherkinSpec, scale: String = "1.0"): String {
        // Initialize the design system theme based on the incoming spec
        theme = ThemeFactory.getTheme(spec.theme)
        isModern = !isPdf && !theme.name.contains("Classic") && !theme.name.contains("Pro")

        val totalHeight = calculateTotalHeight(spec, spec.theme)
        val width = spec.theme.layout.width
        
        val fScale = scale.toDoubleOrNull() ?: 1.0
        val scaledWidth = (width * fScale).toInt()
        val scaledHeight = (totalHeight * fScale).toInt()

        return buildString {
            append(createSvgHeader(scaledWidth, scaledHeight, width, totalHeight))
            append(createDefinitions())
            if (isModern) {
                append(makeModernBackground(width, totalHeight))
            } else {
                append(createBackground(width, totalHeight))
            }

            // Feature Header
            val (headerSvg, headerHeight) = createFeatureHeader(spec.feature, spec.theme)
            append(headerSvg)

            var yOffset = 40 + headerHeight + 30
            spec.scenarios.forEachIndexed { index, scenario ->
                append(createScenario(scenario, spec.theme, yOffset, index + 1))
                yOffset += calculateScenarioHeight(scenario, spec.theme) + 40
            }

            append("</svg>")
        }
    }

    private fun createSvgHeader(scaledWidth: Int, scaledHeight: Int, width: Int, height: Int): String {
        return """<svg width="$scaledWidth" height="$scaledHeight" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $height" preserveAspectRatio="xMidYMid meet">"""
    }

    private fun createDefinitions(): String {
        // Use accentColor from theme for gradients
        val accentStart = theme.accentColor
        val accentEnd = theme.secondaryText

        // Authored shadow color derived from theme canvas
        val shadowColor = if (useDark) "#000000" else SVGColor(theme.canvas).darker() ?: "#cbd5e1"
        val shadowOpacity = if (useDark) "0.5" else "0.3"

        return buildString {
            append("<defs>")
            if (isModern) {
                append("""
                    <linearGradient id="bgGlow" x1="0" y1="0" x2="1" y2="1">
                        <stop offset="0%" stop-color="${if (useDark) "#1f2937" else "#dbe8f8"}" stop-opacity="0.65"/>
                        <stop offset="100%" stop-color="${theme.canvas}" stop-opacity="0"/>
                    </linearGradient>
                    <pattern id="fineGrid" x="0" y="0" width="30" height="30" patternUnits="userSpaceOnUse">
                        <path d="M 30 0 L 0 0 0 30" fill="none" stroke="${theme.accentColor}" stroke-width="0.5" opacity="0.2"/>
                    </pattern>
                    <radialGradient id="vignette" cx="50%" cy="50%" r="70%" fx="50%" fy="50%">
                        <stop offset="0%" stop-color="#000000" stop-opacity="0"/>
                        <stop offset="100%" stop-color="#000000" stop-opacity="0.1"/>
                    </radialGradient>
                    <filter id="glassBlur" x="-10%" y="-10%" width="120%" height="120%">
                        <feGaussianBlur in="SourceGraphic" stdDeviation="1.5" />
                    </filter>
                """.trimIndent())
            }
            append("""
                    <linearGradient id="accentGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" style="stop-color:$accentStart;stop-opacity:1" />
                        <stop offset="100%" style="stop-color:$accentEnd;stop-opacity:1" />
                    </linearGradient>
                    <filter id="cardShadow" x="-10%" y="-10%" width="120%" height="140%">
                        <feDropShadow dx="0" dy="8" stdDeviation="12" flood-color="$shadowColor" flood-opacity="$shadowOpacity"/>
                    </filter>
                """.trimIndent())
            
            append("<style>")
            if (isModern) {
                append("""
                    @import url('https://fonts.googleapis.com/css2?family=Lexend:wght@400;600;800&amp;family=Archivo:wght@400;700;900&amp;display=swap');
                    :root {
                        --bg: ${theme.canvas};
                        --surface: ${if (useDark) "#161b22" else "#ffffff"};
                        --text: ${theme.primaryText};
                        --text-soft: ${theme.secondaryText};
                        --accent: ${theme.accentColor};
                        --card-radius: 16px;
                        --step-gap: 20px;
                        --accent-width: 6px;
                    }
                    text { font-family: ${theme.fontFamily}; }
                    .feature-title { font-family: 'Archivo', sans-serif; font-weight: 900; fill: var(--text) !important; }
                    .feature-label { font-family: 'Monaco', monospace; font-size: 11px; font-weight: bold; fill: var(--accent) !important; letter-spacing: 2px; }
                    .scenario-title { font-family: 'Monaco', monospace; font-size: 14px; font-weight: bold; fill: var(--accent) !important; letter-spacing: 1px; }
                    .step-text { font-family: 'Lexend', sans-serif; font-size: 14px; fill: var(--text) !important; }
                    .step-keyword { font-weight: bold; fill: var(--text) !important; }
                    
                    @keyframes slideDown { from { transform: translateY(-20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
                    @keyframes riseUp { from { transform: translateY(30px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
                    
                    .feature-header { animation: slideDown 0.6s ease-out both; opacity: 0; }
                    .scenario-wrap { transition: transform 0.3s ease, filter 0.3s ease; opacity: 0; animation: riseUp 0.6s ease-out both; }
                    .scenario-wrap:hover { transform: scale(1.01); filter: brightness(1.02); }
                    
                    ${(1..30).joinToString("\n                    ") { ".anim-delay-$it { animation-delay: ${it * 0.1}s; }" }}
                """.trimIndent())
            }
            if (isPdf) {
                append("""
                    .feature-header, .scenario-wrap { animation: none !important; opacity: 1 !important; }
                    path { stroke-dashoffset: 0 !important; }
                """.trimIndent())
            }
            append("</style>")
            append("</defs>")
        }
    }

    private fun makeModernBackground(width: Int, height: Int): String {
        return """
                <rect width="$width" height="$height" fill="var(--bg)" rx="${theme.cornerRadius}"/>
                <circle cx="140" cy="80" r="220" fill="url(#bgGlow)"/>
                <rect width="$width" height="$height" fill="url(#fineGrid)" opacity="0.1"/>
                <rect width="$width" height="$height" fill="url(#vignette)" opacity="0.1"/>
            """.trimIndent()
    }

    private fun createBackground(width: Int, height: Int): String {
        return """
                <rect width="$width" height="$height" fill="${theme.canvas}" rx="${theme.cornerRadius}"/>
            """.trimIndent()
    }

    private fun createFeatureHeader(featureTitle: String, gherkinTheme: GherkinTheme): Pair<String, Int> {
        val width = gherkinTheme.layout.width - 80
        val lines = wrapText(featureTitle, width - 60, 24)
        val lineHeight = 30
        val bgHeight = (lines.size * lineHeight) + 60

        // DESIGN SYSTEM TRANSITION: Industrial/Corporate Aesthetic
        val bgColor = if (isModern) "var(--surface)" else theme.canvas
        val strokeColor = if (isModern) "var(--accent)" else theme.accentColor
        val textColor = if (isModern) "var(--text)" else theme.primaryText
        val labelColor = if (isModern) "var(--accent)" else theme.accentColor

        val sb = StringBuilder()
        sb.append("""
                <g transform="translate(40, 40)" ${if (isModern) "class=\"feature-header\"" else ""}>
                    <rect width="$width" height="$bgHeight" fill="$bgColor" rx="12" stroke="$strokeColor" stroke-width="2"/>
                    <rect width="${if (isModern) "var(--accent-width)" else "6"}" height="${bgHeight - 40}" x="0" y="20" fill="url(#accentGradient)" rx="3"/>
                    <text x="30" y="35" ${if (isModern) "class=\"feature-label\"" else "font-family=\"Monaco, monospace\" font-size=\"11\" font-weight=\"bold\" letter-spacing=\"2\""} fill="$labelColor" style="fill: $labelColor !important;">FEATURE</text>
            """)

        lines.forEachIndexed { idx, line ->
            sb.append("""<text x="30" y="${65 + (idx * lineHeight)}" ${if (isModern) "class=\"feature-title\"" else "font-family=\"sans-serif\" font-weight=\"800\""} font-size="24" fill="$textColor" style="fill: $textColor !important;">$line</text>""")
        }
        sb.append("</g>")
        return Pair(sb.toString(), bgHeight)
    }

    private fun createScenario(scenario: GherkinScenario, gherkinTheme: GherkinTheme, yOffset: Int, index: Int): String {
        val width = gherkinTheme.layout.width - 80
        val textColor = if (isModern) "var(--text)" else theme.primaryText
        val highlightColor = if (isModern) "var(--text)" else theme.primaryText
        val metaColor = if (isModern) "var(--accent)" else theme.accentColor
        val bgColor = if (isModern) "var(--surface)" else theme.canvas
        val strokeColor = if (isModern) "var(--accent)" else theme.accentColor

        // 1. Wrap Scenario Title - Increased font size to 14px
        val titleFontSize = 14
        val titleLines = wrapText("SCENARIO: ${scenario.title.uppercase()}", width - 150, titleFontSize)
        val titleLineHeight = titleFontSize + 4
        val titleHeight = titleLines.size * titleLineHeight

        // 2. Pre-calculate steps with better padding
        val stepData = scenario.steps.map { step ->
            val lines = wrapText(step.text, width - 120, 14)
            val h = lines.size * 22
            Triple(step, lines, h)
        }

        // 3. Dynamic Height Calculation
        val scenarioHeight = calculateScenarioHeight(scenario, gherkinTheme)

        return buildString {
            append("""<g transform="translate(40, $yOffset)" ${if (isModern) "class=\"scenario-wrap anim-delay-$index\"" else "filter=\"url(#cardShadow)\""}>""")
            append("""<rect width="$width" height="$scenarioHeight" fill="$bgColor" rx="${if (isModern) "16" else "12"}" stroke="$strokeColor" stroke-width="1.5"/>""")

            // Render Wrapped Scenario Title
            titleLines.forEachIndexed { idx, line ->
                append("""<text x="30" y="${35 + (idx * titleLineHeight)}" ${if (isModern) "class=\"scenario-title\"" else "font-family=\"Monaco, monospace\" font-size=\"$titleFontSize\" font-weight=\"bold\" letter-spacing=\"1\""} fill="$metaColor" style="fill: $metaColor !important;">$line</text>""")
            }

            // Status Badge
            val statusColors = getStatusTheme(scenario.status)
            append("""
                    <g transform="translate(${width - 100}, 25)">
                        <rect width="80" height="24" rx="12" fill="${statusColors.bg}" stroke="${statusColors.stroke}" stroke-width="1"/>
                        <text x="40" y="16" font-family="Monaco, monospace" font-size="10" font-weight="bold" fill="${statusColors.text}" style="fill: ${statusColors.text} !important;" text-anchor="middle">${scenario.status.name}</text>
                    </g>
                """)

            // Start steps after the wrapped title
            var currentY = 40 + titleHeight + 20
            stepData.forEachIndexed { idx, (step, lines, h) ->
                val stepColor = getStepColorVibrant(step.type)
                val stepIcon = getStepIcon(step.type)
                append("""<g transform="translate(35, $currentY)">""")
                
                // Enhanced Accessibility: Icon in circle
                append("""<circle cx="0" cy="0" r="10" fill="$stepColor"/>""")
                append("""<text x="0" y="0" font-family="sans-serif" font-size="10" font-weight="bold" fill="#FFFFFF" style="fill: #FFFFFF !important;" text-anchor="middle" dominant-baseline="central">$stepIcon</text>""")

                lines.forEachIndexed { lIdx, line ->
                    val escapedLine = line.escapeXml()
                    val keyword = step.type.name.lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString() }
                    
                    if (isModern) {
                        val content = if (lIdx == 0) """<tspan class="step-keyword">$keyword</tspan> $escapedLine""" else escapedLine
                        append("""<text x="25" y="${5 + (lIdx * 22)}" class="step-text" fill="$textColor" style="fill: $textColor !important;">$content</text>""")
                    } else {
                        val content = if (lIdx == 0) """<tspan fill="$highlightColor" font-weight="bold">$keyword</tspan> $escapedLine""" else escapedLine
                        append("""<text x="25" y="${5 + (lIdx * 22)}" font-family="sans-serif" font-size="14" fill="$textColor" style="fill: $textColor !important;">$content</text>""")
                    }
                }

                if (idx < stepData.size - 1) {
                    val connectionHeight = h + 18
                    append("""<path d="M 0 10 L 0 $connectionHeight" stroke="$strokeColor" stroke-width="1.5" stroke-dasharray="3 3"/>""")
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
                      font-weight="bold" fill="$stepColor">${
                step.type.name.lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString() }
            }</text>
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
            GherkinStepType.GIVEN -> theme.chartPalette.getOrNull(0)?.color ?: "#22c55e"
            GherkinStepType.WHEN -> theme.chartPalette.getOrNull(2)?.color ?: "#3b82f6"
            GherkinStepType.THEN -> theme.chartPalette.getOrNull(1)?.color ?: "#8b5cf6"
            else -> theme.secondaryText
        }
    }

    private fun calculateScenarioHeight(scenario: GherkinScenario, theme: GherkinTheme): Int {
        val width = theme.layout.width - 80
        // 1. Title height - Increased font size to 14px
        val titleFontSize = 14
        val titleLines = wrapText("SCENARIO: ${scenario.title.uppercase()}", width - 150, titleFontSize)
        val titleLineHeight = titleFontSize + 4
        val titleHeight = titleLines.size * titleLineHeight

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
        val headerBg = theme.surfaceImpact
        val borderColor = theme.accentColor

        return buildString {
            append("""<g transform="translate(45, $yOffset)">""")
            append("""<text x="0" y="-10" font-family="Monaco, monospace" font-size="10" font-weight="bold" fill="${theme.accentColor}">EXAMPLES:</text>""")

            // Headers
            examples.headers.forEachIndexed { i, header ->
                append("""
                            <rect x="${i * cellWidth}" y="0" width="$cellWidth" height="$rowHeight" fill="$headerBg" stroke="$borderColor" stroke-width="1"/>
                            <text x="${i * cellWidth + cellWidth / 2}" y="17" font-family="sans-serif" font-size="11" font-weight="bold" fill="$textColor" text-anchor="middle">${header.escapeXml()}</text>
                        """.trimIndent())
            }

            // Rows
            examples.rows.forEachIndexed { rowIndex, row ->
                val y = (rowIndex + 1) * rowHeight
                row.forEachIndexed { colIndex, cell ->
                    append("""
                                <rect x="${colIndex * cellWidth}" y="$y" width="$cellWidth" height="$rowHeight" fill="${theme.canvas}" stroke="$borderColor" stroke-width="0.5"/>
                                <text x="${colIndex * cellWidth + cellWidth / 2}" y="${y + 17}" font-family="sans-serif" font-size="11" fill="$textColor" text-anchor="middle">${cell.escapeXml()}</text>
                            """.trimIndent())
                }
            }
            append("</g>")
        }
    }
}
