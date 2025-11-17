package com.backend.domain.auth.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class OAuth2SignupRequest(

    // 신규 가입 시 유저가 입력하는 닉네임
    @field:NotBlank(message = "닉네임은 필수 입력값입니다.")
    @field:Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다.")
    val nickname: String,

    // SuccessHandler가 프론트로 전달한 임시 토큰
    @field:NotBlank(message = "임시 토큰은 필수입니다.")
    val tempToken: String
)