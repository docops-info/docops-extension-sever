package io.docops.docopsextensionssupport.wordcloud

import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler

class WordCloudHandler : DocOpsHandler {

    fun handleSVG(payload: String): String {
        val wordCloudImproved = WordCloudImproved()
        val svg = wordCloudImproved.makeWordCloudSvg(payload)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload)
    }
}