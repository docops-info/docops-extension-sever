package io.docops.docopsextensionssupport.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
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