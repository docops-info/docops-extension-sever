package io.docops.docopsextensionssupport.todo

import io.docops.docopsextensionssupport.web.CsvResponse
import kotlinx.serialization.Serializable

/**
 * Represents a single todo item
 */
@Serializable
data class TodoItem(
    val task: String,
    val status: TodoStatus = TodoStatus.PENDING,
    val priority: TodoPriority = TodoPriority.MEDIUM,
    val category: String? = null,
    val tags: List<String> = emptyList(),
    val assignee: String? = null,
    val dueDate: String? = null,
    val description: String? = null
)

/**
 * Status of a todo item
 */
@Serializable
enum class TodoStatus {
    PENDING,      // ⏳ Not started yet
    IN_PROGRESS,  // 🔄 Currently being worked on
    COMPLETED,    // ✅ Finished
    BLOCKED,      // 🚫 Cannot proceed
    CANCELLED,    // ❌ No longer needed
    ON_HOLD      // ⏸️ Temporarily paused
}

/**
 * Priority levels for todo items
 */
@Serializable
enum class TodoPriority {
    LOW,      // 🔵 Blue
    MEDIUM,   // 🟡 Yellow
    HIGH,     // 🟠 Orange
    CRITICAL  // 🔴 Red
}

/**
 * Configuration for the todo list visualization
 */
@Serializable
data class TodoConfig(
    val title: String = "Todo List",
    val showStats: Boolean = true,
    val showCategories: Boolean = true,
    val showPriority: Boolean = true,
    val showAssignees: Boolean = false,
    val showDueDates: Boolean = false,
    val groupBy: GroupBy = GroupBy.NONE,
    val sortBy: SortBy = SortBy.PRIORITY,
    val layout: Layout = Layout.KANBAN,
    val useGlass: Boolean = true,
    val colorScheme: String = "default"
)

/**
 * Options for grouping todo items
 */
@Serializable
enum class GroupBy {
    NONE,
    STATUS,
    PRIORITY,
    CATEGORY,
    ASSIGNEE
}

/**
 * Options for sorting todo items
 */
@Serializable
enum class SortBy {
    TASK,
    STATUS,
    PRIORITY,
    DUE_DATE,
    CATEGORY
}

/**
 * Layout options for the todo visualization
 */
@Serializable
enum class Layout {
    KANBAN,    // Board with columns for each status
    LIST,      // Simple list view
    GRID,      // Grid of cards
    TIMELINE   // Timeline view (if due dates are present)
}

/**
 * Complete todo list data structure
 */
@Serializable
data class TodoList(
    val config: TodoConfig = TodoConfig(),
    val items: List<TodoItem> = emptyList()
)

/**
 * Convert TodoList to CSV format
 */
fun TodoList.toCsv(): CsvResponse {
    val headers = mutableListOf("Task", "Status", "Priority")
    
    // Add optional headers based on config
    if (config.showCategories) headers.add("Category")
    if (config.showAssignees) headers.add("Assignee")
    if (config.showDueDates) headers.add("Due Date")
    headers.addAll(listOf("Tags", "Description"))
    
    val rows = items.map { item ->
        val row = mutableListOf(
            item.task,
            item.status.name,
            item.priority.name
        )
        
        if (config.showCategories) row.add(item.category ?: "")
        if (config.showAssignees) row.add(item.assignee ?: "")
        if (config.showDueDates) row.add(item.dueDate ?: "")
        row.addAll(listOf(
            item.tags.joinToString(", "),
            item.description ?: ""
        ))
        
        row.toList()
    }
    
    return CsvResponse(headers = headers, rows = rows)
}
