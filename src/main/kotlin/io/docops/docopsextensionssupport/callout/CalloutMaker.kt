package io.docops.docopsextensionssupport.callout

import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update
import kotlinx.serialization.json.Json
import java.lang.Boolean.parseBoolean
import java.util.UUID
import kotlin.compareTo
import kotlin.div
import kotlin.rem
import kotlin.times
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Generic class for creating callout SVGs
 */
open class CalloutMaker(val csvResponse: CsvResponse) {

    private val json = Json { ignoreUnknownKeys = true }

    private interface ThemeColors {
        val bgStart: String
        val bgEnd: String
        val cardBg: String
        val textPrimary: String
        val textSecondary: String
        val accentPrimary: String
        val accentSuccess: String
    }

    private class DarkTheme : ThemeColors {
        override val bgStart = "#020617"
        override val bgEnd = "#0F172A"
        override val cardBg = "#1E293B"
        override val textPrimary = "#F8FAFC"
        override val textSecondary = "#94A3B8"
        override val accentPrimary = "#A855F7"
        override val accentSuccess = "#2DD4BF"
    }

    private class LightTheme : ThemeColors {
        override val bgStart = "#F8FAFC"
        override val bgEnd = "#F1F5F9"
        override val cardBg = "#FFFFFF"
        override val textPrimary = "#0F172A"
        override val textSecondary = "#475569"
        override val accentPrimary = "#7C3AED"
        override val accentSuccess = "#059669"
    }

    // Table format parsing methods
    fun createSystematicApproachFromTable(payload: String, width: Int, height: Int, useDark: Boolean): Pair<String, CsvResponse> {
        val calloutData = parseTableData(payload, "systematic", useDark)
        val svg = generateSystematicSvg(calloutData, width, height)
        return Pair(svg, calloutData.toCsv())
    }

    fun createMetricsFromTable(payload: String, width: Int, height: Int, useDark: Boolean): Pair<String, CsvResponse> {
        val calloutData = parseTableData(payload, "metrics", useDark)
        val svg = generateMetricsSvg(calloutData, width, height)
        return Pair(svg, calloutData.toCsv())
    }

    fun createTimelineFromTable(payload: String, width: Int, height: Int, useDark: Boolean): Pair<String, CsvResponse> {
        val calloutData = parseTableData(payload, "timeline", useDark)
        val svg = generateTimelineSvg(calloutData, width, height)
        return Pair(svg, calloutData.toCsv())
    }

    // JSON format methods
    fun createSystematicApproachSvg(payload: String, width: Int, height: Int): Pair<String, CsvResponse> {
        val calloutData = try {
            json.decodeFromString<CalloutData>(payload)
        } catch (e: Exception) {
            createDefaultCalloutData()
        }

        val svg = generateSystematicSvg(calloutData, width, height)
        return Pair(svg, calloutData.toCsv())
    }

    fun createMetricsSvg(payload: String, width: Int, height: Int): Pair<String, CsvResponse> {
        val calloutData = try {
            json.decodeFromString<CalloutData>(payload)
        } catch (e: Exception) {
            createDefaultCalloutData()
        }
        val svg = generateMetricsSvg(calloutData, width, height)
        return Pair(svg, calloutData.toCsv())
    }

    fun createTimelineSvg(payload: String, width: Int, height: Int): Pair<String, CsvResponse> {
        val calloutData = try {
            json.decodeFromString<CalloutData>(payload)
        } catch (e: Exception) {
            createDefaultCalloutData()
        }
        val svg = generateTimelineSvg(calloutData, width, height)
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

        return when (type) {
            "metrics" -> {
                val metrics = mutableMapOf<String, String>()
                var inDataSection = false
                for (line in lines) {
                    when {
                        line.startsWith("title:") -> title = line.substring(6).trim()
                        line.startsWith("title=") -> title = line.substring(6).trim()

                        line == "---" -> inDataSection = true
                        inDataSection && line.contains("|") && !isHeaderRow(line) -> {
                            val parts = line.split("|").map { it.trim() }
                            if (parts.size >= 2) {
                                metrics[parts[0]] = parts[1]
                            }
                        }
                    }
                }

                CalloutData(title = title, metrics = metrics, useDark = useDark)
            }
            "systematic" -> {
                val steps = mutableListOf<CalloutStep>()
                var inDataSection = false

                for (line in lines) {
                    when {
                        line.startsWith("title:") -> title = line.substring(6).trim()
                        line.startsWith("title=") -> title = line.substring(6).trim()

                        line == "---" -> inDataSection = true
                        inDataSection && line.contains("|") && !isHeaderRow(line) -> {
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
                }

                CalloutData(title = title, steps = steps, useDark = useDark)
            }
            else -> createDefaultCalloutData()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateSystematicSvg(calloutData: CalloutData, width: Int, height: Int): String {
        val stepHeight = 110
        val stepsCount = calloutData.steps.size
        val calculatedHeight = if (stepsCount > 0) {
            140 + (stepsCount * stepHeight) + 40
        } else {
            height
        }
        val finalHeight = calculatedHeight.coerceAtLeast(height)

        return buildString {
            val id = Uuid.random().toHexString()
            val theme = if (calloutData.useDark) DarkTheme() else LightTheme()

            append("""
                <svg id="ID_$id" width="$width" height="$finalHeight" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $finalHeight">
                    <defs>
                        <style>
                            @import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;600&amp;display=swap');
                            .title_$id { font-family: 'Outfit', sans-serif; font-weight: 600; font-size: 26px; fill: ${theme.textPrimary}; letter-spacing: -0.5px; }
                            .phase_$id { font-family: 'Outfit', sans-serif; font-weight: 600; font-size: 17px; fill: ${theme.textPrimary}; }
                            .action_$id { font-family: 'Outfit', sans-serif; font-weight: 300; font-size: 14px; fill: ${theme.textSecondary}; }
                            .result_$id { font-family: 'Outfit', sans-serif; font-weight: 600; font-size: 13px; fill: ${theme.accentSuccess}; }
                            .step-num_$id { font-family: 'Outfit', sans-serif; font-weight: 600; font-size: 12px; fill: ${theme.accentPrimary}; }
                        </style>
                        <linearGradient id="bgGrad_$id" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" stop-color="${theme.bgStart}" />
                            <stop offset="100%" stop-color="${theme.bgEnd}" />
                        </linearGradient>
                        <linearGradient id="spineGrad_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                            <stop offset="0%" stop-color="${theme.accentPrimary}" />
                            <stop offset="100%" stop-color="${theme.accentSuccess}" />
                        </linearGradient>
                    </defs>

                    <!-- Background -->
                    <rect width="100%" height="100%" fill="url(#bgGrad_$id)" rx="24"/>
                    
                    <!-- Decorative Grid -->
                    <g opacity="0.05" stroke="${theme.textPrimary}" stroke-width="1">
                        <path d="M40 0 L40 $finalHeight M${width - 40} 0 L${width - 40} $finalHeight" stroke-dasharray="4 4"/>
                    </g>

                    <!-- Header -->
                    <text x="50" y="70" class="title_$id">${calloutData.title}</text>
                    <rect x="50" y="85" width="40" height="4" fill="${theme.accentPrimary}" rx="2"/>

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
                        <circle cx="18" cy="18" r="18" fill="${theme.cardBg}" stroke="${theme.accentPrimary}" stroke-width="2"/>
                        <text x="18" y="23" text-anchor="middle" class="step-num_$id">${index + 1}</text>
                        
                        <text x="55" y="15" class="phase_$id">${step.phase}</text>
                        <text x="55" y="38" class="action_$id">${step.action}</text>
                        
                        <g transform="translate(55, 52)">
                            <rect width="$resultPillWidth" height="28" rx="14" fill="${theme.accentSuccess}" fill-opacity="0.1"/>
                            <text x="16" y="19" class="result_$id">${step.result}</text>
                        </g>
                """.trimIndent())

                step.improvement?.let { imp ->
                    // Dynamic width for improvement badge
                    val impWidth = (imp.length * 7.5) + 24
                    append("""
                        <rect x="${width - impWidth - 50}" y="10" width="$impWidth" height="22" rx="11" fill="${theme.accentPrimary}" fill-opacity="0.1"/>
                        <text x="${width - (impWidth/2) - 50}" y="25" text-anchor="middle" font-family="Outfit" font-size="10" font-weight="600" fill="${theme.accentPrimary}" style="text-transform:uppercase; letter-spacing: 0.5px;">${imp}</text>
                    """.trimIndent())
                }
                append("</g>")
            }
            append("</svg>")
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateMetricsSvg(calloutData: CalloutData, width: Int, height: Int): String {
        val metricHeight = 90
        val metricsCount = calloutData.metrics.size
        val calculatedHeight = if (metricsCount > 0) {
            140 + (metricsCount * metricHeight) + 40
        } else {
            height
        }
        val finalHeight = calculatedHeight.coerceAtLeast(height)

        return buildString {
            val id = Uuid.random().toHexString()
            val theme = if (calloutData.useDark) DarkTheme() else LightTheme()

            append("""
                <svg id="ID_$id" width="$width" height="$finalHeight" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $finalHeight">
                    <defs>
                        <style>
                            @import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;600&amp;display=swap');
                            .title_$id { font-family: 'Outfit', sans-serif; font-weight: 600; font-size: 26px; fill: ${theme.textPrimary}; letter-spacing: -0.5px; }
                            .metric-key_$id { font-family: 'Outfit', sans-serif; font-weight: 600; font-size: 15px; fill: ${theme.textSecondary}; text-transform: uppercase; letter-spacing: 1px; }
                            .metric-val_$id { font-family: 'Outfit', sans-serif; font-weight: 600; font-size: 18px; fill: ${theme.textPrimary}; }
                        </style>
                        <linearGradient id="bgGrad_$id" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" stop-color="${theme.bgStart}" />
                            <stop offset="100%" stop-color="${theme.bgEnd}" />
                        </linearGradient>
                    </defs>

                    <rect width="100%" height="100%" fill="url(#bgGrad_$id)" rx="24"/>
                    
                    <g opacity="0.05" stroke="${theme.textPrimary}" stroke-width="1">
                        <path d="M0 110 L$width 110" />
                    </g>

                    <text x="50" y="70" class="title_$id">${calloutData.title}</text>
                    <rect x="50" y="85" width="40" height="4" fill="${theme.accentPrimary}" rx="2"/>

            """.trimIndent())

            var currentY = 140
            calloutData.metrics.forEach { (key, value) ->
                // Calculate dynamic width for the value pill
                val valPillWidth = (value.length * 9.5) + 40

                append("""
                    <g transform="translate(50, $currentY)">
                        <!-- Decorative indicator -->
                        <rect width="4" height="60" fill="${theme.accentPrimary}" rx="2" opacity="0.6"/>
                        
                        <!-- Metric Info -->
                        <text x="20" y="20" class="metric-key_$id">$key</text>
                        
                        <g transform="translate(20, 30)">
                            <rect width="$valPillWidth" height="34" rx="17" fill="${theme.accentPrimary}" fill-opacity="0.08" stroke="${theme.accentPrimary}" stroke-opacity="0.2"/>
                            <text x="20" y="23" class="metric-val_$id">$value</text>
                        </g>
                    </g>
                """.trimIndent())
                currentY += metricHeight
            }

            append("</svg>")
        }
    }

    private fun generateTimelineSvg(calloutData: CalloutData, width: Int, height: Int): String {
        // Calculate dynamic height based on number of steps
        // Base height (timeline + header) + extra height for cards
        val baseHeight = 380 // Header (80) + timeline (300)
        val stepsCount = calloutData.steps.size

        // Timeline needs more height for the cards that appear above and below the timeline
        // Each step can have a card that takes up to 120px of vertical space
        val calculatedHeight = if (stepsCount > 0) {
            baseHeight + (stepsCount * 60)
        } else {
            height // Use provided height if no steps
        }

        // Use the larger of calculated height or provided height
        val finalHeight = calculatedHeight.coerceAtLeast(height)

        return buildString {
            append("""
                <svg width="$width" height="$finalHeight" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $finalHeight" preserveAspectRatio='xMidYMid meet'>
                    <defs>
            """.trimIndent())

            // Add glass-specific definitions if useGlass is true
            if (calloutData.useDark) {
                append("""
                        <!-- Glass gradients -->
                        <linearGradient id="glassGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                            <stop offset="0%" style="stop-color:rgba(255,255,255,0.3);stop-opacity:1" />
                            <stop offset="50%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
                            <stop offset="100%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1" />
                        </linearGradient>
                        <radialGradient id="glassRadial" cx="30%" cy="30%" r="70%">
                            <stop offset="0%" style="stop-color:rgba(255,255,255,0.4);stop-opacity:1" />
                            <stop offset="70%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
                            <stop offset="100%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1" />
                        </radialGradient>
                        <linearGradient id="highlight" x1="0%" y1="0%" x2="0%" y2="100%">
                            <stop offset="0%" style="stop-color:rgba(255,255,255,0.6);stop-opacity:1" />
                            <stop offset="100%" style="stop-color:rgba(255,255,255,0);stop-opacity:1" />
                        </linearGradient>

                        <!-- Glass filters -->
                        <filter id="blur" x="-50%" y="-50%" width="200%" height="200%">
                            <feGaussianBlur in="SourceGraphic" stdDeviation="3" />
                        </filter>
                        <filter id="shadow" x="-50%" y="-50%" width="200%" height="200%">
                            <feDropShadow dx="0" dy="4" stdDeviation="8" flood-color="rgba(0,0,0,0.3)"/>
                        </filter>
                        <filter id="innerShadow" x="-50%" y="-50%" width="200%" height="200%">
                            <feOffset dx="0" dy="2"/>
                            <feGaussianBlur stdDeviation="3" result="offset-blur"/>
                            <feFlood flood-color="rgba(0,0,0,0.3)"/>
                            <feComposite in2="offset-blur" operator="in"/>
                            <feComposite in2="SourceGraphic" operator="over"/>
                        </filter>
                """.trimIndent())
            }

            // Always include original gradients and iOS shadow
            append("""
                        <!-- Original gradients -->
                        <linearGradient id="headerGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" style="stop-color:#FF9500;stop-opacity:${if (calloutData.useDark) "0.8" else "1"}" />
                            <stop offset="100%" style="stop-color:#FF2D55;stop-opacity:${if (calloutData.useDark) "0.8" else "1"}" />
                        </linearGradient>
                        <filter id="iosShadow">
                            <feDropShadow dx="0" dy="2" stdDeviation="4" flood-opacity="0.1"/>
                        </filter>
                    </defs>

                    <!-- Background -->
                    <rect width="$width" height="$finalHeight" fill="${if (calloutData.useDark) "#1d4ed8" else "#F2F2F7"}" rx="0" ry="0"/>

                    <!-- Header -->
            """.trimIndent())

            // Conditionally apply glass or original styling to header
            if (calloutData.useDark) {
                append("""
                    <rect x="16" y="16" width="${width - 32}" height="60" rx="16" fill="url(#glassGradient)" stroke="rgba(255,255,255,0.3)" stroke-width="1" filter="url(#shadow)"/>
                    
                """.trimIndent())
            } else {
                append("""
                    <rect x="16" y="16" width="${width - 32}" height="60" rx="16" fill="url(#headerGrad)" filter="url(#iosShadow)"/>
                """.trimIndent())
            }

            append("""
                    <text x="${width/2}" y="53" fill="white" font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                          font-size="20" font-weight="600" text-anchor="middle">${calloutData.title}</text>
            """.trimIndent())

            // Draw timeline
            val timelineY = 320
            val timelineLength = width - 64
            val startX = 32
            val endX = startX + timelineLength

            // Timeline line with conditional styling
            if (calloutData.useDark) {
                append("""
                    <!-- Timeline Line -->
                    <line x1="$startX" y1="$timelineY" x2="$endX" y2="$timelineY" 
                          stroke="rgba(255,255,255,0.4)" stroke-width="4" stroke-linecap="round" filter="url(#blur)"/>
                """.trimIndent())
            } else {
                append("""
                    <!-- Timeline Line -->
                    <line x1="$startX" y1="$timelineY" x2="$endX" y2="$timelineY" 
                          stroke="#E5E5EA" stroke-width="4" stroke-linecap="round"/>
                """.trimIndent())
            }

            // Draw steps on timeline
            val stepCount = calloutData.steps.size
            if (stepCount > 0) {
                val stepSpacing = timelineLength / stepCount

                calloutData.steps.forEachIndexed { index, step ->
                    val x = startX + (index * stepSpacing) + (stepSpacing / 2)
                    val isCompleted = true // Assuming all steps are completed
                    val circleColor = if (isCompleted) "#FF9500" else "#8E8E93"
                    val textY = if (index % 2 == 0) timelineY - 40 else timelineY + 60
                    val lineY1 = if (index % 2 == 0) timelineY - 20 else timelineY
                    val lineY2 = if (index % 2 == 0) timelineY else timelineY + 40

                    if (calloutData.useDark) {
                        append("""
                            <!-- Step ${index + 1} -->
                            <line x1="$x" y1="$lineY1" x2="$x" y2="$lineY2" 
                                  stroke="rgba(255,255,255,0.3)" stroke-width="2" stroke-dasharray="${if (isCompleted) "none" else "4,4"}"/>
                            <circle cx="$x" cy="$timelineY" r="8" fill="url(#glassRadial)" stroke="rgba(255,255,255,0.4)" stroke-width="1" filter="url(#shadow)"/>
                            <text x="$x" y="$textY" fill="white" font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                                  font-size="14" font-weight="600" text-anchor="middle">${step.phase}</text>
                        """.trimIndent())
                    } else {
                        append("""
                            <!-- Step ${index + 1} -->
                            <line x1="$x" y1="$lineY1" x2="$x" y2="$lineY2" 
                                  stroke="#E5E5EA" stroke-width="2" stroke-dasharray="${if (isCompleted) "none" else "4,4"}"/>
                            <circle cx="$x" cy="$timelineY" r="8" fill="$circleColor" filter="url(#iosShadow)"/>
                            <text x="$x" y="$textY" fill="#1C1C1E" font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                                  font-size="14" font-weight="600" text-anchor="middle">${step.phase}</text>
                        """.trimIndent())
                    }

                    // Add card for each step
                    // Ensure cards for odd-indexed steps don't overlap with the header
                    val cardY = if (index % 2 == 0) timelineY + 30 else timelineY - 100
                    if (index % 2 == 1) {
                        if (calloutData.useDark) {
                            append("""
                                <rect x="${x - 100}" y="$cardY" width="200" height="80" rx="16" fill="url(#glassGradient)" 
                                      stroke="rgba(255,255,255,0.3)" stroke-width="1" filter="url(#shadow)"/>
                                <!-- Card highlight -->
                                <rect x="${x - 95}" y="${cardY + 5}" width="190" height="15" rx="7" fill="url(#highlight)"/>
                                <text x="${x - 90}" y="${cardY + 25}" fill="white" font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                                      font-size="14" font-weight="600">${step.action}</text>
                                <text x="${x - 90}" y="${cardY + 50}" fill="rgba(255,255,255,0.8)" font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                                      font-size="12" font-weight="600">${step.result}</text>
                            """.trimIndent())
                        } else {
                            append("""
                                <rect x="${x - 100}" y="$cardY" width="200" height="80" rx="16" fill="white" 
                                      stroke="#E5E5EA" stroke-width="1" filter="url(#iosShadow)"/>
                                <text x="${x - 90}" y="${cardY + 25}" fill="#1C1C1E" font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                                      font-size="14" font-weight="600">${step.action}</text>
                                <text x="${x - 90}" y="${cardY + 50}" fill="#FF9500" font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                                      font-size="12" font-weight="600">${step.result}</text>
                            """.trimIndent())
                        }
                    }
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
