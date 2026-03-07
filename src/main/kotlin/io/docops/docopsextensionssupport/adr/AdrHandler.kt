package io.docops.docopsextensionssupport.adr

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update

class AdrHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse){

    fun handleSVG(payload: String, scale: String, useDark: Boolean, backEnd: String): String {
        val adr = AdrParser().parseContent(payload)

        val isBrutalist = adr.template == "brutalist"
        val svg = if (!isBrutalist) {
            val generator = AdrSvgGenerator(useDark, adr.theme)
            generator.generate(adr, width = 700)
        } else {
            val generator = CyberBrutalistAdrSvgGenerator(useDark = useDark, themeName = adr.theme)
            generator.generate(adr)
        }
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