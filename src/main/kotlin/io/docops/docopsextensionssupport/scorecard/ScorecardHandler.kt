package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.nio.charset.StandardCharsets
import kotlin.time.measureTimedValue

class ScorecardHandler {
    private val log = KotlinLogging.logger {}

    /**
     * Handles the SVG generation process.
     *
     * @param payload The input string in table format
     * @param backend The backend type (e.g., "pdf")
     * @return A ResponseEntity containing the SVG
     */
    fun handleSVG(payload: String, backend: String): ResponseEntity<ByteArray> {
        try {
            val timing = measureTimedValue {
                val isPdf = backend == "pdf"
                val parser = ScoreCardParser()
                val data = uncompressString(payload)
                val migrationScorecard = parser.parse(data)
                val maker = ScoreCardMaker()
                val svg = maker.make(migrationScorecard, isPdf)
                
                val headers = HttpHeaders()
                headers.cacheControl = CacheControl.noCache().headerValue
                headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
                
                ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
            }
            
            log.info{"getMigrationScorecard executed in ${timing.duration.inWholeMilliseconds}ms "}
            return timing.value
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
            
            log.info{"renderMigrationScorecard executed in ${timing.duration.inWholeMilliseconds}ms "}
            return timing.value
        } catch (e: Exception) {
            log.error(e) { "Error rendering migration scorecard HTML" }
            throw e
        }
    }
}