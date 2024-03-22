/*
 * Copyright (c) 2023. The DocOps Consortium
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.docops.docopsextensionssupport.aop

import io.micrometer.context.ContextExecutorService
import io.micrometer.context.ContextSnapshot
import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.observation.ObservationRegistry
import io.micrometer.observation.aop.ObservedAspect
import io.micrometer.tracing.Span
import io.micrometer.tracing.Tracer
import jakarta.servlet.Filter
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.concurrent.*


/**
 * Configuration class for auto-timing functionality.
 * This class provides beans for timing aspects, observed aspects, and trace ID filter.
 * It also includes an example of configuring async servlets.
 */
@Configuration(proxyBeanMethods = false)
@EnableAspectJAutoProxy
class AutoTimingConfiguration {
    /**
     * Creates an instance of TimedAspect with the provided MeterRegistry.
     *
     * @param registry the MeterRegistry instance used by TimedAspect
     * @return an instance of TimedAspect
     */
    @Bean
    fun timedAspect(registry: MeterRegistry): TimedAspect {
        return TimedAspect(registry)
    }

    /**
     * Creates an instance of ObservedAspect with the provided ObservationRegistry.
     *
     * @param observationRegistry the ObservationRegistry instance used by ObservedAspect
     * @return an instance of ObservedAspect
     */
    @Bean
    fun observedAspect(observationRegistry: ObservationRegistry?): ObservedAspect? {
        return ObservedAspect(observationRegistry!!)
    }

    /**
     * Adds traceId to the response headers using the X-B3-TraceId header.
     *
     * @param tracer the Tracer instance used to get the current span
     * @return a Filter instance
     */
    @Bean
    fun traceIdInResponseFilter(tracer: Tracer): Filter {
        return Filter { request, response, chain ->
            val currentSpan: Span? = tracer.currentSpan()
            if (currentSpan != null) {
                val resp = response as HttpServletResponse
                resp.addHeader("X-B3-TraceId", currentSpan.context().traceId())
            }
            chain.doFilter(request, response)
        }
    }

    // Example of Async Servlets setup
    @Configuration(proxyBeanMethods = false)
    @EnableAsync
    internal class AsyncConfig : AsyncConfigurer, WebMvcConfigurer {
        override fun getAsyncExecutor(): Executor {
            return ContextExecutorService.wrap(Executors.newCachedThreadPool(), ContextSnapshot::captureAll)
        }

        override fun configureAsyncSupport(configurer: AsyncSupportConfigurer) {
            configurer.setTaskExecutor(SimpleAsyncTaskExecutor(ThreadFactory { r: Runnable ->
                Thread(
                    ContextSnapshot.captureAll().wrap(r)
                )
            }))
        }
    }


    /**
     * NAME OF THE BEAN IS IMPORTANT!
     *
     *
     * We need to wrap this for @Async related things to propagate the context.
     *
     * @see EnableAsync
     */
    // [Observability] instrumenting executors
    @Bean(name = ["taskExecutor"], destroyMethod = "shutdown")
    fun threadPoolTaskScheduler(): ThreadPoolTaskScheduler = DocOpsThreadPoolTaskScheduler()

}
/**
 * This class represents a custom implementation of the [org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler] class.
 * The class extends the [org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler] class in order to provide additional functionality.
 *
 * The [DocOpsThreadPoolTaskScheduler] class overrides the [initializeExecutor] method to customize the initialization of the executor service.
 * It wraps the executor service returned by the superclass method with a [ContextExecutorService] by using the [ContextExecutorService.wrap] method.
 * This allows capturing the context of the task execution using the [ContextSnapshot.captureAll] method.
 *
 * @see org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
 * @see org.springframework.core.task.support.ExecutorServiceAdapter
 * @see org.springframework.scheduling.support.DelegatingErrorHandlingRunnable
 * @see org.springframework.core.task.SimpleAsyncTaskExecutor
 * @see org.springframework.scheduling.concurrent.ContextExecutorService
 * @see org.springframework.scheduling.concurrent.ContextSnapshot
 */
class DocOpsThreadPoolTaskScheduler : ThreadPoolTaskScheduler() {
    override fun initializeExecutor(
        threadFactory: ThreadFactory,
        rejectedExecutionHandler: RejectedExecutionHandler
    ): ExecutorService {
        val executorService = super.initializeExecutor(threadFactory, rejectedExecutionHandler)
        return ContextExecutorService.wrap(executorService, ContextSnapshot::captureAll)
    }
}