package io.docops.docopsextensionssupport.badge

import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler

class ShieldHandler: DocOpsHandler {
    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        val parser = ShieldTableParser()
        val (shields, config) = parser.parseShieldTable(payload)
        val generator = ShieldSvgGenerator()
        return generator.generateShieldsTable(
            shields = shields, 
            config = config ?: ShieldTableConfig()
        )
    }
}
