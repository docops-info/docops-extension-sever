package io.docops.docopsextensionssupport.web

import jakarta.servlet.http.HttpServletResponse
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.nio.charset.Charset


@RestController
@RequestMapping("/api", produces = ["image/svg+xml"])
class StatsJsonToImageController {

    @PostMapping("jsonToSvg", consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE], produces = [MediaType.TEXT_HTML_VALUE])
    fun makeImage(httpEntity: HttpEntity<String>, servletResponse: HttpServletResponse) {
        val svg = mageImage(httpEntity.body!!)

        extracted(svg, servletResponse)
    }

    private fun extracted(svg: String, servletResponse: HttpServletResponse) {
        val resp = """
                <div id='imageblock'>
                $svg      
                </div>
            """.trimIndent()
        servletResponse.contentType = MediaType.TEXT_HTML_VALUE;
        servletResponse.characterEncoding = "UTF-8";
        servletResponse.status = 200
        val writer = servletResponse.writer
        writer.print(resp)
        writer.flush()
    }

    @GetMapping("jsonToSvg")
    fun getImage(@RequestParam("payload") payload: String, servletResponse: HttpServletResponse) {
        val svg = mageImage(URLDecoder.decode(payload, Charset.defaultCharset()))
        extracted(svg, servletResponse)
    }
    private fun mageImage(httpEntity: String): String {
        //language=puml
        val source = """@startjson
    <style>
    jsonDiagram {
        node {
        BackgroundColor #FFFFFF
        LineColor #12B3DB
        FontColor #0070AD
        FontName Helvetica
        FontSize 12
        FontStyle normal
        RoundCorner 5
        LineThickness 1
        
        separator {
          LineThickness 0.5
          LineColor black
          LineStyle 1;5
        }
      }
      arrow {
        BackGroundColor lightblue
        LineColor green
        LineThickness 2
        LineStyle 2;5
      }
      highlight {
        BackGroundColor red
        FontColor white
        FontStyle italic
      }
    }
    </style>
            $httpEntity
    @endjson""".trimIndent()
        val reader = SourceStringReader(source)
        val os = ByteArrayOutputStream()

        val desc = reader.outputImage(os, FileFormatOption(FileFormat.SVG))
        os.close()

        val svg = String(os.toByteArray(), Charset.forName("UTF-8"))
        return svg
    }


}