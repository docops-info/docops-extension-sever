package io.docops.docopsextensionssupport.releasestrategy

import io.docops.docopsextensionssupport.badge.findHeightWidth
import io.docops.docopsextensionssupport.svgsupport.SvgToPng
import io.docops.docopsextensionssupport.web.panel.uncompressString
import kotlinx.serialization.json.Json
import org.apache.commons.logging.LogFactory
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.time.measureTimedValue

class ReleaseHandler {
    val log = LogFactory.getLog(ReleaseHandler::class.java)
    fun handleSVG(payload: String, useDark: Boolean) : ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val release = Json.decodeFromString<ReleaseStrategy>(data)
            release.useDark = useDark
            var output = ""
            when (release.style) {
                "TL" -> {
                    output = createTimelineSvg(release, false)
                }

                "TLS" -> {
                    output = createTimelineSummarySvg(release, false)
                }

                "R" -> {
                    output = createRoadMap(release, false, "OFF")
                }

                "TLG" -> {
                    output = createTimelineGrouped(release, false)
                }
            }

            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("image/svg+xml")
            ResponseEntity(output.toByteArray(), headers, OK)

        }
        log.info("getRelease executed in ${timing.duration.inWholeMilliseconds}ms ")
        return timing.value
    }

    fun handlePNG(payload: String, useDark: Boolean) : ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val release = Json.decodeFromString<ReleaseStrategy>(data)
            release.useDark = useDark
            var output = ""
            when (release.style) {
                "TL" -> {
                    output = createTimelineSvg(release, true)
                }

                "TLS" -> {
                    output = createTimelineSummarySvg(release, true)
                }

                "R" -> {
                    output = createRoadMap(release, true, "OFF")
                }

                "TLG" -> {
                    output = createTimelineGrouped(release, true)
                }
            }

            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType("image", "png", StandardCharsets.UTF_8)
            val res = findHeightWidth(output)
            val png = SvgToPng().toPngFromSvg(output, res)
            ResponseEntity(png, headers, OK)

        }
        log.info("getRelease executed in ${timing.duration.inWholeMilliseconds}ms ")
        return timing.value
    }
    fun createTimelineSvg(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false): String = ReleaseTimelineMaker().make(releaseStrategy, isPdf)

    private fun createTimelineSummarySvg(release: ReleaseStrategy, pdf: Boolean = false) : String =ReleaseTimelineSummaryMaker().make(release, isPdf = pdf)

    fun createTimelineGrouped(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false): String = ReleaseTimelineGroupedMaker().make(releaseStrategy, isPdf)
    fun createRoadMap(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false, animate: String = "ON"): String = ReleaseRoadMapMaker().make(releaseStrategy, isPdf, animate)

}