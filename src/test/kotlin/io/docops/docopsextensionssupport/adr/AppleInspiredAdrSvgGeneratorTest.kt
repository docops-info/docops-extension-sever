package io.docops.docopsextensionssupport.adr

import io.docops.docopsextensionssupport.web.CsvResponse
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

class AppleInspiredAdrSvgGeneratorTest {

    @Test
    fun `given adr when apple inspired svg generated then it has apple design features`() {
        val generator = AppleInspiredAdrSvgGenerator(useDark = false, themeName = "apple")
        
        val adr = Adr(
            title = "Adopt Swift-style Design for SVG Generation",
            status = AdrStatus.Accepted,
            date = "2023-11-01",
            context = listOf(
                "Our current SVG generators follow different styles.",
                "We want to provide a flagship design option that mimics the Apple ecosystem's elegance and clarity."
            ),
            decision = listOf(
                "Implement a new generator called AppleInspiredAdrSvgGenerator.",
                "Focus on SF Pro typography, large whitespace, and subtle card shadows."
            ),
            consequences = listOf(
                "Premium look and feel for architectural documentation.",
                "Better readability due to improved typography hierarchy.",
                "Increased maintenance if Apple radically changes its design language."
            ),
            participants = listOf(
                Participant("Steve Roach", "Principal Architect", "steve.roach@example.com", emoji = "🍎"),
                Participant("Design Team", "UX/UI", emoji = "🎨")
            ),
            references = listOf(
                WikiLink("Apple Human Interface Guidelines", "https://developer.apple.com/design/human-interface-guidelines/")
            )
        )
        
        val svg = generator.generate(adr)
        
        // Ensure directory exists for generated files
        File("gen").mkdirs()
        File("gen/adr_apple_inspired.svg").writeText(svg)
        
        // Assertions based on Apple design guidelines
        assertTrue(svg.contains("font-family=\"'SF Pro Display'"), "Should prefer SF Pro Display font")
        assertTrue(svg.contains("font-size=\"44\""), "Title should be large (44px)")
        assertTrue(svg.contains("rx=\"28\""), "Cards should have large corner radius (28px)")
        assertTrue(svg.contains("appleShadow"), "Should have custom Apple shadow filter")
        assertTrue(svg.contains("fill=\"#007AFF\""), "Accepted status should use Apple System Blue (via statusColor for Accepted is blue?) Wait, Accepted is Green.")
        
        // Wait, I set Accepted to Green in my generator. Let's check.
        // AdrStatus.Accepted -> "#34C759" // System Green
        assertTrue(svg.contains("fill=\"#34C759\""), "Accepted status should use Apple System Green")
    }

    @Test
    fun `given adr when apple inspired dark svg generated then it uses dark mode tokens`() {
        val generator = AppleInspiredAdrSvgGenerator(useDark = true, themeName = "apple")

        val adr = Adr(
            title = "Apple Inspired Dark Mode",
            status = AdrStatus.Proposed,
            date = "2023-11-01",
            context = listOf("Testing dark mode"),
            decision = listOf("Use dark tokens"),
            consequences = listOf("Looks great at night")
        )

        val svg = generator.generate(adr)
        File("gen/adr_apple_inspired_dark.svg").writeText(svg)

        assertTrue(svg.contains("stop-color=\"#000000\""), "Dark wash start should be #000000")
        assertTrue(svg.contains("fill=\"#FFFFFF\""), "Header text should be #FFFFFF in dark mode")
        assertTrue(svg.contains("fill=\"#1C1C1E\""), "Cards should be #1C1C1E in dark mode")
    }

    @Test
    fun `given participants with emails when apple inspired svg generated then it has Teams chat links`() {
        val generator = AppleInspiredAdrSvgGenerator(useDark = false, themeName = "apple")

        val adr = Adr(
            title = "Teams Chat Test",
            status = AdrStatus.Accepted,
            date = "2023-11-01",
            context = listOf("Testing Teams integration"),
            decision = listOf("Add Teams links"),
            consequences = listOf("Better communication"),
            participants = listOf(
                Participant("Steve Roach", "Architect", "steve.roach@example.com"),
                Participant("John Doe", "Engineer", "john.doe@example.com")
            )
        )

        val svg = generator.generate(adr)
        
        assertTrue(svg.contains("https://teams.microsoft.com/l/chat/0/0?users=steve.roach@example.com,john.doe@example.com"), "Should contain group chat link")
        assertTrue(svg.contains("START GROUP CHAT"), "Should contain group chat button text")
        assertTrue(svg.contains("https://teams.microsoft.com/l/chat/0/0?users=steve.roach@example.com"), "Should contain individual chat link for Steve")
        assertTrue(svg.contains("https://teams.microsoft.com/l/chat/0/0?users=john.doe@example.com"), "Should contain individual chat link for John")
    }

    @Test
    fun `given apple template in payload when handled by AdrHandler then it uses apple generator`() {
        val payload = """
            title=Apple Template Test
            status=Accepted
            date=2023-11-01
            template=apple
            context=Testing the apple template
            decision=It should work
            consequences=Happy users
            participants=John Doe|Engineer
        """.trimIndent()
        
        val handler = AdrHandler(CsvResponse(emptyList(), emptyList()))
        val svg = handler.handleSVG(payload, "1.0", false, "svg")
        
        assertTrue(svg.contains("font-family=\"'SF Pro Display'"), "Should use Apple generator via template flag")
        assertTrue(svg.contains("PARTICIPANTS"), "Should have PARTICIPANTS section even if empty (based on my implementation)")
    }
}
