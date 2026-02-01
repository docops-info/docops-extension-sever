package io.docops.docopsextensionssupport.chart.gauge

import io.docops.docopsextensionssupport.util.ParsingUtils


object GaugeParser {
    
    fun parseTabularInput(payload: String): GaugeChart {
        val (config, data) = ParsingUtils.parseConfigAndData(payload)
        
        val type = GaugeType.valueOf(
            config["type"]?.uppercase() ?: "SEMI_CIRCLE"
        )
        
        val gauges = mutableListOf<GaugeData>()
        val lines = data.lines().filter { it.isNotBlank() }
        
        // Skip header line
        lines.drop(1).forEach { line ->
            val parts = line.split("|").map { it.trim() }
            
            when (type) {
                GaugeType.DASHBOARD -> {
                    // Type | Label | Value | Min | Max | Unit | Color | Extra
                    if (parts.size >= 6) {
                        val gaugeType = GaugeType.valueOf(parts[0].uppercase())
                        val extra = if (parts.size > 7) parseExtra(parts[7]) else mapOf()
                        
                        gauges.add(
                            GaugeData(
                                label = parts[1],
                                value = parts[2].toDoubleOrNull() ?: 0.0,
                                min = parts[3].toDoubleOrNull() ?: 0.0,
                                max = parts[4].toDoubleOrNull() ?: 100.0,
                                unit = parts[5],
                                color = parts.getOrNull(6)?.takeIf { it.isNotEmpty() },
                                target = extra["target"]?.toDoubleOrNull(),
                                statusText = extra["status"],
                                type = gaugeType
                            )
                        )
                    }
                }
                else -> {
                    // Label | Value | Min | Max | Unit | Color | Target/StatusText
                    if (parts.size >= 5) {
                        gauges.add(
                            GaugeData(
                                label = parts[0],
                                value = parts[1].toDoubleOrNull() ?: 0.0,
                                min = parts[2].toDoubleOrNull() ?: 0.0,
                                max = parts[3].toDoubleOrNull() ?: 100.0,
                                unit = parts[4],
                                color = parts.getOrNull(5)?.takeIf { it.isNotEmpty() },
                                target = parts.getOrNull(6)?.toDoubleOrNull(),
                                statusText = parts.getOrNull(6)
                            )
                        )
                    }
                }
            }
        }
        
        val display = GaugeDisplay(
            useDark = config["useDark"]?.toBoolean() ?: false,
            visualVersion = config["visualVersion"]?.toIntOrNull() ?: 1,
            scale = config["scale"]?.toFloatOrNull() ?: 1.0f,
            showLegend = config["showLegend"]?.toBoolean() ?: false,
            showRanges = config["showRanges"]?.toBoolean() ?: true,
            showTarget = config["showTarget"]?.toBoolean() ?: false,
            showLabel = config["showLabel"]?.toBoolean() ?: true,
            showArc = config["showArc"]?.toBoolean() ?: true,
            showStatus = config["showStatus"]?.toBoolean() ?: false,
            animateArc = config["animateArc"]?.toBoolean() ?: true,
            columns = config["columns"]?.toIntOrNull() ?: 3,
            layout = config["layout"] ?: "1x1",
            innerRadius = config["innerRadius"]?.toIntOrNull() ?: 60
        )
        
        return GaugeChart(
            type = type,
            title = config["title"] ?: "",
            gauges = gauges,
            display = display
        )
    }
    
    private fun parseExtra(extra: String): Map<String, String> {
        return extra.split(",")
            .mapNotNull { 
                val kv = it.split("=")
                if (kv.size == 2) kv[0].trim() to kv[1].trim() else null
            }
            .toMap()
    }
}
