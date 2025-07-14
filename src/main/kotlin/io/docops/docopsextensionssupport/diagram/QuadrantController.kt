package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.docops.docopsextensionssupport.web.DefaultCsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.sercasti.tracing.Traceable
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.*
import java.net.URLDecoder
import java.nio.charset.Charset
import kotlin.time.measureTimedValue

/**
 * Controller for handling quadrant chart requests.
 * Updated to use QuadrantHandler with QuadrantChartGenerator for improved chart generation.
 */
@Controller
@RequestMapping("/api/quadrant")
class QuadrantController {
    private val log = KotlinLogging.logger {}
    private val quadrantHandler = QuadrantHandler(DefaultCsvResponse)

    /**
     * Returns the edit mode HTML for the quadrant chart.
     */
    @GetMapping("/edit-mode")
    @ResponseBody
    fun getEditMode(): ResponseEntity<String> {
        val editModeContent = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Quadrant Chart Editor</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    .container { max-width: 1200px; margin: 0 auto; }
                    .editor { display: flex; gap: 20px; }
                    .input-panel { flex: 1; }
                    .preview-panel { flex: 1; }
                    textarea { width: 100%; height: 400px; font-family: monospace; }
                    button { padding: 10px 20px; margin: 5px; }
                    .preview { border: 1px solid #ccc; min-height: 400px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>Quadrant Chart Editor</h1>
                    <div class="editor">
                        <div class="input-panel">
                            <h3>Input Data</h3>
                            <textarea id="payload" placeholder="Enter your quadrant data in table or JSON format...">
title: Product Feature Priority Matrix
subtitle: Q4 2024 Planning Session
xAxisLabel: Implementation Effort (Days)
yAxisLabel: Business Impact Score
q1label: Quick Wins
q2label: Major Projects
q3label: Fill-ins
q4label: Thankless Tasks
---
| Label | X | Y | Category |
|-------|---|---|----------|
| User Authentication | 15 | 85 | security |
| Mobile App | 45 | 90 | mobile |
| Dark Mode | 8 | 70 | ui |
| Database Migration | 30 | 40 | infrastructure |
| Bug Fixes | 5 | 30 | maintenance |
                            </textarea>
                            <div>
                                <button onclick="generateChart()">Generate Chart</button>
                                <button onclick="clearInput()">Clear</button>
                            </div>
                        </div>
                        <div class="preview-panel">
                            <h3>Preview</h3>
                            <div id="preview" class="preview"></div>
                        </div>
                    </div>
                </div>
                
                <script>
                    function generateChart() {
                        const payload = document.getElementById('payload').value;
                        const encodedPayload = encodeURIComponent(payload);
                        const url = `/extension/api/quadrant/?payload=${'$'}{encodedPayload}&scale=1.0&useDark=false`;
                        
                        fetch(url)
                            .then(response => response.arrayBuffer())
                            .then(data => {
                                const blob = new Blob([data], { type: 'image/svg+xml' });
                                const url = URL.createObjectURL(blob);
                                const img = document.createElement('img');
                                img.src = url;
                                img.style.maxWidth = '100%';
                                const preview = document.getElementById('preview');
                                preview.innerHTML = '';
                                preview.appendChild(img);
                            })
                            .catch(error => {
                                console.error('Error:', error);
                                document.getElementById('preview').innerHTML = '<p>Error generating chart</p>';
                            });
                    }
                    
                    function clearInput() {
                        document.getElementById('payload').value = '';
                        document.getElementById('preview').innerHTML = '';
                    }
                </script>
            </body>
            </html>
        """.trimIndent()

        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(editModeContent)
    }

    /**
     * Returns the view mode HTML for the quadrant chart.
     */
    @GetMapping("/view-mode")
    @ResponseBody
    fun getViewMode(): ResponseEntity<String> {
        val viewModeContent = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Quadrant Chart Viewer</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; text-align: center; }
                    .container { max-width: 1000px; margin: 0 auto; }
                    .chart-container { margin: 20px 0; }
                    .controls { margin: 20px 0; }
                    button { padding: 10px 20px; margin: 5px; }
                    input { margin: 5px; padding: 5px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>Quadrant Chart Viewer</h1>
                    <div class="controls">
                        <input type="file" id="fileInput" accept=".json,.txt" onchange="loadFile()">
                        <button onclick="loadSample()">Load Sample</button>
                        <label>
                            <input type="checkbox" id="darkMode" onchange="updateChart()"> Dark Mode
                        </label>
                        <label>
                            Scale: <input type="range" id="scaleSlider" min="0.5" max="2.0" step="0.1" value="1.0" onchange="updateChart()">
                            <span id="scaleValue">1.0</span>
                        </label>
                    </div>
                    <div id="chartContainer" class="chart-container">
                        <p>Load a file or use the sample to view a quadrant chart</p>
                    </div>
                </div>
                
                <script>
                    let currentPayload = '';
                    
                    function loadFile() {
                        const file = document.getElementById('fileInput').files[0];
                        if (file) {
                            const reader = new FileReader();
                            reader.onload = function(e) {
                                currentPayload = e.target.result;
                                updateChart();
                            };
                            reader.readAsText(file);
                        }
                    }
                    
                    function loadSample() {
                        currentPayload = `title: Strategic Priority Matrix
xAxisLabel: Effort Required
yAxisLabel: Impact Level
---
| Label | X | Y |
|-------|---|---|
| Quick Win 1 | 20 | 80 |
| Major Project | 80 | 90 |
| Low Priority | 30 | 30 |
| Avoid This | 90 | 20 |`;
                        updateChart();
                    }
                    
                    function updateChart() {
                        if (!currentPayload) return;
                        
                        const scale = document.getElementById('scaleSlider').value;
                        const useDark = document.getElementById('darkMode').checked;
                        document.getElementById('scaleValue').textContent = scale;
                        
                        const encodedPayload = encodeURIComponent(currentPayload);
                        const url = `/extension/api/quadrant/?payload=${'$'}{encodedPayload}&scale=${'$'}{scale}&useDark=${'$'}{useDark}`;
                        
                        fetch(url)
                            .then(response => response.arrayBuffer())
                            .then(data => {
                                const blob = new Blob([data], { type: 'image/svg+xml' });
                                const url = URL.createObjectURL(blob);
                                const img = document.createElement('img');
                                img.src = url;
                                img.style.maxWidth = '100%';
                                const container = document.getElementById('chartContainer');
                                container.innerHTML = '';
                                container.appendChild(img);
                            })
                            .catch(error => {
                                console.error('Error:', error);
                                document.getElementById('chartContainer').innerHTML = '<p>Error loading chart</p>';
                            });
                    }
                </script>
            </body>
            </html>
        """.trimIndent()

        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(viewModeContent)
    }

    /**
     * Generates a quadrant chart from the request and returns it as a response entity.
     */
    @Traceable
    @PutMapping("/")
    @ResponseBody
    @Counted(value="docops.quadrant.put", description="Creating a Quadrant Chart using http put")
    @Timed(value = "docops.quadrant.put", description="Creating a Quadrant Chart using http put", percentiles=[0.5, 0.9])
    fun makeQuadrant(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val (svg, duration) = measureTimedValue {
            try {
                val body = StreamUtils.copyToString(httpServletRequest.inputStream, Charset.defaultCharset())
                log.info { "Received quadrant request with body length: ${body.length}" }
                fromRequestToQuadrant(body, 1.0f, false)
            } catch (e: Exception) {
                log.error(e) { "Error processing quadrant chart request" }
                throw e
            }
        }

        log.info { "Generated quadrant chart in ${duration.inWholeMilliseconds}ms" }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("image/svg+xml"))
            .body(svg.toByteArray())
    }

    /**
     * Converts the request payload to a quadrant chart and generates the SVG using QuadrantChartGenerator.
     */
    fun fromRequestToQuadrant(contents: String, scale: Float, useDark: Boolean, title: String = ""): String {
        val context = DocOpsContext(
            type = "SVG",
            scale = scale.toString(),
            useDark = useDark,
            title = title,
            backend = "html"
        )

        return quadrantHandler.handleSVG(contents, context)
    }

    /**
     * Retrieves the quadrant chart based on the provided parameters.
     */
    @Traceable
    @GetMapping("/")
    @ResponseBody
    @Counted(value = "docops.quadrant.get", description="Creating a quadrant chart using http get")
    @Timed(value = "docops.quadrant.get", description="Creating a quadrant chart using http get", percentiles=[0.5, 0.9])
    fun getQuadrant(
        @RequestParam(name = "payload") payload: String,
        @RequestParam(name = "scale", defaultValue = "1.0") scale: String,
        @RequestParam("type", required = false, defaultValue = "SVG") type: String,
        @RequestParam(name = "useDark", defaultValue = "false") useDark: Boolean,
        @RequestParam(name = "title", defaultValue = "") title: String,
        @RequestParam(name = "numChars", defaultValue = "24") numChars: String,
        @RequestParam(name = "backend", defaultValue = "html") backend: String
    ): ResponseEntity<ByteArray> {
        val (svg, duration) = measureTimedValue {
            try {
                val decodedPayload = URLDecoder.decode(payload, "UTF-8")
                val uncompressedPayload = uncompressString(decodedPayload)
                log.info { "Processing quadrant GET request with payload length: ${uncompressedPayload.length}" }

                val context = DocOpsContext(
                    type = type,
                    scale = scale,
                    useDark = useDark,
                    title = title,
                    backend = backend
                )

                quadrantHandler.handleSVG(uncompressedPayload, context)
            } catch (e: Exception) {
                log.error(e) { "Error processing quadrant chart GET request" }
                throw e
            }
        }

        log.info { "Generated quadrant chart in ${duration.inWholeMilliseconds}ms" }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("image/svg+xml"))
            .body(svg.toByteArray())
    }

    /**
     * Handles form submission for editing quadrant charts.
     */
    @PostMapping("")
    fun editFormSubmission(@RequestParam("payload") payload: String): ResponseEntity<ByteArray> {
        val (svg, duration) = measureTimedValue {
            try {
                log.info { "Processing quadrant POST request with payload length: ${payload.length}" }

                val context = DocOpsContext(
                    type = "SVG",
                    scale = "1.0",
                    useDark = false,
                    title = "",
                    backend = "html"
                )

                quadrantHandler.handleSVG(payload, context)
            } catch (e: Exception) {
                log.error(e) { "Error processing quadrant chart POST request" }
                throw e
            }
        }

        log.info { "Generated quadrant chart from form submission in ${duration.inWholeMilliseconds}ms" }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("image/svg+xml"))
            .body(svg.toByteArray())
    }
}