package io.docops.docopsextensionssupport.roadmap

import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import io.github.oshai.kotlinlogging.KotlinLogging

class RoadmapHandler : DocOpsHandler {
    val log = KotlinLogging.logger {  }
    fun handleSVG(payload: String, useDark: Boolean, type: String, scale: String,  title: String): String {
            val rmm = PlannerMaker()
            val svg = rmm.makePlannerImage(payload, title, scale)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, context.useDark, context.type, context.scale, context.title)
    }

}
