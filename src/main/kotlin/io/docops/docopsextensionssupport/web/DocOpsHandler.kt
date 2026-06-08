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

    protected fun scaleSvg(svg: String, scale: String): String {
        val scaleFactor = scale.toDoubleOrNull()
            ?.takeIf { it > 0.0 }
            ?: 1.0

        if (scaleFactor == 1.0) {
            return svg
        }

        val svgTagRegex = Regex("""<svg\b([^>]*)>""")
        val match = svgTagRegex.find(svg) ?: return svg

        val originalSvgTag = match.value
        val attributes = match.groupValues[1]

        val width = extractSvgLength(attributes, "width")
        val height = extractSvgLength(attributes, "height")

        if (width == null || height == null) {
            return svg
        }

        val scaledWidth = formatSvgNumber(width * scaleFactor)
        val scaledHeight = formatSvgNumber(height * scaleFactor)

        val scaledSvgTag = originalSvgTag
            .replace(Regex("""width="[^"]*""""), """width="$scaledWidth"""")
            .replace(Regex("""height="[^"]*""""), """height="$scaledHeight"""")

        return svg.replaceFirst(originalSvgTag, scaledSvgTag)
    }

    private fun extractSvgLength(attributes: String, attributeName: String): Double? {
        val regex = Regex("""$attributeName="([0-9.]+)(?:px)?"""")
        return regex.find(attributes)
            ?.groupValues
            ?.getOrNull(1)
            ?.toDoubleOrNull()
    }

    private fun formatSvgNumber(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            "%.2f".format(value).trimEnd('0').trimEnd('.')
        }
    }
    // Force subclasses to implement the main method
    abstract override fun handleSVG(payload: String, context: DocOpsContext): String
}
