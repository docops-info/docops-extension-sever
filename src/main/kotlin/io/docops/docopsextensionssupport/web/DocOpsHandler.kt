package io.docops.docopsextensionssupport.web

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

}

/**
 * Context object containing common parameters for SVG generation
 */
data class DocOpsContext(
    val scale: String = "1.0",
    val type: String = "SVG",
    val title: String = "",
    val numChars: String = "24",
    val useDark: Boolean = false,
    val outlineColor: String = "#37cdbe",
    val backend: String = "html"
)
