package io.docops.docopsextensionssupport.timeline

import io.docops.docopsextensionssupport.web.CsvRequest
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler

class TimelineHandler : DocOpsHandler {
    fun handleSVG(
        payload: String,
        type: String,
        title: String,
        useDark: Boolean,
        scale: String,
        backend: String,
        useGlass: Boolean = true
    ): String {
        val isPdf = backend == "pdf"
        val tm = TimelineMaker(useDark = useDark, useGlass = useGlass)
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
            context.backend,
            context.useGlass
        )
    }

    override fun toCsv(request: CsvRequest): CsvResponse {
        val entries = TimelineParser().parse(request.content)
        return entries.toCsv()
    }
}
