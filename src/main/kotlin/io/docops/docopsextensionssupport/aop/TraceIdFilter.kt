package io.docops.docopsextensionssupport.aop

import io.micrometer.tracing.Span
import io.micrometer.tracing.Tracer
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component


@Component
class TraceIdFilter(val tracer: Tracer) : Filter {
    private val TRACE_ID_HEADER: String = "X-Trace-Id"
    private val TRACE_ID_MDC_KEY: String = "traceId"

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpResponse = response as HttpServletResponse
        val currentSpan: Span? = tracer.currentSpan()
        var traceId = ""
        if (currentSpan != null) {
            traceId = currentSpan.context().traceId()
            httpResponse.addHeader(TRACE_ID_HEADER, traceId)
            MDC.put(TRACE_ID_MDC_KEY, traceId);
        }
        try {
            chain.doFilter(request, response)
        } finally {
            //this is the criticl part
            //clean up
            MDC.remove(TRACE_ID_MDC_KEY);
        }
    }
}