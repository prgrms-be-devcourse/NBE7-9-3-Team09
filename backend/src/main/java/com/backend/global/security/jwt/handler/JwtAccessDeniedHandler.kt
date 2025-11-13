package com.backend.global.security.jwt.handler

import com.backend.global.response.ErrorCode
import com.backend.global.security.jwt.JwtErrorResponseWriter
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import java.io.IOException

/**
 * 인증은 되었지만 권한(Role)이 없는 사용자가 접근할 때 (403 Forbidden)
 */
@Component
@RequiredArgsConstructor
class JwtAccessDeniedHandler(
    private val jwtErrorResponseWriter: JwtErrorResponseWriter
) : AccessDeniedHandler {

    private val log = KotlinLogging.logger {}

    @Throws(IOException::class, ServletException::class)
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        log.warn { "접근 권한이 없는 요청입니다. URI: ${request.requestURI}" }
        jwtErrorResponseWriter.write(response, ErrorCode.ACCESS_DENIED)
    }
}
