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
            str.append(strat(release, startY, index, animate, id))
            startY += 225
        }
        return """
            <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                 width="${releaseStrategy.scale * 1200}" height="${height * releaseStrategy.scale}"
                 viewBox="0 0 ${releaseStrategy.scale * 1200} ${height * releaseStrategy.scale}">
                 <desc>https://docops.io/extension</desc>
                 ${svgDefs(isPdf)}     
                 <g transform="scale(${releaseStrategy.scale})">    
                <text x="600" text-anchor="middle" y="44" font-size="32px" font-family="Arial, Helvetica, sans-serif">${releaseStrategy.title.escapeXml()}</text>
                $str
                </g>
            </svg>
        """.trimIndent()
    }

    private fun strat(release: Release, startY: Int, index: Int, animate: String, id: String): String {
        var ani = ""
        if("ON".equals(animate, true)) {
            ani =  """<animateMotion dur="${release.type.speed(release.type)}" repeatCount="indefinite"
                        path="M 110 60 L 1200 60"/>"""
        }
        val str = StringBuilder(
            """<g id="detail_${id}_$index" visibility="hidden">
                <text x="420" y="208" font-family="Arial, Helvetica, sans-serif" font-size="12px" fill="#fcfcfc">""".trimIndent()
        )
        release.lines.forEach {
            str.append("<tspan x=\"420\" dy=\"18\">* $it</tspan>")
        }
        str.append("</text></g>")
        var color = "#dddddd"
        if (index % 2 == 0) {
            color = "#fcfcfc"
        }
        val lines = linesToUrlIfExist(wrapText(release.goal, 60F), mutableMapOf())
        val tspans = linesToSpanText(lines,24,400)
        val startTextY = 300 - (lines.size * 12)
        //language=svg
        return """<g transform="translate(-200,$startY)" cursor="pointer">
            <rect x="0" y="200" height="235" width="1400" fill="url(#${linearColor(release)})" stroke='#cccccc' class='row'/>
            <g onclick="toggleItem('detail_${id}_$index', 'goal_${id}_$index')">
            <circle cx="325" cy="310" r="84.5" fill-opacity="0.15" filter="url(#filter1)"/>
            <circle class="${release.type.clazz(release.type)}" cx="323" cy="307" r="73" fill="${release.type.color(release.type)}" filter="url(#Bevel)"/>
            <circle cx="323" cy="307" r="66" fill="#ffffff"/>
            <use href="#svg2" x="305" y="340" fill="${release.type.color(release.type)}" width="40" height="40">
            $ani
            </use>
            <text x="325" y="410" text-anchor="middle" dominant-baseline="middle" class="milestoneDate" fill="${release.type.color(release.type)}">${release.date}</text>
            <text x="325" y="315" dominant-baseline="middle" stroke-width="1px" text-anchor="middle" class="milestone"
            fill="#073763">${release.type}
            </text>
            </g>
            $str
            <g id="goal_${id}_$index" transform="translate(450,$startTextY)" text-anchor="middle">
                <text x="400" y="0" font-family="Arial, Helvetica, sans-serif" font-size="25px" fill="#fcfcfc">
                    $tspans
                </text>
            </g>
            <path d="M 420 430 L 1400 430" stroke="none" stroke-width="1"/>
            </g>
        """.trimMargin()
    }

    private fun svgDefs(isPdf: Boolean): String {
        val ani = """ fill: transparent; stroke-width: 10px; stroke-dasharray: 471; stroke-dashoffset: 471; animation: clock-animation 2s linear infinite;""".trimIndent()
        var style = """
            <style>
                    .milestone:hover { cursor: pointer; /* calculate using: (2 * PI * R) */ stroke-width: 16; stroke-opacity: 1; fill: lightblue; }
                    .milestone { font-size: 60px; font-weight: bold; font-family: Arial, Helvetica, sans-serif; }
                    .milestoneDate { font-size: 18px; font-weight: bold; font-family: Arial, Helvetica, sans-serif; }
                    .bev:hover { $ani stroke: #6cadde; } .bev2:hover { $ani stroke: #C766A0; } .bev3:hover { $ani stroke: #136e33; }
                    .row { filter: drop-shadow(3px 5px 2px rgb(0 0 0 / 0.4)); }
                    @keyframes clock-animation {
                        0% {
                            stroke-dashoffset: 471;
                        }
                        100% {
                            stroke-dashoffset: 0;
                        }
                    }
                    </style>
                    <script>
                     function toggleItem(item1, item2) {
                        showHideItem(item1);
                        showHideItem(item2)
                    }
                    function showHideItem(item) {
                        var elem = document.querySelector("#"+item);
                        var display = elem.getAttribute("visibility");
                        if("hidden" === display) {
                            elem.setAttribute("visibility", "")
                        } else {
                            elem.setAttribute("visibility", "hidden")
                        }
                    }
             </script>
        """.trimIndent()
        if(isPdf) {
            style = ""
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
                    <linearGradient id="releaseM" x2="0%" y2="100%">
                        <stop class="stop1" offset="0%" stop-color="#9b9db7"/>
                        <stop class="stop2" offset="50%" stop-color="#696c93"/>
                        <stop class="stop3" offset="100%" stop-color="#373c6f"/>
                    </linearGradient>
                    <linearGradient id="releaseR" x2="0%" y2="100%">
                        <stop class="stop1" offset="0%" stop-color="#b9b0d8"/>
                        <stop class="stop2" offset="50%" stop-color="#9688c4"/>
                        <stop class="stop3" offset="100%" stop-color="#7461b1"/>
                    </linearGradient>
                    <linearGradient id="headerSix" x2="0%" y2="100%">
                        <stop class="stop1" offset="0%" stop-color="#e5c6fc"/>
                        <stop class="stop2" offset="50%" stop-color="#d8aafb"/>
                        <stop class="stop3" offset="100%" stop-color="#cc8efa"/>
                    </linearGradient>
                    <linearGradient id="headerSeven" x2="0%" y2="100%">
                        <stop class="stop1" offset="0%" stop-color="#ffe5f9"/>
                        <stop class="stop2" offset="50%" stop-color="#ffd8f6"/>
                        <stop class="stop3" offset="100%" stop-color="#ffccf4"/>
                    </linearGradient>
                    <linearGradient id="releaseG" x2="0%" y2="100%">
                        <stop class="stop1" offset="0%" stop-color="#87909d"/>
                        <stop class="stop2" offset="50%" stop-color="#4b596c"/>
                        <stop class="stop3" offset="100%" stop-color="#10223c"/>
                    </linearGradient>
                    ${car()}
                    $style
                </defs>
        """.trimIndent()
    }

     fun linearColor(release: Release): String = when {
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