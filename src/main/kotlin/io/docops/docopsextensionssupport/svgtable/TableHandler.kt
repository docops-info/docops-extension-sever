package io.docops.docopsextensionssupport.svgtable

import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import kotlinx.serialization.json.Json

class TableHandler : DocOpsHandler{

    fun handleSVG(payload: String) : String {
        val table = Json.decodeFromString<Table>(payload)
        val svg = table.toSvg()
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload)
    }
}