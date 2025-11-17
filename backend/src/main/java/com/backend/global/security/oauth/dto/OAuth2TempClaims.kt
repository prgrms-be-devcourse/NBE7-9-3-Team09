package com.backend.global.security.oauth.dto

import com.backend.domain.member.entity.Provider

data class OAuth2TempClaims(
    val provider: Provider,
    val providerId: String,
    val email: String
)