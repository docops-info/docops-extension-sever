package io.docops.docopsextensionssupport.web

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component


@Component
class ServerResponseFilter : Filter {
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        val httpServletResponse = servletResponse as HttpServletResponse
        httpServletResponse.setHeader(
            "X-Vendor", "https://docops.io"
        )
        httpServletResponse.setHeader("X-Engine", "Spring Boot 3")
        filterChain.doFilter(servletRequest, servletResponse)

    }
}