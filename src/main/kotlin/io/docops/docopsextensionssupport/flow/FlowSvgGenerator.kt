package io.docops.docopsextensionssupport.flow

import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Generates modern SVG diagrams from FlowDefinition objects.
 * Uses the established styling patterns from the existing car selection flow.
 */
class FlowSvgGenerator {
    private val log = KotlinLogging.logger {}

    fun generate(flowDefinition: FlowDefinition): String {
        val positions = calculatePositions(flowDefinition.steps, flowDefinition.connections)
        val svgWidth = positions.values.maxOfOrNull { it.x + 150 } ?: 1200

        // Calculate dynamic height based on step positions
        val minY = positions.values.minOfOrNull { it.y } ?: 150
        val maxY = positions.values.maxOfOrNull { it.y } ?: 450
        val svgHeight = maxOf(600, maxY - minY + 300) // Ensure minimum height with padding

        return buildString {
            appendLine("""<svg width="$svgWidth" height="$svgHeight" viewBox="0 0 $svgWidth $svgHeight" xmlns="http://www.w3.org/2000/svg">""")
            appendLine(generateDefs())
            appendLine(generateBackground(svgWidth, svgHeight))
            appendLine(generateTitle(flowDefinition.title, svgWidth))

            // Generate steps
            flowDefinition.steps.forEach { step ->
                val pos = positions[step.id] ?: Position(100, 280)
                appendLine(generateStep(step, pos))
            }

            // Generate connections
            flowDefinition.connections.forEach { connection ->
                val fromPos = positions[connection.from]
                val toPos = positions[connection.to]
                if (fromPos != null && toPos != null) {
                    appendLine(generateConnection(connection, fromPos, toPos, flowDefinition.steps))
                }
            }

            appendLine(generateLegend())
            appendLine("</svg>")
        }
    }

    private fun calculatePositions(steps: List<FlowStep>, connections: List<FlowConnection>): Map<String, Position> {
        val positions = mutableMapOf<String, Position>()
        val baseY = 280
        var currentX = 100

        // Build a dependency graph from connections
        val stepDependencies = mutableMapOf<String, MutableSet<String>>()
        val stepChildren = mutableMapOf<String, MutableSet<String>>()

        connections.forEach { connection ->
            stepChildren.getOrPut(connection.from) { mutableSetOf() }.add(connection.to)
            stepDependencies.getOrPut(connection.to) { mutableSetOf() }.add(connection.from)
        }

        // Find steps that come after parallel steps (sequential sub-flows)
        val parallelSteps = steps.filter { it.type == StepType.PARALLEL }
        val sequentialAfterParallel = mutableMapOf<String, MutableList<String>>()

        parallelSteps.forEach { parallelStep ->
            stepChildren[parallelStep.id]?.forEach { childId ->
                val childStep = steps.find { it.id == childId }
                if (childStep != null && childStep.type in listOf(StepType.BRANCH, StepType.COMMON)) {
                    // This is a sequential step after a parallel step
                    val sequence = mutableListOf<String>()
                    var currentStepId: String? = childId
                    while (currentStepId != null) {
                        sequence.add(currentStepId)
                        val nextChildren = stepChildren[currentStepId]
                        currentStepId = if (nextChildren?.size == 1) {
                            val nextStep = steps.find { it.id == nextChildren.first() }
                            if (nextStep?.type in listOf(StepType.BRANCH, StepType.COMMON)) {
                                nextChildren.first()
                            } else null
                        } else null
                    }
                    if (sequence.isNotEmpty()) {
                        sequentialAfterParallel[parallelStep.id] = sequence
                    }
                }
            }
        }

        // Find steps that come after convergence
        val convergenceSteps = steps.filter { it.type == StepType.CONVERGENCE }
        val postConvergenceSteps = mutableSetOf<String>()

        convergenceSteps.forEach { convergence ->
            stepChildren[convergence.id]?.forEach { childId ->
                val visited = mutableSetOf<String>()
                fun collectPostConvergence(stepId: String) {
                    if (stepId in visited) return
                    visited.add(stepId)
                    val step = steps.find { it.id == stepId }
                    if (step != null && step.type != StepType.PARALLEL) {
                        postConvergenceSteps.add(stepId)
                    }
                    stepChildren[stepId]?.forEach { collectPostConvergence(it) }
                }
                collectPostConvergence(childId)
            }
        }

        // Identify steps that are in sequential sub-flows
        val stepsInSequentialFlows = sequentialAfterParallel.values.flatten().toSet()

        // Group steps by their logical positioning needs
        val startSteps = steps.filter { it.type == StepType.START }
        val preDecisionCommonSteps = steps.filter {
            it.type == StepType.COMMON && it.id !in postConvergenceSteps && it.id !in stepsInSequentialFlows
        }
        val decisionSteps = steps.filter { it.type == StepType.DECISION }
        val branchSteps = steps.filter {
            it.type == StepType.BRANCH && it.id !in stepsInSequentialFlows
        }
        val postConvergenceCommonSteps = steps.filter {
            it.type == StepType.COMMON && it.id in postConvergenceSteps
        }
        val finalSteps = steps.filter { it.type == StepType.FINAL }

        // Position start and pre-decision common steps sequentially
        (startSteps + preDecisionCommonSteps).forEach { step ->
            positions[step.id] = Position(currentX, baseY)
            currentX += 130
        }

        // Position decision steps
        decisionSteps.forEach { step ->
            positions[step.id] = Position(currentX, baseY)
            currentX += 150
        }

        // Position branch steps - support for multiple branches
        if (branchSteps.isNotEmpty()) {
            val branchCount = branchSteps.size
            when {
                branchCount <= 2 -> {
                    branchSteps.forEachIndexed { index, step ->
                        val yOffset = if (index % 2 == 0) -130 else 130
                        positions[step.id] = Position(currentX, baseY + yOffset)
                    }
                }
                branchCount == 3 -> {
                    branchSteps.forEachIndexed { index, step ->
                        val yOffset = when (index) {
                            0 -> -130  // Top
                            1 -> 0     // Middle (same as baseY)
                            2 -> 130   // Bottom
                            else -> 0
                        }
                        positions[step.id] = Position(currentX, baseY + yOffset)
                    }
                }
                else -> {
                    val spacing = 240 / (branchCount - 1)
                    val startY = baseY - 120

                    branchSteps.forEachIndexed { index, step ->
                        val yOffset = startY + (index * spacing) - baseY
                        positions[step.id] = Position(currentX, baseY + yOffset)
                    }
                }
            }
        }
        currentX += 130

        // Position parallel steps and their sequential sub-flows
        if (parallelSteps.isNotEmpty()) {
            val parallelCount = parallelSteps.size

            parallelSteps.forEachIndexed { index, step ->
                val yOffset = when {
                    parallelCount <= 2 -> if (index % 2 == 0) -75 else 95
                    parallelCount == 3 -> when (index) {
                        0 -> -90   // Top
                        1 -> 0     // Middle
                        2 -> 90    // Bottom
                        else -> 0
                    }
                    else -> {
                        val spacing = 200 / (parallelCount - 1)
                        val startY = baseY - 100
                        startY + (index * spacing) - baseY
                    }
                }
                positions[step.id] = Position(currentX, baseY + yOffset)

                // Position sequential steps after this parallel step
                sequentialAfterParallel[step.id]?.let { sequence ->
                    var subCurrentX = currentX + 100  // Start after the parallel step
                    sequence.forEach { seqStepId ->
                        positions[seqStepId] = Position(subCurrentX, baseY + yOffset)
                        subCurrentX += 130
                    }
                }
            }
        }
        currentX += 120

        // Add extra space for sequential sub-flows
        val maxSequentialLength = sequentialAfterParallel.values.maxOfOrNull { it.size } ?: 0
        if (maxSequentialLength > 0) {
            currentX += maxSequentialLength * 130
        }

        // Position convergence steps
        convergenceSteps.forEach { step ->
            positions[step.id] = Position(currentX, baseY)
            currentX += 100
        }

        // Position post-convergence common steps
        postConvergenceCommonSteps.forEach { step ->
            positions[step.id] = Position(currentX, baseY)
            currentX += 130
        }

        // Position final steps
        finalSteps.forEach { step ->
            positions[step.id] = Position(currentX, baseY)
            currentX += 130
        }

        return positions
    }


    private fun generateDefs(): String = """
        <defs>
            <!-- Background gradient -->
            <linearGradient id="bgGrad" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stop-color="#f6f7fb"/>
                <stop offset="100%" stop-color="#eef1f7"/>
            </linearGradient>
            
            <!-- Card gradients -->
            <linearGradient id="cardBlue" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#4da3ff"/>
                <stop offset="100%" stop-color="#1e7ae5"/>
            </linearGradient>
            <linearGradient id="cardGreen" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#5fd38a"/>
                <stop offset="100%" stop-color="#2e9c5c"/>
            </linearGradient>
            <linearGradient id="cardPurple" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#b07cf7"/>
                <stop offset="100%" stop-color="#7a49cf"/>
            </linearGradient>
            <linearGradient id="cardPink" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#ff72a8"/>
                <stop offset="100%" stop-color="#d33b78"/>
            </linearGradient>
            <linearGradient id="cardSlate" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#7f96a6"/>
                <stop offset="100%" stop-color="#5a6d7a"/>
            </linearGradient>
            <linearGradient id="cardOrange" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#ffb74d"/>
                <stop offset="100%" stop-color="#fb8c00"/>
            </linearGradient>
            
            <!-- Filters -->
            <filter id="softShadow" x="-20%" y="-20%" width="140%" height="140%">
                <feDropShadow dx="0" dy="2" stdDeviation="3" flood-color="#000" flood-opacity="0.2"/>
            </filter>
            
            <!-- Arrow marker - smaller size and adjusted position -->
            <marker id="arrowhead" markerWidth="8" markerHeight="8" refX="7" refY="4" orient="auto" markerUnits="strokeWidth">
                <path d="M0,0 L8,4 L0,8 Q2,4 0,0" fill="#2f3a4a"/>
            </marker>
            
            <!-- Styles -->
            <style><![CDATA[
                .label { font-family: Inter, Segoe UI, Roboto, Helvetica, Arial, sans-serif; fill: #ffffff; font-size: 13px; font-weight: 600; }
                .title { font-family: Inter, Segoe UI, Roboto, Helvetica, Arial, sans-serif; fill: #2f3a4a; font-size: 22px; font-weight: 800; letter-spacing: 0.2px; }
                .legendTitle { font-family: Inter, Segoe UI, Roboto, Helvetica, Arial, sans-serif; fill: #2f3a4a; font-size: 14px; font-weight: 700; }
                .legendText { font-family: Inter, Segoe UI, Roboto, Helvetica, Arial, sans-serif; fill: #475569; font-size: 12px; }
                .connector { stroke: #2f3a4a; stroke-width: 2.2; fill: none; opacity: 0.9; }
            ]]></style>
        </defs>
    """.trimIndent()

    private fun generateBackground(width: Int, height: Int): String =
        """<rect width="$width" height="$height" fill="url(#bgGrad)"/>"""

    private fun generateTitle(title: String, width: Int): String =
        """<text x="${width/2}" y="36" text-anchor="middle" class="title">$title</text>"""

    private fun generateStep(step: FlowStep, position: Position): String {
        return when (step.type) {
            StepType.DECISION -> generateDecisionStep(step, position)
            StepType.CONVERGENCE -> generateConvergenceStep(step, position)
            else -> generateRectangularStep(step, position)
        }
    }

    private fun generateRectangularStep(step: FlowStep, position: Position): String {
        val colorGradient = getColorGradient(step.color)
        val width = if (step.type == StepType.PARALLEL) 80 else 100
        val height = if (step.type == StepType.PARALLEL) 50 else 60
        val lines = step.name.split(" ")

        return buildString {
            appendLine("""<rect x="${position.x}" y="${position.y}" width="$width" height="$height" rx="14" fill="url(#$colorGradient)" filter="url(#softShadow)"/>""")

            if (lines.size == 1) {
                appendLine("""<text x="${position.x + width/2}" y="${position.y + height/2 + 5}" text-anchor="middle" class="label">${lines[0]}</text>""")
            } else {
                lines.forEachIndexed { index, line ->
                    val y = position.y + height/2 - 8 + (index * 18)
                    val weight = if (index == lines.size - 1) "font-weight:700; opacity:0.95" else ""
                    appendLine("""<text x="${position.x + width/2}" y="$y" text-anchor="middle" class="label" style="$weight">$line</text>""")
                }
            }
        }
    }

    private fun generateDecisionStep(step: FlowStep, position: Position): String {
        val centerX = position.x + 50
        val centerY = position.y + 30

        return buildString {
            appendLine("""<g filter="url(#softShadow)">""")
            appendLine("""<polygon points="${centerX-50},${centerY} ${centerX},${centerY-35} ${centerX+50},${centerY} ${centerX},${centerY+35}" fill="url(#cardOrange)" stroke="#e07b00" stroke-width="0.8"/>""")
            appendLine("""</g>""")
            appendLine("""<text x="$centerX" y="${centerY + 6}" text-anchor="middle" class="label" style="font-size:12px">${step.name}</text>""")
        }
    }

    private fun generateConvergenceStep(step: FlowStep, position: Position): String {
        val centerX = position.x + 20
        val centerY = position.y + 30

        return buildString {
            appendLine("""<g filter="url(#softShadow)">""")
            appendLine("""<circle cx="$centerX" cy="$centerY" r="20" fill="#8b5e3c"/>""")
            appendLine("""</g>""")
            appendLine("""<text x="$centerX" y="${centerY + 6}" text-anchor="middle" class="label" style="font-size:11px">${step.name}</text>""")
        }
    }

    private fun generateConnection(connection: FlowConnection, fromPos: Position, toPos: Position, steps: List<FlowStep>): String {
        // Find the step types to calculate proper connection points
        val fromStep = steps.find { it.id == connection.from }
        val toStep = steps.find { it.id == connection.to }

        // Calculate start position based on the from step type
        val (startX, startY) = when (fromStep?.type) {
            StepType.DECISION -> {
                // Decision diamond: start from right edge of diamond
                val centerX = fromPos.x + 50
                val centerY = fromPos.y + 30
                Pair(centerX + 50, centerY)
            }
            StepType.CONVERGENCE -> {
                // Convergence circle: start from right edge of circle
                val centerX = fromPos.x + 20
                val centerY = fromPos.y + 30
                Pair(centerX + 20, centerY)
            }
            StepType.PARALLEL -> {
                // Parallel step: smaller width (80px)
                Pair(fromPos.x + 80, fromPos.y + 25)
            }
            else -> {
                // Regular rectangular steps: width 100px
                Pair(fromPos.x + 100, fromPos.y + 30)
            }
        }

        // Calculate end position based on the to step type - adjusted to prevent overlap
        // Calculate end position based on the to step type - adjusted to prevent overlap
        val (endX, endY) = when (toStep?.type) {
            StepType.DECISION -> {
                // Decision diamond: end at left edge of diamond, shifted left slightly
                val centerX = toPos.x + 50
                val centerY = toPos.y + 30
                Pair(centerX - 51, centerY)
            }
            StepType.CONVERGENCE -> {
                // Convergence circle: end at left edge of circle, shifted left slightly
                val centerX = toPos.x + 20
                val centerY = toPos.y + 30
                Pair(centerX - 21, centerY)
            }
            StepType.PARALLEL -> {
                // Parallel step: smaller width (80px), shifted left slightly
                Pair(toPos.x - 1, toPos.y + 25)
            }
            else -> {
                // Regular rectangular steps, shifted left slightly
                Pair(toPos.x - 1, toPos.y + 30)
            }
        }

        // Handle parallel connections from convergence point differently
        return if (connection.type == ConnectionType.PARALLEL && fromStep?.type == StepType.CONVERGENCE) {
            // For parallel connections from convergence, create curved paths
            val midX = (startX + endX) / 2
            """<path d="M$startX,$startY C$midX,$startY $midX,$endY $endX,$endY" class="connector" marker-end="url(#arrowhead)"/>"""
        } else {
            when (connection.type) {
                ConnectionType.PARALLEL ->
                    """<path d="M$startX,$startY C${startX+30},$startY ${endX-25},$endY $endX,$endY" class="connector" marker-end="url(#arrowhead)"/>"""
                ConnectionType.DIVERGENT, ConnectionType.CONVERGENT ->
                    """<path d="M$startX,$startY C${(startX+endX)/2},${(startY+endY)/2} ${endX-25},$endY $endX,$endY" class="connector" marker-end="url(#arrowhead)"/>"""
                else ->
                    """<path d="M$startX,$startY C${startX+15},$startY ${endX-15},$endY $endX,$endY" class="connector" marker-end="url(#arrowhead)"/>"""
            }
        }
    }

    private fun generateLegend(): String = """
        <g filter="url(#softShadow)">
            <rect x="50" y="450" width="220" height="120" fill="#ffffff" stroke="#e5e7eb" stroke-width="1" rx="10"/>
        </g>
        <text x="60" y="472" class="legendTitle">Legend</text>
        <rect x="60" y="482" width="15" height="15" rx="3" fill="url(#cardGreen)"/>
        <text x="85" y="494" class="legendText">Start/End Process</text>
        <rect x="60" y="504" width="15" height="15" rx="3" fill="url(#cardBlue)"/>
        <text x="85" y="516" class="legendText">Common Steps</text>
        <polygon points="60,522 67.5,527 75,522 67.5,532" fill="url(#cardOrange)"/>
        <text x="85" y="530" class="legendText">Decision Point</text>
        <rect x="60" y="538" width="15" height="15" rx="3" fill="url(#cardSlate)"/>
        <text x="85" y="550" class="legendText">Parallel Process</text>
    """.trimIndent()

    private fun getColorGradient(color: String): String = when (color.lowercase()) {
        "green" -> "cardGreen"
        "blue" -> "cardBlue"
        "purple" -> "cardPurple"
        "pink" -> "cardPink"
        "slate" -> "cardSlate"
        "orange" -> "cardOrange"
        else -> "cardBlue"
    }

    private data class Position(val x: Int, val y: Int)
}