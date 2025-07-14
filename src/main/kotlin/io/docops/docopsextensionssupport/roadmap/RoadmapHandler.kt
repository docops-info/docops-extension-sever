package io.docops.docopsextensionssupport.roadmap

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update

class RoadmapHandler(csvResponse: CsvResponse): BaseDocOpsHandler(csvResponse) {
    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        val parser = RoadmapParser()
        val data = parser.parseRoadmapData(content = payload)
        val maker = RoadmapMaker()
        csvResponse.update(data.toCsv())
        return maker.generateRoadmapSVG(data.first, data.second)
    }
}