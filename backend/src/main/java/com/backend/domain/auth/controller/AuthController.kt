package com.backend.domain.auth.controller

import com.backend.domain.auth.dto.reponse.TokenResponse
import com.backend.domain.auth.service.AuthService
import com.backend.domain.auth.util.CookieManager
import com.backend.domain.member.dto.request.MemberLoginRequest
import com.backend.global.response.ApiResponse
import com.backend.global.response.ApiResponse.Companion.created
import com.backend.global.response.ApiResponse.Companion.success
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val cookieManager: CookieManager
) {
    @PostMapping("/login")
    fun login(
        @RequestBody request: MemberLoginRequest,
        response: HttpServletResponse
    ): ApiResponse<TokenResponse> {
        val tokenResponse = authService.login(request.memberId, request.password)

        cookieManager.addRefreshTokenCookie(
            response,
            tokenResponse.refreshToken,
            tokenResponse.refreshTokenMaxAge
        )
        return created(tokenResponse)
    }

    @PostMapping("/reissue")
    fun reissue(
        @CookieValue(value = "refreshToken", required = false) refreshToken: String?,
        response: HttpServletResponse
    ): ApiResponse<TokenResponse> {
        val tokenResponse = authService.reissue(refreshToken)

        cookieManager.addRefreshTokenCookie(
            response,
            tokenResponse.refreshToken,
            tokenResponse.refreshTokenMaxAge
        )
        return created(tokenResponse)
    }

    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") accessToken: String,
        response: HttpServletResponse
    ): ApiResponse<Unit> {
        authService.logout(accessToken)
        cookieManager.deleteRefreshTokenCookie(response)
        return success(Unit)
    }
}
