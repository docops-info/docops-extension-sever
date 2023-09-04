package io.docops.docopsextensionssupport.scorecard

import io.docops.asciidoc.utils.escapeXml

import java.awt.Color
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlin.math.max

class ScoreCardMaker {

    companion object {
        const val WIDTH : Float= 715.0f
    }
    fun make(scoreCard: ScoreCard): String {
        val id = scoreCard.id
        val sb = StringBuilder()
        val numOfRowsHeight = max(scoreCard.initiativeItems.size, scoreCard.outcomeItems.size) * 35.1f
        val headerHeight = 50.0f
        val height = numOfRowsHeight+ headerHeight
        sb.append(head(scoreCard, height, id))
        val styles = StringBuilder()
        styles.append(workItem(id))
        styles.append(glass())
        styles.append(raise())
        sb.append(defs(styles = styles.toString(), scoreCard= scoreCard))
        sb.append(startWrapper(scoreCard))
        sb.append(background(height * scoreCard.scale, WIDTH * scoreCard.scale))
        sb.append(titles(scoreCard))
        sb.append(arrowLine(scoreCard))
        sb.append(left(scoreCard))
        sb.append(right(scoreCard))
        sb.append(endWrapper())
        sb.append(tail())
        return sb.toString()
    }
    fun head(scoreCard: ScoreCard, height: Float, id: String): String {
        //50 top, 35.1 each row
        val width = WIDTH
        return """<svg id="$id" xmlns="http://www.w3.org/2000/svg" width="${width * scoreCard.scale}" height="${height * scoreCard.scale}"
     viewBox="0 0 ${width * scoreCard.scale} ${height * scoreCard.scale}" xmlns:xlink="http://www.w3.org/1999/xlink">
"""
    }
    fun tail() = "</svg>"
    fun defs(styles: String, scoreCard: ScoreCard): String {

        return """
            <defs>
            ${arrowHead(scoreCard)}
            ${gradientBackGround(scoreCard)}
            ${buildGradientDef("#fc4141", "leftScoreBox")}
            ${buildGradientDef("#7149c6", "rightScoreBox")}
            ${buildGradientDef(scoreCard.scoreCardTheme.outcomeBackgroundColor, "rightItem")}
            ${buildGradientDef(scoreCard.scoreCardTheme.initiativeBackgroundColor, "leftItem")}
            <style>
            $styles
            .left_${scoreCard.id} {
                fill: url(#leftItem);
            }
            .right_${scoreCard.id} {
                fill: url(#rightItem);
            }
            </style>
            </defs>
            
        """.trimIndent()
    }
    private fun arrowHead(scoreCard: ScoreCard) = """<marker id="arrowhead1" markerWidth="2" markerHeight="5" refX="0" refY="1.5" orient="auto">
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
    fun glass () = """.glass:after,.glass:before{content:"";display:block;position:absolute}.glass{overflow:hidden;color:#fff;text-shadow:0 1px 2px rgba(0,0,0,.7);background-image:radial-gradient(circle at center,rgba(0,167,225,.25),rgba(0,110,149,.5));box-shadow:0 5px 10px rgba(0,0,0,.75),inset 0 0 0 2px rgba(0,0,0,.3),inset 0 -6px 6px -3px rgba(0,129,174,.2);position:relative}.glass:after{background:rgba(0,167,225,.2);z-index:0;height:100%;width:100%;top:0;left:0;backdrop-filter:blur(3px) saturate(400%);-webkit-backdrop-filter:blur(3px) saturate(400%)}.glass:before{width:calc(100% - 4px);height:35px;background-image:linear-gradient(rgba(255,255,255,.7),rgba(255,255,255,0));top:2px;left:2px;border-radius:30px 30px 200px 200px;opacity:.7}.glass:hover{text-shadow:0 1px 2px rgba(0,0,0,.9)}.glass:hover:before{opacity:1}.glass:active{text-shadow:0 0 2px rgba(0,0,0,.9);box-shadow:0 3px 8px rgba(0,0,0,.75),inset 0 0 0 2px rgba(0,0,0,.3),inset 0 -6px 6px -3px rgba(0,129,174,.2)}.glass:active:before{height:25px}"""

    fun raise (strokeColor: String = "gold", opacity: Float = 0.9f) = """.raise {pointer-events: bounding-box;opacity: 1;filter: drop-shadow(3px 5px 2px rgb(0 0 0 / 0.4));}.raise:hover {stroke: ${strokeColor};stroke-width: 3px; opacity: ${opacity};} .raiseText {pointer-events: bounding-box; opacity: 1; filter: drop-shadow(3px 5px 2px rgb(0 0 0 / 0.4)); }"""

    //<rect width="100%" height="100%" fill="url(#backgroundScore)" opacity="1.0" ry="18" rx="18"/>
    fun background(h: Float, w: Float) = """
        <path d="${generateRectPathData(width = w, height = h, 16.0f,16.0f,16.0f,16.0f)}" 
        fill="url(#backgroundScore)"  />
         """.trimIndent()

    private fun titles(scoreCard: ScoreCard): String {
        return """
    <text x="340" y="20" style="font-family: Arial, Helvetica, sans-serif;  text-anchor:middle; font-size: 12px; fill: ${scoreCard.scoreCardTheme.titleColor}; letter-spacing: normal;font-weight: bold;font-variant: small-caps;" class="raiseText">${scoreCard.title}</text>
    <text x="150" y="40" style="font-family: Arial, Helvetica, sans-serif;  text-anchor:middle; font-size: 12px; fill: ${scoreCard.scoreCardTheme.initiativeTitleColor}; letter-spacing: normal;font-weight: bold;font-variant: small-caps;" class="raiseText">${scoreCard.initiativeTitle}</text>
    <text x="530" y="40" style="font-family: Arial, Helvetica, sans-serif;  text-anchor:middle; font-size: 12px; fill: ${scoreCard.scoreCardTheme.outcomeTitleColor}; letter-spacing: normal;font-weight: bold;font-variant: small-caps;" class="raiseText">${scoreCard.outcomeTitle}</text>
   
        """.trimIndent()
    }
    fun left(scoreCard: ScoreCard): String {
        val sb = StringBuilder()
        var startY = 50
        val inc = 35

        scoreCard.initiativeItems.forEach {
            sb.append("""
    <g transform="translate(10, $startY)">
    <path d="${generateRectPathData(width = 340f, height = 30f, 12f,12f,12f,12f)}" class="left_${scoreCard.id}" stroke="gold" cursor="pointer">
    <title>${it.description}</title>
    </path>
        <rect x="5" y="5" height="20" width="20" fill="url(#leftScoreBox)" rx="5" ry="5"/>
        <text x="11" y="19" fill="#efefef" style="font-family: arial;  font-size: 12px; font-weight:bold;">${it.displayText.first()}</text>
        <text x="30" y="7" style="font-family: arial;  font-size: 12px;">
            <tspan x="30" dy="12" style="font-variant: small-caps;fill:${scoreCard.scoreCardTheme.initiativeDisplayTextColor};">${it.displayText.escapeXml()}</tspan>
        </text>
    </g>
            """.trimIndent())
            startY += inc
        }
        return sb.toString()
    }
    fun right(scoreCard: ScoreCard): String {
        val sb = StringBuilder()
        var startY = 50
        val inc = 35
        scoreCard.outcomeItems.forEach {
            sb.append("""
    <g transform="translate(365, $startY)" >
        <path d="${generateRectPathData(width = 340f, height = 30f, 12f,12f,12f,12f)}" class="right_${scoreCard.id}" stroke="gold" cursor="pointer">
        <title>${it.description}</title>
        </path>
        <rect x="5" y="5" height="20" width="20" fill="url(#rightScoreBox)" rx="5" ry="5"/>
        <text x="11" y="19" fill="#efefef" style="font-family: arial;  font-size: 12px; font-weight:bold;">${it.displayText.first()}</text>
        <text x="30" y="7" style="font-family: arial;  font-size: 12px;">
            <tspan x="30" dy="12" style="font-variant: small-caps; fill:${scoreCard.scoreCardTheme.outcomeDisplayTextColor};">${it.displayText.escapeXml()}</tspan>
        </text>
    </g>
            """.trimIndent())
            startY += inc
        }
        return sb.toString()
    }
    private fun arrowLine(scoreCard: ScoreCard) = """<line x1="345" y1="35" x2="369" y2="35" stroke="${scoreCard.scoreCardTheme.arrowColor}" stroke-width="8" marker-end="url(#arrowhead1)"/>"""

    private fun startWrapper(scoreCard: ScoreCard) = """<g transform='scale(${scoreCard.scale})'>"""
    private fun endWrapper() = "</g>"
}

fun buildGradientDef(color: String, id: String): String {
    val m = gradientFromColor(color)
    return """
           <linearGradient id="$id" x2="0" y2="0" x1="0" y1="1">
            <stop class="stop1" offset="0%" stop-color="${m["color1"]}"/>
            <stop class="stop2" offset="50%" stop-color="${m["color2"]}"/>
            <stop class="stop3" offset="100%" stop-color="${m["color3"]}"/>
            </linearGradient> 
        """
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


fun generateRectPathData(width: Float, height: Float, topLetRound:Float, topRightRound:Float, bottomRightRound:Float, bottomLeftRound:Float): String {
    return """M 0 $topLetRound A $topLetRound $topLetRound 0 0 1 $topLetRound 0 L ${(width - topRightRound)} 0 A $topRightRound $topRightRound 0 0 1 $width $topRightRound L $width ${(height - bottomRightRound)} A $bottomRightRound $bottomRightRound 0 0 1 ${(width - bottomRightRound)} $height L $bottomLeftRound $height A $bottomLeftRound $bottomLeftRound 0 0 1 0 ${(height - bottomLeftRound)} Z"""
}
fun main() {
    val sm = ScoreCardMaker()
    val sc = ScoreCard(title="Digital Policy Service", initiativeTitle =  "PCF to EKS", outcomeTitle = "TMVS++",
        initiativeItems = mutableListOf(
            ScoreCardItem("Spring Boot 2.7 on Pcf Platform", "Spring Boot microservice framework"),
            ScoreCardItem("Redis used for storing circuit breaker data", "Redis is a distributed caching layer"),
        ScoreCardItem("MySql for storing zipkin traces"),
        ScoreCardItem("RabbitMQ for sending zipkin traces")),
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
            ScoreCardItem("Splunk queries documented", "Actuator Endpoints validated")),
        scoreCardTheme = ScoreCardTheme(initiativeBackgroundColor = "#111111", initiativeDisplayTextColor = "#fcfcfc", outcomeBackgroundColor = "#31AD18", outcomeDisplayTextColor = "#fcfcfc"),
        scale = 1.0f
    )
    val svg = sm.make(sc)
    val outfile = File("gen/score2.svg")
    println(Json.encodeToString(sc))
    outfile.writeBytes(svg.toByteArray())
}