package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler

class TreeChartHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse){

    fun handleSVG(payload: String, isPdf: Boolean = false) : String {
        val treeMaker = TreeMaker()
        val svg = treeMaker.makeTree(payload, isPdf, csvResponse)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, "pdf".equals(context.backend, ignoreCase = true))
    }
}