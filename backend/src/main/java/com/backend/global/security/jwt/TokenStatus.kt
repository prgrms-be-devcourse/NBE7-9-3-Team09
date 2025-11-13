package com.backend.global.security.jwt

enum class TokenStatus {
    VALID,  // 유효한 토큰
    EXPIRED,  // 만료된 토큰
    INVALID // 서명 오류 등 유효하지 않은 토큰
}
