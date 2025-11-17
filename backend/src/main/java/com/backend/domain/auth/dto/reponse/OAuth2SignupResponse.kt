package com.backend.domain.auth.dto.reponse

data class OAuth2SignupResponse(
    val memberId: Long,
    val accessToken: String,
    val refreshToken: String,
    val refreshTokenMaxAge: Long
)