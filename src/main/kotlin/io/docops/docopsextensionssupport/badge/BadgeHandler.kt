package io.docops.docopsextensionssupport.badge

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update
import kotlinx.serialization.json.Json


class BadgeHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse){

    private val docOpsBadgeGenerator: DocOpsBadgeGenerator = DocOpsBadgeGenerator()
    fun handleSVG(payload: String, backend: String) : String  {
        val isPdf = backend == "pdf"
        val res =  createBadgeFromString(payload)
        csvResponse.update(res.second)
        return res.first
    }

    fun createBadgeFromString(
        data: String
    ): Pair<String, CsvResponse> {
        val badges = createBadgesFromInput(data)

        var rows = 1
        if (badges.size > 5) { // Changed from 3 to 5 based on BadgePerRow
            rows = (badges.size + 4) / 5 // Ceiling division
        }
        val svgSrc = docOpsBadgeGenerator.createBadgeFromList(badges = badges)
        val svg = StringBuilder()

        val totalHeight = rows * 22 // 20px height + 2px spacing

        //language=svg
        svg.append(
            """
                <svg width='${svgSrc.second}' height='$totalHeight' viewBox='0 0 ${svgSrc.second} $totalHeight' xmlns='http://www.w3.org/2000/svg' xmlns:xlink="http://www.w3.org/1999/xlink" role='img' aria-label='Badge Collection'>
                """.trimIndent()
        )
        svg.append(svgSrc.first)
        svg.append("</svg>")
        return Pair(svg.toString(), badges.toCsv())
    }


    fun createBadgesFromInput(data: String): MutableList<Badge> {
        val badges = mutableListOf<Badge>()
        try {
            // Check if the data looks like JSON (starts with [ for array or { for object)
            if (data.trim().startsWith("[") || data.trim().startsWith("{")) {
                // If it starts with [, it's a JSON array of badges
                if (data.trim().startsWith("[")) {
                    val badgeList = Json.decodeFromString<List<Badge>>(data)
                    badges.addAll(badgeList)
                }
                // If it starts with {, it's a single JSON badge
                else {
                    val badge = Json.decodeFromString<Badge>(data)
                    badges.add(badge)
                }
            }
            // If it doesn't look like JSON, process as pipe-delimited
            else {
                processPipeDelimitedData(data, badges)
            }
        }
        // If JSON parsing fails, fall back to pipe-delimited processing
        catch (e: Exception) {
            processPipeDelimitedData(data, badges)
        }
        return badges
    }

    /**
     * Process pipe-delimited data format
     */
    /**
     * Process pipe-delimited data format
     * Supports flexible formats:
     * - Minimum: label|message
     * - With colors: label|message|url|labelColor|messageColor
     * - Full: label|message|url|labelColor|messageColor|logo|fontColor
     */
    private fun processPipeDelimitedData(data: String, badges: MutableList<Badge>) {
        data.lines().forEach { line ->
            if (line.isEmpty()) return@forEach

            val split = line.split("|")

            when {
                split.size < 2 -> {
                    throw BadgeFormatException("Badge Format invalid, expecting at least 2 pipe delimited values (label|message) [$line]")
                }
                else -> {
                    val label: String = split[0].trim()
                    val message: String = split[1].trim()
                    val url: String? = if (split.size > 2 && split[2].trim().isNotEmpty()) split[2].trim() else null
                    val labelColor: String = if (split.size > 3 && split[3].trim().isNotEmpty()) split[3].trim() else "#555555"
                    val messageColor: String = if (split.size > 4 && split[4].trim().isNotEmpty()) split[4].trim() else "#007ec6"
                    val logo: String? = if (split.size > 5 && split[5].trim().isNotEmpty()) split[5].trim() else null
                    val fontColor: String = if (split.size > 6 && split[6].trim().isNotEmpty()) split[6].trim() else "#ffffff"

                    val b = Badge(
                        label = label,
                        message = message,
                        url = url,
                        labelColor = labelColor,
                        messageColor = messageColor,
                        logo = logo,
                        fontColor = fontColor
                    )
                    badges.add(b)
                }
            }
        }
    }
    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, context.backend)
    }

}
