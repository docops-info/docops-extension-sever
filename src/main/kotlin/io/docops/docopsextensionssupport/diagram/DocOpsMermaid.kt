package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext

class DocOpsMermaid(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {
    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return """
            <div class="diagram-container">
                <div class="mermaid">
                    ${payload}
                </div>
            </div>

        """.trimIndent()
    }
}