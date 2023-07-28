package io.docops.docopsextensionssupport.releasestrategy

import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.roadmap.linesToUrlIfExist
import io.docops.docopsextensionssupport.roadmap.wrapText
import java.util.*

class ReleaseRoadMapMaker {

    fun make(releaseStrategy: ReleaseStrategy, isPdf: Boolean, animate: String): String {
        return createSvg(releaseStrategy, isPdf, animate)
    }
    private fun createSvg(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false, animate: String): String {
        val id = UUID.randomUUID().toString()
        val str = StringBuilder()
        var startY = -125
        var height = 350
        if (releaseStrategy.releases.size > 1) {
            height += (220 * (releaseStrategy.releases.size - 1))
        }
        releaseStrategy.releases.forEachIndexed { index, release ->
            str.append(strat(release, startY, index, animate, id, releaseStrategy))
            startY += 225
        }
        return """
            <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                 width="${releaseStrategy.scale * 1200}" height="${height * releaseStrategy.scale}"
                 viewBox="0 0 ${releaseStrategy.scale * 1200} ${height * releaseStrategy.scale}">
                 <desc>https://docops.io/extension</desc>
                 ${svgDefs(isPdf,releaseStrategy)}     
                 <g transform="scale(${releaseStrategy.scale})">    
                <text x="600" text-anchor="middle" y="44" font-size="32px" font-family="Arial, Helvetica, sans-serif">${releaseStrategy.title.escapeXml()}</text>
                $str
                </g>
            </svg>
        """.trimIndent()
    }

    private fun strat(release: Release, startY: Int, index: Int, animate: String, id: String, releaseStrategy: ReleaseStrategy): String {
        var ani = ""
        if("ON".equals(animate, true)) {
            ani =  """<animateMotion dur="${release.type.speed(release.type)}" repeatCount="indefinite"
                        path="M 110 60 L 1200 60"/>"""
        }
        val str = StringBuilder(
            """<g id="detail_${id}_$index" transform="translate(-1000,0)">
                <text x="420" y="208" font-family="Arial, Helvetica, sans-serif" font-size="12px" fill="${releaseStrategy.displayConfig.fontColor}">""".trimIndent()
        )
        release.lines.forEach {
            str.append("<tspan x=\"420\" dy=\"18\">* $it</tspan>")
        }
        str.append("</text></g>")
        val lines = linesToUrlIfExist(wrapText(release.goal, 60F), mutableMapOf())
        val tspans = linesToSpanText(lines,24,400)
        val startTextY = 300 - (lines.size * 12)
        var completed = ""
        if(release.completed) {
            completed = "<use xlink:href=\"#doneCheckMark\" x=\"300\" y=\"-40\"/>"
        }
        //language=svg
        return """<g transform="translate(-200,$startY)" cursor="pointer" onclick="toggleItem('detail_${id}_$index', 'goal_${id}_$index')">
            <rect x="0" y="200" height="235" width="1400" fill="url(#${linearColor(release)})" stroke='#cccccc' class='row'/>
            <g>
            <circle cx="325" cy="310" r="84.5" fill-opacity="0.15" filter="url(#filter1)"/>
            <circle class="${release.type.clazz(release.type)}" cx="323" cy="307" r="73" fill="${releaseStroke(release, releaseStrategy)}" filter="url(#Bevel)"/>
            <circle cx="323" cy="307" r="66" fill="#ffffff"/>
            <use href="#svg2" x="305" y="340" fill="#fcfcfc" width="40" height="40">
            $ani
            </use>
             <text  class="milestoneDate" fill="#073763"><textPath text-anchor="middle" startOffset="25%" xlink:href="#curve">${release.date}</textPath></text>
            <text x="325" y="315" dominant-baseline="middle" stroke-width="1px" text-anchor="middle" class="milestone"
            fill="#073763">${release.type}
            </text>
           $completed
            </g>
            $str
            <g id="goal_${id}_$index" transform="translate(450,$startTextY)" text-anchor="middle">
                <text x="400" y="0" font-family="Arial, Helvetica, sans-serif" font-size="25px" fill="${releaseStrategy.displayConfig.fontColor}">
                    $tspans
                </text>
            </g>
            <path d="M 420 430 L 1400 430" stroke="none" stroke-width="1"/>
            </g>
        """.trimMargin()
    }

    private fun svgDefs(isPdf: Boolean, releaseStrategy: ReleaseStrategy): String {
        val ani = """ fill: transparent; stroke-width: 10px; stroke-dasharray: 471; stroke-dashoffset: 471; animation: clock-animation 2s linear infinite;""".trimIndent()
        var style = """
            <style>
                    .milestone:hover { cursor: pointer; /* calculate using: (2 * PI * R) */ stroke-width: 16; stroke-opacity: 1; fill: lightblue; }
                    .milestone { font-size: 60px; font-weight: bold; font-family: Arial, Helvetica, sans-serif; }
                    .milestoneDate { font-size: 18px; font-weight: bold; font-family: Arial, Helvetica, sans-serif; }
                    .bev:hover { $ani stroke: ${releaseStrategy.displayConfig.circleColors[0]}; } .bev2:hover { $ani stroke: ${releaseStrategy.displayConfig.circleColors[1]}; } .bev3:hover { $ani stroke: ${releaseStrategy.displayConfig.circleColors[2]}; }
                    .row { filter: drop-shadow(3px 5px 2px rgb(0 0 0 / 0.4)); }
                    @keyframes clock-animation {
                        0% {
                            stroke-dashoffset: 471;
                        }
                        100% {
                            stroke-dashoffset: 0;
                        }
                    }
                    .box1Clicked { transition-timing-function: ease-out; transition: 1.25s; transform: translateX(0%); }
                    .box2Clicked { transition-timing-function: ease-out; transition: 2.25s; transform: translateX(-330%); }
                    </style>
                    <script>
                     function toggleItem(item1, item2) {
                        var elem2 = document.querySelector("#"+item2);
                        elem2.classList.toggle("box2Clicked");
                        var elem = document.querySelector("#"+item1);
                        elem.classList.toggle("box1Clicked");
                    }
             </script>
        """.trimIndent()
        if(isPdf) {
            style = ""
        }
        val colors = StringBuilder()
        val shades = mutableMapOf(0 to "M", 1 to "R", 2 to "G")
        releaseStrategy.displayConfig.colors.forEachIndexed { index, s ->
            colors.append(gradientColorFromColor(s, "release${shades[index]}"))
        }
        //language=svg
        return """
            <defs>
                    <filter xmlns="http://www.w3.org/2000/svg" id="filter1">
                        <feGaussianBlur stdDeviation="1.75"/>
                    </filter>
                    <filter xmlns="http://www.w3.org/2000/svg" id="filter2">
                        <feGaussianBlur stdDeviation="0.35"/>
                    </filter>
                    <filter id="Bevel" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                        <feGaussianBlur in="SourceAlpha" stdDeviation="3" result="blur"/>
                        <feSpecularLighting in="blur" surfaceScale="5" specularConstant="0.5" specularExponent="10" result="specOut"
                                            lighting-color="white">
                            <fePointLight x="-5000" y="-10000" z="20000"/>
                        </feSpecularLighting>
                        <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
                        <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0"
                                     result="litPaint"/>
                    </filter>
                    <filter id="Bevel3" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
                        <feGaussianBlur in="SourceAlpha" stdDeviation="0.2" result="blur"/>
                        <feSpecularLighting in="blur" surfaceScale="10" specularConstant="3.5" specularExponent="10"
                                            result="specOut" lighting-color="#ffffff">
                            <fePointLight x="-5000" y="-10000" z="0000"/>
                        </feSpecularLighting>
                        <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
                        <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0"
                                     result="litPaint"/>
                    </filter>
                    <filter id="filter-2">
                        <feMorphology in="SourceAlpha" operator="dilate" radius="2" result="OUTLINE"/>
                        <feComposite operator="out" in="OUTLINE" in2="SourceAlpha"/>
                    </filter>
                    <path id="curve" fill="transparent" d="M267,317a56,56 0 1,0 112,0a56,56 0 1,0 -112,0" />
                    <svg id="doneCheckMark" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 1000"  width="100">
                        <path d="M 7.6885391,404.6142 C 7.6885391,404.6142 122.85389,534.30185 145.88696,587.27791 L 244.92916,587.27791 C 286.38869,460.59602 447.62018,158.16034 585.8186,52.208207 C 614.45182,15.394067 542.5208,0.19798715 484.4731,24.568517 C 396.98668,61.298507 231.98485,341.73657 201.16633,409.22081 C 157.4035,420.73735 111.33735,335.51499 111.33735,335.51499 L 7.6885391,404.6142 z"
                      style="fill:#00bb00;fill-opacity:1;fill-rule:evenodd;stroke:#000000;stroke-width:2;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1"/>
                    </svg>
                    $colors
                    ${car()}
                    $style
                </defs>
        """.trimIndent()
    }

     private fun linearColor(release: Release): String = when {
        release.type.toString().startsWith("M") -> {
            "releaseM"
        }

        release.type.toString().startsWith("R") -> {
            "releaseR"
        }

        release.type.toString().startsWith("G") -> {
            "releaseG"
        }

        else -> ""
    }
    private fun linesToSpanText(lines: MutableList<String>, dy: Int, x: Int): String {
        val text = StringBuilder()
        lines.forEach {
            text.append("""<tspan x="$x" dy="$dy" text-anchor="middle" font-family="Arial, 'Helvetica Neue', Helvetica, sans-serif" font-size="24" font-weight="normal">$it</tspan>""")
        }
        return text.toString()
    }
}
fun releaseStroke(release: Release, releaseStrategy: ReleaseStrategy): String = when {
    release.type.toString().startsWith("M") -> {
        releaseStrategy.displayConfig.circleColors[0]
    }

    release.type.toString().startsWith("R") -> {
        releaseStrategy.displayConfig.circleColors[1]
    }

    release.type.toString().startsWith("G") -> {
        releaseStrategy.displayConfig.circleColors[2]
    }

    else -> ""
}