package com.backend.domain.auth.controller

import com.backend.domain.auth.dto.reponse.TokenResponse
import com.backend.domain.auth.dto.request.OAuth2SignupRequest
import com.backend.domain.auth.service.AuthService
import com.backend.domain.auth.service.OAuth2SignupService
import com.backend.domain.auth.util.CookieManager
import com.backend.global.response.ApiResponse
import com.backend.global.response.ApiResponse.Companion.created
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth/oauth2")
class OAuth2SignupController(
    private val oAuth2SignupService: OAuth2SignupService,
    private val authService: AuthService,
    private val cookieManager: CookieManager
) {

    @PostMapping("/signup")
    fun signupWithOAuth2(
        @RequestBody request: OAuth2SignupRequest,
        response: HttpServletResponse
    ): ApiResponse<TokenResponse> {
        // 1) 회원가입 (Member 생성)
        val member = oAuth2SignupService.signupWithOAuth2(request)

        // 2) 소셜 로그인 처리 (토큰 발급)
        val tokenResponse = authService.loginOAuth2(member)

        // 3) Refresh Token → HttpOnly Cookie 저장
        cookieManager.addRefreshTokenCookie(
            response = response,
            token = tokenResponse.refreshToken,
            maxAgeSeconds = tokenResponse.refreshTokenMaxAge
        )

        return created(tokenResponse)
    }
}