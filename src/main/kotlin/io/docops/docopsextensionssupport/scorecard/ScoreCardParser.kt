package io.docops.docopsextensionssupport.scorecard

class ScoreCardParser {

    /**
     * Parses the input string into a MigrationScoreCard object.
     *
     * @param input The input string in table format
     * @return A MigrationScoreCard object
     */
    fun parse(input: String): MigrationScoreCard {
        val sections = splitIntoSections(input)

        // Parse general configuration
        val config = parseConfig(sections["config"] ?: "")

        // Parse before section
        val beforeSection = parseBeforeSection(
            sections["before"] ?: "",
            sections["before.items"] ?: "",
            sections["before.performance"] ?: ""
        )

        // Parse after section
        val afterSection = parseAfterSection(
            sections["after"] ?: "",
            sections["after.items"] ?: "",
            sections["after.performance"] ?: ""
        )

        // Parse metrics sections
        val metricsCategories = parseMetricsCategories(sections)

        // Parse optimizations
        val optimizations = parseOptimizations(sections["optimizations"] ?: "")

        // Parse summary
        val summary = parseSummary(sections["summary"] ?: "")

        // Create and return the MigrationScoreCard
        return MigrationScoreCard(
            title = config["title"] ?: "Migration ScoreCard",
            subtitle = config["subtitle"] ?: "",
            headerTitle = config["headerTitle"] ?: "",
            beforeSection = beforeSection,
            afterSection = afterSection,
            performanceMetrics = metricsCategories,
            keyOptimizations = optimizations,
            migrationSummary = summary,
            footerText = sections["footer"] ?: "",
            scale = config["scale"]?.toFloatOrNull() ?: 1.0f,
            theme = parseTheme(config)
        )
    }

    /**
     * Splits the input string into sections based on the [section] markers.
     *
     * @param input The input string
     * @return A map of section names to section content
     */
    private fun splitIntoSections(input: String): Map<String, String> {
        val lines = input.lines()
        val sections = mutableMapOf<String, String>()
        var currentSection = "config"
        val sectionContent = StringBuilder()

        for (line in lines) {
            if (line.trim().startsWith("[") && line.trim().endsWith("]")) {
                // Save the previous section
                sections[currentSection] = sectionContent.toString().trim()

                // Start a new section - EXTRACT THE SECTION NAME
                currentSection = line.trim().removeSurrounding("[", "]")
                sectionContent.clear()
            } else if (line.trim() == "---") {
                // Ignore section separators
                continue
            } else {
                sectionContent.append(line).append("\n")
            }
        }

        // Save the last section - THIS IS CRITICAL FOR FOOTER
        if (currentSection.isNotBlank()) {
            val content = sectionContent.toString().trim()
            if (content.isNotBlank()) {
                sections[currentSection] = content
            }
        }


        return sections
    }
    /**
     * Parses the configuration section.
     *
     * @param configSection The configuration section content
     * @return A map of configuration keys to values
     */
    private fun parseConfig(configSection: String): Map<String, String> {
        val config = mutableMapOf<String, String>()

        configSection.lines().forEach { line ->
            if (line.contains("=")) {
                val (key, value) = line.split("=", limit = 2)
                config[key.trim()] = value.trim()
            }
        }

        return config
    }

    /**
     * Parses the before section.
     *
     * @param titleSection The title section content
     * @param itemsSection The items section content
     * @param performanceSection The performance section content
     * @return A BeforeSection object
     */
    private fun parseBeforeSection(
        titleSection: String,
        itemsSection: String,
        performanceSection: String
    ): BeforeSection {
        val titleLine = titleSection.lines().firstOrNull() ?: "Before"
        // Parse the title, removing the "title=" prefix if it exists
        val title = if (titleLine.startsWith("title=")) {
            titleLine.substring("title=".length)
        } else {
            titleLine
        }
        val items = parseInfrastructureItems(itemsSection)
        val performanceBaseline = parsePerformanceMetric(performanceSection)

        return BeforeSection(title, items, performanceBaseline)
    }

    /**
     * Parses the after section.
     *
     * @param titleSection The title section content
     * @param itemsSection The items section content
     * @param performanceSection The performance section content
     * @return An AfterSection object
     */
    private fun parseAfterSection(
        titleSection: String,
        itemsSection: String,
        performanceSection: String
    ): AfterSection {
        val titleLine = titleSection.lines().firstOrNull() ?: "After"
        // Parse the title, removing the "title=" prefix if it exists
        val title = if (titleLine.startsWith("title=")) {
            titleLine.substring("title=".length)
        } else {
            titleLine
        }
        val items = parseInfrastructureItems(itemsSection)
        val performanceImprovement = parsePerformanceMetric(performanceSection)

        return AfterSection(title, items, performanceImprovement)
    }

    /**
     * Parses infrastructure items.
     *
     * @param itemsSection The items section content
     * @return A list of InfrastructureItem objects
     */
    private fun parseInfrastructureItems(itemsSection: String): List<InfrastructureItem> {
        val items = mutableListOf<InfrastructureItem>()

        itemsSection.lines().forEach { line ->
            if (line.isNotBlank()) {
                val parts = line.split("|").map { it.trim() }
                if (parts.size >= 4) {
                    items.add(
                        InfrastructureItem(
                            title = parts[0],
                            description = parts[1],
                            status = parts[2],
                            statusIcon = parts[3]
                        )
                    )
                }
            }
        }

        return items
    }

    /**
     * Parses a performance metric.
     *
     * @param performanceSection The performance section content
     * @return A PerformanceMetric object
     */
    private fun parsePerformanceMetric(performanceSection: String): PerformanceMetric {
        val parts = performanceSection.lines().firstOrNull()?.split("|")?.map { it.trim() }

        return if (parts != null && parts.size >= 3) {
            PerformanceMetric(
                label = parts[0],
                percentage = parts[1].toIntOrNull() ?: 0,
                color = parts[2]
            )
        } else {
            PerformanceMetric("Performance", 0, "#cccccc")
        }
    }

    /**
     * Parses metrics categories.
     *
     * @param sections The map of all sections
     * @return A list of MetricCategory objects
     */
    private fun parseMetricsCategories(sections: Map<String, String>): List<MetricCategory> {
        val categories = mutableListOf<MetricCategory>()

        // Find all metrics sections (not metrics.items)
        val metricsSections = sections.filter { it.key.startsWith("metrics") && !it.key.endsWith(".items") }

        for ((sectionKey, sectionContent) in metricsSections) {
            if (sectionKey == "metrics") {
                // Parse category header: "Test Metrics | #3498db"
                val lines = sectionContent.split("\n").filter { it.isNotBlank() }
                for (line in lines) {
                    val parts = line.split("|").map { it.trim() }
                    if (parts.size >= 2) {
                        val categoryName = parts[0]
                        val categoryColor = parts[1]

                        // Find corresponding metrics.items section
                        val itemsSection = sections["metrics.items"] ?: ""
                        val metrics = parseMetrics(itemsSection)

                        categories.add(MetricCategory(
                            title = categoryName,
                            borderColor = categoryColor,
                            headerColor = categoryColor, // or derive from borderColor
                            metrics = metrics
                        ))
                    }
                }
            }
        }

        return categories
    }


    /**
     * Parses metrics.
     *
     * @param metricsSection The metrics section content
     * @return A list of Metric objects
     */
    private fun parseMetrics(metricsSection: String): List<Metric> {
        val metrics = mutableListOf<Metric>()

        metricsSection.lines().forEach { line ->
            if (line.isNotBlank()) {
                val parts = line.split("|").map { it.trim() }
                if (parts.size >= 2) {
                    metrics.add(
                        Metric(
                            label = parts[0],
                            value = parts[1]
                        )
                    )
                }
            }
        }

        return metrics
    }

    /**
     * Parses optimizations.
     *
     * @param optimizationsSection The optimizations section content
     * @return A list of Optimization objects
     */
    private fun parseOptimizations(optimizationsSection: String): List<Optimization> {
        val optimizations = mutableListOf<Optimization>()


        val lines = optimizationsSection.split("\n").filter { it.isNotBlank() }


        for ((index, line) in lines.withIndex()) {
            val parts = line.split("|").map { it.trim() }

            if (parts.size >= 3) {
                val number = parts[0].toIntOrNull() ?: 0
                val title = parts[1]
                val description = parts[2]
                optimizations.add(Optimization(number, title, description))
            }
        }


        return optimizations
    }
    /**
     * Parses the summary section.
     *
     * @param summarySection The summary section content
     * @return A MigrationSummary object
     */
    private fun parseSummary(summarySection: String): MigrationSummary {
        // Process all lines of the summary section, not just the first one
        val lines = summarySection.lines().filter { it.isNotBlank() }

        // If there are no lines, return a default summary
        if (lines.isEmpty()) {
            return MigrationSummary(0, "Unknown", emptyList())
        }

        // Process the first line to extract the overall improvement and status
        val firstLine = lines[0]
        val parts = firstLine.split("|").map { it.trim() }

        return if (parts.size >= 3) {
            // Extract highlights from all parts starting from index 2
            val highlights = if (parts.size > 2) parts.subList(2, parts.size) else emptyList()

            MigrationSummary(
                overallImprovement = parts[0].toIntOrNull() ?: 0,
                status = parts[1],
                highlights = highlights
            )
        } else {
            MigrationSummary(0, "Unknown", emptyList())
        }
    }
    /**
     * Parses theme settings.
     *
     * @param config The configuration map
     * @return A MigrationScoreCardTheme object
     */
    private fun parseTheme(config: Map<String, String>): MigrationScoreCardTheme {
        return MigrationScoreCardTheme(
            backgroundColor = config["backgroundColor"] ?: "#f8f9fa",
            titleColor = config["titleColor"] ?: "#2c3e50",
            subtitleColor = config["subtitleColor"] ?: "#7f8c8d",
            headerColor = config["headerColor"] ?: "#8e44ad",
            beforeSectionColor = config["beforeSectionColor"] ?: "#e74c3c",
            afterSectionColor = config["afterSectionColor"] ?: "#27ae60"
        )
    }
}
