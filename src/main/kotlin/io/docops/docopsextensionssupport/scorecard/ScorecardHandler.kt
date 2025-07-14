package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.web.BaseDocOpsHandler
import io.docops.docopsextensionssupport.web.CsvRequest
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.DocOpsHandler
import io.docops.docopsextensionssupport.web.update
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.*
import java.nio.charset.StandardCharsets
import kotlin.time.measureTimedValue

class ScorecardHandler(csvResponse: CsvResponse) : BaseDocOpsHandler(csvResponse) {
    private val log = KotlinLogging.logger {}

    /**
     * Handles the SVG generation process.
     *
     * @param payload The input string in table format
     * @param backend The backend type (e.g., "pdf")
     * @return A ResponseEntity containing the SVG
     */
    fun handleSVG(payload: String, backend: String): String {
        try {
            val isPdf = backend == "pdf"
            val parser = ScoreCardParser()
            val migrationScorecard = parser.parse(payload)
            val maker = ScoreCardMaker()
            val svg = maker.make(migrationScorecard, isPdf)
            val csv = migrationScorecard.toCsv()
            csvResponse.update(csv)
            return svg
        } catch (e: Exception) {
            log.error(e) { "Error generating migration scorecard" }
            throw e
        }
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
                val parser = ScoreCardParser()
                val migrationScorecard = parser.parse(payload)
                val maker = ScoreCardMaker()
                val svg = maker.make(migrationScorecard, false)

                val headers = HttpHeaders()
                headers.cacheControl = CacheControl.noCache().headerValue
                headers.contentType = MediaType.parseMediaType("text/html")

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
        return handleSVG(payload, context.backend)
    }
}