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
        useGlass: Boolean = false
    ): String {
        val isPdf = backend == "pdf"
        val entries = TimelineParser().parse(payload)
        val orientation = entries.config.orientation?.let { TimelineOrientation.valueOf(it.uppercase()) }

        val tm = TimelineMaker(useDark = entries.config.useDark, useGlass = entries.config.useGlass, orientation = orientation ?: TimelineOrientation.HORIZONTAL, enableDetailView = entries.config.enableDetailView)
        var ti: String

        if(entries.config.title.isNullOrEmpty()) {
            ti = title
        } else {
            ti = entries.config.title
        }

        val svg = tm.makeTimelineSvg(entries.entries, ti, scale, isPdf = isPdf)
        val csv = entries.entries.toCsv()
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
