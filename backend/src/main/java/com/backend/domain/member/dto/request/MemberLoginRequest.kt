package com.backend.domain.member.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class MemberLoginRequest(
    @field:NotBlank(message = "아이디는 필수 입력값입니다.")
    @field:Size(min = 4, max = 20, message = "아이디는 4~20자여야 합니다.")
    val memberId: String,

    @field:NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @field:Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    val password: String
)
