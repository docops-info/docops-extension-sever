package io.docops.docopsextensionssupport.chart
import io.docops.docopsextensionssupport.chart.gauge.GaugeMakerFactory
import io.docops.docopsextensionssupport.chart.gauge.GaugeParser
import io.docops.docopsextensionssupport.chart.gauge.toDetailedCsvResponse
import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update


// Handler Integration
class GaugeHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {
    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        val gaugeChart = GaugeParser.parseTabularInput(payload)
        gaugeChart.display.useDark = context.useDark
        val maker = GaugeMakerFactory.createMaker(gaugeChart)
        csvResponse.update(gaugeChart.toDetailedCsvResponse())
        val svg = maker.makeGauge(gaugeChart)
        return svg
    }


}
