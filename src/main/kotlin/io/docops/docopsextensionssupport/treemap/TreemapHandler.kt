package io.docops.docopsextensionssupport.chart.treemap

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Handler for treemap chart generation following DocOps pattern
 */
class TreemapHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {
    

    fun handleSVGInternal(payload: String, useDark: Boolean): String {
        val parser = TreemapParser()
        val treemap = parser.parse(payload, useDark)
        
        val generator = TreemapGenerator(useDark)
        val svg = generator.generate(treemap)
        
        csvResponse.update(treemap.toCsv())
        
        return svg
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        val startTime = System.currentTimeMillis()
        val result = handleSVGInternal(payload, context.useDark)
        val duration = System.currentTimeMillis() - startTime
        
        logHandlerExecution("treemap", duration)
        
        return result
    }
}
