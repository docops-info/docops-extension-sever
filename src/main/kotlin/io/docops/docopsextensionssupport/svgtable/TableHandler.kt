package io.docops.docopsextensionssupport.svgtable

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update
import kotlinx.serialization.json.Json

class TableHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse){

    fun handleSVG(payload: String) : String {
        val table = Json.decodeFromString<Table>(payload)
        val svg = table.toSvg()
        csvResponse.update(table.toCsv())
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload)
    }
}