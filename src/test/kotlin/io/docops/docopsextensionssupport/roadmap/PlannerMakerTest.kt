package io.docops.docopsextensionssupport.roadmap

import org.junit.jupiter.api.Test
import java.io.File

class PlannerMakerTest {

    @Test
    fun testPlannerMakerWithGlassDesign() {
        // This test creates an instance of PlannerMaker and generates a new SVG file
        // with the glass design
        println("[DEBUG_LOG] Running PlannerMaker to generate SVG with glass design")

        // Sample planner content
        val plannerContent = """
            - now Docker
            Use common docker image to streamline the process.
            - next
            dockerize API service
            build spring boot 3 version [[https://www.google.com google]] of application
            analyze black duck results
            - later Image
            image embed rectangle
            - now
            image embed slim
            - next Another map #005400
            color background roadmap
            - done Car
            remove car from release [[https://roach.gy roach]] strategy
            - done
            pass in theme (light,dark)
            - later url
            refactor displayConfigUrl to displayTheme
            - blocked dependency
            waiting on team to finish feature
        """.trimIndent()

        // Create PlannerMaker instance and generate SVG
        val plannerMaker = PlannerMaker()
        val svg = plannerMaker.makePlannerImage(plannerContent, "Glass Design Planner", "0.5")

        // Save the SVG to a file
        val file = File("gen/plannernew.svg")
        file.writeBytes(svg.toByteArray())

        // Verify the file was created
        assert(file.exists()) { "SVG file was not created" }
        println("[DEBUG_LOG] SVG file created at: ${file.absolutePath}")
        println("[DEBUG_LOG] File size: ${file.length()} bytes")
    }
}
