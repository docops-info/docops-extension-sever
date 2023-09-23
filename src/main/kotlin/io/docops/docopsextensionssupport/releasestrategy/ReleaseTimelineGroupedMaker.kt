package io.docops.docopsextensionssupport.releasestrategy

import java.util.*

/**
 * This class is responsible for generating a release timeline grouped by certain criteria.
 * It provides methods to build the SVG representation of the timeline.
 */
open class ReleaseTimelineGroupedMaker {

    /**
     * Generates an SVG string representation based on the provided release strategy and PDF preference.
     *
     * @param releaseStrategy The release strategy used for generating the SVG.
     * @param isPdf Indicates whether the generated SVG is intended for PDF usage.
     * @return The SVG string representation.
     */
    open fun make(releaseStrategy: ReleaseStrategy, isPdf: Boolean) : String{
        val id = UUID.randomUUID().toString()
        val width = determineWidth(releaseStrategy = releaseStrategy)
        val height = determineHeight(releaseStrategy = releaseStrategy)
        val str = StringBuilder(head(width, height = height, id=id, title = releaseStrategy.title, releaseStrategy.scale))
        str.append(defs(isPdf, id,  releaseStrategy.scale))
        var titleFill = "#000000"
        if(releaseStrategy.useDark) {
            titleFill = "#fcfcfc"
        }
        str.append(title(releaseStrategy.title, width, titleFill))
        var row = 0
        releaseStrategy.grouped().forEach { (t, u) ->
            u.forEachIndexed { index, release -> str.append(buildReleaseItem(
                release,
                index,
                isPdf,
                row,
                id,
                releaseStrategy
            )) }
            row++
        }
        str.append("</g>")
        str.append(tail())
        return str.toString()
    }


    protected open fun buildReleaseItem(
        release: Release,
        currentIndex: Int,
        isPdf: Boolean,
        row: Int,
        id: String,
        releaseStrategy: ReleaseStrategy
    ): String {
        var startY = 60
        if(row > 0) {
            startY = row * 240 + 60
        }
        var startX = 0
        if (currentIndex > 0) {
            startX = currentIndex * 425 -(20*currentIndex)
        }
        val goal = release.lines[0]
        val lineText = StringBuilder()
        var lineStart = 25
        release.lines.forEachIndexed { index, s ->
            lineText.append(
                """
                <tspan x="$lineStart" dy="10" class="entry" font-size="10px" font-weight="normal"
                   font-family="Arial, 'Helvetica Neue', Helvetica, sans-serif" text-anchor="start">- $s</tspan>
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
        var completed = ""
        if(release.completed) {
            completed = "<use xlink:href=\"#completedCheck\" x=\"425\" y=\"60\" width=\"24\" height=\"24\"/>"
        }
        //language=svg
        return """
         <g transform="translate($startX,$startY)" class="${shadeColor(release)}">
             <text text-anchor="middle" x="200" y="-12" class="milestoneTLG">${release.date}</text>
             <path d="m 0,0 h 400 v 200 h -400 l 0,0 l 100,-100 z" stroke="${strokeColor(release)}" fill="#fcfcfc"/>
             <path d="m 400,0 v 200 l 100,-100 z" fill="${strokeColor(release)}" stroke="${strokeColor(release)}" />
            <text x="410" y="110" class="milestoneTLG" font-size="36px" fill="#fcfcfc">${release.type}</text>
            $completed
            <text $anchor x="$x" y="12" class="milestoneTLG lines" font-size="10px" font-family='Arial, "Helvetica Neue", Helvetica, sans-serif' font-weight="bold">${release.goal}
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


    protected fun determineWidth(releaseStrategy: ReleaseStrategy): Int {
        val groups = releaseStrategy.grouped()
        var maxLen = 0
        groups.forEach { (t, u) ->
            maxLen = maxOf(maxLen, u.size)
        }
        return maxLen * 550
    }
    protected fun determineHeight(releaseStrategy: ReleaseStrategy) = releaseStrategy.grouped().size * 260

    protected fun head(width: Int, height: Int, id: String, title: String, scale: Float) : String{

        //language=svg
        return """
        <svg width="${width * scale}" height="${height*scale}" viewBox='0 0 ${width * scale}  ${height*scale}' xmlns='http://www.w3.org/2000/svg' role='img'
            aria-label='Docops: Release Strategy' id="ID$id">
            <desc>https://docops.io/extension</desc>
            <title>$title</title>
        """.trimIndent()
    }
    protected fun title(title: String, width: Int, titleFill: String) = """
        <text x="${width/2}" y="18" fill="$titleFill" text-anchor="middle"  font-size="18px" font-family="Arial, Helvetica, sans-serif">$title</text>
    """.trimIndent()
    protected fun tail() = "</svg>"

    //language=svg
    protected fun defs(isPdf: Boolean, id: String, scale: Float): String {
        var style = ""
        if (!isPdf) {
            style = """
                <style>
            #ID${id} .shadM {
                fill: #6cadde;
                filter: drop-shadow(0 2mm 1mm #6cadde);
            }
            #ID${id} .shadR {
                fill: #C766A0;
                filter: drop-shadow(0 2mm 1mm #C766A0);
            }

            #ID${id} .shadG {
                fill: #136e33;
                filter: drop-shadow(0 2mm 1mm #136e33);
            }
            #ID${id} .milestone {
                font-family: Arial, "Helvetica Neue", Helvetica, sans-serif;
                font-weight: bold;
            }
            #ID${id} .lines {
                font-size: 10px;
            }

            #ID${id} .milestone > .entry {
                text-anchor: start;
                font-weight: normal;
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
             <linearGradient id="shadM_rect" x2="0%" y2="100%">
                 <stop class="stop1" offset="0%" stop-color="#b5d6ee"/>
                 <stop class="stop2" offset="50%" stop-color="#90c1e6"/>
                 <stop class="stop3" offset="100%" stop-color="#6cadde"/>
            </linearGradient>
            <linearGradient id="shadR_rect" x2="0%" y2="100%">
                <stop class="stop1" offset="0%" stop-color="#e3b2cf"/>
                <stop class="stop2" offset="50%" stop-color="#d58cb7"/>
                <stop class="stop3" offset="100%" stop-color="#C766A0"/>
            </linearGradient>
            <linearGradient id="shadG_rect" x2="0%" y2="100%">
                <stop class="stop1" offset="0%" stop-color="#89b699"/>
                <stop class="stop2" offset="50%" stop-color="#4e9266"/>
                <stop class="stop3" offset="100%" stop-color="#136e33"/>
            </linearGradient>
             $style
         </defs>
         <g transform='scale($scale)'>
         """.trimIndent()
    }
}