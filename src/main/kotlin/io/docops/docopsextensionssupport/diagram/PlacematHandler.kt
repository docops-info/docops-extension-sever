package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import io.docops.docopsextensionssupport.web.ShapeResponse
import kotlinx.serialization.json.Json
import java.net.URLDecoder

class PlacematHandler : DocOpsHandler{
    fun handleSVG(payload: String, type: String, backend: String): String {
        val isPDF = backend == "pdf"
        val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
        val svg = fromRequestToPlaceMat(data, type, isPDF)
        return svg.shapeSvg
    }

    fun fromRequestToPlaceMat(contents: String, type: String = "SVG", isPDF: Boolean): ShapeResponse {
        val pms = Json.decodeFromString<PlaceMatRequest>(contents)
        val maker = PlaceMatMaker(placeMatRequest = pms, type, isPDF)
        return maker.makePlacerMat()
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, context.type, context.backend)
    }
}