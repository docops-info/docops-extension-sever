package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.sercasti.tracing.Traceable
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.*
import java.net.URLDecoder
import java.nio.charset.Charset
import kotlin.time.measureTimedValue

@Controller
@RequestMapping("/api/placemat")
class PlaceMatController {
    private val log = KotlinLogging.logger {  }
    @Traceable
    @PutMapping("/")
    @ResponseBody
    @Counted(value="docops.placemat.put", description="Creating a Placemat using http put")
    @Timed(value = "docops.placemat.put", description="Creating a Placemat using http put", percentiles=[0.5, 0.9])
    fun makeDiag(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            var contents = httpServletRequest.getParameter("content")
            var title = "title"
            if (contents.isNullOrEmpty()) {
                contents = StreamUtils.copyToString(httpServletRequest.inputStream, Charset.defaultCharset())
                title = httpServletRequest.getParameter("title")
            }
            val useDarkInput = httpServletRequest.getParameter("useDark")
            val svg = fromRequestToPlaceMat(contents = contents, useDark = "on" == useDarkInput)
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("text/html")
            val div = """
                <div id='imageblock'>
                $svg
                </div>
                <script>
                var adrSource = `[diag,scale="0.7",role="center"]\n----\n${contents}\n----`;
                </script>
        """.lines().joinToString(transform = String::trim, separator = "\n")
            ResponseEntity(div.toByteArray(), headers, HttpStatus.OK)
        }

        return timings.value
    }

    fun fromRequestToPlaceMat(contents: String, useDark: Boolean): String {
        val pms = Json.decodeFromString<PlaceMatRequest>(contents)
        pms.useDark = useDark
        val maker = PlaceMatMaker(placeMatRequest = pms)
        return maker.makePlacerMat().shapeSvg
    }

    @Traceable
    @GetMapping("/")
    @ResponseBody
    @Counted(value = "docops.placemat.get", description="Creating a placemat diagram using http get")
    @Timed(value = "docops.placemat.get", description="Creating a placemat diagram using http get", percentiles=[0.5, 0.9])
    fun getConnector(
        @RequestParam(name = "payload") payload: String,
        @RequestParam(name = "scale", defaultValue = "1.0") scale: String,
        @RequestParam("type", required = false, defaultValue = "SVG") type: String,
        @RequestParam(name = "useDark", defaultValue = "false") useDark: Boolean,
        @RequestParam(name = "outlineColor", defaultValue = "#37cdbe") outlineColor: String
    ): ResponseEntity<ByteArray> {
        val timing = measureTimedValue {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val svg = fromRequestToPlaceMat(data, useDark = useDark)
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("image/svg+xml")
            ResponseEntity(svg.toByteArray(), headers, HttpStatus.OK)

        }
        log.info{"getConnector executed in ${timing.duration.inWholeMilliseconds}ms "}
        return timing.value
    }
}