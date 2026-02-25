package io.docops.docopsextensionssupport.web

import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Controller
class SkillsController {

    @GetMapping("/skills.zip", produces = ["application/zip"])
    fun downloadSkills(response: HttpServletResponse) {
        response.setHeader("Content-Disposition", "attachment; filename=\"skills.zip\"")
        val skillsDir = ClassPathResource("skills").file
        ZipOutputStream(response.outputStream).use { zos ->
            skillsDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val entryName = "skills/" + file.relativeTo(skillsDir).path
                    val entry = ZipEntry(entryName)
                    zos.putNextEntry(entry)
                    file.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }
        }
    }
}