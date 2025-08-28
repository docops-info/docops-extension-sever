package io.docops.docopsextensionssupport.flow

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.nio.charset.StandardCharsets
import kotlin.time.measureTimedValue

/**
 * Handler for flow diagram generation from AsciiDoc input.
 * Integrates with the DocOps framework following established patterns.
 */
class FlowHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {
    private val log = KotlinLogging.logger {}

    /**
     * Handles the SVG generation process.
     *
     * @param payload The AsciiDoc input string
     * @param backend The backend type (e.g., "pdf")
     * @param useDark Whether to use dark mode (currently not implemented)
     * @param scale Scale factor for the diagram
     * @return SVG string
     */
    fun handleSVG(payload: String, backend: String, useDark: Boolean = false, scale: String = "1.0"): String {
        val parser = FlowParser()
        val flowDefinition = parser.parse(payload)

        // Update CSV metadata for router embedding
        csvResponse.update(flowDefinition.toCsv())

        val generator = FlowSvgGenerator()
        return generator.generate(flowDefinition)
    }

    /**
     * Handles the HTML rendering process for the UI.
     *
     * @param payload The AsciiDoc input string
     * @return ResponseEntity containing the HTML with embedded SVG
     */
    fun handleHTML(payload: String): ResponseEntity<ByteArray> {
        try {
            val timing = measureTimedValue {
                val headers = HttpHeaders()
                headers.cacheControl = CacheControl.noCache().headerValue
                headers.contentType = MediaType.parseMediaType("text/html")

                val svg = handleSVG(payload, "svg")
                val div = """
                    <div id='imageblock'>
                    $svg
                    </div>
                    <script>
                    var flowSource = `[docops,flow]\n----\n${payload}\n----`;
                    </script>
                """.trimIndent()

                ResponseEntity(div.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
            }

            logHandlerExecution("flow diagram HTML", timing.duration.inWholeMilliseconds)
            return timing.value
        } catch (e: Exception) {
            log.error(e) { "Error rendering flow diagram HTML" }
            throw e
        }
    }

    override fun handleSVG(payload: String, context: DocOpsContext): String {
        return handleSVG(payload, context.backend, context.useDark, context.scale)
    }
}

/**
 * Extension function to convert FlowDefinition to CSV format for metadata.
 */
private fun FlowDefinition.toCsv(): CsvResponse {
    val headers = listOf("Step ID", "Step Name", "Step Type", "Color", "Connections")
    val rows = steps.map { step ->
        val connectionCount = connections.count { it.from == step.id || it.to == step.id }
        listOf(step.id, step.name, step.type.name, step.color, connectionCount.toString())
    }
    return CsvResponse(headers, rows)
}