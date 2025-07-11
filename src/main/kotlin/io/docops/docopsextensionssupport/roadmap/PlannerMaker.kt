package io.docops.docopsextensionssupport.roadmap

import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.chart.STUNNINGPIE
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
import java.io.File

// Modern branding colors for dynamic columns
val DOCOPS_BRANDING_COLORS = listOf(
    "#FF6B6B", // Modern Red
    "#4ECDC4", // Modern Blue/Teal
    "#A8E6CF", // Modern Green
    "#FFD93D", // Modern Yellow
    "#9B59B6", // Modern Purple
    "#E67E22", // Modern Orange
    "#3498DB", // Modern Blue
    "#2ECC71"  // Modern Green variant
)

class PlannerMaker {

    fun makePlannerImage(source: String, title: String, scale: String, useDark: Boolean = false): String {
        val parser = PlannerParser()
        val planItems = parser.parse(source)
        val sb = StringBuilder()
        val cols = planItems.toColumns()
        val grads = planItems.colorDefs(cols)
        val itemGrad = itemGradient(planItems)
        val width = determineWidth(planItems)
        val height = determineHeight(planItems)
        sb.append(makeHead(planItems, title, grads, itemGrad, width, height, scale, useDark))
        sb.append("""<g>""")

        // Dynamic title color based on theme
        val titleColor = if (useDark) "#F7FAFC" else "#2D3748"
        sb.append("""
            <!-- Modern Title -->
            <text x="${width/2}" y="40" text-anchor="middle" 
                  style="font-family: 'Inter', 'Segoe UI', sans-serif; 
                         font-size: 32px; 
                         font-weight: 700; 
                         fill: ${titleColor};">${title.escapeXml()}</text>
        """)

        sb.append("<g transform=\"translate(0, 60)\">")
        var column = 0
        cols.forEach { (key, value) ->
            var color = DOCOPS_BRANDING_COLORS[column % DOCOPS_BRANDING_COLORS.size]
            if(value[0].color != null) {
                color = value[0].color!!
            }
            val startX = 50 + (column * 300)
            sb.append(makeColumn(key, value, 60, startX, colorIn = "url(#planItem_$column)", color, useDark))
            column++
        }
        sb.append("""</g>""")
        sb.append("""</g>""")
        sb.append(makeEnd())
        return sb.toString()
    }


    private fun itemGradient(planItems: PlanItems): String {
    val sb = StringBuilder()
    planItems.items.forEach {
        it.color?.let { color ->
            sb.append(it.colorGradient())
        }
    }
    return sb.toString()
}

    // Update makeColumn to accept dark mode parameter
    private fun makeColumn(
        key: String,
        value: List<PlanItem>,
        startY: Int,
        startX: Int,
        colorIn: String,
        color: String,
        useDark: Boolean = false
    ): String {
        val sb = StringBuilder()

        // Column header remains the same (colored background with white text)
        sb.append("""
        <rect x="${startX}" y="${startY}" width="280" height="50" 
              fill="${color}" rx="8" ry="8" class="card"/>
        <text x="${startX + 140}" y="${startY + 30}" text-anchor="middle" class="column-header">
            ${key.escapeXml()}
        </text>
        """)

        var currentY = startY + 70

        value.forEach { item ->
            val cardHeight = calculateCardHeight(item)

            // Dynamic card styling based on theme
            val cardFill = if (useDark) "#2D3748" else "white"
            val cardStroke = if (useDark) "#4A5568" else "#E2E8F0"

            sb.append("""
            <rect x="${startX + 10}" y="${currentY}" width="260" height="${cardHeight}" 
                  fill="${cardFill}" rx="8" ry="8" class="card" 
                  stroke="${cardStroke}" stroke-width="1"/>
            """)

            // Item title with dynamic color
            val titleY = currentY + 25
            val titleColor = if (useDark) "#F7FAFC" else "#2D3748"
            if (item.title != null) {
                sb.append("""
                <text x="${startX + 25}" y="${titleY}" 
                      style="font-family: 'Inter', 'Segoe UI', sans-serif; font-weight: 600; font-size: 16px; fill: ${titleColor};">
                    ${item.title.escapeXml()}
                </text>
                """)
            }

            // Item content with bullets
            val contentY = if (item.title != null) titleY + 25 else titleY
            if (item.content?.isNotEmpty() ?: false) {
                sb.append(renderTextWithBullets(item.content!!, startX + 25f, contentY.toFloat(), item.urlMap, useDark))
            }

            currentY += cardHeight + 15
        }

        return sb.toString()
    }


    private fun calculateCardHeight(item: PlanItem): Int {
        val baseHeight = 40
        val titleHeight = if (item.title != null) 30 else 0

        // Calculate wrapped text lines for proper height
        var totalWrappedLines = 0
        item.content?.split("\n")?.forEach { line ->
            val wrappedLines = when {
                line.startsWith("[BULLET_DOT]") -> {
                    itemTextWidth(line.substring(12), 240F, 14, "Inter")
                }
                line.startsWith("[BULLET_CHEVRON]") -> {
                    itemTextWidth(line.substring(16), 240F, 14, "Inter")
                }
                line.startsWith("[BULLET_PLUS]") -> {
                    itemTextWidth(line.substring(13), 240F, 14, "Inter")
                }
                line.startsWith("[BULLET_DASH]") -> {
                    itemTextWidth(line.substring(13), 240F, 14, "Inter")
                }
                else -> {
                    itemTextWidth(line, 240F, 14, "Inter")
                }
            }
            totalWrappedLines += wrappedLines.size
        }

        val contentHeight = totalWrappedLines * 20
        return baseHeight + titleHeight + contentHeight
    }

    private fun determineWidth(planItems: PlanItems) : Int {
        // Match reference planner.svg size - ensure minimum width of 1200
        val calculatedWidth = (280 * planItems.toColumns().size) + (planItems.toColumns().size * 20) + 80
        return maxOf(calculatedWidth, 1200)
    }
    private fun determineHeight(planItems: PlanItems) : Int {
        // Match reference planner.svg size - ensure minimum height of 600
        val calculatedHeight = planItems.maxRows() * 240 + 160
        return maxOf(calculatedHeight, 600)
    }

    fun createBulletSymbols(): String {
        return """
        <defs>
            <symbol id="bullet-dot" viewBox="0 0 10 10">
                <circle cx="5" cy="5" r="2" fill="currentColor"/>
            </symbol>
            <symbol id="bullet-chevron" viewBox="0 0 10 10">
                <path d="M3 2 L7 5 L3 8" stroke="currentColor" stroke-width="1.5" fill="none"/>
            </symbol>
            <symbol id="bullet-plus" viewBox="0 0 10 10">
                <path d="M5 2 L5 8 M2 5 L8 5" stroke="currentColor" stroke-width="1.5"/>
            </symbol>
            <symbol id="bullet-dash" viewBox="0 0 10 10">
                <path d="M2 5 L8 5" stroke="currentColor" stroke-width="1.5"/>
            </symbol>
        </defs>
    """.trimIndent()
    }

    // Update renderTextWithBullets to accept dark mode parameter
    fun renderTextWithBullets(text: String, x: Float, y: Float, urlMap: MutableMap<String, String>, useDark: Boolean = false, lineHeight: Float = 20f): String {
        val lines = text.split("\n")
        val result = StringBuilder()
        var currentY = y

        val textColor = if (useDark) "#E2E8F0" else "#4A5568"
        val bulletColor = if (useDark) "#A0AEC0" else "#718096"

        lines.forEach { line ->
            when {
                line.startsWith("[BULLET_DOT]") -> {
                    val content = line.substring(12)
                    val wrappedLines = itemTextWidth(content, 240F, 14, "Inter")
                    val processedLines = linesToUrlIfExist(wrappedLines, urlMap)
                    processedLines.forEachIndexed { index, wrappedLine ->
                        if (index == 0) {
                            result.append("""<use href="#bullet-dot" x="${x}" y="${currentY - 3}" width="8" height="8" style="color: ${bulletColor}"/>""")
                            if (wrappedLine.contains("<a xlink:href=")) {
                                result.append("""<text x="${x + 12}" y="${currentY}" style="font-family: 'Inter', 'Segoe UI', sans-serif; font-size: 14px; fill: ${textColor};">$wrappedLine</text>""")
                            } else {
                                result.append("""<text x="${x + 12}" y="${currentY}" style="font-family: 'Inter', 'Segoe UI', sans-serif; font-size: 14px; fill: ${textColor};">${wrappedLine.escapeXml()}</text>""")
                            }
                        } else {
                            if (wrappedLine.contains("<a xlink:href=")) {
                                result.append("""<text x="${x + 12}" y="${currentY}" style="font-family: 'Inter', 'Segoe UI', sans-serif; font-size: 14px; fill: ${textColor};">$wrappedLine</text>""")
                            } else {
                                result.append("""<text x="${x + 12}" y="${currentY}" style="font-family: 'Inter', 'Segoe UI', sans-serif; font-size: 14px; fill: ${textColor};">${wrappedLine.escapeXml()}</text>""")
                            }
                        }
                        currentY += lineHeight
                    }
                    currentY -= lineHeight
                }
                // Similar updates for other bullet types...
                line.startsWith("[BULLET_CHEVRON]") -> {
                    val content = line.substring(16)
                    val wrappedLines = itemTextWidth(content, 240F, 14, "Inter")
                    val processedLines = linesToUrlIfExist(wrappedLines, urlMap)
                    processedLines.forEachIndexed { index, wrappedLine ->
                        if (index == 0) {
                            result.append("""<use href="#bullet-chevron" x="${x}" y="${currentY - 3}" width="8" height="8" style="color: ${bulletColor}"/>""")
                            if (wrappedLine.contains("<a xlink:href=")) {
                                result.append("""<text x="${x + 12}" y="${currentY}" style="font-family: 'Inter', 'Segoe UI', sans-serif; font-size: 14px; fill: ${textColor};">$wrappedLine</text>""")
                            } else {
                                result.append("""<text x="${x + 12}" y="${currentY}" style="font-family: 'Inter', 'Segoe UI', sans-serif; font-size: 14px; fill: ${textColor};">${wrappedLine.escapeXml()}</text>""")
                            }
                        } else {
                            if (wrappedLine.contains("<a xlink:href=")) {
                                result.append("""<text x="${x + 12}" y="${currentY}" style="font-family: 'Inter', 'Segoe UI', sans-serif; font-size: 14px; fill: ${textColor};">$wrappedLine</text>""")
                            } else {
                                result.append("""<text x="${x + 12}" y="${currentY}" style="font-family: 'Inter', 'Segoe UI', sans-serif; font-size: 14px; fill: ${textColor};">${wrappedLine.escapeXml()}</text>""")
                            }
                        }
                        currentY += lineHeight
                    }
                    currentY -= lineHeight
                }
                // Continue with other bullet types...
                else -> {
                    val wrappedLines = itemTextWidth(line, 240F, 14, "Inter")
                    val processedLines = linesToUrlIfExist(wrappedLines, urlMap)
                    processedLines.forEach { wrappedLine ->
                        if (wrappedLine.contains("<a xlink:href=")) {
                            result.append("""<text x="${x}" y="${currentY}" style="font-family: 'Inter', 'Segoe UI', sans-serif; font-size: 14px; fill: ${textColor};">$wrappedLine</text>""")
                        } else {
                            result.append("""<text x="${x}" y="${currentY}" style="font-family: 'Inter', 'Segoe UI', sans-serif; font-size: 14px; fill: ${textColor};">${wrappedLine.escapeXml()}</text>""")
                        }
                        currentY += lineHeight
                    }
                    currentY -= lineHeight
                }
            }
            currentY += lineHeight
        }

        return result.toString()
    }



    // Update makeHead to accept dark mode parameter
    private fun makeHead(planItems: PlanItems, title: String, grads: String, itemGrad: String, width: Int, height: Int, scale: String, useDark: Boolean = false): String {
        val backgroundColor = if (useDark) "#1A202C" else "#FFFFFF"

        return """
        <svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}" xmlns:xlink="http://www.w3.org/1999/xlink">
        <rect width="100%" height="100%" fill="${backgroundColor}"/>
        ${createBulletSymbols()}
        <defs>
            ${grads}
            ${itemGrad}
        </defs>
        """.trimIndent()
    }

    private fun makeEnd() = "</svg>"
}

fun main() {
    val str = """
- now Current Tasks
Learn Node.js and Express
* Regular bullet point
>> Chevron bullet point
[[https://nodejs.org Node.js Documentation]]
- next Upcoming Work
Study React fundamentals
>> Build interactive UIs
* State management with Redux
[[https://reactjs.org React Documentation]]
- later Future Plans
Docker containerization
>> CI/CD pipeline setup
* Cloud deployment (AWS/Azure)
[[https://docker.com Docker Hub]]
- done Completed Items
HTML, CSS, JavaScript
>> Git version control
* Basic algorithms and data structures
[[https://github.com GitHub]]
    """.trimIndent()
    val p = PlannerMaker()
    val svg =p.makePlannerImage(str, "Enhanced Development Roadmap", "1.0")
    val f = File("gen/plannernew.svg")
    f.writeBytes(svg.toByteArray())
    println("Generated enhanced planner with larger size, chevron bullets, and wiki links")
}
