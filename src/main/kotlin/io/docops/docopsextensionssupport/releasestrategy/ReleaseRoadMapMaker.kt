package io.docops.docopsextensionssupport.releasestrategy

class ReleaseRoadMapMaker {

    fun make(releaseStrategy: ReleaseStrategy, isPdf: Boolean): String {
        return createSvg(releaseStrategy, isPdf)
    }
    private fun createSvg(releaseStrategy: ReleaseStrategy, isPdf: Boolean = false): String {
        val str = StringBuilder()
        var startY = -125
        var height = 500
        if (releaseStrategy.releases.size > 2) {
            height += (220 * (releaseStrategy.releases.size - 2))
        }
        releaseStrategy.releases.forEachIndexed { index, release ->
            str.append(strat(release, startY, index))
            startY += 215
        }
        return """
            <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                 width="800" height="$height"
                 viewBox="0 0 800 $height">
                 ${svgDefs(isPdf)}                
                <rect id="mainview" width="100%" height="100%" fill="#f4f0e0" rx="5" ry="5" filter="url(#Bevel)"/>
                <text x="300" text-anchor="middle" y="44" font-size="32px" filter="url(#Bevel3)">${releaseStrategy.title}</text>
               
                $str
            </svg>
        """.trimIndent()
    }

    private fun strat(release: Release, startY: Int, index: Int): String {

        val str = StringBuilder(
            """<text x="440" y="218" fill="#00ff00" font-family="Arial, Helvetica, sans-serif" font-size="16px"
        class="filtered-8">"""
        )
        release.lines.forEach {
            str.append("<tspan x=\"440\" dy=\"18\">* $it</tspan>")
        }
        str.append("</text>")
        var color = "#eeeeee"
        if (index % 2 == 0) {
            color = "#FFEFC1"
        }
        //language=svg
        return """<g transform="translate(-200,$startY)" cursor="pointer">
            <rect x="410" y="200" height="225" width="810" fill="$color"/>
            <circle cx="325" cy="310" r="84.5" fill-opacity="0.15" filter="url(#filter1)"/>
            <circle class="${release.type.clazz(release.type)}" cx="323" cy="307" r="73" fill="${
            release.type.color(
                release.type
            )
        }" filter="url(#Bevel)"/>
            <circle cx="323" cy="307" r="66" fill="#ffffff"/>
            <text x="325" y="315" dominant-baseline="middle" stroke-width="1px" text-anchor="middle" class="milestone"
            fill="#073763">${release.type}
            </text>
            $str
            </g>
        """.trimMargin()
    }

    private fun svgDefs(isPdf: Boolean): String {
        val ani = """
            fill: transparent;
            stroke-width: 10px;
            stroke-dasharray: 471;
            stroke-dashoffset: 471;
            animation: clock-animation 2s linear infinite;
        """.trimIndent()
        var style = """
            <style>
                        .milestone:hover {
                            cursor: pointer;
                            /* calculate using: (2 * PI * R) */
                            stroke-width: 16;
                            stroke-opacity: 1;
                            fill: lightblue;

                        }

                        .milestone {
                            font-size: 60px;
                            font-weight: bold;
                            font-family: Arial, Helvetica, sans-serif;
                        }
                    .bev:hover {
                        $ani
                        stroke: #c30213;
                    }
                    .bev2:hover {
                        $ani
                        stroke: #2cc3cc;
                    }
                    .bev3:hover {
                        $ani
                        stroke: #3dd915;
                    }
                    @keyframes clock-animation {
                        0% {
                            stroke-dashoffset: 471;
                        }
                        100% {
                            stroke-dashoffset: 0;
                        }
                    }
                        .filtered-8 {
                            filter: url(#filter-8);
                            fill: black;
                            font-family: 'Lemon', cursive;
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
                    <filter id="filter-8" filterUnits="objectBoundingBox" primitiveUnits="userSpaceOnUse">
                        <feGaussianBlur stdDeviation="5" in="SourceAlpha" result="BLUR"/>

                        <feSpecularLighting surfaceScale="6" specularConstant="1" specularExponent="30" lighting-color="#white"
                                            in="BLUR" result="SPECULAR">
                            <fePointLight x="40" y="-30" z="200"/>
                        </feSpecularLighting>
                        <feComposite operator="in" in="SPECULAR" in2="SourceAlpha" result="COMPOSITE"/>
                        <feMerge>
                            <feMergeNode in="SourceAlpha"/>
                            <feMergeNode in="COMPOSITE"/>
                        </feMerge>
                    </filter>
                    <filter id="filter-2">
                        <feMorphology in="SourceAlpha" operator="dilate" radius="2" result="OUTLINE"/>
                        <feComposite operator="out" in="OUTLINE" in2="SourceAlpha"/>
                    </filter>
                    $style
                </defs>
        """.trimIndent()
    }
}