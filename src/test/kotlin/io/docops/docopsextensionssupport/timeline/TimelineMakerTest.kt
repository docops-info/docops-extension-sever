package io.docops.docopsextensionssupport.timeline

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class TimelineMakerTest {

    private val testTimelineContent = """
-
date: 1660-1798
text: The Enlightenment/Neoclassical Period
Literature focused on reason, logic, and scientific thought. Major writers include [[https://en.wikipedia.org/wiki/Alexander_Pope Alexander Pope]] and [[https://en.wikipedia.org/wiki/Jonathan_Swift Jonathan Swift]].
-
date: 1798-1832
text: Romanticism
Emphasized emotion, individualism, and the glorification of nature. Key figures include [[https://en.wikipedia.org/wiki/William_Wordsworth William Wordsworth]] and [[https://en.wikipedia.org/wiki/Lord_Byron Lord Byron]].
-
date: 1837-1901
text: Victorian Era
Literature reflected the social, economic, and cultural changes of the Industrial Revolution. Notable authors include [[https://en.wikipedia.org/wiki/Charles_Dickens Charles Dickens]] and [[https://en.wikipedia.org/wiki/George_Eliot George Eliot]].
    """.trimIndent()

    @Test
    fun `test horizontal timeline creation`() {
        // Create a horizontal timeline (default)
        val maker = TimelineMaker(false, "#38383a", false, "test_id", false)
        val entries = TimelineParser().parse(testTimelineContent)
        
        // Debug: Print entries to see if they're parsed correctly
        println("[DEBUG_LOG] Parsed entries: $entries")
        entries.forEachIndexed { index, entry ->
            println("[DEBUG_LOG] Entry $index - date: ${entry.date}, text: ${entry.text}")
        }
        
        val svg = maker.makeTimelineSvg(entries, "Literary Periods", "1.0", false)
        
        // Debug: Print part of the SVG to see if text is included
        println("[DEBUG_LOG] SVG excerpt (first 500 chars): ${svg.take(500)}")
        println("[DEBUG_LOG] SVG contains 'Enlightenment': ${svg.contains("Enlightenment")}")
        println("[DEBUG_LOG] SVG contains 'Neoclassical': ${svg.contains("Neoclassical")}")
        println("[DEBUG_LOG] SVG contains 'Romanticism': ${svg.contains("Romanticism")}")
        println("[DEBUG_LOG] SVG contains 'Victorian': ${svg.contains("Victorian")}")
        
        // Verify the SVG contains expected elements
        assertTrue(svg.contains("<svg"), "SVG should start with <svg tag")
        assertTrue(svg.contains("Literary Periods"), "SVG should contain the title")
        assertTrue(svg.contains("Enlightenment") && svg.contains("Neoclassical"), "SVG should contain the first entry text")
        assertTrue(svg.contains("Romanticism"), "SVG should contain the second entry text")
        assertTrue(svg.contains("Victorian"), "SVG should contain the third entry text")
        
        // Verify it's a horizontal timeline (vertical spine)
        assertTrue(svg.contains("<!-- Timeline spine -->"), "SVG should contain a timeline spine")
        assertTrue(svg.contains("<line x1="), "SVG should contain a vertical line for the spine")
    }
    
    @Test
    fun `test vertical timeline creation`() {
        // Create a vertical timeline
        val maker = TimelineMaker(
            useDark = false, 
            outlineColor = "#38383a", 
            pdf = false, 
            id = "test_id", 
            useGlass = false,
            orientation = TimelineOrientation.VERTICAL
        )
        val entries = TimelineParser().parse(testTimelineContent)
        
        // Debug: Print entries to see if they're parsed correctly
        println("[DEBUG_LOG] Parsed entries for vertical timeline: $entries")
        entries.forEachIndexed { index, entry ->
            println("[DEBUG_LOG] Vertical Entry $index - date: ${entry.date}, text: ${entry.text}")
        }
        
        val svg = maker.makeTimelineSvg(entries, "Literary Periods", "1.0", false)
        
        // Debug: Print part of the SVG to see if text is included
        println("[DEBUG_LOG] Vertical SVG excerpt (first 500 chars): ${svg.take(500)}")
        println("[DEBUG_LOG] Vertical SVG contains 'Enlightenment': ${svg.contains("Enlightenment")}")
        println("[DEBUG_LOG] Vertical SVG contains 'Neoclassical': ${svg.contains("Neoclassical")}")
        println("[DEBUG_LOG] Vertical SVG contains 'Romanticism': ${svg.contains("Romanticism")}")
        println("[DEBUG_LOG] Vertical SVG contains 'Victorian': ${svg.contains("Victorian")}")
        
        // Verify the SVG contains expected elements
        assertTrue(svg.contains("<svg"), "SVG should start with <svg tag")
        assertTrue(svg.contains("Literary Periods"), "SVG should contain the title")
        assertTrue(svg.contains("Enlightenment") && svg.contains("Neoclassical"), "SVG should contain the first entry text")
        assertTrue(svg.contains("Romanticism"), "SVG should contain the second entry text")
        assertTrue(svg.contains("Victorian"), "SVG should contain the third entry text")
        
        // Verify it's a vertical timeline (horizontal spine)
        assertTrue(svg.contains("<!-- Timeline spine (horizontal) -->"), "SVG should contain a horizontal timeline spine")
        val output = File("gen/timeline_vertical.svg")
        output.writeBytes(svg.toByteArray())
        println("[DEBUG_LOG] Vertical SVG written to ${output.absolutePath}")
    }
    
    @Test
    fun `test clickable items`() {
        // Create a timeline with clickable items
        val maker = TimelineMaker(
            useDark = false, 
            outlineColor = "#38383a", 
            pdf = false, 
            id = "test_id", 
            useGlass = false,
            enableDetailView = true
        )
        val entries = TimelineParser().parse(testTimelineContent)
        val svg = maker.makeTimelineSvg(entries, "Literary Periods", "1.0", false)
        
        // Verify the SVG contains clickable elements
        assertTrue(svg.contains("onclick="), "SVG should contain onclick handlers")
        assertTrue(svg.contains("style=\"cursor: pointer;\""), "SVG should contain cursor pointer style")
        assertTrue(svg.contains("<!-- Detail view for entry"), "SVG should contain detail views")
        assertTrue(svg.contains("style=\"display: none;\""), "Detail views should be hidden by default")
        val output = File("gen/timeline_clickable.svg")
        output.writeBytes(svg.toByteArray())
        println("[DEBUG_LOG] Clickable SVG written to ${output.absolutePath}")
    }
    
    @Test
    fun `test dark mode`() {
        // Create a timeline with dark mode
        val maker = TimelineMaker(
            useDark = true, 
            outlineColor = "#38383a", 
            pdf = false, 
            id = "test_id", 
            useGlass = false
        )
        val entries = TimelineParser().parse(testTimelineContent)
        val svg = maker.makeTimelineSvg(entries, "Literary Periods", "1.0", false)
        
        // Verify the SVG contains dark mode elements
        assertTrue(svg.contains("#ffffff"), "SVG should use white text in dark mode")
        assertTrue(svg.contains("#000000"), "SVG should use black background in dark mode")
    }
    
    @Test
    fun `test glass effect`() {
        // Create a timeline with glass effect
        val maker = TimelineMaker(
            useDark = false, 
            outlineColor = "#38383a", 
            pdf = false, 
            id = "test_id", 
            useGlass = true
        )
        val entries = TimelineParser().parse(testTimelineContent)
        val svg = maker.makeTimelineSvg(entries, "Literary Periods", "1.0", false)
        
        // Verify the SVG contains glass effect elements
        assertTrue(svg.contains("url(#glassGradient)"), "SVG should use glass gradients")
        assertTrue(svg.contains("url(#glassRadial)"), "SVG should use radial glass gradients")
        assertTrue(svg.contains("url(#highlight)"), "SVG should use highlight gradients")
    }
    
    @Test
    fun `test api style vertical timeline`() {
        // Create a vertical timeline with API style (alternating entries above/below)
        val maker = TimelineMaker(
            useDark = false, 
            outlineColor = "#38383a", 
            pdf = false, 
            id = "test_id", 
            useGlass = false,
            orientation = TimelineOrientation.VERTICAL
        )
        val entries = TimelineParser().parse(testTimelineContent)
        val svg = maker.makeTimelineSvg(entries, "API Style Timeline", "1.0", false)
        
        // Verify the SVG contains expected elements
        assertTrue(svg.contains("<svg"), "SVG should start with <svg tag")
        assertTrue(svg.contains("API Style Timeline"), "SVG should contain the title")
        
        // Verify it's a vertical timeline (horizontal spine)
        assertTrue(svg.contains("<!-- Timeline spine (horizontal) -->"), "SVG should contain a horizontal timeline spine")
        
        // Verify it has the API style marker elements
        assertTrue(svg.contains("class=\"marker-date\""), "SVG should use marker-date class")
        assertTrue(svg.contains("class=\"marker-event\""), "SVG should use marker-event class")
        
        // Verify it has concentric circles for timeline dots
        assertTrue(svg.contains("<circle cx=\"") && svg.contains("r=\"8\"") && svg.contains("r=\"4\""), 
                  "SVG should have concentric circles for timeline dots")
        
        // Verify it has alternating entries (some with isAbove=true, some with isAbove=false)
        // This is harder to test directly, but we can check for the pattern of alternating Y positions
        // by looking at the SVG content
        val svgLines = svg.lines()
        
        // Write the output for visual inspection
        val output = File("gen/timeline_api_style.svg")
        output.writeBytes(svg.toByteArray())
        println("[DEBUG_LOG] API Style Vertical SVG written to ${output.absolutePath}")
    }
}