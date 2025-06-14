package io.docops.docopsextensionssupport.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.beans.factory.annotation.Autowired
import io.docops.docopsextensionssupport.web.StatsWebSocketHandler

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic")
        registry.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/api/stats/ws")
               .setAllowedOriginPatterns("*")
               .withSockJS()
    }

    override fun configureWebSocketTransport(registration: WebSocketTransportRegistration) {
        registration.setMessageSizeLimit(64 * 1024) // 64KB
        registration.setSendBufferSizeLimit(512 * 1024) // 512KB
        registration.setSendTimeLimit(20000) // 20 seconds
    }
}
