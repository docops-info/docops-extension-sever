package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.diagram.treechart.CyberTreeMaker
import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext

class TreeChartHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse){

    fun handleSVG(payload: String, isPdf: Boolean = false, useDark: Boolean = false) : String {
        val treeMaker = CyberTreeMaker(useDark)
        val svg = treeMaker.makeTree(payload,  csvResponse)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, "pdf".equals(context.backend, ignoreCase = true), context.useDark)
    }
}