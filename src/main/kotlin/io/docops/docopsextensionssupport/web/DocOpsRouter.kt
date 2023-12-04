package io.docops.docopsextensionssupport.web

import io.docops.docopsextensionssupport.ShapeResponse
import io.docops.docopsextensionssupport.diagram.ConnectorHandler
import io.docops.docopsextensionssupport.diagram.ConnectorMaker
import io.docops.docopsextensionssupport.diagram.Connectors
import io.docops.docopsextensionssupport.diagram.PlaceMatMaker
import io.docops.docopsextensionssupport.diagram.PlaceMatRequest
import io.docops.docopsextensionssupport.diagram.PlacematHandler
import io.docops.docopsextensionssupport.scorecard.ScorecardHandler
import io.docops.docopsextensionssupport.svgsupport.SvgToPng
import io.docops.docopsextensionssupport.timeline.TimelineHandler
import io.docops.docopsextensionssupport.web.panel.uncompressString
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import kotlin.time.measureTimedValue
import kotlinx.serialization.json.Json
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Controller
@RequestMapping("/api/docops")
class DocOpsRouter @Autowired constructor(private val meterRegistry: MeterRegistry) {

    private val connectorSvgCounter: Counter
    private val placematSvgCounter: Counter
    private val timelineSvgCounter: Counter
    private val connectorPngCounter: Counter
    private val placematPngCounter: Counter
    private val timelinePngCounter: Counter
    init {
        connectorSvgCounter = Counter.builder("connector.svg.counter").tag("connector", "svg").description("Count Number of times a Connector was created with SVG").register(meterRegistry)
        placematSvgCounter = Counter.builder("placemat.svg.counter").tag("placemat", "svg").description("Count Number of times a placemat was created with SVG").register(meterRegistry)
        timelineSvgCounter = Counter.builder("timeline.svg.counter").tag("timeline", "svg").description("Count Number of times a timeline was created with SVG").register(meterRegistry)
        connectorPngCounter = Counter.builder("connector.png.counter").tag("connector", "png").description("Count Number of times a Connector was created with PNG").register(meterRegistry)
        placematPngCounter = Counter.builder("placemat.png.counter").tag("placemat", "png").description("Count Number of times a placemat was created with PNG").register(meterRegistry)
        timelinePngCounter = Counter.builder("timeline.png.counter").tag("timeline", "png").description("Count Number of times a timeline was created with PNG").register(meterRegistry)
    }
    private val log = LogFactory.getLog(DocOpsRouter::class.java)
    @GetMapping("/svg")
    @Timed(value = "docops.router.svg", description="Creating a docops visual using http get", percentiles=[0.5, 0.9])
    fun getSvg(@RequestParam(value = "kind", required = true) kind: String,
               @RequestParam(name = "payload") payload: String,
               @RequestParam(name = "scale", defaultValue = "1.0") scale: String,
               @RequestParam("type", required = false, defaultValue = "SVG") type: String,
               @RequestParam("title", required = false, defaultValue = "title") title: String,
               @RequestParam("numChars", required = false, defaultValue = "35") numChars: String,
               @RequestParam(name = "useDark", defaultValue = "false") useDark: Boolean,
               @RequestParam(name = "outlineColor", defaultValue = "#37cdbe") outlineColor: String
    ) : ResponseEntity<ByteArray> {
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        if("connector".equals(kind, true)) {
            val timing = measureTimedValue {
                val handler = ConnectorHandler()
                handler.handleSVG(payload = payload, type, scale = scale, useDark = useDark)
            }
            log.info("getPlacemat executed in ${timing.duration.inWholeMilliseconds}ms ")
            connectorSvgCounter.increment()

            return timing.value
        }
        else if("placemat".equals(kind, true)) {
            val timing = measureTimedValue {
                val handler = PlacematHandler()
                handler.handleSVG(payload=payload, type = type)
            }
            placematSvgCounter.increment()
            log.info("getConnector executed in ${timing.duration.inWholeMilliseconds}ms ")
            return timing.value
        }
        else if("timeline".equals(kind, true)) {
            val timing = measureTimedValue {
                val handler = TimelineHandler()
                handler.handleSVG(payload, type= type, title = title, useDark = useDark, outlineColor = outlineColor, scale = scale, numChars = numChars)
            }
            timelineSvgCounter.increment()
            log.info("timeline executed in ${timing.duration.inWholeMilliseconds}ms ")
            return timing.value
        } else if("scorecard".equals(kind, true)) {
            val timing = measureTimedValue {
                val handler = ScorecardHandler()
                handler.handleSVG(payload)
            }
            log.info("scorecard executed in ${timing.duration.inWholeMilliseconds}ms ")
            return timing.value
        }
        return ResponseEntity("Not Found".toByteArray(), HttpStatus.NOT_FOUND)
    }



    @GetMapping("/png")
    @Timed(value = "docops.router.png", description="Creating a docops visual png using http get", percentiles=[0.5, 0.9])
    fun getPng(@RequestParam(value = "kind", required = true) kind: String,
               @RequestParam(name = "payload") payload: String,
               @RequestParam(name = "scale", defaultValue = "1.0") scale: String,
               @RequestParam("type", required = false, defaultValue = "PDF") type: String,
               @RequestParam("title", required = false, defaultValue = "title") title: String,
               @RequestParam("numChars", required = false, defaultValue = "35") numChars: String,
               @RequestParam(name = "useDark", defaultValue = "false") useDark: Boolean,
               @RequestParam(name = "outlineColor", required = false,defaultValue = "#37cdbe") outlineColor: String): ResponseEntity<ByteArray> {
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType("image", "png", StandardCharsets.UTF_8)
        if ("connector".equals(kind, true)) {
            val timing = measureTimedValue {
                val handler = ConnectorHandler()
                handler.handlePNG(payload = payload, type, scale = scale, useDark = useDark)
            }
            log.info("getConnector executed in ${timing.duration.inWholeMilliseconds}ms ")
            connectorPngCounter.increment()
            return timing.value
        }
        else if("placemat".equals(kind, true)) {
            val timing = measureTimedValue {
                val handler = PlacematHandler()
                handler.handlePNG(payload, type)
            }
            placematPngCounter.increment()

            log.info("getPlacemat executed in ${timing.duration.inWholeMilliseconds}ms ")
            return timing.value
        }
        else if("timeline".equals(kind, true)) {
            val timing = measureTimedValue {
                val handler = TimelineHandler()
                handler.handlePNG(payload, type= type, title = title, useDark = useDark, outlineColor = outlineColor, scale = scale, numChars = numChars)
            }
            log.info("timeline executed in ${timing.duration.inWholeMilliseconds}ms ")
            timelinePngCounter.increment()
            return timing.value
        }
        else if("scorecard".equals(kind, true)) {
            val timing = measureTimedValue {
                val handler = ScorecardHandler()
                handler.handlePNG(payload)
            }
            log.info("scorecard executed in ${timing.duration.inWholeMilliseconds}ms ")
            return timing.value
        }
        return ResponseEntity("Not Found".toByteArray(), HttpStatus.NOT_FOUND)
    }

}



