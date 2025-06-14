package io.docops.docopsextensionssupport.web

import io.docops.docopsextensionssupport.adr.AdrHandler
import io.docops.docopsextensionssupport.badge.BadgeHandler
import io.docops.docopsextensionssupport.button.ButtonHandler
import io.docops.docopsextensionssupport.cal.CalHandler
import io.docops.docopsextensionssupport.callout.CalloutHandler
import io.docops.docopsextensionssupport.chart.BarGroupHandler
import io.docops.docopsextensionssupport.chart.BarHandler
import io.docops.docopsextensionssupport.chart.LineHandler
import io.docops.docopsextensionssupport.chart.PieSliceHandler
import io.docops.docopsextensionssupport.diagram.*
import io.docops.docopsextensionssupport.metricscard.MetricsCardHandler
import io.docops.docopsextensionssupport.releasestrategy.ReleaseHandler
import io.docops.docopsextensionssupport.roadmap.RoadmapHandler
import io.docops.docopsextensionssupport.scorecard.ComparisonChartHandler
import io.docops.docopsextensionssupport.scorecard.ScorecardHandler
import io.docops.docopsextensionssupport.swimlane.SwimLaneHandler
import io.docops.docopsextensionssupport.svgsupport.addSvgMetadata
import io.docops.docopsextensionssupport.svgsupport.joinXmlLines
import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.docops.docopsextensionssupport.svgtable.TableHandler
import io.docops.docopsextensionssupport.timeline.TimelineHandler
import io.docops.docopsextensionssupport.wordcloud.WordCloudHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.sercasti.tracing.Traceable
import io.micrometer.core.annotation.Timed
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.net.URLDecoder
import kotlin.time.measureTimedValue

@Controller
@RequestMapping("/api/docops")
class DocOpsRouter (
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val badgeHandler: BadgeHandler) {


    private val logger = KotlinLogging.logger {}

    // Map to track execution counts for each event
    private val eventCounts = mutableMapOf<String, Int>()

    // Getter for eventCounts map
    fun getEventCounts(): Map<String, Int> = eventCounts.toMap()

    // Registry of handlers by kind
    private val handlers: Map<String, DocOpsHandler> = mapOf(
        "connector" to ConnectorHandler(),
        "placemat" to PlacematHandler(),
        "timeline" to TimelineHandler(),
        "scorecard" to ScorecardHandler(),
        "release" to ReleaseHandler(),
        "cal" to CalHandler(),
        "badge" to badgeHandler,
        "buttons" to ButtonHandler(),
        "adr" to AdrHandler(),
        "roadmap" to RoadmapHandler(),
        "pie" to PieHandler(),
        "pieslice" to PieSliceHandler(),
        "bar" to BarHandler(),
        "bargroup" to BarGroupHandler(),
        "line" to LineHandler(),
        "comp" to ComparisonChartHandler(),
        "table" to TableHandler(),
        "treechart" to TreeChartHandler(),
        "callout" to CalloutHandler(),
        "metricscard" to MetricsCardHandler(),
        "wordcloud" to WordCloudHandler(),
        "quadrant" to QuadrantHandler(),
        "swim" to SwimLaneHandler()
        // Add more handlers as needed
    )

    @Traceable
    @GetMapping("/svg")
    @Timed(value = "docops.router.svg", description="Creating a docops visual using http get", percentiles=[0.5, 0.9])
    fun getSvg(@RequestParam(value = "kind", required = true) kind: String,
               @RequestParam(name = "payload") payload: String,
               @RequestParam(name = "scale", defaultValue = "1.0") scale: String,
               @RequestParam("type", required = false, defaultValue = "SVG") type: String,
               @RequestParam("title", required = false, defaultValue = "") title: String,
               @RequestParam("numChars", required = false, defaultValue = "24") numChars: String,
               @RequestParam("useDark", defaultValue = "false") useDark: Boolean,
               @RequestParam("outlineColor", defaultValue = "#37cdbe") outlineColor: String,
               @RequestParam("backend", required = false, defaultValue = "html") backend: String
    ) : ResponseEntity<ByteArray> {
        val context = DocOpsContext(
            scale = scale,
            type = type,
            title = title,
            numChars = numChars,
            useDark = useDark,
            outlineColor = outlineColor,
            backend = backend
        )

        val headers = HttpHeaders()
        val handler = handlers[kind.lowercase()]
            ?: throw IllegalArgumentException("Unknown handler kind: $kind")
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val decodedPayload = decodePayloadIfNeeded(data)
            joinXmlLines(addSvgMetadata(handler.handleSVG(decodedPayload, context)))
        }
        logger.info { "$kind executed in ${timing.duration.inWholeMilliseconds}ms" }

        // Increment the count for this event
        val count = eventCounts.getOrDefault(kind, 0) + 1
        eventCounts[kind] = count

        // Publish the event with the count
        applicationEventPublisher.publishEvent(DocOpsExtensionEvent(kind, timing.duration.inWholeMilliseconds, true, count))
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        return ResponseEntity(timing.value.toByteArray(), headers, HttpStatus.OK)
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
            URLDecoder.decode(payload, "UTF-8")
        } else {
            payload
        }
    }

}
