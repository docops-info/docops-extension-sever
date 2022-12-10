package io.docops.docopsextensionssupport.web.echart

import io.docops.asciidoc.buttons.service.ScriptLoader
import io.docops.docopsextensionssupport.aop.LogExecution
import io.github.wimdeblauwe.hsbt.mvc.HtmxResponse
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.lang.IllegalArgumentException
import java.nio.charset.Charset


@Controller
@RequestMapping("/api")
@LogExecution
class ChartRoute(private val observationRegistry: ObservationRegistry) {

    private val scriptLoader = ScriptLoader()


    @PostMapping("/bar")
    @ResponseBody
    fun bar(httpServletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
        return Observation.createNotStarted("docops.charts.bar", observationRegistry).observe {
            try {
                val source = getPostBody(httpServletRequest)
                val data = scriptLoader.parseKotlinScript<BarChartModels>(source)
                val res = fromTpl(data)
                process(res, servletResponse)
            } catch (e: Exception) {
                throw IllegalArgumentException(e.message,e)
            }
        }
    }

    @PostMapping("/bar/stacked")
    fun stacked(httpServletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
        return Observation.createNotStarted("docops.charts.bar.stacked", observationRegistry).observe {
            try {
                val source = getPostBody(httpServletRequest)
                val data = scriptLoader.parseKotlinScript<StackedBar>(source)
                val res = data.toEChart()
                process(res, servletResponse)
            } catch (e: Exception) {
                throw IllegalArgumentException(e.message,e)
            }
        }
    }

    @PostMapping("/treechart")
    fun treeChart(httpServletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
        return Observation.createNotStarted("docops.charts.treechart", observationRegistry).observe {
            try {
                val source = getPostBody(httpServletRequest)
                val data = scriptLoader.parseKotlinScript<TreeChart>(source)
                val res = data.toEchart()
                process(res, servletResponse)
            } catch (e: Exception) {
                throw IllegalArgumentException(e.message,e)
            }
        }
    }

    @PostMapping("/chart/custom")
    fun custom(httpServletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
        return Observation.createNotStarted("docops.charts.custom", observationRegistry).observe {
            try {
                val source = StreamUtils.copyToString(httpServletRequest.inputStream, Charset.defaultCharset())

                val width = httpServletRequest.getParameter("width")
                val height = httpServletRequest.getParameter("height")
                val resp = ChartShell(width = width, height = height)
                val res = resp.build(source)
                process(res, servletResponse)
            } catch (e: Exception) {
                throw IllegalArgumentException(e.message,e)
            }
        }
    }

    private fun getPostBody(httpServletRequest: HttpServletRequest): String {
        val contents = StreamUtils.copyToString(httpServletRequest.inputStream, Charset.defaultCharset())
        return """
                import io.docops.docopsextensionssupport.web.echart.*
                
                $contents
                """.trimIndent()
    }

    private fun process(res: String, servletResponse: HttpServletResponse) {
        try {
            servletResponse.contentType = "text/html";
            servletResponse.characterEncoding = "UTF-8";
            servletResponse.status = 200
            val writer = servletResponse.writer
            writer.print(res)
            writer.flush()
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message,e)
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