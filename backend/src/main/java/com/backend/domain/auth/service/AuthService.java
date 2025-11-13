package com.backend.domain.auth.service;

import com.backend.domain.auth.dto.reponse.TokenResponse;
import com.backend.domain.auth.entity.RefreshToken;
import com.backend.domain.auth.repository.RefreshTokenRepository;
import com.backend.domain.member.entity.Member;
import com.backend.domain.member.entity.MemberStatus;
import com.backend.domain.member.repository.MemberRepository;
import com.backend.global.exception.BusinessException;
import com.backend.global.jwt.JwtTokenProvider;
import com.backend.global.jwt.TokenStatus;
import com.backend.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 인증 서비스 (JWT 발급, 재발급, 로그아웃)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /** 로그인: AccessToken + RefreshToken 발급 */
    @Transactional
    public TokenResponse login(String loginId, String password) {

        Member member = memberRepository.findByMemberId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INACTIVE_MEMBER); // 새로운 에러 코드 추가
        }

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        Long memberPk = member.getId();

        String accessToken = jwtTokenProvider.generateAccessToken(memberPk, member.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(memberPk, member.getRole());
        long refreshTokenMaxAge = jwtTokenProvider.getRefreshTokenExpireTime();

        saveOrUpdateRefreshToken(memberPk, refreshToken);

        log.info("[Auth] 로그인 성공: memberPk={}, issuedAt={}", memberPk, LocalDateTime.now());

        return TokenResponse.of(accessToken, refreshToken, refreshTokenMaxAge, member.getRole().name());
    }

    /** AccessToken 재발급 */
    @Transactional
    public TokenResponse reissue(String refreshToken) {

        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.TOKEN_NOT_FOUND);
        }

        // 1. 유효성 검사
        validateTokenStatus(refreshToken);

        // 2. 토큰에서 memberPk 추출
        Long memberPk = jwtTokenProvider.getMemberIdFromToken(refreshToken);

        // 3. DB 토큰 검증
        RefreshToken savedToken = getValidatedRefreshToken(refreshToken, memberPk);

        // 4. RefreshToken에는 role 정보가 없기 때문에 MemberService로부터 다시 조회 필요
        Member member = memberRepository.findById(memberPk)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 5. 새 AccessToken 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(memberPk, member.getRole());
        long refreshTokenMaxAge = jwtTokenProvider.getRefreshTokenExpireTime();

        log.info("[Auth] AccessToken 재발급 완료: memberPk={}, reissuedAt={}", memberPk, LocalDateTime.now());

        return TokenResponse.of(newAccessToken, savedToken.getToken(), refreshTokenMaxAge, member.getRole().name());
    }

    @Transactional
    public void logout(String accessTokenHeader) {
        Long memberPk = jwtTokenProvider.getMemberIdFromToken(extractToken(accessTokenHeader));
        refreshTokenRepository.deleteByMemberPk(memberPk);
        log.info("[Auth] 로그아웃 완료: memberPk={}, deletedAt={}", memberPk, LocalDateTime.now());
    }

    /**
     * RefreshToken 생성 or 갱신
     */
    @Transactional
    private void saveOrUpdateRefreshToken(Long memberPk, String refreshToken) {
        LocalDateTime expiryTime = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshTokenExpireTime());

        RefreshToken token = refreshTokenRepository.findByMemberPk(memberPk)
                .orElseGet(() -> RefreshToken.builder()
                        .memberPk(memberPk)
                        .issuedAt(LocalDateTime.now())
                        .build());

        token.updateToken(refreshToken, expiryTime);
        refreshTokenRepository.save(token);
    }

    // === 공통 유틸 메서드 === //

    /** 토큰에서 memberId 가져오기 */
    public Long getMemberId(String accessTokenHeader) {
        String token = extractToken(accessTokenHeader);
        return jwtTokenProvider.getMemberIdFromToken(token);
    }

    /** Bearer 접두사 제거 */
    private String extractToken(String headerValue) {
        if (headerValue == null || !headerValue.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.TOKEN_NOT_FOUND);
        }
        return headerValue.replace("Bearer ", "").trim();
    }

    /** RefreshToken DB 검증 */
    private RefreshToken getValidatedRefreshToken(String refreshToken, Long memberPk) {
        RefreshToken savedToken = refreshTokenRepository.findByMemberPk(memberPk)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!savedToken.getToken().equals(refreshToken)) {
            throw new BusinessException(ErrorCode.MISMATCH_REFRESH_TOKEN);
        }
        return savedToken;
    }

    /** 토큰 상태 검증 (TokenStatus 기반) */
    private void validateTokenStatus(String token) {
        TokenStatus status = jwtTokenProvider.validateTokenStatus(token);

        switch (status) {
            case EXPIRED -> throw new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN);
            case INVALID -> throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
            case VALID -> log.debug("[Auth] 토큰 유효성 검증 완료");
        }
    }
}

