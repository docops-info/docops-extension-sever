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
            .trim()
            .lowercase()
            .let { if (it == "horizontal") "horizontal" else "vertical" }

        val isCompact = config["mode"]?.lowercase() == "compact" || config["compact"]?.toBoolean() == true
        val useLinkGradients = config["linkGradients"]?.toBoolean() ?: true

        val customColors = config["colors"]
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { isValidHexColor(it) }
            ?.takeIf { it.isNotEmpty() }
            ?: theme.chartPaletteHex.filter { isValidHexColor(it) }.ifEmpty {
                listOf("#00d4ff", "#2563eb", "#06d6a0", "#f59e0b", "#ef476f")
            }

        val depth = calculateDepth(treeData)
        val maxWidth = calculateMaxWidth(treeData)

        val nodeRadius = if (isCompact) {
            max(22, min(38.0, 42.0 - (maxWidth * 1.5)).toInt())
        } else {
            max(28, min(48, 52 - (maxWidth * 2)))
        }
        val levelSpacing = if (isCompact) {
            max(110, min(180.0, 160.0 - (depth * 6.0)).toInt())
        } else {
            max(150, min(240, 220 - (depth * 8)))
        }
        val siblingSpacing = if (isCompact) {
            max(70, min(140, 120 - maxWidth))
        } else {
            max(100, min(180, 148 - maxWidth))
        }

        // Correctly calculate dimensions based on orientation
        val calculatedWidth: Int
        val calculatedHeight: Int
        val requestedWidth = config["width"]?.toIntOrNull() ?: 800
        val requestedHeight = config["height"]?.toIntOrNull() ?: 600

        if (orientation == "horizontal") {
            calculatedWidth = max(requestedWidth, depth * levelSpacing + 280)
            calculatedHeight = max(requestedHeight, maxWidth * siblingSpacing + 180)
        } else {
            calculatedWidth = max(requestedWidth, maxWidth * siblingSpacing + 180)
            calculatedHeight = max(requestedHeight, depth * levelSpacing + 280)
        }

        val margin = mapOf("top" to 128, "right" to 88, "bottom" to 96, "left" to 108)
        val positions = calculateDynamicPositions(
            root = treeData,
            w = calculatedWidth,
            h = calculatedHeight,
            m = margin,
            orient = orientation,
            levelSpacing = levelSpacing,
            siblingSpacing = siblingSpacing
        )

        val svgId = "cyber_tree_${System.nanoTime()}"
        val svgBuilder = StringBuilder()
        svgBuilder.append("<svg id='$svgId' width='$calculatedWidth' height='$calculatedHeight' viewBox='0 0 $calculatedWidth $calculatedHeight' xmlns='http://www.w3.org/2000/svg'>")
        // Definitions (Styles, filters, gradients, grid)
        val titleColor = if (useDark) "#e8f0ff" else "#102542"
        val mutedText = if (useDark) "#9eb2d4" else "#4d678d"
        val nodeInnerFill = if (useDark) "#0c1424" else "#ffffff"
        val nodeInnerOpacity = if (useDark) "0.86" else "0.84"
        val dominant = theme.accentColor
        val bgStart = if (useDark) "#090f1c" else "#f6f9fc"
        val bgEnd = if (useDark) "#0f1a30" else "#eaf2fb"
        val gridStroke = if (useDark) "#9cb8ec" else "#254a7c"
        val gridStrokeOpacity = if (useDark) "0.12" else "0.10"


        val linkDefs = StringBuilder()
        val linksPaths = StringBuilder()
        var linkIndex = 0
        drawLinks(
            sb = linksPaths,
            defs = linkDefs,
            node = treeData,
            pos = positions,
            radius = nodeRadius,
            orient = orientation,
            colors = customColors,
            svgId = svgId,
            useGradients = useLinkGradients,
            nextLinkId = { linkIndex++ }
        )

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
                    <path d='M28 0H0V28' fill='none' stroke='$gridStroke' stroke-opacity='$gridStrokeOpacity' stroke-width='1'/>
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
                <filter id='${svgId}_rootGlow' x='-150%' y='-150%' width='400%' height='400%'>
                    <feGaussianBlur stdDeviation='6' result='b'/>
                    <feMerge>
                        <feMergeNode in='b'/>
                        <feMergeNode in='SourceGraphic'/>
                    </feMerge>
                </filter>
                $linkDefs
                <style type='text/css'>
                    /* <![CDATA[ */

                    #$svgId {
                            --tree-title: $titleColor;
                            --tree-muted: $mutedText;
                            --tree-node-fill: $nodeInnerFill;
                            --tree-node-fill-opacity: $nodeInnerOpacity;
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
                            stroke-opacity: ${if (useDark) "0.7" else "0.6"};
                            stroke-width: 2.2;
                        }

                        #$svgId .label-main {
                            font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                            font-size: ${if (isCompact) "10px" else "11px"};
                            font-weight: 800;
                            text-transform: uppercase;
                            letter-spacing: 0.2px;
                            pointer-events: none;
                        }

                        #$svgId .label-sub {
                            font-family: ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                            font-size: ${if (isCompact) "8px" else "9px"};
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

        val animationOrder = buildTraversalOrder(treeData)

        // Draw links paths collected earlier
        svgBuilder.append(linksPaths)

        drawNodes(
            sb = svgBuilder,
            node = treeData,
            pos = positions,
            colors = customColors,
            radius = nodeRadius,
            level = 0,
            useDark = useDark,
            nodeInnerFill = nodeInnerFill,
            svgId = svgId,
            animationOrder = animationOrder,
            isCompact = isCompact
        )

        val titleLines = wrapTitleByWidth(
            text = title.uppercase(),
            maxPixelWidth = calculatedWidth - 96,
            fontSize = 34,
            maxLines = 2
        )

        val titleText = titleLines.mapIndexed { i, line ->
            "<text x='0' y='${i * 38}' font-family='Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, \"Segoe UI\", sans-serif' font-size='34' fill='var(--tree-title)' font-weight='800' letter-spacing='-0.34'>${escapeXml(line)}</text>"
        }.joinToString("")
        val titleAccentY = 18 + ((titleLines.size - 1) * 38)

        // Title: outer translate group + inner animated group (safe for SVG/CSS transforms)
        svgBuilder.append(
            """
                <g transform='translate(40, 64)'>
                    <g class='title-wrap'>
                        $titleText
                        <rect y='$titleAccentY' width='124' height='4' fill='var(--tree-accent)' rx='2' />
                    </g>
                </g>
                """.trimIndent()
        )

        svgBuilder.append("</svg>")
        return svgBuilder.toString()
    }


    private fun calculateDynamicPositions(
        root: TreeNode,
        w: Int,
        h: Int,
        m: Map<String, Int>,
        orient: String,
        levelSpacing: Int,
        siblingSpacing: Int
    ): Map<TreeNode, Pair<Double, Double>> {
        val posMap = mutableMapOf<TreeNode, Pair<Double, Double>>()
        val leafOrder = mutableMapOf<TreeNode, Int>()
        var nextLeafIndex = 0

        fun assignLeafSlots(node: TreeNode) {
            if (node.children.isEmpty()) {
                leafOrder[node] = nextLeafIndex
                nextLeafIndex += 1
                return
            }

            node.children.forEach { assignLeafSlots(it) }
        }

        fun resolveBreadthPosition(node: TreeNode): Double {
            if (node.children.isEmpty()) {
                return leafOrder.getValue(node).toDouble()
            }

            val childPositions = node.children.map { resolveBreadthPosition(it) }
            return childPositions.average()
        }

        fun place(node: TreeNode, level: Int) {
            val breadth = resolveBreadthPosition(node)

            if (orient == "vertical") {
                val availableWidth = w - m["left"]!! - m["right"]!!
                val requiredWidth = max(1, nextLeafIndex - 1) * siblingSpacing
                val startX = m["left"]!! + max(0.0, (availableWidth - requiredWidth) / 2.0)

                posMap[node] = Pair(
                    startX + breadth * siblingSpacing,
                    m["top"]!! + level * levelSpacing.toDouble()
                )
            } else {
                val availableHeight = h - m["top"]!! - m["bottom"]!!
                val requiredHeight = max(1, nextLeafIndex - 1) * siblingSpacing
                val startY = m["top"]!! + max(0.0, (availableHeight - requiredHeight) / 2.0)

                posMap[node] = Pair(
                    m["left"]!! + level * levelSpacing.toDouble(),
                    startY + breadth * siblingSpacing
                )
            }

            node.children.forEach { place(it, level + 1) }
        }

        assignLeafSlots(root)

        if (nextLeafIndex == 0) {
            posMap[root] = if (orient == "vertical") {
                Pair(w / 2.0, m["top"]!!.toDouble())
            } else {
                Pair(m["left"]!!.toDouble(), h / 2.0)
            }
            return posMap
        }

        place(root, 0)
        return posMap
    }

    private fun drawLinks(
        sb: StringBuilder,
        defs: StringBuilder,
        node: TreeNode,
        pos: Map<TreeNode, Pair<Double, Double>>,
        radius: Int,
        orient: String,
        colors: List<String>,
        colorIdx: Int = 0,
        svgId: String,
        useGradients: Boolean,
        level: Int = 0,
        nextLinkId: () -> Int
    ) {
        val (px, py) = pos[node]!!
        val parentAccent = node.color ?: colors[colorIdx % colors.size]
        val pr = if (level == 0) radius * 1.25 else radius.toDouble()

        node.children.forEachIndexed { i, child ->
            val childIdx = colorIdx + i + 1
            val childAccent = child.color ?: colors[childIdx % colors.size]
            val (cx, cy) = pos[child]!!
            val cr = radius.toDouble()

            val linkStroke: String
            if (useGradients) {
                val gradId = "${svgId}_link_${nextLinkId()}"
                val x1: String; val y1: String; val x2: String; val y2: String
                if(orient == "vertical") {
                    x1 = "0%"; y1 = "0%"; x2 = "0%"; y2 = "100%"
                } else {
                    x1 = "0%"; y1 = "0%"; x2 = "100%"; y2 = "0%"
                }
                defs.append("<linearGradient id='$gradId' x1='$x1' y1='$y1' x2='$x2' y2='$y2'>")
                defs.append("<stop offset='0%' stop-color='$parentAccent' stop-opacity='1'/>")
                defs.append("<stop offset='100%' stop-color='$childAccent' stop-opacity='1'/>")
                defs.append("</linearGradient>")
                linkStroke = "url(#$gradId)"
            } else {
                linkStroke = childAccent
            }

            val d = if (orient == "vertical") {
                "M$px,${py + pr} C$px,${(py+cy)/2} $cx,${(py+cy)/2} $cx,${cy - cr}"
            } else {
                "M${px + pr},$py C${(px+cx)/2},$py ${(px+cx)/2},$cy ${cx - cr},$cy"
            }
            sb.append("<path class='link' d='$d' stroke='$linkStroke' />")
            drawLinks(sb, defs, child, pos, radius, orient, colors, childIdx, svgId, useGradients, level + 1, nextLinkId)

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
        svgId: String,
        animationOrder: Map<TreeNode, Int>,
        isCompact: Boolean = false
    ) {
        val (x, y) = pos[node]!!
        val accent = node.color ?: colors[colorIdx % colors.size]

        val effectiveRadius = if (level == 0) radius * 1.25 else radius.toDouble()
        val strokeWidth = if (level == 0) 3.5 else 2.0
        val glowFilter = if (level == 0) "url(#${svgId}_rootGlow)" else "url(#${svgId}_nodeGlow)"

        val lines = wrapTextByWidth(node.label, maxPixelWidth = (effectiveRadius * 2 - 14).toInt(), fontSize = if (isCompact) 10 else 11)
        val subLabelColor = if (useDark) "#9eb2d4" else "#4d678d"
        val revealDelay = ((animationOrder[node] ?: 0) * 46) + (level * 28)

        // Outer group handles layout translate, inner group handles animation (prevents transform conflicts)
        sb.append("<g class='node-layout' transform='translate($x,$y)'>")
        sb.append("<g class='node-anim' style='animation-delay: ${revealDelay}ms;'>")
        sb.append("<circle cx='0' cy='0' r='$effectiveRadius' fill='var(--tree-node-fill)' fill-opacity='var(--tree-node-fill-opacity)' stroke='$accent' stroke-width='$strokeWidth' filter='$glowFilter'/>")
        sb.append("<circle cx='0' cy='0' r='${effectiveRadius - 5}' fill='none' stroke='$accent' stroke-width='${if (level == 0) 1.0 else 0.7}' stroke-opacity='0.35'/>")

        val lineHeight = if (isCompact) 12 else 14
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
            drawNodes(
                sb = sb,
                node = child,
                pos = pos,
                colors = colors,
                radius = radius,
                level = level + 1,
                colorIdx = colorIdx + i + 1,
                useDark = useDark,
                nodeInnerFill = nodeInnerFill,
                svgId = svgId,
                animationOrder = animationOrder,
                isCompact = isCompact
            )
        }
    }


    private fun calculateDepth(node: TreeNode): Int = if (node.children.isEmpty()) 1 else 1 + node.children.maxOf { calculateDepth(it) }

    private fun buildTraversalOrder(root: TreeNode): Map<TreeNode, Int> {
        val order = mutableMapOf<TreeNode, Int>()
        var index = 0

        fun walk(node: TreeNode) {
            order[node] = index
            index += 1
            node.children.forEach { walk(it) }
        }

        walk(root)
        return order
    }

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



    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
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

    private fun wrapTitleByWidth(
        text: String,
        maxPixelWidth: Int,
        fontSize: Int,
        maxLines: Int
    ): List<String> {
        val words = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return listOf("")

        val lines = mutableListOf<String>()
        var current = ""

        words.forEach { word ->
            val candidate = if (current.isEmpty()) word else "$current $word"

            if (estimateTitleTextWidth(candidate, fontSize) <= maxPixelWidth) {
                current = candidate
            } else {
                if (current.isNotEmpty()) {
                    lines.add(current)
                }
                current = word
            }
        }

        if (current.isNotEmpty()) {
            lines.add(current)
        }

        if (lines.size <= maxLines) {
            return lines.map { line ->
                if (estimateTitleTextWidth(line, fontSize) <= maxPixelWidth) {
                    line
                } else {
                    ellipsizeTitle(line, maxPixelWidth, fontSize)
                }
            }
        }

        val visibleLines = lines.take(maxLines).toMutableList()
        val remainingText = lines.drop(maxLines - 1).joinToString(" ")
        visibleLines[maxLines - 1] = ellipsizeTitle(remainingText, maxPixelWidth, fontSize)
        return visibleLines
    }

    private fun ellipsize(text: String, maxPixelWidth: Int, fontSize: Int): String {
        if (estimateTextWidth(text, fontSize) <= maxPixelWidth) return text
        var out = text
        while (out.isNotEmpty() && estimateTextWidth("$out…", fontSize) > maxPixelWidth) {
            out = out.dropLast(1)
        }
        return if (out.isEmpty()) "…" else "$out…"
    }

    private fun ellipsizeTitle(text: String, maxPixelWidth: Int, fontSize: Int): String {
        if (estimateTitleTextWidth(text, fontSize) <= maxPixelWidth) return text

        var out = text
        while (out.isNotEmpty() && estimateTitleTextWidth("$out…", fontSize) > maxPixelWidth) {
            out = out.dropLast(1)
        }

        return if (out.isEmpty()) "…" else "$out…"
    }

    private fun estimateTextWidth(text: String, fontSize: Int): Int = (text.length * fontSize * 0.58).toInt()

    private fun estimateTitleTextWidth(text: String, fontSize: Int): Int = (text.length * fontSize * 0.62).toInt()

    private fun isValidHexColor(value: String): Boolean {
        return Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$").matches(value)
    }
}

@Serializable
data class TreeNode(val label: String, val color: String? = null, val children: MutableList<TreeNode> = mutableListOf()) {
    fun toCsv(): CsvResponse {
        val rows = mutableListOf<List<String>>()

        fun walk(node: TreeNode, parent: String, depth: Int, path: List<String>) {
            val currentPath = path + node.label

            rows.add(
                listOf(
                    node.label,
                    parent,
                    depth.toString(),
                    node.color.orEmpty(),
                    currentPath.joinToString(" > ")
                )
            )

            node.children.forEach { child ->
                walk(
                    node = child,
                    parent = node.label,
                    depth = depth + 1,
                    path = currentPath
                )
            }
        }

        walk(
            node = this,
            parent = "",
            depth = 0,
            path = emptyList()
        )

        return CsvResponse(
            listOf("Label", "Parent", "Depth", "Color", "Path"),
            rows
        )
    }
}
