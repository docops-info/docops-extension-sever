package io.docops.extension.server.echart

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@ChartDsl
@kotlinx.serialization.Serializable
class TreeChart {
    var name: String = ""
    var value: Double? = null
    var children: MutableList<TreeChart>? = null
    fun child(treeChart: TreeChart.() -> Unit) {
        val tc = TreeChart().apply(treeChart)
        if (children == null) {
            children = mutableListOf<TreeChart>()
        }
        children?.add(tc)
    }
}

fun treeChart(treeChart: TreeChart.() -> Unit): TreeChart {
    return TreeChart().apply(treeChart)
}

fun TreeChart.toEchart() : String {
    val divId = "div_${System.currentTimeMillis()}"
    val data = Json.encodeToString(this)
    //language=html
    val html = """
        <div id="$divId" style="width: 800px; height: 500px;"></div>

        <script>
            myChart = echarts.init(document.getElementById('$divId'), 'shine');
            myChart.showLoading();
            var data = $data;

            myChart.hideLoading();
            data.children.forEach(function (datum, index) {
                index % 2 === 0 && (datum.collapsed = true);
            });
            myChart.setOption(
                (option = {
                    tooltip: {
                        trigger: 'item',
                        triggerOn: 'mousemove'
                    },
                    label: {
                        show: true
                    },
                    legend: {},
                    toolbox: {
                        show: true,
                        feature: {
                            restore: {},
                            saveAsImage: {}
                        }
                    },
                    series: [
                        {
                            type: 'tree',
                            data: [data],
                            top: '1%',
                            left: '7%',
                            bottom: '1%',
                            right: '20%',
                            symbolSize: 7,
                            label: {
                                position: 'left',
                                verticalAlign: 'middle',
                                align: 'right',
                                fontSize: 9
                            },
                            leaves: {
                                label: {
                                    position: 'right',
                                    verticalAlign: 'middle',
                                    align: 'left'
                                }
                            },
                            emphasis: {
                                focus: 'descendant'
                            },
                            expandAndCollapse: true,
                            animationDuration: 550,
                            animationDurationUpdate: 750
                        }
                    ]
                })
            );

        </script>
    """.trimIndent()
    return html
}
fun main() {
    val tc = treeChart {
        name = "Product Range"
        child {
            name = "USB Converter"
            child {
                name = "USB 3.0 Converter"
            }
            child {
                name = "USB 2.0 Converter"
                child {
                    name = "USB 2 Serial"
                }
                child {
                    name = "USB 2 RS485"
                }
                child {
                    name = "USB 2 Dual Serial"
                }
                child {
                    name = "USB 2 MIDI"
                }
            }
        }
        child {
            name = "HDMI"
        }
    }
    val str = Json.encodeToString(tc)
    println(str)
}