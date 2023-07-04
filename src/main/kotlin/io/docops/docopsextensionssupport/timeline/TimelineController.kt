package io.docops.docopsextensionssupport.timeline

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/api/timeline")
class TimelineController {

    @PutMapping("/")
    @ResponseBody
    fun putTimeline(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val contents = httpServletRequest.getParameter("content")
        val tm = TimelineMaker()
        val svg = tm.makeTimelineSvg(contents)
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        return ResponseEntity(svg.toByteArray(),headers,HttpStatus.OK)
    }
}