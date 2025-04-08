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

import io.docops.docopsextensionssupport.adr.model.escapeXml
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
         var titleFill = "#000000"
         if(releaseStrategy.useDark) {
             titleFill = "#fcfcfc"
             str.append("""<rect width="100%" height="100%" fill="url(#dmode1)"/>""")

         }
         str.append(title(releaseStrategy.title, width, titleFill))
         str.append("""<g transform='translate(0,20),scale(${releaseStrategy.scale})' id='GID${releaseStrategy.id}'>""")
        releaseStrategy.releases.forEachIndexed { index, release ->
            str.append(buildReleaseItem(release,index, isPdf, releaseStrategy.id, releaseStrategy))
            str.append(buildReleaseItemHidden(release,index, isPdf, releaseStrategy.id, releaseStrategy))
        }

        str.append("</g>")
        str.append(tail())
        return str.toString()
    }

    private fun head(width: Float, id: String, title: String, scale: Float, releaseStrategy: ReleaseStrategy) : String{

        val height = (275  + releaseStrategy.maxLinesForHeight() + 38)* scale
        //language=svg
        return """
            <svg width="${width / DISPLAY_RATIO_16_9}" height="${height / DISPLAY_RATIO_16_9}" viewBox='0 0 $width $height' xmlns='http://www.w3.org/2000/svg' 
            xmlns:xlink="http://www.w3.org/1999/xlink" role='img' preserveAspectRatio='xMidYMid meet'
            aria-label='Docops: Release Strategy' id="ID$id">
            <desc>https://docops.io/extension</desc>
            <title>${title.escapeXml()}</title>
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
        var clz = "raise"
        if(isPdf) {
            clz = ""
            fill =release.fillColor(releaseStrategy)
        }
        //language=svg
        return """
         <g transform="translate(${positionX+10},60)" >
             <text text-anchor="middle" x="250" y="-12" class="milestoneTL" fill='${fishTailColor(release, releaseStrategy)}'>${release.date}</text>
             <path d="m 0,0 h 400 v 200 h -400 l 0,0 l 100,-100 z m 400,0 v 200 l 100,-100 z" fill="#fcfcfc" stroke="${fishTailColor(release, releaseStrategy)}" stroke-width="2"/>
            <text x="410" y="110" class="milestoneTL" font-size="36px" fill="#111111">${release.type}</text>
            $completed
            <g transform="translate(100,0)" cursor="pointer" onclick="strategyShowItem('ID${id}_${currentIndex}')">
                <text text-anchor="middle" x="150" y="$textY" class="milestoneTL lines" font-size="12px"
                      font-family="Arial, 'Helvetica Neue', Helvetica, sans-serif" font-weight="bold" fill="#111111">
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

            //language=svg
            return """
         <g transform="translate(${positionX+10},275)" class="${shadeColor(release)}" id="ID${id}_${currentIndex}" $visibility>
            <rect width='400' height='$height' stroke="${fishTailColor(release, releaseStrategy)}" fill="#fcfcfc"/>
            <text $anchor x="$x" y="2" class="milestoneTL lines" font-size="12px" font-family='Arial, "Helvetica Neue", Helvetica, sans-serif' font-weight="bold">
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
          "Team will make the application communication private and local to the API Gateway",
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

    val release = Json.decodeFromString<ReleaseStrategy>(data)
    release.useDark = true
    val str = ReleaseTimelineSummaryMaker().make(release, isPdf = false)
    val f = File("gen/rel2.svg")
    f.writeText(str)
}