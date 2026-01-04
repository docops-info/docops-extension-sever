package io.docops.docopsextensionssupport.wordcloud

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext

class WordCloudHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {

    fun handleSVG(payload: String, useDark: Boolean = false): String {
        val wordCloudImproved = WordCloudImproved(useDark)
        val svg = wordCloudImproved.makeWordCloudSvg(payload, csvResponse)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, context.useDark)
    }
}