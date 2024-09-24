package io.docops.docopsextensionssupport.scorecard

import io.docops.docopsextensionssupport.svgsupport.itemTextWidth
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
class ComparisonChart(
    val id: String = UUID.randomUUID().toString(),
    val title: String, val colHeader: MutableList<String>, val rows: MutableList<Row>
)

@Serializable
data class Row(val title: String, val original: String, val next: String)

data class ColLine(val lines: Pair<MutableList<String>, MutableList<String>>, val begin: Int)

fun ColLine.rows(): Int {
    return maxOf(lines.first.size, lines.second.size)
}

private const val SPLIT_BY = 300

fun Row.numOfRows(): Int {
    val outcome1 = itemTextWidth(original, SPLIT_BY, 14, "Arial")
    val outcome2 = itemTextWidth(next, SPLIT_BY, 14, "Arial")
    return maxOf(outcome1.size, outcome2.size)
}


fun ComparisonChart.lines(): MutableMap<String, ColLine> {
    val resp = mutableMapOf<String, ColLine>()
    var begin = 71
    rows.forEach { row ->
        val outcome1 = itemTextWidth(row.original, SPLIT_BY, 14, "Arial")
        val outcome2 = itemTextWidth(row.next, SPLIT_BY, 14, "Arial")
        val maxLines = maxOf(outcome1.size, outcome2.size)
        val colLine = ColLine(Pair(outcome1, outcome2), begin)
        resp.put(row.title, colLine)
        begin += 20 + (maxLines * 14)
    }
    return resp
}