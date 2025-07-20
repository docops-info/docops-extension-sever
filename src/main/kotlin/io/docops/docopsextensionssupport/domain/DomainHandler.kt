package io.docops.docopsextensionssupport.domain

import io.docops.docopsextensionssupport.domain.model.toCsv
import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update

class DomainHandler(csvResponse: CsvResponse): BaseDocOpsHandler(csvResponse) {
    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        val parser = MarkupParser()
        val (structure, config) = parser.parseMarkup(payload)

        val generator = SvgGenerator()
        val svg = generator.generateSvg(structure, config.useGradients, config.useGlass)
        csvResponse.update(structure.toCsv())
        return svg
    }
}