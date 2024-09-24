package io.docops.docopsextensionssupport.scorecard

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.text.toByteArray

class ComparisonChartMaker {
    fun make(comparisonChart: ComparisonChart): String {
        var lastLine = comparisonChart.lines().entries.last().value.begin
        lastLine += (comparisonChart.lines().entries.last().value.rows() * 14) + 5

        val sb = StringBuilder()
        sb.append(head(comparisonChart, lastLine))
        sb.append(defs(comparisonChart))
        sb.append(headerRow(comparisonChart, lastLine))
        comparisonChart.lines().forEach { key, value ->
            sb.append(makeRow(key= key, value = value, startY = value.begin))
        }
        sb.append(tail(comparisonChart))
        return sb.toString()
    }

    private fun head(comparisonChart: ComparisonChart, lastLine: Int) = """
        <svg id="ID_${comparisonChart.id}"
     xmlns="http://www.w3.org/2000/svg"
     width="683" height="${lastLine * 0.667}"
     viewBox="0 0 1024.0 $lastLine"
     preserveAspectRatio="xMidYMin slice"
     xmlns:xlink="http://www.w3.org/1999/xlink">
    """.trimIndent()

    private fun tail(comparisonChart: ComparisonChart) = "</svg>"
    private fun defs(comparisonChart: ComparisonChart): String {
        return """
        <defs>
        <linearGradient id="leftCol" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="#e5e5e5"/>
            <stop class="stop2" offset="50%" stop-color="#d8d8d8"/>
            <stop class="stop3" offset="100%" stop-color="#cccccc"/>
        </linearGradient>
        <linearGradient id="col1" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="#e99f7f"/>
            <stop class="stop2" offset="50%" stop-color="#de6f3f"/>
            <stop class="stop3" offset="100%" stop-color="#D44000"/>
        </linearGradient>
        <linearGradient id="col2" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="#9adccb"/>
            <stop class="stop2" offset="50%" stop-color="#68cbb1"/>
            <stop class="stop3" offset="100%" stop-color="#36BA98"/>
        </linearGradient>
        <filter id="shadow" x="0" y="0" width="200%" height="200%">
            <feDropShadow dx="3" dy="3" stdDeviation="1" flood-color="#cccccc" flood-opacity="1" />
        </filter>
    </defs>
    """.trimIndent()
    }

    fun headerRow(comparisonChart: ComparisonChart, lastLine: Int) : String {
        val sb = StringBuilder()
        sb.append("""
               <rect x="0" y="0" width="340" height="$lastLine" fill="url(#leftCol)" style="filter: url(#shadow);"/>
               <g transform="translate(342,0)">
               <rect x="0" y="0" width="340" height="$lastLine" fill="url(#col1)" style="filter: url(#shadow);"/>
               <text x="170" y="30" text-anchor="middle" style="font-size: 36px; font-family: Arial, Helvetica, sans-serif; font-weight: bold;font-variant: small-caps; fill: #fcfcfc" >
                    ${comparisonChart.colHeader[0]}
                </text>
                </g>
                <g transform="translate(684,0)">
               <rect x="0" y="0" width="340" height="$lastLine" fill="url(#col2)" style="filter: url(#shadow);"/>
               <text x="170" y="30" text-anchor="middle" style="font-size: 36px; font-family: Arial, Helvetica, sans-serif; font-weight: bold;font-variant: small-caps; fill: #fcfcfc" >
                    ${comparisonChart.colHeader[1]}
                </text>
                </g>
        """.trimIndent())
        return sb.toString()
    }
    fun makeRow(key: String, value: ColLine, startY: Int) : String {
        val sb = StringBuilder()
        //col 1

        sb.append("""<g transform="translate(2,$startY)">""")
        sb.append("""<text style="font-size: 14px; font-family: Arial, Helvetica, sans-serif; font-weight: bold;" x="0" y="0">
            $key
        </text>""")
        sb.append("</g>")
        sb.append("""<g transform="translate(346,$startY)">""")
        sb.append("""<text style="font-size: 14px; font-family: Arial, Helvetica, sans-serif; font-weight: bold;" x="0" y="0">""")
        var dy = 0
        value.lines.first.forEachIndexed { idx, t ->
            if(idx > 0) {
                dy =14
            }
            sb.append("""<tspan x="0" dy="$dy" style="font-size: 14px; font-family: Arial, Helvetica, sans-serif;">$t</tspan>""")
        }
        sb.append("</text>")
        sb.append("</g>")
        sb.append("""<g transform="translate(686,$startY)">""")
        sb.append("""<text style="font-size: 14px; font-family: Arial, Helvetica, sans-serif; font-weight: bold;" x="0" y="0">""")
        dy = 0
        value.lines.second.forEachIndexed { idx, t ->
            if(idx > 0) {
                dy =14
            }
            sb.append("""<tspan x="0" dy="$dy" style="font-size: 14px; font-family: Arial, Helvetica, sans-serif;">$t</tspan>""")
        }
        sb.append("</text>")
        sb.append("</g>")
        val endY = startY + (value.rows() * 14)
        sb.append("""<line x1="0" x2="1024" y1="$endY" y2="$endY" stroke="#fcfcfc"/>""")
        return sb.toString()
    }
}

fun main() {
    val comparisonChartMaker = ComparisonChartMaker()

    val rows = mutableListOf<Row>()
    rows.add(Row("SpringBoot Version", original = "2.7.0", next = "3.3.3"))
    rows.add(Row("Admin DashBoard?",original = "No", next = "Yes"))
    rows.add(Row("OCF",original = "No", next = "Yes"))
    rows.add(Row("Vault",original = "Vault configured in Bootstrap yaml", next = "Vault Configured in Application since bootstrap is deprecated."))
    rows.add(Row("Redis",original = "PCF did not require explicit binding, builtin to Pivotal Libraries", next = "OCF configured Redis binding since OCF does not support service binding. Redis ssl property migrated from String to boolean of contained class."))
    rows.add(Row("Config Server",original = "Config server migrated from bootstrap yaml", next = "Config Server defined in application yaml and config modified to seamlessly pull vault secrets as well as config using the unified config api."))
    rows.add(Row("Commons Http Client migrate 4 to 5?", original = "4.5.13", next = "5.2.1"))
    rows.add(Row("High or Critical Vulnerabilities", original = "12 High and 2 Critical", next = "0 High and 0 Critical"))
    val comparisonChart = ComparisonChart(
        title = "SpringBoot Upgrade",
        colHeader = mutableListOf("Original", "TMA++"),
        rows = rows
    )
    val str = Json.encodeToString(comparisonChart)
    println(str)
    val svg = comparisonChartMaker.make(comparisonChart)
    val outfile2 = File("gen/compare.svg")
    outfile2.writeBytes(svg.toByteArray())
}