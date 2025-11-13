package com.backend.global.security.jwt

import com.backend.global.response.ErrorCode
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
@RequiredArgsConstructor
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtErrorResponseWriter: JwtErrorResponseWriter
) : OncePerRequestFilter() {

    private val log = KotlinLogging.logger {}

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestURI = request.getRequestURI()

        // 1. 인증이 필요 없는 경로
        val whiteList = listOf(
            "/api/auth/login",
            "/api/members/signup",
            "/api/auth/reissue"
        )
        if (requestURI in whiteList) {
            filterChain.doFilter(request, response)
            return
        }

        // 2. Authorization 헤더에서 JWT 추출
        val token = request.getHeader(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith("Bearer ") }
            ?.substring(7)
            ?.trim()

        // 3. 토큰이 없으면 다음 필터로 (비로그인 요청)
        if (token.isNullOrBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            // 4. 토큰 상태 검증
            when (jwtTokenProvider.validateTokenStatus(token)) {
                TokenStatus.VALID -> {
                    val tokenType = jwtTokenProvider.getTokenType(token)
                    if (tokenType != "access") {
                        log.warn { "[JWT] Refresh Token으로 접근 시도 차단: $requestURI" }
                        jwtErrorResponseWriter.write(response, ErrorCode.INVALID_ACCESS_TOKEN)
                        return
                    }

                    val authentication = jwtTokenProvider.getAuthentication(token)
                    SecurityContextHolder.getContext().authentication = authentication
                    log.debug { "[JWT] 유효한 토큰으로 인증 완료: $requestURI" }
                }

                TokenStatus.EXPIRED -> {
                    log.warn { "[JWT] 만료된 토큰입니다. 요청 URI: $requestURI" }
                    jwtErrorResponseWriter.write(response, ErrorCode.EXPIRED_ACCESS_TOKEN)
                    return
                }

                TokenStatus.INVALID -> {
                    log.warn { "[JWT] 유효하지 않은 토큰입니다. 요청 URI: $requestURI" }
                    jwtErrorResponseWriter.write(response, ErrorCode.INVALID_ACCESS_TOKEN)
                    return
                }
            }

            // 5. 다음 필터로 요청 전달
            filterChain.doFilter(request, response)

        } catch (e: Exception) {
            log.error(e) { "[JWT] 필터 처리 중 예외 발생: ${e.message}" }
            jwtErrorResponseWriter.write(response, ErrorCode.UNAUTHORIZED_REQUEST)
        }
    }
}
