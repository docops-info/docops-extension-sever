package io.docops.docopsextensionssupport.roadmap

import org.junit.jupiter.api.Test
import java.io.File

class PlannerMakerTest {

    @Test
    fun testBulletPoints() {
        val content = """
- now
* Implement user authentication system
* Set up CI/CD pipeline
* Create database schema
- next
* Develop REST API endpoints
* Build frontend components
* Implement search functionality
- later
* Add analytics dashboard
* Optimize performance
* Implement advanced features
- done
* Project requirements gathering
* Architecture design
* Technology stack selection
        """.trimIndent()

        val plannerMaker = PlannerMaker()
        val svg = plannerMaker.makePlannerImage(content, "Project Roadmap", "1.0")

        val outputFile = File("gen/planner-bullet-test.svg")
        outputFile.writeBytes(svg.toByteArray())

        println("SVG with bullet points generated at: ${outputFile.absolutePath}")
    }

    @Test
    fun testMakePlannerImage() {
        val str = """
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
        val p = PlannerMaker()
        val svg = p.makePlannerImage(str, "Improved Planner", "0.5")
        val f = File("gen/plannernew.svg")
        f.writeBytes(svg.toByteArray())
        println("Generated SVG at: ${f.absolutePath}")
    }
}
