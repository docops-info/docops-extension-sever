package io.docops.docopsextensionssupport.web

import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Controller
class SkillsController {

    @GetMapping("/skills.zip", produces = ["application/zip"])
    fun downloadSkills(response: HttpServletResponse) {
        response.setHeader("Content-Disposition", "attachment; filename=\"skills.zip\"")
        val resolver = PathMatchingResourcePatternResolver()
        val resources = resolver.getResources("classpath*:/skills/**")
        ZipOutputStream(response.outputStream).use { zos ->
            resources.forEach { resource ->
                if (resource.isReadable && !resource.description.contains("directory")) {
                    val url = resource.url.toString()
                    val pathInSkills = url.substringAfterLast("/skills/")
                    if (pathInSkills != url) {
                        val entryName = "skills/$pathInSkills"
                        val entry = ZipEntry(entryName)
                        zos.putNextEntry(entry)
                        resource.inputStream.use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            }
        }
    }
}