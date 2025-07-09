package io.docops.docopsextensionssupport.roadmap

import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler

class RoadmapHandler: DocOpsHandler {
    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        val parser = RoadmapParser()
        val data = parser.parseRoadmapData(content = payload)
        val maker = RoadmapMaker()
        return maker.generateRoadmapSVG(data.first, data.second)
    }
}