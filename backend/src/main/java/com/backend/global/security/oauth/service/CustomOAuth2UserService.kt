package com.backend.global.security.oauth.service

import com.backend.domain.member.entity.Provider
import com.backend.domain.member.repository.MemberRepository
import com.backend.global.exception.BusinessException
import com.backend.global.response.ErrorCode
import com.backend.global.security.oauth.provider.kakao.KakaoOAuthAttributes
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

/**
 * providerId, email 등을 우리가 필요한 형태로 재가공해 넘겨주는 역할
 */
@Service
class CustomOAuth2UserService(
    private val memberRepository: MemberRepository
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        // 1) 기본 OAuth2User 가져오기
        val oAuth2User = super.loadUser(userRequest)

        // 2) 어떤 provider인지 확인 (kakao, google 등)
        val registrationId = userRequest.clientRegistration.registrationId

        // 3) 카카오 전용 attributes 파싱
        if (registrationId == "kakao") {
            val kakaoAttributes = KakaoOAuthAttributes.Companion.of(oAuth2User.attributes)
            val providerId = kakaoAttributes.providerId

            // 4) 기존 회원 조회
            val existingMember = memberRepository.findByProviderAndProviderId(
                Provider.KAKAO,
                providerId
            )

            val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))

            return if (existingMember != null) {
                // 기존 회원: 그냥 UserDetails 포맷의 OAuth2User 반환
                DefaultOAuth2User(
                    authorities,
                    mapOf(
                        "provider" to Provider.KAKAO,
                        "providerId" to providerId
                    ),
                    "providerId"
                )
            } else {
                // 신규 회원: SuccessHandler에서 회원가입 분기
                DefaultOAuth2User(
                    authorities,
                    mapOf(
                        "provider" to Provider.KAKAO,
                        "providerId" to providerId,
                        "email" to kakaoAttributes.email
                    ),
                    "providerId"
                )
            }
        }
        throw BusinessException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER)
    }
}