package io.docops.docopsextensionssupport.badge

import io.docops.docopsextensionssupport.web.panel.uncompressString
import io.micrometer.core.annotation.Timed
import io.micrometer.observation.annotation.Observed
import jakarta.servlet.http.HttpServletResponse
import org.silentsoft.badge4j.Style
import org.silentsoft.simpleicons.SimpleIcons
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.io.ByteArrayInputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory


@Controller
@RequestMapping("/api")
@Observed(name = "badge.controller")
class BadgeController() {


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
        servletResponse.contentType = "text/html";
        servletResponse.characterEncoding = "UTF-8";
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


    @GetMapping("/badge/item", produces = ["image/svg+xml"])
    @Timed(value = "docops.badge.get", histogram = true, percentiles = [0.5, 0.95])
    fun getBadgeParams(@RequestParam payload: String, servletResponse: HttpServletResponse) {
        val data = uncompressString(payload)
        val split = data.split("|")
        when {
            split.size != 6 -> {
                throw BadgeFormatException("Badge Format invalid, expecting 5 pipe delimited values [$data]")
            }

            else -> {
                val message: String = split[1]
                val label: String = split[0]

                var mcolor: String = "GREEN"

                val color: String = split[3].trim()


                val c = split[4].trim()
                if (c.isNotEmpty()) {
                    mcolor = c
                }

                val logo = split[5].trim()


                val src = badgeAgain(FormBadge(label = label,
                    message = message, url =  "", labelColor = color, messageColor = mcolor, logo = logo ))
                servletResponse.contentType = "image/svg+xml"
                servletResponse.characterEncoding = "UTF-8"
                servletResponse.status = 200
                val writer = servletResponse.writer
                writer.print(src)
                writer.flush()

            }
        }

    }


    private fun badgeAgain(formBadge: FormBadge): String {
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
        var logo = "data:image/svg+xml;charset=utf-8,%3Csvg xmlns%3D'http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg' viewBox%3D'0 0 200 150'%2F%3E"
        input?.let {
            if (input.startsWith("<") && input.endsWith(">")) {

                val simpleIcon = SimpleIcons.get(input.replace("<","")  .replace(">","") )
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
    fun getLogoFromUrl(url: String,): String {
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
            if (text.startsWith("&amp;", i)) {
                result.append('&')
                i += 5
            } else if (text.startsWith("&apos;", i)) {
                result.append('\'')
                i += 6
            } else if (text.startsWith("&quot;", i)) {
                result.append('"')
                i += 6
            } else if (text.startsWith("&lt;", i)) {
                result.append('<')
                i += 4
            } else if (text.startsWith("&gt;", i)) {
                result.append('>')
                i += 4
            } else i++
        }
    }
    return result.toString()
}