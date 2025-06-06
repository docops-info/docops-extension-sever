package io.docops.docopsextensionssupport.callout

import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Generic class for creating callout SVGs
 */
open class CalloutMaker {

    private val json = Json { ignoreUnknownKeys = true }

    // Table format parsing methods
    fun createSystematicApproachFromTable(data: String, width: Int, height: Int): String {
        val calloutData = parseTableData(data, "systematic")
        return generateSystematicSvg(calloutData, width, height)
    }

    fun createMetricsFromTable(data: String, width: Int, height: Int): String {
        val calloutData = parseTableData(data, "metrics")
        return generateMetricsSvg(calloutData, width, height)
    }

    fun createTimelineFromTable(data: String, width: Int, height: Int): String {
        val calloutData = parseTableData(data, "timeline")
        return generateTimelineSvg(calloutData, width, height)
    }

    // JSON format methods
    fun createSystematicApproachSvg(data: String, width: Int, height: Int): String {
        val calloutData = try {
            json.decodeFromString<CalloutData>(data)
        } catch (e: Exception) {
            createDefaultCalloutData()
        }
        return generateSystematicSvg(calloutData, width, height)
    }

    fun createMetricsSvg(data: String, width: Int, height: Int): String {
        val calloutData = try {
            json.decodeFromString<CalloutData>(data)
        } catch (e: Exception) {
            createDefaultCalloutData()
        }
        return generateMetricsSvg(calloutData, width, height)
    }

    fun createTimelineSvg(data: String, width: Int, height: Int): String {
        val calloutData = try {
            json.decodeFromString<CalloutData>(data)
        } catch (e: Exception) {
            createDefaultCalloutData()
        }
        return generateTimelineSvg(calloutData, width, height)
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

    private fun parseTableData(data: String, type: String): CalloutData {
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

                CalloutData(title = title, metrics = metrics)
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

                CalloutData(title = title, steps = steps)
            }
            else -> createDefaultCalloutData()
        }
    }

    private fun generateSystematicSvg(calloutData: CalloutData, width: Int, height: Int): String {
        // Calculate dynamic height based on number of steps
        // Header height (92) + (steps count * stepHeight) + bottom padding (20)
        val stepHeight = 90
        val stepsCount = calloutData.steps.size
        val calculatedHeight = if (stepsCount > 0) {
            92 + (stepsCount * stepHeight) + 20
        } else {
            height // Use provided height if no steps
        }

        // Use the larger of calculated height or provided height
        val finalHeight = calculatedHeight.coerceAtLeast(height)

        return buildString {
            val id = UUID.randomUUID().toString()
            append("""
                <svg id="ID_$id" width="$width" height="$finalHeight" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $finalHeight" preserveAspectRatio='xMidYMid meet'>
                    <defs>
                        <linearGradient id="headerGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" style="stop-color:#0A84FF;stop-opacity:1" />
                            <stop offset="100%" style="stop-color:#007AFF;stop-opacity:1" />
                        </linearGradient>
                        <linearGradient id="stepGrad" x1="0%" y1="0%" x2="0%" y2="100%">
                            <stop offset="0%" style="stop-color:#FFFFFF;stop-opacity:1" />
                            <stop offset="100%" style="stop-color:#F2F2F7;stop-opacity:1" />
                        </linearGradient>
                        <filter id="iosShadow">
                            <feDropShadow dx="0" dy="2" stdDeviation="4" flood-opacity="0.1"/>
                        </filter>
                    </defs>

                    <!-- Background -->
                    <rect width="$width" height="$finalHeight" fill="#F2F2F7" rx="0" ry="0"/>

                    <!-- Header -->
                    <rect x="16" y="16" width="${width - 32}" height="60" rx="16" fill="url(#headerGrad)" filter="url(#iosShadow)"/>
                    <text x="${width/2}" y="53" fill="white" font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" font-size="20" font-weight="600" text-anchor="middle">
                        ${calloutData.title}
                    </text>
            """.trimIndent())

            // Draw steps
            val stepHeight = 90
            val startY = 92

            calloutData.steps.forEachIndexed { index, step ->
                val y = startY + (index * stepHeight)

                append("""
                    <!-- Step ${index + 1} Background -->
                    <rect x="16" y="$y" width="${width - 32}" height="80" fill="url(#stepGrad)" 
                          stroke="#E5E5EA" stroke-width="1" rx="16" filter="url(#iosShadow)"/>

                    <!-- Step Number -->
                    <circle cx="48" cy="${y + 40}" r="16" fill="#007AFF" filter="url(#iosShadow)"/>
                    <text x="48" y="${y + 45}" fill="white" font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                          font-size="14" font-weight="600" text-anchor="middle">${index + 1}</text>

                    <!-- Phase Label -->
                    <text x="80" y="${y + 30}" fill="#1C1C1E" font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                          font-size="16" font-weight="600">${step.phase}</text>

                    <!-- Action -->
                    <text x="80" y="${y + 50}" fill="#8E8E93" font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" font-size="14">
                        ${step.action}
                    </text>

                    <!-- Result -->
                    <text x="80" y="${y + 70}" fill="#34C759" font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                          font-size="14" font-weight="600">${step.result}</text>
                """.trimIndent())

                // Add improvement indicator if present
                step.improvement?.let { improvement ->
                    // Calculate width based on text length (minimum 110px, 10px per character)
                    val textWidth = maxOf(110, improvement.length * 10)
                    val rectX = width - textWidth - 20 // 20px padding from right edge
                    val textX = rectX + (textWidth / 2) // Center text in rectangle

                    append("""
                        <rect x="$rectX" y="${y + 15}" width="$textWidth" height="24" fill="#F2F9F6" 
                              stroke="#34C759" stroke-width="1" rx="12" filter="url(#iosShadow)"/>
                        <text x="$textX" y="${y + 30}" fill="#34C759" font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                              font-size="12" font-weight="600" text-anchor="middle">$improvement</text>
                    """.trimIndent())
                }
            }

            append("</svg>")
        }
    }
    private fun generateMetricsSvg(calloutData: CalloutData, width: Int, height: Int): String {
        // Calculate dynamic height based on number of metrics
        // Header height (92) + (metrics count * 70) + bottom padding (20)
        val metricsCount = calloutData.metrics.size
        val calculatedHeight = if (metricsCount > 0) {
            92 + (metricsCount * 70) + 20
        } else {
            height // Use provided height if no metrics
        }

        // Use the larger of calculated height or provided height
        val finalHeight = calculatedHeight.coerceAtLeast(height)

        return buildString {
            append("""
            <svg width="$width" height="$finalHeight" xmlns="http://www.w3.org/2000/svg">
                <defs>
                    <linearGradient id="headerGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                        <stop offset="0%" style="stop-color:#5856D6;stop-opacity:1" />
                        <stop offset="100%" style="stop-color:#AF52DE;stop-opacity:1" />
                    </linearGradient>
                    <filter id="iosShadow">
                        <feDropShadow dx="0" dy="2" stdDeviation="4" flood-opacity="0.1"/>
                    </filter>
                </defs>

                <!-- Background -->
                <rect width="$width" height="$finalHeight" fill="#F2F2F7" rx="0" ry="0"/>

                <!-- Header -->
                <rect x="16" y="16" width="${width - 32}" height="60" rx="16" fill="url(#headerGrad)" filter="url(#iosShadow)"/>
                <text x="${width/2}" y="53" fill="white" font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                      font-size="20" font-weight="600" text-anchor="middle">${calloutData.title}</text>
        """.trimIndent())

            var y = 92
            calloutData.metrics.forEach { (key, value) ->
                append("""
                <rect x="16" y="$y" width="${width - 32}" height="60" fill="white" 
                      stroke="#E5E5EA" stroke-width="1" rx="16" filter="url(#iosShadow)"/>

                <!-- Metric Icon -->
                <circle cx="48" cy="${y + 30}" r="16" fill="#5856D6" filter="url(#iosShadow)"/>
                <text x="48" y="${y + 35}" fill="white" font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                      font-size="14" font-weight="600" text-anchor="middle">📊</text>

                <!-- Metric Label -->
                <text x="80" y="${y + 25}" fill="#1C1C1E" font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                      font-size="16" font-weight="600">$key</text>

                <!-- Metric Value -->
                <text x="80" y="${y + 45}" fill="#5856D6" font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                      font-size="14" font-weight="600">$value</text>
            """.trimIndent())
                y += 70
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
                <svg width="$width" height="$finalHeight" xmlns="http://www.w3.org/2000/svg">
                    <defs>
                        <linearGradient id="headerGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" style="stop-color:#FF9500;stop-opacity:1" />
                            <stop offset="100%" style="stop-color:#FF2D55;stop-opacity:1" />
                        </linearGradient>
                        <filter id="iosShadow">
                            <feDropShadow dx="0" dy="2" stdDeviation="4" flood-opacity="0.1"/>
                        </filter>
                    </defs>

                    <!-- Background -->
                    <rect width="$width" height="$finalHeight" fill="#F2F2F7" rx="0" ry="0"/>

                    <!-- Header -->
                    <rect x="16" y="16" width="${width - 32}" height="60" rx="16" fill="url(#headerGrad)" filter="url(#iosShadow)"/>
                    <text x="${width/2}" y="53" fill="white" font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                          font-size="20" font-weight="600" text-anchor="middle">${calloutData.title}</text>
            """.trimIndent())

            // Draw timeline
            val timelineY = 320
            val timelineLength = width - 64
            val startX = 32
            val endX = startX + timelineLength

            // Timeline line
            append("""
                <!-- Timeline Line -->
                <line x1="$startX" y1="$timelineY" x2="$endX" y2="$timelineY" 
                      stroke="#E5E5EA" stroke-width="4" stroke-linecap="round"/>
            """.trimIndent())

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

                    append("""
                        <!-- Step ${index + 1} -->
                        <line x1="$x" y1="$lineY1" x2="$x" y2="$lineY2" 
                              stroke="#E5E5EA" stroke-width="2" stroke-dasharray="${if (isCompleted) "none" else "4,4"}"/>
                        <circle cx="$x" cy="$timelineY" r="8" fill="$circleColor" filter="url(#iosShadow)"/>
                        <text x="$x" y="$textY" fill="#1C1C1E" font-family="system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro', sans-serif" 
                              font-size="14" font-weight="600" text-anchor="middle">${step.phase}</text>
                    """.trimIndent())

                    // Add card for each step
                    // Ensure cards for odd-indexed steps don't overlap with the header
                    val cardY = if (index % 2 == 0) timelineY + 30 else timelineY - 100
                    if (index % 2 == 1) {
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
