package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.support.svgGradient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.text.toByteArray

class ComparisonChartMaker {
    fun make(comparisonChart: ComparisonChart): String {
        var lastLine = comparisonChart.lines().entries.last().value.begin
        lastLine += (comparisonChart.lines().entries.last().value.rows() * 12) + 5

        val sb = StringBuilder()
        sb.append(head(comparisonChart, lastLine))
        sb.append(defs(comparisonChart))
        sb.append(headerRow(comparisonChart, lastLine))
        comparisonChart.lines().forEach { (key, value) ->
            sb.append(makeRow(key= key, value = value, startY = value.begin, display = comparisonChart.display, lastLine))
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
        val item = svgGradient(comparisonChart.display.itemColumnColor, "ID_${comparisonChart.display.id}_item")
        val left = svgGradient(comparisonChart.display.leftColumnColor, "ID_${comparisonChart.display.id}_leftCol")
        val right = svgGradient(comparisonChart.display.rightColumnColor, "ID_${comparisonChart.display.id}_rightCol")
        //language=svg
        return """
        <defs>
        $item
        $left
        $right
        <filter id="shadow" x="0" y="0" width="200%" height="200%">
            <feDropShadow dx="3" dy="3" stdDeviation="1" flood-color="#cccccc" flood-opacity="1" />
        </filter>
    </defs>
    """.trimIndent()
    }

    private fun headerRow(comparisonChart: ComparisonChart, lastLine: Int) : String {
        val sb = StringBuilder()
        val textColor = determineTextColor(comparisonChart.display.itemColumnColor)
        //language=svg
        sb.append("""
               <rect x="0" y="0" width="341" height="100%" fill="url(#ID_${comparisonChart.display.id}_item)" aria-label='left column'/>
               <g transform="translate(0,0)">
                <rect y="30" width="341" height="34" fill="${comparisonChart.display.itemColumnColor}" stroke="#a3742c"/>
                </g>
               <g transform="translate(342,0)">
               <rect x="0" y="0" width="340" height="$lastLine" fill="${comparisonChart.display.leftColumnColor}" aria-label='middle column'/>
               <rect y="30" width="341" height="34" fill="#f5f5f7" stroke="#a3742c"/>
               <text x="170" y="56" text-anchor="middle" style="${comparisonChart.display.leftColumnHeaderFontStyle}" >
                    ${comparisonChart.colHeader[0]}
                </text>
                </g>
                <g transform="translate(684,0)">
               <rect x="0" y="0" width="340" height="$lastLine" fill="${comparisonChart.display.rightColumnColor}" aria-label='right column'/>
               <rect y="30" width="341" height="34" fill="#f5f5f7" stroke="#a3742c"/>
               <text x="170" y="56" text-anchor="middle" style="${comparisonChart.display.rightColumnHeaderFontStyle}" >
                    ${comparisonChart.colHeader[1]}
                </text>
                </g>
                <rect y="0" width="100%" height="30" fill="url(#ID_${comparisonChart.display.id}_item)" aria-label='title bar'/>
                <text x="512" y="24" text-anchor="middle" style="${comparisonChart.display.titleFontStyle}; fill:$textColor;" aria-label='${comparisonChart.title}'>${comparisonChart.title}</text>
        """.trimIndent())
        return sb.toString()
    }
    private fun makeRow(key: String, value: ColLine, startY: Int, display: ComparisonChartDisplay, lastLine: Int) : String {
        val sb = StringBuilder()
        //col 1
        //language=
        sb.append("""<g transform="translate(2,$startY)">""")
        val textColor = determineTextColor(display.itemColumnColor)
        //language=
        sb.append("""<text style="font-size: 12px; font-family: Arial, Helvetica, sans-serif; font-weight: bold; fill: $textColor;" x="5" y="0">
            $key
        </text>""")
        sb.append("</g>")
        var dy = 0

        //language=svg
        sb.append("""<g transform="translate(346,$startY)">""")
        sb.append("<text style='font-size: 12px; font-family: Arial, Helvetica, sans-serif; font-weight: bold;' x='0' y='0'>")
        columnText(value.lines.first, dy, sb, display.leftColumnFontColor)
        sb.append("</text>")
        sb.append("</g>")
        sb.append("""<g transform="translate(686,$startY)">""")
        sb.append("""<text style="font-size: 12px; font-family: Arial, Helvetica, sans-serif; font-weight: bold;" x="0" y="0">""")
        dy = 0
        //language=
        columnText(value.lines.second, dy, sb, display.rightColumnFontColor)
        sb.append("</text>")
        sb.append("</g>")
        val endY = startY + (value.rows() * 14)
        //language=
        sb.append("""<line x1="0" x2="1024" y1="$endY" y2="$endY" stroke="#a3742c"/>""")
        sb.append("""<line x1="341" x2="341" y1="30" y2="$lastLine" stroke="#a3742c"/>""")
        sb.append("""<line x1="683" x2="683" y1="30" y2="$lastLine" stroke="#a3742c"/>""")
        return sb.toString()
    }

    private fun columnText(
        value: MutableList<String>,
        dy: Int,
        sb: StringBuilder,
        display: String
    ): Int {
        var dy1 = dy
        value.forEachIndexed { idx, t ->
            if (idx > 0) {
                dy1 = 14
            }
            sb.append("""<tspan x="0" dy="$dy1" style="font-size: 12px; font-family: Arial, Helvetica, sans-serif;fill: ${display};">$t</tspan>""")
        }
        return dy1
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