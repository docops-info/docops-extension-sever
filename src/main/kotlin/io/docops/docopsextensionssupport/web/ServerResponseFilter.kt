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