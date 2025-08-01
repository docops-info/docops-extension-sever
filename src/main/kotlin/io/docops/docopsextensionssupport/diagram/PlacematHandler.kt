package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.web.*
import kotlinx.serialization.json.Json

class PlacematHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse)  {
    fun handleSVG(payload: String, type: String, backend: String): String {
        val isPDF = backend == "pdf"
        val svg = fromRequestToPlaceMat(payload, type, isPDF)
        return svg.shapeSvg
    }

    fun fromRequestToPlaceMat(contents: String, type: String = "SVG", isPDF: Boolean): ShapeResponse {
        val pms = Json.decodeFromString<PlaceMatRequest>(contents)
        val maker = PlaceMatMaker(placeMatRequest = pms, type, isPDF)
        val csv = pms.toCsv()
        csvResponse.update(csv)
        return maker.makePlacerMat()
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, context.type, context.backend)
    }

}