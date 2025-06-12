package io.docops.docopsextensionssupport.svgtable

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import kotlinx.serialization.json.Json
import org.springframework.http.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class TableHandler : DocOpsHandler{

    fun handleSVG(payload: String) : String {
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val table = Json.decodeFromString<Table>(data)
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