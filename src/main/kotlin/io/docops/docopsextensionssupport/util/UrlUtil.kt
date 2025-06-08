package io.docops.docopsextensionssupport.util

import jakarta.servlet.http.HttpServletRequest

/**
 * Utility class for URL-related operations.
 */
object UrlUtil {
    
    /**
     * Generates a base URL using the current request's host and port.
     * 
     * @param request The current HttpServletRequest
     * @return The base URL (e.g., "http://localhost:8010/extension")
     */
    fun getBaseUrl(request: HttpServletRequest): String {
        val scheme = request.scheme
        val serverName = request.serverName
        val serverPort = request.serverPort
        val contextPath = request.contextPath
        
        // Build the base URL
        val url = StringBuilder()
        url.append(scheme).append("://").append(serverName)
        
        // Add port if it's not the default port for the scheme
        if ((scheme.equals("http") && serverPort != 80) || 
            (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort)
        }
        
        // Add context path
        url.append(contextPath)
        
        return url.toString()
    }
    
    /**
     * Generates a complete URL for the image request with the given parameters.
     * 
     * @param request The current HttpServletRequest
     * @param kind The kind of image (e.g., "adr", "bar", "timeline")
     * @param payload The compressed payload
     * @param type The type of image (default: "SVG")
     * @param useDark Whether to use dark mode (default: false)
     * @param title The title of the image (default: "Title")
     * @param numChars The number of characters (default: "24")
     * @param filename The filename of the image (default: based on kind)
     * @return The complete URL for the image request
     */
    fun getImageUrl(
        request: HttpServletRequest,
        kind: String,
        payload: String,
        type: String = "SVG",
        useDark: Boolean = false,
        title: String = "Title",
        numChars: String = "24",
        scale: String? = null,
        filename: String? = null
    ): String {
        val baseUrl = getBaseUrl(request)
        val actualFilename = filename ?: "$kind.svg"
        
        val url = StringBuilder()
        url.append(baseUrl)
        url.append("/api/docops/svg?kind=").append(kind)
        url.append("&payload=").append(payload)
        url.append("&type=").append(type)
        url.append("&useDark=").append(useDark)
        url.append("&title=").append(title)
        url.append("&numChars=").append(numChars)
        url.append("&backend=html5")
        if (scale != null) {
            url.append("&scale=").append(scale)
        }
        url.append("&filename=").append(actualFilename)
        
        return url.toString()
    }
}