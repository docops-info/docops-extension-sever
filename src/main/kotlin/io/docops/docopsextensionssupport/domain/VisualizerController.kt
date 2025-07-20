package io.docops.docopsextensionssupport.domain

import io.docops.docopsextensionssupport.domain.model.MarkupRequest
import io.docops.docopsextensionssupport.domain.model.SvgResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/visualizer")
class VisualizerController(
    private val markupParser: MarkupParser,
    private val svgGenerator: SvgGenerator
) {

    @PostMapping("/generate-svg")
    fun generateSvg(@RequestBody request: MarkupRequest): ResponseEntity<SvgResponse> {
        try {
            val structure = markupParser.parseMarkup(request.markup)
            val svg = svgGenerator.generateSvg(structure)

            // Extract dimensions from SVG (simple regex)
            val widthMatch = Regex("width=\"(\\d+)\"").find(svg)
            val heightMatch = Regex("height=\"(\\d+)\"").find(svg)

            val width = widthMatch?.groupValues?.get(1)?.toIntOrNull() ?: 800
            val height = heightMatch?.groupValues?.get(1)?.toIntOrNull() ?: 600

            return ResponseEntity.ok(SvgResponse(svg, width, height))
        } catch (e: Exception) {
            return ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/example")
    fun getExample(): ResponseEntity<String> {
        val example = """
            # Technology [blue]
            ## Software Development [green]
            ### Frontend Development
            ### Backend Development
            ### DevOps & Infrastructure

            ## Data & Analytics [purple]
            ### Data Science
            ### Business Intelligence
            ### Machine Learning

            ---

            # Operations [orange]
            ## Human Resources [red]
            ### Recruitment
            ### Training & Development
            ### Performance Management

            ## Finance & Accounting [yellow]
            ### Financial Planning
            ### Accounts Payable
            ### Accounts Receivable

            # Marketing [pink]
            ## Digital Marketing [teal]
            ### Social Media
            ### Content Marketing
            ### SEO & SEM

            ## Traditional Marketing [indigo]
            ### Print Advertising
            ### Event Marketing
            ### Public Relations
        """.trimIndent()

        return ResponseEntity.ok(example)
    }
}