package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler

class TreeChartHandler : DocOpsHandler{

    fun handleSVG(payload: String, isPdf: Boolean = false) : String {
        val treeMaker = TreeMaker()
        val svg = treeMaker.makeTree(payload, isPdf)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, "pdf".equals(context.backend, ignoreCase = true))
    }
}