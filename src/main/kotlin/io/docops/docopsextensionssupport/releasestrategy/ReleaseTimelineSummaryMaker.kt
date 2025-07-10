/*
 * Copyright (c) 2023. The DocOps Consortium
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.docops.docopsextensionssupport.releasestrategy

import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.roadmap.linesToUrlIfExist
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

/**
 * This class represents a Release Timeline Summary Maker.
 * It extends the ReleaseTimelineMaker class.
 * The ReleaseTimelineSummaryMaker class is responsible for generating a summary of the release timeline
 * based on the given release strategy.
 */
class ReleaseTimelineSummaryMaker : ReleaseTimelineMaker() {

    /**
     * Generates a SVG string representation of a document using the given release strategy.
     *
     * @param releaseStrategy The release strategy to use for generating the document.
     * @param isPdf Specifies whether the document format is PDF.
     * @return The SVG string representation of the generated document.
     */
    override fun make(releaseStrategy: ReleaseStrategy, isPdf: Boolean) : String{
        val width = determineWidth(releaseStrategy = releaseStrategy)

        val str = StringBuilder(head(
            width,
            releaseStrategy.id,
            title = releaseStrategy.title,
            releaseStrategy.scale,
            releaseStrategy
        ))
        str.append(defs(isPdf, releaseStrategy.id,  releaseStrategy.scale, releaseStrategy))

        // Add custom CSS for clean design without glass effects
        if (!isPdf) {
            str.append("""
                <style>
                    #ID${releaseStrategy.id} .raise { 
                        pointer-events: bounding-box; 
                        opacity: 1; 
                        transition: transform 0.2s ease;
                    }
                    #ID${releaseStrategy.id} .raise:hover { 
                        transform: translateY(-2px);
                    }
                    #ID${releaseStrategy.id} .milestoneTL { 
                        font-family: Arial, "Helvetica Neue", Helvetica, sans-serif; 
                        font-weight: bold; 
                        transition: none;
                    }
                    #ID${releaseStrategy.id} .lines { 
                        font-size: 12px; 
                        line-height: 1.4;
                        transition: none;
                    }
                    #ID${releaseStrategy.id} text {
                        text-rendering: optimizeLegibility;
                        shape-rendering: geometricPrecision;
                        -webkit-font-smoothing: antialiased;
                        -moz-osx-font-smoothing: grayscale;
                    }
                    #ID${releaseStrategy.id} .raise text {
                        transition: none;
                        transform: translateZ(0);
                        will-change: auto;
                    }
                </style>
            """.trimIndent())
        }

        var titleFill = "#000000"
        var backgroundColor = "#f8f9fa"
        if(releaseStrategy.useDark) {
            titleFill = "#fcfcfc"
            backgroundColor = "#21252B"
            str.append("""<rect width="100%" height="100%" fill="$backgroundColor"/>""")
        } else {
            str.append("""<rect width="100%" height="100%" fill="$backgroundColor"/>""")
        }

        // Use regular title instead of glassTitle
        str.append(title(releaseStrategy.title, width, titleFill))
        str.append("""<g transform='translate(0,60),scale(${releaseStrategy.scale})' id='GID${releaseStrategy.id}'>""")

        releaseStrategy.releases.forEachIndexed { index, release ->
            str.append(buildReleaseItem(release,index, isPdf, releaseStrategy.id, releaseStrategy))
            str.append(buildReleaseItemHidden(release,index, isPdf, releaseStrategy.id, releaseStrategy))
        }

        str.append("</g>")
        str.append(tail())
        return str.toString()
    }

    private fun head(width: Float, id: String, title: String, scale: Float, releaseStrategy: ReleaseStrategy) : String{
        // Calculate height based on releases and their content
        val baseHeight = 275
        val maxLinesHeight = releaseStrategy.maxLinesForHeight()
        val titlePadding = 38

        // Calculate additional height needed for detail lines
        val maxDetailLines = releaseStrategy.releases.maxOfOrNull { release ->
            release.lines.size
        } ?: 0

        // Add extra space for detail lines (each line is approximately 15px with spacing)
        val detailLinesHeight = maxDetailLines * 15

        // Add padding for detail panels
        val detailPanelPadding = 40

        val totalHeight = (baseHeight + maxLinesHeight + titlePadding + detailLinesHeight + detailPanelPadding) * scale

        //language=svg
        return """
        <svg width="${width / DISPLAY_RATIO_16_9}" height="${totalHeight / DISPLAY_RATIO_16_9}" viewBox='0 0 $width $totalHeight' xmlns='http://www.w3.org/2000/svg' 
        xmlns:xlink="http://www.w3.org/1999/xlink" role='img' preserveAspectRatio='xMidYMid meet'
        aria-label='Docops: Release Strategy' id="ID$id">
        <desc>https://docops.io/extension</desc>
        <title>${title.escapeXml()}</title>
        <g transform='translate(0,20),scale($scale)' id='GID$id'>
    """.trimIndent()
    }



    fun buildReleaseItem(release: Release, currentIndex: Int, isPdf: Boolean, id: String, releaseStrategy: ReleaseStrategy): String {
        var startX = 0
        if (currentIndex > 0) {
            startX = currentIndex * 425 -(20*currentIndex)
        }
        val lineText = StringBuilder()
        var lineStart = 25
        release.lines.forEachIndexed { index, s ->
            lineText.append(
                """
                <tspan x="$lineStart" dy="10" class="entry" font-size="12px" font-weight="normal"
                   font-family="Arial, 'Helvetica Neue', Helvetica, sans-serif" text-anchor="start">- ${s.escapeXml()}</tspan>
            """.trimIndent()
            )
            if (index <= 7) {
                lineStart += 10
            } else {
                lineStart -= 10
            }
        }
        val goals = release.goal.escapeXml()
        val itemArray = itemTextWidth(goals, 290F, 20)
        val lines = linesToUrlIfExist(itemArray, mutableMapOf())

        val spans = linesToSpanText(lines,20, 150)
        val textY = 100 - (lines.size * 12)
        var positionX = startX
        if(currentIndex>0) {
            positionX += currentIndex * 5
        }
        var completed = ""
        if(release.completed) {
            completed = "<use xlink:href=\"#completedCheck\" x=\"405\" y=\"65\" width=\"24\" height=\"24\"/>"
        }

        var fill = "url(#${shadeColor(release)}_rect_$id)"

        if(isPdf) {
            fill = release.fillColor(releaseStrategy)
        }

        // Set colors based on dark mode
        var cardFill = "#fcfcfc"
        var textFill = "#111111"
        var dateColor = fishTailColor(release, releaseStrategy)

        if(releaseStrategy.useDark) {
            cardFill = "#2c3033"
            textFill = "#e6e6e6"
        }

        //language=svg
        return """
         <g transform="translate(${positionX+10},60)">
             <!-- Date text with glass effect -->
             <text text-anchor="middle" x="250" y="-12" class="milestoneTL" 
                   fill='$dateColor' filter="url(#title-shadow)">${release.date}</text>

             <!-- Glass card background -->
             <defs>
                <filter id="card-glass-${release.type}-${currentIndex}" x="-10%" y="-10%" width="120%" height="120%">
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

             <!-- Main card with glass effect -->
             <path d="m 0,0 h 400 v 200 h -400 l 0,0 l 100,-100 z m 400,0 v 200 l 100,-100 z" 
                   fill="url(#glassGradient)" 
                   stroke="$dateColor" 
                   stroke-width="2"
                   filter="url(#glass-shadow)"/>

             <!-- Glass highlight overlay -->
             <path d="m 5,5 h 390 v 40 h -390 z" 
                   fill="url(#glass-overlay)" 
                   opacity="0.7"/>

             <!-- Type label with glass effect -->
             <text x="410" y="110" class="milestoneTL" 
                   font-size="36px" 
                   fill="$textFill"
                   filter="url(#title-shadow)">${release.type}</text>

            $completed

            <!-- Content with glass effect -->
            <g transform="translate(100,0)" cursor="pointer" onclick="strategyShowItem('ID${id}_${currentIndex}')">
                <text text-anchor="middle" x="150" y="$textY" class="milestoneTL lines" 
                      font-size="12px"
                      font-family="Arial, 'Helvetica Neue', Helvetica, sans-serif" 
                      font-weight="bold" 
                      fill="$textFill">
                   $spans
                </text>
            </g>
        </g>
        """.trimIndent()
    }
    fun buildReleaseItemHidden(release: Release, currentIndex: Int, isPdf: Boolean, id: String, releaseStrategy: ReleaseStrategy): String {
            var startX = 0
            if (currentIndex > 0) {
                startX = currentIndex * 425 -(20*currentIndex)
            }
            val lineText = StringBuilder()
            val bulletStar = StringBuilder()
            var lineStart = 2
        var y = -3
        val newLines = releaseStrategy.releaseLinesToDisplay(release.lines)
        newLines.forEachIndexed { index, s ->
            if(s is BulletLine) {
                bulletStar.append("""
                <use xlink:href="#bullStar${release.type.marker(release.type)}$id" x="1" y="$y" width="24" height="24"/>
            """.trimIndent())
                lineStart = 12
            } else {
                lineStart = 14
            }

                lineText.append(
                    """
                <tspan x="$lineStart" dy="12" class="entry" font-size="12px" font-weight="normal"
                   font-family="Arial, 'Helvetica Neue', Helvetica, sans-serif" text-anchor="start">${s.text.escapeXml()}</tspan>
            """.trimIndent()
                )
                y += 12
            }
            var x = 200
            var visibility = "visibility='hidden'"
            var anchor = "text-anchor='middle'"
            if (isPdf || releaseStrategy.displayConfig.notesVisible) {
                x = 10
                anchor = ""
                visibility = "visibility='visible'"
            }
        val height = (newLines.size+1) * 12
        var positionX = startX
        if(currentIndex>0) {
            positionX += currentIndex * 5
        }

        // Set colors based on dark mode
        var cardFill = "#fcfcfc"
        var textFill = "#111111"
        var borderColor = fishTailColor(release, releaseStrategy)

        if(releaseStrategy.useDark) {
            cardFill = "#2c3033"
            textFill = "#e6e6e6"
        }

            //language=svg
            return """
         <g transform="translate(${positionX+10},275)" class="${shadeColor(release)}" id="ID${id}_${currentIndex}" $visibility>
            <!-- Glass card background -->
            <defs>
                <filter id="hidden-card-glass-${release.type}-${currentIndex}" x="-10%" y="-10%" width="120%" height="120%">
                    <feGaussianBlur in="SourceAlpha" stdDeviation="2" result="blur" />
                    <feOffset in="blur" dx="0" dy="3" result="offsetBlur" />
                    <feComponentTransfer in="offsetBlur" result="shadow">
                        <feFuncA type="linear" slope="0.2" />
                    </feComponentTransfer>
                    <feMerge>
                        <feMergeNode in="shadow" />
                        <feMergeNode in="SourceGraphic" />
                    </feMerge>
                </filter>
            </defs>

            <!-- Main card with glass effect -->
            <rect width='400' height='$height' 
                  stroke="$borderColor" 
                  fill="url(#glassGradient)" 
                  rx="8" ry="8"
                  filter="url(#glass-shadow)"/>

            <!-- Glass highlight overlay -->
            <rect x="5" y="5" width="390" height="15" 
                  rx="5" ry="5"
                  fill="url(#glass-overlay)" 
                  opacity="0.7"/>

            <!-- Text content -->
            <text $anchor x="$x" y="2" class="milestoneTL lines" 
                  font-size="12px" 
                  font-family='Arial, "Helvetica Neue", Helvetica, sans-serif' 
                  font-weight="bold" 
                  fill="$textFill">
                $lineText
            </text>
            $bulletStar
        </g>
        """.trimIndent()
    }

    private fun linesToSpanText(lines: MutableList<String>, dy: Int, x: Int): String {
        val text = StringBuilder()
        lines.forEach {
            text.append("""<tspan x="$x" dy="$dy" text-anchor="middle" font-family="Arial, 'Helvetica Neue', Helvetica, sans-serif" font-size="20" font-weight="normal">$it</tspan>""")
        }
        return text.toString()
    }

    /**
     * Create a glass-styled title
     */
    private fun glassTitle(title: String, width: Float, titleFill: String): String {
        return """
            <!-- Glass title background -->
            <rect x="${width/2 - 300}" y="5" width="600" height="50" rx="15" ry="15"
                  fill="url(#glassGradient)" 
                  stroke="rgba(255,255,255,0.3)" 
                  stroke-width="1" 
                  filter="url(#glass-shadow)" />

            <!-- Title highlight -->
            <rect x="${width/2 - 295}" y="10" width="590" height="15" rx="10" ry="10"
                  fill="url(#glass-overlay)" opacity="0.7" />

            <!-- Title text with glass effect -->
            <text x="${width/2}" y="38" text-anchor="middle" 
                  style="font-family: 'Segoe UI', Arial, Helvetica, sans-serif; 
                         font-size: 24px; 
                         font-weight: bold; 
                         letter-spacing: 1px; 
                         fill: url(#title-gradient); 
                         filter: url(#title-shadow);">${title.escapeXml()}</text>
        """.trimIndent()
    }


}

fun main() {
    val data = """
  {
    "title": "Release Strategy Builder",
    "releases": [
      {
        "type": "M1",
        "lines": [
          "Team will deploy application and build out infrastructure with Terraform scripts.",
          "Team will Apply API gateway pattern to establish API version infrastructure.",
          "Team will validate access to the application",
          "Team will shutdown infrastructure as security is not in place."
        ],
        "date": "July 30th, 2023",
        "selected": true,
        "goal": "Our Goal is to provision new infrastructure on our cloud EKS platform without enabling production traffic",
        "completed": true
      },
      {
        "type": "RC1",
        "lines": [
          "Team will leverage CICD pipeline to deploy latest code",
          "Team will enable OAuth security on the API Gateway",
          "Team will make the application communication private and local to the API Gateway. Then switch out the config for the new API Gateway",
          "Team will enable API throttling at the Gateway layer",
          "Team will have QA do initial testing."
        ],
        "date": "September 20th, 2023",
        "completed": true,
        "goal": "Our goal is to deploy lastest code along with security applied at the API Layer"
      },
      {
        "type": "GA",
        "lines": [
          "Team will deploy latest code.",
          "QA will test and sign off"
        ],
        "date": "September 30th",
        "selected": true,
        "goal": "Our goal is to release version 1.0 of API making it generally available to all consumers."
      }
    ],
    "style": "TLS",
    "scale": 0.5,
    "displayConfig": {
      "colors": [
        "#5f57ff",
        "#2563eb",
        "#7149c6"
      ],
      "fontColor": "#fcfcfc"
    }
  }
    """.trimIndent()

    // Generate light mode version
    val lightRelease = Json.decodeFromString<ReleaseStrategy>(data)
    lightRelease.useDark = false
    val lightStr = ReleaseTimelineSummaryMaker().make(lightRelease, isPdf = false)
    val lightFile = File("gen/release_light.svg")
    lightFile.writeText(lightStr)
    println("Generated light mode SVG: ${lightFile.absolutePath}")

    // Generate dark mode version
    val darkRelease = Json.decodeFromString<ReleaseStrategy>(data)
    darkRelease.useDark = true
    val darkStr = ReleaseTimelineSummaryMaker().make(darkRelease, isPdf = false)
    val darkFile = File("gen/release_dark.svg")
    darkFile.writeText(darkStr)
    println("Generated dark mode SVG: ${darkFile.absolutePath}")
    val rm = ReleaseRoadMapMaker().make(releaseStrategy = darkRelease, isPdf = false, animate = "ON")
    val rmFile = File("gen/release_rm.svg")
    rmFile.writeText(rm)
    println("Generated release road map SVG: ${rmFile.absolutePath}")
    val rmPdf = ReleaseRoadMapMaker().make(releaseStrategy = darkRelease, isPdf = true, animate = "ON")
    val rmPdfFile = File("gen/release_rm_pdf.svg")
    rmPdfFile.writeText(rmPdf)
    println("Generated release road map PDF: ${rmPdfFile.absolutePath}")
    val rmPdf2 = ReleaseRoadMapMaker().make(releaseStrategy = darkRelease, isPdf = true, animate = "OFF")
    val rmPdfFile2 = File("gen/release_rm_pdf2.svg")
    rmPdfFile2.writeText(rmPdf2)
    println("Generated release road map PDF: ${rmPdfFile2.absolutePath}")
}
