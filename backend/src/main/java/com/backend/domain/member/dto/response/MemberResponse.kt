package com.backend.domain.member.dto.response

import com.backend.domain.member.entity.Member
import com.backend.domain.member.entity.Role

data class MemberResponse(
    val id: Long?,
    val memberId: String,
    val email: String,
    val nickname: String,
    val role: Role
) {
    companion object {
        fun from(member: Member): MemberResponse =
            MemberResponse(
                id = member.id,
                memberId = member.memberId,
                email = member.email,
                nickname = member.nickname,
                role = member.role
            )
    }
}
