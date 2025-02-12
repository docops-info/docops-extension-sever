package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.adr.model.escapeXml
import io.docops.docopsextensionssupport.button.shape.joinXmlLines
import io.docops.docopsextensionssupport.support.determineTextColor
import io.docops.docopsextensionssupport.support.svgGradient
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.awt.Font

class ComparisonTableMaker {
    fun make(comparisonChart: ComparisonChart): String {
        var lastLine = comparisonChart.lines().entries.last().value.begin
        lastLine += (comparisonChart.lines().entries.last().value.rows() * 12)
        val sb = StringBuilder()
        sb.append(head(comparisonChart, lastLine))
        sb.append(defs(comparisonChart))
        sb.append(headerRow(comparisonChart, lastLine))
        var startY = 64
        var evenOdd = 0
        var idx = 1
        comparisonChart.lines().forEach { (key, value) ->
            val keyToRows = itemTextWidth(key, 310, 12, "Arial", style = Font.BOLD)
            val totalLinesMax = maxOf(value.maxLines, keyToRows.size)

            sb.append(makeRow(value = value, startY = startY, display = comparisonChart.display, isEven(evenOdd), keyToRows, totalLinesMax, idx++))

            startY += (34 + ((totalLinesMax-1) * 12))
            evenOdd++
        }
        sb.append(tail(comparisonChart))
        return joinXmlLines(sb.toString())
    }

    private fun isEven(number: Int): Boolean {
        return number % 2 == 0
    }
    private fun head(comparisonChart: ComparisonChart, lastLine: Int) = """
        <svg id="ID_${comparisonChart.id}" xmlns="http://www.w3.org/2000/svg" width="${(1024 * comparisonChart.display.scale )/ DISPLAY_RATIO_16_9}" height="${((lastLine + 13) * comparisonChart.display.scale) / DISPLAY_RATIO_16_9}" viewBox="0 0 1024.0 ${lastLine+13}" preserveAspectRatio="xMidYMin slice">
    """.trimIndent()
    private fun tail(comparisonChart: ComparisonChart) = "</svg>"

    private fun headerRow(comparisonChart: ComparisonChart, lastLine: Int) : String {
        val sb = StringBuilder()
        val textColor = determineTextColor(comparisonChart.display.itemColumnColor)
        //language=svg
        sb.append("""
            <g aria-label="title">
            <rect y="0" width="100%" height="30" fill="${comparisonChart.display.itemColumnColor}" aria-label="title bar" />
            <text x="512" y="24" text-anchor="middle" style="${comparisonChart.display.titleFontStyle}; fill:$textColor;" aria-label='${comparisonChart.title}'>${comparisonChart.title.escapeXml()}</text>
            </g>
            <g aria-label="header" class="rowShade">
               <g transform="translate(0,0)">
                <rect y="30" width="341" height="34" fill="${comparisonChart.display.itemColumnColor}" />
                </g>
                <g transform="translate(341,0)">
                    <rect y="30" width="341" height="34" fill="${comparisonChart.display.leftColumnColor}" aria-label='middle column header'/>
                    <text x="170" y="56" text-anchor="middle" style="${comparisonChart.display.leftColumnHeaderFontStyle}">
                         ${comparisonChart.colHeader[0].escapeXml()}
                    </text>
                </g>
                <g transform="translate(682,0)">
                    <rect y="30" width="341" height="34" fill="${comparisonChart.display.rightColumnColor}" aria-label='right column header'/>
                    <text x="170" y="56" text-anchor="middle" style="${comparisonChart.display.rightColumnHeaderFontStyle}">
                         ${comparisonChart.colHeader[1].escapeXml()}
                    </text>
                </g>
                <line x1="0" x2="1024" y1="64" y2="64" stroke="#cccccc"/>
            </g>
        """.trimIndent())
        return sb.toString()
    }

    private fun makeRow(
        value: ColLine,
        startY: Int,
        display: ComparisonChartDisplay,
        even: Boolean,
        keyToRows: MutableList<String>,
        totalLinesMax: Int,
        idx: Int
    ) : String {
        val sb = StringBuilder()
        //col 1
        val textColor = determineTextColor(display.itemColumnColor)

        val rowHeight = 34 + ((totalLinesMax-1) * 12)

        val dy = 0
        val sb2 = StringBuilder()
        columnText(keyToRows, dy, sb2, determineTextColor(display.itemColumnColor))
        sb.append("<g aria-label=\"row $idx\" class=\"rowShade\">")
        sb.append("""
        <g transform="translate(0,$startY)">    
        <rect y="0" width="341" height="$rowHeight" fill="${display.itemColumnColor}" />
        <text style="font-size: 12px; font-family: Arial, Helvetica, sans-serif; font-weight: bold; fill: $textColor;" x="5" y="24">
            $sb2
        </text>
        </g>
        """.trimIndent())


        var rowFill = display.defaultRowColor
        if(even) {
            rowFill = display.rowColor
        }
        sb.append("""
            <g transform="translate(341,$startY)">
            <rect y="0" width="341" height="$rowHeight" fill="$rowFill"/>
            <text id="path1" style="font-size: 12px; font-family: Arial, Helvetica, sans-serif; " x="2" y="24">""".trimIndent())
        columnText(value.lines.first, dy, sb, display.leftColumnFontColor)
            sb.append("</text></g>")
        //language=svg
        sb.append("""
            <g transform="translate(682,$startY)">
        <rect y="0" width="341" height="$rowHeight" fill="$rowFill"/>
        <text style="font-size: 12px; font-family: Arial, Helvetica, sans-serif; " x="2" y="24">
        """.trimIndent())
        columnText(value.lines.second, dy, sb, display.rightColumnFontColor)
        sb.append("</text></g>")
        sb.append("""<line x1="0" x2="1024" y1="${startY+rowHeight}" y2="${startY+rowHeight}" stroke="#cccccc"/>""")
        sb.append("</g>")
        return sb.toString()
    }
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
      <style>
            .rowShade {
                pointer-events: bounding-box;
            }
            .rowShade:hover {
                filter: grayscale(100%) sepia(100%);
            }

        </style>
    </defs>
    """.trimIndent()
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
            sb.append("""<tspan x="5" dy="$dy1" style="font-size: 12px; font-family: Arial, Helvetica, sans-serif;fill: ${display};">${t.escapeXml()}</tspan>""")
        }
        return dy1
    }

    fun toCsv(comparisonChart: ComparisonChart): String {
        val header = ""","${comparisonChart.title}","""
        val subHeader = ""","${comparisonChart.colHeader[0]}","${comparisonChart.colHeader[1]}""""
        val sb = StringBuilder()
        sb.append(header)
        sb.appendLine()
        sb.append(subHeader)
        sb.appendLine()
        comparisonChart.lines().forEach { (key, value) ->
            sb.append(""""$key","${value.lines.first.joinToString(" ")}","${value.lines.second.joinToString(" ")}"""")
            sb.appendLine()

        }
        return sb.toString()

    }
}

fun main() {
    val comparisonTableMaker = ComparisonTableMaker()
    val rows = mutableListOf<Row>()
    rows.add(Row("SpringBoot Version", original = "2.7.0", next = "3.3.3"))
    rows.add(Row("Admin DashBoard?",original = "No", next = "Yes"))
    rows.add(Row("OCF",original = "No", next = "Yes"))
    rows.add(Row("Vault",original = "Vault configured in Bootstrap yaml", next = "Vault Configured in Application since bootstrap is deprecated."))
    rows.add(Row("Redis",original = "PCF did not require explicit binding, builtin to Pivotal Libraries", next = "OCF configured Redis binding since OCF does not support service binding. Redis ssl property migrated from String to boolean of contained class."))
    rows.add(Row("Config Server",original = "Config server migrated from bootstrap yaml", next = "Config Server defined in application yaml and config modified to seamlessly pull vault secrets as well as config using the unified config api."))
    val comparisonChart = ComparisonChart(
        title = "Spring Boot 3 Upgrade and Migration to OCF",
        colHeader = mutableListOf("Started", "Now We are Here (TMA++)"),
        rows = rows
    )
    val str = Json.encodeToString(comparisonChart)
    println(str)
    val svg = comparisonTableMaker.make(comparisonChart)
    val outfile2 = File("gen/compare.svg")
    outfile2.writeBytes(svg.toByteArray())
    println(comparisonTableMaker.toCsv(comparisonChart))
}