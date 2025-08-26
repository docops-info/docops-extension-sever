package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.web.DefaultCsvResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
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
        title=Software Release v2.4.0 - Feature & Bug Summary
        subtitle=Migration from Legacy System to Modern Architecture
        headerTitle=Complete System Migration Overview
        scale=1.0
        useDark=false
        ---
        [before]
        title=BEFORE v2.4.0
        ---
        [before.items]
        === Feature Status
        Dark Mode Theme | Missing feature affecting user experience | critical | ⚠️
        Multi-language Support | Not available, limiting global reach | critical | ⚠️
        Advanced Search Filters | Basic search only, slow performance | warning | !
        Export to PDF | Feature not implemented | warning | !
        Two-Factor Authentication | Security vulnerability exists | critical | ⚠️
        API Rate Limiting | No protection against abuse | critical | ⚠️
        === Known Issues  
        Login timeout issues | Users frequently logged out | critical | ⚠️
        Memory leaks in dashboard | System becomes slow over time | critical | ⚠️
        File upload corruption | Files sometimes corrupted during upload | critical | ⚠️
        Mobile UI overlapping | Interface broken on mobile devices | warning | !
        Database connection drops | Intermittent connection failures | critical | ⚠️
        Email notifications failing | Users not receiving important updates | warning | !
        Report generation errors | Reports fail to generate properly | warning | !
        ---
        [before.performance]
        Legacy Performance Baseline | 30 | #e74c3c
        ---
        [after]
        title=AFTER v2.4.0
        ---
        [after.items]
        === New Features Added
        Dark Mode Theme | Implemented with user preference saving | good | ✅
        Multi-language Support | Added 12 languages with automatic detection | good | ✅
        Advanced Search Filters | Fast indexing with multiple filter options | good | ✅
        Export to PDF | High-quality PDF export with custom templates | good | ✅
        Two-Factor Authentication | TOTP and SMS-based 2FA implemented | good | ✅
        API Rate Limiting | Intelligent rate limiting with user tiers | good | ✅
        === Bugs Resolved
        Login timeout issues | Session management completely rewritten | good | ✅
        Memory leaks in dashboard | React components optimized, memory usage -67% | good | ✅
        File upload corruption | New chunked upload system with integrity checks | good | ✅
        Mobile UI overlapping | Responsive design overhaul completed | good | ✅
        Database connection drops | Connection pooling and retry logic implemented | good | ✅
        Email notifications failing | New email service with 99.9% delivery rate | good | ✅
        Report generation errors | Async report generation with progress tracking | good | ✅
        ---
        [after.performance]
        Enhanced Performance | 90 | #27ae60
        ---
        [metrics]
        Performance Improvements | #3498db
        ---
        [metrics.items]
        Page Load Time | -78%
        Memory Usage | -67%
        API Response Time | -85%
        Database Queries | -92%
        Error Rate | -95%
        User Satisfaction | +340%
        ---
        [metrics]
        Feature Delivery | #27ae60
        ---
        [metrics.items]
        Features Delivered | 6 Major
        Bugs Fixed | 7 Critical
        Test Coverage | 95%
        Code Quality Score | A+
        Performance Score | 90/100
        Security Score | 98/100
        ---
        [optimizations]
        1 | React Component Optimization | Reduced memory leaks and improved render performance
        2 | Database Query Optimization | Implemented connection pooling and query caching
        3 | API Rate Limiting | Intelligent throttling based on user behavior patterns
        4 | Mobile Responsiveness | Complete UI overhaul for mobile devices
        5 | Security Enhancements | Two-factor authentication and improved session management
        ---
        [summary]
        95 | EXCELLENT | 6 features delivered | 7 critical bugs fixed | 78% performance improvement
        ---
        [team]
        JS | 👨‍💻 | #3498db
        AM | 👩‍🎨 | #e74c3c
        RK | 👨‍🔬 | #2ecc71
        LM | 👩‍💼 | #f39c12
        ---
        [footer]
        Release Duration: 12 weeks | Team: 2 Frontend + 2 Backend + 1 QA | Downtime: 2 hours | Status: Production Ready
        """.trimIndent()

        val editModeHtml = """
            <div id="scorecardContainer" class="bg-gray-50 rounded-lg p-4 h-auto">
                <form hx-put="api/scorecard/render" hx-target="#scorecardPreview" class="space-y-4">
                    <div>
                        <label for="content" class="block text-sm font-medium text-gray-700 mb-1">Edit ScoreCard Content:</label>
                        <textarea id="content" name="content" rows="25" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm font-mono">${defaultScoreCardContent}</textarea>
                    </div>
                    <div class="flex justify-between items-center">
                        <div class="flex items-center space-x-4">
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
                        <div class="flex items-center">
                            <input type="checkbox" id="darkMode" name="darkMode" class="mr-2 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded">
                            <label for="darkMode" class="text-sm text-gray-700">Dark Mode</label>
                        </div>
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
            val darkMode = httpServletRequest.getParameter("darkMode") == "on"

            // Modify the content to include dark mode setting if checked
            val modifiedContent = if (darkMode) {
                content.replace("useDark=false", "useDark=true")
            } else {
                content.replace("useDark=true", "useDark=false")
            }

            scoreCardHandler.handleHTML(modifiedContent)
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