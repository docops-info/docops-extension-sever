package io.docops.docopsextensionssupport.todo

import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.DocOpsContext
import io.docops.docopsextensionssupport.web.update
import kotlinx.serialization.json.Json
import java.util.*

/**
 * Creates SVG visualizations for todo lists
 */
class TodoMaker(private val csvResponse: CsvResponse, val useDark: Boolean) {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Create todo visualization from JSON format
     */
    fun createFromJson(data: String, context: DocOpsContext): String {
        val todoList = try {
            json.decodeFromString<TodoList>(data)
        } catch (e: Exception) {
            createDefaultTodoList()
        }

        csvResponse.update(todoList.toCsv())
        return generateSvg(todoList, context)
    }

    /**
     * Create todo visualization from table format
     */
    fun createFromTable(data: String, context: DocOpsContext): String {
        val todoList = parseTableData(data)
        csvResponse.update(todoList.toCsv())
        return generateSvg(todoList, context)
    }

    /**
     * Parse table format data into TodoList
     */
    private fun parseTableData(data: String): TodoList {
        val lines = data.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val items = mutableListOf<TodoItem>()
        var config = TodoConfig()
        var inDataSection = false

        for (line in lines) {
            when {
                line.startsWith("title:") -> {
                    config = config.copy(title = line.substringAfter(":").trim())
                }
                line.startsWith("layout=") -> {
                    val layoutValue = line.substringAfter("=").trim()
                    config = config.copy(layout = Layout.valueOf(layoutValue.uppercase()))
                }
                line.startsWith("groupBy=") -> {
                    val groupValue = line.substringAfter("=").trim()
                    config = config.copy(groupBy = GroupBy.valueOf(groupValue.uppercase()))
                }
                line.startsWith("useGlass=") -> {
                    val glassValue = line.substringAfter("=").trim().lowercase() == "true"
                    config = config.copy(useGlass = glassValue)
                }
                line == "---" -> inDataSection = true
                inDataSection && line.contains("|") && !isHeaderRow(line) -> {
                    parseTableRow(line)?.let { items.add(it) }
                }
            }
        }

        return TodoList(config = config, items = items)
    }

    /**
     * Parse a single table row into a TodoItem
     */
    private fun parseTableRow(line: String): TodoItem? {
        val parts = line.split("|").map { it.trim() }

        if (parts.size < 2) return null

        val task = parts[0]
        val status = if (parts.size > 1) parseStatus(parts[1]) else TodoStatus.PENDING
        val priority = if (parts.size > 2) parsePriority(parts[2]) else TodoPriority.MEDIUM
        val category = if (parts.size > 3 && parts[3].isNotEmpty()) parts[3] else null
        val assignee = if (parts.size > 4 && parts[4].isNotEmpty()) parts[4] else null
        val dueDate = if (parts.size > 5 && parts[5].isNotEmpty()) parts[5] else null
        val tags = if (parts.size > 6) parts[6].split(",").map { it.trim() }.filter { it.isNotEmpty() } else emptyList()
        val description = if (parts.size > 7 && parts[7].isNotEmpty()) parts[7] else null

        return TodoItem(
            task = task,
            status = status,
            priority = priority,
            category = category,
            assignee = assignee,
            dueDate = dueDate,
            tags = tags,
            description = description
        )
    }

    /**
     * Parse status from string
     */
    private fun parseStatus(statusStr: String): TodoStatus {
        return when (statusStr.lowercase().trim()) {
            "pending", "todo", "⏳" -> TodoStatus.PENDING
            "in_progress", "in progress", "working", "🔄" -> TodoStatus.IN_PROGRESS
            "completed", "done", "finished", "✅" -> TodoStatus.COMPLETED
            "blocked", "stuck", "🚫" -> TodoStatus.BLOCKED
            "cancelled", "canceled", "❌" -> TodoStatus.CANCELLED
            "on_hold", "on hold", "paused", "⏸️" -> TodoStatus.ON_HOLD
            else -> TodoStatus.PENDING
        }
    }

    /**
     * Parse priority from string
     */
    private fun parsePriority(priorityStr: String): TodoPriority {
        return when (priorityStr.lowercase().trim()) {
            "low", "l", "🔵" -> TodoPriority.LOW
            "medium", "med", "m", "🟡" -> TodoPriority.MEDIUM
            "high", "h", "🟠" -> TodoPriority.HIGH
            "critical", "crit", "c", "urgent", "🔴" -> TodoPriority.CRITICAL
            else -> TodoPriority.MEDIUM
        }
    }

    /**
     * Check if line is a header row
     */
    private fun isHeaderRow(line: String): Boolean {
        val lowerLine = line.lowercase()
        return lowerLine.contains("task") || lowerLine.contains("status") || lowerLine.contains("priority")
    }

    /**
     * Create default todo list for error cases
     */
    private fun createDefaultTodoList(): TodoList {
        return TodoList(
            config = TodoConfig(title = "Sample Todo List"),
            items = listOf(
                TodoItem(task = "Review project requirements", status = TodoStatus.COMPLETED, priority = TodoPriority.HIGH),
                TodoItem(task = "Create design mockups", status = TodoStatus.IN_PROGRESS, priority = TodoPriority.MEDIUM),
                TodoItem(task = "Implement user authentication", status = TodoStatus.PENDING, priority = TodoPriority.HIGH),
                TodoItem(task = "Write unit tests", status = TodoStatus.PENDING, priority = TodoPriority.MEDIUM)
            )
        )
    }

    /**
     * Generate SVG based on layout type
     */
    private fun generateSvg(todoList: TodoList, context: DocOpsContext): String {
        return when (todoList.config.layout) {
            Layout.KANBAN -> generateKanbanSvg(todoList, context)
            Layout.LIST -> generateListSvg(todoList, context)
            Layout.GRID -> generateGridSvg(todoList, context)
            Layout.TIMELINE -> generateTimelineSvg(todoList, context)
        }
    }

    /**
     * Generate Kanban board style SVG
     */
    private fun generateKanbanSvg(todoList: TodoList, context: DocOpsContext): String {
        val id = UUID.randomUUID().toString()
        val statusColumns = TodoStatus.values().toList()
        val groupedItems = todoList.items.groupBy { it.status }

        // Calculate dynamic column width based on status names
        val maxStatusNameLength = statusColumns.maxOfOrNull { formatStatusName(it).length } ?: 10
        val minColumnWidth = 200
        val baseColumnWidth = kotlin.math.max(minColumnWidth, maxStatusNameLength * 12 + 60)
        val columnWidth = kotlin.math.min(baseColumnWidth, 280) // Cap at 280

        val margin = 15
        val cardHeight = 120
        val totalColumns = statusColumns.size
        val width = totalColumns * columnWidth + (totalColumns + 1) * margin
        val height = 800

        val isDark = useDark
        val bgColor = if (isDark) "#1a1a1a" else "#ffffff"
        val textColor = if (isDark) "#e0e0e0" else "#2d3748"
        val columnBg = if (isDark) "#2d2d2d" else "#f7fafc"

        val svg = StringBuilder()
        svg.append("""
            <svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height">
                <defs>
                    ${generateDefs(isDark, todoList.config.useGlass)}
                </defs>
                
                <rect width="$width" height="$height" fill="$bgColor"/>
                
                <!-- Title -->
                <text x="${width / 2}" y="40" text-anchor="middle" fill="$textColor" font-size="24" font-weight="bold" font-family="Arial, sans-serif">
                    ${escapeXml(todoList.config.title)}
                </text>
        """.trimIndent())

        // Draw columns
        statusColumns.forEachIndexed { colIndex, status ->
            val x = margin + colIndex * (columnWidth + margin)
            val y = 70
            val items = groupedItems[status] ?: emptyList()
            val statusName = formatStatusName(status)

            // Column background
            svg.append("""
                <rect x="$x" y="$y" width="$columnWidth" height="${height - y - margin}" 
                      fill="$columnBg" stroke="${if (isDark) "#404040" else "#e2e8f0"}" stroke-width="1" 
                      rx="8" class="${if (todoList.config.useGlass) "glass" else ""}"/>
                
                <!-- Column Title with proper text wrapping -->
                <text x="${x + columnWidth / 2}" y="${y + 25}" text-anchor="middle" fill="$textColor" 
                      font-size="14" font-weight="bold" font-family="Arial, sans-serif">
                    ${escapeXml(statusName)}
                </text>
                <text x="${x + columnWidth / 2}" y="${y + 40}" text-anchor="middle" fill="${if (isDark) "#a0a0a0" else "#718096"}" 
                      font-size="12" font-family="Arial, sans-serif">
                    (${items.size})
                </text>
            """.trimIndent())

            // Draw cards
            items.forEachIndexed { cardIndex, item ->
                val cardY = y + 55 + cardIndex * (cardHeight + 10)
                generateTodoCard(svg, item, x + 8, cardY, columnWidth - 16, cardHeight - 10, isDark, todoList.config)
            }
        }

        // Add statistics if enabled
        if (todoList.config.showStats) {
            generateStats(svg, todoList, width - 200, 100, isDark)
        }

        svg.append("</svg>")
        return svg.toString()
    }

    /**
     * Generate List style SVG
     */
    private fun generateListSvg(todoList: TodoList, context: DocOpsContext): String {
        val id = UUID.randomUUID().toString()
        val width = 800
        val itemHeight = 60
        val height = 150 + todoList.items.size * itemHeight
        val margin = 20

        val isDark = useDark
        val bgColor = if (isDark) "#1a1a1a" else "#ffffff"
        val textColor = if (isDark) "#e0e0e0" else "#2d3748"

        val svg = StringBuilder()
        svg.append("""
            <svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height">
                <defs>
                    ${generateDefs(isDark, todoList.config.useGlass)}
                </defs>
                
                <rect width="$width" height="$height" fill="$bgColor"/>
                
                <!-- Title -->
                <text x="${width / 2}" y="40" text-anchor="middle" fill="$textColor" font-size="24" font-weight="bold" font-family="Arial, sans-serif">
                    ${todoList.config.title}
                </text>
        """.trimIndent())

        // Sort and display items
        val sortedItems = sortItems(todoList.items, todoList.config.sortBy)

        sortedItems.forEachIndexed { index, item ->
            val y = 80 + index * itemHeight
            generateListItem(svg, item, margin, y, width - 2 * margin, itemHeight - 10, isDark, todoList.config)
        }

        svg.append("</svg>")
        return svg.toString()
    }

    /**
     * Generate Grid style SVG
     */
    private fun generateGridSvg(todoList: TodoList, context: DocOpsContext): String {
        val id = UUID.randomUUID().toString()
        val cardWidth = 250
        val cardHeight = 150
        val margin = 20
        val cols = 4
        val rows = (todoList.items.size + cols - 1) / cols
        val width = cols * (cardWidth + margin) + margin
        val height = 100 + rows * (cardHeight + margin) + margin

        val isDark = useDark
        val bgColor = if (isDark) "#1a1a1a" else "#ffffff"
        val textColor = if (isDark) "#e0e0e0" else "#2d3748"

        val svg = StringBuilder()
        svg.append("""
            <svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height">
                <defs>
                    ${generateDefs(isDark, todoList.config.useGlass)}
                </defs>
                
                <rect width="$width" height="$height" fill="$bgColor"/>
                
                <!-- Title -->
                <text x="${width / 2}" y="40" text-anchor="middle" fill="$textColor" font-size="24" font-weight="bold" font-family="Arial, sans-serif">
                    ${todoList.config.title}
                </text>
        """.trimIndent())

        // Draw grid items
        todoList.items.forEachIndexed { index, item ->
            val col = index % cols
            val row = index / cols
            val x = margin + col * (cardWidth + margin)
            val y = 80 + row * (cardHeight + margin)

            generateTodoCard(svg, item, x, y, cardWidth, cardHeight, isDark, todoList.config)
        }

        svg.append("</svg>")
        return svg.toString()
    }

    /**
     * Generate Timeline style SVG
     */
    private fun generateTimelineSvg(todoList: TodoList, context: DocOpsContext): String {
        // Similar to other layouts but arranged in a timeline format
        return generateListSvg(todoList, context) // Simplified for now
    }

    /**
     * Generate a todo card
     */
    private fun generateTodoCard(
        svg: StringBuilder,
        item: TodoItem,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        isDark: Boolean,
        config: TodoConfig
    ) {
        val cardBg = if (isDark) "#2d3748" else "#ffffff"
        val textColor = if (isDark) "#e0e0e0" else "#2d3748"
        val borderColor = getPriorityColor(item.priority)

        svg.append("""
            <rect x="$x" y="$y" width="$width" height="$height" 
                  fill="$cardBg" stroke="$borderColor" stroke-width="2" 
                  rx="8" class="${if (config.useGlass) "glass" else ""}"/>
            
            <!-- Status Icon -->
            <text x="${x + 12}" y="${y + 22}" fill="${getStatusColor(item.status)}" 
                  font-size="16" font-family="Arial, sans-serif">
                ${getStatusIcon(item.status)}
            </text>
            
            <!-- Task Title with proper wrapping -->
            <text x="${x + 35}" y="${y + 22}" fill="$textColor" font-size="13" font-weight="bold" 
                  font-family="Arial, sans-serif">
                ${escapeXml(truncateText(item.task, getMaxTaskLength(width)))}
            </text>
            
            <!-- Priority Badge -->
            <rect x="${x + width - 55}" y="${y + 8}" width="45" height="18" 
                  fill="${getPriorityColor(item.priority)}" rx="9"/>
            <text x="${x + width - 32}" y="${y + 19}" fill="white" font-size="9" 
                  text-anchor="middle" font-family="Arial, sans-serif">
                ${item.priority.name.take(4)}
            </text>
        """.trimIndent())

        var currentY = y + 45

        // Add category if present
        if (item.category != null && config.showCategories) {
            svg.append("""
                <text x="${x + 12}" y="$currentY" fill="${if (isDark) "#a0a0a0" else "#718096"}" 
                      font-size="11" font-family="Arial, sans-serif">
                    📁 ${escapeXml(truncateText(item.category, getMaxFieldLength(width)))}
                </text>
            """.trimIndent())
            currentY += 16
        }

        // Add assignee if present and space allows
        if (item.assignee != null && config.showAssignees && currentY < y + height - 20) {
            svg.append("""
                <text x="${x + 12}" y="$currentY" fill="${if (isDark) "#a0a0a0" else "#718096"}" 
                      font-size="11" font-family="Arial, sans-serif">
                    👤 ${escapeXml(truncateText(item.assignee, getMaxFieldLength(width)))}
                </text>
            """.trimIndent())
            currentY += 16
        }

        // Add due date if present and space allows
        if (item.dueDate != null && config.showDueDates && currentY < y + height - 8) {
            svg.append("""
                <text x="${x + width - 12}" y="${y + height - 8}" fill="${if (isDark) "#a0a0a0" else "#718096"}" 
                      font-size="10" text-anchor="end" font-family="Arial, sans-serif">
                    📅 ${escapeXml(item.dueDate)}
                </text>
            """.trimIndent())
        }
    }


    /**
     * Generate a list item
     */
    private fun generateListItem(
        svg: StringBuilder,
        item: TodoItem,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        isDark: Boolean,
        config: TodoConfig
    ) {
        val itemBg = if (isDark) "#2d3748" else "#f8f9fa"
        val textColor = if (isDark) "#e0e0e0" else "#2d3748"

        svg.append("""
            <rect x="$x" y="$y" width="$width" height="$height" 
                  fill="$itemBg" stroke="${getPriorityColor(item.priority)}" stroke-width="2" 
                  stroke-left-width="4" rx="4" class="${if (config.useGlass) "glass" else ""}"/>
            
            <!-- Status Icon -->
            <text x="${x + 15}" y="${y + height/2 + 5}" fill="${getStatusColor(item.status)}" 
                  font-size="20" font-family="Arial, sans-serif">
                ${getStatusIcon(item.status)}
            </text>
            
            <!-- Task Title -->
            <text x="${x + 50}" y="${y + height/2 + 5}" fill="$textColor" font-size="16" 
                  font-family="Arial, sans-serif">
                ${item.task}
            </text>
            
            <!-- Priority Badge -->
            <rect x="${x + width - 80}" y="${y + 15}" width="60" height="25" 
                  fill="${getPriorityColor(item.priority)}" rx="12"/>
            <text x="${x + width - 50}" y="${y + 30}" fill="white" font-size="12" 
                  text-anchor="middle" font-family="Arial, sans-serif">
                ${item.priority.name}
            </text>
        """.trimIndent())
    }

    /**
     * Generate statistics panel
     */
    private fun generateStats(svg: StringBuilder, todoList: TodoList, x: Int, y: Int, isDark: Boolean) {
        val stats = todoList.items.groupingBy { it.status }.eachCount()
        val textColor = if (isDark) "#e0e0e0" else "#2d3748"

        svg.append("""
            <text x="$x" y="$y" fill="$textColor" font-size="14" font-weight="bold" 
                  font-family="Arial, sans-serif">
                Statistics
            </text>
        """.trimIndent())

        var currentY = y + 25
        stats.forEach { (status, count) ->
            svg.append("""
                <text x="$x" y="$currentY" fill="${getStatusColor(status)}" font-size="12" 
                      font-family="Arial, sans-serif">
                    ${getStatusIcon(status)} ${formatStatusName(status)}: $count
                </text>
            """.trimIndent())
            currentY += 20
        }
    }

    /**
     * Generate SVG definitions for gradients and effects
     */
    private fun generateDefs(isDark: Boolean, useGlass: Boolean): String {
        return """
            <linearGradient id="priorityHigh" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" style="stop-color:#ff6b6b;stop-opacity:1" />
                <stop offset="100%" style="stop-color:#ff8e8e;stop-opacity:1" />
            </linearGradient>
            <linearGradient id="priorityMedium" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" style="stop-color:#ffa726;stop-opacity:1" />
                <stop offset="100%" style="stop-color:#ffb74d;stop-opacity:1" />
            </linearGradient>
            <linearGradient id="priorityLow" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" style="stop-color:#42a5f5;stop-opacity:1" />
                <stop offset="100%" style="stop-color:#64b5f6;stop-opacity:1" />
            </linearGradient>
            <filter id="glass" x="-50%" y="-50%" width="200%" height="200%">
                <feGaussianBlur in="SourceGraphic" stdDeviation="2"/>
                <feOffset dx="0" dy="2" result="shadow"/>
                <feFlood flood-color="#000000" flood-opacity="0.1"/>
                <feComposite in2="shadow" operator="in"/>
            </filter>
            <style>
                .glass {
                    filter: ${if (useGlass) "url(#glass)" else "none"};
                    backdrop-filter: ${if (useGlass) "blur(10px)" else "none"};
                    opacity: ${if (useGlass) "0.9" else "1"};
                }
            </style>
        """.trimIndent()
    }

    /**
     * Helper methods
     */
    private fun getStatusIcon(status: TodoStatus): String = when (status) {
        TodoStatus.PENDING -> "⏳"
        TodoStatus.IN_PROGRESS -> "🔄"
        TodoStatus.COMPLETED -> "✅"
        TodoStatus.BLOCKED -> "🚫"
        TodoStatus.CANCELLED -> "❌"
        TodoStatus.ON_HOLD -> "⏸️"
    }

    private fun getStatusColor(status: TodoStatus): String = when (status) {
        TodoStatus.PENDING -> "#ffa726"
        TodoStatus.IN_PROGRESS -> "#42a5f5"
        TodoStatus.COMPLETED -> "#66bb6a"
        TodoStatus.BLOCKED -> "#ef5350"
        TodoStatus.CANCELLED -> "#bdbdbd"
        TodoStatus.ON_HOLD -> "#ab47bc"
    }

    private fun getPriorityColor(priority: TodoPriority): String = when (priority) {
        TodoPriority.LOW -> "#42a5f5"
        TodoPriority.MEDIUM -> "#ffa726"
        TodoPriority.HIGH -> "#ff7043"
        TodoPriority.CRITICAL -> "#ef5350"
    }


    private fun sortItems(items: List<TodoItem>, sortBy: SortBy): List<TodoItem> = when (sortBy) {
        SortBy.TASK -> items.sortedBy { it.task }
        SortBy.STATUS -> items.sortedBy { it.status.ordinal }
        SortBy.PRIORITY -> items.sortedByDescending { it.priority.ordinal }
        SortBy.DUE_DATE -> items.sortedBy { it.dueDate ?: "9999-12-31" }
        SortBy.CATEGORY -> items.sortedBy { it.category ?: "zzz" }
    }

    /**
     * Helper method to calculate max task length based on card width
     */
    private fun getMaxTaskLength(cardWidth: Int): Int {
        return when {
            cardWidth < 200 -> 18
            cardWidth < 250 -> 25
            else -> 30
        }
    }

    /**
     * Helper method to calculate max field length based on card width
     */
    private fun getMaxFieldLength(cardWidth: Int): Int {
        return when {
            cardWidth < 200 -> 12
            cardWidth < 250 -> 16
            else -> 20
        }
    }

    /**
     * Format status name for display
     */
    private fun formatStatusName(status: TodoStatus): String = when (status) {
        TodoStatus.PENDING -> "Pending"
        TodoStatus.IN_PROGRESS -> "In Progress"
        TodoStatus.COMPLETED -> "Completed"
        TodoStatus.BLOCKED -> "Blocked"
        TodoStatus.CANCELLED -> "Cancelled"
        TodoStatus.ON_HOLD -> "On Hold"
    }

    /**
     * Escape XML special characters
     */
    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    /**
     * Truncate text to specified length
     */
    private fun truncateText(text: String, maxLength: Int): String =
        if (text.length <= maxLength) text else text.take(maxLength - 3) + "..."

}