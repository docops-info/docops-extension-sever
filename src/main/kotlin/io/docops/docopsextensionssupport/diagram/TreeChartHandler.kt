package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import java.net.URLDecoder

class TreeChartHandler : DocOpsHandler{

    fun handleSVG(payload: String, isPdf: Boolean = false) : String {
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val treeMaker = TreeMaker()
        val svg = treeMaker.makeTree(data, isPdf)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, "pdf".equals(context.backend, ignoreCase = true))
    }
}