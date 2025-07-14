package io.docops.docopsextensionssupport.web

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity

interface DocOpsHandler {
    /**
     * Handles SVG generation with common parameters
     *
     * @param payload The compressed/encoded payload containing the data
     * @param context Additional context parameters for SVG generation
     * @return ResponseEntity containing the generated SVG as ByteArray
     */
    fun handleSVG(payload: String, context: DocOpsContext): String

     open fun toCsv(request: CsvRequest): CsvResponse {

        return DefaultCsvResponse
    }

}

/**
 * Context object containing common parameters for SVG generation
 */
data class DocOpsContext(
    val scale: String = "1.0",
    val type: String = "SVG",
    val title: String = "",
    val useDark: Boolean = false,
    val backend: String = "html",
    val useGlass: Boolean = false,
    val docname: String = ""
)

abstract class BaseDocOpsHandler(
    protected val csvResponse: CsvResponse
) : DocOpsHandler {

    protected val logger = KotlinLogging.logger {}
    // Common functionality can go here
    protected fun logHandlerExecution(kind: String, duration: Long) {
        logger.info { "handling $kind took $duration ms" }
    }

    // Force subclasses to implement the main method
    abstract override fun handleSVG(payload: String, context: DocOpsContext): String
}
