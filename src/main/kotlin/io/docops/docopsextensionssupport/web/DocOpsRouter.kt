package io.docops.docopsextensionssupport.web

import io.docops.docopsextensionssupport.adr.AdrHandler
import io.docops.docopsextensionssupport.badge.BadgeHandler
import io.docops.docopsextensionssupport.badge.ShieldHandler
import io.docops.docopsextensionssupport.button.ButtonHandler
import io.docops.docopsextensionssupport.cal.CalHandler
import io.docops.docopsextensionssupport.callout.CalloutHandler
import io.docops.docopsextensionssupport.chart.*
import io.docops.docopsextensionssupport.diagram.*
import io.docops.docopsextensionssupport.domainviz.DomainVizHandler
import io.docops.docopsextensionssupport.flow.FlowHandler
import io.docops.docopsextensionssupport.gherkin.GherkinHandler
import io.docops.docopsextensionssupport.metricscard.MetricsCardHandler
import io.docops.docopsextensionssupport.releasestrategy.ReleaseHandler
import io.docops.docopsextensionssupport.roadmap.PlannerHandler
import io.docops.docopsextensionssupport.roadmap.RoadmapHandler
import io.docops.docopsextensionssupport.scorecard.FeatureCardHandler
import io.docops.docopsextensionssupport.scorecard.ScorecardHandler
import io.docops.docopsextensionssupport.svgsupport.addSvgMetadata
import io.docops.docopsextensionssupport.svgsupport.joinXmlLines
import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.docops.docopsextensionssupport.swimlane.SwimLaneHandler
import io.docops.docopsextensionssupport.timeline.TimelineHandler
import io.docops.docopsextensionssupport.todo.TodoHandler
import io.docops.docopsextensionssupport.vcard.VCardHandler
import io.docops.docopsextensionssupport.wordcloud.WordCloudHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.time.measureTimedValue

@RestController
@RequestMapping("/api/docops")
class DocOpsRouter(
    private val applicationEventPublisher: ApplicationEventPublisher
) {


    private val logger = KotlinLogging.logger {}

    // Map to track execution counts for each event
    private val eventCounts = mutableMapOf<String, Int>()

    // Getter for eventCounts map
    fun getEventCounts(): Map<String, Int> = eventCounts.toMap()

    // Registry of handlers by kind

    private fun createHandler(kind: String, csvResponse: CsvResponse): DocOpsHandler {
        return when (kind.lowercase()) {
            "connector" -> ConnectorHandler(csvResponse)
            "placemat" -> PlacematHandler(csvResponse)
            "timeline" -> TimelineHandler(csvResponse)
            "scorecard" -> ScorecardHandler(csvResponse)
            "release" -> ReleaseHandler(csvResponse)
            "cal" -> CalHandler(csvResponse)
            "badge" -> BadgeHandler(csvResponse) // Injected dependency, reuse
            "buttons" -> ButtonHandler(csvResponse)
            "adr" -> AdrHandler(csvResponse)
            "planner" -> PlannerHandler(csvResponse)
            "roadmap" -> RoadmapHandler(csvResponse)
            "pie" -> PieHandler(csvResponse)
            "pieslice" -> PieSliceHandler(csvResponse)
            "bar" -> BarHandler(csvResponse)
            "bargroup" -> BarGroupHandler(csvResponse)
            "line" -> LineHandler(csvResponse)
            "combination" -> CombinationChartHandler(csvResponse)
            "treechart" -> TreeChartHandler(csvResponse)
            "callout" -> CalloutHandler(csvResponse)
            "metricscard" -> MetricsCardHandler(csvResponse)
            "wordcloud" -> WordCloudHandler(csvResponse)
            "quadrant" -> MagicQuadrantHandler(csvResponse)
            "swim" -> SwimLaneHandler(csvResponse)
            "feature" -> FeatureCardHandler(csvResponse)
            "shield" -> ShieldHandler(csvResponse)
            "mermaid" -> DocOpsMermaid(csvResponse)
            "gherkin" -> GherkinHandler(csvResponse)
            "todo" -> TodoHandler(csvResponse)
            "flow" -> FlowHandler(csvResponse)
            "domain" -> DomainVizHandler(csvResponse)
            "vcard" -> VCardHandler(csvResponse)
            else -> throw IllegalArgumentException("Unknown handler kind: $kind")
        }
    }


    @GetMapping("/svg")
    @Timed(
        value = "docops.router.svg",
        description = "Creating a docops visual using http get",
        percentiles = [0.5, 0.9]
    )
    fun getSvg(
        @RequestParam(value = "kind", required = true) kind: String,
        @RequestParam(name = "payload") payload: String,
        @RequestParam(name = "scale", defaultValue = "1.0") scale: String,
        @RequestParam("type", required = false, defaultValue = "SVG") type: String,
        @RequestParam("title", required = false, defaultValue = "") title: String,
        @RequestParam("useDark", defaultValue = "false") useDark: Boolean,
        @RequestParam("useGlass", defaultValue = "false") useGlass: Boolean,
        @RequestParam("docname", defaultValue = "unknown") docname: String,
        @RequestParam("backend", required = false, defaultValue = "html") backend: String,
        httpServletRequest: HttpServletRequest
    ): ResponseEntity<ByteArray> {
        val context = DocOpsContext(
            scale = scale,
            type = type,
            title = title,
            useDark = useDark,
            useGlass = useGlass,
            backend = backend,
            docname = docname
        )

        //println(buildDocOpsSvgUriWithBuilder(httpServletRequest, kind= kind, payload = payload, scale = scale, type = type, title = title, useDark = useDark, useGlass = useGlass, docname = docname, backend = backend))
        val headers = HttpHeaders()
        val response = CsvResponse(mutableListOf<String>(), mutableListOf<List<String>>())
        val handler = createHandler(kind.lowercase(), response)

        val timing = measureTimedValue {
            // First try to URL decode the payload, if it fails, use the original payload

            val decodedPayload = try {
                URLDecoder.decode(payload, "UTF-8")
            } catch (e: IllegalArgumentException) {
                logger.error(e) { "Failed to URL decode payload: ${e.message}" }
                payload
            }

            val data = uncompressString(decodedPayload)
            val finalPayload = decodePayloadIfNeeded(data)
            if(handler is DocOpsMermaid) {
                 handler.handleSVG(finalPayload, context)
            } else {
                joinXmlLines(addSvgMetadata(handler.handleSVG(finalPayload, context), response))
            }
        }


        logger.info { "$kind executed in ${timing.duration.inWholeMilliseconds}ms" }

        // Increment the count for this event
        val count = eventCounts.getOrDefault(kind, 0) + 1
        eventCounts[kind] = count

        // Publish the event with the count
        applicationEventPublisher.publishEvent(
            DocOpsExtensionEvent(
                kind,
                timing.duration.inWholeMilliseconds,
                true,
                count
            )
        )
        headers.cacheControl = CacheControl.noCache().headerValue
        headers["X-Content-Type-Options"] = "nosniff"
        headers["Content-Security-Policy"] =
            "default-src 'self'; script-src 'self'; object-src 'none'; style-src 'self' 'unsafe-inline'"
        if (handler is DocOpsMermaid) {
            headers.contentType = MediaType("text", "html", StandardCharsets.UTF_8)
        } else {
            headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
        }


        return ResponseEntity(
            timing.value.toByteArray(
                StandardCharsets.UTF_8
            ), headers, HttpStatus.OK
        )
    }

    private fun isUrlEncoded(payload: String): Boolean {
        // Check for common URL encoded characters
        val urlEncodedPattern = Regex("%[0-9A-Fa-f]{2}")
        return urlEncodedPattern.containsMatchIn(payload) ||
                payload.contains("+") && payload.contains("%") ||
                payload.contains("%20") || // encoded space
                payload.contains("%2B") || // encoded +
                payload.contains("%2F") || // encoded /
                payload.contains("%3D")    // encoded =
    }

    private fun decodePayloadIfNeeded(payload: String): String {
        return if (isUrlEncoded(payload)) {
            try {
                URLDecoder.decode(payload, "UTF-8")
            } catch (e: IllegalArgumentException) {
                logger.warn { "Original Payload $payload" }
                logger.warn(e) { "Failed to URL decode payload in decodePayloadIfNeeded: ${e.message}" }
                payload
            }
        } else {
            payload
        }
    }

    fun buildDocOpsSvgUriWithBuilder(
        request: HttpServletRequest,
        kind: String,
        payload: String,
        scale: String = "1.0",
        type: String = "SVG",
        title: String = "",
        useDark: Boolean = false,
        useGlass: Boolean = false,
        docname: String = "unknown",
        backend: String = "html"
    ): String {
        val baseUrl = "${request.scheme}://${request.serverName}" +
                (if (request.serverPort != 80 && request.serverPort != 443)
                    ":${request.serverPort}" else "") +
                request.contextPath

        return UriComponentsBuilder.fromUriString(baseUrl)
            .path("/api/docops/svg")
            .queryParam("kind", kind)
            .queryParam("payload", payload)
            .queryParam("scale", scale)
            .queryParam("type", type)
            .queryParam("title", title)
            .queryParam("useDark", useDark)
            .queryParam("useGlass", useGlass)
            .queryParam("docname", docname)
            .queryParam("backend", backend)
            .build()
            .toUriString()
    }


}

@Serializable
data class CsvRequest(
    val content: String,
    val kind: String
)

@Serializable
data class CsvResponse(
    var headers: List<String>,
    var rows: List<List<String>>
)

fun CsvResponse.update(csvResponse: CsvResponse) {
    this.headers = csvResponse.headers
    this.rows = csvResponse.rows
}


val DefaultCsvResponse = CsvResponse(mutableListOf(), mutableListOf(mutableListOf()))

fun CsvResponse.toCsvJsonMetaData(): String {
    val json = Json { encodeDefaults = true }
    val str = """
        <metadata type="text/csv">
            <![CDATA[
            ${json.encodeToString(this)}
            ]]>
          </metadata>
    """.trimIndent()
    return str
}


/**
 * Escape XML special characters
 */
private fun escapeXml(text: String): String {
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
