package io.docops.docopsextensionssupport.chart.gauge

import io.docops.docopsextensionssupport.support.VisualDisplay
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@Serializable
data class GaugeChart(
    val type: GaugeType,
    val title: String = "",
    val gauges: List<GaugeData>,
    val display: GaugeDisplay = GaugeDisplay()
)

@Serializable
enum class GaugeType {
    SEMI_CIRCLE,      // Classic speedometer
    FULL_CIRCLE,      // 360-degree radial
    LINEAR,           // Horizontal bullet graph
    SOLID_FILL,       // Filled donut
    MULTI_GAUGE,      // Grid of small gauges
    DIGITAL,          // Large number with minimal arc
    DASHBOARD         // Mixed types in grid
}

@Serializable
data class GaugeData(
    val label: String,
    val value: Double,
    val min: Double = 0.0,
    val max: Double = 100.0,
    val unit: String = "%",
    val color: String? = null,  // Auto-assign if null
    val target: Double? = null,  // For LINEAR type
    val statusText: String? = null,  // For DIGITAL type
    val type: GaugeType? = null  // Override for DASHBOARD
)

@Serializable
data class GaugeDisplay @OptIn(ExperimentalUuidApi::class) constructor(
    override var useDark: Boolean = false,
    override val visualVersion: Int = 1,
    val scale: Float = 1.0f,
    val showLegend: Boolean = false,
    val showRanges: Boolean = true,  // Show color zones
    val showTarget: Boolean = false,  // For LINEAR
    val showLabel: Boolean = true,
    val showArc: Boolean = true,     // For DIGITAL
    val showStatus: Boolean = false,  // For DIGITAL
    val animateArc: Boolean = true,
    val columns: Int = 3,            // For MULTI_GAUGE
    val layout: String = "1x1",      // For DASHBOARD (e.g., "2x3")
    val innerRadius: Int = 60,       // For SOLID_FILL
    val theme: String = "classic",
    val id: String = "gauge_${Uuid.random().toHexString()}"
) : VisualDisplay

// Color range thresholds (traffic light)
@Serializable
data class GaugeRanges(
    // Normal range (Green)
    val normalStart: Double = 0.0,
    val normalEnd: Double = 60.0,

    // Caution range (Yellow)
    val cautionStart: Double = 60.0,
    val cautionEnd: Double = 85.0,

    // Critical range (Red)
    val criticalStart: Double = 85.0,
    val criticalEnd: Double = 100.0
) {
    fun getColorForValue(value: Double): String = when {
        value <= normalEnd -> "#10B981"           // Green
        value <= cautionEnd -> "#F59E0B"          // Yellow/Orange
        value <= criticalEnd -> "#EF4444"         // Red
        else -> "#94a3b8"                         // Gray
    }

    fun getZoneName(value: Double): String = when {
        value <= normalEnd -> "NORMAL"
        value <= cautionEnd -> "CAUTION"
        else -> "CRITICAL"
    }
}





