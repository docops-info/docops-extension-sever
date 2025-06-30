package io.docops.docopsextensionssupport.diagram

import org.slf4j.LoggerFactory
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Generates SVG connectors between diagram elements.
 */
class ConnectorMakerImproved(private val config: ConnectorConfig = ConnectorConfig()) {
    private val logger = LoggerFactory.getLogger(ConnectorMaker::class.java)

    /**
     * Configuration for connector appearance and behavior
     */
    data class ConnectorConfig(
        val strokeWidth: Float = 2f,
        val strokeColor: String = "#4b5563",
        val showArrow: Boolean = true,
        val arrowSize: Float = 6f,
        val markerScale: Float = 2f,
        val lineOpacity: Float = 0.8f,
        val radius: Float = 8f,
        val smoothFactor: Float = 0.5f,
        val useGlassEffect: Boolean = true
    )

    /**
     * Creates an SVG path for a connector between two points with glass effect
     */
    fun makeConnector(startX: Float, startY: Float, endX: Float, endY: Float, id: String = ""): String {
        val dx = endX - startX
        val dy = endY - startY

        // Calculate control points for smooth curve
        val distanceX = abs(dx) * config.smoothFactor
        val distanceY = abs(dy) * config.smoothFactor

        val controlPoint1X = startX + (if (dx > 0) distanceX else -distanceX)
        val controlPoint1Y = startY
        val controlPoint2X = endX - (if (dx > 0) distanceX else -distanceX)
        val controlPoint2Y = endY

        // Create unique marker ID if not provided
        val markerId = if (id.isBlank()) "arrow-${System.currentTimeMillis()}" else "arrow-$id"
        val glassId = if (id.isBlank()) "glass-${System.currentTimeMillis()}" else "glass-$id"

        return buildString {
            // Define arrow marker and glass effect gradients
            append("""
                <defs>
                    <marker 
                        id="$markerId" 
                        viewBox="0 0 10 10" 
                        refX="5" 
                        refY="5"
                        markerWidth="${config.arrowSize * config.markerScale}" 
                        markerHeight="${config.arrowSize * config.markerScale}"
                        orient="auto-start-reverse">
                        <path d="M 0 0 L 10 5 L 0 10 z" fill="${config.strokeColor}"/>
                    </marker>

                    <!-- Glass effect gradients for connector -->
                    <linearGradient id="connector-glass-${glassId}" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" style="stop-color:rgba(255,255,255,0.7);stop-opacity:1" />
                        <stop offset="50%" style="stop-color:rgba(255,255,255,0.3);stop-opacity:1" />
                        <stop offset="100%" style="stop-color:rgba(255,255,255,0.7);stop-opacity:1" />
                    </linearGradient>

                    <!-- Glow filter for glass effect -->
                    <filter id="connector-glow-${glassId}" x="-20%" y="-20%" width="140%" height="140%">
                        <feGaussianBlur stdDeviation="2" result="blur" />
                        <feComposite in="SourceGraphic" in2="blur" operator="over" />
                    </filter>
                </defs>
            """.trimIndent())

            // Create a group for the glass connector
            append("""
                <g class="glass-connector">
                    <!-- Base path with drop shadow -->
                    <path 
                        d="M $startX,$startY C $controlPoint1X,$controlPoint1Y $controlPoint2X,$controlPoint2Y $endX,$endY"
                        fill="none" 
                        stroke="${config.strokeColor}" 
                        stroke-width="${config.strokeWidth + 2}" 
                        stroke-opacity="0.3"
                        filter="url(#connector-glow-${glassId})"
                        stroke-linecap="round"
                    />

                    <!-- Main path with glass effect -->
                    <path 
                        d="M $startX,$startY C $controlPoint1X,$controlPoint1Y $controlPoint2X,$controlPoint2Y $endX,$endY"
                        fill="none" 
                        stroke="${config.strokeColor}" 
                        stroke-width="${config.strokeWidth}" 
                        stroke-opacity="${config.lineOpacity}"
                        ${if (config.showArrow) """marker-end="url(#$markerId)"""" else ""}
                        stroke-linecap="round"
                    />

                    <!-- Glass highlight overlay -->
                    <path 
                        d="M $startX,$startY C $controlPoint1X,$controlPoint1Y $controlPoint2X,$controlPoint2Y $endX,$endY"
                        fill="none" 
                        stroke="url(#connector-glass-${glassId})" 
                        stroke-width="${config.strokeWidth * 0.7}" 
                        stroke-opacity="0.5"
                        stroke-linecap="round"
                    />
                </g>
            """.trimIndent())
        }
    }

    /**
     * Creates an SVG orthogonal connector with rounded corners and glass effect
     */
    fun makeOrthogonalConnector(startX: Float, startY: Float, endX: Float, endY: Float, id: String = ""): String {
        val markerId = if (id.isBlank()) "arrow-${System.currentTimeMillis()}" else "arrow-$id"
        val glassId = if (id.isBlank()) "glass-${System.currentTimeMillis()}" else "glass-$id"
        val midX = (startX + endX) / 2
        val midY = (startY + endY) / 2

        val path = buildString {
            append("M $startX,$startY ")

            // Determine if we need to go horizontal first or vertical first
            if (abs(endX - startX) > abs(endY - startY)) {
                // Go horizontal, then vertical
                append("L $midX,$startY ")
                append("Q ${midX + (if (endX > midX) config.radius else -config.radius)},$startY ")
                append("$midX,${startY + (if (endY > startY) config.radius else -config.radius)} ")
                append("L $midX,${endY - (if (endY > startY) config.radius else -config.radius)} ")
                append("Q $midX,${endY} ")
                append("${midX + (if (endX > midX) config.radius else -config.radius)},$endY ")
                append("L $endX,$endY")
            } else {
                // Go vertical, then horizontal
                append("L $startX,$midY ")
                append("Q $startX,${midY + (if (endY > midY) config.radius else -config.radius)} ")
                append("${startX + (if (endX > startX) config.radius else -config.radius)},$midY ")
                append("L ${endX - (if (endX > startX) config.radius else -config.radius)},$midY ")
                append("Q $endX,$midY ")
                append("$endX,${midY + (if (endY > midY) config.radius else -config.radius)} ")
                append("L $endX,$endY")
            }
        }

        return buildString {
            // Define arrow marker and glass effect gradients
            append("""
                <defs>
                    <marker 
                        id="$markerId" 
                        viewBox="0 0 10 10" 
                        refX="5" 
                        refY="5"
                        markerWidth="${config.arrowSize * config.markerScale}" 
                        markerHeight="${config.arrowSize * config.markerScale}"
                        orient="auto-start-reverse">
                        <path d="M 0 0 L 10 5 L 0 10 z" fill="${config.strokeColor}"/>
                    </marker>

                    <!-- Glass effect gradients for connector -->
                    <linearGradient id="connector-glass-${glassId}" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" style="stop-color:rgba(255,255,255,0.7);stop-opacity:1" />
                        <stop offset="50%" style="stop-color:rgba(255,255,255,0.3);stop-opacity:1" />
                        <stop offset="100%" style="stop-color:rgba(255,255,255,0.7);stop-opacity:1" />
                    </linearGradient>

                    <!-- Glow filter for glass effect -->
                    <filter id="connector-glow-${glassId}" x="-20%" y="-20%" width="140%" height="140%">
                        <feGaussianBlur stdDeviation="2" result="blur" />
                        <feComposite in="SourceGraphic" in2="blur" operator="over" />
                    </filter>
                </defs>
            """.trimIndent())

            // Create a group for the glass connector
            append("""
                <g class="glass-connector">
                    <!-- Base path with drop shadow -->
                    <path 
                        d="$path"
                        fill="none" 
                        stroke="${config.strokeColor}" 
                        stroke-width="${config.strokeWidth + 2}" 
                        stroke-opacity="0.3"
                        filter="url(#connector-glow-${glassId})"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                    />

                    <!-- Main path with glass effect -->
                    <path 
                        d="$path"
                        fill="none" 
                        stroke="${config.strokeColor}" 
                        stroke-width="${config.strokeWidth}" 
                        stroke-opacity="${config.lineOpacity}"
                        ${if (config.showArrow) """marker-end="url(#$markerId)"""" else ""}
                        stroke-linecap="round"
                        stroke-linejoin="round"
                    />

                    <!-- Glass highlight overlay -->
                    <path 
                        d="$path"
                        fill="none" 
                        stroke="url(#connector-glass-${glassId})" 
                        stroke-width="${config.strokeWidth * 0.7}" 
                        stroke-opacity="0.5"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                    />
                </g>
            """.trimIndent())
        }
    }

    /**
     * Creates a self-connecting loop connector with glass effect
     */
    fun makeSelfConnector(x: Float, y: Float, size: Float = 40f, id: String = ""): String {
        val markerId = if (id.isBlank()) "arrow-${System.currentTimeMillis()}" else "arrow-$id"
        val glassId = if (id.isBlank()) "glass-${System.currentTimeMillis()}" else "glass-$id"

        return buildString {
            // Define arrow marker and glass effect gradients
            append("""
                <defs>
                    <marker 
                        id="$markerId" 
                        viewBox="0 0 10 10" 
                        refX="5" 
                        refY="5"
                        markerWidth="${config.arrowSize * config.markerScale}" 
                        markerHeight="${config.arrowSize * config.markerScale}"
                        orient="auto-start-reverse">
                        <path d="M 0 0 L 10 5 L 0 10 z" fill="${config.strokeColor}"/>
                    </marker>

                    <!-- Glass effect gradients for connector -->
                    <linearGradient id="connector-glass-${glassId}" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" style="stop-color:rgba(255,255,255,0.7);stop-opacity:1" />
                        <stop offset="50%" style="stop-color:rgba(255,255,255,0.3);stop-opacity:1" />
                        <stop offset="100%" style="stop-color:rgba(255,255,255,0.7);stop-opacity:1" />
                    </linearGradient>

                    <!-- Glow filter for glass effect -->
                    <filter id="connector-glow-${glassId}" x="-20%" y="-20%" width="140%" height="140%">
                        <feGaussianBlur stdDeviation="2" result="blur" />
                        <feComposite in="SourceGraphic" in2="blur" operator="over" />
                    </filter>
                </defs>
            """.trimIndent())

            // Create a group for the glass connector
            append("""
                <g class="glass-connector">
                    <!-- Base path with drop shadow -->
                    <path 
                        d="M $x,$y C ${x+size},${y-size} ${x+size},${y+size} $x,$y"
                        fill="none" 
                        stroke="${config.strokeColor}" 
                        stroke-width="${config.strokeWidth + 2}" 
                        stroke-opacity="0.3"
                        filter="url(#connector-glow-${glassId})"
                        stroke-linecap="round"
                    />

                    <!-- Main path with glass effect -->
                    <path 
                        d="M $x,$y C ${x+size},${y-size} ${x+size},${y+size} $x,$y"
                        fill="none" 
                        stroke="${config.strokeColor}" 
                        stroke-width="${config.strokeWidth}" 
                        stroke-opacity="${config.lineOpacity}"
                        ${if (config.showArrow) """marker-end="url(#$markerId)"""" else ""}
                        stroke-linecap="round"
                    />

                    <!-- Glass highlight overlay -->
                    <path 
                        d="M $x,$y C ${x+size},${y-size} ${x+size},${y+size} $x,$y"
                        fill="none" 
                        stroke="url(#connector-glass-${glassId})" 
                        stroke-width="${config.strokeWidth * 0.7}" 
                        stroke-opacity="0.5"
                        stroke-linecap="round"
                    />
                </g>
            """.trimIndent())
        }
    }

    /**
     * Creates a multipoint connector that can navigate around obstacles with glass effect
     */
    fun makeMultipointConnector(points: List<Pair<Float, Float>>, id: String = ""): String {
        if (points.size < 2) {
            logger.warn("At least two points are required for a multipoint connector")
            return ""
        }

        val markerId = if (id.isBlank()) "arrow-${System.currentTimeMillis()}" else "arrow-$id"
        val glassId = if (id.isBlank()) "glass-${System.currentTimeMillis()}" else "glass-$id"

        // Build the path data
        val pathData = buildString {
            append("M ${points[0].first},${points[0].second} ")

            for (i in 1 until points.size) {
                val prevX = points[i-1].first
                val prevY = points[i-1].second
                val currentX = points[i].first
                val currentY = points[i].second

                // Add curved connections between points
                if (i < points.size - 1) {
                    val nextX = points[i+1].first
                    val nextY = points[i+1].second

                    // Calculate control points for smooth curve
                    val ctrl1X = prevX + (currentX - prevX) * 0.7f
                    val ctrl1Y = prevY + (currentY - prevY) * 0.7f
                    val ctrl2X = currentX - (nextX - currentX) * 0.7f
                    val ctrl2Y = currentY - (nextY - currentY) * 0.7f

                    append("C $ctrl1X,$ctrl1Y $ctrl2X,$ctrl2Y $currentX,$currentY ")
                } else {
                    // For the last segment, use a simple line
                    append("L $currentX,$currentY")
                }
            }
        }

        return buildString {
            // Define arrow marker and glass effect gradients
            append("""
                <defs>
                    <marker 
                        id="$markerId" 
                        viewBox="0 0 10 10" 
                        refX="5" 
                        refY="5"
                        markerWidth="${config.arrowSize * config.markerScale}" 
                        markerHeight="${config.arrowSize * config.markerScale}"
                        orient="auto-start-reverse">
                        <path d="M 0 0 L 10 5 L 0 10 z" fill="${config.strokeColor}"/>
                    </marker>

                    <!-- Glass effect gradients for connector -->
                    <linearGradient id="connector-glass-${glassId}" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" style="stop-color:rgba(255,255,255,0.7);stop-opacity:1" />
                        <stop offset="50%" style="stop-color:rgba(255,255,255,0.3);stop-opacity:1" />
                        <stop offset="100%" style="stop-color:rgba(255,255,255,0.7);stop-opacity:1" />
                    </linearGradient>

                    <!-- Glow filter for glass effect -->
                    <filter id="connector-glow-${glassId}" x="-20%" y="-20%" width="140%" height="140%">
                        <feGaussianBlur stdDeviation="2" result="blur" />
                        <feComposite in="SourceGraphic" in2="blur" operator="over" />
                    </filter>
                </defs>
            """.trimIndent())

            // Create a group for the glass connector
            append("""
                <g class="glass-connector">
                    <!-- Base path with drop shadow -->
                    <path 
                        d="$pathData"
                        fill="none" 
                        stroke="${config.strokeColor}" 
                        stroke-width="${config.strokeWidth + 2}" 
                        stroke-opacity="0.3"
                        filter="url(#connector-glow-${glassId})"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                    />

                    <!-- Main path with glass effect -->
                    <path 
                        d="$pathData"
                        fill="none" 
                        stroke="${config.strokeColor}" 
                        stroke-width="${config.strokeWidth}" 
                        stroke-opacity="${config.lineOpacity}"
                        ${if (config.showArrow) """marker-end="url(#$markerId)"""" else ""}
                        stroke-linecap="round"
                        stroke-linejoin="round"
                    />

                    <!-- Glass highlight overlay -->
                    <path 
                        d="$pathData"
                        fill="none" 
                        stroke="url(#connector-glass-${glassId})" 
                        stroke-width="${config.strokeWidth * 0.7}" 
                        stroke-opacity="0.5"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                    />
                </g>
            """.trimIndent())
        }
    }

    companion object {
        /**
         * Creates a quick connector with default settings and glass effect
         */
        fun quickConnector(startX: Float, startY: Float, endX: Float, endY: Float, color: String = "#4b5563"): String {
            return ConnectorMakerImproved(ConnectorConfig(strokeColor = color))
                .makeConnector(startX, startY, endX, endY)
        }
    }
}
fun createFlowDiagram(): String {
    val connectorMaker = ConnectorMakerImproved(
        ConnectorMakerImproved.ConnectorConfig(
            strokeWidth = 2.5f,
            strokeColor = "#4b5563",
            showArrow = true,
            lineOpacity = 0.9f
        )
    )

    // Define the positions of the nodes
    val startX = 100f
    val startY = 50f

    val processX = 100f
    val processY = 150f

    val decisionX = 100f
    val decisionY = 250f

    val option1X = 220f
    val option1Y = 250f

    val option2X = 100f
    val option2Y = 350f

    // Create the connectors
    val startToProcess = connectorMaker.makeConnector(
        startX, startY + 30f, processX, processY - 30f, "conn1"
    )

    val processToDecision = connectorMaker.makeConnector(
        processX, processY + 30f, decisionX, decisionY - 30f, "conn2"
    )

    val decisionToOption1 = connectorMaker.makeConnector(
        decisionX + 30f, decisionY, option1X - 40f, option1Y, "conn3"
    )

    val decisionToOption2 = connectorMaker.makeConnector(
        decisionX, decisionY + 30f, option2X, option2Y - 30f, "conn4"
    )

    // Build the complete SVG
    return """
        <svg width="350" height="400" xmlns="http://www.w3.org/2000/svg">
            <!-- Flow diagram nodes -->
            <rect x="50" y="20" width="100" height="60" rx="10" fill="#93c5fd" stroke="#2563eb" stroke-width="2" />
            <text x="100" y="55" font-family="Arial" font-size="14" text-anchor="middle">Start</text>

            <rect x="50" y="120" width="100" height="60" rx="10" fill="#bfdbfe" stroke="#3b82f6" stroke-width="2" />
            <text x="100" y="155" font-family="Arial" font-size="14" text-anchor="middle">Process</text>

            <polygon points="100,220 130,250 100,280 70,250" fill="#ddd6fe" stroke="#7c3aed" stroke-width="2" />
            <text x="100" y="255" font-family="Arial" font-size="14" text-anchor="middle">?</text>

            <rect x="180" y="220" width="80" height="60" rx="10" fill="#c7d2fe" stroke="#4f46e5" stroke-width="2" />
            <text x="220" y="255" font-family="Arial" font-size="14" text-anchor="middle">Option 1</text>

            <rect x="60" y="320" width="80" height="60" rx="10" fill="#c7d2fe" stroke="#4f46e5" stroke-width="2" />
            <text x="100" y="355" font-family="Arial" font-size="14" text-anchor="middle">Option 2</text>

            <!-- Labels for decision paths -->
            <text x="150" y="235" font-family="Arial" font-size="12" text-anchor="middle">Yes</text>
            <text x="85" y="300" font-family="Arial" font-size="12" text-anchor="middle">No</text>

            <!-- The connectors -->
            $startToProcess
            $processToDecision
            $decisionToOption1
            $decisionToOption2
        </svg>
    """.trimIndent()
}

fun main() {
    val flowDiagram = createFlowDiagram()
    println(flowDiagram)

    // Optionally save to file
     File("gen/flow-diagram.svg").writeText(flowDiagram)
}
