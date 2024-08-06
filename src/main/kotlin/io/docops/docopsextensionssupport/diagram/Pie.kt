package io.docops.docopsextensionssupport.diagram

import kotlinx.serialization.Serializable

@Serializable
class Pie (val percent: Float, val label: String = "", val color: String = "#E14D2A")
@Serializable
data class Pies(val pies: MutableList<Pie>)