package io.docops.docopsextensionssupport.scorecard

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

class ScorecardHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {
    private val log = KotlinLogging.logger {}

    /**
     * Handles the SVG generation process with dark mode support.
     *
     * @param payload The input string in table format
     * @param backend The backend type (e.g., "pdf")
     * @param useDark Whether to use dark mode
     * @return A ResponseEntity containing the SVG
     */
    fun handleSVG(payload: String, backend: String, useDark: Boolean = false, scale: String = "1.0f"): String {
        val parser = ScoreCardParser()
        val model = parser.parse(payload, useDark, scale)
        // populate CSV metadata for router embedding
        csvResponse.update(model.toCsv())
        val maker = ScoreCardMaker()
        return maker.make(model)
    }

    /**
     * Handles the HTML rendering process for the UI.
     *
     * @param payload The input string in table format
     * @return A ResponseEntity containing the HTML with embedded SVG
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
                    var scorecardSource = `[docops,scorecard]\n----\n${payload}\n----`;
                    </script>
                """.trimIndent()

                ResponseEntity(div.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
            }

            log.info { "renderMigrationScorecard executed in ${timing.duration.inWholeMilliseconds}ms " }
            return timing.value
        } catch (e: Exception) {
            log.error(e) { "Error rendering migration scorecard HTML" }
            throw e
        }
    }

    override fun handleSVG(
        payload: String,
        context: DocOpsContext
    ): String {
        return handleSVG(payload, context.backend, context.useDark, context.scale)
    }
}