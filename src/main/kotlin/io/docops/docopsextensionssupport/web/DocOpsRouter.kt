package io.docops.docopsextensionssupport.web

import io.docops.docopsextensionssupport.adr.AdrHandler
import io.docops.docopsextensionssupport.badge.BadgeHandler
import io.docops.docopsextensionssupport.button.ButtonHandler
import io.docops.docopsextensionssupport.cal.CalHandler
import io.docops.docopsextensionssupport.chart.BarGroupHandler
import io.docops.docopsextensionssupport.chart.BarHandler
import io.docops.docopsextensionssupport.chart.LineHandler
import io.docops.docopsextensionssupport.chart.PieSliceHandler
import io.docops.docopsextensionssupport.diagram.ConnectorHandler
import io.docops.docopsextensionssupport.diagram.PieHandler
import io.docops.docopsextensionssupport.diagram.PlacematHandler
import io.docops.docopsextensionssupport.releasestrategy.ReleaseHandler
import io.docops.docopsextensionssupport.scorecard.ScorecardHandler
import io.docops.docopsextensionssupport.timeline.TimelineHandler
import io.docops.docopsextensionssupport.roadmap.RoadmapHandler
import io.docops.docopsextensionssupport.scorecard.ComparisonChartHandler
import io.github.sercasti.tracing.Traceable
import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.MeterRegistry
import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import kotlin.time.measureTimedValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

@Controller
@RequestMapping("/api/docops")
class DocOpsRouter @Autowired constructor(private val meterRegistry: MeterRegistry, private val applicationEventPublisher: ApplicationEventPublisher) {


    private var log = LogFactory.getLog(DocOpsRouter::class.java)

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
        val headers = HttpHeaders()
        if(null == log) {
            log = LogFactory.getLog(DocOpsRouter::class.java)
        }

        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        if("connector".equals(kind, true)) {
            val timing = measureTimedValue {
                val handler = ConnectorHandler()
                handler.handleSVG(payload = payload, type, scale = scale, useDark = useDark)
            }

            applicationEventPublisher.publishEvent(DocOpsExtensionEvent("connector", timing.duration.inWholeMilliseconds))
            return timing.value
        }
        else if("placemat".equals(kind, true)) {
            val timing = measureTimedValue {
                val handler = PlacematHandler()
                handler.handleSVG(payload=payload, type = type, backend = backend)
            }
            log.info("getConnector executed in ${timing.duration.inWholeMilliseconds}ms")
            applicationEventPublisher.publishEvent(DocOpsExtensionEvent("placemat", timing.duration.inWholeMilliseconds))
            return timing.value
        }
        else if("timeline".equals(kind, true)) {
            val timing = measureTimedValue {
                val handler = TimelineHandler()
                handler.handleSVG(payload, type= type, title = title, useDark = useDark, outlineColor = outlineColor, scale = scale, numChars = numChars, backend = backend)
            }
            log.info("timeline executed in ${timing.duration.inWholeMilliseconds}ms")
            applicationEventPublisher.publishEvent(DocOpsExtensionEvent("timeline", timing.duration.inWholeMilliseconds))

            return timing.value
        } else if("scorecard".equals(kind, true)) {
            val timing = measureTimedValue {
                val handler = ScorecardHandler()
                handler.handleSVG(payload, backend)
            }
            log.info("scorecard executed in ${timing.duration.inWholeMilliseconds}ms")
            applicationEventPublisher.publishEvent(DocOpsExtensionEvent("scorecard", timing.duration.inWholeMilliseconds))

            return timing.value
        } else if("roadmap".equals(kind, true)) {
            val timing = measureTimedValue {
                val handler = RoadmapHandler()
                handler.handleSVG(payload, useDark = useDark, type = type, title = title, scale = scale, numChars = numChars)
            }
            log.info("roadmap executed in ${timing.duration.inWholeMilliseconds}ms")
            applicationEventPublisher.publishEvent(DocOpsExtensionEvent("roadmap", timing.duration.inWholeMilliseconds))

            return timing.value
        }else if ("buttons".equals(kind, true)) {
            val timing = measureTimedValue {
                val handler = ButtonHandler()
                handler.handleSVG(payload, useDark = useDark, type = type, backend = backend)
            }
            log.info("buttons executed in ${timing.duration.inWholeMilliseconds}ms")
            applicationEventPublisher.publishEvent(DocOpsExtensionEvent("buttons", timing.duration.inWholeMilliseconds))

            return timing.value
        }else if ("release".equals(kind, true)) {
            val timing = measureTimedValue {
                val handler = ReleaseHandler()
                handler.handleSVG(payload, useDark = useDark, backend)
            }
            log.info("release executed in ${timing.duration.inWholeMilliseconds}ms")
            applicationEventPublisher.publishEvent(DocOpsExtensionEvent("release", timing.duration.inWholeMilliseconds))

            return timing.value
        }
        else if("adr".equals(kind, ignoreCase = true)) {
            val timing = measureTimedValue {
                val handler = AdrHandler()
                handler.handleSVG(payload = payload, scale = scale, useDark = useDark, backEnd = backend)
            }
            log.info("adr executed in ${timing.duration.inWholeMilliseconds}ms")
            applicationEventPublisher.publishEvent(DocOpsExtensionEvent("adr", timing.duration.inWholeMilliseconds))

            return timing.value
        }
        else if("badge".equals(kind, ignoreCase = true)) {
            val timing = measureTimedValue {
                val handler = BadgeHandler()
                handler.handleSVG(payload=payload, backend = backend)
            }
            log.info("buttons executed in ${timing.duration.inWholeMilliseconds}ms")
            applicationEventPublisher.publishEvent(DocOpsExtensionEvent("badge", timing.duration.inWholeMilliseconds))

            return timing.value
        }
        else if("cal".equals(kind, ignoreCase = true)) {
            val timing = measureTimedValue {
                val handler = CalHandler()
                handler.handleSVG(payload=payload)
            }
            log.info("calendar executed in ${timing.duration.inWholeMilliseconds}ms")
            applicationEventPublisher.publishEvent(DocOpsExtensionEvent("calendar", timing.duration.inWholeMilliseconds))

            return timing.value
        }
        else if("pie".equals(kind, ignoreCase = true)) {
            val timing = measureTimedValue {
                val handler = PieHandler()
                handler.handleSVG(payload=payload)
            }
            log.info("pie handler executed in ${timing.duration.inWholeMilliseconds}ms")
            applicationEventPublisher.publishEvent(DocOpsExtensionEvent("pie", timing.duration.inWholeMilliseconds))

            return timing.value
        }
        else if("pieslice".equals(kind, ignoreCase = true)) {
            val timing = measureTimedValue {
                val handler = PieSliceHandler()
                handler.handleSVG(payload=payload)
            }
            log.info("pie handler executed in ${timing.duration.inWholeMilliseconds}ms")
            applicationEventPublisher.publishEvent(DocOpsExtensionEvent("pieslice", timing.duration.inWholeMilliseconds))

            return timing.value
        }
        else if("bar".equals(kind, ignoreCase = true)) {
            val timing = measureTimedValue {
                val handler = BarHandler()
                handler.handleSVG(payload=payload)
            }
            log.info("bar handler executed in ${timing.duration.inWholeMilliseconds}ms")
            applicationEventPublisher.publishEvent(DocOpsExtensionEvent("bar", timing.duration.inWholeMilliseconds))

            return timing.value
        }
        else if("bargroup".equals(kind, ignoreCase = true)) {
            val timing = measureTimedValue {
                val handler = BarGroupHandler()
                handler.handleSVG(payload=payload)
            }
            log.info("bar handler executed in ${timing.duration.inWholeMilliseconds}ms")
            applicationEventPublisher.publishEvent(DocOpsExtensionEvent("bargroup", timing.duration.inWholeMilliseconds))

            return timing.value
        }
        else if("line".equals(kind, ignoreCase = true)) {
            val timing = measureTimedValue {
                val handler = LineHandler()
                handler.handleSVG(payload=payload, backend.equals("PDF", true))
            }
            log.info("line handler executed in ${timing.duration.inWholeMilliseconds}ms")
            applicationEventPublisher.publishEvent(DocOpsExtensionEvent("line", timing.duration.inWholeMilliseconds))

            return timing.value
        }
        else if("comp".equals(kind, ignoreCase = true)) {
            val timing = measureTimedValue {
                val handler = ComparisonChartHandler()
                handler.handleSVG(payload=payload)
            }
            log.info("line handler executed in ${timing.duration.inWholeMilliseconds}ms")
            applicationEventPublisher.publishEvent(DocOpsExtensionEvent("comparison", timing.duration.inWholeMilliseconds))

            return timing.value
        }
        return ResponseEntity("$kind Not Found".toByteArray(), HttpStatus.NOT_FOUND)
    }


}





