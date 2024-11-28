package io.docops.docopsextensionssupport.gallery

import io.docops.docopsextensionssupport.adr.model.escapeXml
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
    @GetMapping("/badge/gallery.html")
    fun badgeGallery(model: Model): String {
        model.addAttribute("imageUrlGenerator", ImageUrlGenerator())
        return "badge/gallery"
    }

    @GetMapping("/galleryItem.html")
    fun galleryItem(@RequestParam("name") name: String, @RequestParam("kind") kind: String, @RequestParam("type") type: String): ResponseEntity<String> {
        val imageUrlGenerator = ImageUrlGenerator(name)
        val obj = """
            <object type="image/svg+xml" data="${imageUrlGenerator.getUrl(name= "$type/$name", kind = kind)}">
                <img src="${imageUrlGenerator.getUrl(name="$type/$name", kind)}"/>
            </object>
            <div id="json-target" hx-swap-oob="true"></div>
            <a hx-swap-oob="true" id="targetJsonView" href="#" data-hx-get="galleryJsonView.html?name=${name}&type=$type" data-hx-target="#json-target">View JSON</a>
        """.trimIndent()
        return ResponseEntity(obj, HttpStatus.OK)
    }

    @GetMapping("/galleryJsonView.html", produces = [MediaType.TEXT_HTML_VALUE])
    fun jsonView(@RequestParam(name = "name", required = true) name: String, @RequestParam("type") type: String): ResponseEntity<String> {
        try {
            val headers = HttpHeaders()
            headers.set("HX-Trigger", """{"button-click": {"element": "$name"}}""")
            val json =  applicationContext.getResource("classpath:gallery/$type/$name")
            //language=html
            return ResponseEntity("""<div>
                    <pre><code id="dataView">${json.getContentAsString(Charset.defaultCharset()).escapeXml()}</code></pre>
                
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