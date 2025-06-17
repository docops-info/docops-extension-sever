package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.roadmap.wrapText
import kotlin.math.max

class ScoreCardMaker {

    /**
     * Generates an SVG representation of the migration scorecard using iOS-style design.
     *
     * @param scorecard The MigrationScoreCard object
     * @param isPdf Whether the SVG is being generated for PDF output
     * @return The SVG as a string
     */
    fun make(scorecard: MigrationScoreCard, isPdf: Boolean = false): String {
        val svg = StringBuilder()

        // Add SVG header with iOS-style gradients and filters
        svg.append("""
            <svg width="${scorecard.calcWidth()}" height="${scorecard.calcHeight()}" xmlns="http://www.w3.org/2000/svg">
                <!-- iOS-style background with subtle gradient -->
                <defs>
                    <linearGradient id="bgGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" style="stop-color:#f8f9fa;stop-opacity:1" />
                        <stop offset="100%" style="stop-color:#f1f3f4;stop-opacity:1" />
                    </linearGradient>

                    <!-- iOS-style shadows -->
                    <filter id="cardShadow" x="-20%" y="-20%" width="140%" height="140%">
                        <feDropShadow dx="0" dy="4" stdDeviation="12" flood-color="#000" flood-opacity="0.08"/>
                    </filter>

                    <filter id="lightShadow" x="-20%" y="-20%" width="140%" height="140%">
                        <feDropShadow dx="0" dy="2" stdDeviation="6" flood-color="#000" flood-opacity="0.06"/>
                    </filter>

                    <!-- iOS System Blue gradient -->
                    <linearGradient id="blueGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" style="stop-color:#007AFF;stop-opacity:1" />
                        <stop offset="100%" style="stop-color:#5AC8FA;stop-opacity:1" />
                    </linearGradient>

                    <!-- iOS System Green gradient -->
                    <linearGradient id="greenGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" style="stop-color:#34C759;stop-opacity:1" />
                        <stop offset="100%" style="stop-color:#30D158;stop-opacity:1" />
                    </linearGradient>

                    <!-- iOS System Red -->
                    <linearGradient id="redGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" style="stop-color:#FF3B30;stop-opacity:1" />
                        <stop offset="100%" style="stop-color:#FF453A;stop-opacity:1" />
                    </linearGradient>

                    <!-- iOS System Orange -->
                    <linearGradient id="orangeGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" style="stop-color:#FF9500;stop-opacity:1" />
                        <stop offset="100%" style="stop-color:#FF9F0A;stop-opacity:1" />
                    </linearGradient>
                </defs>

                <rect width="${scorecard.calcWidth()}" height="${scorecard.calcHeight()}" fill="url(#bgGradient)"/>
        """.trimIndent())

        // Add title and subtitle
        svg.append(generateTitle(scorecard))

        // Add before section
        svg.append(generateBeforeSection(scorecard))

        // Add migration arrow
        svg.append(generateMigrationArrow())

        // Add after section
        svg.append(generateAfterSection(scorecard))

        // Add key improvements summary
        svg.append(generateKeyImprovements(scorecard))

        // Add team avatars
        svg.append(generateTeamAvatars(scorecard))

        // Close SVG
        svg.append("</svg>")

        return svg.toString()
    }

    /**
     * Generates the iOS-style title and subtitle section of the SVG.
     *
     * @param scorecard The MigrationScoreCard object
     * @return The SVG fragment as a string
     */
    private fun generateTitle(scorecard: MigrationScoreCard): String {
        return """
            <!-- iOS-style title -->
            <text x="${scorecard.calcWidth() / 2}" y="45" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Display', system-ui, sans-serif"
                  font-size="24" font-weight="600" fill="#1D1D1F">
                ${(scorecard.title.escapeXml())}
            </text>

            <text x="${scorecard.calcWidth() / 2}" y="65" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Text', system-ui, sans-serif"
                  font-size="17" font-weight="400" fill="#86868B">
                ${(scorecard.subtitle.escapeXml())}
            </text>
        """.trimIndent()
    }

    /**
     * Generates the header section of the SVG.
     *
     * @param scorecard The MigrationScoreCard object
     * @return The SVG fragment as a string
     */
    private fun generateHeader(scorecard: MigrationScoreCard): String {
        return """
            <!-- Header -->
            <rect x="50" y="100" width="1300" height="50" rx="5" fill="${scorecard.theme.headerColor}"/>
            <text x="700" y="135" font-family="Arial, sans-serif" font-size="20" font-weight="bold" text-anchor="middle" fill="white">${(scorecard.headerTitle.escapeXml())}</text>
        """.trimIndent()
    }

    /**
     * Generates the iOS-style before section of the SVG.
     *
     * @param scorecard The MigrationScoreCard object
     * @return The SVG fragment as a string
     */
    private fun generateBeforeSection(scorecard: MigrationScoreCard): String {
        val beforeSection = scorecard.beforeSection
        val svg = StringBuilder()

        // Before Card - iOS style with shadow
        svg.append("""
            <!-- Before Card -->
            <g>
                <rect x="40" y="100" width="300" height="380" fill="white" stroke="none" rx="20" filter="url(#cardShadow)"/>

                <!-- Before Header -->
                <rect x="60" y="120" width="260" height="44" fill="url(#redGradient)" rx="12"/>
                <text x="190" y="147" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Text', system-ui, sans-serif"
                      font-size="16" font-weight="600" fill="white">
                    ${(beforeSection.title.escapeXml())}
                </text>
        """.trimIndent())

        // Legacy issues
        svg.append("""
                <!-- Legacy issues -->
                <g transform="translate(80, 190)">
        """.trimIndent())

        // Add items with iOS-style
        var currentY = 0
        beforeSection.items.forEach { item ->
            val statusColor = when (item.status) {
                "critical" -> "url(#redGradient)"
                "warning" -> "url(#orangeGradient)"
                "good" -> "url(#greenGradient)"
                else -> "#cccccc"
            }

            val emoji = when (item.statusIcon) {
                "!" -> "⚠️"
                "$" -> "💰"
                "✓" -> "✅"
                else -> item.statusIcon
            }

            // Wrap title and description text
            val titleLines = wrapText(item.title, 20f)
            val descLines = wrapText(item.description, 25f)

            // Calculate y position for this item
            val y = currentY

            // Add circle and emoji
            svg.append("""
                    <!-- ${item.title} -->
                    <circle cx="12" cy="${y + 12}" r="8" fill="${statusColor}" opacity="0.15"/>
                    <text x="12" y="${y + 17}" text-anchor="middle" font-size="10" fill="${statusColor}">${emoji}</text>
            """.trimIndent())

            // Add title with multiple lines if needed
            svg.append("""
                    <text x="30" y="${y + 12}" font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Text', system-ui, sans-serif"
                          font-size="15" font-weight="500" fill="#1D1D1F">
            """.trimIndent())

            titleLines.forEachIndexed { i, line ->
                val lineY = if (i == 0) 0 else 18
                svg.append("""
                        <tspan x="30" dy="${lineY}">${line.trim()}</tspan>
                """.trimIndent())
            }

            svg.append("</text>")

            // Calculate y position for description based on number of title lines
            val descY = y + 12 + (titleLines.size * 18)

            // Add description with multiple lines if needed
            svg.append("""
                    <text x="30" y="${descY}" font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Text', system-ui, sans-serif"
                          font-size="13" font-weight="400" fill="#86868B">
            """.trimIndent())

            descLines.forEachIndexed { i, line ->
                val lineY = if (i == 0) 0 else 16
                svg.append("""
                        <tspan x="30" dy="${lineY}">${line.trim()}</tspan>
                """.trimIndent())
            }

            svg.append("</text>")

            // Update currentY for next item
            currentY = y + 30 + (titleLines.size * 18) + (descLines.size * 16)
        }

        svg.append("""
                </g>
        """.trimIndent())

        // Legacy score indicator
        val baseline = beforeSection.performanceBaseline
        svg.append("""
                <!-- Legacy score indicator -->
                <text x="190" y="450" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Text', system-ui, sans-serif"
                      font-size="12" font-weight="600" fill="#86868B">${(baseline.label.escapeXml())}</text>
            </g>
        """.trimIndent())

        return svg.toString()
    }

    /**
     * Generates the iOS-style upgrade arrow between the before and after sections.
     *
     * @return The SVG fragment as a string
     */
    private fun generateMigrationArrow(): String {
        return """
            <!-- iOS-style upgrade arrow -->
            <g transform="translate(400, 290)">
                <circle cx="0" cy="0" r="30" fill="url(#blueGradient)" filter="url(#lightShadow)"/>
                <path d="M-10,0 L10,0 M5,-6 L10,0 L5,6" stroke="white" stroke-width="3" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
                <text x="0" y="-45" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Text', system-ui, sans-serif"
                      font-size="14" font-weight="500" fill="#007AFF">UPGRADE</text>
            </g>
        """.trimIndent()
    }

    /**
     * Generates the iOS-style after section of the SVG.
     *
     * @param scorecard The MigrationScoreCard object
     * @return The SVG fragment as a string
     */
    private fun generateAfterSection(scorecard: MigrationScoreCard): String {
        val afterSection = scorecard.afterSection
        val svg = StringBuilder()

        // After Card - iOS style with shadow
        svg.append("""
            <!-- After Card -->
            <g>
                <rect x="460" y="100" width="300" height="380" fill="white" stroke="none" rx="20" filter="url(#cardShadow)"/>

                <!-- After Header -->
                <rect x="480" y="120" width="260" height="44" fill="url(#greenGradient)" rx="12"/>
                <text x="610" y="147" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Text', system-ui, sans-serif"
                      font-size="16" font-weight="600" fill="white">
                    ${(afterSection.title.escapeXml())}
                </text>
        """.trimIndent())

        // Modern improvements
        svg.append("""
                <!-- Modern improvements -->
                <g transform="translate(500, 190)">
        """.trimIndent())

        // Add items with iOS-style
        var currentY = 0
        afterSection.items.forEach { item ->
            val statusColor = when (item.status) {
                "critical" -> "url(#redGradient)"
                "warning" -> "url(#orangeGradient)"
                "good" -> "url(#greenGradient)"
                else -> "#cccccc"
            }

            val emoji = when (item.statusIcon) {
                "!" -> "⚠️"
                "$" -> "💰"
                "✓" -> "✅"
                else -> item.statusIcon
            }

            // Wrap title and description text
            val titleLines = wrapText(item.title, 20f)
            val descLines = wrapText(item.description, 25f)

            // Calculate y position for this item
            val y = currentY

            // Add circle and emoji
            svg.append("""
                    <!-- ${item.title} -->
                    <circle cx="12" cy="${y + 12}" r="8" fill="${statusColor}" opacity="0.15"/>
                    <text x="12" y="${y + 17}" text-anchor="middle" font-size="10" fill="${statusColor}">${emoji}</text>
            """.trimIndent())

            // Add title with multiple lines if needed
            svg.append("""
                    <text x="30" y="${y + 12}" font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Text', system-ui, sans-serif"
                          font-size="15" font-weight="500" fill="#1D1D1F">
            """.trimIndent())

            titleLines.forEachIndexed { i, line ->
                val lineY = if (i == 0) 0 else 18
                svg.append("""
                        <tspan x="30" dy="${lineY}">${line.trim()}</tspan>
                """.trimIndent())
            }

            svg.append("</text>")

            // Calculate y position for description based on number of title lines
            val descY = y + 12 + (titleLines.size * 18)

            // Add description with multiple lines if needed
            svg.append("""
                    <text x="30" y="${descY}" font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Text', system-ui, sans-serif"
                          font-size="13" font-weight="400" fill="#86868B">
            """.trimIndent())

            descLines.forEachIndexed { i, line ->
                val lineY = if (i == 0) 0 else 16
                svg.append("""
                        <tspan x="30" dy="${lineY}">${line.trim()}</tspan>
                """.trimIndent())
            }

            svg.append("</text>")

            // Update currentY for next item
            currentY = y + 30 + (titleLines.size * 18) + (descLines.size * 16)
        }

        svg.append("""
                </g>
        """.trimIndent())

        // Modern score indicator
        val improvement = afterSection.performanceImprovement
        svg.append("""
                <!-- Modern score indicator -->
                <text x="610" y="450" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Text', system-ui, sans-serif"
                      font-size="12" font-weight="600" fill="#86868B">${(improvement.label.escapeXml())}</text>
            </g>
        """.trimIndent())

        return svg.toString()
    }



    /**
     * Generates the iOS-style key improvements summary section.
     *
     * @param scorecard The MigrationScoreCard object
     * @return The SVG fragment as a string
     */
    private fun generateKeyImprovements(scorecard: MigrationScoreCard): String {
        return """
            <!-- Key improvements summary -->
            <g transform="translate(200, 500)">
                <rect x="0" y="0" width="400" height="70" fill="white" stroke="none" rx="12" filter="url(#lightShadow)"/>
                <text x="200" y="25" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Text', system-ui, sans-serif"
                      font-size="13" font-weight="500" fill="#1D1D1F">Key Improvements Delivered</text>
                <text x="200" y="45" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Text', system-ui, sans-serif"
                      font-size="11" font-weight="400" fill="#86868B">Significant Performance Improvement</text>
            </g>
        """.trimIndent()
    }

    /**
     * Generates the iOS-style team avatars section.
     *
     * @param scorecard The MigrationScoreCard object
     * @return The SVG fragment as a string
     */
    private fun generateTeamAvatars(scorecard: MigrationScoreCard): String {
        val svg = StringBuilder()

        svg.append("""
            <!-- iOS-style team avatars with initials -->
            <g transform="translate(260, 565)">
        """.trimIndent())

        // Calculate the number of team members to display (max 4)
        val teamMembers = scorecard.teamMembers.take(4)

        // Generate avatars for each team member
        teamMembers.forEachIndexed { index, member ->
            val x = 40 + (index * 60) // 60 pixels between avatars

            svg.append("""
                <!-- Team member ${index + 1} -->
                <circle cx="${x}" cy="25" r="18" fill="${member.color}" filter="url(#lightShadow)"/>
                <text x="${x}" y="31" text-anchor="middle" font-size="16">${member.emoji}</text>
                <text x="${x}" y="52" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'SF Pro Text', system-ui, sans-serif"
                      font-size="12" font-weight="500" fill="#86868B">${member.initials}</text>
            """.trimIndent())

            // Add a newline between team members for better readability in the SVG
            if (index < teamMembers.size - 1) {
                svg.append("\n\n")
            }
        }

        svg.append("\n            </g>")

        return svg.toString()
    }
}
