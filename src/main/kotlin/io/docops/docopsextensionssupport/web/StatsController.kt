package io.docops.docopsextensionssupport.web

import io.docops.docopsextensionssupport.button.shape.joinXmlLines
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
        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType.parseMediaType("image/svg+xml")
        return ResponseEntity(joinXmlLines("""
           <svg xmlns="http://www.w3.org/2000/svg" width="173" height="40" viewBox="0 0 390 120">
    <defs>
    <linearGradient id="grad1" x2="0%" y2="100%">
        <stop class="stop1" offset="0%" stop-color="#153e69"/>
        <stop class="stop2" offset="50%" stop-color="#123458"/>
        <stop class="stop3" offset="100%" stop-color="#0e2946"/>
    </linearGradient>
    <linearGradient id="grad2" x2="0%" y2="100%">
        <stop class="stop1" offset="0%" stop-color="#ffffff"/>
        <stop class="stop2" offset="50%" stop-color="#F2EFE7"/>
        <stop class="stop3" offset="100%" stop-color="#c1bfb8"/>
    </linearGradient>
    </defs>
    <rect x="0" y="0" width="100%" height="100%" fill="url(#grad1)"/>
    <rect x="5" y="10" width="185" height="100" fill="url(#grad1)" rx="5" ry="5" stroke="#a1a1a1" stroke-width="1"/>
    <rect x="195" y="10" width="185" height="100" fill="url(#grad1)" rx="5" ry="5" stroke="#a1a1a1" stroke-width="1"/>

    <g transform="translate(5,10)">
        <text x="92.5" y="30" text-anchor="middle" style="font-family: Helvetica,Arial,sans-serif;" fill="#fcfcfc">
            <tspan x="92.5" style="font-weight: bold;  font-size:14px; fill: #fcfcfc;">Estimated reading time</tspan>
            <tspan x="92.5" dy="40" style="font-weight: bold; font-size: 36px; fill: #1b98f8;">$mins Mins</tspan>
        </text>
    </g>
    <g transform="translate(195,10)">
    <text x="92.5" y="30" text-anchor="middle"  style="font-family: Helvetica,Arial,sans-serif;" fill="#fcfcfc">
        <tspan x="92.5"  style="font-weight: bold; font-size: 14px; fill: #fcfcfc;">Word count</tspan>
        <tspan x="92.5" dy="40" style="font-weight: bold; font-size: 36px; fill: #1b98f8;">$count</tspan>
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