package com.backend.domain.auth.dto.reponse

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val refreshTokenMaxAge: Long,
    val role: String
) {
    companion object {
        fun fromTokens(
            accessToken: String,
            refreshToken: String,
            refreshTokenMaxAge: Long,
            role: String
        ): TokenResponse {
            return TokenResponse(
                accessToken,
                refreshToken,
                refreshTokenMaxAge,
                role
            )
        }
    }
}
