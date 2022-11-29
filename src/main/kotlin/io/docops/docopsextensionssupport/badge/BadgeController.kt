package io.docops.docopsextensionssupport.badge

import io.github.dsibilio.badgemaker.core.BadgeFormatBuilder
import io.github.dsibilio.badgemaker.core.BadgeMaker
import io.github.dsibilio.badgemaker.model.BadgeFormat
import io.github.dsibilio.badgemaker.model.NamedColor
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*


@Controller
@RequestMapping("/api")
class BadgeController(private val observationRegistry: ObservationRegistry) {

    @PutMapping("/badge", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun getBadge(@RequestBody badge: MutableList<Badge>): String {
        return gen(badge)
    }

    @PutMapping("/badge/item", produces = ["image/svg+xml"])
    fun getBadgeByForm(@RequestBody badge: FormBadge, servletResponse: HttpServletResponse) {
        return Observation.createNotStarted("docops.badge.put", observationRegistry).observe {
            val src = makeBadge(message=badge.message, label=badge.label, color = null)
            servletResponse.contentType = "image/svg+xml";
            servletResponse.characterEncoding = "UTF-8";
            servletResponse.status = 200
            val writer = servletResponse.writer
            writer.print(src)
            writer.flush()
        }
    }

    private fun makeBadge(message: String, label: String, color: String?): String {

        val clr: NamedColor = if(color != null) {
            NamedColor.valueOf(color)
        } else {
            val rands = (0 until 9).random()
            NamedColor.values()[rands]
        }

        val fmt: BadgeFormat = BadgeFormatBuilder(message)
            .withLabel(label)
            .withLabelColor(clr) // left-side background color (default: GREY)
            .withMessageColor(NamedColor.BRIGHTGREEN) // right-side background color (default: BRIGHTGREEN)
            .withScaleMultiplier(1) // the scale factor of the rendered badge (default: 1, min: 1, max: 10000)
            .build()
        return BadgeMaker.makeBadge(fmt)
    }
    @GetMapping("/badge/item", produces = ["image/svg+xml"])
    fun getBadgeByUrl(servletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
        val map = servletRequest.queryString.replace("&amp;", "&")
            .split("&")
            .map { it.split("=") }.associate { it.first() to it.last() }
        println(map)
        val message: String = map["message"]as String
        val label: String = map["label"] as String
        val color: String? = map["color"]
        return Observation.createNotStarted("docops.badge.get", observationRegistry).observe {
            val src = makeBadge(message=message, label=label, color)
            servletResponse.contentType = "image/svg+xml";
            servletResponse.characterEncoding = "UTF-8";
            servletResponse.status = 200
            val writer = servletResponse.writer
            writer.print(src)
            writer.flush()
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
        svgList.forEach { _, v ->
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