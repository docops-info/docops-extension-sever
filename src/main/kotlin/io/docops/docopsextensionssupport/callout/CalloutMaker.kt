package io.docops.docopsextensionssupport.callout

import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.web.CsvResponse
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Generic class for creating callout SVGs
 */
open class CalloutMaker(val useDark: Boolean) {

    private val theme = ThemeFactory.getThemeByName("brand", useDark)

    fun createSystematicApproachFromTable(payload: String, width: Int, height: Int, scale: String = "1.0"): Pair<String, CsvResponse> {
        val calloutData = parseTableData(payload, "systematic", useDark)
        val svg = generateSystematicSvg(calloutData, width, height, scale)
        return Pair(svg, calloutData.toCsv())
    }

    fun createMetricsFromTable(payload: String, width: Int, height: Int, scale: String = "1.0"): Pair<String, CsvResponse> {
        val calloutData = parseTableData(payload, "metrics", useDark)
        val svg = generateMetricsSvg(calloutData, width, height, scale)
        return Pair(svg, calloutData.toCsv())
    }

    fun createTimelineFromTable(payload: String, width: Int, height: Int, scale: String = "1.0"): Pair<String, CsvResponse> {
        val calloutData = parseTableData(payload, "timeline", useDark)
        val svg = generateTimelineSvg(calloutData, width, height, scale)
        return Pair(svg, calloutData.toCsv())
    }

    /**
     * Helper function to detect common header row patterns
     * Identifies standard header patterns in callout tables
     */
    private fun isHeaderRow(line: String): Boolean {
        val lowerLine = line.lowercase()
        return (lowerLine.contains("metric") && lowerLine.contains("value")) ||
                (lowerLine.contains("phase") && lowerLine.contains("action")) ||
                (lowerLine.contains("step") && lowerLine.contains("result")) ||
                (lowerLine.contains("timeline") && lowerLine.contains("event"))
    }

    /**
     * Extract title from a line using either format: "title=" or "title:"
     */
    private fun extractTitle(line: String): String? {
        return when {
            line.startsWith("title=") -> line.substring(6)
            line.startsWith("title:") -> line.substring(6)
            else -> null
        }
    }

    private fun parseTableData(data: String, type: String, useDark: Boolean): CalloutData {
        val lines = data.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        var title = "Callout"

        // Pre-parse title from any line starting with title= or title:
        val titleRegex = """^title[:=]\s*(.*?)(?:\s+type=|\s*$)""".toRegex(RegexOption.IGNORE_CASE)
        lines.forEach { line ->
            val match = titleRegex.find(line)
            if (match != null) {
                title = match.groupValues[1].trim()
            }
        }
        return when (type) {
            "metrics" -> {
                val metrics = mutableMapOf<String, String>()
                var inDataSection = false
                for (line in lines) {
                    if (line == "---") {
                        inDataSection = true
                        continue
                    }
                    if (inDataSection && line.contains("|") && !isHeaderRow(line)) {
                        val parts = line.split("|").map { it.trim() }
                        if (parts.size >= 2) {
                            metrics[parts[0]] = parts[1]
                        }
                    }
                }
                CalloutData(title = title, metrics = metrics, useDark = useDark)
            }
            "systematic", "timeline" -> {
                val steps = mutableListOf<CalloutStep>()
                var inDataSection = false

                for (line in lines) {
                    if (line == "---") {
                        inDataSection = true
                        continue
                    }
                    if (inDataSection && line.contains("|") && !isHeaderRow(line)) {
                        val parts = line.split("|").map { it.trim() }
                        if (parts.size >= 3) {
                            val phase = parts[0]
                            val action = parts[1]
                            val result = parts[2]
                            val improvement = if (parts.size > 3) parts[3] else null
                            steps.add(CalloutStep(phase, action, result, improvement))
                        }
                    }
                }
                CalloutData(title = title, steps = steps, useDark = useDark)
            }
            else -> createDefaultCalloutData().copy(title = title)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateSystematicSvg(calloutData: CalloutData, width: Int, height: Int, scale: String): String {
        val stepHeight = 110
        val stepsCount = calloutData.steps.size
        val calculatedHeight = if (stepsCount > 0) {
            140 + (stepsCount * stepHeight) + 40
        } else {
            height
        }
        val finalHeight = calculatedHeight.coerceAtLeast(height)
        val fScale = scale.toDoubleOrNull() ?: 1.0
        val scaledWidth = (width * fScale).toInt()
        val scaledHeight = (finalHeight * fScale).toInt()

        var stopColor = theme.surfaceImpact
        if(!useDark) {
            stopColor = SVGColor(theme.canvas).darker()!!
        }
        return buildString {
            val id = Uuid.random().toHexString()
            //val theme = if (calloutData.useDark) DarkTheme() else LightTheme()

            append("""
                <svg id="ID_$id" width="$scaledWidth" height="$scaledHeight" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $finalHeight">
                    <defs>
                        <style>
                            ${theme.fontImport}
                            .title_$id { font-family: ${theme.fontFamily}; font-weight: 600; font-size: 26px; fill: ${theme.primaryText}; letter-spacing: -0.5px; }
                            .phase_$id { font-family: ${theme.fontFamily}; font-weight: 600; font-size: 17px; fill: ${theme.primaryText}; }
                            .action_$id { font-family: ${theme.fontFamily}; font-weight: 400; font-size: 14px; fill: ${theme.secondaryText}; }
                            .result_$id { font-family: ${theme.fontFamily}; font-weight: 600; font-size: 13px; fill: ${theme.accentColor}; }
                            .step-num_$id { font-family: ${theme.fontFamily}; font-weight: 600; font-size: 12px; fill: ${theme.accentColor}; }
                        </style>
                        <linearGradient id="bgGrad_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                            <stop offset="0%" stop-color="${theme.canvas}" />
                            <stop offset="100%" stop-color="$stopColor" />
                        </linearGradient>
                        <linearGradient id="spineGrad_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                            <stop offset="0%" stop-color="${theme.accentColor}" />
                            <stop offset="100%" stop-color="${theme.surfaceImpact}" />
                        </linearGradient>
                    </defs>

                    <!-- Background -->
                    <rect width="100%" height="100%" fill="url(#bgGrad_$id)" rx="24"/>
                    
                    <!-- Decorative Grid -->
                    <g opacity="0.05" stroke="${theme.primaryText}" stroke-width="1">
                        <path d="M40 0 L40 $finalHeight M${width - 40} 0 L${width - 40} $finalHeight" stroke-dasharray="4 4"/>
                    </g>

                    <!-- Header -->
                    <text x="50" y="70" class="title_$id">${calloutData.title}</text>
                    <rect x="50" y="85" width="40" height="4" fill="${theme.secondaryText}" rx="2"/>

                    <!-- Vertical Spine -->
                    <rect x="68" y="130" width="2" height="${stepsCount * stepHeight}" fill="url(#spineGrad_$id)" opacity="0.3"/>

                    <!-- Steps -->
            """.trimIndent())

            calloutData.steps.forEachIndexed { index, step ->
                val y = 140 + (index * stepHeight)
                // Calculate dynamic width for the result pill based on text length
                // Approx 8.5px per character + 32px padding
                val resultPillWidth = (step.result.length * 8.5) + 32

                append("""
                    <g transform="translate(50, $y)">
                        <circle cx="18" cy="18" r="18" fill="${theme.canvas}" stroke="${theme.accentColor}" stroke-width="2"/>
                        <text x="18" y="23" text-anchor="middle" class="step-num_$id">${index + 1}</text>
                        
                        <text x="55" y="15" class="phase_$id">${step.phase}</text>
                        <text x="55" y="38" class="action_$id">${step.action}</text>
                        
                        <g transform="translate(55, 52)">
                            <rect width="$resultPillWidth" height="28" rx="14" fill="${theme.accentColor}" fill-opacity="0.1"/>
                            <text x="16" y="19" class="result_$id">${step.result}</text>
                        </g>
                """.trimIndent())

                step.improvement?.let { imp ->
                    // Dynamic width for improvement badge
                    val impWidth = (imp.length * 7.5) + 24
                    append("""
                        <rect x="${width - impWidth - 50}" y="10" width="$impWidth" height="22" rx="11" fill="${theme.accentColor}" fill-opacity="0.1"/>
                        <text x="${width - (impWidth/2) - 50}" y="25" text-anchor="middle" font-family="${theme.fontFamily}" font-size="10" font-weight="600" fill="${theme.accentColor}" style="text-transform:uppercase; letter-spacing: 0.5px;">${imp}</text>
                    """.trimIndent())
                }
                append("</g>")
            }
            append("</svg>")
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateMetricsSvg(calloutData: CalloutData, width: Int, height: Int, scale: String): String {
        val metricHeight = 90
        val metricsCount = calloutData.metrics.size
        val calculatedHeight = if (metricsCount > 0) {
            140 + (metricsCount * metricHeight) + 40
        } else {
            height
        }
        val finalHeight = calculatedHeight.coerceAtLeast(height)
        val fScale = scale.toDoubleOrNull() ?: 1.0
        val scaledWidth = (width * fScale).toInt()
        val scaledHeight = (finalHeight * fScale).toInt()

        return buildString {
            val id = Uuid.random().toHexString()
            var stopColor = theme.surfaceImpact
            if(!useDark) {
                stopColor = SVGColor(theme.canvas).darker()!!
            }
            append("""
                <svg id="ID_$id" width="$scaledWidth" height="$scaledHeight" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $finalHeight">
                    <defs>
                        <style>
                            ${theme.fontImport}
                            .title_$id { font-family: ${theme.fontFamily}; font-weight: 600; font-size: 26px; fill: ${theme.primaryText}; letter-spacing: -0.5px; }
                            .metric-key_$id { font-family: ${theme.fontFamily}; font-weight: 600; font-size: 15px; fill: ${theme.secondaryText}; text-transform: uppercase; letter-spacing: 1px; }
                            .metric-val_$id { font-family: ${theme.fontFamily}; font-weight: 600; font-size: 18px; fill: ${theme.primaryText}; }
                        </style>
                        <linearGradient id="bgGrad_$id" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" stop-color="${theme.canvas}" />
                            <stop offset="100%" stop-color="$stopColor" />
                        </linearGradient>
                    </defs>

                    <rect width="100%" height="100%" fill="url(#bgGrad_$id)" rx="24"/>
                    
                    <g opacity="0.05" stroke="${theme.primaryText}" stroke-width="1">
                        <path d="M0 110 L$width 110" />
                    </g>

                    <text x="50" y="70" class="title_$id">${calloutData.title}</text>
                    <rect x="50" y="85" width="40" height="4" fill="${theme.accentColor}" rx="2"/>

            """.trimIndent())

            var currentY = 140
            calloutData.metrics.forEach { (key, value) ->
                // Calculate dynamic width for the value pill
                val valPillWidth = (value.length * 9.5) + 40

                append("""
                    <g transform="translate(50, $currentY)">
                        <!-- Decorative indicator -->
                        <rect width="4" height="60" fill="${theme.accentColor}" rx="2" opacity="0.6"/>
                        
                        <!-- Metric Info -->
                        <text x="20" y="20" class="metric-key_$id">$key</text>
                        
                        <g transform="translate(20, 30)">
                            <rect width="$valPillWidth" height="34" rx="17" fill="${theme.accentColor}" fill-opacity="0.08" stroke="${theme.accentColor}" stroke-opacity="0.2"/>
                            <text x="20" y="23" class="metric-val_$id">$value</text>
                        </g>
                    </g>
                """.trimIndent())
                currentY += metricHeight
            }

            append("</svg>")
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateTimelineSvg(calloutData: CalloutData, width: Int, height: Int, scale: String): String {
        val stepsCount = calloutData.steps.size
        // Timeline needs more height for the cards that appear above and below the timeline
        val baseHeight = 450
        val finalHeight = baseHeight.coerceAtLeast(height)
        val fScale = scale.toDoubleOrNull() ?: 1.0
        val scaledWidth = (width * fScale).toInt()
        val scaledHeight = (finalHeight * fScale).toInt()
        val id = Uuid.random().toHexString()

        // Authored shadow color derived from theme canvas
        val shadowColor = SVGColor(theme.canvas).darkenColor(theme.canvas, 0.5)
        val shadowAlpha = if (useDark) 0.5 else 0.2

        return buildString {
            append("""
                <svg id="ID_$id" width="$scaledWidth" height="$scaledHeight" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $finalHeight" preserveAspectRatio='xMidYMid meet'>
                    <defs>
                        ${theme.fontImport}
                        <style>
                            .title_$id { font-family: ${theme.fontFamily}; font-weight: 700; font-size: 24px; fill: ${theme.primaryText}; letter-spacing: -0.5px; }
                            .phase_$id { font-family: ${theme.fontFamily}; font-weight: 600; font-size: 15px; fill: ${theme.primaryText}; }
                            .action_$id { font-family: ${theme.fontFamily}; font-weight: 400; font-size: 14px; fill: ${theme.secondaryText}; }
                            .result_$id { font-family: ${theme.fontFamily}; font-weight: 700; font-size: 11px; fill: ${theme.accentColor}; text-transform: uppercase; letter-spacing: 0.5px; }
                        </style>
                        <filter id="shadow_$id">
                            <feDropShadow dx="0" dy="2" stdDeviation="4" flood-color="$shadowColor" flood-opacity="$shadowAlpha"/>
                        </filter>
                    </defs>

                    <!-- Background -->
                    <rect width="100%" height="100%" fill="${theme.canvas}" rx="16"/>

                    <!-- Header -->
                    <text x="40" y="55" class="title_$id">${calloutData.title}</text>
                    <rect x="40" y="70" width="60" height="4" fill="${theme.accentColor}" rx="2"/>

                    <!-- Timeline Line -->
                    <line x1="40" y1="250" x2="${width - 40}" y2="250" stroke="${theme.secondaryText}" stroke-width="2" stroke-opacity="0.3"/>
            """.trimIndent())

            val timelineY = 250
            val timelineLength = width - 100
            val startX = 50

            if (stepsCount > 0) {
                val stepSpacing = if (stepsCount > 1) timelineLength / (stepsCount - 1) else timelineLength
                calloutData.steps.forEachIndexed { index, step ->
                    val x = startX + (index * stepSpacing)
                    val isTop = index % 2 != 0
                    val cardY = if (isTop) timelineY - 160 else timelineY + 40
                    val stemY1 = if (isTop) timelineY - 40 else timelineY
                    val stemY2 = if (isTop) timelineY else timelineY + 40

                    append("""
                        <!-- Step ${index + 1} -->
                        <line x1="$x" y1="$stemY1" x2="$x" y2="$stemY2" stroke="${theme.accentColor}" stroke-width="1.5" stroke-dasharray="2,2"/>
                        
                        <!-- Status Indicator with Redundant Cue (Industrial Utilitarian) -->
                        <g transform="translate(${x - 12}, ${timelineY - 12})">
                            <circle cx="12" cy="12" r="10" fill="${theme.canvas}" stroke="${theme.accentColor}" stroke-width="2"/>
                            <path d="M8 12l3 3 5-5" stroke="${theme.accentColor}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
                        </g>
                        
                        <!-- Content Card (Editorial Style) -->
                        <g transform="translate(${x - 90}, $cardY)" filter="url(#shadow_$id)">
                            <rect width="180" height="110" rx="4" fill="${theme.canvas}" stroke="${theme.surfaceImpact}" stroke-width="1"/>
                            <rect width="4" height="110" fill="${theme.accentColor}" rx="2"/>
                            
                            <text x="15" y="25" class="phase_$id">${step.phase}</text>
                            <text x="15" y="50" class="action_$id">${step.action}</text>
                            
                            <g transform="translate(15, 75)">
                                <rect width="${(step.result.length * 8.0) + 16}" height="22" rx="4" fill="${theme.accentColor}" fill-opacity="0.1"/>
                                <text x="8" y="15" class="result_$id">${step.result}</text>
                            </g>
                        </g>
                    """.trimIndent())
                }
            }
            append("</svg>")
        }
    }

    private fun createDefaultCalloutData(): CalloutData {
        return CalloutData(
            title = "Systematic Problem-Solving Approach",
            steps = listOf(
                CalloutStep(
                    phase = "1. Infrastructure",
                    action = "Provisioned larger database instance",
                    result = "CPU: 88% → 77%",
                    improvement = "modest improvement"
                ),
                CalloutStep(
                    phase = "2. Code Optimization",
                    action = "Removed UPPER() functions from SQL queries",
                    result = "CPU: 77% → 60%",
                    improvement = "significant improvement"
                ),
                CalloutStep(
                    phase = "3. AI-Assisted Analysis",
                    action = "Analyzed table definitions, queries & execution plans",
                    result = "Identified indexing opportunity"
                ),
                CalloutStep(
                    phase = "4. Index Optimization",
                    action = "Created composite index based on AI recommendations",
                    result = "Query cost: 12,000 → 405 (97% reduction)"
                )
            ),
            metrics = mapOf(
                "Query Performance" to "97% reduction in execution cost",
                "Database CPU" to "Reduced from 88% to 60%",
                "Methodology" to "Data-driven approach combining team expertise with AI insights"
            )
        )
    }



}
