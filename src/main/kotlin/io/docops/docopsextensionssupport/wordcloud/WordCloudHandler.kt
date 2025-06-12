package io.docops.docopsextensionssupport.wordcloud

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import org.springframework.http.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class WordCloudHandler : DocOpsHandler {

    fun handleSVG(payload: String): String {
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val wordCloudImproved = WordCloudImproved()
        val svg = wordCloudImproved.makeWordCloudSvg(data)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload)
    }
}