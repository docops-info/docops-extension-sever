package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.diagram.placemat.PlaceMatMaker
import io.docops.docopsextensionssupport.diagram.placemat.PlaceMatRequest
import io.docops.docopsextensionssupport.diagram.placemat.toCsv
import io.docops.docopsextensionssupport.web.*
import kotlinx.serialization.json.Json

class PlacematHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse)  {
    fun handleSVG(payload: String, type: String, backend: String, useDark: Boolean): String {
        val isPDF = backend == "pdf"
        val svg = fromRequestToPlaceMat(payload, type, isPDF, useDark)
        return svg.shapeSvg
    }

    fun fromRequestToPlaceMat(contents: String, type: String = "SVG", isPDF: Boolean, useDark: Boolean): ShapeResponse {
        val pms = Json.decodeFromString<PlaceMatRequest>(contents)
        pms.useDark = useDark
        val maker = PlaceMatMaker(placeMatRequest = pms, type, isPDF)
        val csv = pms.toCsv()
        csvResponse.update(csv)
        return maker.makePlacerMat()
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, context.type, context.backend, context.useDark)
    }

}