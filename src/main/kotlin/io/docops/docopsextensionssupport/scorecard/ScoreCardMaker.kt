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

package io.docops.docopsextensionssupport.scorecard

import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.support.hexToHsl
import kotlinx.serialization.json.Json
import java.awt.Color
import java.io.File
import kotlin.math.max

/**
 * This class is responsible for generating a scorecard SVG representation based on the given ScoreCard object.
 */
class ScoreCardMaker {

    companion object {
        const val WIDTH: Float = 1045.0f
    }

    private var isPdf = false

    /**
     * Generates a string representing a formatted scorecard HTML.
     *
     * @param scoreCard the scorecard object containing the data to be used in generating the HTML
     * @return a string representing the formatted scorecard HTML
     */
    fun make(scoreCard: ScoreCard, isPdf: Boolean = false): String {
        this.isPdf = isPdf
        val id = scoreCard.id
        val sb = StringBuilder()
        val numOfRowsHeight = scoreCard.scoreCardHeight(numChars = 85, factor = 30.1F)
        val headerHeight = 50.0f
        val height = numOfRowsHeight + headerHeight
        val leftSide = left(scoreCard)
        val rightSide = right(scoreCard)
        sb.append(head(scoreCard, max(leftSide.second, rightSide.second), id))
        val styles = StringBuilder()
        styles.append(workItem(id))
        //styles.append(glass())

        sb.append(defs(styles = styles.toString(), scoreCard = scoreCard ))

        sb.append(startWrapper(scoreCard))
        sb.append(titles(scoreCard, height = max(leftSide.second, rightSide.second)))
        sb.append(arrowLine(scoreCard))
        sb.append(leftSide.first)
        sb.append(rightSide.first)
        sb.append(endWrapper())
        sb.append(tail())
        return sb.toString()
    }

    fun head(scoreCard: ScoreCard, height: Float, id: String): String {
        //50 top, 35.1 each row
        val width = WIDTH
        return """<svg id="d$id" xmlns="http://www.w3.org/2000/svg" width="${width * scoreCard.scale}" height="${height * scoreCard.scale}"
     viewBox="0 0 ${width * scoreCard.scale} ${height * scoreCard.scale}"
     preserveAspectRatio="xMidYMin slice"
     xmlns:xlink="http://www.w3.org/1999/xlink">
"""
    }

    fun tail() = "</svg>"
    fun defs(styles: String, scoreCard: ScoreCard): String {

        var style = """
            <style>
            $styles
            .left_${scoreCard.id} {
                fill: url(#leftItem_${scoreCard.id});
            }
            .right_${scoreCard.id} {
                fill: url(#rightItem_${scoreCard.id});
            }
            </style>
            <script>
                var reveal = function () {
                    var elems = document.querySelectorAll('#d${scoreCard.id} [display="none"]');
                    if(elems.length > 0) {
                        var item = elems[0];
                        item.setAttribute("display", "block");
                    }
                }
            </script>
        """.trimIndent()
        if (isPdf) {
            style = ""
        }
        return """
            <defs>
            ${arrowHead(scoreCard)}
            ${gradientBackGround(scoreCard)}
            ${buildGradientDef(scoreCard.scoreCardTheme.arrowColor, "arrowColor")}
            ${buildGradientDef("#D36B00", "leftScoreBox")}
            ${buildGradientDef("#5D9C59", "rightScoreBox")}
            ${buildGradientDef(scoreCard.scoreCardTheme.outcomeBackgroundColor, "rightItem_${scoreCard.id}")}
            ${buildGradientDef(scoreCard.scoreCardTheme.initiativeBackgroundColor, "leftItem_${scoreCard.id}")}
            <linearGradient id="leftBullet" x2="0%" y2="100%">
                <stop class="stop1" offset="0%" stop-color="#f9b890"/>
                <stop class="stop2" offset="50%" stop-color="#f69458"/>
                <stop class="stop3" offset="100%" stop-color="#F37121"/>
            </linearGradient>
            <linearGradient id="rightBullet" x2="0%" y2="100%">
                <stop class="stop1" offset="0%" stop-color="#82b09d"/>
                <stop class="stop2" offset="50%" stop-color="#44896c"/>
                <stop class="stop3" offset="100%" stop-color="#06623B"/>
            </linearGradient>
            $style
            </defs>
            <rect width="100%" height="100%" fill="#F7F7F7"/>
        """.trimIndent()
    }

    private fun arrowHead(scoreCard: ScoreCard) =
        """<marker id="arrowhead1" markerWidth="2" markerHeight="5" refX="0" refY="1.5" orient="auto">
            <polygon points="0 0, 1 1.5, 0 3" fill="${scoreCard.scoreCardTheme.arrowColor}"/>
        </marker>"""

    private fun gradientBackGround(scoreCard: ScoreCard): String {
        return buildGradientDef(scoreCard.scoreCardTheme.backgroundColor, "backgroundScore")
    }

    private fun workItem(id: String) = """
        #$id .workitem {
                filter: drop-shadow(3px 5px 2px rgb(0 0 0 / 0.4));
            }
         #$id .workitem :hover {
                opacity: 0.5;
                stroke-opacity: 1.7;
                stroke-width: 3;
                stroke: #bd5d5d;
            }
            """

    fun glass() =
        """.glass:after,.glass:before{content:"";display:block;position:absolute}.glass{overflow:hidden;color:#fff;text-shadow:0 1px 2px rgba(0,0,0,.7);background-image:radial-gradient(circle at center,rgba(0,167,225,.25),rgba(0,110,149,.5));box-shadow:0 5px 10px rgba(0,0,0,.75),inset 0 0 0 2px rgba(0,0,0,.3),inset 0 -6px 6px -3px rgba(0,129,174,.2);position:relative}.glass:after{background:rgba(0,167,225,.2);z-index:0;height:100%;width:100%;top:0;left:0;backdrop-filter:blur(3px) saturate(400%);-webkit-backdrop-filter:blur(3px) saturate(400%)}.glass:before{width:calc(100% - 4px);height:35px;background-image:linear-gradient(rgba(255,255,255,.7),rgba(255,255,255,0));top:2px;left:2px;border-radius:30px 30px 200px 200px;opacity:.7}.glass:hover{text-shadow:0 1px 2px rgba(0,0,0,.9)}.glass:hover:before{opacity:1}.glass:active{text-shadow:0 0 2px rgba(0,0,0,.9);box-shadow:0 3px 8px rgba(0,0,0,.75),inset 0 0 0 2px rgba(0,0,0,.3),inset 0 -6px 6px -3px rgba(0,129,174,.2)}.glass:active:before{height:25px}"""

    //<rect width="100%" height="100%" fill="url(#backgroundScore)" opacity="1.0" ry="18" rx="18"/>

    private fun titles(scoreCard: ScoreCard, height: Float): String {
        var reveal = ""
        if (scoreCard.slideShow) {
            reveal = """onclick="reveal();" cursor="pointer""""
        }
        return """
    <text x="522.5" y="20" style="font-family: Arial, Helvetica, sans-serif;  text-anchor:middle; font-size: 12px; fill: ${scoreCard.scoreCardTheme.titleColor}; letter-spacing: normal;font-weight: bold;" >${scoreCard.title}</text>
    <text x="255" y="40" style="font-family: Arial, Helvetica, sans-serif;  text-anchor:middle; font-size: 12px; fill: ${scoreCard.scoreCardTheme.initiativeTitleColor}; letter-spacing: normal;font-weight: bold;" >${scoreCard.initiativeTitle}</text>
    <text x="755.5" y="40" style="font-family: Arial, Helvetica, sans-serif;  text-anchor:middle; font-size: 12px; fill: ${scoreCard.scoreCardTheme.outcomeTitleColor}; letter-spacing: normal;font-weight: bold;"  $reveal>${scoreCard.outcomeTitle}</text>
   <line x1="522.5" x2="522.5" y1="50" y2="${height-10}" stroke="#cccccc" stroke-dasharray="2"/>
        """.trimIndent()
    }

    fun left(scoreCard: ScoreCard): Pair<String, Float> {
        val sb = StringBuilder()
        var startY = 50f
        val inc = 5

        var grad = "url(#leftScoreBox)"
        var fill = "fill=\"url(#leftItem_${scoreCard.id})\""
        if (isPdf) {
            grad = "#D36B00"

        }
        scoreCard.initiativeItems.forEach {
            val items = it.displayTextToList(70)
            val h = 12+ items.size * 12f
            sb.append(
                """
                <g transform="translate(10, $startY)">
                <g transform="translate(8,11)">
                    <polygon points="0,5 1.6666666666666667,2.5 0,0 5,2.5" stroke="url(#leftBullet)" stroke-width="3"
                             fill="url(#leftBullet)"/>
                    </g>
                    <text x="15" y="7" style="font-family: arial;  font-size: 12px;" >
                        ${itemsToSpan(items, scoreCard.scoreCardTheme.initiativeDisplayTextColor, startX= 20)}
                    </text>
                </g>
            """.trimIndent()
            )
            startY += h + inc
        }
        return Pair(sb.toString(), startY)
    }

    fun itemsToSpan(items: MutableList<String>, color: String , startX: Int = 25): String {
        var sb = StringBuilder()
        var indent = startX
        items.forEachIndexed { i, str ->
        if(i != 0) {
            indent = 10
        }
        sb.append("""
            <tspan x="$indent" dy="12" style="fill:${color};">${str.escapeXml()}</tspan>
        """.trimIndent())
            }
        return sb.toString()
    }

    fun right(scoreCard: ScoreCard): Pair<String, Float> {
        val sb = StringBuilder()
        var startY = 50f
        val inc = 5f
        var display = """display="block""""
        if (scoreCard.slideShow) {
            display = """display="none""""
        }
        var grad = "url(#rightScoreBox)"
        val fill = "fill=\"url(#rightItem_${scoreCard.id})\""
        if (isPdf) {
            grad = "#5D9C59"
        }
        scoreCard.outcomeItems.forEach {
            val items = it.displayTextToList(70)
            val h = 12+ items.size * 12f
            sb.append(
                """
    <g transform="translate(575, $startY)" $display>
        <g transform="translate(8,12)">
                <polygon points="0,5 1.6666666666666667,2.5 0,0 5,2.5" stroke="url(#rightBullet)" stroke-width="3"
                         fill="url(#rightBullet)"/>
        </g>
        <text x="30" y="7" style="font-family: arial;  font-size: 12px;" >
            ${itemsToSpan(items, scoreCard.scoreCardTheme.initiativeDisplayTextColor, 25)}
        </text>
    </g>
            """.trimIndent()
            )
            startY += h + inc
        }
        return Pair(sb.toString(), startY)
    }

    private fun arrowLine(scoreCard: ScoreCard): String {
        var grad = "url(#arrowColor)"
        if (isPdf) {
            grad = scoreCard.scoreCardTheme.arrowColor
        }
        return """
        <g transform="translate(490,25)" >
            <line x1="10" x2="50" y1="10" y2="10" stroke="${scoreCard.scoreCardTheme.arrowColor}" stroke-width="5" stroke-linecap="round" stroke-linejoin="round"/>
            <g transform="translate(50,7.5)">
                <polygon points="0,5 1.6666666666666667,2.5 0,0 5,2.5" stroke="$grad" stroke-width="3"
                         fill="$grad"/>
            </g>
        </g>
        
        """.trimIndent()
    }

    private fun startWrapper(scoreCard: ScoreCard) = """<g transform='scale(${scoreCard.scale})'>"""
    private fun endWrapper() = "</g>"
    fun buildGradientDef(color: String, id: String): String {
        val m = gradientFromColor(color)
        val hsl = hexToHsl(color, isPdf)
        return """
        <linearGradient x2="0%" y2="100%" id="$id">
            <stop stop-color="${m["color1"]}" stop-opacity="1" offset="0%"/>
            <stop stop-color="$hsl" stop-opacity="1" offset="100%"/>
        </linearGradient>
    """.trimIndent()
    }

    fun gradientFromColor(color: String): Map<String, String> {
        val decoded = Color.decode(color)
        val tinted1 = tint(decoded, 0.5)
        val tinted2 = tint(decoded, 0.25)
        return mapOf("color1" to tinted1, "color2" to tinted2, "color3" to color)
    }

    private fun tint(color: Color, factor: Double): String {
        val rs = color.red + (factor * (255 - color.red))
        val gs = color.green + (factor * (255 - color.green))
        val bs = color.blue + (factor * (255 - color.blue))
        return "#${rs.toInt().toString(16)}${gs.toInt().toString(16)}${bs.toInt().toString(16)}"
    }


}


fun generateRectPathData(
    width: Float,
    height: Float,
    topLetRound: Float,
    topRightRound: Float,
    bottomRightRound: Float,
    bottomLeftRound: Float
): String {
    return """M 0 $topLetRound A $topLetRound $topLetRound 0 0 1 $topLetRound 0 L ${(width - topRightRound)} 0 A $topRightRound $topRightRound 0 0 1 $width $topRightRound L $width ${(height - bottomRightRound)} A $bottomRightRound $bottomRightRound 0 0 1 ${(width - bottomRightRound)} $height L $bottomLeftRound $height A $bottomLeftRound $bottomLeftRound 0 0 1 0 ${(height - bottomLeftRound)} Z"""
}

fun main() {
    val sm = ScoreCardMaker()
    val sc = ScoreCard(
        title = "Digital Policy Service", initiativeTitle = "PCF to EKS", outcomeTitle = "TMVS++",
        initiativeItems = mutableListOf(
            ScoreCardItem("Spring Boot 2.7 on Pcf Platform", "Spring Boot microservice framework"),
            ScoreCardItem("Redis used for storing circuit breaker data", "Redis is a distributed caching layer"),
            ScoreCardItem("MySql for storing zipkin traces"),
            ScoreCardItem("RabbitMQ for sending zipkin traces")
        ),
        outcomeItems = mutableListOf(
            ScoreCardItem("Docker Containerized Spring Boot 3.x on EKS"),
            ScoreCardItem("Okta used for Authentication"),
            ScoreCardItem("PVC used for storing circuit breaker data"),
            ScoreCardItem("Open Search for zipkin trace storage"),
            ScoreCardItem("IBOB guidelines compliance"),
            ScoreCardItem("Flight check completed"),
            ScoreCardItem("Blue/Green/Canary deployments supported"),
            ScoreCardItem("Documented Local Setup for building & debugging"),
            ScoreCardItem("Production Support Guidelines documented"),
            ScoreCardItem("Splunk queries documented", "Actuator Endpoints validated")
        ),
        scoreCardTheme = ScoreCardTheme(
            initiativeBackgroundColor = "#111111",
            initiativeDisplayTextColor = "#fcfcfc",
            outcomeBackgroundColor = "#3081D0",
            outcomeDisplayTextColor = "#fcfcfc",
            arrowColor = "#FF6C22"
        ),
        scale = 1.0f,
        slideShow = false
    )
    val svg = sm.make(sc, false)
    val outfile = File("gen/score1.svg")
    outfile.writeBytes(svg.toByteArray())

    val scoreCard = Json.decodeFromString<ScoreCard>(
        """
        {
  "title": "Initiative",
  "initiativeTitle": "Journey Starts",
  "outcomeTitle": "Outcomes Since",
  "initiativeItems": [
    {
      "displayText": "Introduced API Versioning at the strategic integration level. Partnering with ETS for consensus. API versioning is the process of managing and tracking changes to an API. It also involves communicating those changes to the API consumers.",
      "description": "Host API versioning"
    },
    {
      "displayText": "Network Switch. Change is a natural part of API development. Sometimes, developers have to update their API's code to fix security vulnerabilities, while other changes introduce new features or functionality.",
      "description": "Migrate IBM Websphere to Liberty Server with RHEL 8 and JSESSION ID fix with Application cache busting and versioning"
    },
    {
      "displayText": "MFA. ome changes do not affect consumers at all, while others, which are known as breaking changes, lead to backward-compatibility issues, such as unexpected errors and data corruption",
      "description": "Enabling single signon from server sign-in page"
    },
    {
      "displayText": "Product Elimination - from system. API versioning ensures that these changes are rolled out successfully in order to preserve consumer trust while keeping the API secure, bug-free, and highly performant."
    }
  ],
  "outcomeItems": [
    {
      "displayText": "API Versioning changes with backwards compatibility. Here, we'll review the benefits of API versioning and discuss several scenarios in which it is necessary. We'll also explore some of the most common approaches to API versioning, provide five steps for successfully versioning an API, and highlight some best practices for API versioning."
    },
    {
      "displayText": "Network Mark-In/Mark-Out Servers"
    },
    {
      "displayText": "Phased Rollout, Milestones and Release Candidate"
    },
    {
      "displayText": "Software toggle to Enable/Disable OAuth Server"
    }
  ],
  "slideShow": false,
  "scale": 1.0,
  "scoreCardTheme": {
    "titleColor": "#27005D",
    "initiativeTitleColor": "#27005D",
    "outcomeTitleColor": "#27005D",
    "backgroundColor": "#FF74B1",
    "initiativeBackgroundColor": "#F7F7F7",
    "outcomeBackgroundColor": "#F7F7F7",
    "initiativeDisplayTextColor": "#27005D",
    "outcomeDisplayTextColor": "#27005D",
    "arrowColor": "#FF008E"
  }
}

            
    """.trimIndent()
    )
    val scoreCardMaker = ScoreCardMaker()
    val svg2 = scoreCardMaker.make(scoreCard, true)
    val outfile2 = File("gen/score2.svg")
    outfile2.writeBytes(svg2.toByteArray())
}