package io.docops.docopsextensionssupport.domainviz

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update

class DomainVizHandler(csvResponse: CsvResponse): BaseDocOpsHandler(csvResponse) {
    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        val parser = DiagramParser()
        val data = parser.parseCSV(payload)

        // Switch based on the flag detected in the CSV
        val svg = if (data.useNeural) {
            val diagramGenerator = NeuralDomainVisualizer(useDark = context.useDark)
            diagramGenerator.generateSVG(data)
        } else {
            val diagramGenerator = SVGDiagramGenerator(useDark = context.useDark)
            diagramGenerator.generateSVG(data)
        }

        csvResponse.update(data.toCsv())
        return svg
    }
}