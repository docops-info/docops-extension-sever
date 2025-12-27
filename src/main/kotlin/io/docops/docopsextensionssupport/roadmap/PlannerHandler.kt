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
    fun handleSVG(payload: String, useDark: Boolean, type: String, scale: String, title: String, theme: String = "DEFAULT"): String {
        val parser = PlannerParser()
        val planItems = parser.parse(payload)
        val rmm = ModernPlannerMaker()
        val svg = rmm.makePlannerImage(planItems, title, scale, useDark = useDark)
        /*val svg = if (theme.uppercase() == "MODERN") {
            val rmm = ModernPlannerMaker()
            rmm.makePlannerImage(planItems, title, scale, useDark = useDark)
        } else {
            val rmm = PlannerMaker()
            rmm.makePlannerImage(planItems, title, scale, useDark = useDark)
        }*/

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
