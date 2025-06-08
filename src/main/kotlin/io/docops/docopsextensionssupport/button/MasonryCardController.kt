
package io.docops.docopsextensionssupport.button

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/masonry")
class MasonryCardController {

    @GetMapping(value = ["/cards"], produces = ["image/svg+xml"])
    fun generateMasonryCards(
        @RequestParam(defaultValue = "3") columns: Int,
        @RequestParam(defaultValue = "300") cardWidth: Int,
        @RequestParam(defaultValue = "20") gap: Int,
        @RequestParam(defaultValue = "false") darkMode: Boolean
    ): ResponseEntity<String> {
        val cards = getSampleCardData()
        val svg = generateMasonrySvg(cards, columns, cardWidth, gap, darkMode)

        val headers = HttpHeaders().apply {
            contentType = MediaType.parseMediaType("image/svg+xml")
            cacheControl = "public, max-age=3600"
        }

        return ResponseEntity.ok()
            .headers(headers)
            .body(svg)
    }

    @PostMapping(value = ["/cards"], produces = ["image/svg+xml"])
    fun generateCustomMasonryCards(@RequestBody request: MasonryRequest): ResponseEntity<String> {
        val svg = generateMasonrySvg(
            request.cards,
            request.columns,
            request.cardWidth,
            request.gap,
            request.darkMode
        )

        val headers = HttpHeaders().apply {
            contentType = MediaType.parseMediaType("image/svg+xml")
        }

        return ResponseEntity.ok()
            .headers(headers)
            .body(svg)
    }

    private fun generateMasonrySvg(
        cards: List<CardData>,
        columns: Int,
        cardWidth: Int,
        gap: Int,
        darkMode: Boolean
    ): String {
        val svg = StringBuilder()
        val columnHeights = IntArray(columns)
        val totalWidth = (cardWidth * columns) + (gap * (columns - 1))

        // Enhanced color scheme matching showcase cards
        val bgColor = if (darkMode) "#0f172a" else "#f8fafc"
        val cardBg = if (darkMode) "#1e293b" else "#ffffff"
        val cardBorder = if (darkMode) "#334155" else "#e2e8f0"
        val primaryColor = if (darkMode) "#60a5fa" else "#3b82f6"
        val textPrimary = if (darkMode) "#f1f5f9" else "#1e293b"
        val textSecondary = if (darkMode) "#94a3b8" else "#64748b"
        val textMuted = if (darkMode) "#64748b" else "#94a3b8"

        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" ")
        svg.append("width=\"$totalWidth\" ")
        svg.append("viewBox=\"0 0 $totalWidth ")

        // Calculate total height
        val tempColumnHeights = IntArray(columns)
        cards.forEach { card ->
            val shortestColumn = getShortestColumnIndex(tempColumnHeights)
            tempColumnHeights[shortestColumn] += card.height + gap
        }
        val totalHeight = getMaxHeight(tempColumnHeights)

        svg.append("$totalHeight\">\n")

        // Enhanced CSS styles matching showcase design
        svg.append("""
            <defs>
                <style>
                    .masonry-card { 
                        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                        filter: drop-shadow(0 1px 3px rgba(0, 0, 0, 0.1)) drop-shadow(0 1px 2px rgba(0, 0, 0, 0.06));
                    }
                    .masonry-card:hover { 
                        transform: translateY(-8px);
                        filter: drop-shadow(0 20px 25px rgba(0, 0, 0, 0.15)) drop-shadow(0 10px 10px rgba(0, 0, 0, 0.04));
                    }
                    .card-title { 
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', sans-serif;
                        font-weight: 700;
                        letter-spacing: -0.025em;
                    }
                    .card-subtitle {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', sans-serif;
                        font-weight: 600;
                        letter-spacing: -0.01em;
                    }
                    .card-content { 
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', sans-serif;
                        font-weight: 400;
                        line-height: 1.5;
                    }
                    .card-tag {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', sans-serif;
                        font-weight: 500;
                        letter-spacing: 0.025em;
                    }
                    .icon-circle {
                        transition: all 0.3s ease;
                    }
                    .masonry-card:hover .icon-circle {
                        transform: scale(1.1);
                    }
                    @media (max-width: 768px) { 
                        .masonry-card { transform: scale(0.95); }
                        .masonry-card:hover { transform: scale(0.95) translateY(-4px); }
                    }
                </style>
                
                <!-- Gradient definitions -->
                <linearGradient id="cardGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:${if (darkMode) "#1e293b" else "#ffffff"};stop-opacity:1" />
                    <stop offset="100%" style="stop-color:${if (darkMode) "#334155" else "#f8fafc"};stop-opacity:1" />
                </linearGradient>
                
                <linearGradient id="iconGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:#60a5fa;stop-opacity:1" />
                    <stop offset="100%" style="stop-color:#3b82f6;stop-opacity:1" />
                </linearGradient>
                
                <!-- Card shadow filter -->
                <filter id="cardShadow" x="-50%" y="-50%" width="200%" height="200%">
                    <feDropShadow dx="0" dy="4" stdDeviation="6" flood-color="rgba(0,0,0,0.1)"/>
                </filter>
            </defs>
            
        """.trimIndent())

        // Background with subtle gradient
        svg.append("<rect width=\"100%\" height=\"100%\" fill=\"$bgColor\"/>\n")

        // Generate enhanced cards
        cards.forEach { card ->
            val columnIndex = getShortestColumnIndex(columnHeights)
            val x = columnIndex * (cardWidth + gap)
            val y = columnHeights[columnIndex]

            svg.append(generateEnhancedCardSvg(card, x, y, cardWidth, cardBg, cardBorder, primaryColor, textPrimary, textSecondary, textMuted))

            columnHeights[columnIndex] += card.height + gap
        }

        svg.append("</svg>")
        return svg.toString()
    }

    private fun generateEnhancedCardSvg(
        card: CardData,
        x: Int,
        y: Int,
        width: Int,
        cardBg: String,
        cardBorder: String,
        primaryColor: String,
        textPrimary: String,
        textSecondary: String,
        textMuted: String
    ): String {
        val cardSvg = StringBuilder()

        cardSvg.append("<g class=\"masonry-card\" transform=\"translate($x,$y)\">\n")

        // Enhanced card background with subtle gradient and better shadow
        cardSvg.append(
            "<rect width=\"$width\" height=\"${card.height}\" " +
                    "rx=\"12\" ry=\"12\" fill=\"url(#cardGradient)\" " +
                    "stroke=\"$cardBorder\" stroke-width=\"1\" " +
                    "filter=\"url(#cardShadow)\"/>\n"
        )

        // Card content with better spacing and typography
        var contentY = 24

        // Icon/Avatar circle (top-left)
        cardSvg.append(
            "<circle class=\"icon-circle\" cx=\"32\" cy=\"32\" r=\"16\" " +
                    "fill=\"url(#iconGradient)\" opacity=\"0.1\"/>\n"
        )
        cardSvg.append(
            "<circle class=\"icon-circle\" cx=\"32\" cy=\"32\" r=\"8\" " +
                    "fill=\"$primaryColor\"/>\n"
        )

        // Status indicator (top-right)
        if (card.status != null) {
            val statusColor = when (card.status.lowercase()) {
                "active", "completed", "live" -> "#10b981"
                "pending", "in-progress" -> "#f59e0b"
                "archived", "deprecated" -> "#ef4444"
                else -> primaryColor
            }
            cardSvg.append(
                "<circle cx=\"${width - 24}\" cy=\"24\" r=\"4\" fill=\"$statusColor\"/>\n"
            )
        }

        contentY = 64 // Start content below the icon

        // Enhanced title with better typography
        card.title?.let { title ->
            cardSvg.append(
                "<text x=\"20\" y=\"$contentY\" class=\"card-title\" " +
                        "fill=\"$textPrimary\" font-size=\"18\">${escapeXml(title)}</text>\n"
            )
            contentY += 32
        }

        // Subtitle/Category
        card.category?.let { category ->
            cardSvg.append(
                "<text x=\"20\" y=\"$contentY\" class=\"card-subtitle\" " +
                        "fill=\"$primaryColor\" font-size=\"12\" text-transform=\"uppercase\">${escapeXml(category)}</text>\n"
            )
            contentY += 24
        }

        // Enhanced description with better line spacing
        card.description?.let { description ->
            val lines = wrapText(description, width - 40, 14)
            lines.take(3).forEach { line -> // Limit to 3 lines for better layout
                cardSvg.append(
                    "<text x=\"20\" y=\"$contentY\" class=\"card-content\" " +
                            "fill=\"$textSecondary\" font-size=\"14\">${escapeXml(line)}</text>\n"
                )
                contentY += 20
            }
            if (lines.size > 3) {
                cardSvg.append(
                    "<text x=\"20\" y=\"$contentY\" class=\"card-content\" " +
                            "fill=\"$textMuted\" font-size=\"12\">...</text>\n"
                )
            }
            contentY += 16
        }

        // Metrics/Stats section
        card.metrics?.let { metrics ->
            contentY += 8
            var metricX = 20
            metrics.take(2).forEach { metric -> // Show up to 2 metrics
                cardSvg.append(
                    "<text x=\"$metricX\" y=\"$contentY\" class=\"card-content\" " +
                            "fill=\"$textPrimary\" font-size=\"16\" font-weight=\"600\">${escapeXml(metric.value)}</text>\n"
                )
                cardSvg.append(
                    "<text x=\"$metricX\" y=\"${contentY + 16}\" class=\"card-content\" " +
                            "fill=\"$textMuted\" font-size=\"11\">${escapeXml(metric.label)}</text>\n"
                )
                metricX += (width - 40) / 2
            }
            contentY += 40
        }

        // Enhanced tags with modern pill design
        if (!card.tags.isNullOrEmpty()) {
            contentY += 8
            var tagX = 20
            var tagY = contentY
            val maxTagsPerRow = 2
            var tagsInCurrentRow = 0

            card.tags.take(4).forEach { tag -> // Limit to 4 tags
                val tagWidth = minOf(tag.length * 7 + 20, (width - 50) / maxTagsPerRow)

                if (tagX + tagWidth > width - 20 || tagsInCurrentRow >= maxTagsPerRow) {
                    tagX = 20
                    tagY += 28
                    tagsInCurrentRow = 0
                }

                // Modern pill-shaped tag
                cardSvg.append(
                    "<rect x=\"$tagX\" y=\"${tagY - 14}\" width=\"$tagWidth\" height=\"20\" " +
                            "rx=\"10\" fill=\"$primaryColor\" opacity=\"0.08\"/>\n"
                )
                cardSvg.append(
                    "<text x=\"${tagX + tagWidth/2}\" y=\"${tagY - 2}\" class=\"card-tag\" " +
                            "fill=\"$primaryColor\" font-size=\"10\" text-anchor=\"middle\">${escapeXml(tag)}</text>\n"
                )

                tagX += tagWidth + 8
                tagsInCurrentRow++
            }
        }

        cardSvg.append("</g>\n")
        return cardSvg.toString()
    }

    private fun getShortestColumnIndex(columnHeights: IntArray): Int =
        columnHeights.indices.minByOrNull { columnHeights[it] } ?: 0

    private fun getMaxHeight(columnHeights: IntArray): Int =
        columnHeights.maxOrNull() ?: 0

    private fun wrapText(text: String, maxWidth: Int, fontSize: Int): List<String> {
        val charsPerLine = maxWidth / (fontSize / 2)
        val lines = mutableListOf<String>()
        val words = text.split(" ")
        var currentLine = StringBuilder()

        words.forEach { word ->
            if (currentLine.length + word.length + 1 <= charsPerLine) {
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder(word)
                } else {
                    lines.add(word)
                }
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }

        return lines
    }

    private fun escapeXml(text: String): String =
        text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")

    private fun getSampleCardData(): List<CardData> = listOf(
        CardData(
            title = "API Gateway",
            category = "Infrastructure",
            description = "Centralized API management and routing solution with advanced load balancing and security features.",
            tags = listOf("Spring", "Gateway", "Security", "Cloud"),
            height = 200,
            status = "Active",
            metrics = listOf(
                Metric("99.9%", "Uptime"),
                Metric("1.2ms", "Avg Response")
            )
        ),
        CardData(
            title = "ML Pipeline",
            category = "Data Science",
            description = "Automated machine learning workflow for predictive analytics with real-time processing capabilities.",
            tags = listOf("Python", "TensorFlow", "Kubernetes"),
            height = 180,
            status = "In-Progress",
            metrics = listOf(
                Metric("94.2%", "Accuracy"),
                Metric("156K", "Models Trained")
            )
        ),
        CardData(
            title = "Mobile App",
            category = "Frontend",
            description = "Cross-platform mobile application with offline-first architecture and real-time synchronization.",
            tags = listOf("React Native", "TypeScript"),
            height = 160,
            status = "Live",
            metrics = listOf(
                Metric("4.8★", "App Store"),
                Metric("50K+", "Downloads")
            )
        ),
        CardData(
            title = "Documentation Portal",
            category = "DevOps",
            description = "Comprehensive documentation system with automated generation and interactive examples.",
            tags = listOf("AsciiDoc", "DocOps"),
            height = 190,
            status = "Active"
        ),
        CardData(
            title = "Security Framework",
            category = "Security",
            description = "Advanced security implementation with OAuth2, JWT tokens, and comprehensive audit logging.",
            tags = listOf("OAuth2", "JWT", "Security"),
            height = 170,
            status = "Completed",
            metrics = listOf(
                Metric("0", "Vulnerabilities"),
                Metric("100%", "Coverage")
            )
        ),
        CardData(
            title = "Analytics Dashboard",
            category = "Analytics",
            description = "Real-time business intelligence dashboard with customizable widgets and automated reporting.",
            tags = listOf("React", "D3.js", "Analytics"),
            height = 185,
            status = "Active",
            metrics = listOf(
                Metric("2.3M", "Data Points"),
                Metric("15ms", "Query Time")
            )
        )
    )

    // Enhanced data classes
    data class CardData(
        val title: String? = null,
        val category: String? = null,
        val description: String? = null,
        val tags: List<String>? = null,
        val height: Int = 180,
        val status: String? = null,
        val metrics: List<Metric>? = null
    )

    data class Metric(
        val value: String,
        val label: String
    )

    data class MasonryRequest(
        val cards: List<CardData> = emptyList(),
        val columns: Int = 3,
        val cardWidth: Int = 300,
        val gap: Int = 20,
        val darkMode: Boolean = false
    )
}