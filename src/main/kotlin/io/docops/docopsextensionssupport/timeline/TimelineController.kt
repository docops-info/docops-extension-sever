package io.docops.docopsextensionssupport.timeline

import io.docops.docopsextensionssupport.badge.findHeightWidth
import io.docops.docopsextensionssupport.svgsupport.SvgToPng
import io.docops.docopsextensionssupport.web.panel.uncompressString
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.net.URLDecoder
import java.nio.charset.Charset

@Controller
@RequestMapping("/api/timeline")
class TimelineController {

    @PutMapping("/")
    @ResponseBody
    fun putTimeline(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        var title = "title"
        var contents = httpServletRequest.getParameter("content")
        if(contents.isNullOrEmpty()) {
            contents = StreamUtils.copyToString(httpServletRequest.inputStream, Charset.defaultCharset())
             title = httpServletRequest.getParameter("title")
        }
        val tm = TimelineMaker()
        val svg = tm.makeTimelineSvg(contents, title, "1.0", false)
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        return ResponseEntity(svg.toByteArray(),headers,HttpStatus.OK)
    }

    @GetMapping("/")
    @ResponseBody
    fun getTimeLine(@RequestParam(name = "payload") payload: String, @RequestParam(name="title") title: String, @RequestParam(name="scale") scale: String, @RequestParam("type", required = false, defaultValue = "SVG") type: String): ResponseEntity<ByteArray> {
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val tm = TimelineMaker()
        val isPdf = "PDF" == type
        val svg = tm.makeTimelineSvg(data, title, scale, isPdf)
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
            ResponseEntity(svg.toByteArray(),headers,HttpStatus.OK)
        }
    }

    @GetMapping("/table")
    @ResponseBody
    fun getTimeLineTable(@RequestParam(name = "payload") payload: String, @RequestParam(name="title") title: String): ResponseEntity<ByteArray> {
        val data = uncompressString(URLDecoder.decode(payload,"UTF-8"))
        val tm = TimelineParser()
        val entries = tm.parse(data)
        val sb = StringBuilder(".$title\n")
        sb.append("[%header,cols=\"1,2\",stripes=even]\n")
        sb.append("!===\n")
        sb.append("|Date |Event\n")
        entries.forEach {
            sb.append("a|${it.date} |${it.text}\n")
        }
        sb.append("!===")
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.TEXT_PLAIN
        return ResponseEntity(sb.toString().toByteArray(),headers,HttpStatus.OK)
    }
}