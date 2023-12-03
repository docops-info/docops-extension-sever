package io.docops.docopsextensionssupport.web

import io.docops.docopsextensionssupport.ShapeResponse
import io.docops.docopsextensionssupport.diagram.ConnectorMaker
import io.docops.docopsextensionssupport.diagram.Connectors
import io.docops.docopsextensionssupport.diagram.PlaceMatMaker
import io.docops.docopsextensionssupport.diagram.PlaceMatRequest
import io.docops.docopsextensionssupport.svgsupport.SvgToPng
import io.docops.docopsextensionssupport.web.panel.uncompressString
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import kotlin.time.measureTimedValue
import kotlinx.serialization.json.Json
import org.apache.commons.logging.LogFactory
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Controller
@RequestMapping("/api/docops")
class DocOpsRouter {

    private val log = LogFactory.getLog(DocOpsRouter::class.java)
    @GetMapping("/svg")
    @Counted(value="docops.router.get", description="Creating a docops visual using http get")
    @Timed(value = "docops.router.get", description="Creating a docops visual using http get", percentiles=[0.5, 0.9])
    fun getSvg(@RequestParam(value = "kind", required = true) kind: String,
               @RequestParam(name = "payload") payload: String,
               @RequestParam(name = "scale", defaultValue = "1.0") scale: String,
               @RequestParam("type", required = false, defaultValue = "SVG") type: String,
               @RequestParam(name = "useDark", defaultValue = "false") useDark: Boolean,
               @RequestParam(name = "outlineColor", defaultValue = "#37cdbe") outlineColor: String
    ) : ResponseEntity<ByteArray> {
        if("connector".equals(kind, true)) {
            val timing = measureTimedValue {
                val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
                val svg = fromRequestToConnector(data, scale = scale.toFloat(), useDark = useDark)
                val headers = HttpHeaders()
                headers.cacheControl = CacheControl.noCache().headerValue
                headers.contentType = MediaType.parseMediaType("image/svg+xml")
                ResponseEntity(svg.shapeSvg.toByteArray(), headers, HttpStatus.OK)

            }
            log.info("getPlacemat executed in ${timing.duration.inWholeMilliseconds}ms ")
            return timing.value
        }
        else if("placemat".equals(kind, true)) {
            val timing = measureTimedValue {
                val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
                val svg = fromRequestToPlaceMat(data, type)
                val headers = HttpHeaders()
                headers.cacheControl = CacheControl.noCache().headerValue
                headers.contentType = MediaType.parseMediaType("image/svg+xml")
                ResponseEntity(svg.shapeSvg.toByteArray(), headers, HttpStatus.OK)

            }
            log.info("getConnector executed in ${timing.duration.inWholeMilliseconds}ms ")
            return timing.value
        }

        return ResponseEntity("Not Found".toByteArray(), HttpStatus.NOT_FOUND)
    }


    fun fromRequestToConnector(contents: String, scale: Float, useDark: Boolean, type: String = "SVG"): ShapeResponse {
        val connectors = Json.decodeFromString<Connectors>(contents)
        val maker = ConnectorMaker(connectors = connectors.connectors, useDark = useDark, type)
        val svg = maker.makeConnectorImage(scale = scale)
        return svg
    }

    @GetMapping("/png")
    @Counted(value="docops.router.get", description="Creating a docops visual png using http get")
    @Timed(value = "docops.router.get", description="Creating a docops visual png using http get", percentiles=[0.5, 0.9])
    fun getPng(@RequestParam(value = "kind", required = true) kind: String,
               @RequestParam(name = "payload") payload: String,
               @RequestParam(name = "scale", defaultValue = "1.0") scale: String,
               @RequestParam("type", required = false, defaultValue = "PDF") type: String,
               @RequestParam(name = "useDark", defaultValue = "false") useDark: Boolean,
               @RequestParam(name = "outlineColor", defaultValue = "#37cdbe") outlineColor: String): ResponseEntity<ByteArray> {
        if ("connector".equals(kind, true)) {
            val timing = measureTimedValue {
                val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
                val svg = fromRequestToConnector(
                    data,
                    scale = scale.toFloat(),
                    useDark = useDark,
                    type = type
                )
                val headers = HttpHeaders()
                headers.cacheControl = CacheControl.noCache().headerValue
                headers.contentType = MediaType("image", "png", StandardCharsets.UTF_8)
                val png = SvgToPng().toPngFromSvg(
                    svg.shapeSvg,
                    Pair(svg.height.toString(), svg.width.toString())
                )
                ResponseEntity(png, headers, HttpStatus.OK)

            }
            log.info("getConnector executed in ${timing.duration.inWholeMilliseconds}ms ")
            return timing.value
        }
        else if("placemat".equals(kind, true)) {
            val timing = measureTimedValue {
                val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
                val svg = fromRequestToPlaceMat(data, type)
                val headers = HttpHeaders()
                headers.cacheControl = CacheControl.noCache().headerValue
                headers.contentType = MediaType("image", "png", StandardCharsets.UTF_8)
                val png = SvgToPng().toPngFromSvg(
                    svg.shapeSvg,
                    Pair(svg.height.toString(), svg.width.toString())
                )
                ResponseEntity(png, headers, HttpStatus.OK)

            }
            log.info("getPlacemat executed in ${timing.duration.inWholeMilliseconds}ms ")
            return timing.value
        }
        return ResponseEntity("Not Found".toByteArray(), HttpStatus.NOT_FOUND)
    }
    fun fromRequestToPlaceMat(contents: String,  type: String = "SVG"): ShapeResponse {
        val pms = Json.decodeFromString<PlaceMatRequest>(contents)
        val maker = PlaceMatMaker(placeMatRequest = pms, type)
        return maker.makePlacerMat()
    }
}