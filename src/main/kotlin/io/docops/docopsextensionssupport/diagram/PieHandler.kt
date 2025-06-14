package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import kotlinx.serialization.json.Json

class PieHandler : DocOpsHandler {

    fun handleSVG(payload: String) : String {
        val calMaker = PieMaker()
        val pies = Json.decodeFromString<Pies>(payload)
        val svg = calMaker.makePies(pies)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload)
    }
}