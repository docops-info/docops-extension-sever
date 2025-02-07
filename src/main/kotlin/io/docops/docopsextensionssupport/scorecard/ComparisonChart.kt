package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
class ComparisonChart(
    val id: String = UUID.randomUUID().toString(),
    val title: String, val colHeader: MutableList<String>, val rows: MutableList<Row>, val display: ComparisonChartDisplay = ComparisonChartDisplay()
)
private const val SPLIT_BY = 325

@Serializable
class ComparisonChartDisplay(val id: String = UUID.randomUUID().toString(),
    val titleFontStyle: String = "font-family: Arial,Helvetica, sans-serif; font-size:18px; text-anchor:middle; font-weight: bold;",
    val leftColumnHeaderFontStyle: String = "font-family: Arial,Helvetica, sans-serif; fill: #111111; font-size:14px; text-anchor:middle; font-weight: bold;",
    val rightColumnHeaderFontStyle: String = "font-family: Arial,Helvetica, sans-serif; fill: #111111; font-size:14px; text-anchor:middle; font-weight: bold;",
    val leftColumnColor: String = "#fcfcfc",
    val leftColumnFontColor: String = "#111111",
    val rightColumnColor: String = "#fcfcfc",
    val rightColumnFontColor: String = "#111111",
    val itemColumnColor: String = "#F5F5F7",
    val lineColor: String = "#C4DAD2",
    val rowColor: String = "#F1F1F1",
    val defaultRowColor: String = "#fcfcfc",
    val scale: Double = 1.0
        )

@Serializable
data class Row(val title: String, val original: String, val next: String)

data class ColLine(val lines: Pair<MutableList<String>, MutableList<String>>, val begin: Int, val maxLines: Int = 0)

fun ColLine.rows(): Int {

    return maxOf(lines.first.size, lines.second.size)
}


fun Row.numOfRows(): Int {
    val outcome1 = itemTextWidth(original, SPLIT_BY, 12, "Arial")
    val outcome2 = itemTextWidth(next, SPLIT_BY, 12, "Arial")
    return maxOf(outcome1.size, outcome2.size)
}



fun ComparisonChart.lines(): MutableMap<String, ColLine> {
    val resp = mutableMapOf<String, ColLine>()
    var begin = 110
    rows.forEach { row ->
        val outcome1 = itemTextWidth(row.original, SPLIT_BY, 12, "Arial")
        val outcome2 = itemTextWidth(row.next, SPLIT_BY, 12, "Arial")
        val outcome3 =  itemTextWidth(row.title, SPLIT_BY, 12, "Arial")
        val maxLines = maxOf(outcome3.size,maxOf(outcome1.size, outcome2.size))
        val colLine = ColLine(Pair(outcome1, outcome2), begin, maxLines= maxLines)
        resp.put(row.title, colLine)
        begin += 20 + (maxLines * 14)
    }
    return resp
}
