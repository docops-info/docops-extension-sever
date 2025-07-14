package io.docops.docopsextensionssupport.timeline

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update

class TimelineHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {
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
        val entries = TimelineParser().parse(payload)
        val svg = tm.makeTimelineSvg(entries, title, scale, isPdf = isPdf)
        val csv = entries.toCsv()
        csvResponse.update(csv)
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

}
