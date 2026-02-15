package io.docops.docopsextensionssupport.vcard

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.viz.vcard.VCardSvgGeneratorService


class VCardHandler(csvResponse: CsvResponse): BaseDocOpsHandler(csvResponse) {


    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        val parser = VCardParserService()
        val parsedVCard = parser.parseVCardInput(payload)
        val vCard = parser.parseVCard(parsedVCard.vcardContent)
        val generator = VCardSvgGeneratorService(context.useDark)
        return generator.generateSvg(vCard, parsedVCard.config)
    }
}