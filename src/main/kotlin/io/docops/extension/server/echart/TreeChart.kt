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
    val tc = TreeChart().apply(treeChart)
    return tc
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