package com.backend.global.security.jwt

import com.backend.domain.member.entity.Provider
import com.backend.domain.member.entity.Role
import com.backend.global.security.oauth.dto.OAuth2TempClaims
import com.backend.global.security.oauth.util.OAuth2TempTokenParser
import com.backend.global.security.user.CustomUserDetails
import com.backend.global.security.user.CustomUserDetailsService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey
import jakarta.annotation.PostConstruct
import java.lang.IllegalArgumentException

@Component
class JwtTokenProvider(
    private val customUserDetailsService: CustomUserDetailsService,

    @Value("\${custom.jwt.secret-key}")
    private val secretKey: String,

    @Value("\${custom.jwt.access-token.expire-time}")
    private val accessTokenExpireTimeMs: Long,

    @Value("\${custom.jwt.refresh-token.expire-time}")
    private val refreshTokenExpireTimeMs: Long,

    @Value("\${custom.jwt.temp-token.expire-time}")
    private val tempTokenExpireMs: Long
) {

    private lateinit var key: SecretKey

    @PostConstruct
    fun init() {
        key = Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))
    }

    /** Access Token 생성 */
    fun generateAccessToken(memberId: Long, role: Role): String {
        return generateToken(
            subject = memberId.toString(),
            claims = mapOf(
                "role" to role.name,
                "type" to TokenType.ACCESS.name
            ),
            expireTime = accessTokenExpireTimeMs
        )
    }

    /** Refresh Token 생성 */
    fun generateRefreshToken(memberId: Long, role: Role): String {
        return generateToken(
            subject = memberId.toString(),
            claims = mapOf(
                "role" to role.name,
                "type" to TokenType.REFRESH.name
            ),
            expireTime = refreshTokenExpireTimeMs
        )
    }

    /** ⭐ 소셜 회원가입 임시 토큰 (provider / providerId / email 기반) */
    fun generateOAuth2TempToken(
        provider: Provider,
        providerId: String,
        email: String
    ): String {
        return generateToken(
            subject = providerId,
            claims = mapOf(
                "provider" to provider.name,
                "providerId" to providerId,
                "email" to email,
                "type" to TokenType.OAUTH2_TEMP.name
            ),
            expireTime = tempTokenExpireMs
        )
    }

    /** 공통 토큰 생성 로직 */
    private fun generateToken(
        subject: String,
        claims: Map<String, Any>,
        expireTime: Long
    ): String {
        val now = Date()
        val expiry = Date(now.time + expireTime)

        return Jwts.builder()
            .subject(subject)
            .issuedAt(now)
            .expiration(expiry)
            .claims(claims)
            .signWith(key)
            .compact()
    }

    /** 토큰 유효성 검증 (서명 및 만료 확인) */
    fun validateTokenStatus(token: String): TokenStatus =
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parse(token)
            TokenStatus.VALID
        } catch (e: ExpiredJwtException) {
            log.info("== 토큰이 만료되었습니다 ==")
            TokenStatus.EXPIRED
        } catch (e: JwtException) {
            log.info("== 유효하지 않은 토큰입니다 ==")
            TokenStatus.INVALID
        } catch (e: IllegalArgumentException) {
            log.info("== 유효하지 않은 토큰입니다 ==")
            TokenStatus.INVALID
        }

    /** Claims 파싱 (JWT Payload 디코드) */
    private fun parseClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

    /** 토큰에서 memberId(subject) 추출 */
    fun getMemberIdFromToken(token: String): Long =
        parseClaims(token).subject.toLong()

    /** 토큰에서 Role 추출 */
    fun getRoleFromToken(token: String): Role =
        Role.valueOf(parseClaims(token)["role"].toString())

    /** 토큰 타입 조회 (access / refresh) */
    fun getTokenType(token: String): TokenType =
        TokenType.valueOf(parseClaims(token)["type"].toString())

    /** JWT → Authentication 변환 */
    fun getAuthentication(token: String): Authentication {
        val memberPk = getMemberIdFromToken(token)
        val userDetails = customUserDetailsService.loadUserById(memberPk) as CustomUserDetails

        return UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities
        )
    }

    /** 만료 시간 (초 단위 반환) */
    val accessTokenExpireTime: Long get() = accessTokenExpireTimeMs / 1000
    val refreshTokenExpireTime: Long get() = refreshTokenExpireTimeMs / 1000

    companion object {
        private val log = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    }

    fun parseOAuthClaims(token: String): Claims =
        parseClaims(token)
}