
package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler

import org.springframework.http.MediaType
import org.springframework.stereotype.Component

@Component
class CombinationChartHandler : DocOpsHandler {
    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        val maker = CombinationChartImproved()
        return maker.makeCombinationChartSvg(payload)
    }


}
