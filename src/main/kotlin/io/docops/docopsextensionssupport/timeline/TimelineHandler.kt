package io.docops.docopsextensionssupport.timeline

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update
import io.docops.extension.wasm.timeline.TimelineMaker

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
        val parser = TimelineParser()
        val config = parser.parse(payload)
        val maker = TimelineMaker()
        val svg = maker.makeSvg(config, useDark, scale = scale )
        csvResponse.update(config.timelineEventsToCsv())
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
