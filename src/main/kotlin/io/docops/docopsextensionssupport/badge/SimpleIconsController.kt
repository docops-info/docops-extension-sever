package io.docops.docopsextensionssupport.badge

import org.silentsoft.simpleicons.SimpleIcons
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.RegexPatternTypeFilter
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.regex.Pattern
import kotlin.collections.emptyList

@Controller
@RequestMapping("/icons")
class SimpleIconsController {

    // Discover all available icon slugs by scanning the SimpleIcons package
    private val allIcons: List<String> by lazy {
        try {
            // Scan classes under org.silentsoft.simpleicons.icons and derive slugs from class names
            val provider = ClassPathScanningCandidateComponentProvider(false)
            provider.addIncludeFilter(RegexPatternTypeFilter(Pattern.compile(".*")))
            val beans = provider.findCandidateComponents("org.silentsoft.simpleicons.icons")
            val slugs = beans.mapNotNull { def ->
                def.beanClassName
                    ?.removePrefix("org.silentsoft.simpleicons.icons.")
                    ?.removeSuffix("Icon")
            }
                .map { it.lowercase() }
                .distinct()
                .sorted()
            if (slugs.isNotEmpty()) slugs else generateKnownIconsList()
        } catch (e: Exception) {
            // Fallback to a curated list of known popular icons
            generateKnownIconsList()
        }
    }

    // Fallback method with known popular icons
    private fun generateKnownIconsList(): List<String> {
        return listOf(
            "apple", "google", "microsoft", "amazon", "facebook", "twitter", "instagram",
            "linkedin", "github", "gitlab", "stackoverflow", "youtube", "android", "ios",
            "java", "kotlin", "python", "javascript", "typescript", "react", "vue", "angular",
            "nodejs", "npm", "docker", "kubernetes", "aws", "azure", "gcp", "firebase",
            "mongodb", "postgresql", "mysql", "redis", "elasticsearch", "jenkins", "travis",
            "circleci", "slack", "discord", "telegram", "whatsapp", "zoom", "teams",
            "chrome", "firefox", "safari", "edge", "opera", "brave", "tor", "thunderbird",
            "vscode", "intellij", "eclipse", "atom", "sublime", "vim", "emacs", "nano",
            "ubuntu", "debian", "centos", "redhat", "archlinux", "fedora", "opensuse",
            "windows", "macos", "linux", "freebsd", "openbsd", "netbsd", "solaris"
        ).sorted()
    }

    // Organize icons by first letter
    private val iconsByLetter: Map<Char, List<String>> by lazy {
        allIcons.groupBy { it.first().uppercaseChar() }
    }

    // First page
    @GetMapping
    fun showIconsHome(model: Model): String {
        val letters = iconsByLetter.keys.sorted()
        model.addAttribute("letters", letters)

        // Default to first letter if available
        val firstLetter = letters.firstOrNull() ?: 'A'
        model.addAttribute("currentLetter", firstLetter)
        model.addAttribute("icons", iconsByLetter[firstLetter] ?: emptyList<String>())

        return "icons/simpleicons"
    }

    // Get icons by letter (for HTMX fragment requests)
    @GetMapping("/letter/{letter}")
    fun getIconsByLetter(@PathVariable letter: Char, model: Model): String {
        val upperLetter = letter.uppercaseChar()
        model.addAttribute("currentLetter", upperLetter)
        model.addAttribute("icons", iconsByLetter[upperLetter] ?: emptyList<String>())

        return "icons/icon-grid"
    }

    // Get icon details (for modal/popup)
    @GetMapping("/details/{slug}")
    fun getIconDetails(@PathVariable slug: String, model: Model): String {
        try {
            val icon = SimpleIcons.get(slug)
            model.addAttribute("slug", slug)
            model.addAttribute("svg", icon.svg)
            model.addAttribute("hex", icon.hex)
            model.addAttribute("title", icon.title)
            model.addAttribute("hasIcon", true)
        } catch (e: Exception) {
            model.addAttribute("slug", slug)
            model.addAttribute("hasIcon", false)
            model.addAttribute("error", "Icon not found or unavailable")
        }

        return "icons/icon-details"
    }

    // Search functionality
    @GetMapping("/search")
    fun searchIcons(@RequestParam query: String, model: Model): String {
        val results = if (query.isBlank()) {
            emptyList()
        } else {
            allIcons.filter { it.contains(query, ignoreCase = true) }
        }

        model.addAttribute("query", query)
        model.addAttribute("icons", results)
        model.addAttribute("resultCount", results.size)

        return "icons/search-results"
    }
}
