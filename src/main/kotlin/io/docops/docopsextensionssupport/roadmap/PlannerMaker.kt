package io.docops.docopsextensionssupport.roadmap

import io.docops.docopsextensionssupport.adr.model.escapeXml
import io.docops.docopsextensionssupport.chart.STUNNINGPIE
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
import java.io.File

// DocOps branding colors based on the legend.html branding look
val DOCOPS_BRANDING_COLORS = listOf(
    // Business Capability (Red gradient)
    "#eb0e0e",
    // Engineering (Blue gradient)
    "#004680",
    // Both (Pink gradient)
    "#ff6dd0",
    // Additional colors from the original STUNNINGPIE
    "#e6d800", // Yellow
    "#50e991", // Green
    "#9b19f5"  // Purple
)

class PlannerMaker {

    fun makePlannerImage(source: String, title: String, scale: String): String {
        val parser = PlannerParser()
        val planItems = parser.parse(source)
        val sb = StringBuilder()
        val cols = planItems.toColumns()
        val grads = planItems.colorDefs(cols)
        val itemGrad = itemGradient(planItems)
        val width = determineWidth(planItems)
        val height = determineHeight(planItems)
        sb.append(makeHead(planItems, title, grads, itemGrad, width, height, scale))
        sb.append("""<g>""")
        sb.append("""
            <defs>
                <filter id="title-shadow" x="-10%" y="-10%" width="120%" height="120%">
                    <feDropShadow dx="1" dy="1" stdDeviation="1" flood-opacity="0.2" flood-color="#000000" />
                </filter>
                <linearGradient id="title-gradient" x1="0%" y1="0%" x2="100%" y2="0%">
                    <stop offset="0%" style="stop-color:#2c3e50;stop-opacity:1" />
                    <stop offset="100%" style="stop-color:#3498db;stop-opacity:1" />
                </linearGradient>
            </defs>
            <text x="${width/2}" y="50" text-anchor="middle" style="font-family: 'Segoe UI', Arial, Helvetica, sans-serif; font-size: 44px; font-weight: bold; letter-spacing: 1px; fill: url(#title-gradient); filter: url(#title-shadow);">${title.escapeXml()}</text>
        """)
        sb.append("<g transform=\"translate(0, 60)\">")
        var column = 0
        cols.forEach { (key, value) ->
            var color = DOCOPS_BRANDING_COLORS[column % DOCOPS_BRANDING_COLORS.size]
            if(value[0].color != null) {
                color = value[0].color!!
            }
            val startX = 20 + (column * 572)
            sb.append(makeColumn( key, value, 60, startX, colorIn = "url(#planItem_$column)", color))
            column++
        }
        sb.append("""</g>""")
        sb.append("""</g>""")
        sb.append(makeEnd())
        return sb.toString()
    }

private fun itemGradient(planItems: PlanItems): String {
    val sb = StringBuilder()
    planItems.items.forEach {
        it.color?.let { color ->
            sb.append(it.colorGradient())
        }
    }
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
        var parentColor = ""
        planItems.forEachIndexed { _, planItem ->
            if(planItem.color != null) {
                color = "url(#${planItem.id})"
                if(planItem.isParent) {
                    parentColor = color
                }
            }

            sb.append("""<g transform="translate($startX, $y)">""")
            // Add drop shadow filter
            sb.append("""
                <defs>
                    <filter id="shadow-${planItem.id}" x="-10%" y="-10%" width="120%" height="120%">
                        <feDropShadow dx="2" dy="2" stdDeviation="3" flood-opacity="0.15" />
                    </filter>
                    <linearGradient id="card-gradient-${planItem.id}" x1="0%" y1="0%" x2="100%" y2="100%">
                        <stop offset="0%" style="stop-color:#ffffff;stop-opacity:1" />
                        <stop offset="100%" style="stop-color:#f8f8f8;stop-opacity:1" />
                    </linearGradient>
                </defs>
                <rect x="0" y="0" height="360" width="552" rx="12" ry="12"
                      style="fill: url(#card-gradient-${planItem.id}); stroke: #e0e0e0; stroke-width: 1; filter: url(#shadow-${planItem.id});"/>
                <path d="M 0 12.0 A 12.0 12.0 0 0 1 12.0 0 L 540.0 0 A 12.0 12.0 0 0 1 552.0 12.0 L 552.0 54.0 A 0.0 0.0 0 0 1 552.0 54.0 L 0.0 54.0 A 0.0 0.0 0 0 1 0 54.0 Z"
                      fill="$color"/>
            """.trimIndent())

            planItem.title?.let {
                val textColor = determineTextColor(columnColor)
                sb.append("""<text x="24" y="36" style="font-family: 'Segoe UI', Arial, Helvetica, sans-serif; fill: $textColor; font-size: 24px; font-weight: 600; letter-spacing: 0.5px;">${planItem.title}</text>""")
            }
            planItem.content?.let {
                //todo fix url
                val contentList = itemTextWidth(planItem.content!!, 532F, 24, "Helvetica")
                val list = linesToUrlIfExist(contentList, planItem.urlMap)
                sb.append("<text x='24' y='74' style='font-family: \"Segoe UI\", Arial, Helvetica, sans-serif; fill: #444444; font-size: 20px; line-height: 1.5;'>")
                list.forEachIndexed { index, string ->
                    // Check if the line starts with a bullet point
                    if (string.startsWith("•")) {
                        // Render bullet point with proper indentation
                        sb.append("""<tspan x="24" dy="28" style="font-family: 'Segoe UI', Arial, Helvetica, sans-serif; fill: #444444; font-size: 20px; font-weight: 600;">• </tspan>""")
                        sb.append("""<tspan style="font-family: 'Segoe UI', Arial, Helvetica, sans-serif; fill: #444444; font-size: 20px;">${string.substring(1)}</tspan>""")
                    } else {
                        sb.append("""<tspan x="24" dy="28" style="font-family: 'Segoe UI', Arial, Helvetica, sans-serif; fill: #444444; font-size: 20px;">${string}</tspan>""")
                    }
                }
                sb.append("</text>")
            }
            sb.append("</g>")
            y += 360 + 20
        }

        sb.append("""
            <g transform="translate($startX, 10)">
                <filter id="glow-$key" x="-20%" y="-20%" width="140%" height="140%">
                    <feGaussianBlur stdDeviation="2" result="blur" />
                    <feComposite in="SourceGraphic" in2="blur" operator="over" />
                </filter>
                <text x="281" y="26" text-anchor="middle" style="font-family: 'Segoe UI', Arial, Helvetica, sans-serif; fill: $parentColor; font-size: 36px; font-weight: bold; letter-spacing: 1px; filter: url(#glow-$key);">${key.escapeXml().uppercase()}</text>
            </g>
            """.trimIndent())
        return sb.toString()
    }
    private fun determineWidth(planItems: PlanItems) : Int {
        return (552 * planItems.toColumns().size) + (planItems.toColumns().size * 20) + 20
    }
    private fun determineHeight(planItems: PlanItems) : Int {
        return planItems.maxRows() * 360 + (planItems.maxRows() * 20) + 120
    }
    private fun makeHead(
        planItems: PlanItems,
        title: String,
        grads: String,
        itemGrad: String,
        width: Int,
        height: Int,
        scale: String
    ): String {
        return """
            <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
     width="${(width * 0.50 * scale.toFloat()) / DISPLAY_RATIO_16_9}" height="${(height * 0.50 * scale.toFloat())/DISPLAY_RATIO_16_9}"
     viewBox="0 0 $width $height">
     <defs>
     $grads
     $itemGrad
     </defs>
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
    val svg =p.makePlannerImage(str, "title", "0.5")
    val f = File("gen/plannernew.svg")
    f.writeBytes(svg.toByteArray())
}
