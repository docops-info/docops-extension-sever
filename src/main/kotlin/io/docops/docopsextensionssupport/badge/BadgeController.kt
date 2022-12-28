package io.docops.docopsextensionssupport.badge

import io.docops.docopsextensionssupport.web.panel.uncompressString

import io.micrometer.core.annotation.Timed
import io.micrometer.observation.annotation.Observed
import jakarta.servlet.http.HttpServletResponse
import org.silentsoft.badge4j.Style
import org.silentsoft.simpleicons.SimpleIcons
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.io.ByteArrayInputStream
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory


@Controller
@RequestMapping("/api")
@Observed(name = "badge.controller")
class BadgeController() {


    @PutMapping("/badge/item", produces = ["image/svg+xml"])
    @ResponseBody
    @Timed(value = "docops.badge.put", percentiles = [0.5, 0.95])
    fun getBadgeByForm(@RequestBody badge: FormBadge, servletResponse: HttpServletResponse) {
        var mColor = badge.messageColor
        if (null == mColor) {
            mColor = "GREEN"
        }
        //val src = makeBadge(message = badge.message, label = badge.label, color = badge.labelColor, mColor)
        val src = badgeAgain(formBadge = badge)
        val badgeSource =
            """
[badge]
----
${badge.label}|${badge.message}|${badge.url}|${badge.labelColor}|$mColor|${badge.logo}
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
        <h3>Adr Source</h3>
        <div class='pure-u-1 pure-u-md-20-24'>
        <pre>
        <code class="kotlin">
$txt
        </code>
        </pre>
        <script>
        var badgeSource = `${txt}`;
        document.querySelectorAll('pre code').forEach((el) => {
            hljs.highlightElement(el);
        });
        </script>
        </div>
    """.trimIndent()
    }


    @GetMapping("/badge/item", produces = ["image/svg+xml"])
    @Timed(value = "docops.badge.get", percentiles = [0.5, 0.95])
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
        var logo = "data:image/svg+xml;charset=utf-8,%3Csvg xmlns%3D'http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg' viewBox%3D'0 0 200 150'%2F%3E"
        if(formBadge.logo!!.isNotBlank()){
            val stuff  = SimpleIcons.get(formBadge.logo)
            if (stuff != null)  {
                val ico = stuff.svg
                val xml = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(ByteArrayInputStream(ico?.toByteArray()))
                var src = ""
                xml?.let {
                    src = manipulateSVG(xml, stuff.hex)
                }
                logo = "data:image/svg+xml;base64," + Base64.getEncoder()
                    .encodeToString(src.toByteArray())
            }
        }

        return org.silentsoft.badge4j.Badge.builder()
            .label(formBadge.label)
            .labelColor(formBadge.labelColor)
            .message(formBadge.message)
            .color(formBadge.messageColor)
            .style(Style.Plastic)
            .links(arrayOf(formBadge.url))
            .logo(logo)
            .build()
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