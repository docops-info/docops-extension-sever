package io.docops.docopsextensionssupport.callout

import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.web.CsvResponse
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Generic class for creating callout SVGs
 */
open class CalloutMaker(val useDark: Boolean) {

    private val theme = ThemeFactory.getThemeByName("premium", useDark)

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
        val stepHeight = 120
        val stepsCount = calloutData.steps.size
        val calculatedHeight = if (stepsCount > 0) {
            160 + (stepsCount * stepHeight) + 40
        } else {
            height
        }
        val finalHeight = calculatedHeight.coerceAtLeast(height)
        val fScale = scale.toDoubleOrNull() ?: 1.0
        val scaledWidth = (width * fScale).toInt()
        val scaledHeight = (finalHeight * fScale).toInt()

        val stopColor = theme.surfaceLow
        return buildString {
            val id = Uuid.random().toHexString()

            append("""
                <svg id="ID_$id" width="$scaledWidth" height="$scaledHeight" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $finalHeight" role="img" aria-labelledby="title_$id desc_$id">
                    <title id="title_$id">${calloutData.title.escapeXml()}</title>
                    <desc id="desc_$id">A systematic process diagram with $stepsCount steps</desc>
                    <defs>
                        <style>
                            ${theme.fontImport}
                            .title_$id { font-family: ${theme.fontFamily}; font-weight: 700; font-size: 24px; fill: ${theme.primaryText}; letter-spacing: -0.5px; }
                            .phase_$id { font-family: ${theme.fontFamily}; font-weight: 600; font-size: 18px; fill: ${theme.primaryText}; }
                            .action_$id { font-family: ${theme.fontFamily}; font-weight: 400; font-size: 14px; fill: ${theme.secondaryText}; }
                            .result_$id { font-family: ${theme.fontFamily}; font-weight: 600; font-size: 14px; fill: ${theme.accentColor}; }
                            .step-num_$id { font-family: ${theme.fontFamily}; font-weight: 600; font-size: 12px; fill: ${theme.accentColor}; }
                        </style>
                        <linearGradient id="bgGrad_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                            <stop offset="0%" stop-color="${theme.canvas}" />
                            <stop offset="100%" stop-color="$stopColor" />
                        </linearGradient>
                        <linearGradient id="spineGrad_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                            <stop offset="0%" stop-color="${theme.accentColor}" />
                            <stop offset="100%" stop-color="${theme.surfaceLow}" />
                        </linearGradient>
                    </defs>

                    <!-- Background -->
                    <rect width="100%" height="100%" fill="url(#bgGrad_$id)" rx="${theme.cornerRadius}"/>
                    
                    <!-- Decorative Grid -->
                    <g opacity="0.05" stroke="${theme.primaryText}" stroke-width="1">
                        <path d="M48 0 L48 $finalHeight M${width - 48} 0 L${width - 48} $finalHeight" stroke-dasharray="4 4"/>
                    </g>

                    <!-- Header -->
                    <text x="48" y="64" class="title_$id">${calloutData.title}</text>
                    <rect x="48" y="80" width="48" height="4" fill="${theme.accentColor}" rx="2"/>

                    <!-- Vertical Spine -->
                    <rect x="66" y="120" width="2" height="${stepsCount * stepHeight}" fill="url(#spineGrad_$id)" opacity="0.3"/>

                    <!-- Steps -->
            """.trimIndent())

            calloutData.steps.forEachIndexed { index, step ->
                val y = 120 + (index * stepHeight)
                // Calculate dynamic width for the result pill based on text length
                val resultPillWidth = (step.result.length * 8.5) + 32

                append("""
                    <g transform="translate(48, $y)">
                        <circle cx="18" cy="18" r="18" fill="${theme.canvas}" stroke="${theme.accentColor}" stroke-width="2"/>
                        <text x="18" y="23" text-anchor="middle" class="step-num_$id">${index + 1}</text>
                        
                        <text x="56" y="18" class="phase_$id">${step.phase}</text>
                        <text x="56" y="44" class="action_$id">${step.action}</text>
                        
                        <g transform="translate(56, 64)">
                            <rect width="$resultPillWidth" height="32" rx="16" fill="${theme.accentColor}" fill-opacity="0.1"/>
                            <text x="16" y="21" class="result_$id">${step.result}</text>
                        </g>
                """.trimIndent())

                step.improvement?.let { imp ->
                    // Dynamic width for improvement badge
                    val impWidth = (imp.length * 7.5) + 24
                    append("""
                        <rect x="${width - impWidth - 96}" y="10" width="$impWidth" height="24" rx="12" fill="${theme.accentColor}" fill-opacity="0.1"/>
                        <text x="${width - (impWidth/2) - 96}" y="26" text-anchor="middle" font-family="${theme.fontFamily}" font-size="12" font-weight="600" fill="${theme.accentColor}" style="text-transform:uppercase; letter-spacing: 0.5px;">${imp}</text>
                    """.trimIndent())
                }
                append("</g>")
            }
            append("</svg>")
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateMetricsSvg(calloutData: CalloutData, width: Int, height: Int, scale: String): String {
        val metricHeight = 96
        val metricsCount = calloutData.metrics.size
        val calculatedHeight = if (metricsCount > 0) {
            160 + (metricsCount * metricHeight) + 40
        } else {
            height
        }
        val finalHeight = calculatedHeight.coerceAtLeast(height)
        val fScale = scale.toDoubleOrNull() ?: 1.0
        val scaledWidth = (width * fScale).toInt()
        val scaledHeight = (finalHeight * fScale).toInt()

        val stopColor = theme.surfaceLow
        return buildString {
            val id = Uuid.random().toHexString()
            append("""
                <svg id="ID_$id" width="$scaledWidth" height="$scaledHeight" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $finalHeight" role="img" aria-labelledby="title_$id desc_$id">
                    <title id="title_$id">${calloutData.title.escapeXml()}</title>
                    <desc id="desc_$id">A metrics callout displaying $metricsCount key values</desc>
                    <defs>
                        <style>
                            ${theme.fontImport}
                            .title_$id { font-family: ${theme.fontFamily}; font-weight: 700; font-size: 24px; fill: ${theme.primaryText}; letter-spacing: -0.5px; }
                            .metric-key_$id { font-family: ${theme.fontFamily}; font-weight: 600; font-size: 14px; fill: ${theme.secondaryText}; text-transform: uppercase; letter-spacing: 1px; }
                            .metric-val_$id { font-family: ${theme.fontFamily}; font-weight: 600; font-size: 18px; fill: ${theme.primaryText}; }
                        </style>
                        <linearGradient id="bgGrad_$id" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" stop-color="${theme.canvas}" />
                            <stop offset="100%" stop-color="$stopColor" />
                        </linearGradient>
                    </defs>

                    <rect width="100%" height="100%" fill="url(#bgGrad_$id)" rx="${theme.cornerRadius}"/>
                    
                    <g opacity="0.05" stroke="${theme.primaryText}" stroke-width="1">
                        <path d="M0 112 L$width 112" />
                    </g>

                    <text x="48" y="64" class="title_$id">${calloutData.title}</text>
                    <rect x="48" y="80" width="48" height="4" fill="${theme.accentColor}" rx="2"/>

            """.trimIndent())

            var currentY = 144
            calloutData.metrics.forEach { (key, value) ->
                // Calculate dynamic width for the value pill
                val valPillWidth = (value.length * 9.5) + 40

                append("""
                    <g transform="translate(48, $currentY)">
                        <!-- Decorative indicator -->
                        <rect width="4" height="64" fill="${theme.accentColor}" rx="2" opacity="0.6"/>
                        
                        <!-- Metric Info -->
                        <text x="24" y="20" class="metric-key_$id">$key</text>
                        
                        <g transform="translate(24, 32)">
                            <rect width="$valPillWidth" height="34" rx="8" fill="${theme.accentColor}" fill-opacity="0.08" stroke="${theme.accentColor}" stroke-opacity="0.2"/>
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
        val baseHeight = 480
        val finalHeight = baseHeight.coerceAtLeast(height)
        val fScale = scale.toDoubleOrNull() ?: 1.0
        val scaledWidth = (width * fScale).toInt()
        val scaledHeight = (finalHeight * fScale).toInt()
        val id = Uuid.random().toHexString()

        // Neutral shadow derived from surface tokens
        val shadowColor = if(useDark) "#000000" else "#64748b"
        val shadowAlpha = if (useDark) 0.5 else 0.15

        return buildString {
            append("""
                <svg id="ID_$id" width="$scaledWidth" height="$scaledHeight" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $finalHeight" preserveAspectRatio='xMidYMid meet' role="img" aria-labelledby="title_$id desc_$id">
                    <title id="title_$id">${calloutData.title.escapeXml()}</title>
                    <desc id="desc_$id">A horizontal timeline with $stepsCount events</desc>
                    <defs>
                        ${theme.fontImport}
                        <style>
                            .title_$id { font-family: ${theme.fontFamily}; font-weight: 700; font-size: 24px; fill: ${theme.primaryText}; letter-spacing: -0.5px; }
                            .phase_$id { font-family: ${theme.fontFamily}; font-weight: 600; font-size: 16px; fill: ${theme.primaryText}; }
                            .action_$id { font-family: ${theme.fontFamily}; font-weight: 400; font-size: 14px; fill: ${theme.secondaryText}; }
                            .result_$id { font-family: ${theme.fontFamily}; font-weight: 700; font-size: 12px; fill: ${theme.accentColor}; text-transform: uppercase; letter-spacing: 0.5px; }
                        </style>
                        <filter id="shadow_$id">
                            <feDropShadow dx="0" dy="4" stdDeviation="8" flood-color="$shadowColor" flood-opacity="$shadowAlpha"/>
                        </filter>
                    </defs>

                    <!-- Background -->
                    <rect width="100%" height="100%" fill="${theme.canvas}" rx="${theme.cornerRadius}"/>

                    <!-- Header -->
                    <text x="48" y="64" class="title_$id">${calloutData.title}</text>
                    <rect x="48" y="80" width="64" height="4" fill="${theme.accentColor}" rx="2"/>

                    <!-- Timeline Line -->
                    <line x1="48" y1="256" x2="${width - 48}" y2="256" stroke="${theme.secondaryText}" stroke-width="2" stroke-opacity="0.2"/>
            """.trimIndent())

            val timelineY = 256
            val timelineLength = width - 128
            val startX = 64

            if (stepsCount > 0) {
                val stepSpacing = if (stepsCount > 1) timelineLength / (stepsCount - 1) else timelineLength
                calloutData.steps.forEachIndexed { index, step ->
                    val x = startX + (index * stepSpacing)
                    val isTop = index % 2 != 0
                    val cardY = if (isTop) timelineY - 160 else timelineY + 48
                    val stemY1 = if (isTop) timelineY - 48 else timelineY
                    val stemY2 = if (isTop) timelineY else timelineY + 48

                    append("""
                        <!-- Step ${index + 1} -->
                        <line x1="$x" y1="$stemY1" x2="$x" y2="$stemY2" stroke="${theme.accentColor}" stroke-width="1.5" stroke-dasharray="4,2"/>
                        
                        <!-- Status Indicator -->
                        <g transform="translate(${x - 12}, ${timelineY - 12})">
                            <circle cx="12" cy="12" r="10" fill="${theme.canvas}" stroke="${theme.success}" stroke-width="2"/>
                            <path d="M8 12l3 3 5-5" stroke="${theme.success}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
                        </g>
                        
                        <!-- Content Card -->
                        <g transform="translate(${x - 90}, $cardY)" filter="url(#shadow_$id)">
                            <rect width="180" height="112" rx="${theme.cornerRadius}" fill="${theme.canvas}" stroke="${theme.surfaceLow}" stroke-width="1"/>
                            <rect width="4" height="112" fill="${theme.accentColor}" rx="2"/>
                            
                            <text x="16" y="28" class="phase_$id">${step.phase}</text>
                            <text x="16" y="52" class="action_$id">${step.action}</text>
                            
                            <g transform="translate(16, 76)">
                                <rect width="${(step.result.length * 8.0) + 16}" height="24" rx="4" fill="${theme.accentColor}" fill-opacity="0.1"/>
                                <text x="8" y="16" class="result_$id">${step.result}</text>
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
