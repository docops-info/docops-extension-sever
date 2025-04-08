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

import io.docops.docopsextensionssupport.button.shape.joinXmlLines
import io.docops.docopsextensionssupport.svgsupport.uncompressString
import io.github.sercasti.tracing.Traceable
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletResponse
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
class BadgeController @Autowired constructor(private val docOpsBadgeGenerator: DocOpsBadgeGenerator){

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
    fun getBadgeByForm(@RequestBody badge: FormBadge, servletResponse: HttpServletResponse) {
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
        val contents = makeBadgeAndSource(badgeSource, svg)
        servletResponse.contentType = "text/html"
        servletResponse.characterEncoding = "UTF-8"
        servletResponse.status = 200
        val writer = servletResponse.writer
        writer.print(contents)
        writer.flush()
    }

    fun makeBadgeAndSource(txt: String, svg: String): String {
        return """
        <div id='imageblock'>
        $svg
        </div>
        <br/>
        <h3>Badge Source</h3>
        <div class='mb-5'>
        <pre>
        <code class="asciidoc">
$txt
        </code>
        </pre>
        <script>
        var badgeSource = `${txt}`;
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
        //val data = uncompressString(payload)
        val data = (URLDecoder.decode(payload,"UTF-8"))
        val lines = mutableListOf<String>()
        var x = 0.0f
        var y = 0.0f
        val buffer = 5.0f
        val str = StringBuilder()
        var count =0
        data.lines().forEach { line ->
            val split = line.split("|")
            var link = ""
            if (split.size > 2) {
                link = split[2]
            }
            if (split.size != 6) {
                throw BadgeFormatException("Badge Format invalid, expecting 5 pipe delimited values [$data]")
            }
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


            var output = docOpsBadgeGenerator.createBadge(label, message, color, mcolor)
            output = output.replace("id='m'", "id='m${count}'")
            output = output.replace("url(#m4)", "url(#m${count++})")
            str.append(
                """<g transform="translate($x, $y)">
                $output
                </g>
            """.trimMargin()
            )
            x += buffer + getXY(output)

        }
        val output = """
            <svg xmlns="http://www.w3.org/2000/svg" width="$x" height="20">
            $str
            </svg>
        """.trimIndent()
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)


        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
       return ResponseEntity(output.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)

    }

    fun getXY(originalSvg: String): Float {
        val hw = findHeightWidth(originalSvg)
        return hw.second.toFloat()
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
        return ResponseEntity(joinXmlLines(svg.toString()).toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
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

fun findHeightWidth(src: String): Pair<String, String> {
    val document =
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ByteArrayInputStream(src.toByteArray()))
    val xpathExpressionHeight = "/svg/@height"
    val height = evaluateXPath(document, xpathExpressionHeight)
    val xpathExpressionWidth = "/svg/@width"
    val width = evaluateXPath(document, xpathExpressionWidth)
    return Pair(height[0], width[0])
}

fun findHeightWidthViewBox(src: String): Pair<String, String> {
    val document =
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ByteArrayInputStream(src.toByteArray()))
    val xpathExpression = "/svg/@viewBox"
    val values = evaluateXPath(document, xpathExpression)
    val items = values[0].split(" ")

    return Pair(items[3], items[2])
}

private fun evaluateXPath(document: Document, xpathExpression: String): List<String> {
    val xpathFactory: XPathFactory = XPathFactory.newInstance()
    val xpath: XPath = xpathFactory.newXPath()
    val values: MutableList<String> = ArrayList()
    try {
        val expr: XPathExpression = xpath.compile(xpathExpression)
        val nodes: NodeList = expr.evaluate(document, XPathConstants.NODESET) as NodeList
        for (i in 0 until nodes.length) {


            //Customize the code to fetch the value based on the node type and hierarchy
            values.add(nodes.item(i).nodeValue)
        }
    } catch (e: XPathExpressionException) {
        e.printStackTrace()
    }
    return values
}