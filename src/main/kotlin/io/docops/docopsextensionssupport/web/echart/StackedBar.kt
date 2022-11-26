package io.docops.docopsextensionssupport.web.echart

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
                myChart = echarts.init(document.getElementById('$divId'), 'shine');
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
                    calculable: true,
                    grid: {
                      top: 80,
                      bottom: 100,
                      tooltip: {
                        trigger: 'axis',
                        axisPointer: {
                          type: 'shadow',
                          label: {
                            show: true,
                            formatter: function (params) {
                              return params.value.replace('\n', '');
                            }
                          }
                        }
                      }
                    },
                    xAxis: {
                        name: "$xAxisLabel",
                        data: ${dimensionJson()}
                    },
                    yAxis: {
                        name: "$yAxisLabel"
                    },
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
        //language=json
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
              "data": ${dataJson()}
            }
        """.trimIndent()
    }
}



fun stackBar(stackedBar: StackedBar.()-> Unit): StackedBar {
    return StackedBar().apply(stackedBar).validate()
}
