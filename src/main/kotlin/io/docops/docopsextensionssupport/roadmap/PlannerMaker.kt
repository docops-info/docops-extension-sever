package io.docops.docopsextensionssupport.roadmap

import io.docops.docopsextensionssupport.adr.model.escapeXml
import io.docops.docopsextensionssupport.chart.DefaultChartColors
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
import java.io.File

class PlannerMaker {

    fun makePlannerImage(source: String, title: String): String {
        val parser = PlannerParser()
        val planItems = parser.parse(source)
        val sb = StringBuilder()
        val cols = planItems.toColumns()
        val grads = planItems.colorDefs(cols)

        sb.append(makeHead(planItems, title, grads))
        sb.append("<g transform=\"translate(0, 60)\">")
        var column = 0
        cols.forEach { key, value ->
            val color = DefaultChartColors.reversed()[column % DefaultChartColors.size]
            val startX = 10 + (column * 562)
            sb.append(makeColumn( key, value, 60, startX, colorIn = "url(#planItem_$column)", color))
            column++
        }
        sb.append("""</g>""")
        sb.append(makeEnd())
        return sb.toString()
    }



    private fun makeColumn(
        key: String,
        planItems: List<PlanItem>,
        startY: Int,
        startX: Int,
        colorIn: String,
        columnColor: String
    ): String {
        val sb = StringBuilder()
        var color = colorIn
        var y = startY

        planItems.forEachIndexed { index, planItem ->
            if(planItem.color != null) {
                color = planItem.color
            }
            sb.append("""<g transform="translate($startX, $y)">""")
            sb.append("""
                <rect x="0" y="0" fill="#fcfcfc" height="360" width="552" rx="5" ry="5"
                          style="fill: #fcfcfc; font-family: Arial, Helvetica, sans-serif; stroke: $color;stroke-width: 2;"/>
                        <path d="M 0 5.0 A 5.0 5.0 0 0 1 5.0 0 L 547.0 0 A 5.0 5.0 0 0 1 552.0 5.0 L 552.0 54.0 A 0.0 0.0 0 0 1 552.0 54.0 L 0.0 54.0 A 0.0 0.0 0 0 1 0 54.0 Z
                " fill="$color"/>
            """.trimIndent())

            planItem.title?.let {
                val textColor = determineTextColor(columnColor)
                sb.append("""<text x="20" y="36" style="font-family: Arial, Helvetica, sans-serif; fill: $textColor; font-size: 24px;">${planItem.title}</text>""")
            }
            planItem.content?.let {
                //todo fix url
                val contentList = itemTextWidth(planItem.content!!, 542, 24, "Helvetica")
                val list = linesToUrlIfExist(contentList, planItem.urlMap)
                sb.append("<text x='20' y='62' style='font-family: Arial, Helvetica, sans-serif; fill: #111111; font-size: 24;'>")
                list.forEachIndexed { index, string ->
                    sb.append("""<tspan x="20" dy="24" style="font-family: Arial, Helvetica, sans-serif; fill: #111111; font-size: 24;">${string}</tspan>""")
                }
                sb.append("</text>")
            }
            sb.append("</g>")
            y += 360 +10
        }
        sb.append("""
            <g transform="translate($startX, 10)">
            <text x="281" y="26" text-anchor="middle" style="font-family: Arial, Helvetica, sans-serif; fill: $color; font-size: 36; stroke: $color; font-weight: bold;">${key.escapeXml().uppercase()}</text>
            </g>
            """.trimIndent())
        return sb.toString()
    }
    private fun makeHead(planItems: PlanItems, title: String, grads: String): String {
        val width = (552 * planItems.toColumns().size) + (planItems.toColumns().size * 10) + 10
        val height = planItems.maxRows() * 360 + (planItems.maxRows() * 10)+ 120
        return """
            <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
     width="${width * 0.50}" height="${height * 0.50}"
     viewBox="0 0 $width $height">
     <defs>
     $grads
     </defs>
     <text x="${width/2}" y="40" text-anchor="middle" style="font-family: Arial, Helvetica, sans-serif; font-size: 36; font-weight: bold;">${title.escapeXml()}</text>
         
        """.trimIndent()
    }
    private fun makeEnd() = "</svg>"
}

fun main() {
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
    val svg =p.makePlannerImage(str, "title")
    val f = File("gen/plannernew.svg")
    f.writeBytes(svg.toByteArray())
}