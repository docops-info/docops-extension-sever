package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.escapeXml

class ScoreCardMaker {

    /**
     * Generates an SVG representation of the migration scorecard.
     *
     * @param scorecard The MigrationScoreCard object
     * @param isPdf Whether the SVG is being generated for PDF output
     * @return The SVG as a string
     */
    fun make(scorecard: MigrationScoreCard, isPdf: Boolean = false): String {
        val svg = StringBuilder()
        
        // Add SVG header
        svg.append("""
            <svg width="${scorecard.calcWidth()}" height="${scorecard.calcHeight()}" viewBox="0 0 1400 1100" xmlns="http://www.w3.org/2000/svg">
                <!-- Background -->
                <rect width="1400" height="1100" fill="${scorecard.theme.backgroundColor}"/>
        """.trimIndent())
        
        // Add title and subtitle
        svg.append(generateTitle(scorecard))
        
        // Add header
        svg.append(generateHeader(scorecard))
        
        // Add before section
        svg.append(generateBeforeSection(scorecard))
        
        // Add migration arrow
        svg.append(generateMigrationArrow())
        
        // Add after section
        svg.append(generateAfterSection(scorecard))
        
        // Add performance metrics section
        svg.append(generatePerformanceMetricsSection(scorecard))
        
        // Add metrics grid
        svg.append(generateMetricsGrid(scorecard))
        
        // Add key optimizations
        svg.append(generateKeyOptimizations(scorecard))
        
        // Add migration summary
        svg.append(generateMigrationSummary(scorecard))
        
        // Add footer
        svg.append(generateFooter(scorecard))
        
        // Close SVG
        svg.append("</svg>")
        
        return svg.toString()
    }
    
    /**
     * Generates the title and subtitle section of the SVG.
     *
     * @param scorecard The MigrationScoreCard object
     * @return The SVG fragment as a string
     */
    private fun generateTitle(scorecard: MigrationScoreCard): String {
        return """
            <!-- Title and Subtitle -->
            <text x="700" y="50" font-family="Arial, sans-serif" font-size="28" font-weight="bold" text-anchor="middle" fill="${scorecard.theme.titleColor}">${(scorecard.title.escapeXml())}</text>
            <text x="700" y="80" font-family="Arial, sans-serif" font-size="18" text-anchor="middle" fill="${scorecard.theme.subtitleColor}">${(scorecard.subtitle.escapeXml())}</text>
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
     * Generates the before section of the SVG.
     *
     * @param scorecard The MigrationScoreCard object
     * @return The SVG fragment as a string
     */
    private fun generateBeforeSection(scorecard: MigrationScoreCard): String {
        val beforeSection = scorecard.beforeSection
        val svg = StringBuilder()
        
        // Before section header
        svg.append("""
            <!-- Before Section -->
            <rect x="50" y="170" width="500" height="40" rx="5" fill="${scorecard.theme.beforeSectionColor}"/>
            <text x="300" y="197" font-family="Arial, sans-serif" font-size="18" font-weight="bold" text-anchor="middle" fill="white">${(beforeSection.title.escapeXml())}</text>
        """.trimIndent())
        
        // Before section items
        svg.append("""
            <rect x="50" y="210" width="500" height="${60 + beforeSection.items.size * 60}" rx="5" fill="white" stroke="#ddd" stroke-width="1"/>
        """.trimIndent())
        
        // Add items
        beforeSection.items.forEachIndexed { index, item ->
            val y = 240 + index * 60
            val statusColor = when (item.status) {
                "critical" -> "#e74c3c"
                "warning" -> "#f39c12"
                "good" -> "#2ecc71"
                else -> "#cccccc"
            }
            
            svg.append("""
                <text x="70" y="${y}" font-family="Arial, sans-serif" font-size="16" font-weight="bold" fill="#333">${(item.title.escapeXml())}</text>
                <text x="70" y="${y + 20}" font-family="Arial, sans-serif" font-size="14" fill="#666">${(item.description.escapeXml())}</text>
                <circle cx="520" cy="${y - 5}" r="15" fill="${statusColor}"/>
                <text x="520" y="${y}" font-family="Arial, sans-serif" font-size="14" font-weight="bold" text-anchor="middle" fill="white">${(item.statusIcon.escapeXml())}</text>
            """.trimIndent())
        }
        
        // Performance baseline
        val baseline = beforeSection.performanceBaseline
        svg.append("""
            <rect x="50" y="${210 + 60 + beforeSection.items.size * 60}" width="500" height="60" rx="5" fill="${baseline.color}" opacity="0.9"/>
            <text x="70" y="${240 + 60 + beforeSection.items.size * 60}" font-family="Arial, sans-serif" font-size="16" font-weight="bold" fill="white">${(baseline.label.escapeXml())}</text>
            <text x="500" y="${240 + 60 + beforeSection.items.size * 60}" font-family="Arial, sans-serif" font-size="24" font-weight="bold" text-anchor="end" fill="white">${baseline.percentage}%</text>
        """.trimIndent())
        
        return svg.toString()
    }
    
    /**
     * Generates the migration arrow between the before and after sections.
     *
     * @return The SVG fragment as a string
     */
    private fun generateMigrationArrow(): String {
        return """
            <!-- Migration Arrow -->
            <path d="M 570 300 L 830 300" stroke="#666" stroke-width="3" stroke-dasharray="10,5"/>
            <polygon points="830,300 820,295 820,305" fill="#666"/>
        """.trimIndent()
    }
    
    /**
     * Generates the after section of the SVG.
     *
     * @param scorecard The MigrationScoreCard object
     * @return The SVG fragment as a string
     */
    private fun generateAfterSection(scorecard: MigrationScoreCard): String {
        val afterSection = scorecard.afterSection
        val svg = StringBuilder()
        
        // After section header
        svg.append("""
            <!-- After Section -->
            <rect x="850" y="170" width="500" height="40" rx="5" fill="${scorecard.theme.afterSectionColor}"/>
            <text x="1100" y="197" font-family="Arial, sans-serif" font-size="18" font-weight="bold" text-anchor="middle" fill="white">${(afterSection.title.escapeXml())}</text>
        """.trimIndent())
        
        // After section items
        svg.append("""
            <rect x="850" y="210" width="500" height="${60 + afterSection.items.size * 60}" rx="5" fill="white" stroke="#ddd" stroke-width="1"/>
        """.trimIndent())
        
        // Add items
        afterSection.items.forEachIndexed { index, item ->
            val y = 240 + index * 60
            val statusColor = when (item.status) {
                "critical" -> "#e74c3c"
                "warning" -> "#f39c12"
                "good" -> "#2ecc71"
                else -> "#cccccc"
            }
            
            svg.append("""
                <text x="870" y="${y}" font-family="Arial, sans-serif" font-size="16" font-weight="bold" fill="#333">${(item.title.escapeXml())}</text>
                <text x="870" y="${y + 20}" font-family="Arial, sans-serif" font-size="14" fill="#666">${(item.description.escapeXml())}</text>
                <circle cx="1320" cy="${y - 5}" r="15" fill="${statusColor}"/>
                <text x="1320" y="${y}" font-family="Arial, sans-serif" font-size="14" font-weight="bold" text-anchor="middle" fill="white">${(item.statusIcon.escapeXml())}</text>
            """.trimIndent())
        }
        
        // Performance improvement
        val improvement = afterSection.performanceImprovement
        svg.append("""
            <rect x="850" y="${210 + 60 + afterSection.items.size * 60}" width="500" height="60" rx="5" fill="${improvement.color}" opacity="0.9"/>
            <text x="870" y="${240 + 60 + afterSection.items.size * 60}" font-family="Arial, sans-serif" font-size="16" font-weight="bold" fill="white">${(improvement.label.escapeXml())}</text>
            <text x="1300" y="${240 + 60 + afterSection.items.size * 60}" font-family="Arial, sans-serif" font-size="24" font-weight="bold" text-anchor="end" fill="white">${improvement.percentage}%</text>
        """.trimIndent())
        
        return svg.toString()
    }
    
    /**
     * Generates the performance metrics section of the SVG.
     *
     * @param scorecard The MigrationScoreCard object
     * @return The SVG fragment as a string
     */
    private fun generatePerformanceMetricsSection(scorecard: MigrationScoreCard): String {
        return """
            <!-- Performance Metrics Header -->
            <rect x="50" y="500" width="1300" height="40" rx="5" fill="#34495e"/>
            <text x="700" y="527" font-family="Arial, sans-serif" font-size="18" font-weight="bold" text-anchor="middle" fill="white">Performance Metrics &amp; Improvements</text>
        """.trimIndent()
    }
    
    /**
     * Generates the metrics grid section of the SVG.
     *
     * @param scorecard The MigrationScoreCard object
     * @return The SVG fragment as a string
     */
    private fun generateMetricsGrid(scorecard: MigrationScoreCard): String {
        val svg = StringBuilder()
        
        // Calculate grid layout
        val numCategories = scorecard.performanceMetrics.size
        val categoriesPerRow = 2
        val numRows = (numCategories + categoriesPerRow - 1) / categoriesPerRow
        
        // Add metrics grid
        svg.append("<!-- Metrics Grid -->")
        
        scorecard.performanceMetrics.forEachIndexed { index, category ->
            val row = index / categoriesPerRow
            val col = index % categoriesPerRow
            
            val x = 50 + col * 650
            val y = 550 + row * 180
            
            // Category box
            svg.append("""
                <rect x="${x}" y="${y}" width="630" height="170" rx="5" fill="white" stroke="${category.borderColor}" stroke-width="2"/>
                <rect x="${x}" y="${y}" width="630" height="40" rx="5" fill="${category.headerColor}"/>
                <text x="${x + 315}" y="${y + 27}" font-family="Arial, sans-serif" font-size="16" font-weight="bold" text-anchor="middle" fill="white">${(category.title.escapeXml())}</text>
            """.trimIndent())
            
            // Metrics
            category.metrics.forEachIndexed { metricIndex, metric ->
                val metricX = x + 20 + (metricIndex % 3) * 200
                val metricY = y + 70 + (metricIndex / 3) * 40
                
                svg.append("""
                    <text x="${metricX}" y="${metricY}" font-family="Arial, sans-serif" font-size="14" fill="#333">${(metric.label.escapeXml())}</text>
                    <text x="${metricX}" y="${metricY + 20}" font-family="Arial, sans-serif" font-size="16" font-weight="bold" fill="#333">${(metric.value.escapeXml())}</text>
                """.trimIndent())
            }
        }
        
        return svg.toString()
    }
    
    /**
     * Generates the key optimizations section of the SVG.
     *
     * @param scorecard The MigrationScoreCard object
     * @return The SVG fragment as a string
     */
    private fun generateKeyOptimizations(scorecard: MigrationScoreCard): String {
        val svg = StringBuilder()
        
        // Key optimizations header
        svg.append("""
            <!-- Key Optimizations -->
            <rect x="50" y="${550 + (scorecard.performanceMetrics.size + 1) / 2 * 180}" width="1300" height="40" rx="5" fill="#34495e"/>
            <text x="700" y="${577 + (scorecard.performanceMetrics.size + 1) / 2 * 180}" font-family="Arial, sans-serif" font-size="18" font-weight="bold" text-anchor="middle" fill="white">Key Optimizations</text>
        """.trimIndent())
        
        // Key optimizations content
        svg.append("""
            <rect x="50" y="${590 + (scorecard.performanceMetrics.size + 1) / 2 * 180}" width="1300" height="${scorecard.keyOptimizations.size * 60 + 20}" rx="5" fill="white" stroke="#ddd" stroke-width="1"/>
        """.trimIndent())
        
        // Add optimizations
        scorecard.keyOptimizations.forEachIndexed { index, optimization ->
            val y = 620 + (scorecard.performanceMetrics.size + 1) / 2 * 180 + index * 60
            
            svg.append("""
                <circle cx="80" cy="${y}" r="20" fill="#3498db"/>
                <text x="80" y="${y + 5}" font-family="Arial, sans-serif" font-size="16" font-weight="bold" text-anchor="middle" fill="white">${optimization.number}</text>
                <text x="120" y="${y}" font-family="Arial, sans-serif" font-size="16" font-weight="bold" fill="#333">${(optimization.title.escapeXml())}</text>
                <text x="120" y="${y + 25}" font-family="Arial, sans-serif" font-size="14" fill="#666">${(optimization.description.escapeXml())}</text>
            """.trimIndent())
        }
        
        return svg.toString()
    }
    
    /**
     * Generates the migration summary section of the SVG.
     *
     * @param scorecard The MigrationScoreCard object
     * @return The SVG fragment as a string
     */
    private fun generateMigrationSummary(scorecard: MigrationScoreCard): String {
        val summary = scorecard.migrationSummary
        val summaryY = 610 + (scorecard.performanceMetrics.size + 1) / 2 * 180 + scorecard.keyOptimizations.size * 60
        
        val statusColor = when (summary.status.uppercase()) {
            "EXCEPTIONAL" -> "#27ae60"
            "GOOD" -> "#2ecc71"
            "SATISFACTORY" -> "#f39c12"
            "NEEDS IMPROVEMENT" -> "#e74c3c"
            else -> "#3498db"
        }
        
        val svg = StringBuilder()
        
        // Summary header
        svg.append("""
            <!-- Migration Summary -->
            <rect x="50" y="${summaryY}" width="1300" height="40" rx="5" fill="#34495e"/>
            <text x="700" y="${summaryY + 27}" font-family="Arial, sans-serif" font-size="18" font-weight="bold" text-anchor="middle" fill="white">Migration Summary</text>
        """.trimIndent())
        
        // Summary content
        svg.append("""
            <rect x="50" y="${summaryY + 40}" width="1300" height="100" rx="5" fill="white" stroke="#ddd" stroke-width="1"/>
        """.trimIndent())
        
        // Overall improvement circle
        svg.append("""
            <circle cx="150" cy="${summaryY + 90}" r="50" fill="${statusColor}"/>
            <text x="150" y="${summaryY + 90}" font-family="Arial, sans-serif" font-size="24" font-weight="bold" text-anchor="middle" fill="white">${summary.overallImprovement}%</text>
            <text x="150" y="${summaryY + 110}" font-family="Arial, sans-serif" font-size="12" text-anchor="middle" fill="white">Improvement</text>
        """.trimIndent())
        
        // Status
        svg.append("""
            <text x="250" y="${summaryY + 70}" font-family="Arial, sans-serif" font-size="18" font-weight="bold" fill="${statusColor}">${(summary.status.escapeXml())}</text>
        """.trimIndent())
        
        // Highlights
        summary.highlights.forEachIndexed { index, highlight ->
            svg.append("""
                <text x="250" y="${summaryY + 95 + index * 20}" font-family="Arial, sans-serif" font-size="14" fill="#666">• ${(highlight.escapeXml())}</text>
            """.trimIndent())
        }
        
        return svg.toString()
    }
    
    /**
     * Generates the footer section of the SVG.
     *
     * @param scorecard The MigrationScoreCard object
     * @return The SVG fragment as a string
     */
    private fun generateFooter(scorecard: MigrationScoreCard): String {
        val footerY = 750 + (scorecard.performanceMetrics.size + 1) / 2 * 180 + scorecard.keyOptimizations.size * 60
        
        return """
            <!-- Footer -->
            <rect x="50" y="${footerY}" width="1300" height="40" rx="5" fill="#f8f9fa" stroke="#ddd" stroke-width="1"/>
            <text x="700" y="${footerY + 25}" font-family="Arial, sans-serif" font-size="14" text-anchor="middle" fill="#666">${(scorecard.footerText.escapeXml())}</text>
        """.trimIndent()
    }
}