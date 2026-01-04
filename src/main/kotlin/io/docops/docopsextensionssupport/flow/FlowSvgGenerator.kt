package io.docops.docopsextensionssupport.flow

import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Generates modern SVG diagrams from FlowDefinition objects.
 * Uses the established styling patterns from the existing car selection flow.
 */
class FlowSvgGenerator(val useDark: Boolean = false) {
    private val log = KotlinLogging.logger {}

    fun generate(flowDefinition: FlowDefinition): String {
        val id = "flow_${System.currentTimeMillis()}"
        val positions = calculatePositions(flowDefinition.steps, flowDefinition.connections)
        val svgWidth = positions.values.maxOfOrNull { it.x + 150 } ?: 1200

        // Calculate dynamic height based on step positions
        val minY = positions.values.minOfOrNull { it.y } ?: 150
        val maxY = positions.values.maxOfOrNull { it.y } ?: 450
        val svgHeight = maxOf(600, maxY - minY + 300) // Ensure minimum height with padding

        return buildString {
            appendLine("""<svg width="$svgWidth" height="$svgHeight" viewBox="0 0 $svgWidth $svgHeight" xmlns="http://www.w3.org/2000/svg">""")
            appendLine(generateDefs(id))
            appendLine(generateBackground(svgWidth, svgHeight, id))
            appendLine(generateTitle(flowDefinition.title, svgWidth, id))

            // Generate steps
            flowDefinition.steps.forEachIndexed { index, step ->
                val pos = positions[step.id] ?: Position(100, 280)
                appendLine("""<g class="anim-node" style="animation-delay: ${0.1 + index * 0.05}s">""")
                appendLine(generateStep(step, pos, id))
                appendLine("</g>")
            }

            // Generate connections
            flowDefinition.connections.forEach { connection ->
                val fromPos = positions[connection.from]
                val toPos = positions[connection.to]
                if (fromPos != null && toPos != null) {
                    appendLine(generateConnection(connection, fromPos, toPos, flowDefinition.steps, id =  id))
                }
            }

            appendLine(generateLegend(id))
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


    private fun generateDefs(id: String): String = """
            <defs>
                <linearGradient id="glassBorder_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:rgba(255,255,255,0.5);stop-opacity:1" />
                    <stop offset="100%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
                </linearGradient>
                <linearGradient id="glassOverlay_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:rgba(255,255,255,0.4);stop-opacity:1" />
                    <stop offset="100%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1" />
                </linearGradient>
                <filter id="glassBlur_$id">
                    <feGaussianBlur in="SourceGraphic" stdDeviation="5" />
                </filter>
                <filter id="nodeShadow_$id" x="-20%" y="-20%" width="140%" height="140%">
                    <feDropShadow dx="0" dy="4" stdDeviation="6" flood-color="#000" flood-opacity="0.15"/>
                </filter>
                <marker id="arrowhead" markerWidth="10" markerHeight="10" refX="9" refY="5" orient="auto">
                    <path d="M0,0 L10,5 L0,10 L2,5 Z" fill="#475569"/>
                </marker>
                <style>
                    @import url('https://fonts.googleapis.com/css2?family=Syne:wght@800&amp;family=JetBrains+Mono:wght@500;700&amp;display=swap');
                    .label { font-family: 'JetBrains Mono', monospace; fill: #1e293b; font-size: 11px; font-weight: 700; text-transform: uppercase; }
                    .title { font-family: 'Syne', sans-serif; fill: #0f172a; font-size: 28px; font-weight: 800; text-transform: uppercase; letter-spacing: -1px; }
                    .connector { stroke: #94a3b8; stroke-width: 1.5; fill: none; }
                    @keyframes slideUpFlow { 
                        from { opacity: 0; transform: translateY(20px); } 
                        to { opacity: 1; transform: translateY(0); } 
                    }
                    .anim-node { animation: slideUpFlow 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards; opacity: 0; }
                </style>
            </defs>
        """.trimIndent()

    private fun generateBackground(width: Int, height: Int, id: String): String =
        """<rect width="$width" height="$height" fill="url(#bgGrad_$id)"/>"""

    private fun generateTitle(title: String, width: Int, id: String): String =
        """<text x="50" y="50" class="title">${title.escapeXml()}</text>"""

    private fun generateStep(step: FlowStep, position: Position, id: String): String {
        val color = getColorForStep(step.color)
        val width = if (step.type == StepType.PARALLEL) 85 else 110
        val height = 55

        return """
        <g transform="translate(${position.x}, ${position.y})">
            <rect width="$width" height="$height" rx="10" fill="white" fill-opacity="0.6" filter="url(#nodeShadow_$id)"/>
            <rect width="$width" height="$height" rx="10" fill="url(#glassOverlay_$id)" stroke="url(#glassBorder_$id)" stroke-width="1"/>
            <rect x="0" y="0" width="4" height="$height" rx="2" fill="$color"/>
            <text x="${width / 2 + 4}" y="${height / 2 + 4}" text-anchor="middle" class="label" style="fill: #334155">${step.name.escapeXml()}</text>
        </g>
    """.trimIndent()
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

    private fun generateDecisionStep(step: FlowStep, position: Position, id: String): String {
        val centerX = position.x + 50
        val centerY = position.y + 30

        return buildString {
            appendLine("""<g filter="url(#nodeShadow_$id)">""")
            appendLine("""<polygon points="${centerX - 50},${centerY} ${centerX},${centerY - 35} ${centerX + 50},${centerY} ${centerX},${centerY + 35}" fill="white" fill-opacity="0.7" stroke="url(#glassBorder_$id)" stroke-width="1"/>""")
            appendLine("""<rect x="${centerX - 50}" y="$centerY" width="4" height="2" fill="#fb8c00"/>""") // Accent dot
            appendLine("""</g>""")
            appendLine("""<text x="$centerX" y="${centerY + 6}" text-anchor="middle" class="label" style="font-size:10px; fill: #fb8c00">${step.name.escapeXml()}</text>""")
        }
    }

    private fun generateConvergenceStep(step: FlowStep, position: Position, id: String): String {
        val centerX = position.x + 20
        val centerY = position.y + 30

        return buildString {
            appendLine("""<g filter="url(#nodeShadow_$id)">""")
            appendLine("""<circle cx="$centerX" cy="$centerY" r="20" fill="white" fill-opacity="0.8" stroke="url(#glassBorder_$id)" stroke-width="1.5"/>""")
            appendLine("""</g>""")
            appendLine("""<text x="$centerX" y="${centerY + 5}" text-anchor="middle" class="label" style="font-size:9px; fill: #64748b">${step.name.escapeXml()}</text>""")
        }
    }

    private fun generateConnection(connection: FlowConnection, fromPos: Position, toPos: Position, steps: List<FlowStep>, id: String): String {
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
            val midX = (startX + endX) / 2
            """<path d="M$startX,$startY C$midX,$startY $midX,$endY $endX,$endY" class="connector" marker-end="url(#arrowhead_$id)"/>"""
        } else {
            when (connection.type) {
                ConnectionType.PARALLEL ->
                    """<path d="M$startX,$startY C${startX + 30},$startY ${endX - 25},$endY $endX,$endY" class="connector" marker-end="url(#arrowhead_$id)"/>"""
                ConnectionType.DIVERGENT, ConnectionType.CONVERGENT ->
                    """<path d="M$startX,$startY C${(startX + endX) / 2},${(startY + endY) / 2} ${endX - 25},$endY $endX,$endY" class="connector" marker-end="url(#arrowhead_$id)"/>"""
                else ->
                    """<path d="M$startX,$startY C${startX + 15},$startY ${endX - 15},$endY $endX,$endY" class="connector" marker-end="url(#arrowhead_$id)"/>"""
            }
        }
    }

    private fun generateLegend(id: String): String = """
    <g transform="translate(50, 450)">
        <rect width="220" height="120" fill="white" fill-opacity="0.8" stroke="url(#glassBorder_$id)" stroke-width="1" rx="12" filter="url(#nodeShadow_$id)"/>
        <text x="15" y="25" class="legendTitle">Legend</text>
        <rect x="15" y="40" width="12" height="12" rx="3" fill="#2e9c5c"/>
        <text x="35" y="50" class="legendText">Start/End Process</text>
        <rect x="15" y="60" width="12" height="12" rx="3" fill="#1e7ae5"/>
        <text x="35" y="70" class="legendText">Common Steps</text>
        <rect x="15" y="80" width="12" height="12" rx="3" fill="#fb8c00"/>
        <text x="35" y="90" class="legendText">Decision Point</text>
    </g>
""".trimIndent()

    private fun getColorForStep(color: String): String = when (color.lowercase()) {
        "green" -> "#10b981"
        "blue" -> "#3b82f6"
        "purple" -> "#8b5cf6"
        "pink" -> "#ec4899"
        "slate" -> "#64748b"
        "orange" -> "#f59e0b"
        else -> "#3b82f6"
    }

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