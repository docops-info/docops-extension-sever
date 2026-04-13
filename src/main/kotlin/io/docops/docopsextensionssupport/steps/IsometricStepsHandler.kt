package io.docops.docopsextensionssupport.steps

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update

class IsometricStepsHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {


    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        val parser = IsometricStepsParser()
        val model = parser.parse(payload)
        val svg = if (model.config.view.lowercase() == "road") {
            val generator = RoadStepsSvgGenerator(context.useDark)
            generator.createSvg(model, context.scale.toDouble())
        } else {
            val generator = IsometricStepsSvgGenerator(context.useDark)
            generator.createSvg(model, context.scale.toDouble())
        }
        csvResponse.update(model.toCSV())
        return svg
    }
}
