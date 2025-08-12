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

import io.docops.docopsextensionssupport.badge.BadgeFormatException
import io.github.oshai.kotlinlogging.KotlinLogging
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
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onException(request: HttpServletRequest, ex: IllegalArgumentException): ProblemDetail {
        logger.error(ex) { "Bad request: ${ex.message}" }
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message!!)
    }

    @ExceptionHandler(BadgeFormatException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onBadgeException(request: HttpServletRequest, ex: BadgeFormatException): ProblemDetail {
        logger.error(ex) { "Bad badge request: ${ex.message}" }
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message!!)
    }
}