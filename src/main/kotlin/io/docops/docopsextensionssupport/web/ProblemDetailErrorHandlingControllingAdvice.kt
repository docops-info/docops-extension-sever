package io.docops.docopsextensionssupport.web

import io.docops.docopsextensionssupport.badge.BadgeFormatException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.IllegalArgumentException

/**
 * Global error handling controller advice class for handling exceptions and returning appropriate problem details.
 */
@ControllerAdvice
class ProblemDetailErrorHandlingControllingAdvice {

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onException(request: HttpServletRequest, ex: IllegalArgumentException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message!!)
    }

    @ExceptionHandler(BadgeFormatException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onBadgeException(request: HttpServletRequest, ex: BadgeFormatException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message!!)
    }
}