package io.docops.docopsextensionssupport.releasestrategy

import io.docops.asciidoc.buttons.theme.Theme
import io.docops.asciidoc.utils.escapeXml
import java.util.UUID

class ReleaseTimelineMaker {

    fun make(releaseStrategy: ReleaseStrategy, isPdf: Boolean) : String{
        val width = determineWidth(releaseStrategy = releaseStrategy)
        val id = UUID.randomUUID().toString()
        val str = StringBuilder(head(width, id, title= releaseStrategy.title, releaseStrategy.scale))
        str.append(defs(isPdf, id,releaseStrategy.scale))
        str.append(title(releaseStrategy.title, width))
        releaseStrategy.releases.forEachIndexed { index, release ->
            str.append(buildReleaseItem(release,index, isPdf, id))
        }
        val w = (width * releaseStrategy.scale)/2
        str.append("""
            <path d="m 2,0 v 40 l 20,-20 z" transform="translate($w, 275)" onclick="inc();" fill="#cfcfcf" cursor="pointer" class="raise"/>
            <path d="m 2,0 v 40 l -20,-20 z" transform="translate(${w-10}, 275)" onclick="dec();" fill="#cfcfcf" cursor="pointer" class="raise"/>
        """.trimMargin())
        str.append("</g>")
        str.append(tail())
        return str.toString()
    }


    private fun buildReleaseItem(release: Release, currentIndex: Int, isPdf: Boolean, id: String): String {
        var startX = 0
        if (currentIndex > 0) {
            startX = currentIndex * 425 -(20*currentIndex)
        }
        val lineText = StringBuilder()
        var lineStart = 25
        release.lines.forEachIndexed { index, s ->
            lineText.append(
                """
                <tspan x="$lineStart" dy="10" class="entry" font-size="10px" font-weight="normal"
                   font-family="Arial, 'Helvetica Neue', Helvetica, sans-serif" text-anchor="start">- ${s.escapeXml()}</tspan>
            """.trimIndent()
            )
            if (index <= 7) {
                lineStart += 10
            } else {
                lineStart -= 10
            }
        }
        var x = 200
        var anchor = "text-anchor=\"middle\""
        if (isPdf) {
            x = 15
            anchor = ""
        }
        //language=svg
        return """
         <g transform="translate($startX,60)" class="${shadeColor(release)}" id='GID$id'>
             <text text-anchor="middle" x="200" y="-12" class="milestoneTL">${release.date}</text>
             <path d="m 0,0 h 400 v 200 h -400 l 0,0 l 100,-100 z" stroke="${strokeColor(release)}" fill="#fcfcfc"/>
             <path d="m 400,0 v 200 l 100,-100 z" fill="${strokeColor(release)}" stroke="${strokeColor(release)}" />
            <text x="410" y="110" class="milestoneTL" font-size="36px" fill="#fcfcfc">${release.type}</text>
            <text $anchor x="$x" y="12" class="milestoneTL lines" font-size="10px" font-family='Arial, "Helvetica Neue", Helvetica, sans-serif' font-weight="bold">${release.goal.escapeXml()}
                $lineText
            </text>
        </g>
        """.trimIndent()
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
            "#6cadde"
        }

        release.type.toString().startsWith("R") -> {
            "#C766A0"
        }

        release.type.toString().startsWith("G") -> {
            "#F2DE83"
        }

        else -> ""
    }
    private fun determineWidth(releaseStrategy: ReleaseStrategy) = releaseStrategy.releases.size * 410 + releaseStrategy.releases.size * 20 + 40


    private fun head(width: Int, id: String, title: String, scale: Float) : String{
        val ratioWidth = width * scale
        val ratioHeight = 200 * scale
        //language=svg
        return """
            <svg width="$ratioWidth" height="$ratioHeight" viewBox='0 0 $ratioWidth 400' xmlns='http://www.w3.org/2000/svg' role='img'
            aria-label='Docops: Release Strategy' id="ID$id">
            <desc>https://docops.io/extension</desc>
            <title>${title.escapeXml()}</title>
        """.trimIndent()
    }
    private fun title(title: String, width: Int) = """
        <text x="${width/2}" y="18" fill="#000000" text-anchor="middle"  font-size="20px" font-family="Arial, Helvetica, sans-serif">${title.escapeXml()}</text>
    """.trimIndent()
    private fun tail() = "</svg>"

    //language=svg
    private fun defs(isPdf: Boolean, id: String, scale: Float): String {
        var style = ""
        if (!isPdf) {
            style = """
                <style>
            #ID${id} .shadM {
                fill: #6cadde;
                filter: drop-shadow(0 1mm 1mm #6cadde);
            }
            #ID${id} .shadR {
                fill: #C766A0;
                filter: drop-shadow(0 1mm 1mm #C766A0);
            }

            #ID${id} .shadG {
                fill: #F2DE83;
                filter: drop-shadow(0 1mm 1mm #F2DE83);
            }
            #ID${id} .milestoneTL {
                font-family: Arial, "Helvetica Neue", Helvetica, sans-serif;
                font-weight: bold;
            }
            #ID${id} .lines {
                font-size: 10px;
            }

            #ID${id} .milestoneTL > .entry {
                text-anchor: start;
                font-weight: normal;
            }
            .raise {
                pointer-events: bounding-box;
                opacity: 1;
                filter: drop-shadow(3px 5px 2px rgb(0 0 0 / 0.4));
            }

            .raise:hover {
                stroke: gold;
                stroke-width: 3px;
                opacity: 0.9;
            }
        </style>
            """.trimIndent()
        }
        return """
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
             <script>
              var scale = $scale;
              var inc = function() {
                    scale += 0.1;
                    var box = document.querySelector("#ID$id");
                    box.setAttribute("transform", "scale("+scale+")");
                    
              }
              var dec = function() {
                    scale -= 0.1;
                    var box = document.querySelector("#ID$id");
                    box.setAttribute("transform", "scale("+scale+")");
                    
              }
             </script>
             $style
         </defs>
         <g transform='scale($scale)'>
         """.trimIndent()
    }
}