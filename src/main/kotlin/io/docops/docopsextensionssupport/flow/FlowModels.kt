package io.docops.docopsextensionssupport.flow

data class FlowDefinition(
    val title: String,
    val description: String = "",
    val theme: String = "modern",
    val steps: List<FlowStep>,
    val connections: List<FlowConnection>
)

data class FlowStep(
    val id: String,
    val name: String,
    val type: StepType,
    val color: String = "blue",
    val position: Int? = null,
    val options: List<DecisionOption> = emptyList()
)

data class FlowConnection(
    val from: String,
    val to: String,
    val label: String? = null,
    val type: ConnectionType = ConnectionType.SEQUENTIAL
)

data class DecisionOption(
    val label: String,
    val target: String
)

enum class StepType {
    START, COMMON, DECISION, BRANCH, CONVERGENCE, PARALLEL, FINAL
}

enum class ConnectionType {
    SEQUENTIAL, DIVERGENT, CONVERGENT, PARALLEL
}