package com.backend.global.security.oauth.provider.kakao

class KakaoOAuthAttributes(
    val providerId: String,   // 카카오 고유 회원 번호(id)
    val email: String         // 필수 제공 이메일 (null 불가)
) {
    companion object {

        // 카카오 OAuth 응답(Map<String, Any>)에서 필요한 값만 추출하는 메서드
        fun of(attributes: Map<String, Any>): KakaoOAuthAttributes {

            // 카카오 회원 고유 ID(id)
            val kakaoId = attributes["id"].toString()

            // kakao_account 객체에는 이메일 등 계정 정보가 들어 있음
            val account = attributes["kakao_account"] as Map<String, Any>

            // 이메일은 필수 동의이므로 null 이면 안됨
            val email = account["email"] as String

            // 필요한 값만 담아 반환
            return KakaoOAuthAttributes(
                providerId = kakaoId,
                email = email
            )
        }
    }
}