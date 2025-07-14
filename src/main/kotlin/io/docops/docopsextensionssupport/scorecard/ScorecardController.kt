package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.docops.docopsextensionssupport.web.DefaultCsvResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.net.URLDecoder
import kotlin.time.measureTimedValue

@Controller
@RequestMapping("/api/scorecard")
class ScorecardController {
    private val log = KotlinLogging.logger {}
    private val scoreCardHandler = ScorecardHandler(DefaultCsvResponse)

    @GetMapping("/edit-mode")
    @ResponseBody
    fun getEditMode(): ResponseEntity<String> {
        val defaultScoreCardContent = """
        title=Database Migration ScoreCard: Oracle → AWS Aurora PostgreSQL
        subtitle=On-Premise to Cloud Migration with SQL Optimization & Performance Tuning
        headerTitle=Database Architecture Transformation & Query Optimization Results
        scale=1.0
        ---
        [before]
        title=BEFORE: On-Premise Oracle Database
        ---
        [before.items]
        Oracle Database 19c (On-Premise) | Physical server, manual scaling, high licensing costs | critical | !
        Cross-Datacenter Communication | App in cloud, DB on-premise (high latency) | critical | !
        Inefficient SQL Queries | • Unnecessary UPPER() functions on pre-uppercase columns\n• Missing composite indexes causing full table scans\n• Non-optimized WHERE clauses and JOINs | critical | !
        Indexing Strategy | Single-column indexes only, no composite optimization | warning | !
        Operations & Maintenance | Manual backups, patching windows, limited monitoring | critical | !
        Cost Structure | High Oracle licensing + hardware + datacenter costs | critical | $
        ---
        [before.performance]
        Legacy Performance Baseline | 30 | #e74c3c
        ---
        [after]
        title=AFTER: AWS Aurora PostgreSQL
        ---
        [after.items]
        Aurora PostgreSQL (AWS Managed) | Auto-scaling, managed service, no licensing fees | good | ✓
        Same-Region Communication | App and DB both in AWS (low latency) | good | ✓
        Optimized SQL Queries | • Removed UPPER() functions, app handles case conversion\n• Composite indexes eliminate full table scans\n• Optimized WHERE clauses and efficient JOINs | good | ✓
        Advanced Indexing Strategy | Composite indexes for multi-column queries, B-tree optimization | good | ✓
        Managed Operations | Auto backups, patching, monitoring with CloudWatch | good | ✓
        Cost Optimization | Pay-as-you-scale, no Oracle licensing, reduced ops costs | good | $
        ---
        [after.performance]
        Enhanced Performance | 90 | #27ae60
        ---
        [metrics]
        Query Performance Gains | #e74c3c
        ---
        [metrics.items]
        Average Query Time | -78%
        Full Table Scans | -95%
        Index Usage | +340%
        Complex Query Time | -85%
        JOIN Performance | -67%
        Query Throughput | +250%
        ---
        [metrics]
        Network & Latency | #3498db
        ---
        [metrics.items]
        Network Latency | -89%
        Connection Time | -92%
        Data Transfer Speed | +450%
        Response Time P95 | -71%
        Connection Pool Eff. | +180%
        Timeout Errors | -98%
        ---
        [metrics]
        SQL Optimizations | #27ae60
        ---
        [metrics.items]
        UPPER() Function Calls | Eliminated
        Composite Indexes | +15 Added
        Query Complexity | Simplified
        Execution Plans | Optimized
        CPU Usage | -65%
        I/O Operations | -82%
        ---
        [metrics]
        Cost & Operations | #f39c12
        ---
        [metrics.items]
        Total Cost of Ownership | -68%
        Oracle Licensing | $0
        Backup Storage | -45%
        Maintenance Windows | Eliminated
        Scaling Time | Instant
        Admin Overhead | -80%
        ---
        [optimizations]
        1 | Removed UPPER() Functions | App handles case conversion, eliminated function overhead
        2 | Composite Indexes | Multi-column indexes for complex WHERE clauses
        3 | Network Co-location | Same AWS region eliminates cross-DC latency
        4 | Query Optimization | Eliminated full table scans, optimized JOINs
        ---
        [summary]
        93 | EXCEPTIONAL | Zero data loss migration | 78% query improvement | 68% cost reduction
        ---
        [footer]
        Migration Duration: 8 weeks | Team: 3 DBAs + 2 developers | Downtime: 4 hours | Status: Production Ready
        """.trimIndent()

        val editModeHtml = """
            <div id="scorecardContainer" class="bg-gray-50 rounded-lg p-4 h-auto">
                <form hx-put="api/scorecard/render" hx-target="#scorecardPreview" class="space-y-4">
                    <div>
                        <label for="content" class="block text-sm font-medium text-gray-700 mb-1">Edit ScoreCard Content:</label>
                        <textarea id="content" name="content" rows="20" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">${defaultScoreCardContent}</textarea>
                    </div>
                    <div class="flex justify-between">
                        <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-4 py-2 text-center">
                            Update ScoreCard
                        </button>
                        <button class="text-gray-700 bg-gray-200 hover:bg-gray-300 focus:ring-4 focus:outline-none focus:ring-gray-300 font-medium rounded-lg text-sm px-4 py-2 text-center"
                                hx-get="api/scorecard/view-mode"
                                hx-target="#scorecardContainer"
                                hx-swap="outerHTML">
                            Cancel
                        </button>
                    </div>
                    <div id="scorecardPreview" class="mt-4 p-4 border border-gray-200 rounded-lg bg-white min-h-[200px]">
                        <div class="text-center text-gray-500 text-sm">
                            Click "Update ScoreCard" to see the preview
                        </div>
                    </div>
                </form>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(editModeHtml, headers, HttpStatus.OK)
    }

    @GetMapping("/view-mode")
    @ResponseBody
    fun getViewMode(): ResponseEntity<String> {
        val viewModeHtml = """
            <div id="scorecardContainer" class="bg-gray-50 rounded-lg p-4 h-64 flex items-center justify-center">
                 <object data="images/scorecard.svg" type="image/svg+xml" height="100%" width="100%">
                    <img src="images/scorecard.svg" alt="Scorecard" class="max-h-full max-w-full" />
                </object>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(viewModeHtml, headers, HttpStatus.OK)
    }

    @PutMapping("/render")
    @ResponseBody
    fun renderScorecard(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            val content = httpServletRequest.getParameter("content")
            scoreCardHandler.handleHTML(content)
        }
        
        log.info{"scorecard render executed in ${timings.duration.inWholeMilliseconds}ms "}
        return timings.value
    }

    @PostMapping("")
    fun editorFormSubmit(@RequestParam("payload") payload: String) : ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            scoreCardHandler.handleHTML(payload)
        }

        log.info{"scorecard render executed in ${timings.duration.inWholeMilliseconds}ms "}
        return timings.value
    }
}