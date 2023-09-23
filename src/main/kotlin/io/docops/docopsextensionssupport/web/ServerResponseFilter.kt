package io.docops.docopsextensionssupport.web

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.SpringBootVersion
import org.springframework.stereotype.Component


/**
 * This class is a server response filter that adds custom headers to the response.
 *
 * It implements the jakarta.servlet.Filter interface.
 *
 * The custom headers that are added include:
 *  - "X-Vendor": A link to the vendor's website.
 *  - "X-Engine": The version of Spring Boot being used.
 *
 * Example usage:
 * ```
 * val filter = ServerResponseFilter()
 * filter.doFilter(request, response, filterChain)
 * ```
 */
@Component
class ServerResponseFilter : Filter {
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        val httpServletResponse = servletResponse as HttpServletResponse
        httpServletResponse.setHeader(
            "X-Vendor", "https://docops.io"
        )
        httpServletResponse.setHeader("X-Engine", "Spring Boot ${SpringBootVersion.getVersion()}")
        filterChain.doFilter(servletRequest, servletResponse)

    }
}