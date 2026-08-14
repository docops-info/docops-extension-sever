package io.docops.docopsextensionssupport.adr

import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

class AdrSvgGeneratorTest {

    @Test
    fun `given adr when premium svg generated then it has premium features`() {
        val generator = AdrSvgGenerator(useDark = false, themeName = "premium")
        
        val adr = Adr(
            title = "Use Kotlin for Android Development",
            status = AdrStatus.Accepted,
            date = "2023-10-27",
            context = listOf("We need a modern, expressive language for Android.", "Java is becoming verbose."),
            decision = listOf("Use Kotlin as the primary language."),
            consequences = listOf("Faster development", "Better safety"),
            participants = listOf(
                Participant("John Doe", "Lead Engineer", "john.doe@example.com", emoji = "👨‍💻"),
                Participant("Jane Smith", "Designer", emoji = "🎨")
            )
        )
        
        val svg = generator.generate(adr)
        
        // Ensure directory exists for generated files
        File("gen").mkdirs()
        File("gen/adr_premium.svg").writeText(svg)
        
        // Assertions based on premium guidelines
        assertTrue(svg.contains("font-family: 'Inter'"), "Premium theme should use Inter font")
        assertTrue(svg.contains("font-size: 24px"), "Title should be 24px")
        assertTrue(svg.contains("font-weight: 700"), "Title should be bold (700)")
        assertTrue(svg.contains("letter-spacing: -0.02em"), "Title should have -0.02em tracking")
        
        assertTrue(svg.contains("rx=\"12\""), "Cards should have 12px corner radius")
        assertTrue(svg.contains("stroke: #E5E7EB"), "Cards should have light grey border")
        
        // Check status badge colors
        assertTrue(svg.contains("fill=\"#DCFCE7\""), "Accepted status should have green tint bg")
        assertTrue(svg.contains("fill=\"#166534\""), "Accepted status text should be high-contrast green")
        
        // Check participants
        assertTrue(svg.contains("r=\"16\""), "Participant avatars should be 32px diameter (r=16)")
        assertTrue(svg.contains("class=\"participant-icon\""), "Should have participant-icon class")
        
        // Accessibility
        assertTrue(svg.contains("<title>Use Kotlin for Android Development</title>"), "Should have SVG title")
        assertTrue(svg.contains("<desc>Architecture Decision Record: Use Kotlin for Android Development - Status: Accepted</desc>"), "Should have SVG description")
    }

    @Test
    fun `given adr when premium dark svg generated then it uses dark tokens`() {
        val generator = AdrSvgGenerator(useDark = true, themeName = "premium")
        
        val adr = Adr(
            title = "Use Kotlin for Android Development",
            status = AdrStatus.Proposed,
            date = "2023-10-27",
            context = listOf("Context line"),
            decision = listOf("Decision line"),
            consequences = listOf("Consequence line")
        )
        
        val svg = generator.generate(adr)
        File("gen/adr_premium_dark.svg").writeText(svg)
        
        assertTrue(svg.contains("fill: rgba(31, 41, 55, 0.8)"), "Dark card should have correct surface token")
        assertTrue(svg.contains("fill: #F9FAFB"), "Dark title should use correct text token")
        assertTrue(svg.contains("fill=\"#FEF3C7\""), "Proposed status should have yellow tint bg")
    }
}
