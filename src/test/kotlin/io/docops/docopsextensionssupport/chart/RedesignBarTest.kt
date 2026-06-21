package io.docops.docopsextensionssupport.chart

import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import org.junit.jupiter.api.Test
import java.io.File

class RedesignBarTest {

    @Test
    fun generateRedesignSvg() {
        val payload = """
title=Monthly Sales Performance
yLabel=Revenue ($)
xLabel=Month
type=R
paletteType=MODERN_EDITORIAL
---
January | 120.0
February | 334.0
March | 455.0
April | 244.0
May | 256.0
June | 223.0
        """.trimIndent()

        val csvResponse = CsvResponse(mutableListOf(), mutableListOf())
        val handler = BarHandler(csvResponse)
        val svg = handler.handleSVG(payload, DocOpsContext(useDark = false, backend = "web"))
        
        val genDir = File("gen")
        if(!genDir.exists()) genDir.mkdirs()
        File("gen/redesigned_result.svg").writeText(svg)
        println("SVG generated in gen/redesigned_result.svg")
    }

    @Test
    fun generateRedesignDarkSvg() {
        val payload = """
title=Monthly Sales Performance
yLabel=Revenue ($)
xLabel=Month
type=R
theme=modern_editorial
useDark=true
---
January | 120.0
February | 334.0
March | 455.0
April | 244.0
May | 256.0
June | 223.0
        """.trimIndent()

        val csvResponse = CsvResponse(mutableListOf(), mutableListOf())
        val handler = BarHandler(csvResponse)
        val svg = handler.handleSVG(payload, DocOpsContext(useDark = true, backend = "web"))
        
        val genDir = File("gen")
        if(!genDir.exists()) genDir.mkdirs()
        File("gen/redesigned_dark_result.svg").writeText(svg)
        println("SVG generated in gen/redesigned_dark_result.svg")
    }

    @Test
    fun generateDefaultThemeSvg() {
        val payload = """
title=Monthly Sales Performance - Default Theme
yLabel=Revenue ($)
xLabel=Month
type=R
---
January | 120.0
February | 334.0
March | 455.0
April | 244.0
May | 256.0
June | 223.0
        """.trimIndent()

        val csvResponse = CsvResponse(mutableListOf(), mutableListOf())
        val handler = BarHandler(csvResponse)
        val svg = handler.handleSVG(payload, DocOpsContext(useDark = false, backend = "web"))
        
        val genDir = File("gen")
        if(!genDir.exists()) genDir.mkdirs()
        File("gen/default_theme_result.svg").writeText(svg)
        println("SVG generated in gen/default_theme_result.svg")
    }

    @Test
    fun generateExplicitClassicThemeSvg() {
        val payload = """
title=Monthly Sales Performance - Classic Theme
yLabel=Revenue ($)
xLabel=Month
type=R
theme=classic
---
January | 120.0
February | 334.0
March | 455.0
April | 244.0
May | 256.0
June | 223.0
        """.trimIndent()

        val csvResponse = CsvResponse(mutableListOf(), mutableListOf())
        val handler = BarHandler(csvResponse)
        val svg = handler.handleSVG(payload, DocOpsContext(useDark = false, backend = "web"))
        
        val genDir = File("gen")
        if(!genDir.exists()) genDir.mkdirs()
        File("gen/explicit_classic_result.svg").writeText(svg)
        println("SVG generated in gen/explicit_classic_result.svg")
    }

    @Test
    fun generateGroupBarModernThemeSvg() {
        val payload = """
title=Annual Product Sales Report
yLabel=Sales (USD)
xLabel=Quarters
theme=ayu
paletteType=AYU_LIGHT
---
Product A | Q1 | 5000.0
Product A | Q2 | 7000.0
Product A | Q3 | 8000.0
Product A | Q4 | 6000.0
Product B | Q1 | 6000.0
Product B | Q2 | 8000.0
Product B | Q3 | 7000.0
Product B | Q4 | 9000.0
        """.trimIndent()

        val csvResponse = CsvResponse(mutableListOf(), mutableListOf())
        val handler = BarGroupHandler(csvResponse)
        val svg = handler.handleSVG(payload, DocOpsContext(useDark = true, backend = "web"))
        
        val genDir = File("gen")
        if(!genDir.exists()) genDir.mkdirs()
        File("gen/group_bar_modern_result.svg").writeText(svg)
        println("SVG generated in gen/group_bar_modern_result.svg")
    }

    @Test
    fun generateCylinderBarSvg() {
        val payload = """
title=Revenue by Quarter
summary=Cylinder chart with semantic contrast and staggered animation
yLabel=Revenue (USD)
xLabel=Fiscal Quarter
type=C
theme=modern_editorial
---
Q1 | 42000.0
Q2 | 67000.0
Q3 | 74000.0
Q4 | 96000.0
        """.trimIndent()

        val csvResponse = CsvResponse(mutableListOf(), mutableListOf())
        val handler = BarHandler(csvResponse)
        val svg = handler.handleSVG(payload, DocOpsContext(useDark = false, backend = "web"))
        
        val genDir = File("gen")
        if(!genDir.exists()) genDir.mkdirs()
        File("gen/cylinder_result.svg").writeText(svg)
        println("SVG generated in gen/cylinder_result.svg")
    }

    @Test
    fun generateCylinderBarDarkSvg() {
        val payload = """
title=Revenue by Quarter - Dark
summary=Dark theme cylinder chart with staggered animations
yLabel=Revenue (USD)
xLabel=Fiscal Quarter
type=C
theme=modern_editorial
---
Q1 | 42000.0
Q2 | 67000.0
Q3 | 74000.0
Q4 | 96000.0
        """.trimIndent()

        val csvResponse = CsvResponse(mutableListOf(), mutableListOf())
        val handler = BarHandler(csvResponse)
        val svg = handler.handleSVG(payload, DocOpsContext(useDark = true, backend = "web"))
        
        val genDir = File("gen")
        if(!genDir.exists()) genDir.mkdirs()
        File("gen/cylinder_dark_result.svg").writeText(svg)
        println("SVG generated in gen/cylinder_dark_result.svg")
    }
}
