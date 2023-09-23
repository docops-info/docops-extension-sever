/*
 * Copyright (c) 2023. The DocOps Consortium
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.docops.docopsextensionssupport.web.echart

import io.docops.asciidoc.buttons.service.ScriptLoader
import io.docops.asciidoc.charts.*
import io.docops.docopsextensionssupport.web.panel.uncompressString
import io.micrometer.core.annotation.Timed
import io.micrometer.observation.annotation.Observed
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.*
import java.lang.IllegalArgumentException
import java.nio.charset.Charset


/**
 * This class represents the ChartRoute controller which handles various chart-related requests.
 * It provides methods for generating different types of charts using the provided data.
 */
@Controller
@RequestMapping("/api")
@Observed(name = "chart.controller")
class ChartRoute() {

    private val scriptLoader = ScriptLoader()


    @PostMapping("/bar")
    @ResponseBody
    @Timed(value = "docops.charts.bar", histogram = true, percentiles = [0.5, 0.95])
    fun bar(httpServletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
            try {
                val source = getPostBody(httpServletRequest)
                val data = scriptLoader.parseKotlinScript<BarChartModels>(source)
                val res = fromTpl(data)
                process(res, servletResponse)
            } catch (e: Exception) {
                throw IllegalArgumentException(e.message,e)
            }
    }

    @PostMapping("/bar/stacked")
    @Timed(value = "docops.charts.bar.stacked", histogram = true, percentiles = [0.5, 0.95])
    fun stacked(httpServletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
            try {
                val source = getPostBody(httpServletRequest)
                val data = scriptLoader.parseKotlinScript<StackedBar>(source)
                val res = data.toEChart()
                process(res, servletResponse)
            } catch (e: Exception) {
                throw IllegalArgumentException(e.message,e)
            }
    }

    @PostMapping("/treechart")
    @Timed(value = "docops.charts.treechart", histogram = true, percentiles = [0.5, 0.95])
    fun treeChart(httpServletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
            try {
                val source = getPostBody(httpServletRequest)
                val data = scriptLoader.parseKotlinScript<TreeChart>(source)
                val res = data.toEchart()
                process(res, servletResponse)
            } catch (e: Exception) {
                throw IllegalArgumentException(e.message,e)
            }
    }

    @PostMapping("/chart/custom")
    @Timed(value = "docops.charts.custom", histogram = true, percentiles = [0.5, 0.95])
    fun custom(httpServletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
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

    @GetMapping("/echart")
    @Timed(value = "docops.charts.loadEChart", histogram = true, percentiles = [0.5, 0.95])
    fun loadEChart(@RequestParam("data") payload: String, @RequestParam("type") type: String, @RequestParam(required = false,defaultValue = "800") width: String = "800",@RequestParam(required = false,defaultValue = "500") height: String = "500", servletResponse: HttpServletResponse) {
        val source = uncompressString(payload)
        when (type) {
            "bar" -> {
                val data = scriptLoader.parseKotlinScript<BarChartModels>(source)
                val res = fromTpl(data)
                process(res, servletResponse)
            }
            "stackbar" -> {
                val data = scriptLoader.parseKotlinScript<StackedBar>(source)
                val res = data.toEChart()
                process(res, servletResponse)
            }
            "tree" -> {
                val data = scriptLoader.parseKotlinScript<TreeChart>(source)
                val res = data.toEchart()
                process(res, servletResponse)
            }
            "custom" -> {
                val resp = ChartShell(width = width, height = height)
                val res = resp.build(source)
                process(res, servletResponse)
            }
        }
    }
    private fun getPostBody(httpServletRequest: HttpServletRequest): String {
        val contents = StreamUtils.copyToString(httpServletRequest.inputStream, Charset.defaultCharset())
        return """
                import io.docops.asciidoc.charts.*
                import kotlin.collections.*
                
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

fun dataGroupJsonString(dg: MutableList<DataGroup>) : String {
    return Json.encodeToString(dg)
}

fun seriesDataJson(seriesData: MutableList<Data>) : String {
    return Json.encodeToString(seriesData)
}
fun xAxisJson(xAxisData: MutableList<String>): String {
    return Json.encodeToString(xAxisData)
}

fun legendJson(legend: MutableList<String>): String {
    return Json.encodeToString(legend)
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
                data: ${legendJson(bcModels.legend)},
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
                    data: ${xAxisJson(bcModels.xAxisData)}
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
                    data: ${seriesDataJson(bcModels.seriesData)},
                    universalTransition: {
                        enabled: true,
                        divideShape: 'clone'
                    }
                }
            };
            const drillDownData = ${dataGroupJsonString(bcModels.dg)};
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