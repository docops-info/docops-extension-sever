package io.docops.docopsextensionssupport.diagram.treechart

import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.util.ParsingUtils
import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.update
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min

class CyberTreeMaker(val useDark: Boolean = false) {

    private var theme = ThemeFactory.getTheme(useDark)

    fun makeTree(payload: String, csvResponse: CsvResponse): String {
        val (config, chartData) = ParsingUtils.parseConfigAndData(payload)
        val treeData = parseTreeChartData(chartData)
        csvResponse.update(treeData.toCsv())
        val themeName = config["theme"] ?: "modern"
        theme = ThemeFactory.getThemeByName(themeName, useDark)

        val title = config.getOrDefault("title", "Project Roadmap")
        val orientation = config.getOrDefault("orientation", "vertical")
        val customColors = config["colors"]?.split(",")?.map { it.trim() } ?: theme.chartPaletteHex

        val depth = calculateDepth(treeData)
        val maxWidth = calculateMaxWidth(treeData)

        val nodeRadius = max(28, min(48, 52 - (maxWidth * 2)))
        val levelSpacing = max(150, min(240, 220 - (depth * 8)))
        val siblingSpacing = max(100, min(180, 148 - maxWidth))

        // Correctly calculate dimensions based on orientation
        val calculatedWidth: Int
        val calculatedHeight: Int

        if (orientation == "horizontal") {
            calculatedWidth = max(config.getOrDefault("width", "800").toInt(), depth * levelSpacing + 280)
            calculatedHeight = max(config.getOrDefault("height", "600").toInt(), maxWidth * siblingSpacing + 180)
        } else {
            calculatedWidth = max(config.getOrDefault("width", "800").toInt(), maxWidth * siblingSpacing + 180)
            calculatedHeight = max(config.getOrDefault("height", "600").toInt(), depth * levelSpacing + 280)
        }

        val margin = mapOf("top" to 128, "right" to 88, "bottom" to 96, "left" to 108)
        val positions = calculateDynamicPositions(treeData, calculatedWidth, calculatedHeight, margin, orientation, levelSpacing)

        val svgId = "cyber_tree_${System.nanoTime()}"
        val svgBuilder = StringBuilder()
        svgBuilder.append("<svg id='$svgId' width='$calculatedWidth' height='$calculatedHeight' viewBox='0 0 $calculatedWidth $calculatedHeight' xmlns='http://www.w3.org/2000/svg'>")

        // Definitions (Styles, filters, gradients, grid)
        val titleColor = if (useDark) "#e8f0ff" else "#102542"
        val mutedText = if (useDark) "#9eb2d4" else "#4d678d"
        val nodeInnerFill = if (useDark) "rgba(12, 20, 36, 0.86)" else "rgba(255, 255, 255, 0.84)"
        val dominant = theme.accentColor
        val bgStart = if (useDark) "#090f1c" else "#f6f9fc"
        val bgEnd = if (useDark) "#0f1a30" else "#eaf2fb"
        val gridStroke = if (useDark) "rgba(156,184,236,0.12)" else "rgba(37,74,124,0.10)"

        svgBuilder.append(
            """
            <defs>
                <linearGradient id='${svgId}_bg' x1='0%' y1='0%' x2='100%' y2='100%'>
                    <stop offset='0%' stop-color='$bgStart'/>
                    <stop offset='100%' stop-color='$bgEnd'/>
                </linearGradient>
                <radialGradient id='${svgId}_washA' cx='16%' cy='20%' r='56%'>
                    <stop offset='0%' stop-color='$dominant' stop-opacity='${if (useDark) "0.18" else "0.14"}'/>
                    <stop offset='100%' stop-color='$dominant' stop-opacity='0'/>
                </radialGradient>
                <radialGradient id='${svgId}_washB' cx='88%' cy='82%' r='52%'>
                    <stop offset='0%' stop-color='${if (useDark) "#00d4ff" else "#2563eb"}' stop-opacity='${if (useDark) "0.14" else "0.10"}'/>
                    <stop offset='100%' stop-color='${if (useDark) "#00d4ff" else "#2563eb"}' stop-opacity='0'/>
                </radialGradient>
                <pattern id='${svgId}_grid' width='28' height='28' patternUnits='userSpaceOnUse'>
                    <path d='M28 0H0V28' fill='none' stroke='$gridStroke' stroke-width='1'/>
                </pattern>
                <filter id='${svgId}_ambientBlur' x='-30%' y='-30%' width='160%' height='160%'>
                    <feGaussianBlur stdDeviation='56'/>
                </filter>
                <filter id='${svgId}_nodeGlow' x='-120%' y='-120%' width='340%' height='340%'>
                    <feGaussianBlur stdDeviation='3.2' result='b'/>
                    <feMerge>
                        <feMergeNode in='b'/>
                        <feMergeNode in='SourceGraphic'/>
                    </feMerge>
                </filter>
                <style type='text/css'>
                    /* <![CDATA[ */
                    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@500;700;800&amp;family=Outfit:wght@400;600&amp;display=swap');

                    #$svgId {
                        --tree-title: $titleColor;
                        --tree-muted: $mutedText;
                        --tree-node-fill: $nodeInnerFill;
                        --tree-link: $dominant;
                        --tree-accent: $dominant;
                    }

                    #$svgId .node-layout {}
                    #$svgId .node-anim {
                        opacity: 0;
                        transform-origin: center;
                        animation: cyberReveal 760ms cubic-bezier(0.16, 1, 0.3, 1) forwards;
                    }

                    #$svgId .link {
                        fill: none;
                        stroke: var(--tree-link);
                        stroke-opacity: ${if (useDark) "0.26" else "0.20"};
                        stroke-width: 1.6;
                    }

                    #$svgId .label-main {
                        font-family: 'Inter', -apple-system, sans-serif;
                        font-size: 11px;
                        font-weight: 800;
                        text-transform: uppercase;
                        letter-spacing: 0.2px;
                        pointer-events: none;
                    }

                    #$svgId .label-sub {
                        font-family: 'Outfit', sans-serif;
                        font-size: 9px;
                        font-weight: 500;
                        fill: var(--tree-muted);
                        pointer-events: none;
                    }

                    #$svgId .title-wrap {
                        opacity: 0;
                        animation: titleReveal 640ms cubic-bezier(0.16, 1, 0.3, 1) forwards;
                        animation-delay: 90ms;
                    }

                    @keyframes cyberReveal {
                        from { transform: translateY(8px) scale(0.92); opacity: 0; }
                        to { transform: translateY(0) scale(1); opacity: 1; }
                    }

                    @keyframes titleReveal {
                        from { transform: translateY(10px); opacity: 0; }
                        to { transform: translateY(0); opacity: 1; }
                    }
                    /* ]]> */
                </style>
            </defs>
            """.trimIndent()
        )

        svgBuilder.append("<rect width='100%' height='100%' fill='url(#${svgId}_bg)'/>")
        svgBuilder.append("<rect width='100%' height='100%' fill='url(#${svgId}_grid)'/>")
        svgBuilder.append("<rect width='100%' height='100%' fill='url(#${svgId}_washA)'/>")
        svgBuilder.append("<rect width='100%' height='100%' fill='url(#${svgId}_washB)'/>")
        svgBuilder.append("<circle cx='${calculatedWidth / 2}' cy='${calculatedHeight / 2}' r='${(calculatedWidth * 0.44).toInt()}' fill='$dominant' opacity='0.06' filter='url(#${svgId}_ambientBlur)' />")

        // Draw links first so nodes stay in foreground
        drawLinks(svgBuilder, treeData, positions, nodeRadius, orientation)
        drawNodes(
            sb = svgBuilder,
            node = treeData,
            pos = positions,
            colors = customColors,
            radius = nodeRadius,
            level = 0,
            useDark = useDark,
            nodeInnerFill = nodeInnerFill,
            svgId = svgId
        )

        // Title: outer translate group + inner animated group (safe for SVG/CSS transforms)
        svgBuilder.append(
            """
            <g transform='translate(40, 64)'>
                <g class='title-wrap'>
                    <text font-family='Inter, -apple-system, sans-serif' font-size='34' fill='var(--tree-title)' font-weight='800'>${escapeXml(title.uppercase())}</text>
                    <rect y='18' width='124' height='4' fill='var(--tree-accent)' rx='2' />
                </g>
            </g>
            """.trimIndent()
        )

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
        nodeInnerFill: String,
        svgId: String
    ) {
        val (x, y) = pos[node]!!
        val accent = node.color ?: colors[colorIdx % colors.size]
        val lines = wrapTextByWidth(node.label, maxPixelWidth = radius * 2 - 14, fontSize = 11)
        val subLabelColor = if (useDark) "#9eb2d4" else "#4d678d"

        // Outer group handles layout translate, inner group handles animation (prevents transform conflicts)
        sb.append("<g class='node-layout' transform='translate($x,$y)'>")
        sb.append("<g class='node-anim' style='animation-delay: ${level * 0.1}s;'>")
        sb.append("<circle cx='0' cy='0' r='$radius' fill='$nodeInnerFill' stroke='$accent' stroke-width='2' filter='url(#${svgId}_nodeGlow)'/>")
        sb.append("<circle cx='0' cy='0' r='${radius - 5}' fill='none' stroke='$accent' stroke-width='0.7' stroke-opacity='0.35'/>")

        val lineHeight = 14
        val startY = -((lines.size - 1) * lineHeight / 2.0)

        lines.forEachIndexed { i, line ->
            val isFirst = i == 0
            val className = if (isFirst) "label-main" else "label-sub"
            val fill = if (isFirst) accent else subLabelColor
            sb.append("<text x='0' y='${startY + (i * lineHeight)}' text-anchor='middle' dominant-baseline='middle' class='$className' fill='$fill'>${escapeXml(line)}</text>")
        }

        sb.append("</g>")
        sb.append("</g>")

        node.children.forEachIndexed { i, child ->
            drawNodes(sb, child, pos, colors, radius, level + 1, colorIdx + i + 1, useDark, nodeInnerFill, svgId)
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

    private fun wrapTextByWidth(text: String, maxPixelWidth: Int, fontSize: Int): List<String> {
        val words = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return listOf("")

        val lines = mutableListOf<String>()
        var current = ""

        words.forEach { word ->
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (estimateTextWidth(candidate, fontSize) <= maxPixelWidth) {
                current = candidate
            } else {
                if (current.isNotEmpty()) lines.add(current)
                current = word
            }
        }
        if (current.isNotEmpty()) lines.add(current)

        return lines.map { line ->
            if (estimateTextWidth(line, fontSize) <= maxPixelWidth) {
                line
            } else {
                ellipsize(line, maxPixelWidth, fontSize)
            }
        }.take(3)
    }

    private fun ellipsize(text: String, maxPixelWidth: Int, fontSize: Int): String {
        if (estimateTextWidth(text, fontSize) <= maxPixelWidth) return text
        var out = text
        while (out.isNotEmpty() && estimateTextWidth("$out…", fontSize) > maxPixelWidth) {
            out = out.dropLast(1)
        }
        return if (out.isEmpty()) "…" else "$out…"
    }

    private fun estimateTextWidth(text: String, fontSize: Int): Int = (text.length * fontSize * 0.58).toInt()

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}

@Serializable
data class TreeNode(val label: String, val color: String? = null, val children: MutableList<TreeNode> = mutableListOf()) {
    fun toCsv(): CsvResponse = CsvResponse(listOf("Label"), listOf(listOf(label))) // Simplified for example
}
