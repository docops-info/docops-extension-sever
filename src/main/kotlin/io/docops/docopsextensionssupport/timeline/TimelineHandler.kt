package io.docops.docopsextensionssupport.timeline

import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

class TimelineHandler : DocOpsHandler {
    fun handleSVG(
        payload: String,
        type: String,
        title: String,
        useDark: Boolean,
        outlineColor: String,
        scale: String,
        numChars: String,
        backend: String
    ): String {
        val isPdf = backend == "pdf"
        val tm = TimelineMaker(useDark = useDark, outlineColor = outlineColor)
        val svg = tm.makeTimelineSvg(payload, title, scale, isPdf = isPdf)
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")

        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(
            payload,
            context.type,
            context.title,
            context.useDark,
            context.outlineColor,
            context.scale,
            context.numChars,
            context.backend
        )
    }

}