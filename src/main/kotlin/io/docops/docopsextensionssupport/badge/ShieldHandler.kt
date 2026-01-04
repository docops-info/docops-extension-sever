package io.docops.docopsextensionssupport.badge

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update

class ShieldHandler(csvResponse: CsvResponse): BaseDocOpsHandler(csvResponse) {
    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        val parser = ShieldTableParser()
        val (shields, config) = parser.parseShieldTable(payload)
        csvResponse.update(shields.toCsv())
        val generator = ShieldSvgGenerator(context.useDark)
        return generator.generateShieldsTable(
            shields = shields, 
            config = config ?: ShieldTableConfig()
        )
    }
}
