package io.docops.docopsextensionssupport.diagram.treechart

import io.docops.docopsextensionssupport.util.ParsingUtils
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.update
import kotlin.math.max

class CyberTreeMaker(val useDark: Boolean = false) {

    private val neonPalette = listOf(
        "#00f2ff", // Cyan
        "#7000ff", // Purple
        "#39ff14", // Neon Green
        "#ff007f", // Hot Pink
        "#f39c12"  // Amber
    )

    fun makeTree(payload: String, csvResponse: CsvResponse): String {
        val (config, chartData) = ParsingUtils.parseConfigAndData(payload)
        val treeData = parseTreeChartData(chartData)
        csvResponse.update(treeData.toCsv())

        val title = config.getOrDefault("title", "Project Roadmap")
        val orientation = config.getOrDefault("orientation", "vertical")
        val customColors = config["colors"]?.split(",")?.map { it.trim() } ?: neonPalette

        val depth = calculateDepth(treeData)
        val maxWidth = calculateMaxWidth(treeData)

        val nodeRadius = 45
        val levelSpacing = 200 // Space between levels
        val siblingSpacing = 130 // Minimum space between nodes on the same level

        // Correctly calculate dimensions based on orientation
        val calculatedWidth: Int
        val calculatedHeight: Int

        if (orientation == "horizontal") {
            calculatedWidth = max(config.getOrDefault("width", "800").toInt(), depth * levelSpacing + 200)
            calculatedHeight = max(config.getOrDefault("height", "600").toInt(), maxWidth * siblingSpacing + 100)
        } else {
            calculatedWidth = max(config.getOrDefault("width", "800").toInt(), maxWidth * siblingSpacing + 100)
            calculatedHeight = max(config.getOrDefault("height", "600").toInt(), depth * levelSpacing + 200)
        }

        val margin = mapOf("top" to 120, "right" to 80, "bottom" to 80, "left" to 100)
        val positions = calculateDynamicPositions(treeData, calculatedWidth, calculatedHeight, margin, orientation, levelSpacing)

        val svgBuilder = StringBuilder()
        svgBuilder.append("<svg width='$calculatedWidth' height='$calculatedHeight' viewBox='0 0 $calculatedWidth $calculatedHeight' xmlns='http://www.w3.org/2000/svg'>")

        // Definitions (Styles & Gradients)
        svgBuilder.append("""
            <defs>
                <style type="text/css">
                    /* <![CDATA[ */
                    @import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;600&amp;family=Syne:wght@700&amp;display=swap');
                    .node-group { opacity: 0; animation: cyberReveal 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
                    .link { fill: none; stroke: #00f2ff; stroke-opacity: 0.15; stroke-width: 1.5; }
                    .label-main { font-family: 'Syne', sans-serif; font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; pointer-events: none; }
                    .label-sub { font-family: 'Outfit', sans-serif; font-size: 9px; font-weight: 400; fill: #94a3b8; pointer-events: none; }
                    @keyframes cyberReveal { from { transform: scale(0.9); opacity: 0; } to { transform: scale(1); opacity: 1; } }
                    /* ]]> */
                </style>
            </defs>
        """.trimIndent())

        // Background
        val bgFill = if (useDark) "#020617" else "#f8fafc"
        val glowColor = if (useDark) "#7000ff" else "#818cf8"
        val nodeInnerFill = if (useDark) "rgba(15, 23, 42, 0.8)" else "rgba(255, 255, 255, 0.9)"
        val titleColor = if (useDark) "#ffffff" else "#0f172a"

        svgBuilder.append("<rect width='100%' height='100%' fill='$bgFill' />")
        svgBuilder.append("<circle cx='${calculatedWidth/2}' cy='${calculatedHeight/2}' r='${calculatedWidth/2}' fill='$glowColor' opacity='0.05' filter='blur(120px)' />")

        // Draw Links first to keep them behind nodes
        drawLinks(svgBuilder, treeData, positions, nodeRadius, orientation)
        drawNodes(svgBuilder, treeData, positions, customColors, nodeRadius, 0, useDark = useDark, nodeInnerFill = nodeInnerFill)

        // Title
        svgBuilder.append("""
            <g transform="translate(40, 60)">
                <text font-family="Syne, sans-serif" font-size="28" fill="$titleColor" font-weight="700">${title.uppercase()}</text>
                <rect y="15" width="100" height="4" fill="#7000ff" rx="2" />
            </g>
        """.trimIndent())

        svgBuilder.append("</svg>")
        return svgBuilder.toString()
    }


    private fun calculateDynamicPositions(root: TreeNode, w: Int, h: Int, m: Map<String, Int>, orient: String, levelSpacing: Int): Map<TreeNode, Pair<Double, Double>> {
        val posMap = mutableMapOf<TreeNode, Pair<Double, Double>>()
        val levelNodes = mutableMapOf<Int, MutableList<TreeNode>>()

        fun mapLevels(n: TreeNode, l: Int) {
            levelNodes.getOrPut(l) { mutableListOf() }.add(n)
            n.children.forEach { mapLevels(it, l + 1) }
        }
        mapLevels(root, 0)

        levelNodes.forEach { (level, nodes) ->
            val total = nodes.size
            nodes.forEachIndexed { i, node ->
                if (orient == "vertical") {
                    val availableWidth = w - m["left"]!! - m["right"]!!
                    val dx = availableWidth.toDouble() / (total + 1)
                    posMap[node] = Pair(m["left"]!! + dx * (i + 1), m["top"]!! + (level * levelSpacing).toDouble())
                } else {
                    // For horizontal, 'level' controls X, and 'i' (siblings) controls Y
                    val availableHeight = h - m["top"]!! - m["bottom"]!!
                    val dy = availableHeight.toDouble() / (total + 1)
                    posMap[node] = Pair(m["left"]!! + (level * levelSpacing).toDouble(), m["top"]!! + dy * (i + 1))
                }
            }
        }
        return posMap
    }


    private fun drawLinks(sb: StringBuilder, node: TreeNode, pos: Map<TreeNode, Pair<Double, Double>>, radius: Int, orient: String) {
        val (px, py) = pos[node]!!
        node.children.forEach { child ->
            val (cx, cy) = pos[child]!!
            val d = if (orient == "vertical") {
                "M$px,${py + radius} C$px,${(py+cy)/2} $cx,${(py+cy)/2} $cx,${cy - radius}"
            } else {
                "M${px + radius},$py C${(px+cx)/2},$py ${(px+cx)/2},$cy ${cx - radius},$cy"
            }
            sb.append("<path class='link' d='$d' />")
            drawLinks(sb, child, pos, radius, orient)
        }
    }

    private fun drawNodes(
        sb: StringBuilder,
        node: TreeNode,
        pos: Map<TreeNode, Pair<Double, Double>>,
        colors: List<String>,
        radius: Int,
        level: Int,
        colorIdx: Int = 0,
        useDark: Boolean,
        nodeInnerFill: String
    ) {
        val (x, y) = pos[node]!!
        val accent = node.color ?: colors[colorIdx % colors.size]
        val lines = wrapText(node.label, 12)

        sb.append("<g class='node-group' style='animation-delay: ${level * 0.1}s;'>")
        // Inner Glow - Using nodeInnerFill for theme-aware background
        sb.append("<circle cx='$x' cy='$y' r='$radius' fill='$nodeInnerFill' stroke='$accent' stroke-width='2' />")
        sb.append("<circle cx='$x' cy='$y' r='${radius-5}' fill='none' stroke='$accent' stroke-width='0.5' stroke-opacity='0.3' />")

        // Multi-line Text logic
        val lineHeight = 14
        val startY = y - ((lines.size - 1) * lineHeight / 2.0)
        val subLabelColor = if (useDark) "#94a3b8" else "#475569"

        lines.forEachIndexed { i, line ->
            val isFirst = i == 0
            val className = if (isFirst) "label-main" else "label-sub"
            val fill = if (isFirst) accent else subLabelColor
            sb.append("<text x='$x' y='${startY + (i * lineHeight)}' text-anchor='middle' dominant-baseline='middle' class='$className' fill='$fill'>$line</text>")
        }
        sb.append("</g>")

        node.children.forEachIndexed { i, child ->
            drawNodes(sb, child, pos, colors, radius, level + 1, colorIdx + i + 1, useDark, nodeInnerFill)
        }
    }

    private fun calculateDepth(node: TreeNode): Int = if (node.children.isEmpty()) 1 else 1 + node.children.maxOf { calculateDepth(it) }

    private fun calculateMaxWidth(root: TreeNode): Int {
        val counts = mutableMapOf<Int, Int>()
        fun walk(n: TreeNode, l: Int) { counts[l] = (counts[l] ?: 0) + 1; n.children.forEach { walk(it, l + 1) } }
        walk(root, 0)
        return counts.values.maxOrNull() ?: 1
    }
    // ... (Helper methods for wrapText, parseTreeChartData, calculatePositions identical to previous logic but refined for absolute coords) ...
    private fun wrapText(text: String, max: Int) = text.split(" ").let { words ->
        val lines = mutableListOf<String>()
        var current = ""
        words.forEach { w ->
            if ((current + w).length > max) { lines.add(current); current = w }
            else current = if (current.isEmpty()) w else "$current $w"
        }
        if (current.isNotEmpty()) lines.add(current)
        lines
    }

    private fun calculatePositions(root: TreeNode, w: Int, h: Int, m: Map<String, Int>, orient: String): Map<TreeNode, Pair<Double, Double>> {
        val posMap = mutableMapOf<TreeNode, Pair<Double, Double>>()
        val levelMap = mutableMapOf<Int, MutableList<TreeNode>>()
        fun mapLevels(n: TreeNode, l: Int) { levelMap.getOrPut(l) { mutableListOf() }.add(n); n.children.forEach { mapLevels(it, l + 1) } }
        mapLevels(root, 0)

        fun walk(n: TreeNode, l: Int) {
            val siblings = levelMap[l]!!
            val i = siblings.indexOf(n)
            val total = siblings.size
            if (orient == "vertical") {
                val dx = (w - m["left"]!! - m["right"]!!) / (total + 1.0)
                posMap[n] = Pair(m["left"]!! + dx * (i + 1), m["top"]!! + l * 160.0)
            } else {
                val dy = (h - m["top"]!! - m["bottom"]!!) / (total + 1.0)
                posMap[n] = Pair(m["left"]!! + l * 180.0, m["top"]!! + dy * (i + 1))
            }
            n.children.forEach { walk(it, l + 1) }
        }
        walk(root, 0)
        return posMap
    }

    private fun parseTreeChartData(content: String): TreeNode {
        // Implementation similar to TreeMaker.parseTreeChartData but isolated here
        val lines = content.lines().filter { it.isNotBlank() }
        val rootParts = lines.first().split("|").map { it.trim() }
        val root = TreeNode(rootParts[0], if (rootParts.size > 1) rootParts[1] else null)
        val stack = mutableListOf(Pair(root, 0))
        for (i in 1 until lines.size) {
            val indent = lines[i].indexOfFirst { !it.isWhitespace() }
            val parts = lines[i].trim().split("|").map { it.trim() }
            val node = TreeNode(parts[0], if (parts.size > 1) parts[1] else null)
            while (stack.isNotEmpty() && stack.last().second >= indent) stack.removeAt(stack.size - 1)
            stack.last().first.children.add(node)
            stack.add(Pair(node, indent))
        }
        return root
    }

    private data class TreeNode(val label: String, val color: String? = null, val children: MutableList<TreeNode> = mutableListOf()) {
        fun toCsv(): CsvResponse = CsvResponse(listOf("Label"), listOf(listOf(label))) // Simplified for example
    }
}
