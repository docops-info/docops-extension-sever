package io.docops.docopsextensionssupport.diagram

import io.docops.docopsextensionssupport.svgsupport.uncompressString
import jakarta.servlet.http.HttpServletResponse
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.URLDecoder
import java.util.Base64


@Controller
@RequestMapping("/api")
class PlantumlJsonController {

    @GetMapping("/plantumljson/{encodedPayload}")
    fun handleRequest(@PathVariable("encodedPayload") encodedPayload: String, response: HttpServletResponse)  {
        val data = Base64.getDecoder().decode(encodedPayload)
        val os = response.outputStream
        response.contentType = "image/svg+xml"
        generateSVG(String(data), os, 1920, 1080)
        os.flush()
        os.close()
    }

    fun generateSVG(plantUMLText: String, outputStream: OutputStream, width: Int, height: Int) {
        try {
            // Create a SourceStringReader
            val reader = SourceStringReader(plantUMLText)

            // Generate the SVG with specified dimensions
            val options = FileFormatOption(FileFormat.SVG, true)
            val desc: String? = reader.outputImage(outputStream, options).description
        } catch (e: IOException) {
            e.printStackTrace()
            throw RuntimeException(e)
        }
    }
}