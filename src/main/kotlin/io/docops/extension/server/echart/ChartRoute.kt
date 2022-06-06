package io.docops.extension.server.echart

import io.docops.asciidoc.buttons.service.ScriptLoader
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.chartRoutes(){
    val scriptLoader = ScriptLoader()
    route("/api") {
        post("/bar") {
            try {
                val contents = call.receiveText()
                val source = """
            import io.docops.extension.server.echart.*
            
            $contents
        """.trimIndent()
                val data = scriptLoader.parseKotlinScript<BarChartModels>(source)
                val res = fromTpl(data)
                call.respondBytes(res.toByteArray(), ContentType.Text.Html, HttpStatusCode.Accepted)
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
        post("/bar/stacked") {
            try {
                val contents = call.receiveText()
                val source = """
            import io.docops.extension.server.echart.*
            
            $contents
        """.trimIndent()
                val data = scriptLoader.parseKotlinScript<StackedBar>(source)
                val res = data.toEChart()
                call.respondBytes(res.toByteArray(), ContentType.Text.Html, HttpStatusCode.Accepted)
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
        post("/treechart") {
            try {
                val contents = call.receiveText()
                //language=kotlin
                val source = """
            import io.docops.extension.server.echart.*
            
            $contents
            """.trimIndent()
                val data = scriptLoader.parseKotlinScript<TreeChart>(source)
                val res = data.toEchart()
                call.respondBytes(res.toByteArray(), ContentType.Text.Html, HttpStatusCode.Accepted)
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
        post("/chart/custom") {
            try {
                val contents = call.receiveText()
                val width = call.request.queryParameters["width"]
                val height = call.request.queryParameters["height"]
                val resp = ChartShell(width= width, height= height)
                call.respondBytes (resp.build(contents).toByteArray(), ContentType.Text.Html, HttpStatusCode.Accepted)
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }
}

fun fromTpl(bcModels: BarChartModels): String {
    //language=html
    val tpl = """
        <div id="drill" style="width: ${bcModels.width}px; height: ${bcModels.height}px;"></div>
        <script>
            myChart = echarts.init(document.getElementById('drill'));
            var barColors = [
                ['rgba(176,196,222, 0.3)', 'rgba(176,196,222, 1)'],
                ['rgba(220,20,60, 0.3)', 'rgba(220,20,60, 1)'],
                ['rgba(189,183,107, 0.3)', 'rgba(189,183,107, 1)'],
                ['rgba(47,79,79, 0.3)', 'rgba(47,79,79, 1)'],
                ['rgba(30,144,255, 0.3)', 'rgba(30,144,255, 1)'],
                ['rgba(112,128,144, 0.3)', 'rgba(112,128,144, 1)'],
            ];
            option = {
            legend: {
                data: ${bcModels.legendJson()},
                orient: 'vertical',
                right: 10,
                top: 'center'
            },
            toolbox: {
                show: true,
                feature: {
                  dataZoom: {
                    yAxisIndex: 'none'
                  },
                  dataView: { readOnly: false },
                  magicType: { type: ['line', 'bar'] },
                  restore: {},
                  saveAsImage: {}
                }
              },
            title: {
                    text: '${bcModels.title}',
                    subtext: '${bcModels.subTitle}'
                 },
                xAxis: {
                    name: '${bcModels.xAxisLabel}',
                    data: ${bcModels.xAxisJson()}
                },
                yAxis: {
                    name: '${bcModels.yAxisLabel}'
                },
                dataGroupId: '',
                animationDurationUpdate: 500,
                series: {
                    type: 'bar',
                    id: 'sales',
                    itemStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: 'rgba(220,20,60, 0.3)' },
                            { offset: 0.5, color: 'rgba(220,20,60, 0.5)' },
                            { offset: 1, color: 'rgba(220,20,60, 1)' }
                        ])
                    },
                    emphasis: {
                        itemStyle: {
                          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: '#2378f7' },
                            { offset: 0.7, color: '#2378f7' },
                            { offset: 1, color: '#83bff6' }
                          ])
                        }
                      },
                    data: ${bcModels.seriesDataJson()},
                    universalTransition: {
                        enabled: true,
                        divideShape: 'clone'
                    }
                }
            };
            const drillDownData = ${bcModels.dataGroupJsonString()};
            myChart.on('click', function (event) {
                if (event.data) {
                    let subData = drillDownData.find(function (data) {
                        return data.dataGroupId === event.data.groupId;
                    });
                    if (!subData) {
                        return;
                    }
                    myChart.setOption({
                        xAxis: {
                            data: subData.data.map(function (item) {
                                return item[0]["groupId"];
                            })
                        },
                        series: {
                            type: 'bar',
                            id: 'sales',
                            dataGroupId: subData.dataGroupId,
                            data: subData.data.map(function (item) {
                                return item[0]["value"];
                            }),
                            universalTransition: {
                                enabled: true,
                                divideShape: 'clone'
                            }
                        },
                        graphic: [
                            {
                                type: 'text',
                                left: 200,
                                top: 20,
                                style: {
                                    text: 'Back',
                                    fontSize: 18
                                },
                                onclick: function () {
                                    myChart.setOption(option);
                                }
                            }
                        ]
                    });
                }
            });
            myChart.setOption(option);
        </script>
    """.trimIndent()
    return tpl
}