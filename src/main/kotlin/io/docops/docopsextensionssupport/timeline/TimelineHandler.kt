package io.docops.docopsextensionssupport.timeline

import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler

class TimelineHandler : DocOpsHandler {
    fun handleSVG(
        payload: String,
        type: String,
        title: String,
        useDark: Boolean,
        scale: String,
        backend: String
    ): String {
        val isPdf = backend == "pdf"
        val tm = TimelineMaker(useDark = useDark)
        val svg = tm.makeTimelineSvg(payload, title, scale, isPdf = isPdf)
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
            context.scale,
            context.backend
        )
    }

}