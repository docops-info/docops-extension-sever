package io.docops.docopsextensionssupport.roadmap

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.URLDecoder

class RoadmapHandler : DocOpsHandler {
    val log = KotlinLogging.logger {  }
    fun handleSVG(payload: String, useDark: Boolean, type: String, scale: String, numChars: String, title: String): String {
            val data = uncompressString(URLDecoder.decode(payload, "UTF-8"))
            val rmm = PlannerMaker()
            val svg = rmm.makePlannerImage(data, title, scale)
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, context.useDark, context.type, context.scale, context.numChars, context.title)
    }

}
