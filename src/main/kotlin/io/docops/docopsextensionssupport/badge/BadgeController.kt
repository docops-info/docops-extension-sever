package io.docops.docopsextensionssupport.badge

import io.docops.docopsextensionssupport.web.panel.uncompressString
import io.github.dsibilio.badgemaker.core.BadgeFormatBuilder
import io.github.dsibilio.badgemaker.core.BadgeMaker
import io.github.dsibilio.badgemaker.model.BadgeFormat
import io.github.dsibilio.badgemaker.model.NamedColor
import io.micrometer.core.annotation.Timed
import io.micrometer.observation.annotation.Observed
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*


@Controller
@RequestMapping("/api")
@Observed(name = "badge.controller")
class BadgeController() {

    @PutMapping("/badge", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    @Timed(value = "docops.badge.put", percentiles = [0.5, 0.95])
    fun getBadge(@RequestBody badge: MutableList<Badge>): String {
        return gen(badge)
    }

    @PutMapping("/badge/item", produces = ["image/svg+xml"])
    @ResponseBody
    @Timed(value = "docops.badge.put", percentiles = [0.5, 0.95])
    fun getBadgeByForm(@RequestBody badge: FormBadge, servletResponse: HttpServletResponse) {
        val src = makeBadge(message = badge.message, label = badge.label, color = null, "GREEN")
        servletResponse.contentType = "image/svg+xml";
        servletResponse.characterEncoding = "UTF-8";
        servletResponse.status = 200
        val writer = servletResponse.writer
        writer.print(src)
        writer.flush()
    }

    private fun makeBadge(message: String, label: String, color: String?, mColor: String): String {

        val clr: NamedColor = if (color != null) {
            NamedColor.valueOf(color)
        } else {
            val rands = (0 until 9).random()
            NamedColor.values()[rands]
        }

        val fmt: BadgeFormat = BadgeFormatBuilder(message)
            .withLabel(label)
            .withLabelColor(clr) // left-side background color (default: GREY)
            .withMessageColor(NamedColor.valueOf(mColor)) // right-side background color (default: BRIGHTGREEN)
            .withScaleMultiplier(1) // the scale factor of the rendered badge (default: 1, min: 1, max: 10000)
            .withLogo("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=")
            .build()
        return BadgeMaker.makeBadge(fmt)
    }

    @GetMapping("/badge/item", produces = ["image/svg+xml"])
    @Timed(value = "docops.badge.get", percentiles = [0.5, 0.95])
    fun getBadgeParams(@RequestParam payload: String, servletResponse: HttpServletResponse) {
        val data = uncompressString(payload)
        val split = data.split("|")
        when {
            split.size != 5 -> {
                throw BadgeFormatException("Badge Format invalid, expecting 4 pipe delimited values [$data]")
            }

            else -> {
                val message: String = split[1]
                val label: String = split[0]

                var mcolor: String = "GREEN"

                var color: String = split[3].trim()


                val c = split[4].trim()
                if (c.isNotEmpty()) {
                    mcolor = c
                }


                val src = makeBadge(message = message, label = label, color, mcolor)
                servletResponse.contentType = "image/svg+xml"
                servletResponse.characterEncoding = "UTF-8"
                servletResponse.status = 200
                val writer = servletResponse.writer
                writer.print(src)
                writer.flush()

            }
        }

    }

    private fun gen(badge: MutableList<Badge>): String {
        val svgList = mutableMapOf<Int, String>()
        badge.sortByDescending { it.label.length }
        badge.forEach {
            val rands = (0 until 9).random()
            val fmt: BadgeFormat = BadgeFormatBuilder(it.message.toString())
                .withLabel(it.label)
                .withLabelColor(NamedColor.values()[rands]) // left-side background color (default: GREY)
                .withMessageColor(NamedColor.BRIGHTGREEN) // right-side background color (default: BRIGHTGREEN)
                .withScaleMultiplier(1) // the scale factor of the rendered badge (default: 1, min: 1, max: 10000)
                .build()
            //language=html
            svgList[it.message] = """<div><a href="${it.url}">${BadgeMaker.makeBadge(fmt)}</a></div>"""
        }
        val sorted = svgList.toSortedMap(compareByDescending { it })
        val str = StringBuffer()
        svgList.forEach { (_, v) ->
            str.append(v)
        }
        //language=html
        return """
            <div>
                $str
            </div> 
        """.trimIndent()
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