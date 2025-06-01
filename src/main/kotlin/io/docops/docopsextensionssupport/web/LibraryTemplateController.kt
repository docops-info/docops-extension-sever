package io.docops.docopsextensionssupport.web

import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import io.micrometer.observation.annotation.Observed
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Controller for handling library template operations.
 * 
 * This controller provides functionality to download and customize the index.html template.
 */
@Controller
@Observed(name = "library.template.controller")
class LibraryTemplateController {

    /**
     * Displays the template customization form.
     * 
     * @param model The Spring MVC model
     * @return The name of the view to render
     */
    @GetMapping("/template/customize")
    @Counted
    @Timed(value = "docops.template.customize")
    fun customizeTemplate(model: Model): String {
        // Add default values to the model
        model.addAttribute("title", "My Library Documentation")
        model.addAttribute("favicon", "images/favicon.svg")
        model.addAttribute("logoUrl", "images/docops.svg")
        model.addAttribute("logoAlt", "Library Logo")
        model.addAttribute("headerTitle", "My Library Documentation")
        model.addAttribute("headerDescription", "Documentation for my awesome library")
        model.addAttribute("getStartedButtonText", "Get Started")
        model.addAttribute("githubUrl", "https://github.com/yourusername/your-repo")
        model.addAttribute("githubButtonText", "GitHub")
        model.addAttribute("heroTitle", "Beautiful Documentation For Your Library")
        model.addAttribute("heroDescription", "Create stunning documentation for your library with just a few lines of code. Lightweight, flexible, and powerful.")
        model.addAttribute("getStartedTitle", "Get Started")
        model.addAttribute("getStartedDescription", "Add the library to your project to get started. Follow these steps:")
        model.addAttribute("mavenTitle", "Maven")
        model.addAttribute("mavenCode", "<dependency>\n  <groupId>com.example</groupId>\n  <artifactId>my-library</artifactId>\n  <version>1.0.0</version>\n</dependency>")
        model.addAttribute("gradleTitle", "Gradle")
        model.addAttribute("gradleCode", "implementation 'com.example:my-library:1.0.0'")
        model.addAttribute("usageTitle", "Using the Library")
        model.addAttribute("usageDescription", "After adding the dependency to your project, you can use the library as follows:")
        model.addAttribute("usageCode", "// In your code\nimport com.example.library.MyLibrary;\n\n// Initialize the library\nMyLibrary library = new MyLibrary();\n\n// Use the library\nlibrary.doSomething();")
        model.addAttribute("usageFooter", "Check out the examples below to see the various features you can use.")
        model.addAttribute("featuresTitle", "Core Features")
        model.addAttribute("footerTitle", "My Library")
        model.addAttribute("footerDescription", "A powerful library for your projects.")
        model.addAttribute("footerLogoUrl", "images/docops.svg")
        model.addAttribute("footerLogoAlt", "Library Logo")
        model.addAttribute("footerCol1Title", "Documentation")
        model.addAttribute("footerCol1Links", "<li><a href=\"#get-started\" class=\"hover:text-white\">Getting Started</a></li>\n<li><a href=\"#\" class=\"hover:text-white\">API Reference</a></li>")
        model.addAttribute("footerCol2Title", "Community")
        model.addAttribute("footerCol2Links", "<li><a href=\"#\" class=\"hover:text-white\">GitHub</a></li>\n<li><a href=\"#\" class=\"hover:text-white\">Discord</a></li>")
        model.addAttribute("footerCol3Title", "Resources")
        model.addAttribute("footerCol3Links", "<li><a href=\"#\" class=\"hover:text-white\">Changelog</a></li>\n<li><a href=\"#\" class=\"hover:text-white\">Roadmap</a></li>")
        model.addAttribute("copyright", "&copy; 2025 My Library. All rights reserved.")
        
        return "template/customize"
    }

    /**
     * Generates a customized template based on user input and returns it for download.
     * 
     * @param params Map of template parameters
     * @return ResponseEntity containing the customized template
     */
    @PostMapping("/template/download")
    @ResponseBody
    @Counted
    @Timed(value = "docops.template.download")
    fun downloadTemplate(@RequestParam allParams: Map<String, String>): ResponseEntity<ByteArray> {
        // Read the template file
        val resource = ClassPathResource("static/index.template.html")
        var templateContent = String(resource.inputStream.readAllBytes(), StandardCharsets.UTF_8)
        
        // Replace placeholders with user-provided values
        for ((key, value) in allParams) {
            templateContent = templateContent.replace("\${$key}", value)
        }
        
        // Set up the response headers for download
        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        headers.setContentDispositionFormData("attachment", "index.html")
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(templateContent.toByteArray(StandardCharsets.UTF_8))
    }

    /**
     * Provides a direct download of the template without customization.
     * 
     * @return ResponseEntity containing the template
     */
    @GetMapping("/template/download")
    @ResponseBody
    @Counted
    @Timed(value = "docops.template.download.direct")
    fun downloadTemplateDirectly(): ResponseEntity<ByteArray> {
        // Read the template file
        val resource = ClassPathResource("static/index.template.html")
        val templateContent = resource.inputStream.readAllBytes()
        
        // Set up the response headers for download
        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        headers.setContentDispositionFormData("attachment", "index.template.html")
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(templateContent)
    }

    /**
     * Previews the template with the provided parameters.
     * 
     * @param params Map of template parameters
     * @param response HTTP response
     */
    @PostMapping("/template/preview")
    @ResponseBody
    @Counted
    @Timed(value = "docops.template.preview")
    fun previewTemplate(@RequestParam allParams: Map<String, String>, response: HttpServletResponse) {
        // Read the template file
        val resource = ClassPathResource("static/index.template.html")
        var templateContent = String(resource.inputStream.readAllBytes(), StandardCharsets.UTF_8)
        
        // Replace placeholders with user-provided values
        for ((key, value) in allParams) {
            templateContent = templateContent.replace("\${$key}", value)
        }
        
        // Set response content type and write the template
        response.contentType = MediaType.TEXT_HTML_VALUE
        response.characterEncoding = StandardCharsets.UTF_8.name()
        response.writer.write(templateContent)
        response.writer.flush()
    }
}