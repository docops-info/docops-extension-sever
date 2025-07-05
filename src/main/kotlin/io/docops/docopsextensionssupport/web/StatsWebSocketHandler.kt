package io.docops.docopsextensionssupport.web

import org.springframework.stereotype.Component
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.context.event.EventListener
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

/**
 * WebSocket message payload for stats events
 */
data class StatsMessage(
    val type: String,
    val data: Any,
    val html: String? = null,
    val timestamp: String = LocalDateTime.now().toString()
)

/**
 * Handler for WebSocket stats messages using STOMP
 */
@Component
class StatsWebSocketHandler @Autowired constructor(
    private val messagingTemplate: SimpMessagingTemplate
) {

    /**
     * Listen for DocOps extension events and broadcast them to WebSocket clients
     */
    @EventListener
    fun docopsEvent(event: DocOpsExtensionEvent) {
        //println("=== EVENT RECEIVED ===")
        //println("Event: ${event.eventName}")
        //println("Duration: ${event.duration}ms")
        //println("Status: ${event.status}")

        val eventHtml = formatEventToHtml(event)
        val eventData = mapOf(
            "eventName" to event.eventName,
            "duration" to event.duration,
            "status" to event.status,
            "count" to event.count,
            "time" to LocalDateTime.now().toString()
        )

        val message = StatsMessage(
            type = "message",
            data = eventData,
            html = eventHtml
        )

        //println("Sending event to WebSocket topic: /topic/stats")
        messagingTemplate.convertAndSend("/topic/stats", message)
        //println("=== EVENT PROCESSED ===")
    }

    /**
     * Send a direct message to all connected WebSocket clients
     */
    fun sendDirectMessage(message: String) {
        //println("=== SENDING DIRECT WEBSOCKET MESSAGE ===")

        val statsMessage = StatsMessage(
            type = "direct",
            data = message
        )

        messagingTemplate.convertAndSend("/topic/stats", statsMessage)
        //println("Direct message sent to WebSocket topic: /topic/stats")
    }

    /**
     * Format an event as HTML for display
     */
    private fun formatEventToHtml(event: DocOpsExtensionEvent): String {
        return """
            <div class='mb-5 p-3 border rounded'>
                <h3 class='text-lg font-semibold text-gray-900'>${event.eventName}</h3>
                <p class='text-sm text-gray-600'>${event.duration}ms</p>
                <p class='text-xs text-gray-500'>Status: ${if(event.status) "✅ Success" else "❌ Failed"}</p>
                <p class='text-xs text-gray-500'>Execution count: <span class="font-semibold">${event.count}</span></p>
                <p class='text-xs text-gray-400'>Time: ${LocalDateTime.now()}</p>
            </div>
        """.trimIndent()
    }
}
