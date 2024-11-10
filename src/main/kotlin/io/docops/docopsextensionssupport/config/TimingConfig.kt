package io.docops.docopsextensionssupport.config

import io.github.sercasti.tracing.config.TracingConfig
import io.github.sercasti.tracing.core.Tracing
import io.github.sercasti.tracing.filter.TracingFilter
import io.github.sercasti.tracing.interceptor.TracingInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TimingConfig {

    @Bean
    fun tracing(): Tracing {
        return TracingConfig.createTracing()
    }

    @Bean
    protected fun tracingFilter(): TracingFilter {
        return TracingFilter()
    }

    @Bean
    protected fun tracingInterceptor(): TracingInterceptor {
        return TracingInterceptor(tracing())
    }
}