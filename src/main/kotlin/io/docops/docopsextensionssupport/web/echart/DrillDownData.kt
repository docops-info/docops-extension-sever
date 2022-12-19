package io.docops.docopsextensionssupport.web.echart

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString




@Serializable
class DataGroup(val dataGroupId: String, val value: Double, val data: MutableList<MutableList<Data>>)

@Serializable
data class Data(val value: Double, val groupId: String)

@ChartDsl
class BarChartModels {
    var width: Int = 800
    var height: Int = 400
    var title = ""
    var subTitle = ""
    var yAxisLabel = ""
    var xAxisLabel = ""
    private val legend = mutableListOf<String>()
    private val xAxisData = mutableListOf<String>()
    private val seriesData = mutableListOf<Data>()
    private var barChartData: MutableList<BarChartData> = mutableListOf()
    var dg = mutableListOf<DataGroup>()

    fun data(inputData: BarChartData.() -> Unit) {
        val dt = BarChartData().apply(inputData)
        barChartData.add(dt)
    }

    private fun fromBcToDataGroup() {
        this.barChartData.forEach {
            val list = mutableListOf<MutableList<Data>>()
            it.nameValues.forEach { values ->
                val m = mutableMapOf(values.name to values.value.toDouble())
                list.add(mutableListOf(Data(values.value.toDouble(), values.name)))
            }
            dg.add(DataGroup(dataGroupId = it.name, value = it.value.toDouble(), data = list))
            seriesData.add(Data(it.value.toDouble(), it.name))
            legend.add(it.name)
        }
    }

    private fun xAxisData() {
        barChartData.forEach {
            xAxisData.add(it.name)
        }

    }


    fun validate(): BarChartModels {
        fromBcToDataGroup()
        xAxisData()
        return this
    }
    fun dataGroupJsonString() : String {
        return Json.encodeToString(dg)
    }
    fun xAxisJson(): String {
        return Json.encodeToString(xAxisData)
    }
    fun seriesDataJson() : String {
        return Json.encodeToString(seriesData)
    }
    fun legendJson(): String {
        return Json.encodeToString(legend)
    }
}

@ChartDsl
class BarChartData {
    var name: String = ""
    var value: Number = 0.0
    var nameValues = mutableListOf<NameValue>()
    fun nv(name: String, value: Number) {
        nameValues.add(NameValue(name, value))
    }
}

@ChartDsl
class NameValue(val name: String, val value: Number)

fun barChart(barChartModels: BarChartModels.() -> Unit): BarChartModels {
    return BarChartModels().apply(barChartModels).validate()
}

