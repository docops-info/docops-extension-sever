package io.docops.docopsextensionssupport.aop

import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.logging.LoggingMeterRegistry
import io.micrometer.core.instrument.logging.LoggingRegistryConfig
import io.micrometer.core.instrument.util.NamedThreadFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration


class LoggingMeterRegistryConfig {
    fun loggingMeterRegistry(): LoggingMeterRegistry {
        val config: LoggingRegistryConfig = object : LoggingRegistryConfig {
            override fun get(s: String): String? {
                return null
            }

            override fun step(): Duration {
                return Duration.ofSeconds(300)
            }
        }
        return LoggingMeterRegistry.builder(config).clock(Clock.SYSTEM)
            .threadFactory(NamedThreadFactory("logging-metrics-publisher")).build()
    }
}