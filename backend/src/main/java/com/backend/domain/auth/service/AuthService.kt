package com.backend.domain.auth.service

import com.backend.domain.auth.dto.reponse.TokenResponse
import com.backend.domain.auth.dto.request.OAuthLoginResult
import com.backend.domain.auth.entity.RefreshToken
import com.backend.domain.auth.repository.RefreshTokenRepository
import com.backend.domain.member.entity.Member
import com.backend.domain.member.entity.MemberStatus
import com.backend.domain.member.entity.Provider
import com.backend.domain.member.repository.MemberRepository
import com.backend.global.exception.BusinessException
import com.backend.global.response.ErrorCode
import com.backend.global.security.jwt.JwtTokenProvider
import com.backend.global.security.jwt.TokenStatus
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 인증 서비스 (JWT 발급, 재발급, 로그아웃)
 */
@Service
@Transactional(readOnly = true)
class AuthService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {
    private val log = KotlinLogging.logger {}

    /** OAuth 통합 처리 */
    @Transactional
    fun handleOAuth2Login(
        provider: Provider,
        providerId: String,
        email: String?
    ): OAuthLoginResult {

        val existingMember = memberRepository.findByProviderAndProviderId(provider, providerId)

        return if (existingMember == null) {
            // ★ 신규 회원 → TempToken 발급
            val tempToken = issueOAuth2TempToken(provider, providerId, email!!)
            OAuthLoginResult.newUser(tempToken)
        } else {
            // ★ 기존 회원 → 로그인 처리
            val tokens = loginOAuth2(existingMember)
            OAuthLoginResult.existingUser(
                access = tokens.accessToken,
                refresh = tokens.refreshToken
            )
        }
    }

    /** OAuth 신규 회원 TempToken 발급 */
    @Transactional
    fun issueOAuth2TempToken(
        provider: Provider,
        providerId: String,
        email: String
    ): String {
        return jwtTokenProvider.generateOAuth2TempToken(
            provider = provider,
            providerId = providerId,
            email = email
        )
    }

    /** OAuth2 로그인 (기존 회원) */
    @Transactional
    fun loginOAuth2(member: Member): TokenResponse {
        return generateAndStoreTokens(member)
    }

    /** 일반 로그인: AccessToken + RefreshToken 발급  */
    @Transactional
    fun login(loginId: String, password: String): TokenResponse {
        val member: Member = memberRepository.findByMemberId(loginId)
            ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)

        if (member.status != MemberStatus.ACTIVE) {
            throw BusinessException(ErrorCode.INACTIVE_MEMBER) // 새로운 에러 코드 추가
        }

        if (!passwordEncoder.matches(password, member.password)) {
            throw BusinessException(ErrorCode.INVALID_PASSWORD)
        }

        log.info { "[Auth] 로그인 성공: memberPk=${member.id}, issuedAt=${LocalDateTime.now()}" }

        return generateAndStoreTokens(member)
    }

    /** AccessToken 재발급 */
    @Transactional
    fun reissue(refreshToken: String?): TokenResponse {
        val token = refreshToken
            ?: throw BusinessException(ErrorCode.TOKEN_NOT_FOUND)

        // 1. 유효성 검사
        validateTokenStatus(token)

        // 2. 토큰에서 memberPk 추출
        val memberPk = jwtTokenProvider.getMemberIdFromToken(token)

        // 3. DB 토큰 검증
        val savedToken = getValidatedRefreshToken(token, memberPk)

        // 4. RefreshToken에는 role 정보가 없기 때문에 MemberService로부터 다시 조회 필요
        val member = memberRepository.findByIdOrNull(memberPk)
            ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)

        // 5. 새 AccessToken 발급
        val newAccessToken = jwtTokenProvider.generateAccessToken(memberPk, member.role)

        log.info { "${"[Auth] AccessToken 재발급 완료: memberPk={}, reissuedAt={}"} $memberPk ${LocalDateTime.now()}" }

        return TokenResponse.fromTokens(
            newAccessToken,
            savedToken.token,
            jwtTokenProvider.refreshTokenExpireTime,
            member.role.name)
    }

    @Transactional
    fun logout(accessTokenHeader: String) {
        val memberPk = jwtTokenProvider.getMemberIdFromToken(extractToken(accessTokenHeader))
        refreshTokenRepository.deleteByMemberPk(memberPk)
        log.info { "${"[Auth] 로그아웃 완료: memberPk={}, deletedAt={}"} $memberPk ${LocalDateTime.now()}" }
    }

    /** RefreshToken 저장/갱신 */
    @Transactional
    fun saveOrUpdateRefreshToken(memberPk: Long, refreshToken: String) {
        val expiryTime = LocalDateTime.now()
            .plusSeconds(jwtTokenProvider.refreshTokenExpireTime)

        val token: RefreshToken = refreshTokenRepository.findByMemberPk(memberPk)
            ?: RefreshToken.create(
                memberPk = memberPk,
                token = refreshToken,
                expiry = expiryTime
            )

        token.updateToken(refreshToken, expiryTime)
        refreshTokenRepository.save(token)
    }

    /** Access/Refresh 발급 후 저장 */
    @Transactional
    private fun generateAndStoreTokens(member: Member): TokenResponse {
        val memberPk = member.id!!

        val accessToken = jwtTokenProvider.generateAccessToken(memberPk, member.role)
        val refreshToken = jwtTokenProvider.generateRefreshToken(memberPk, member.role)
        val refreshTokenMaxAge = jwtTokenProvider.refreshTokenExpireTime

        saveOrUpdateRefreshToken(memberPk, refreshToken)

        return TokenResponse.fromTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            refreshTokenMaxAge = refreshTokenMaxAge,
            role = member.role.name
        )
    }

    /**
     * ====== ======  ======
     * === 공통 유틸 메서드 ===
     * ====== ====== ======
     */

    /** 토큰에서 memberId 가져오기  */
    fun getMemberId(accessTokenHeader: String): Long {
        val token = extractToken(accessTokenHeader)
        return jwtTokenProvider.getMemberIdFromToken(token)
    }

    /** Bearer 접두사 제거  */
    private fun extractToken(headerValue: String?): String {
        val raw = headerValue ?: throw BusinessException(ErrorCode.TOKEN_NOT_FOUND)

        if (!raw.startsWith("Bearer ")) {
            throw BusinessException(ErrorCode.TOKEN_NOT_FOUND)
        }

        return raw.removePrefix("Bearer ").trim()
    }

    /** RefreshToken DB 검증  */
    private fun getValidatedRefreshToken(refreshToken: String, memberPk: Long): RefreshToken {
        val savedToken = refreshTokenRepository.findByMemberPk(memberPk)
            ?: throw BusinessException(ErrorCode.INVALID_REFRESH_TOKEN)

        if (savedToken.token != refreshToken) {
            throw BusinessException(ErrorCode.MISMATCH_REFRESH_TOKEN)
        }
        return savedToken
    }

    /** 토큰 상태 검증 (TokenStatus 기반)  */
    private fun validateTokenStatus(token: String) {
        when (jwtTokenProvider.validateTokenStatus(token)) {
            TokenStatus.EXPIRED -> throw BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN)
            TokenStatus.INVALID -> throw BusinessException(ErrorCode.INVALID_REFRESH_TOKEN)
            TokenStatus.VALID -> log.debug { "[Auth] 토큰 유효성 검증 완료" }
        }
    }
}