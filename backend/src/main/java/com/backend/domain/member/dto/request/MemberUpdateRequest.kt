package com.backend.domain.member.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class MemberUpdateRequest(
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    val email: String? = null,

    @field:NotBlank(message = "닉네임을 입력해주세요")
    val nickname: String? = null
)