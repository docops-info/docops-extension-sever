package io.docops.docopsextensionssupport.releasestrategy

import io.docops.docopsextensionssupport.web.panel.uncompressString
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import kotlinx.serialization.json.Json
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URLDecoder
import kotlin.time.measureTimedValue

class ReleaseHandler {
    val log = KotlinLogging.logger {}
    fun handleSVG(payload: String, useDark: Boolean, backend: String) : ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val release = Json.decodeFromString<ReleaseStrategy>(data)
            release.useDark = useDark
            val isPdf = backend == "pdf"
            var output = ""
            when (release.style) {
                "TL" -> {
                    output = createTimelineSvg(release, isPdf)
                }

                "TLS" -> {
                    output = createTimelineSummarySvg(release, isPdf)
                }

                "R" -> {
                    output = createRoadMap(release, isPdf, "OFF")
                }

                "TLG" -> {
                    output = createTimelineGrouped(release, isPdf)
                }
            }

            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("image/svg+xml")
            withLoggingContext("useDark" to "$useDark", "backend" to backend, "style" to release.style)
            {
                log.info{"Generated release strategy"}
            }
            ResponseEntity(output.toByteArray(), headers, OK)

        }
        log.info{"getRelease executed in ${timing.duration.inWholeMilliseconds}ms "}
        return timing.value
    }

    fun createTimelineSvg(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false): String = ReleaseTimelineMaker().make(releaseStrategy, isPdf)

    private fun createTimelineSummarySvg(release: ReleaseStrategy, pdf: Boolean = false) : String =ReleaseTimelineSummaryMaker().make(release, isPdf = pdf)

    fun createTimelineGrouped(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false): String = ReleaseTimelineGroupedMaker().make(releaseStrategy, isPdf)
    fun createRoadMap(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false, animate: String = "ON"): String = ReleaseRoadMapMaker().make(releaseStrategy, isPdf, animate)

}