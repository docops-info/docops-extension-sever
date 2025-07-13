package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.web.CsvRequest
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import io.docops.docopsextensionssupport.web.ShapeResponse
import kotlinx.serialization.json.Json


/**
 * ConnectorHandler class is responsible for handling requests related to SVG and PNG images.
 * Supports both JSON and table format for connectors.
 */
class ConnectorHandler: DocOpsHandler {



    /**
     * Handles the SVG request and returns the SVG image as a byte array.
     *
     * @param payload The compressed and encoded SVG payload.
     * @param type The type of the SVG image.
     * @param scale The scale of the SVG image.
     * @param useDark A boolean indicating whether to use dark mode for the SVG image.
     * @return The ResponseEntity containing the SVG image as a byte array.
     */
    fun handleSVG(payload: String, type: String, scale: String, useDark: Boolean): String {
        val svg = fromRequestToConnector(payload, scale = scale.toFloat(), useDark = useDark)
        return svg.shapeSvg
    }



    fun fromRequestToConnector(contents: String, scale: Float, useDark: Boolean, type: String = "SVG"): ShapeResponse {
        val connectors = if (isTableFormat(contents)) {
            parseTableData(contents)
        } else {
            decodeFromJson(contents)
        }
        val maker = createConnectorMaker(connectors.connectors, useDark, type)
        return makeConnectorImage(maker, scale)
    }

    /**
     * Determines if the data is in table format
     */
    private fun isTableFormat(data: String): Boolean {
        return data.contains("---") || (!data.trim().startsWith("{") && data.contains("|"))
    }

    /**
     * Parses table-like data into Connectors object
     */
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

    /**
     * Helper function to detect header rows in table format
     */
    private fun isHeaderRow(line: String): Boolean {
        val lowerLine = line.lowercase()
        return lowerLine.contains("text") || 
               lowerLine.contains("description") || 
               lowerLine.contains("color")
    }

    private fun decodeFromJson(contents: String): Connectors {
        return Json.decodeFromString<Connectors>(contents)
    }

    private fun createConnectorMaker(
        connectors: MutableList<Connector>,
        useDark: Boolean,
        type: String
    ): ConnectorMaker {
        return ConnectorMaker(connectors, useDark, type)
    }

    private fun makeConnectorImage(maker: ConnectorMaker, scale: Float): ShapeResponse {
        return maker.makeConnectorImage(scale)
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, context.type, context.scale, context.useDark)
    }

    override fun toCsv(request: CsvRequest): CsvResponse {
        val connectors = if (isTableFormat(request.content)) {
            parseTableData(request.content)
        } else {
            decodeFromJson(request.content)
        }
        return connectors.connectors.toCsv()
    }

}
