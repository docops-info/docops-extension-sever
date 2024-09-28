package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.support.svgGradient
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
            sb.append(makeRow(key= key, value = value, startY = value.begin, display = comparisonChart.display))
        }
        sb.append(tail(comparisonChart))
        return sb.toString()
    }

    //language=svg
    private fun head(comparisonChart: ComparisonChart, lastLine: Int) = """
        <svg id="ID_${comparisonChart.id}"
     xmlns="http://www.w3.org/2000/svg"
     width="${1024 * comparisonChart.display.scale}" height="${lastLine * comparisonChart.display.scale}"
     viewBox="0 0 1024.0 $lastLine"
     preserveAspectRatio="xMidYMin slice"
     >
    """.trimIndent()

    private fun tail(comparisonChart: ComparisonChart) = "</svg>"
    private fun defs(comparisonChart: ComparisonChart): String {
        val left = svgGradient(comparisonChart.display.leftColumnColor, "ID_${comparisonChart.display.id}_leftCol")
        val right = svgGradient(comparisonChart.display.rightColumnColor, "ID_${comparisonChart.display.id}_rightCol")
        //language=svg
        return """
        <defs>
        $left
        $right
        <linearGradient id="leftCol" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="#e5e5e5"/>
            <stop class="stop2" offset="50%" stop-color="#d8d8d8"/>
            <stop class="stop3" offset="100%" stop-color="#cccccc"/>
        </linearGradient>
        <filter id="shadow" x="0" y="0" width="200%" height="200%">
            <feDropShadow dx="3" dy="3" stdDeviation="1" flood-color="#cccccc" flood-opacity="1" />
        </filter>
    </defs>
    """.trimIndent()
    }

    fun headerRow(comparisonChart: ComparisonChart, lastLine: Int) : String {
        val sb = StringBuilder()
        //language=svg
        sb.append("""
               <rect x="0" y="0" width="340" height="$lastLine" fill="url(#leftCol)" style="filter: url(#shadow);"/>
               <g transform="translate(342,0)">
               <rect x="0" y="0" width="340" height="$lastLine" fill="url(#ID_${comparisonChart.display.id}_leftCol)" style="filter: url(#shadow);"/>
               <rect y="40" width="341" height="26" fill="url(#ID_${comparisonChart.display.id}_leftCol)"/>
               <text x="170" y="60" text-anchor="middle" style="${comparisonChart.display.leftColumnHeaderFontStyle}" >
                    ${comparisonChart.colHeader[0]}
                </text>
                </g>
                <g transform="translate(684,0)">
               <rect x="0" y="0" width="340" height="$lastLine" fill="url(#ID_${comparisonChart.display.id}_rightCol)" style="filter: url(#shadow);"/>
               <rect y="40" width="341" height="26" fill="url(#ID_${comparisonChart.display.id}_rightCol)"/>
               <text x="170" y="60" text-anchor="middle" style="${comparisonChart.display.rightColumnHeaderFontStyle}" >
                    ${comparisonChart.colHeader[1]}
                </text>
                </g>
                <rect y="0" width="100%" height="30" fill="url(#leftCol)" filter="url(#shadow)"/>
                <text x="512" y="24" text-anchor="middle" style="${comparisonChart.display.titleFontStyle}">${comparisonChart.title}</text>
        """.trimIndent())
        return sb.toString()
    }
    fun makeRow(key: String, value: ColLine, startY: Int, display: ComparisonChartDisplay) : String {
        val sb = StringBuilder()
        //col 1
        //language=svg
        sb.append("""<g transform="translate(2,$startY)">""")
        //language=svg
        sb.append("""<text style="font-size: 14px; font-family: Arial, Helvetica, sans-serif; font-weight: bold;" x="5" y="0">
            $key
        </text>""")
        sb.append("</g>")
        //language=svg
        sb.append("""<g transform="translate(346,$startY)">""")
        sb.append("<text style='font-size: 14px; font-family: Arial, Helvetica, sans-serif; font-weight: bold;' x='0' y='0'>")
        var dy = 0
        //language=svg
        value.lines.first.forEachIndexed { idx, t ->
            if(idx > 0) {
                dy =14
            }
            sb.append("""<tspan x="0" dy="$dy" style="font-size: 14px; font-family: Arial, Helvetica, sans-serif;fill: ${display.leftColumnFontColor};">$t</tspan>""")
        }
        sb.append("</text>")
        sb.append("</g>")
        //language=svg
        sb.append("""<g transform="translate(686,$startY)">""")
        sb.append("""<text style="font-size: 14px; font-family: Arial, Helvetica, sans-serif; font-weight: bold;" x="0" y="0">""")
        dy = 0
        //language=svg
        value.lines.second.forEachIndexed { idx, t ->
            if(idx > 0) {
                dy =14
            }
            sb.append("""<tspan x="0" dy="$dy" style="font-size: 14px; font-family: Arial, Helvetica, sans-serif;fill: ${display.rightColumnFontColor};">$t</tspan>""")
        }
        sb.append("</text>")
        sb.append("</g>")
        val endY = startY + (value.rows() * 14)
        //language=svg
        sb.append("""<line x1="341" x2="1024" y1="$endY" y2="$endY" stroke="#fcfcfc"/>""")
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
    rows.add(Row("Duplicate Filter clean up", original = "Request Filter in app and library", next = "Request filter deleted from application and kept in the library"))
    val comparisonChart = ComparisonChart(
        title = "Spring Boot 3 Upgrade and Migration to OCF",
        colHeader = mutableListOf("Started", "Now We are Here (TMA++)"),
        rows = rows
    )
    val str = Json.encodeToString(comparisonChart)
    println(str)
    val svg = comparisonChartMaker.make(comparisonChart)
    val outfile2 = File("gen/compare.svg")
    outfile2.writeBytes(svg.toByteArray())
}