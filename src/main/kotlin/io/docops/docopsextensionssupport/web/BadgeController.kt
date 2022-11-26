package io.docops.docopsextensionssupport.web

import io.docops.docopsextensionssupport.badge.Badge
import io.github.dsibilio.badgemaker.core.BadgeFormatBuilder
import io.github.dsibilio.badgemaker.core.BadgeMaker
import io.github.dsibilio.badgemaker.model.BadgeFormat
import io.github.dsibilio.badgemaker.model.NamedColor
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam


@Controller
@RequestMapping("/api")
class BadgeController(private val observationRegistry: ObservationRegistry) {

    @PutMapping("/badge")
    fun getBadge(@RequestBody badge: MutableList<Badge>, servletResponse: HttpServletResponse) {


        val str = gen(badge)
        servletResponse.contentType = "text/html";
        servletResponse.characterEncoding = "UTF-8";
        servletResponse.status = 200
        val writer = servletResponse.writer
        writer.print(str)
        writer.flush()
    }

    private fun gen(badge: MutableList<Badge>): String {
        val svgList = mutableMapOf<Int,String>()
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
        val str= StringBuffer()
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