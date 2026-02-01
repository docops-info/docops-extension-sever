package io.docops.docopsextensionssupport.chart.gauge

import io.docops.docopsextensionssupport.svgsupport.formatDecimal
import io.docops.docopsextensionssupport.web.CsvResponse
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi

/**
 * Converts a GaugeChart to a CSV response for data export.
 * 
 * The CSV format includes comprehensive gauge data including metrics,
 * ranges, targets, and status information suitable for analysis.
 */
fun GaugeChart.toCsvResponse(): CsvResponse {
    val headers = buildHeaders()
    val csvData = gauges.map { gauge -> buildRow(gauge) }
    
    return CsvResponse(headers, csvData)
}

/**
 * Builds the CSV header row based on gauge type.
 */
private fun GaugeChart.buildHeaders(): List<String> {
    val baseHeaders = mutableListOf(
        "Label",
        "Value",
        "Min",
        "Max",
        "Unit",
        "Percentage",
        "Color",
        "Zone"
    )
    
    // Add type-specific headers
    when (type) {
        GaugeType.LINEAR -> {
            baseHeaders.add("Target")
            baseHeaders.add("Gap to Target")
        }
        GaugeType.DIGITAL -> {
            baseHeaders.add("Status Text")
        }
        GaugeType.DASHBOARD -> {
            baseHeaders.add(0, "Type")  // Prepend type column
            baseHeaders.add("Target")
            baseHeaders.add("Status Text")
        }
        else -> {
            // No additional headers for other types
        }
    }
    
    // Add metadata headers
    baseHeaders.addAll(listOf(
        "Chart Type",
        "Chart Title",
        "Show Ranges",
        "Theme Version"
    ))
    
    return baseHeaders
}

/**
 * Builds a CSV data row for a single gauge.
 */
private fun GaugeChart.buildRow(gauge: GaugeData): List<String> {
    val ranges = GaugeRanges()
    val percentage = ((gauge.value - gauge.min) / (gauge.max - gauge.min)) * 100
    val zone = ranges.getZoneName(gauge.value)
    
    val baseData = mutableListOf(
        gauge.label,
        formatCsvValue(gauge.value),
        formatCsvValue(gauge.min),
        formatCsvValue(gauge.max),
        gauge.unit,
        formatCsvValue(percentage),
        gauge.color ?: ranges.getColorForValue(gauge.value),
        zone
    )
    
    // Add type-specific data
    when (type) {
        GaugeType.LINEAR -> {
            baseData.add(gauge.target?.let { formatCsvValue(it) } ?: "")
            baseData.add(gauge.target?.let { formatCsvValue(gauge.value - it) } ?: "")
        }
        GaugeType.DIGITAL -> {
            baseData.add(gauge.statusText ?: "")
        }
        GaugeType.DASHBOARD -> {
            baseData.add(0, (gauge.type ?: type).name)  // Prepend type
            baseData.add(gauge.target?.let { formatCsvValue(it) } ?: "")
            baseData.add(gauge.statusText ?: "")
        }
        else -> {
            // No additional data for other types
        }
    }
    
    // Add metadata
    baseData.addAll(listOf(
        type.name,
        title,
        display.showRanges.toString(),
        display.visualVersion.toString()
    ))
    
    return baseData
}

/**
 * Formats a numeric value for CSV export.
 * Uses the multiplatform-compatible formatDecimal from TextUtils.
 */
private fun formatCsvValue(value: Double): String {
    return formatDecimal(value, 2)
}

/**
 * Converts a GaugeChart to a detailed CSV with additional statistical information.
 * Includes summary statistics and range information.
 */
fun GaugeChart.toDetailedCsvResponse(): CsvResponse {
    val headers = listOf(
        "Metric",
        "Value"
    )
    
    val csvData = mutableListOf<List<String>>()
    
    // Chart metadata
    csvData.add(listOf("Chart Title", title))
    csvData.add(listOf("Chart Type", type.name))
    csvData.add(listOf("Number of Gauges", gauges.size.toString()))
    csvData.add(listOf("Theme Version", display.visualVersion.toString()))
    csvData.add(listOf("Dark Mode", display.useDark.toString()))
    csvData.add(listOf("Show Ranges", display.showRanges.toString()))
    csvData.add(listOf("Timestamp", getCurrentTimestamp()))
    
    csvData.add(listOf("", "")) // Separator
    
    // Range thresholds
    val ranges = GaugeRanges()
    csvData.add(listOf("Normal Range Start", formatCsvValue(ranges.normalStart)))
    csvData.add(listOf("Normal Range End", formatCsvValue(ranges.normalEnd)))
    csvData.add(listOf("Caution Range Start", formatCsvValue(ranges.cautionStart)))
    csvData.add(listOf("Caution Range End", formatCsvValue(ranges.cautionEnd)))
    csvData.add(listOf("Critical Range Start", formatCsvValue(ranges.criticalStart)))
    csvData.add(listOf("Critical Range End", formatCsvValue(ranges.criticalEnd)))
    
    csvData.add(listOf("", "")) // Separator
    
    // Statistics per gauge
    gauges.forEachIndexed { index, gauge ->
        val percentage = ((gauge.value - gauge.min) / (gauge.max - gauge.min)) * 100
        val zone = ranges.getZoneName(gauge.value)
        
        csvData.add(listOf("=== Gauge ${index + 1}: ${gauge.label} ===", ""))
        csvData.add(listOf("  Label", gauge.label))
        csvData.add(listOf("  Value", formatCsvValue(gauge.value)))
        csvData.add(listOf("  Min", formatCsvValue(gauge.min)))
        csvData.add(listOf("  Max", formatCsvValue(gauge.max)))
        csvData.add(listOf("  Unit", gauge.unit))
        csvData.add(listOf("  Percentage", "${formatCsvValue(percentage)}%"))
        csvData.add(listOf("  Zone", zone))
        csvData.add(listOf("  Color", gauge.color ?: ranges.getColorForValue(gauge.value)))
        
        gauge.target?.let {
            csvData.add(listOf("  Target", formatCsvValue(it)))
            csvData.add(listOf("  Gap to Target", formatCsvValue(gauge.value - it)))
            val targetPercentage = if (it > 0) (gauge.value / it) * 100 else 0.0
            csvData.add(listOf("  Target Achievement", "${formatCsvValue(targetPercentage)}%"))
        }
        
        gauge.statusText?.let {
            csvData.add(listOf("  Status", it))
        }
        
        csvData.add(listOf("", "")) // Separator
    }
    
    // Overall statistics
    if (gauges.isNotEmpty()) {
        csvData.add(listOf("=== Overall Statistics ===", ""))
        
        val avgValue = gauges.map { it.value }.average()
        val maxValue = gauges.maxOf { it.value }
        val minValue = gauges.minOf { it.value }
        val normalCount = gauges.count { ranges.isNormal(it.value) }
        val cautionCount = gauges.count { ranges.isCaution(it.value) }
        val criticalCount = gauges.count { ranges.isCritical(it.value) }
        
        csvData.add(listOf("Average Value", formatCsvValue(avgValue)))
        csvData.add(listOf("Maximum Value", formatCsvValue(maxValue)))
        csvData.add(listOf("Minimum Value", formatCsvValue(minValue)))
        csvData.add(listOf("Normal Zone Count", normalCount.toString()))
        csvData.add(listOf("Caution Zone Count", cautionCount.toString()))
        csvData.add(listOf("Critical Zone Count", criticalCount.toString()))
        
        if (type == GaugeType.MULTI_GAUGE || type == GaugeType.DASHBOARD) {
            val normalPercent = (normalCount.toDouble() / gauges.size) * 100
            val cautionPercent = (cautionCount.toDouble() / gauges.size) * 100
            val criticalPercent = (criticalCount.toDouble() / gauges.size) * 100
            
            csvData.add(listOf("Normal Zone %", "${formatCsvValue(normalPercent)}%"))
            csvData.add(listOf("Caution Zone %", "${formatCsvValue(cautionPercent)}%"))
            csvData.add(listOf("Critical Zone %", "${formatCsvValue(criticalPercent)}%"))
        }
    }
    
    return CsvResponse(headers, csvData)
}

/**
 * Converts a GaugeChart to a pivot-style CSV suitable for time-series analysis.
 * Each gauge becomes a column, suitable for tracking metrics over time.
 */
fun GaugeChart.toPivotCsvResponse(): CsvResponse {
    val headers = mutableListOf("Timestamp", "Chart Type")
    headers.addAll(gauges.map { "${it.label} (${it.unit})" })
    headers.add("Average")
    
    val timestamp = getCurrentTimestamp()
    val values = mutableListOf(timestamp, type.name)
    values.addAll(gauges.map { formatCsvValue(it.value) })
    
    // Calculate average
    val average = if (gauges.isNotEmpty()) {
        gauges.map { it.value }.average()
    } else {
        0.0
    }
    values.add(formatCsvValue(average))
    
    return CsvResponse(headers, listOf(values))
}

/**
 * Converts a GaugeChart to a zone-distribution CSV.
 * Useful for analyzing how many gauges fall into each performance zone.
 */
fun GaugeChart.toZoneDistributionCsvResponse(): CsvResponse {
    val ranges = GaugeRanges()
    
    val headers = listOf(
        "Zone",
        "Start Range",
        "End Range",
        "Gauge Count",
        "Percentage",
        "Gauge Labels"
    )
    
    val normalGauges = gauges.filter { ranges.isNormal(it.value) }
    val cautionGauges = gauges.filter { ranges.isCaution(it.value) }
    val criticalGauges = gauges.filter { ranges.isCritical(it.value) }
    
    val totalCount = gauges.size.toDouble()
    
    val csvData = listOf(
        listOf(
            "NORMAL",
            formatCsvValue(ranges.normalStart),
            formatCsvValue(ranges.normalEnd),
            normalGauges.size.toString(),
            "${formatCsvValue((normalGauges.size / totalCount) * 100)}%",
            normalGauges.joinToString("; ") { it.label }
        ),
        listOf(
            "CAUTION",
            formatCsvValue(ranges.cautionStart),
            formatCsvValue(ranges.cautionEnd),
            cautionGauges.size.toString(),
            "${formatCsvValue((cautionGauges.size / totalCount) * 100)}%",
            cautionGauges.joinToString("; ") { it.label }
        ),
        listOf(
            "CRITICAL",
            formatCsvValue(ranges.criticalStart),
            formatCsvValue(ranges.criticalEnd),
            criticalGauges.size.toString(),
            "${formatCsvValue((criticalGauges.size / totalCount) * 100)}%",
            criticalGauges.joinToString("; ") { it.label }
        )
    )
    
    return CsvResponse(headers, csvData)
}

/**
 * Helper extension functions for GaugeRanges to check zone membership.
 */
private fun GaugeRanges.isNormal(value: Double): Boolean = value <= normalEnd
private fun GaugeRanges.isCaution(value: Double): Boolean = value > normalEnd && value <= cautionEnd
private fun GaugeRanges.isCritical(value: Double): Boolean = value > cautionEnd

/**
 * Multiplatform-compatible timestamp generation.
 * Uses Kotlin UUID for uniqueness and current time approximation.
 */
@OptIn(ExperimentalUuidApi::class)
private fun getCurrentTimestamp(): String {
    return Clock.System.now().toString()
}
