package io.docops.docopsextensionssupport.scorecard

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class ScoreCardMakerTest {

    private fun samplePayload(): String = """
        title=Software Release v2.4.0 - Feature & Bug Summary
        subtitle=Migration from Legacy System to Modern Architecture
        ---
        
        [before]
        title=BEFORE v2.4.0
        ---
        [before.items]
        === Feature Status
        Dark Mode Theme | Missing feature affecting user experience
        Multi-language Support | Not available, limiting global reach
        Advanced Search Filters | Basic search only, slow performance
        Export to PDF | Feature not implemented
        Two-Factor Authentication | Security vulnerability exists
        API Rate Limiting | No protection against abuse
        === Known Issues
        Login timeout issues | Users frequently logged out
        Memory leaks in dashboard | System becomes slow over time
        File upload corruption | Files sometimes corrupted during upload
        Mobile UI overlapping | Interface broken on mobile devices
        Database connection drops | Intermittent connection failures
        Email notifications failing | Users not receiving important updates
        Report generation errors | Reports fail to generate properly
        ---
        
        [after]
        title=AFTER v2.4.0
        ---
        [after.items]
        === New Features Added
        Dark Mode Theme | Implemented with user preference saving
        Multi-language Support | Added 12 languages with automatic detection
        Advanced Search Filters | Fast indexing with multiple filter options
        Export to PDF | High-quality PDF export with custom templates
        Two-Factor Authentication | TOTP and SMS-based 2FA implemented
        API Rate Limiting | Intelligent rate limiting with user tiers
        === Bugs Resolved
        Login timeout issues | Session management completely rewritten
        Memory leaks in dashboard | React components optimized, memory usage -67%
        File upload corruption | New chunked upload system with integrity checks
        Mobile UI overlapping | Responsive design overhaul completed
        Database connection drops | Connection pooling and retry logic implemented
        Email notifications failing | New email service with 99.9% delivery rate
        Report generation errors | Async report generation with progress tracking
    """.trimIndent()

    @ParameterizedTest
    @CsvSource(
        value = [
            "false,1.0,gen/scorecard_light_1.0.svg",
            "true,1.0,gen/scorecard_dark_1.0.svg",
            "false,1.5,gen/scorecard_light_1.5.svg",
            "true,0.75,gen/scorecard_dark_0.75.svg"
        ]
    )
    fun `parses payload and generates valid svg`(useDark: Boolean, scale: String, outputPath: String) {
        val payload = samplePayload()

        val parser = ScoreCardParser()

        val model = parser.parse(payload, useDark, scale)

        // Basic model sanity checks
        assertEquals("Software Release v2.4.0 - Feature & Bug Summary — Migration from Legacy System to Modern Architecture", model.title)
        assertTrue(model.beforeSections.isNotEmpty(), "Before sections should not be empty")
        assertTrue(model.afterSections.isNotEmpty(), "After sections should not be empty")

        // Expect two groups: Feature Status + Known Issues in before; New Features Added + Bugs Resolved in after
        val beforeTitles = model.beforeSections.map { it.title }.toSet()
        val afterTitles = model.afterSections.map { it.title }.toSet()
        assertTrue(beforeTitles.contains("Feature Status"))
        assertTrue(beforeTitles.contains("Known Issues"))
        assertTrue(afterTitles.contains("New Features Added"))
        assertTrue(afterTitles.contains("Bugs Resolved"))

        // Count a few items to ensure they were captured
        val beforeCount = model.beforeSections.sumOf { it.items.size }
        val afterCount = model.afterSections.sumOf { it.items.size }
        assertTrue(beforeCount >= 13, "Expected at least 13 before items, got $beforeCount")
        assertTrue(afterCount >= 13, "Expected at least 13 after items, got $afterCount")

        // Make SVG
        val maker = ScoreCardMaker()
        val svg = maker.make(model)

        val f = File(outputPath)
        f.writeBytes(svg.toByteArray())
        // Root is svg and contains escaped ampersands
        assertTrue(svg.startsWith("<svg"), "SVG should start with <svg")
        assertTrue("Feature &amp; Bug Summary" in svg, "Ampersand in title should be escaped")

        // Ensure presence of key text strings in output
        val mustContain = listOf(
            "BEFORE v2.4.0", // header fallback/section
            "AFTER v2.4.0",
            "Feature Status",
            "Known Issues",
            "New Features Added",
            "Bugs Resolved",
            "Dark Mode Theme",
            "Multi-language Support",
            "Advanced Search Filters",
            "Login timeout issues",
            "Memory leaks in dashboard",
            "Export to PDF",
            "Two-Factor Authentication",
            "API Rate Limiting"
        )
        mustContain.forEach { needle ->
            assertTrue(svg.contains(needle), "SVG should contain '$needle'")
        }

        // Validate well-formed XML
        val dbf = DocumentBuilderFactory.newInstance()
        dbf.isNamespaceAware = true
        val builder = dbf.newDocumentBuilder()
        builder.parse(svg.byteInputStream()) // throws if not well-formed
    }
}
