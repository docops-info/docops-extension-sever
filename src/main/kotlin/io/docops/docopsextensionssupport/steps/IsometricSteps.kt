package io.docops.docopsextensionssupport.steps

import io.docops.docopsextensionssupport.web.CsvResponse


data class IsometricSteps(
    val config: IsometricStepsConfig,
    val steps: List<IsometricStep>
) {
    fun toCSV(): CsvResponse {
        val headers = listOf("order", "title", "desc", "color")
        val rows = steps.map {
            listOf(it.order.toString(), it.title, it.desc, it.color ?: "")
        }
        return CsvResponse(headers, rows)
    }
}

data class IsometricStepsConfig(
    val infographic: String,
    val version: String = "1",
    val title: String,
    val subtitle: String? = null,
    val view: String = "isometric",
    val canvasWidth: Int = 1220,
    val canvasHeight: Int = 760,
    val theme: String = "classic",
    val palette: String = "vibrant",
    val startX: Int? = null,
    val startY: Int? = null,
    val dx: Int? = null,
    val dy: Int? = null,
    val labelOffsetX: Int = 130,
    val labelOffsetY: Int = 65,
    val maxDescLines: Int = 2
)

data class IsometricStep(
    val order: Int,
    val title: String,
    val desc: String = "",
    val color: String? = null,
    val icon: String? = null
)

