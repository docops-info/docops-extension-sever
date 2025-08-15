package io.docops.docopsextensionssupport.todo

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext

/**
 * Handler for todo list visualizations
 */
class TodoHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {

    /**
     * Handles SVG generation for todo lists
     */
    override fun handleSVG(payload: String, context: DocOpsContext): String {
        return makeTodoSvg(payload, context)
    }

    private fun makeTodoSvg(payload: String, context: DocOpsContext): String {
        val maker = TodoMaker(csvResponse)
        
        return if (isTableFormat(payload)) {
            maker.createFromTable(payload, context)
        } else {
            maker.createFromJson(payload, context)
        }
    }

    /**
     * Determines if the data is in table format
     */
    private fun isTableFormat(data: String): Boolean {
        return data.contains("---") || (!data.trim().startsWith("{") && !data.trim().startsWith("["))
    }
}
