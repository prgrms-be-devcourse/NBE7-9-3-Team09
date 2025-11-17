package com.backend.global.security.jwt.handler

import com.backend.global.response.ErrorCode
import com.backend.global.security.jwt.JwtErrorResponseWriter
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.IOException

/**
 * 인증되지 않은 사용자가 보호된 리소스에 접근했을 때 (401 Unauthorized)
 */
@Component
class JwtAuthenticationEntryPoint(
    private val jwtErrorResponseWriter: JwtErrorResponseWriter
) : AuthenticationEntryPoint {

    private val log = KotlinLogging.logger {}

    @Throws(IOException::class, ServletException::class)
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        log.warn { "인증되지 않은 요청입니다. URI: ${request.requestURI}" }
        jwtErrorResponseWriter.write(response, ErrorCode.UNAUTHORIZED_REQUEST)
    }

}