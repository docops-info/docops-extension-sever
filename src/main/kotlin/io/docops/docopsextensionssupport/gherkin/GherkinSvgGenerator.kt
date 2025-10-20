package io.docops.docopsextensionssupport.gherkin

import kotlinx.serialization.Serializable
import org.springframework.stereotype.Service

@Service
class GherkinMaker {

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

    fun makeGherkin(spec: GherkinSpec, useDark: Boolean = false): String {
        val theme = if (useDark) spec.theme.copy(
            colors = spec.theme.colors.copy(
                scenario = "#2d3748",
                feature = "#63b3ed"
            )
        ) else spec.theme

        val totalHeight = calculateTotalHeight(spec, theme)
        
        return buildString {
            append(createSvgHeader(theme.layout.width, totalHeight))
            append(createDefinitions(theme))
            append(createBackground(theme.layout.width, totalHeight, useDark))
            val featureHeader = createFeatureHeader(spec.feature, theme)
            append(featureHeader.first)
            
            var yOffset = 20 + featureHeader.second + 20
            spec.scenarios.forEachIndexed { index, scenario ->
                append(createScenario(scenario, theme, yOffset, index))
                yOffset += calculateScenarioHeight(scenario, theme) + theme.layout.scenarioSpacing
            }
            
            append("</svg>")
        }
    }

    private fun createSvgHeader(width: Int, height: Int): String {
        return """
            <svg width="$width" height="$height" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $height">
        """.trimIndent()
    }

    private fun createDefinitions(theme: GherkinTheme): String {
        return """
            <defs>
                <linearGradient id="featureGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                    <stop offset="0%" style="stop-color:${theme.colors.feature};stop-opacity:1" />
                    <stop offset="100%" style="stop-color:#7209b7;stop-opacity:1" />
                </linearGradient>
                
                <linearGradient id="scenarioGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                    <stop offset="0%" style="stop-color:${theme.colors.scenario};stop-opacity:1" />
                    <stop offset="100%" style="stop-color:#e9ecef;stop-opacity:1" />
                </linearGradient>
                
                <filter id="dropShadow" x="-20%" y="-20%" width="140%" height="140%">
                    <feDropShadow dx="0" dy="2" stdDeviation="3" flood-color="#000000" flood-opacity="0.1"/>
                </filter>
                
                <filter id="glow" x="-50%" y="-50%" width="200%" height="200%">
                    <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
                    <feMerge> 
                        <feMergeNode in="coloredBlur"/>
                        <feMergeNode in="SourceGraphic"/> 
                    </feMerge>
                </filter>
            </defs>
        """.trimIndent()
    }

    private fun createBackground(width: Int, height: Int, useDark: Boolean): String {
        val bgColor = if (useDark) "#1a202c" else "#ffffff"
        return """<rect width="$width" height="$height" fill="$bgColor" rx="8"/>"""
    }

    private fun createFeatureHeader(featureTitle: String, theme: GherkinTheme): Pair<String, Int> {
        val width = theme.layout.width - (theme.layout.padding * 2)
        val lines = featureLines(featureTitle, theme)
        val lineHeight = theme.typography.featureSize + 6
        val bgHeight = kotlin.math.max(50, lines.size * lineHeight + 16)
        val centerY = 20 + (bgHeight / 2)
        val firstBaseline = centerY - ((lines.size - 1) * lineHeight) / 2
        val sb = StringBuilder()
        sb.append(
            """
            <rect x="${theme.layout.padding}" y="20" width="$width" height="$bgHeight" 
                  fill="url(#featureGradient)" rx="8" filter="url(#dropShadow)"/>
            
            <circle cx="${theme.layout.padding + 25}" cy="$centerY" r="12" fill="#ffffff" opacity="0.3"/>
            <text x="${theme.layout.padding + 25}" y="$centerY" 
                  font-family="${theme.typography.fontFamily}" font-size="16" 
                  fill="#ffffff" text-anchor="middle" dominant-baseline="middle">🎯</text>
        """.trimIndent()
        )
        lines.forEachIndexed { idx, line ->
            val y = firstBaseline + (idx * lineHeight)
            sb.append(
                """
            <text x="${theme.layout.padding + 50}" y="$y" 
                  font-family="${theme.typography.fontFamily}" font-size="${theme.typography.featureSize}" 
                  font-weight="bold" fill="#ffffff">$line</text>
                """.trimIndent()
            )
        }
        return Pair(sb.toString(), bgHeight)
    }

    private fun createScenario(scenario: GherkinScenario, theme: GherkinTheme, yOffset: Int, index: Int): String {
        val scenarioHeight = calculateScenarioHeight(scenario, theme)
        val width = theme.layout.width - (theme.layout.padding * 2) - 20
        val headerHeight = scenarioHeaderHeight(scenario.title, theme)
        val headerCenterY = yOffset + headerHeight / 2
        val headerLines = scenarioHeaderLines(scenario.title, theme)
        val lineHeightHeader = theme.typography.scenarioSize + 6
        val headerFirstBaseline = headerCenterY - ((headerLines.size - 1) * lineHeightHeader) / 2
        
        return buildString {
            // Scenario background
            append("""
                <rect x="${theme.layout.padding + 20}" y="$yOffset" width="$width" height="$scenarioHeight" 
                      fill="url(#scenarioGradient)" rx="6" stroke="#dee2e6" stroke-width="1" filter="url(#dropShadow)"/>
            """.trimIndent())
            
            // Scenario header
            append("""
                <rect x="${theme.layout.padding + 20}" y="$yOffset" width="$width" height="$headerHeight" 
                      fill="#ffffff" rx="6"/>
                
                <circle cx="${theme.layout.padding + 45}" cy="$headerCenterY" r="10" fill="#6c757d" opacity="0.2"/>
                <text x="${theme.layout.padding + 45}" y="$headerCenterY" 
                      font-family="${theme.typography.fontFamily}" font-size="14" 
                      fill="#6c757d" text-anchor="middle" dominant-baseline="middle">📋</text>
            """.trimIndent())
            headerLines.forEachIndexed { idx, line ->
                val y = headerFirstBaseline + (idx * lineHeightHeader)
                append(
                    """
                <text x="${theme.layout.padding + 65}" y="$y" 
                      font-family="${theme.typography.fontFamily}" font-size="${theme.typography.scenarioSize}" 
                      font-weight="600" fill="#495057">$line</text>
                    """.trimIndent()
                )
            }
            
            // Connecting line
            val lineStartY = yOffset + headerHeight + 10
            val lineEndY = yOffset + scenarioHeight - 20
            append("""
                <line x1="${theme.layout.padding + 60}" y1="$lineStartY" x2="${theme.layout.padding + 60}" y2="$lineEndY" stroke="#dee2e6" stroke-width="2"/>
            """.trimIndent())
            
            // Steps
            var stepY = lineStartY + 15
            val interStepGap = 8
            scenario.steps.forEach { step ->
                append(createStep(step, theme, stepY))
                stepY += calculateStepHeight(step, theme) + interStepGap
            }
            
            // Scenario status indicator
            val statusColor = getStatusColor(scenario.status, theme)
            val statusIcon = getStatusIcon(scenario.status)
            append("""
                <circle cx="${theme.layout.width - 40}" cy="$headerCenterY" r="8" fill="$statusColor"/>
                <text x="${theme.layout.width - 40}" y="$headerCenterY" 
                      font-family="${theme.typography.fontFamily}" font-size="10" 
                      fill="#ffffff" text-anchor="middle" dominant-baseline="middle">$statusIcon</text>
            """.trimIndent())
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

    private fun calculateScenarioHeight(scenario: GherkinScenario, theme: GherkinTheme): Int {
        val headerHeight = scenarioHeaderHeight(scenario.title, theme)
        if (scenario.steps.isEmpty()) return headerHeight + 20
        val interStepGap = 8
        val stepsHeight = scenario.steps.sumOf { calculateStepHeight(it, theme) } + interStepGap * (scenario.steps.size - 1)
        return headerHeight + stepsHeight + 20
    }

    private fun calculateTotalHeight(spec: GherkinSpec, theme: GherkinTheme): Int {
        val featureHeight = featureHeaderHeight(spec.feature, theme)
        var height = 20 + featureHeight + 20
        spec.scenarios.forEach { scenario ->
            height += calculateScenarioHeight(scenario, theme) + theme.layout.scenarioSpacing
        }
        return height + theme.layout.padding
    }
}
