package io.docops.docopsextensionssupport.roadmap

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvRequest
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import io.docops.docopsextensionssupport.web.update
import io.github.oshai.kotlinlogging.KotlinLogging

class PlannerHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {
    val log = KotlinLogging.logger { }
    fun handleSVG(payload: String, useDark: Boolean, type: String, scale: String, title: String): String {
        val rmm = PlannerMaker()
        val parser = PlannerParser()
        val planItems = parser.parse(payload)
        val svg = rmm.makePlannerImage(planItems, title, scale, useDark = useDark)
        csvResponse.update(planItems.toCsv())
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, context.useDark, context.type, context.scale, context.title)
    }

}
