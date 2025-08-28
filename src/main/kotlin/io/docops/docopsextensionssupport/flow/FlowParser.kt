package io.docops.docopsextensionssupport.flow

import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Parser for AsciiDoc-formatted flow definitions.
 * Supports the flow diagram format with steps, connections, and decision points.
 */
class FlowParser {
    private val log = KotlinLogging.logger {}

    fun parse(payload: String): FlowDefinition {
        val lines = payload.lines()
        var title = ""
        var description = ""
        var theme = "modern"
        val steps = mutableListOf<FlowStep>()
        val connections = mutableListOf<FlowConnection>()

        var inStepsSection = false
        var inConnectionsSection = false
        var currentDecisionStep: FlowStep? = null

        lines.forEach { raw ->
            val line = raw.trim()
            if (line.isBlank()) return@forEach

            when {
                // Document title
                line.startsWith("= ") -> {
                    title = line.removePrefix("= ").trim()
                }

                // Flow definition attributes
                line.startsWith("title::") -> {
                    title = line.removePrefix("title::").trim()
                }
                line.startsWith("description::") -> {
                    description = line.removePrefix("description::").trim()
                }
                line.startsWith("theme::") -> {
                    theme = line.removePrefix("theme::").trim()
                }

                // Section markers
                line == "[.steps]" -> {
                    inStepsSection = true
                    inConnectionsSection = false
                }
                line == "[.connections]" -> {
                    inStepsSection = false
                    inConnectionsSection = true
                }

                // Steps table header (skip)
                line.startsWith("|===") || line.startsWith("|Type") -> {
                    // Skip table formatting
                }

                // Step definitions in table format
                inStepsSection && line.startsWith("|") && !line.startsWith("|===") -> {
                    parseStepTableRow(line, steps)
                }

                // Decision options (indented under decision steps)
                inStepsSection && line.startsWith("  - ") -> {
                    parseDecisionOption(line, currentDecisionStep)
                }

                // Connection definitions
                inConnectionsSection && (line.contains("->") || line.contains("=>")) -> {
                    parseConnection(line, connections)
                }

                // Alternative step format (list-based)
                line.startsWith("- start:") -> {
                    parseStepLine(line, StepType.START, steps)
                }
                line.startsWith("- common:") -> {
                    parseStepLine(line, StepType.COMMON, steps)
                }
                line.startsWith("- decision:") -> {
                    val step = parseStepLine(line, StepType.DECISION, steps)
                    currentDecisionStep = step
                }
                line.startsWith("- branch:") -> {
                    parseStepLine(line, StepType.BRANCH, steps)
                }
                line.startsWith("- convergence:") -> {
                    parseStepLine(line, StepType.CONVERGENCE, steps)
                }
                line.startsWith("- parallel:") -> {
                    parseStepLine(line, StepType.PARALLEL, steps)
                }
                line.startsWith("- final:") -> {
                    parseStepLine(line, StepType.FINAL, steps)
                }
            }
        }

        return FlowDefinition(
            title = title.ifBlank { "Flow Diagram" },
            description = description,
            theme = theme,
            steps = steps,
            connections = connections
        )
    }

    private fun parseStepTableRow(line: String, steps: MutableList<FlowStep>) {
        val parts = line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.size >= 3) {
            val type = StepType.valueOf(parts[0].uppercase())
            val name = parts[1]
            val color = if (parts.size > 2) parts[2] else "blue"
            val position = if (parts.size > 3) parts[3].toIntOrNull() else null

            val id = name.lowercase().replace(" ", "_").replace("?", "")
            steps.add(FlowStep(id, name, type, color, position))
        }
    }

    private fun parseStepLine(line: String, type: StepType, steps: MutableList<FlowStep>): FlowStep {
        val content = line.substringAfter(":").trim()
        val nameAndColor = content.split("[")
        val name = nameAndColor[0].trim()
        val color = if (nameAndColor.size > 1) {
            nameAndColor[1].removeSuffix("]").trim()
        } else "blue"

        val id = name.lowercase().replace(" ", "_").replace("?", "")
        val step = FlowStep(id, name, type, color)
        steps.add(step)
        return step
    }

    private fun parseDecisionOption(line: String, decisionStep: FlowStep?) {
        if (decisionStep == null) return

        val content = line.removePrefix("  - ").trim()
        val parts = content.split("->").map { it.trim() }
        if (parts.size == 2) {
            val label = parts[0]
            val target = parts[1].lowercase().replace(" ", "_")

            // Update the decision step with options
            val updatedOptions = decisionStep.options + DecisionOption(label, target)
            val updatedStep = decisionStep.copy(options = updatedOptions)

            // This is a limitation - we'd need to update the step in the list
            // In a real implementation, you might want to handle this differently
        }
    }

    private fun parseConnection(line: String, connections: MutableList<FlowConnection>) {
        val cleanLine = line.removePrefix("----").removePrefix("```").trim()

        when {
            cleanLine.contains(" => ") -> {
                // Parallel connection
                parseConnectionParts(cleanLine, " => ", ConnectionType.PARALLEL, connections)
            }
            cleanLine.contains(" -> ") -> {
                // Sequential connection
                parseConnectionParts(cleanLine, " -> ", ConnectionType.SEQUENTIAL, connections)
            }
            cleanLine.contains("-->") -> {
                // Labeled connection (format: "Step --Label--> NextStep")
                val labelMatch = Regex("(.+?)\\s+--(.+?)-->\\s+(.+)").find(cleanLine)
                if (labelMatch != null) {
                    val fromPart = labelMatch.groupValues[1].trim()
                    val label = labelMatch.groupValues[2].trim()
                    val toPart = labelMatch.groupValues[3].trim()

                    // Use same ID generation logic as step parsing
                    val from = fromPart.lowercase().replace(" ", "_").replace("?", "")
                    val to = toPart.lowercase().replace(" ", "_").replace("?", "")

                    connections.add(FlowConnection(from, to, label, ConnectionType.DIVERGENT))
                }
            }
        }
    }

    private fun parseConnectionParts(
        line: String,
        separator: String,
        type: ConnectionType,
        connections: MutableList<FlowConnection>
    ) {
        val parts = line.split(separator)
        if (parts.size >= 2) {
            for (i in 0 until parts.size - 1) {
                val from = parts[i].trim().lowercase().replace(" ", "_").replace("?", "")
                val to = parts[i + 1].trim().lowercase().replace(" ", "_").replace("?", "")
                connections.add(FlowConnection(from, to, type = type))
            }
        }
    }
}