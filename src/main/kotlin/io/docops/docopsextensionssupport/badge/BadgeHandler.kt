package io.docops.docopsextensionssupport.badge

import io.docops.docopsextensionssupport.button.shape.joinXmlLines
import io.docops.docopsextensionssupport.web.panel.uncompressString
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class BadgeHandler {

    fun handleSVG(payload: String, backend: String) : ResponseEntity<ByteArray>  {

        val docOpsBadgeGenerator = DocOpsBadgeGenerator()
        val data = uncompressString(URLDecoder.decode(payload,"UTF-8"))
        println(data)
        val badges = mutableListOf<Badge>()
        val isPdf = backend == "pdf"

        data.lines().forEach { line ->
            val split = line.split("|")
            if(line.isNotEmpty()) {
            when {
                split.size < 6 -> {
                    println(line)
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
                        url = split[1],
                        logo = logo,
                        fontColor = fontColor,
                        isPdf = isPdf
                    )
                    badges.add(b)
                }
            }}
        }

        var rows= 1
        if(badges.size>3) {
            rows = badges.size / 3 + 1
        }
        val svgSrc = docOpsBadgeGenerator.createBadgeFromList(badges=badges)
        val svg = StringBuilder()
        //language=svg
        svg.append("""
            <svg width='${svgSrc.second}' height='${rows*20}' xmlns='http://www.w3.org/2000/svg' role='img' xmlns:xlink="http://www.w3.org/1999/xlink" aria-label='Made With: Kotlin'>
        """.trimIndent())
        svg.append(svgSrc.first)
        svg.append("</svg>")
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
        return ResponseEntity(joinXmlLines(svg.toString()).toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
    }
}