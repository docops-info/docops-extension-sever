package io.docops.docopsextensionssupport.badge

import com.starxg.badge4j.Badge
import io.docops.docopsextensionssupport.svgsupport.SvgToPng
import io.docops.docopsextensionssupport.web.panel.uncompressString
import io.micrometer.core.annotation.Timed
import io.micrometer.observation.annotation.Observed
import jakarta.servlet.http.HttpServletResponse
import org.silentsoft.badge4j.Style
import org.silentsoft.simpleicons.SimpleIcons
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.*


@Controller
@RequestMapping("/api")
@Observed(name = "badge.controller")
class BadgeController {


    @PutMapping("/badge/item", produces = ["image/svg+xml"])
    @ResponseBody
    @Timed(value = "docops.badge.put", histogram = true, percentiles = [0.5, 0.95])
    fun getBadgeByForm(@RequestBody badge: FormBadge, servletResponse: HttpServletResponse) {
        var fillColor = badge.messageColor
        if (null == fillColor) {
            fillColor = "GREEN"
        }
        //val src = makeBadge(message = badge.message, label = badge.label, color = badge.labelColor, mColor)
        val src = makeBadgeMessageOnly(formBadge = badge)
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
        val data = uncompressString(payload)
        val split = data.split("|")
        when {
            split.size != 6 -> {
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

                val src = badgeAgain(
                    FormBadge(
                        label = label,
                        message = message, url = "", labelColor = color, messageColor = mcolor, logo = logo
                    ), type
                )

                val output = Badge.create(label, message, color, mcolor, null, 0, 1)

                val headers = HttpHeaders()
                headers.cacheControl = CacheControl.noCache().headerValue
                headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
               // return ResponseEntity(output.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
                return if("SVG" == type) {
                    //val output = Badge.create(label, message, color, mcolor, null, 0, 1)

                    val headers = HttpHeaders()
                    headers.cacheControl = CacheControl.noCache().headerValue
                    headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
                    ResponseEntity(output.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
                }else {
                    val headers = HttpHeaders()
                    headers.cacheControl = CacheControl.noCache().headerValue
                    headers.contentType = MediaType.IMAGE_PNG
                    val res = findHeightWidth(src)
                    val baos = SvgToPng().toPngFromSvg(output, res)
                    ResponseEntity(baos, headers, HttpStatus.OK)
                }


            }
        }

    }



    private fun badgeAgain(formBadge: FormBadge, type: String): String {
        val logo = getBadgeLogo(formBadge.logo)
        val builder = org.silentsoft.badge4j.Badge.builder()
            .label(formBadge.labelOrNull())
            .labelColor(formBadge.labelColor)
            .message(formBadge.message)
            .color(formBadge.messageColor)
            .links(arrayOf(formBadge.url))
        when {
            "PDF" == type && formBadge.logo.isNullOrEmpty() -> {
                 builder.style(Style.FlatSquare)
            }
            else -> {
                builder.style(Style.Plastic)
                builder.logo(logo)
            }
        }
        return builder.build()

    }

    private fun makeBadgeMessageOnly(formBadge: FormBadge): String {
        val logo = getBadgeLogo(formBadge.logo)

        return org.silentsoft.badge4j.Badge.builder()
            .label(formBadge.labelOrNull())
            .labelColor(formBadge.labelColor)
            .message(formBadge.message)
            .color(formBadge.messageColor)
            .style(Style.Plastic)
            .links(arrayOf(formBadge.url))
            .logo(logo)
            .build()
    }

    fun getBadgeLogo(input: String?): String {
        //http://docops.io/images/docops.svg
        var logo =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII="
        input?.let {
            if (input.startsWith("<") && input.endsWith(">")) {

                val simpleIcon = SimpleIcons.get(input.replace("<", "").replace(">", ""))
                if (simpleIcon != null) {
                    val ico = simpleIcon.svg
                    val xml = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .parse(ByteArrayInputStream(ico?.toByteArray()))
                    var src = ""
                    xml?.let {
                        src = manipulateSVG(xml, simpleIcon.hex)
                    }
                    logo = "data:image/svg+xml;base64," + Base64.getEncoder()
                        .encodeToString(src.toByteArray())
                }
            } else if (input.startsWith("http")) {
                logo = getLogoFromUrl(input)
                logo = "data:image/svg+xml;base64," + Base64.getEncoder()
                    .encodeToString(logo.toByteArray())
            }
        }
        return logo
    }

    fun getLogoFromUrl(url: String): String {
        val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(20))
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(10))
            .build()
        return try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            response.body()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
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
    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ByteArrayInputStream(src.toByteArray()))
    val xpathExpressionHeight = "/svg/@height"
   val height =  evaluateXPath(document, xpathExpressionHeight)
    val xpathExpressionWidth = "/svg/@width"
    val width =  evaluateXPath(document, xpathExpressionWidth)
    return Pair(height[0], width[0])
}
fun findHeightWidthViewBox(src: String): Pair<String, String> {
    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ByteArrayInputStream(src.toByteArray()))
    val xpathExpression = "/svg/@viewBox"
    val values =  evaluateXPath(document, xpathExpression)
    println(values[0].split(" "))
    val xpathExpressionWidth = "/svg/@width"
    val width =  evaluateXPath(document, xpathExpressionWidth)
    return Pair("800", "970")
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