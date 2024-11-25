package io.docops.docopsextensionssupport.gallery

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.nio.charset.Charset

@Controller
class GalleryController (private val applicationContext: ApplicationContext){

    @GetMapping("/gallery.html")
    fun gallery(model: Model): String {
        model.addAttribute("imageUrlGenerator", ImageUrlGenerator())
        return "chart/gallery"
    }

    @GetMapping("/galleryItem.html")
    fun galleryItem(@RequestParam("name") name: String, @RequestParam("kind") kind: String, response: HttpServletResponse) {
        val imageUrlGenerator = ImageUrlGenerator(name)
        val obj = """
            <object type="image/svg+xml" data="${imageUrlGenerator.getUrl("chart/$name.json", kind)}">
                <img src="${imageUrlGenerator.getUrl("chart/$name.json", kind)}"/>
            </object>
            <div id="json-target" hx-swap-oob="true"></div>
            <a hx-swap-oob="true" id="targetJsonView" href="#" data-hx-get="galleryJsonView.html?name=${name}" data-hx-target="#json-target">View JSON</a>
        """.trimIndent()
        response.outputStream.write(obj.toByteArray())
    }

    @GetMapping("/galleryJsonView.html", produces = [MediaType.TEXT_HTML_VALUE])
    fun jsonView(@RequestParam(name = "name", required = true) name: String): ResponseEntity<String> {
        try {
            val headers = HttpHeaders()
            headers.set("HX-Trigger", """{"button-click": {"element": "$name"}}""")
            val json =  applicationContext.getResource("classpath:gallery/chart/$name.json")
            //language=html
            return ResponseEntity("""<div>
                    <pre><code id="dataView">${json.getContentAsString(Charset.defaultCharset())}</code></pre>
                
                </div>
                <script>
                document.querySelectorAll('pre code').forEach((el) => {
                    hljs.highlightElement(el);
                });
                </script>
                """.trimMargin(), headers, HttpStatus.OK)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}