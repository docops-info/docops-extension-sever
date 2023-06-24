package io.docops.docopsextensionssupport.releasestrategy

class ReleaseMaker {

    fun make(releaseStrategy: ReleaseStrategy) : String{
        val width = determineWidth(releaseStrategy = releaseStrategy)
        val str = StringBuilder(head(width))
        str.append(defs())
        str.append(title(releaseStrategy.title, width))
        releaseStrategy.releases.forEachIndexed { index, release ->
            str.append(buildReleaseItem(release,index))
        }

        str.append(tail())
        return str.toString()
    }


    private fun buildReleaseItem(release: Release, currentIndex: Int): String {
        var startX = 20
        if(currentIndex>0) {
            startX = currentIndex * 500
        }
        val goal = release.lines[0]
        val lineText = StringBuilder()
        var lineStart = 15
        release.lines.forEachIndexed { index, s ->
            lineText.append("""
                <tspan x="$lineStart" dy="10" class="entry">- $s</tspan>
            """.trimIndent())
            if(index <=7) {
                lineStart += 5
            } else {
                lineStart -=5
            }
        }
        //language=svg
        val item = """
         <g transform="translate($startX,60)" class="${shadeColor(release)}">
             <text text-anchor="middle" x="200" y="-12" class="milestone">${release.date}</text>
             <path d="m 0,0 h 400 v 200 h -400 l 0,0 l 50,-100 z" stroke="${strokeColor(release)}" fill="#fcfcfc"/>
            <path d="m 400,0 v 200 l 100,-100 z" fill="#fcfcfc" stroke="${strokeColor(release)}" />
            <text x="410" y="110" class="milestone" font-size="36px">${release.type}</text>
            <text text-anchor="middle" x="200" y="12" class="milestone lines">${release.goal}
                $lineText
            </text>
        </g>
        """.trimIndent()
        return item
    }
    private fun shadeColor(release: Release): String = when {
        release.type.toString().startsWith("M") -> {
            "shadM"
        }

        release.type.toString().startsWith("R") -> {
            "shadR"
        }

        release.type.toString().startsWith("G") -> {
            "shadG"
        }

        else -> ""
    }

    private fun strokeColor(release: Release): String = when {
        release.type.toString().startsWith("M") -> {
            "#c30213"
        }

        release.type.toString().startsWith("R") -> {
            "rgb(51, 182, 169)"
        }

        release.type.toString().startsWith("G") -> {
            "rgb(84, 210, 0)"
        }

        else -> ""
    }
    private fun determineWidth(releaseStrategy: ReleaseStrategy) = releaseStrategy.releases.size * 550


    private fun head(width: Int) : String{
        val ratioWidth = width *0.7
        val ratioHeight = 400 * 0.7
        return """
            <svg width="$ratioWidth" height="$ratioHeight" viewBox='0 0 $width 400' xmlns='http://www.w3.org/2000/svg' role='img'
     aria-label='Docops: Release Strategy'>
    <title>Docops: Release Strategy</title>
        """.trimIndent()
    }
    private fun title(title: String, width: Int) = """
        <text x="${width/2}" y="18" fill="#000000" text-anchor="middle"  font-size="18px">$title</text>
    """.trimIndent()
    private fun tail() = "</svg>"
    //language=svg
    private fun defs() = """
        <defs>
        <filter id="Bevel2" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
            <feGaussianBlur in="SourceAlpha" stdDeviation="0.5" result="blur"/>
            <feSpecularLighting in="blur" surfaceScale="5" specularConstant="0.5" specularExponent="10" result="specOut" lighting-color="white">
                <fePointLight x="-5000" y="-10000" z="0000"/>
            </feSpecularLighting>
            <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
            <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0" result="litPaint" />
        </filter>
        <linearGradient id="ID0756d7d2-2648-4a67-89af-c133b3a8d4c9" x2="1" y2="1">
            <stop class="stop1" offset="0%" stop-color="#a48bdb">
                <animate attributeName="stop-color"
                         values="#a48bdb;#7651c9;#4918B8;#a48bdb;#7651c9;#4918B8;" dur="20s" repeatCount="indefinite">
                </animate>
            </stop>
            <stop class="stop2" offset="50%" stop-color="#7651c9">
                <animate attributeName="stop-color"
                         values="#a48bdb;#7651c9;#4918B8;#a48bdb;#7651c9;#4918B8;" dur="20s" repeatCount="indefinite">
                </animate>
            </stop>
            <stop class="stop3" offset="100%" stop-color="#4918B8">
                <animate attributeName="stop-color"
                         values="#a48bdb;#7651c9;#4918B8;#a48bdb;#7651c9;#4918B8;" dur="20s" repeatCount="indefinite">
                </animate>
            </stop>
            <animateTransform attributeName="gradientTransform" type="rotate" values="360 .5 .5;0 .5 .5"
                              dur="10s" repeatCount="indefinite" />
        </linearGradient>
        <style>
            .shadM {
                fill: #c30213;
                filter: drop-shadow(0 2mm 1mm #c30213);
            }
            .shadR {
                fill: rgb(51, 182, 169);
                filter: drop-shadow(0 2mm 1mm rgb(51, 182, 169));
            }

            .shadG {
                fill: rgb(84, 210, 0);
                filter: drop-shadow(0 2mm 1mm rgb(84, 210, 0));
            }
            .milestone {
                font-family: Arial, "Helvetica Neue", Helvetica, sans-serif;
                font-weight: bold;
            }
            .lines {
                font-size: 10px;
            }

            .milestone > .entry {
                text-anchor: start;
                font-weight: normal;
            }
        </style>
    </defs>
    """.trimIndent()
}