package io.docops.docopsextensionssupport.gherkin

import kotlinx.serialization.Serializable
import org.springframework.stereotype.Service

@Service
class GherkinMaker {

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
            append(createFeatureHeader(spec.feature, theme))
            
            var yOffset = 90
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

    private fun createFeatureHeader(featureTitle: String, theme: GherkinTheme): String {
        val width = theme.layout.width - (theme.layout.padding * 2)
        return """
            <rect x="${theme.layout.padding}" y="20" width="$width" height="50" 
                  fill="url(#featureGradient)" rx="8" filter="url(#dropShadow)"/>
            
            <circle cx="${theme.layout.padding + 25}" cy="45" r="12" fill="#ffffff" opacity="0.3"/>
            <text x="${theme.layout.padding + 25}" y="50" 
                  font-family="${theme.typography.fontFamily}" font-size="16" 
                  fill="#ffffff" text-anchor="middle">🎯</text>
            
            <text x="${theme.layout.padding + 50}" y="50" 
                  font-family="${theme.typography.fontFamily}" font-size="${theme.typography.featureSize}" 
                  font-weight="bold" fill="#ffffff">Feature: $featureTitle</text>
        """.trimIndent()
    }

    private fun createScenario(scenario: GherkinScenario, theme: GherkinTheme, yOffset: Int, index: Int): String {
        val scenarioHeight = calculateScenarioHeight(scenario, theme)
        val width = theme.layout.width - (theme.layout.padding * 2) - 20
        
        return buildString {
            // Scenario background
            append("""
                <rect x="${theme.layout.padding + 20}" y="$yOffset" width="$width" height="$scenarioHeight" 
                      fill="url(#scenarioGradient)" rx="6" stroke="#dee2e6" stroke-width="1" filter="url(#dropShadow)"/>
            """.trimIndent())
            
            // Scenario header
            append("""
                <rect x="${theme.layout.padding + 20}" y="$yOffset" width="$width" height="40" 
                      fill="#ffffff" rx="6"/>
                
                <circle cx="${theme.layout.padding + 45}" cy="${yOffset + 20}" r="10" fill="#6c757d" opacity="0.2"/>
                <text x="${theme.layout.padding + 45}" y="${yOffset + 24}" 
                      font-family="${theme.typography.fontFamily}" font-size="14" 
                      fill="#6c757d" text-anchor="middle">📋</text>
                
                <text x="${theme.layout.padding + 65}" y="${yOffset + 24}" 
                      font-family="${theme.typography.fontFamily}" font-size="${theme.typography.scenarioSize}" 
                      font-weight="600" fill="#495057">Scenario: ${scenario.title}</text>
            """.trimIndent())
            
            // Connecting line
            val lineStartY = yOffset + 50
            val lineEndY = yOffset + scenarioHeight - 20
            append("""
                <line x1="${theme.layout.padding + 60}" y1="$lineStartY" 
                      x2="${theme.layout.padding + 60}" y2="$lineEndY" 
                      stroke="#dee2e6" stroke-width="2"/>
            """.trimIndent())
            
            // Steps
            var stepY = lineStartY + 15
            scenario.steps.forEach { step ->
                append(createStep(step, theme, stepY))
                stepY += theme.layout.stepSpacing
            }
            
            // Scenario status indicator
            val statusColor = getStatusColor(scenario.status, theme)
            val statusIcon = getStatusIcon(scenario.status)
            append("""
                <circle cx="${theme.layout.width - 40}" cy="${yOffset + 20}" r="8" fill="$statusColor"/>
                <text x="${theme.layout.width - 40}" y="${yOffset + 24}" 
                      font-family="${theme.typography.fontFamily}" font-size="10" 
                      fill="#ffffff" text-anchor="middle">$statusIcon</text>
            """.trimIndent())
        }
    }

    private fun createStep(step: GherkinStep, theme: GherkinTheme, yOffset: Int): String {
        val stepColor = getStepColor(step.type, theme)
        val statusColor = getStatusColor(step.status, theme)
        val stepIcon = getStepIcon(step.type)
        val statusIcon = getStatusIcon(step.status)
        
        return buildString {
            // Step circle
            append("""
                <circle cx="${theme.layout.padding + 60}" cy="$yOffset" r="8" fill="$stepColor"/>
                <text x="${theme.layout.padding + 60}" y="${yOffset + 3}" 
                      font-family="${theme.typography.fontFamily}" font-size="10" 
                      fill="#ffffff" text-anchor="middle">$stepIcon</text>
            """.trimIndent())
            
            // Step background
            append("""
                <rect x="${theme.layout.padding + 80}" y="${yOffset - 10}" width="${theme.layout.width - 160}" height="20" 
                      fill="$stepColor" opacity="0.1" rx="4"/>
            """.trimIndent())
            
            // Step keyword
            append("""
                <text x="${theme.layout.padding + 90}" y="${yOffset + 2}" 
                      font-family="Monaco, monospace" font-size="${theme.typography.stepSize}" 
                      font-weight="bold" fill="$stepColor">${step.type.name.lowercase().capitalize()}</text>
            """.trimIndent())
            
            // Step text
            val keywordWidth = step.type.name.length * 8 + 20
            append("""
                <text x="${theme.layout.padding + 90 + keywordWidth}" y="${yOffset + 2}" 
                      font-family="${theme.typography.fontFamily}" font-size="${theme.typography.stepSize}" 
                      fill="#495057">${step.text}</text>
            """.trimIndent())
            
            // Status indicator
            append("""
                <circle cx="${theme.layout.width - 60}" cy="$yOffset" r="6" fill="$statusColor"/>
                <text x="${theme.layout.width - 60}" y="${yOffset + 2}" 
                      font-family="${theme.typography.fontFamily}" font-size="8" 
                      fill="#ffffff" text-anchor="middle">$statusIcon</text>
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
        return 40 + (scenario.steps.size * theme.layout.stepSpacing) + 20
    }

    private fun calculateTotalHeight(spec: GherkinSpec, theme: GherkinTheme): Int {
        var height = 90 // Feature header
        spec.scenarios.forEach { scenario ->
            height += calculateScenarioHeight(scenario, theme) + theme.layout.scenarioSpacing
        }
        return height + theme.layout.padding
    }
}
