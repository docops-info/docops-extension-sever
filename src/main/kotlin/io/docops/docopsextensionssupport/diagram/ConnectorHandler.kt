package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.diagram.connector.Connector
import io.docops.docopsextensionssupport.diagram.connector.ConnectorMaker
import io.docops.docopsextensionssupport.diagram.connector.Connectors
import io.docops.docopsextensionssupport.diagram.connector.toCsv
import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.ShapeResponse
import io.docops.docopsextensionssupport.web.update
import kotlinx.serialization.json.Json


/**
 * ConnectorHandler class is responsible for handling requests related to SVG and PNG images.
 * Supports both JSON and table format for connectors.
 */
class ConnectorHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {



    /**
     * Handles the SVG request and returns the SVG image as a byte array.
     *
     * @param payload The compressed and encoded SVG payload.
     * @param type The type of the SVG image.
     * @param scale The scale of the SVG image.
     * @param useDark A boolean indicating whether to use dark mode for the SVG image.
     * @return The ResponseEntity containing the SVG image as a byte array.
     */
    fun handleSVGInternal(payload: String, type: String, scale: String, useDark: Boolean, backend: String): Pair<ShapeResponse, CsvResponse> {
        val svg = fromRequestToConnector(payload, scale = scale.toFloat(), useDark = useDark, "SVG", backend)
        return svg
    }



    fun fromRequestToConnector(contents: String, scale: Float, useDark: Boolean, type: String = "SVG", backend: String): Pair<ShapeResponse, CsvResponse> {
        val isPDF = "pdf".equals(backend, ignoreCase = true)
        val connectors = parseTableData(contents)
        val resp = connectors.connectors.toCsv()

        var docType = type
        if(isPDF) {
            docType = "PDF"
        }
        val maker = createConnectorMaker(connectors.connectors, useDark, docType, isPDF)
        return Pair(makeConnectorImage(maker, scale), resp)
    }

    private fun parseTableData(data: String): Connectors {
        val lines = data.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val connectorsList = mutableListOf<Connector>()
        var inDataSection = false

        for (line in lines) {
            when {
                line == "---" -> inDataSection = true
                inDataSection && line.contains("|") && !isHeaderRow(line) -> {
                    val parts = line.split("|").map { it.trim() }
                    if (parts.size >= 1) {
                        val text = parts[0]
                        val description = if (parts.size > 1) parts[1] else ""
                        val baseColor = if (parts.size > 2) parts[2] else "#E14D2A"
                        connectorsList.add(Connector(text = text, description = description, baseColor = baseColor))
                    }
                }
            }
        }

        return Connectors(connectors = connectorsList)
    }

    private fun isHeaderRow(line: String): Boolean {
        val lowerLine = line.lowercase()
        return lowerLine.contains("text") ||
                lowerLine.contains("description") ||
                lowerLine.contains("color")
    }

    private fun createConnectorMaker(
        connectors: MutableList<Connector>,
        useDark: Boolean,
        type: String,
        isPDF: Boolean
    ): ConnectorMaker {
        return ConnectorMaker(connectors, useDark, type, isPdf = isPDF)
    }

    private fun makeConnectorImage(maker: ConnectorMaker, scale: Float): ShapeResponse {
        return maker.makeConnectorImage(scale)
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        val resp = handleSVGInternal(payload, type = context.type, context.scale, context.useDark, context.backend)
        csvResponse.update(resp.second)
        return resp.first.shapeSvg
    }



}
