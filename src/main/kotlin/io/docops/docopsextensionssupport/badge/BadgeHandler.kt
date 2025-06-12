package io.docops.docopsextensionssupport.badge

import io.docops.docopsextensionssupport.button.shape.joinXmlLines
import io.docops.docopsextensionssupport.svgsupport.addSvgMetadata
import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.github.sercasti.tracing.Traceable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Component
class BadgeHandler @Autowired constructor(private val docOpsBadgeGenerator: DocOpsBadgeGenerator) {

    @Traceable
    fun handleSVG(payload: String, backend: String) : ResponseEntity<ByteArray>  {

        val data = uncompressString(URLDecoder.decode(payload,"UTF-8"))

        val isPdf = backend == "pdf"

        return createBadgeFromString(data, isPdf)
    }

    public fun createBadgeFromString(
        data: String,
        isPdf: Boolean
    ): ResponseEntity<ByteArray> {
        val badges = mutableListOf<Badge>()
        // Try to parse as JSON first
        try {
            // Check if the data looks like JSON (starts with [ for array or { for object)
            if (data.trim().startsWith("[") || data.trim().startsWith("{")) {
                // If it starts with [, it's a JSON array of badges
                if (data.trim().startsWith("[")) {
                    val badgeList = Json.decodeFromString<List<Badge>>(data)
                    badges.addAll(badgeList.map {
                        Badge(
                            label = it.label,
                            message = it.message,
                            labelColor = it.labelColor,
                            messageColor = it.messageColor,
                            url = it.url,
                            logo = it.logo,
                            fontColor = it.fontColor,
                            isPdf = isPdf
                        )
                    })
                }
                // If it starts with {, it's a single JSON badge
                else {
                    val badge = Json.decodeFromString<Badge>(data)
                    badges.add(
                        Badge(
                            label = badge.label,
                            message = badge.message,
                            labelColor = badge.labelColor,
                            messageColor = badge.messageColor,
                            url = badge.url,
                            logo = badge.logo,
                            fontColor = badge.fontColor,
                            isPdf = isPdf
                        )
                    )
                }
            }
            // If it doesn't look like JSON, process as pipe-delimited
            else {
                processPipeDelimitedData(data, badges, isPdf)
            }
        }
        // If JSON parsing fails, fall back to pipe-delimited processing
        catch (e: Exception) {
            processPipeDelimitedData(data, badges, isPdf)
        }

        var rows = 1
        if (badges.size > 3) {
            rows = badges.size / 3 + 1
        }
        val svgSrc = docOpsBadgeGenerator.createBadgeFromList(badges = badges)
        val svg = StringBuilder()
        //language=svg
        svg.append(
            """
                <svg width='${svgSrc.second}' height='${rows * 20}' xmlns='http://www.w3.org/2000/svg' role='img' xmlns:xlink="http://www.w3.org/1999/xlink" aria-label='Made With: Kotlin'>
            """.trimIndent()
        )
        svg.append(svgSrc.first)
        svg.append("</svg>")
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
        val xml = addSvgMetadata(svg.toString())
        return ResponseEntity(joinXmlLines(xml).toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
    }

    /**
     * Process pipe-delimited data format
     */
    private fun processPipeDelimitedData(data: String, badges: MutableList<Badge>, isPdf: Boolean) {
        data.lines().forEach { line ->
            val split = line.split("|")
            if(line.isNotEmpty()) {
            when {
                split.size < 6 -> {
                    throw BadgeFormatException("Badge Format invalid, expecting 5 pipe delimited values [$data]")
                }
                else -> {
                    val message: String = split[1]
                    val label: String = split[0]
                    var mcolor = "GREEN"
                    val color: String = split[3].trim()
                    val c = split[4].trim()
                    if (c.isNotEmpty()) {
                        mcolor = c
                    }
                    var logo = split[5].trim()

                    var fontColor = "#ffffff"
                    if (split.size == 7) {
                        fontColor = split[6]
                    }

                    //val output = Badge.create(label, message, color, mcolor, null, 0, 1)
                    val b = Badge(
                        label = label,
                        message = message,
                        labelColor = color,
                        messageColor = mcolor,
                        url = split[2],
                        logo = logo,
                        fontColor = fontColor,
                        isPdf = isPdf
                    )
                    badges.add(b)
                }
            }}
        }
    }
}
