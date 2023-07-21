package io.docops.docopsextensionssupport.roadmap

import io.docops.docopsextensionssupport.badge.findHeightWidth
import io.docops.docopsextensionssupport.svgsupport.SvgToPng
import io.docops.docopsextensionssupport.web.panel.uncompressString
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
class RoadmapPlanController {
    val log = LogFactory.getLog(RoadmapPlanController::class.java)
    @PutMapping("/")
    @ResponseBody
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
        val rmm = RoadMapMaker()
        val svg = rmm.makeRoadMapImage(contents, scale, title, chars)
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        return ResponseEntity(svg.toByteArray(),headers,HttpStatus.OK)
    }

    @GetMapping("/")
    @ResponseBody
    fun getRoadMap(@RequestParam(name = "payload") payload: String,
                   @RequestParam(name="scale") scale: String,
                   @RequestParam("type", required = false, defaultValue = "SVG") type: String,
                   @RequestParam("title", required = false) title: String,
                   @RequestParam("numChars", required = false, defaultValue = "30") numChars: String
    )
                        : ResponseEntity<ByteArray> {
        log.info("compressed data received $payload")
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        log.info("Received from plugin > $data")
        val rmm = RoadMapMaker()
        val isPdf = "PDF" == type
        val svg = rmm.makeRoadMapImage(data, scale, title, numChars)
        log.info(svg)
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