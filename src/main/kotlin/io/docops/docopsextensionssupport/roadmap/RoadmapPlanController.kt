package io.docops.docopsextensionssupport.roadmap

import io.docops.docopsextensionssupport.badge.findHeightWidth
import io.docops.docopsextensionssupport.svgsupport.SvgToPng
import io.docops.docopsextensionssupport.web.panel.uncompressString
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.*
import java.net.URLDecoder
import java.nio.charset.Charset

@Controller
@RequestMapping("/api/roadmap")
class RoadmapPlanController {

    @PutMapping("/")
    @ResponseBody
    fun putRoadmapPlan(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        var contents = httpServletRequest.getParameter("content")
        if(contents.isNullOrEmpty()) {
            contents = StreamUtils.copyToString(httpServletRequest.inputStream, Charset.defaultCharset())
        }
        val rmm = RoadMapMaker()
        val svg = rmm.makeRoadMapImage(contents, "1.0")
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        return ResponseEntity(svg.toByteArray(),headers,HttpStatus.OK)
    }

    @GetMapping("/")
    @ResponseBody
    fun getRoadMap(@RequestParam(name = "payload") payload: String, @RequestParam(name="scale") scale: String, @RequestParam("type", required = false, defaultValue = "SVG") type: String): ResponseEntity<ByteArray> {
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val rmm = RoadMapMaker()
        val isPdf = "PDF" == type
        val svg = rmm.makeRoadMapImage(data, scale)
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