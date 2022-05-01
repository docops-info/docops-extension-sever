package io.docops.extension.server.echart

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@ChartDsl
class StackedBar {
    var width: Int = 800
    var height: Int = 400
    private val divId = "div_${System.currentTimeMillis()}"
    var showLabel = true
    var title = ""
    var subTitle = ""
    var yAxisLabel = ""
    var xAxisLabel = ""
    private val legend = mutableListOf<String>()
    private val xAxisData = mutableListOf<String>()
    private val seriesData = mutableListOf<Data>()
    private var barChartData: MutableList<BarChartData> = mutableListOf()
    var series = mutableListOf<Series>()
    var dimension = mutableListOf<String>()
    fun s(series: Series.() -> Unit) {
        val ser = Series().apply(series)
        this.series.add(ser)
    }
    fun validate(): StackedBar {
        return this
    }
    
    fun dimensionJson() : String {
        return Json.encodeToString(dimension)
    }
    private fun seriesJson(): String {
        val sb = StringBuilder("[")
        series.forEachIndexed { idx, value ->
            sb.append("${value.tpl(idx)},")
        }
        sb.append("]")
        return sb.toString()
    }
    fun toEChart() : String {
        //language=html
        val tpl = """
            <div id="$divId" style="width: ${width}px; height: ${height}px;"></div>
            <script>
                myChart = echarts.init(document.getElementById('$divId'));
                option = {
                    tooltip: {},
                    label: {
                        show: $showLabel
                    },
                    legend: {},
                    toolbox: {
                        show: true,
                        feature: {
                            dataZoom: {
                                yAxisIndex: 'none'
                            },
                            dataView: {readOnly: false},
                            magicType: {type: ['line', 'bar', 'stack']},
                            restore: {},
                            saveAsImage: {}
                        }
                    },
                    title: {
                        text: '$title',
                        subtext: '$subTitle'
                    },
                    xAxis: {
                        data: ${dimensionJson()}
                    },
                    yAxis: {},
                    series: ${seriesJson()}
                };
                myChart.setOption(option);
            </script>
        """.trimIndent()
        return tpl
    }
}

@ChartDsl
class Series
{
    var name = ""
    var isStack = true
    var stackGroupName:String = ""
    var data = mutableListOf<Double>()

    fun dataJson() : String {
        return Json.encodeToString(data)
    }
    fun tpl(idx: Int): String {
        //language=javascript
        return """
            {
              "name": "$name",
              "type": "bar",
              "stack": "$stackGroupName",
              "emphasis": {
                    "itemStyle": {
                    "shadowBlur": 10,
                    "shadowColor": "rgba(112,128,144, 0.3)"
                  }
              },
              itemStyle: itemStyle($idx),
              "data": ${dataJson()}
            }
        """.trimIndent()
    }
}



fun stackBar(stackedBar: StackedBar.()-> Unit): StackedBar {
    return StackedBar().apply(stackedBar).validate()
}

fun main() {
    val s = stackBar {
        title = "Group 1 Startup Time"
        subTitle = "Memory & Disk Storage"
        dimension = mutableListOf("CT", "NY", "NH", "VT", "ME")
        s{
            name = "Start Up (ms)"
            stackGroupName = "group-1"
            data = mutableListOf(24.0,33.0,23.0,33.0,43.0)
        }
        s{
            name = "Storage (mb)"
            stackGroupName = "group-1"
            data = mutableListOf(5.0,17.0,12.0,14.0,15.0)
        }
        s{
            name= " Memory (mb)"
            stackGroupName = "group-1"
            data = mutableListOf(100.0,120.0,90.0,55.0,62.0)
        }
    }
    println(s.toEChart())
}