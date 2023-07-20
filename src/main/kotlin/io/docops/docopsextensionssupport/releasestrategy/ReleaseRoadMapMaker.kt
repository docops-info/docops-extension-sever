package io.docops.docopsextensionssupport.releasestrategy

import io.docops.asciidoc.utils.escapeXml

class ReleaseRoadMapMaker {

    fun make(releaseStrategy: ReleaseStrategy, isPdf: Boolean, animate: String): String {
        return createSvg(releaseStrategy, isPdf, animate)
    }
    private fun createSvg(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false, animate: String): String {
        val str = StringBuilder()
        var startY = -125
        var height = 350
        if (releaseStrategy.releases.size > 1) {
            height += (220 * (releaseStrategy.releases.size - 1))
        }
        releaseStrategy.releases.forEachIndexed { index, release ->
            str.append(strat(release, startY, index, animate))
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

    private fun strat(release: Release, startY: Int, index: Int, animate: String): String {
        var ani = ""
        if("ON".equals(animate, true)) {
            ani =  """<animateMotion dur="${release.type.speed(release.type)}" repeatCount="indefinite"
                        path="M 110 60 L 1200 60"/>"""
        }
        val str = StringBuilder(
            """<text x="420" y="208" font-family="Arial, Helvetica, sans-serif" font-size="12px">"""
        )
        release.lines.forEach {
            str.append("<tspan x=\"420\" dy=\"18\">* $it</tspan>")
        }
        str.append("</text>")
        var color = "#dddddd"
        if (index % 2 == 0) {
            color = "#fcfcfc"
        }
        //language=svg
        return """<g transform="translate(-200,$startY)" cursor="pointer">
            <rect x="0" y="200" height="235" width="1400" fill="$color" stroke='#cccccc' class='row'/>
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
            $str
            <path d="M 420 430 L 1400 430" stroke="#b3b3b3" stroke-width="5"/>
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
                    ${car()}
                    $style
                </defs>
        """.trimIndent()
    }
}