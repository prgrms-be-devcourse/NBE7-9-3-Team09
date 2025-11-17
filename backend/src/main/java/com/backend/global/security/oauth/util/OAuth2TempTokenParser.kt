package com.backend.global.security.oauth.util

import com.backend.domain.member.entity.Provider
import com.backend.global.security.oauth.dto.OAuth2TempClaims
import io.jsonwebtoken.Claims
import org.springframework.stereotype.Component

@Component
class OAuth2TempTokenParser {

    fun toTempClaims(claims: Claims): OAuth2TempClaims {
        return OAuth2TempClaims(
            provider = Provider.valueOf(claims["provider"].toString()),
            providerId = claims["providerId"].toString(),
            email = claims["email"].toString()
        )
    }
}