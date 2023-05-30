package io.docops.docopsextensionssupport.badge

import com.starxg.badge4j.Badge
import io.docops.docopsextensionssupport.svgsupport.SvgToPng
import io.docops.docopsextensionssupport.web.panel.uncompressString
import io.micrometer.core.annotation.Timed
import io.micrometer.observation.annotation.Observed
import jakarta.servlet.http.HttpServletResponse
import org.silentsoft.badge4j.Style
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.*


@Controller
@RequestMapping("/api")
@Observed(name = "badge.controller")
class BadgeController @Autowired constructor(private val docOpsBadgeGenerator: DocOpsBadgeGenerator){


    @PutMapping("/badge/item", produces = ["image/svg+xml"])
    @ResponseBody
    @Timed(value = "docops.badge.put", histogram = true, percentiles = [0.5, 0.95])
    fun getBadgeByForm(@RequestBody badge: FormBadge, servletResponse: HttpServletResponse) {
        var fillColor = badge.messageColor
        if (null == fillColor) {
            fillColor = "GREEN"
        }
        //val src = makeBadge(message = badge.message, label = badge.label, color = badge.labelColor, mColor)
        //val src = makeBadgeMessageOnly(formBadge = badge)
        val src = Badge.create(badge.label, badge.message, badge.labelColor, badge.messageColor, null, 0, 1)
        //val src = badgeAgain(formBadge = badge, type = "SVG")
        val badgeSource =
            """
[badge]
----
${badge.label}|${badge.message}|${badge.url}|${badge.labelColor}|$fillColor|${badge.logo}
----
""".trimIndent()
        val contents = makeBadgeAndSource(badgeSource, src)
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
        <div class='pure-u-1 pure-u-md-20-24'>
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


    @GetMapping("/badge/item", produces = ["image/svg+xml", "image/png"])
    @Timed(value = "docops.badge.get", histogram = true, percentiles = [0.5, 0.95])
    fun getBadgeParams(
        @RequestParam(name = "payload") payload: String,
        @RequestParam(name = "type", defaultValue = "SVG", required = false) type: String,
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
                val output = docOpsBadgeGenerator.createBadge(label, message, color, mcolor, split[1], logo, fontColor)

                val headers = HttpHeaders()
                headers.cacheControl = CacheControl.noCache().headerValue
                headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
                // return ResponseEntity(output.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
                return if ("SVG" == type) {
                    //val output = Badge.create(label, message, color, mcolor, null, 0, 1)
                    headers.cacheControl = CacheControl.noCache().headerValue
                    headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
                    ResponseEntity(output.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
                } else {
                    headers.cacheControl = CacheControl.noCache().headerValue
                    headers.contentType = MediaType.IMAGE_PNG
                    val res = findHeightWidth(output)
                    val baos = SvgToPng().toPngFromSvg(output, res)
                    ResponseEntity(baos, headers, HttpStatus.OK)
                }
            }
        }

    }



    @PostMapping("/badges", consumes = [MediaType.ALL_VALUE], produces = ["image/svg+xml", "image/png"])
    @Timed(value = "docops.badges.post", histogram = true, percentiles = [0.5, 0.95])
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


            var output = Badge.create(label, message, color, mcolor)
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