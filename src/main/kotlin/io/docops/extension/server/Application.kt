package io.docops.extension.server

import com.codahale.metrics.Slf4jReporter
import com.codahale.metrics.jmx.JmxReporter
import io.docops.extension.server.ktor.plugins.configureHTTP
import io.docops.extension.server.ktor.plugins.configureMonitoring
import io.docops.extension.server.ktor.plugins.configureRouting
import io.docops.extension.server.ktor.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.dropwizard.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit


fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    val l: Logger = LoggerFactory.getLogger("ExtensionsServer")

    configureRouting()
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    install(DropwizardMetrics) {
        Slf4jReporter.forRegistry(registry)
            .outputTo(l)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build()
            .start(10, TimeUnit.MINUTES)
        JmxReporter.forRegistry(registry)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build()
            .start()
    }
}
