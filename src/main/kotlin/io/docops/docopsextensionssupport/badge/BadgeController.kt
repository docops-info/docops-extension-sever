/*
 * Copyright (c) 2023. The DocOps Consortium
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.docops.docopsextensionssupport.badge

import io.docops.docopsextensionssupport.svgsupport.compressString
import io.docops.docopsextensionssupport.svgsupport.joinXmlLines
import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.docops.docopsextensionssupport.util.UrlUtil
import io.github.sercasti.tracing.Traceable
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.*


/**
 * Controller class for managing badges.
 * This class handles the creation and retrieval of badges in various formats (SVG, PNG).
 * It contains methods for creating badges from form data, retrieving badge parameters, and creating multiple badges.
 *
 * @property docOpsBadgeGenerator An instance of the DocOpsBadgeGenerator class for generating badges.
 */
@Controller
@RequestMapping("/api")
class BadgeController @Autowired constructor(
    private val docOpsBadgeGenerator: DocOpsBadgeGenerator,
    private val badgeHandler: BadgeHandler
){

    @GetMapping("/badge/edit-mode")
    @ResponseBody
    fun getEditMode(): ResponseEntity<String> {
        val pipeDelimitedContent = """Made With|Kotlin||#06133b|#6fc441|<Kotlin>|#fcfcfc
JVM|Runtime||#acacac|#3B1E54|<Java>|#fcfcfc
AsciiDoctor|Documentation||#acacac|#4CC9FE|<asciidoctor>|#fcfcfc"""

        val jsonContent = """[
  {
    "label": "Made With",
    "message": "Kotlin",
    "url": "",
    "labelColor": "#06133b",
    "messageColor": "#6fc441",
    "logo": "<Kotlin>",
    "fontColor": "#fcfcfc"
  },
  {
    "label": "JVM",
    "message": "Runtime",
    "url": "",
    "labelColor": "#acacac",
    "messageColor": "#3B1E54",
    "logo": "<Java>",
    "fontColor": "#fcfcfc"
  },
  {
    "label": "AsciiDoctor",
    "message": "Documentation",
    "url": "",
    "labelColor": "#acacac",
    "messageColor": "#4CC9FE",
    "logo": "<asciidoctor>",
    "fontColor": "#fcfcfc"
  }
]"""

        val editModeHtml = """
            <div id="badgeContainer" class="bg-gray-50 rounded-lg p-4 h-auto">
                <form hx-post="api/badges" hx-target="#badgePreview" class="space-y-4">
                    <div class="mb-4">
                        <label class="block text-sm font-medium text-gray-700 mb-2">Format:</label>
                        <div class="flex space-x-4">
                            <label class="inline-flex items-center">
                                <input type="radio" name="format" value="pipe" class="form-radio" checked onclick="switchFormat('pipe')">
                                <span class="ml-2 text-sm text-gray-700">Pipe Delimited</span>
                            </label>
                            <label class="inline-flex items-center">
                                <input type="radio" name="format" value="json" class="form-radio" onclick="switchFormat('json')">
                                <span class="ml-2 text-sm text-gray-700">JSON</span>
                            </label>
                        </div>
                    </div>
                    <div>
                        <label for="payload" class="block text-sm font-medium text-gray-700 mb-1">Edit Badge Configuration:</label>
                        <textarea id="payload" name="payload" rows="10" class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm">${pipeDelimitedContent}</textarea>
                    </div>
                    <div class="flex justify-between">
                        <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-4 py-2 text-center">
                            Update Badges
                        </button>
                        <button class="text-gray-700 bg-gray-200 hover:bg-gray-300 focus:ring-4 focus:outline-none focus:ring-gray-300 font-medium rounded-lg text-sm px-4 py-2 text-center"
                                hx-get="api/badge/view-mode"
                                hx-target="#badgeContainer"
                                hx-swap="outerHTML">
                            Cancel
                        </button>
                    </div>
                    <div id="badgePreview" class="mt-4 p-4 border border-gray-200 rounded-lg bg-white min-h-[200px]">
                        <div class="text-center text-gray-500 text-sm">
                            Click "Update Badges" to see the preview
                        </div>
                    </div>
                    <script>
                        const pipeContent = `${pipeDelimitedContent}`;
                        const jsonContent = `${jsonContent}`;

                        function switchFormat(format) {
                            const payloadTextarea = document.getElementById('payload');
                            if (format === 'pipe') {
                                payloadTextarea.value = pipeContent;
                            } else if (format === 'json') {
                                payloadTextarea.value = jsonContent;
                            }
                        }
                    </script>
                </form>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(editModeHtml, headers, HttpStatus.OK)
    }

    @GetMapping("/badge/view-mode")
    @ResponseBody
    fun getViewMode(): ResponseEntity<String> {
        val viewModeHtml = """
            <div id="badgeContainer" class="bg-gray-50 rounded-lg p-4 h-64 flex items-center justify-center">
                <object data="images/badge.svg" type="image/svg+xml" height="100%" width="100%">
                <img src="images/badge.svg" alt="Badges & Shields" class="max-h-full max-w-full" />
                </object>
            </div>
        """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity(viewModeHtml, headers, HttpStatus.OK)
    }

    /**
     * Retrieves a badge based on the provided form data and sends it as a response.
     *
     * @param badge The form data containing the badge details.
     * @param servletResponse The HTTP servlet response object used to send the badge as a response.
     */
    @Traceable
    @PutMapping("/badge/item", produces = ["image/svg+xml"])
    @ResponseBody
    @Counted(value = "docops.badge.put", description= "Number of times create a badge using put method")
    @Timed(value = "docops.badge.put", description= "Time taken to create a badge using put method", percentiles=[0.5, 0.9])
    fun getBadgeByForm(@RequestBody badge: FormBadge, servletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
        var fillColor = badge.messageColor
        if (null == fillColor) {
            fillColor = "GREEN"
        }
        var logo = ""
        if(badge.logo != null) {
            logo = badge.logo
        }
        val svg = docOpsBadgeGenerator.createBadge(badge.label, badge.message, badge.labelColor!!, badge.messageColor!!, "", logo, badge.fontColor!!)
        //val src = badgeAgain(formBadge = badge, type = "SVG")
        val badgeSource =
            """
[docops,badge]
----
${badge.label}|${badge.message}|${badge.url}|${badge.labelColor}|$fillColor|${badge.logo}|${badge.fontColor}|
----
""".trimIndent()
        val contents = makeBadgeAndSource(badgeSource, svg, servletRequest)
        servletResponse.contentType = "text/html"
        servletResponse.characterEncoding = "UTF-8"
        servletResponse.status = 200
        val writer = servletResponse.writer
        writer.print(contents)
        writer.flush()
    }

    fun makeBadgeAndSource(txt: String, svg: String, request: HttpServletRequest): String {
        val compressedPayload = compressString(txt)
        val imageUrl = UrlUtil.getImageUrl(
            request = request,
            kind = "badge",
            payload = compressedPayload,
            type = "SVG",
            useDark = false,
            title = "Title",
            numChars = "24",
            filename = "badge.svg"
        )

        return """
        <div id='imageblock'>
        $svg
        </div>
        <div class="mb-4">
            <h3>Image Request</h3>
            <div class="flex items-center">
                <input id="imageUrlInput" type="text" value="$imageUrl" readonly class="w-full p-2 border border-gray-300 rounded-l-md text-sm bg-gray-50">
                <button onclick="copyToClipboard('imageUrlInput')" class="bg-blue-600 text-white px-4 py-2 rounded-r-md hover:bg-blue-700 transition-colors">
                    Copy URL
                </button>
            </div>
        </div>
        <h3>Badge Source</h3>
        <div class='mb-5'>
        <pre>
        <code class="asciidoc">
$txt
        </code>
        </pre>
        <script>
        var badgeSource = `${txt}`;

        function copyToClipboard(elementId) {
            const element = document.getElementById(elementId);
            element.select();
            document.execCommand('copy');

            // Show a temporary "Copied!" message
            const button = element.nextElementSibling;
            const originalText = button.textContent;
            button.textContent = "Copied!";
            setTimeout(() => {
                button.textContent = originalText;
            }, 2000);
        }
        </script>
        </div>
    """.trimIndent()
    }

    @GetMapping("/text/{text}")
    @ResponseBody
    fun textLen(@PathVariable(required = true) text: String): Float {
        return docOpsBadgeGenerator.measureText(text) * 100F
    }

    @GetMapping("/text/box/{text}", produces = ["image/svg+xml"])
    @ResponseBody
    fun textBox(@PathVariable(required = true) text: String): String {
        val len = docOpsBadgeGenerator.measureText(text) * 100F + 10
        //language=svg
        val svg = """
            <svg xmlns="http://www.w3.org/2000/svg" width="$len" height="120"
     viewBox="0 0 $len 120" xmlns:xlink="http://www.w3.org/1999/xlink">
     <defs>
     <filter id="filter">
            <feMorphology in="SourceAlpha" operator="dilate" radius="2" result="OUTLINE"/>
            <feComposite operator="out" in="OUTLINE" in2="SourceAlpha"/>
        </filter>
        <style>
            .filtered {
                filter: url(#filter);
                fill: black;
                font-family: 'Ultra', serif;
                font-size: 100px;
            }
        </style>
     </defs>
     <g transform="translate(0,0)" cursor="pointer" font-family="Arial,DejaVu Sans,sans-serif" font-size="110">
        <rect x="0" y="0" width="$len" height="110" fill="#000000" rx="18" ry="18" stroke="green" stroke-width="5"/>
        <text text-anchor="middle" fill="#ffffff" x="${len/2}" y="90"   textLength="${len-10}">$text</text>
    </g>
     </svg>
        """.trimIndent()
        return svg
    }

    @GetMapping("/badge/item", produces = ["image/svg+xml"])
    fun getBadgeParams(
        @RequestParam(name = "payload") payload: String,
        @RequestParam(name = "type", defaultValue = "SVG", required = false) type: String,
        @RequestParam(name = "backend", defaultValue = "", required = false) backend: String,
        servletResponse: HttpServletResponse
    ): ResponseEntity<ByteArray> {
        val data = uncompressString(URLDecoder.decode(payload,"UTF-8"))
        val split = data.split("|")
        when {
            split.size < 6 -> {
                println(data)
                throw BadgeFormatException("Badge Format invalid, expecting 5 pipe delimited values [$data]")
            }

            else -> {
                val message: String = split[1]
                val label: String = split[0]
                var mcolor = "GREEN"
                val color: String = split[3].trim()
                val c = split[4].trim()
                if (c.isNotEmpty()) {
                    mcolor = c
                }
                var logo = ""
                if ("SVG" == type) {
                    logo = split[5].trim()
                }

                var fontColor = "#ffffff"
                if(split.size == 7) {
                    fontColor = split[6]
                }

                //val output = Badge.create(label, message, color, mcolor, null, 0, 1)
                val output = docOpsBadgeGenerator.createBadge(
                    iLabel = label,
                    iMessage = message,
                    labelColor = color,
                    messageColor = mcolor,
                    href = split[1],
                    icon = logo,
                    fontColor = fontColor,
                    backend = backend
                )

                val headers = HttpHeaders()
                headers.cacheControl = CacheControl.noCache().headerValue
                headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
                return ResponseEntity(output.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
            }
        }
    }


    @GetMapping("/badge/plain", produces = ["image/svg+xml"])
    @ResponseBody
    fun getBadgeItemPlain(
        @RequestParam(name = "label") label: String,
        @RequestParam(name = "message") message: String,
        @RequestParam(name = "color") color: String,
        @RequestParam(name = "messageColor") messageColor: String,
        @RequestParam(name = "icon", required = false) icon: String = "",
        @RequestParam(name = "fontColor", required = false) fontColor: String = "#000000"
    ): ResponseEntity<ByteArray> {
        val output = docOpsBadgeGenerator.createBadge(label, message, color, messageColor, "", icon, fontColor)
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
        return ResponseEntity(output.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
    }

    @PostMapping("/badges", consumes = [MediaType.ALL_VALUE], produces = ["image/svg+xml", "image/png"])
    @Timed(value = "docops.badges.post")
    fun badges(
        @RequestBody payload: String,
        @RequestParam(name = "type", defaultValue = "SVG", required = false) type: String
    ):  ResponseEntity<ByteArray>{
        var decodedPayload = URLDecoder.decode(payload,"UTF-8")

        // Check if the payload starts with format=pipe&payload= and extract just the payload part
        if (decodedPayload.startsWith("format=pipe&payload=")) {
            decodedPayload = decodedPayload.substringAfter("format=pipe&payload=")
        }
        else if (decodedPayload.startsWith("format=json&payload=")) {
            decodedPayload = decodedPayload.substringAfter("format=json&payload=")
        }
        val svg = badgeHandler.createBadgeFromString(decodedPayload,  isPdf = false)
        return ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), HttpStatus.OK)
    }

    @PostMapping("badge", produces = ["image/svg+xml"])
    fun badgeFromContent(@RequestParam("payload") payload: String): ResponseEntity<ByteArray> {
        return badges(payload, "SVG")
    }

    @GetMapping("/badge")
    fun  getFormBadge(
    @RequestParam  label: String,
    @RequestParam  message: String,
    @RequestParam(required = false)  url: String? = null,
    @RequestParam(required = false)  labelColor: String? = "#3C3D37",
    @RequestParam(required = false)  messageColor: String? = "#982B1C",
    @RequestParam(required = false) logo: String? = null,
    @RequestParam(defaultValue = "#fcfcfc") fontColor: String): ResponseEntity<ByteArray> {
        val svgSrc = docOpsBadgeGenerator.createBadgeFromList(mutableListOf(Badge(label = label, message = message, url= url, labelColor = labelColor, messageColor = messageColor, logo = logo, fontColor = fontColor)))
        val svg = StringBuilder()
        //language=svg
        svg.append("""
            <svg width='${svgSrc.second}' height='20' xmlns='http://www.w3.org/2000/svg' role='img' xmlns:xlink="http://www.w3.org/1999/xlink" aria-label='Made With: Kotlin'>
        """.trimIndent())
        svg.append(svgSrc.first)
        svg.append("</svg>")
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
        return ResponseEntity(svg.toString().toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
    }
}

fun unescape(text: String): String {
    val result = StringBuilder(text.length)
    var i = 0
    val n = text.length
    while (i < n) {
        val charAt = text[i]
        if (charAt != '&') {
            result.append(charAt)
            i++
        } else {
            when {
                text.startsWith("&amp;", i) -> {
                    result.append('&')
                    i += 5
                }

                text.startsWith("&apos;", i) -> {
                    result.append('\'')
                    i += 6
                }

                text.startsWith("&quot;", i) -> {
                    result.append('"')
                    i += 6
                }

                text.startsWith("&lt;", i) -> {
                    result.append('<')
                    i += 4
                }

                text.startsWith("&gt;", i) -> {
                    result.append('>')
                    i += 4
                }

                else -> i++
            }
        }
    }
    return result.toString()
}
