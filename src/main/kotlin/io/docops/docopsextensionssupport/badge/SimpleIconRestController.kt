package io.docops.docopsextensionssupport.badge

import org.silentsoft.simpleicons.SimpleIcons
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/simpleicon")
class SimpleIconRestController {

    @GetMapping("/{slug}", produces = ["image/svg+xml"])
    fun getIcon(@PathVariable slug: String): ResponseEntity<String> {
        return try {
            val icon = SimpleIcons.get(slug)
            val colored = colorizeSVG(icon.svg, icon.hex)
            ResponseEntity.ok(colored)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{slug}/download", produces = ["image/svg+xml"])
    fun downloadIcon(@PathVariable slug: String): ResponseEntity<String> {
        return try {
            val icon = SimpleIcons.get(slug)
            val colored = colorizeSVG(icon.svg, icon.hex)
            ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"$slug.svg\"")
                .body(colored)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }


}

fun colorizeSVG(svg: String, hex: String): String {
    // Replace common uses of currentColor with the brand hex
    var out = svg.replace("currentColor", "#$hex")
    // If there is still no explicit fill attribute anywhere, set it on the root <svg>
    if (!Regex("\\bfill=").containsMatchIn(out)) {
        out = out.replaceFirst("<svg", "<svg fill=\"#$hex\"")
    }
    return out
}