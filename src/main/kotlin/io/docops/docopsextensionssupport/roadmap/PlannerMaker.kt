package io.docops.docopsextensionssupport.roadmap

import io.docops.docopsextensionssupport.svgsupport.escapeXml
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
                <linearGradient id="title-gradient" x1="0%" y1="0%" x2="100%" y2="0%">
                    <stop offset="0%" style="stop-color:#2c3e50;stop-opacity:1" />
                    <stop offset="100%" style="stop-color:#3498db;stop-opacity:1" />
                </linearGradient>
            </defs>

            <!-- Glass title background -->
            <rect x="${width/2 - 400}" y="10" width="800" height="70" rx="15" ry="15"
                  fill="url(#glassGradient)" 
                  stroke="rgba(255,255,255,0.3)" 
                  stroke-width="1" 
                  filter="url(#glass-shadow)" />

            <!-- Title highlight -->
            <rect x="${width/2 - 395}" y="15" width="790" height="25" rx="10" ry="10"
                  fill="url(#glass-overlay)" opacity="0.7" />

            <!-- Title text with glass effect -->
            <text x="${width/2}" y="60" text-anchor="middle" 
                  style="font-family: 'Segoe UI', Arial, Helvetica, sans-serif; 
                         font-size: 44px; 
                         font-weight: bold; 
                         letter-spacing: 1px; 
                         fill: url(#title-gradient); 
                         filter: url(#title-shadow);">${title.escapeXml()}</text>
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
            // Add glass effect
            sb.append("""
                <defs>
                    <filter id="glass-effect-${planItem.id}" x="-10%" y="-10%" width="120%" height="120%">
                        <feGaussianBlur in="SourceAlpha" stdDeviation="3" result="blur" />
                        <feOffset in="blur" dx="0" dy="4" result="offsetBlur" />
                        <feComponentTransfer in="offsetBlur" result="shadow">
                            <feFuncA type="linear" slope="0.3" />
                        </feComponentTransfer>
                        <feMerge>
                            <feMergeNode in="shadow" />
                            <feMergeNode in="SourceGraphic" />
                        </feMerge>
                    </filter>
                </defs>

                <!-- Glass card background -->
                <rect x="0" y="0" height="360" width="552" rx="12" ry="12"
                      style="fill: url(#glassGradient); stroke: rgba(255,255,255,0.3); stroke-width: 1; filter: url(#glass-shadow);"/>

                <!-- Color header with glass effect -->
                <path d="M 0 12.0 A 12.0 12.0 0 0 1 12.0 0 L 540.0 0 A 12.0 12.0 0 0 1 552.0 12.0 L 552.0 54.0 A 0.0 0.0 0 0 1 552.0 54.0 L 0.0 54.0 A 0.0 0.0 0 0 1 0 54.0 Z"
                      fill="$color" filter="url(#glass-blur)" />

                <!-- Glass highlight overlay -->
                <rect x="5" y="5" width="542" height="40" rx="8" ry="8"
                      fill="url(#glass-overlay)" opacity="0.7" />

                <!-- Bottom glass highlight -->
                <rect x="10" y="300" width="532" height="50" rx="8" ry="8"
                      fill="rgba(255,255,255,0.1)" />
            """.trimIndent())

            planItem.title?.let {
                val textColor = determineTextColor(columnColor)
                sb.append("""<text x="24" y="36" style="font-family: 'Segoe UI', Arial, Helvetica, sans-serif; fill: $textColor; font-size: 1rem; font-weight: 600; letter-spacing: 0.5px;">${planItem.title}</text>""")
            }
            planItem.content?.let {
                //todo fix url
                val contentList = itemTextWidth(planItem.content!!, 532F, 24, "Helvetica")
                val list = linesToUrlIfExist(contentList, planItem.urlMap)
                sb.append("<text x='24' y='74' style='font-family: \"Segoe UI\", Arial, Helvetica, sans-serif; fill: #444444; font-size: 1rem; line-height: 1.5;'>")
                list.forEachIndexed { index, string ->
                    // Check if the line starts with a bullet point
                    if (string.startsWith("•")) {
                        // Render bullet point with proper indentation
                        sb.append("""<tspan x="24" dy="28" style="font-family: 'Segoe UI', Arial, Helvetica, sans-serif; fill: #444444; font-size: 1rem; font-weight: 600;">• </tspan>""")
                        sb.append("""<tspan style="font-family: 'Segoe UI', Arial, Helvetica, sans-serif; fill: #444444; font-size: 1rem;">${string.substring(1)}</tspan>""")
                    } else {
                        sb.append("""<tspan x="24" dy="28" style="font-family: 'Segoe UI', Arial, Helvetica, sans-serif; fill: #444444; font-size: 1rem;">${string}</tspan>""")
                    }
                }
                sb.append("</text>")
            }
            sb.append("</g>")
            y += 360 + 20
        }

        sb.append("""
            <g transform="translate($startX, 10)">
                <!-- Glass column header -->
                <rect x="0" y="0" width="552" height="40" rx="8" ry="8"
                      fill="url(#glassGradient)" 
                      stroke="rgba(255,255,255,0.3)" 
                      stroke-width="1" 
                      filter="url(#glass-shadow)" />

                <!-- Column header highlight -->
                <rect x="5" y="5" width="542" height="15" rx="5" ry="5"
                      fill="url(#glass-overlay)" opacity="0.7" />

                <!-- Column header text with glass effect -->
                <filter id="glow-$key" x="-20%" y="-20%" width="140%" height="140%">
                    <feGaussianBlur stdDeviation="2" result="blur" />
                    <feComposite in="SourceGraphic" in2="blur" operator="over" />
                </filter>
                <text x="281" y="26" text-anchor="middle" 
                      style="font-family: 'Segoe UI', Arial, Helvetica, sans-serif; 
                             fill: $parentColor; 
                             font-size: 1rem; 
                             font-weight: bold; 
                             letter-spacing: 1px; 
                             filter: url(#title-shadow);">${key.escapeXml().uppercase()}</text>
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
     viewBox="0 0 $width $height" preserveAspectRatio="xMidYMin meet">
     <defs>
     $grads
     $itemGrad

     <!-- Glass effect filters -->
     <filter id="glass-shadow" x="-20%" y="-20%" width="140%" height="140%">
         <feDropShadow dx="0" dy="5" stdDeviation="10" flood-opacity="0.75" flood-color="#000000" />
     </filter>

     <filter id="glass-blur" x="-10%" y="-10%" width="120%" height="120%">
         <feGaussianBlur in="SourceGraphic" stdDeviation="3" result="blur" />
     </filter>

     <filter id="title-shadow" x="-10%" y="-10%" width="120%" height="120%">
         <feDropShadow dx="1" dy="1" stdDeviation="1" flood-opacity="0.2" flood-color="#000000" />
     </filter>

     <!-- Glass effect gradients -->
     <linearGradient id="glass-overlay" x1="0%" y1="0%" x2="0%" y2="100%">
         <stop offset="0%" style="stop-color:rgba(255,255,255,0.7);stop-opacity:1" />
         <stop offset="100%" style="stop-color:rgba(255,255,255,0);stop-opacity:1" />
     </linearGradient>

     <!-- Gradient for glass base -->
     <linearGradient id="glassGradient" x1="0%" y1="0%" x2="0%" y2="100%">
         <stop offset="0%" style="stop-color:rgba(255,255,255,0.3);stop-opacity:1" />
         <stop offset="50%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
         <stop offset="100%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1" />
     </linearGradient>

     <!-- Inner shadow for depth -->
     <filter id="innerShadow" x="-50%" y="-50%" width="200%" height="200%">
         <feOffset dx="0" dy="2"/>
         <feGaussianBlur stdDeviation="3" result="offset-blur"/>
         <feFlood flood-color="rgba(0,0,0,0.3)"/>
         <feComposite in2="offset-blur" operator="in"/>
         <feComposite in2="SourceGraphic" operator="over"/>
     </filter>
     </defs>
     """.trimIndent()
    }
    private fun makeEnd() = "</svg>"
}

fun main() {
    val str = """
- now Backend Development
Learn Node.js and Express
Master database design with MongoDB
Implement authentication and authorization
- next Frontend Frameworks
Study React fundamentals
Build interactive UIs
State management with Redux
- later DevOps Skills
Docker containerization
CI/CD pipeline setup
Cloud deployment (AWS/Azure)
- done Programming Basics
HTML, CSS, JavaScript
Git version control
Basic algorithms and data structures
    """.trimIndent()
    val p = PlannerMaker()
    val svg =p.makePlannerImage(str, "title", "0.5")
    val f = File("gen/plannernew.svg")
    f.writeBytes(svg.toByteArray())
}
