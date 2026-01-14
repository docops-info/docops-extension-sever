
package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.chart.combo.CombinationChartImproved
import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext


class CombinationChartHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {
    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        val maker = CombinationChartImproved(context.useDark)
        return maker.makeCombinationChartSvg(payload, csvResponse )
    }


}
