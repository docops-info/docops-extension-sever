package io.docops.docopsextensionssupport.diagram

import java.util.UUID
import kotlin.math.max

class TreeMaker {

    // Modern color palette for tree chart
    private val defaultColors = listOf(
        "#3498db", // Blue
        "#2ecc71", // Green
        "#e74c3c", // Red
        "#f39c12", // Orange
        "#9b59b6", // Purple
        "#1abc9c", // Turquoise
        "#34495e", // Dark Blue
        "#e67e22", // Dark Orange
        "#27ae60", // Dark Green
        "#d35400"  // Burnt Orange
    )

    fun makeTree(payload: String, isPdf: Boolean = false): String {

        // Parse configuration and data from content
        val (config, chartData) = parseConfigAndData(payload)

        // Get configuration values from content or fall back to attributes
        val title = config.getOrDefault("title", "Tree Chart")
        val width = config.getOrDefault("width", "800")
        val height = config.getOrDefault("height", "600")
        val enableHoverEffects = config["hover"]?.toBoolean() ?: true
        val darkMode = config["darkMode"]?.toBoolean() ?: false
        // Override collapsible setting if isPdf is true
        val collapsible = if (isPdf) false else (config["collapsible"]?.toBoolean() ?: true)
        // For PDF, always show fully expanded tree
        val initiallyExpanded = if (isPdf) true else (config["expanded"]?.toBoolean() ?: true)
        val orientation = config.getOrDefault("orientation", "vertical")
        // Parse colors from config or attributes
        val configColors = config["colors"]?.split(",")?.map { it.trim() }
        val customColors = configColors

        val treeData = parseTreeChartData(chartData)

        // Calculate required dimensions based on tree structure
        val calculatedDimensions = calculateRequiredDimensions(treeData, orientation)
        val adjustedWidth = max(width.toInt(), calculatedDimensions.first)
        val adjustedHeight = max(height.toInt(), calculatedDimensions.second)

        // Generate SVG
        val svg = generateTreeChartSvg(
            treeData,
            title,
            width.toInt(),
            height.toInt(),
            customColors ?: defaultColors,
            enableHoverEffects,
            darkMode,
            collapsible,
            initiallyExpanded,
            orientation,
            isPdf, adjustedWidth, adjustedHeight
        )
        return svg
    }

    /**
     * Wraps text to fit within the specified character limit per line.
     */
    private fun wrapText(text: String, maxCharsPerLine: Int = 12): List<String> {
        if (text.length <= maxCharsPerLine) {
            return listOf(text)
        }

        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            // If adding this word would exceed the limit
            if (currentLine.length + word.length + (if (currentLine.isNotEmpty()) 1 else 0) > maxCharsPerLine) {
                // If current line has content, save it and start new line
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder(word)
                } else {
                    // Single word is too long, break it
                    if (word.length > maxCharsPerLine) {
                        lines.add(word.substring(0, maxCharsPerLine))
                        currentLine = StringBuilder(word.substring(maxCharsPerLine))
                    } else {
                        currentLine.append(word)
                    }
                }
            } else {
                // Add word to current line
                if (currentLine.isNotEmpty()) {
                    currentLine.append(" ")
                }
                currentLine.append(word)
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }

        return lines
    }

    /**
     * Calculate the minimum required dimensions for the tree
     */
    private fun calculateRequiredDimensions(root: TreeNode, orientation: String): Pair<Int, Int> {
        // Calculate the maximum width needed at any level
        val levelWidths = mutableListOf<Int>()

        fun calculateLevelWidths(node: TreeNode, level: Int) {
            if (levelWidths.size <= level) {
                levelWidths.add(0)
            }
            levelWidths[level]++

            node.children.forEach { child ->
                calculateLevelWidths(child, level + 1)
            }
        }

        calculateLevelWidths(root, 0)

        val maxWidth = levelWidths.maxOrNull() ?: 1
        val depth = levelWidths.size

        return if (orientation == "vertical") {
            // For vertical layout: width depends on max nodes at any level, height on depth
            val requiredWidth = max(800, maxWidth * 140 + 100) // Increased to 140px per node + padding for wrapped text
            val requiredHeight = max(600, depth * 140 + 200) // Increased to 140px per level + padding for wrapped text
            Pair(requiredWidth, requiredHeight)
        } else {
            // For horizontal layout: height depends on max nodes at any level, width on depth
            val requiredWidth = max(800, depth * 140 + 200)
            val requiredHeight = max(600, maxWidth * 140 + 100)
            Pair(requiredWidth, requiredHeight)
        }
    }

    /**
     * Parses the content to extract configuration parameters and chart data.
     * Configuration parameters are specified at the beginning of the content in the format "key=value",
     * followed by a separator line "---", and then the actual chart data.
     *
     * @param content The full content of the block
     * @return A Pair containing the configuration map and the chart data string
     */
    private fun parseConfigAndData(content: String): Pair<Map<String, String>, String> {
        val lines = content.lines()
        val config = mutableMapOf<String, String>()
        var separatorIndex = -1

        // Find the separator line and parse configuration
        for (i in lines.indices) {
            val line = lines[i].trim()
            if (line == "---") {
                separatorIndex = i
                break
            }

            // Parse key=value pairs
            val keyValuePair = line.split("=", limit = 2)
            if (keyValuePair.size == 2) {
                val key = keyValuePair[0].trim()
                val value = keyValuePair[1].trim()
                if (key.isNotEmpty()) {
                    config[key] = value
                }
            }
        }

        // Extract chart data
        val chartData = if (separatorIndex >= 0) {
            lines.subList(separatorIndex + 1, lines.size).joinToString("\n")
        } else {
            // If no separator is found, assume the entire content is chart data
            content
        }

        return Pair(config, chartData)
    }

    private fun parseTreeChartData(content: String): TreeNode {
        val lines = content.lines().filter { it.isNotBlank() }

        // Create root node
        val rootLine = lines.firstOrNull() ?: throw IllegalArgumentException("No data provided for tree chart")
        val rootParts = rootLine.split("|").map { it.trim() }
        val rootLabel = rootParts[0]
        val rootColor = if (rootParts.size > 1 && rootParts[1].isNotBlank()) rootParts[1] else null

        val root = TreeNode(rootLabel, color = rootColor)

        // Parse the rest of the tree using indentation to determine parent-child relationships
        val nodeStack = mutableListOf<Pair<TreeNode, Int>>() // Node and its indentation level
        nodeStack.add(Pair(root, 0))

        for (i in 1 until lines.size) {
            val line = lines[i]
            val indentCount = line.indexOfFirst { !it.isWhitespace() }
            if (indentCount < 0) continue // Skip empty lines

            val trimmedLine = line.trim()
            val parts = trimmedLine.split("|").map { it.trim() }
            val label = parts[0]
            val color = if (parts.size > 1 && parts[1].isNotBlank()) parts[1] else null

            val newNode = TreeNode(label, color = color)

            // Find the parent node based on indentation
            while (nodeStack.isNotEmpty() && nodeStack.last().second >= indentCount) {
                nodeStack.removeAt(nodeStack.size - 1)
            }

            if (nodeStack.isEmpty()) {
                // If we've popped all nodes, use root as parent
                root.children.add(newNode)
                nodeStack.add(Pair(newNode, indentCount))
            } else {
                // Add to the current parent
                val parent = nodeStack.last().first
                parent.children.add(newNode)
                nodeStack.add(Pair(newNode, indentCount))
            }
        }

        return root
    }

    private fun generateTreeChartSvg(
        root: TreeNode,
        title: String,
        width: Int,
        height: Int,
        colors: List<String>,
        enableHoverEffects: Boolean,
        darkMode: Boolean = false,
        collapsible: Boolean = true,
        initiallyExpanded: Boolean = true,
        orientation: String = "vertical",
        isPdf: Boolean = false, adjustedWidth: Int, adjustedHeight: Int
    ): String {
        // Define colors based on dark mode
        val backgroundColor = if (darkMode) "#1e293b" else "transparent"
        val textColor = if (darkMode) "#f8fafc" else "#000000"
        val linkColor = if (darkMode) "#64748b" else "#94a3b8"
        val nodeStrokeColor = if (darkMode) "#334155" else "#e2e8f0"

        val svgBuilder = StringBuilder()

        val id = UUID.randomUUID().toString()
        // Start SVG
        svgBuilder.append("<svg id='treeChart_$id' width='$width' height='$height' xmlns='http://www.w3.org/2000/svg' preserveAspectRatio='xMidYMid meet' viewBox='0 0 $adjustedWidth $adjustedHeight'>")

        // Add background if in dark mode
        if (darkMode) {
            svgBuilder.append("<rect width='$width' height='$height' fill='$backgroundColor' />")
        }

        // Add title
        svgBuilder.append("<text x='${width / 2}' y='30' font-family='Arial' font-size='20' text-anchor='middle' font-weight='bold' fill='$textColor'>$title</text>")

        // Add CSS styles
        svgBuilder.append(
            """
            <style>
                .node {
                    ${if (collapsible && !isPdf) "cursor: pointer;" else ""}
                    transition: transform 0.2s, filter 0.2s;
                }
                .node:hover {
                    filter: brightness(1.1);
                }
                .node-circle {
                    stroke: $nodeStrokeColor;
                    stroke-width: 2px;
                }
                .node-label {
                    font-family: Arial;
                    font-size: 11px;
                    text-anchor: middle;
                    dominant-baseline: middle;
                    pointer-events: none;
                }
                .link {
                    fill: none;
                    stroke: $linkColor;
                    stroke-width: 1.5px;
                }
                .collapse-icon {
                    fill: $textColor;
                    font-family: Arial;
                    font-size: 10px;
                    text-anchor: middle;
                    dominant-baseline: middle;
                    ${if (collapsible && !isPdf) "cursor: pointer;" else ""}
                }
            </style>
        """.trimIndent()
        )

        // For PDF, generate static SVG without JavaScript
        if (isPdf) {
            svgBuilder.append(generateStaticTreeSvg(root, colors, width, height, textColor, orientation))
        } else {
            // Add JavaScript for tree layout and interactivity
            svgBuilder.append(generateInteractiveTreeSvg(root, colors, width, height, textColor, orientation, collapsible, initiallyExpanded, id))
        }

        svgBuilder.append("</svg>")
        return svgBuilder.toString()
    }

    private fun generateStaticTreeSvg(
        root: TreeNode,
        colors: List<String>,
        width: Int,
        height: Int,
        textColor: String,
        orientation: String
    ): String {
        val margin = mapOf("top" to 60, "right" to 40, "bottom" to 30, "left" to 40)
        val innerWidth = width - margin["left"]!! - margin["right"]!!
        val innerHeight = height - margin["top"]!! - margin["bottom"]!!
        val nodeRadius = 20 // Increased radius for wrapped text

        val svgBuilder = StringBuilder()
        svgBuilder.append("<g transform='translate(${margin["left"]}, ${margin["top"]})'>")

        // Build level map for positioning
        val levelNodes = mutableMapOf<Int, MutableList<TreeNode>>()
        fun buildLevelMap(node: TreeNode, level: Int) {
            levelNodes.getOrPut(level) { mutableListOf() }.add(node)
            node.children.forEach { child ->
                buildLevelMap(child, level + 1)
            }
        }
        buildLevelMap(root, 0)

        // Calculate positions for all nodes
        val nodePositions = mutableMapOf<TreeNode, Pair<Double, Double>>()
        fun calculatePositions(node: TreeNode, level: Int) {
            val currentLevelNodes = levelNodes[level]!!
            val indexInLevel = currentLevelNodes.indexOf(node)
            val totalNodesInLevel = currentLevelNodes.size

            val (x, y) = if (orientation == "vertical") {
                val y = level * 120.0 // Increased spacing for wrapped text
                val x = if (totalNodesInLevel == 1) {
                    innerWidth / 2.0
                } else {
                    val spacing = innerWidth.toDouble() / (totalNodesInLevel + 1)
                    spacing * (indexInLevel + 1)
                }
                Pair(x, y)
            } else {
                val x = level * 140.0 // Increased spacing for wrapped text
                val y = if (totalNodesInLevel == 1) {
                    innerHeight / 2.0
                } else {
                    val spacing = innerHeight.toDouble() / (totalNodesInLevel + 1)
                    spacing * (indexInLevel + 1)
                }
                Pair(x, y)
            }

            nodePositions[node] = Pair(x, y)
            node.children.forEach { child ->
                calculatePositions(child, level + 1)
            }
        }
        calculatePositions(root, 0)

        // Draw links first
        fun drawLinks(node: TreeNode) {
            val (parentX, parentY) = nodePositions[node]!!
            node.children.forEach { child ->
                val (childX, childY) = nodePositions[child]!!

                val pathData = if (orientation == "vertical") {
                    val midY = (parentY + childY) / 2
                    "M$parentX,${parentY + nodeRadius} C$parentX,$midY $childX,$midY $childX,${childY - nodeRadius}"
                } else {
                    val midX = (parentX + childX) / 2
                    "M${parentX + nodeRadius},$parentY C$midX,$parentY $midX,$childY ${childX - nodeRadius},$childY"
                }

                svgBuilder.append("<path class='link' d='$pathData' />")
                drawLinks(child)
            }
        }
        drawLinks(root)

        // Draw nodes with wrapped text
        fun drawNodes(node: TreeNode, colorIndex: Int) {
            val (x, y) = nodePositions[node]!!
            val color = node.color ?: colors[colorIndex % colors.size]
            val wrappedText = wrapText(node.label)

            svgBuilder.append("<g class='node' transform='translate($x, $y)'>")
            svgBuilder.append("<circle class='node-circle' r='$nodeRadius' fill='$color' />")

            // Render wrapped text lines
            val lineHeight = 12
            val startY = if (wrappedText.size == 1) 0 else -(wrappedText.size - 1) * lineHeight / 2

            wrappedText.forEachIndexed { index, line ->
                val yOffset = startY + (index * lineHeight)
                svgBuilder.append("""
                    <text class='node-label' fill='$textColor' y='$yOffset'>${line.replace("'", "&apos;").replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")}</text>
                """.trimIndent())
            }

            svgBuilder.append("</g>")

            node.children.forEachIndexed { index, child ->
                drawNodes(child, colorIndex + index + 1)
            }
        }
        drawNodes(root, 0)

        svgBuilder.append("</g>")
        return svgBuilder.toString()
    }

    private fun generateInteractiveTreeSvg(
        root: TreeNode,
        colors: List<String>,
        width: Int,
        height: Int,
        textColor: String,
        orientation: String,
        collapsible: Boolean,
        initiallyExpanded: Boolean,
        id: String
    ): String {
        return """
            <script type="text/javascript">
            <![CDATA[
            (function() {
                // Text wrapping function
                function wrapText(text, maxCharsPerLine = 12) {
                    if (text.length <= maxCharsPerLine) {
                        return [text];
                    }
                    
                    const words = text.split(' ');
                    const lines = [];
                    let currentLine = '';

                    for (const word of words) {
                        if (currentLine.length + word.length + (currentLine.length > 0 ? 1 : 0) > maxCharsPerLine) {
                            if (currentLine.length > 0) {
                                lines.push(currentLine);
                                currentLine = word;
                            } else {
                                if (word.length > maxCharsPerLine) {
                                    lines.push(word.substring(0, maxCharsPerLine));
                                    currentLine = word.substring(maxCharsPerLine);
                                } else {
                                    currentLine = word;
                                }
                            }
                        } else {
                            if (currentLine.length > 0) {
                                currentLine += ' ';
                            }
                            currentLine += word;
                        }
                    }

                    if (currentLine.length > 0) {
                        lines.push(currentLine);
                    }

                    return lines;
                }

                // Tree data
                const treeData = ${serializeTreeToJson(root, colors)};

                // Constants for layout
                const margin = {top: 60, right: 40, bottom: 30, left: 40};
                const innerWidth = $width - margin.left - margin.right;
                const innerHeight = $height - margin.top - margin.bottom;
                const nodeRadius = 20; // Increased for wrapped text
                const orientation = "$orientation";
                const initiallyExpanded = $initiallyExpanded;
                const collapsible = $collapsible;
                const treeChartId = "#treeChart_$id";

                // Create the tree layout
                function createTreeLayout() {
                    const g = document.createElementNS("http://www.w3.org/2000/svg", "g");
                    g.setAttribute("transform", "translate("+ margin.left+ ","+margin.top+")");
                    document.querySelector(treeChartId).appendChild(g);

                    // Process the data to create a tree structure
                    const root = treeData;
                    root.x0 = innerWidth / 2;
                    root.y0 = 0;
                    root.parent = null;

                    // Initialize children visibility based on initiallyExpanded
                    function initialize(node) {
                        if (node.children) {
                            node._children = node.children;
                            if (!initiallyExpanded) {
                                node.children = null;
                            }
                            node._children.forEach(child => {
                                child.parent = node;
                                initialize(child);
                            });
                        }
                    }

                    if (collapsible) {
                        initialize(root);
                    }

                    update(root);

                    function update(source) {
                        const nodes = [];
                        const links = [];
                        const levelNodes = {};
                        
                        function buildLevelMap(node, level) {
                            if (!levelNodes[level]) {
                                levelNodes[level] = [];
                            }
                            levelNodes[level].push(node);
                            
                            if (node.children) {
                                node.children.forEach(child => {
                                    buildLevelMap(child, level + 1);
                                });
                            }
                        }
                        
                        buildLevelMap(root, 0);

                        function calculateNodePositions(node, level, indexInLevel = 0) {
                            nodes.push(node);
                            const currentLevelNodes = levelNodes[level];
                            const totalNodesInLevel = currentLevelNodes.length;

                            if (orientation === "vertical") {
                                node.y = level * 120; // Increased spacing
                                if (totalNodesInLevel === 1) {
                                    node.x = innerWidth / 2;
                                } else {
                                    const spacing = innerWidth / (totalNodesInLevel + 1);
                                    node.x = spacing * (indexInLevel + 1);
                                }
                            } else {
                                node.x = level * 140; // Increased spacing
                                if (totalNodesInLevel === 1) {
                                    node.y = innerHeight / 2;
                                } else {
                                    const spacing = innerHeight / (totalNodesInLevel + 1);
                                    node.y = spacing * (indexInLevel + 1);
                                }
                            }

                            if (node.children) {
                                node.children.forEach((child, i) => {
                                    child.parent = node;
                                    const childLevel = level + 1;
                                    const childLevelNodes = levelNodes[childLevel];
                                    const childIndexInLevel = childLevelNodes.indexOf(child);
                                    calculateNodePositions(child, childLevel, childIndexInLevel);
                                });
                            }
                        }

                        calculateNodePositions(root, 0, 0);

                        nodes.forEach(function(d) {
                            if (d.parent) {
                                links.push({
                                    source: d.parent,
                                    target: d
                                });
                            }
                        });

                        g.innerHTML = "";

                        // Add links
                        links.forEach(function(d) {
                            const path = document.createElementNS("http://www.w3.org/2000/svg", "path");
                            path.setAttribute("class", "link");

                            let pathData;
                            if (orientation === "vertical") {
                                const midY = (d.source.y + d.target.y) / 2;
                                pathData = "M" + d.source.x + "," + (d.source.y + nodeRadius) + 
                                          " C" + d.source.x + "," + midY + 
                                          " " + d.target.x + "," + midY + 
                                          " " + d.target.x + "," + (d.target.y - nodeRadius);
                            } else {
                                const midX = (d.source.x + d.target.x) / 2;
                                pathData = "M" + (d.source.x + nodeRadius) + "," + d.source.y + 
                                          " C" + midX + "," + d.source.y + 
                                          " " + midX + "," + d.target.y + 
                                          " " + (d.target.x - nodeRadius) + "," + d.target.y;
                            }

                            path.setAttribute("d", pathData);
                            g.appendChild(path);
                        });

                        // Add nodes with wrapped text
                        nodes.forEach(function(d) {
                            const nodeGroup = document.createElementNS("http://www.w3.org/2000/svg", "g");
                            nodeGroup.setAttribute("class", "node");
                            nodeGroup.setAttribute("transform", "translate("+d.x +","+d.y+")");

                            if (collapsible) {
                                nodeGroup.onclick = function() {
                                    if (d.children) {
                                        d._children = d.children;
                                        d.children = null;
                                    } else if (d._children) {
                                        d.children = d._children;
                                        d.children.forEach(child => {
                                            child.parent = d;
                                        });
                                    }
                                    update(d);
                                };
                            }

                            // Node circle
                            const circle = document.createElementNS("http://www.w3.org/2000/svg", "circle");
                            circle.setAttribute("class", "node-circle");
                            circle.setAttribute("r", nodeRadius);
                            circle.setAttribute("fill", d.color);
                            nodeGroup.appendChild(circle);

                            // Wrapped text
                            const wrappedLines = wrapText(d.name);
                            const lineHeight = 12;
                            const startY = wrappedLines.length === 1 ? 0 : -(wrappedLines.length - 1) * lineHeight / 2;
                            
                            wrappedLines.forEach((line, index) => {
                                const text = document.createElementNS("http://www.w3.org/2000/svg", "text");
                                text.setAttribute("class", "node-label");
                                text.setAttribute("fill", "$textColor");
                                text.setAttribute("y", startY + (index * lineHeight));
                                text.textContent = line;
                                nodeGroup.appendChild(text);
                            });

                            // Add collapse/expand icon if needed
                            if (collapsible && (d.children || d._children)) {
                                const icon = document.createElementNS("http://www.w3.org/2000/svg", "text");
                                icon.setAttribute("class", "collapse-icon");
                                icon.setAttribute("x", 0);
                                icon.setAttribute("y", nodeRadius + 15);
                                icon.textContent = d.children ? "-" : "+";
                                nodeGroup.appendChild(icon);
                            }

                            g.appendChild(nodeGroup);
                        });
                    }
                }

                document.addEventListener("DOMContentLoaded", createTreeLayout);
                if (document.readyState === "complete" || document.readyState === "interactive") {
                    setTimeout(createTreeLayout, 1);
                }
            })();
            ]]>
            </script>
        """.trimIndent()
    }

    private fun serializeTreeToJson(node: TreeNode, colors: List<String>, colorIndex: Int = 0): String {
        val color = node.color ?: colors[colorIndex % colors.size]
        val childrenJson = if (node.children.isNotEmpty()) {
            node.children.mapIndexed { index, child ->
                serializeTreeToJson(child, colors, (colorIndex + index + 1) % colors.size)
            }.joinToString(", ", prefix = "[", postfix = "]")
        } else {
            "null"
        }

        return """
            {
                "name": "${node.label.replace("\"", "\\\"")}", 
                "color": "$color", 
                "children": $childrenJson
            }
        """.trimIndent()
    }

    private data class TreeNode(
        val label: String,
        val color: String? = null,
        val children: MutableList<TreeNode> = mutableListOf()
    )
}