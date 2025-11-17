package com.backend.domain.member.dto.request

import com.backend.domain.member.entity.Member
import com.backend.domain.member.entity.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class MemberSignupRequest(
    @field:NotBlank(message = "아이디는 필수 입력값입니다.")
    @field:Size(min = 4, max = 20, message = "아이디는 4~20자여야 합니다.")
    val memberId: String,

    @field:NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @field:Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    @field:Pattern(
        regexp = "^[A-Za-z0-9!@#\$%^&*()_+=\\-{}\\[\\]:;\"'<>,.?/]+$",
        message = "비밀번호는 영문자, 숫자, 특수문자만 포함할 수 있습니다."
    )
    val password: String,

    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    val email: String,

    @field:NotBlank(message = "닉네임은 필수 입력값입니다.")
    val nickname: String
) {

}
