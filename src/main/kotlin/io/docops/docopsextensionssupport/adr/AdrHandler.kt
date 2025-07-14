package io.docops.docopsextensionssupport.adr

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvRequest
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import io.docops.docopsextensionssupport.web.update

class AdrHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse){

    fun handleSVG(payload: String, scale: String, useDark: Boolean, backEnd: String): String {
        val generator = AdrSvgGenerator()
        val adr = AdrParser().parseContent(payload)
        val svg = generator.generate(adr, width = 700)
        csvResponse.update(adr.toCsv())
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, context.scale, context.useDark, context.backend)
    }

}