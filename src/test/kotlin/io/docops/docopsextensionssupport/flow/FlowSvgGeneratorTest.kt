package io.docops.docopsextensionssupport.flow

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class FlowSvgGeneratorTest {

    private val data = """
        = Car Selection Flow

[.flow-definition]
--
description:: Complete car purchasing workflow
theme:: modern
--

[.steps]
|===
|Type |Name |Color

|start |Select Year |green
|common |Choose Make |blue
|common |Pick Model |blue
|decision |Engine Type? |orange
|branch |Configure Electric |purple
|branch |Configure Gas Engine |pink
|convergence |Join |slate
|common |Configure Interior |blue
|common |Configure Exterior |blue
|final |Complete Purchase |green
|===

[.connections]
---
Select Year -> Choose Make -> Pick Model -> Engine Type?
Engine Type? --Electric--> Configure Electric
Engine Type? --Gas--> Configure Gas Engine
Configure Electric -> Join
Configure Gas Engine -> Join
Join -> Configure Interior
Configure Interior -> Configure Exterior
Configure Exterior -> Complete Purchase
---
    """.trimIndent()


    @Test
    fun generate() {

        val parser = FlowParser()
        val definition = parser.parse(data)
        val generator = FlowSvgGenerator()
        val svg = generator.generate(definition)
        val f = File("gen/flow.svg")
        f.writeBytes(svg.toByteArray())
        assertTrue(svg.isNotEmpty())
        println(svg)

    }

}