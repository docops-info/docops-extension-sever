package io.docops.docopsextensionssupport.web

import io.docops.docopsextensionssupport.button.shape.joinXmlLines
import io.docops.docopsextensionssupport.chart.valueFmt
import io.docops.docopsextensionssupport.svgsupport.ToolTip
import io.docops.docopsextensionssupport.svgsupport.ToolTipConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.http.*
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.Duration
import java.time.LocalTime
import java.util.UUID
import java.util.concurrent.CopyOnWriteArraySet
import java.util.function.Consumer


@Controller
@RequestMapping("/api")
class StatsController @Autowired constructor(private val applicationEventPublisher: ApplicationEventPublisher) {

    private val clients: MutableSet<SseEmitter> = CopyOnWriteArraySet()

    @GetMapping("/stats/docops-stream") //2.2
    fun stocksStream(): SseEmitter {
        val sseEmitter = SseEmitter(Duration.ofMinutes(5).toMillis())
        clients.add(sseEmitter)

        sseEmitter.onTimeout { clients.remove(sseEmitter) }
        sseEmitter.onError { throwable: Throwable? -> clients.remove(sseEmitter) }

        return sseEmitter
    }

    @Async
    @EventListener
    fun docopsEvent(event: DocOpsExtensionEvent) {
        val errorEmitters: MutableList<SseEmitter> = ArrayList()
        clients.forEach(Consumer { emitter: SseEmitter ->
            try {
                val sseEvent = SseEmitter.event().id(UUID.randomUUID().toString())
                    .name("message")
                    .data(formatEventToHtml(event))
                emitter.send(sseEvent)
            } catch (e: Exception) {
                errorEmitters.add(emitter)
            }
        })

        errorEmitters.forEach(Consumer { o: SseEmitter? -> clients.remove(o) })
    }

    @GetMapping("/stats/doc", produces = ["image/svg+xml"])
    fun getDocStats(@RequestParam("mins") mins: String,
                    @RequestParam("count") count: String,): ResponseEntity<ByteArray> {
        val toolTipGen = ToolTip()
        val headers = HttpHeaders()
        val guid = UUID.randomUUID().toString()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")

        // Determine color based on reading time
        val readTimeColor = when {
            mins.toIntOrNull() ?: 0 < 5 -> "#50C878" // Green for short reads
            mins.toIntOrNull() ?: 0 < 15 -> "#F4C430" // Yellow for medium reads
            else -> "#C70039" // Red for long reads
        }

        return ResponseEntity(("""
           <svg xmlns="http://www.w3.org/2000/svg" width="190" height="32" viewBox="0 0 190 32">
    <defs>
        <linearGradient id="grad1" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="#f8f9fa"/>
            <stop class="stop2" offset="50%" stop-color="#f1f3f5"/>
            <stop class="stop3" offset="100%" stop-color="#e9ecef"/>
        </linearGradient>
        <filter id="dropShadow" filterUnits="userSpaceOnUse" width="180" height="32">
            <feDropShadow dx="0.5" dy="0.5" stdDeviation="0.5" flood-color="#0003"/>
        </filter>
    </defs>
    <!-- Main container with shadow -->
    <rect x="1" y="0" width="99%" height="100%" fill="url(#grad1)" rx="6" ry="6" />

    <!-- Clock icon -->
    <g transform="translate(2,11) scale(0.018,0.018)">
        <path d="M512 256C512 397.4 397.4 512 256 512C114.6 512 0 397.4 0 256C0 114.6 114.6 0 256 0C397.4 0 512 114.6 512 256zM256 48C141.1 48 48 141.1 48 256C48 370.9 141.1 464 256 464C370.9 464 464 370.9 464 256C464 141.1 370.9 48 256 48zM256 352C247.2 352 240 344.8 240 336V272H176C167.2 272 160 264.8 160 256C160 247.2 167.2 240 176 240H256C264.8 240 272 247.2 272 256V336C272 344.8 264.8 352 256 352z" fill="#495057"/>
    </g>

    <!-- Reading time text -->
    <g transform="translate(18,16)">
        <text style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif; font-size: 14px; fill: #212529;">
            <tspan x="0" y="5">Reading time:</tspan>
            <tspan x="95" y="5" style="font-weight: 600; fill: #C70039;">$mins min</tspan>
        </text>
    </g>
</svg>
        """.trimIndent()).toByteArray(), headers, HttpStatus.OK)
    }
    private fun formatEventToHtml(event: DocOpsExtensionEvent): String {
        //language=html
        return """
            <div class='mb-5'>
                <h3 class='mt-3 text-lg font-semibold leading-6 text-gray-900 group-hover:text-gray-600'>${event.eventName}</h3>
                <p class='mt-5  text-sm leading-6 text-gray-600'>${event.duration}</p>
            </div>
        """.trimIndent()
    }
}
