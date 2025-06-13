package io.docops.docopsextensionssupport.swimlane

import io.docops.docopsextensionssupport.svgsupport.itemTextWidth

/**
 * Maker class for generating swimlane SVG diagrams
 */
class SwimLaneMaker {

    /**
     * Generate SVG for the swimlane diagram
     */
    fun generateSvg(data: SwimLaneData): String {
        val sb = StringBuilder()

        // Calculate dimensions
        val width = data.width
        val height = data.height
        val laneCount = data.lanes.size

        // Calculate card width and spacing
        val spacing = 20 // Space between cards
        val totalSpacing = (laneCount - 1) * spacing
        val totalCardWidth = width - 60 - totalSpacing // 30px padding on each side
        val laneWidth = if (laneCount > 0) totalCardWidth / laneCount else totalCardWidth

        // Start SVG
        sb.append("""
            <svg width="${width}" height="${height}" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
                <defs>
                    <style>
                    <![CDATA[
                        /* Light theme (default) */
                        :root {
                            --bg-color: #f2f2f7;
                            --card-bg-start: #ffffff;
                            --card-bg-end: #fafafa;
                            --card-border: #e5e5e5;
                            --header-bg-start: #f8f8f8;
                            --header-bg-end: #f0f0f0;
                            --header-separator: #e5e5e5;
                            --text-primary: #1d1d1f;
                            --text-secondary: #333333;
                            --shadow-color: rgba(0, 0, 0, 0.08);
                        }

                        /* Dark theme */
                        ${if (data.useDarkTheme) """
                        :root {
                            --bg-color: #000000;
                            --card-bg-start: #1c1c1e;
                            --card-bg-end: #2c2c2e;
                            --card-border: #38383a;
                            --header-bg-start: #2c2c2e;
                            --header-bg-end: #3a3a3c;
                            --header-separator: #48484a;
                            --text-primary: #ffffff;
                            --text-secondary: #f2f2f7;
                            --shadow-color: rgba(0, 0, 0, 0.3);
                        }
                        """ else ""}

                        .background {
                            fill: var(--bg-color);
                        }

                        .card-bg {
                            fill: var(--card-bg-start);
                            stroke: var(--card-border);
                        }

                        .header-bg {
                            fill: var(--header-bg-start);
                        }

                        .separator-line {
                            stroke: var(--header-separator);
                        }

                        .header-text {
                            fill: var(--text-primary);
                            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif;
                            font-size: 18px;
                            font-weight: 600;
                        }

                        .body-text {
                            fill: var(--text-secondary);
                            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif;
                            font-size: 15px;
                        }
                    ]]>
                    </style>

                    <!-- Light theme gradients -->
                    <linearGradient id="cardGradientLight" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" style="stop-color:#ffffff;stop-opacity:1" />
                        <stop offset="100%" style="stop-color:#fafafa;stop-opacity:1" />
                    </linearGradient>

                    <linearGradient id="headerGradientLight" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" style="stop-color:#f8f8f8;stop-opacity:1" />
                        <stop offset="100%" style="stop-color:#f0f0f0;stop-opacity:1" />
                    </linearGradient>

                    <!-- Dark theme gradients -->
                    <linearGradient id="cardGradientDark" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" style="stop-color:#1c1c1e;stop-opacity:1" />
                        <stop offset="100%" style="stop-color:#2c2c2e;stop-opacity:1" />
                    </linearGradient>

                    <linearGradient id="headerGradientDark" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" style="stop-color:#2c2c2e;stop-opacity:1" />
                        <stop offset="100%" style="stop-color:#3a3a3c;stop-opacity:1" />
                    </linearGradient>

                    <!-- Light theme shadow -->
                    <filter id="cardShadowLight" x="-20%" y="-20%" width="140%" height="140%">
                        <feDropShadow dx="0" dy="4" stdDeviation="8" flood-color="#000000" flood-opacity="0.08"/>
                    </filter>

                    <!-- Dark theme shadow -->
                    <filter id="cardShadowDark" x="-20%" y="-20%" width="140%" height="140%">
                        <feDropShadow dx="0" dy="4" stdDeviation="8" flood-color="#000000" flood-opacity="0.3"/>
                    </filter>
                </defs>

                <!-- Background -->
                <rect width="100%" height="100%" class="background"/>
        """.trimIndent())

        // Draw each swimlane
        data.lanes.forEachIndexed { index, lane ->
            // Calculate x position with even spacing
            val x = 30 + (index * (laneWidth + spacing))

            sb.append("""
                <!-- Swim Lane ${index + 1}: ${lane.title} -->
                <g class="swimlane-card">
                    <!-- Card background -->
                    <rect x="$x" y="30" width="$laneWidth" height="${height - 60}" rx="16" ry="16"
                          fill="url(#${if (data.useDarkTheme) "cardGradientDark" else "cardGradientLight"})" 
                          stroke="${if (data.useDarkTheme) "#38383a" else "#e5e5e5"}" 
                          stroke-width="1" 
                          class="card-bg"
                          filter="url(#${if (data.useDarkTheme) "cardShadowDark" else "cardShadowLight"})"/>

                    <!-- Header background -->
                    <rect x="$x" y="30" width="$laneWidth" height="50" rx="16" ry="16"
                          fill="url(#${if (data.useDarkTheme) "headerGradientDark" else "headerGradientLight"})" class="header-bg"/>
                    <rect x="$x" y="65" width="$laneWidth" height="15"
                          fill="url(#${if (data.useDarkTheme) "headerGradientDark" else "headerGradientLight"})" class="header-bg"/>

                    <!-- Header separator line -->
                    <line x1="$x" y1="80" x2="${x + laneWidth}" y2="80" 
                          stroke="${if (data.useDarkTheme) "#48484a" else "#e5e5e5"}" 
                          stroke-width="1" class="separator-line"/>

                    <!-- Header label -->
                    <text x="${x + 20}" y="60" class="header-text">${lane.title}</text>

                    <!-- Body text -->
                    <text x="${x + 20}" y="110" class="body-text">
            """.trimIndent())

            // Add items for this lane
            var yOffset = 0
            lane.items.forEach { item ->
                // Wrap the title if needed
                val availableWidth = laneWidth - 40 // 20px padding on each side
                val wrappedTitle = itemTextWidth(item.title, availableWidth.toFloat())

                // Add the title
                wrappedTitle.forEachIndexed { i, line ->
                    sb.append("""
                        <tspan x="${x + 20}" dy="${if (i == 0 && yOffset == 0) "0" else if (i == 0) "35" else "20"}">${line}</tspan>
                    """.trimIndent())

                    yOffset += 20
                }

                // Add content for this item
                item.content.forEach { contentLine ->
                    // Wrap the content line if needed
                    val wrappedContent = itemTextWidth("• $contentLine", availableWidth.toFloat())

                    wrappedContent.forEachIndexed { i, line ->
                        val indentedLine = if (i == 0) line else "  $line" // Indent continuation lines
                        sb.append("""
                            <tspan x="${x + 20}" dy="25">${indentedLine}</tspan>
                        """.trimIndent())

                        yOffset += 25
                    }
                }
            }

            sb.append("""
                    </text>
                </g>
            """.trimIndent())
        }

        // Add dynamic theme application via JavaScript
        sb.append("""
            <script type="text/javascript">
            <![CDATA[
                function applyTheme() {
                    const isDark = ${if (data.useDarkTheme) "true" else "window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches"};
                    const cards = document.querySelectorAll('.card-bg');
                    const headers = document.querySelectorAll('.header-bg');
                    const lines = document.querySelectorAll('.separator-line');
                    const cardGroups = document.querySelectorAll('.swimlane-card');

                    if (isDark) {
                        cards.forEach(card => {
                            card.setAttribute('fill', 'url(#cardGradientDark)');
                            card.setAttribute('stroke', '#38383a');
                        });
                        headers.forEach(header => {
                            header.setAttribute('fill', 'url(#headerGradientDark)');
                        });
                        lines.forEach(line => {
                            line.setAttribute('stroke', '#48484a');
                        });
                        cardGroups.forEach(group => {
                            group.setAttribute('filter', 'url(#cardShadowDark)');
                        });
                    } else {
                        cards.forEach(card => {
                            card.setAttribute('fill', 'url(#cardGradientLight)');
                            card.setAttribute('stroke', '#e5e5e5');
                        });
                        headers.forEach(header => {
                            header.setAttribute('fill', 'url(#headerGradientLight)');
                        });
                        lines.forEach(line => {
                            line.setAttribute('stroke', '#e5e5e5');
                        });
                        cardGroups.forEach(group => {
                            group.setAttribute('filter', 'url(#cardShadowLight)');
                        });
                    }
                }

                applyTheme();

                if (window.matchMedia) {
                    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', applyTheme);
                }
            ]]>
            </script>
        """.trimIndent())

        // Close SVG
        sb.append("</svg>")

        return sb.toString()
    }

    /**
     * Create default swimlane data for testing or when parsing fails
     */
    fun createDefaultSwimLaneData(): SwimLaneData {
        return SwimLaneData(
            title = "Project Workflow",
            lanes = listOf(
                SwimLane(
                    title = "To Do",
                    items = listOf(
                        SwimLaneItem(
                            title = "Tasks planned for the current development cycle:",
                            content = listOf(
                                "Design new dashboard widgets",
                                "Implement two-factor auth",
                                "Refactor the caching layer",
                                "Write API documentation",
                                "Update user interface"
                            )
                        )
                    )
                ),
                SwimLane(
                    title = "In Progress",
                    items = listOf(
                        SwimLaneItem(
                            title = "Currently under development:",
                            content = listOf(
                                "Backend services for dashboard"
                            )
                        ),
                        SwimLaneItem(
                            title = "Waiting for UI mockups to proceed with front-end implementation.",
                            content = listOf(
                                "Expected completion by end of sprint."
                            )
                        )
                    )
                ),
                SwimLane(
                    title = "Done",
                    items = listOf(
                        SwimLaneItem(
                            title = "Tasks completed, tested, and merged into main branch:",
                            content = listOf(
                                "Fixed authentication bug",
                                "Updated third-party libraries",
                                "Optimized database queries",
                                "Enhanced error handling"
                            )
                        ),
                        SwimLaneItem(
                            title = "All items have been deployed to production successfully.",
                            content = emptyList()
                        )
                    )
                )
            )
        )
    }
}