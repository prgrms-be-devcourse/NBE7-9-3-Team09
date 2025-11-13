package com.backend.domain.member.repository

import com.backend.domain.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MemberRepository : JpaRepository<Member, Long> {
    fun findByMemberId(memberId: String): Member?

    fun existsByMemberId(memberId: String): Boolean
    fun existsByEmail(email: String): Boolean
    fun existsByNickname(nickname: String): Boolean
}
