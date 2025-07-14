package io.docops.docopsextensionssupport.wordcloud

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler

class WordCloudHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {

    fun handleSVG(payload: String): String {
        val wordCloudImproved = WordCloudImproved()
        val svg = wordCloudImproved.makeWordCloudSvg(payload, csvResponse)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload)
    }
}