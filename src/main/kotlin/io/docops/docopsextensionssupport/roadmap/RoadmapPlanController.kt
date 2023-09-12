package io.docops.docopsextensionssupport.roadmap

import io.docops.asciidoctorj.extension.adr.compressString
import io.docops.docopsextensionssupport.badge.findHeightWidth
import io.docops.docopsextensionssupport.svgsupport.SvgToPng
import io.docops.docopsextensionssupport.web.panel.uncompressString
import io.micrometer.core.annotation.Timed
import io.micrometer.observation.annotation.Observed
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.logging.LogFactory
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.*
import java.net.URLDecoder
import java.nio.charset.Charset

@Controller
@RequestMapping("/api/roadmap")
@Observed(name = "roadmap.controller")
class RoadmapPlanController {
    val log = LogFactory.getLog(RoadmapPlanController::class.java)
    @PutMapping("/")
    @ResponseBody
    @Timed(value = "docops.roadmap.put.html", histogram = true, percentiles = [0.5, 0.95])
    fun putRoadmapPlan(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        var contents = httpServletRequest.getParameter("content")
        if(contents.isNullOrEmpty()) {
            contents = StreamUtils.copyToString(httpServletRequest.inputStream, Charset.defaultCharset())
        }
        val scale = httpServletRequest.getParameter("scale")
        val title = httpServletRequest.getParameter("title")
        val numChars = httpServletRequest.getParameter("numChars")
        var chars = numChars
        if(numChars == null || numChars.isEmpty()) {
            chars = "32"
        }
        val useDarkInput = httpServletRequest.getParameter("useDark")
        val rmm = RoadMapMaker("on".equals(useDarkInput))
        val svg = rmm.makeRoadMapImage(contents, scale, title, chars)
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("text/html")
        val div = """
            <div>$svg</div>
            <div class="pure-u-1">
                Copy Url: <a href="api/roadmap/?payload=${compressString(contents)}&title=$title&numChars=$numChars&scale=$scale&type=svg" target="_blank">Open Url</a>
            </div>
        """.trimIndent()
        return ResponseEntity(div.toByteArray(),headers,HttpStatus.OK)
    }

    @GetMapping("/")
    @ResponseBody
    @Timed(value = "docops.roadmap.get.html", histogram = true, percentiles = [0.5, 0.95])
    fun getRoadMap(@RequestParam(name = "payload") payload: String,
                   @RequestParam(name="scale") scale: String,
                   @RequestParam("type", required = false, defaultValue = "SVG") type: String,
                   @RequestParam("title", required = false) title: String,
                   @RequestParam("numChars", required = false, defaultValue = "30",) numChars: String,
                   @RequestParam(name="useDark", defaultValue = "false") useDark: Boolean
    )
                        : ResponseEntity<ByteArray> {
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val rmm = RoadMapMaker(useDark)
        val isPdf = "PDF" == type
        val svg = rmm.makeRoadMapImage(data, scale, title, numChars)
        return if(isPdf) {
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.IMAGE_PNG
            val res = findHeightWidth(svg)
            val baos = SvgToPng().toPngFromSvg(svg, res)
            ResponseEntity(baos, headers, HttpStatus.OK)
        } else {
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("image/svg+xml")
            ResponseEntity(svg.toByteArray(),headers, HttpStatus.OK)
        }
    }

}