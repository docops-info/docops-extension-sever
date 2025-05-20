package io.docops.docopsextensionssupport.roadmap

import org.junit.jupiter.api.Test
import java.io.File

class PlannerMakerTest {

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