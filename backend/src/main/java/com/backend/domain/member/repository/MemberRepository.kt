package com.backend.domain.member.repository

import com.backend.domain.member.entity.Member
import com.backend.domain.member.entity.Provider
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MemberRepository : JpaRepository<Member, Long> {
    fun findByMemberId(memberId: String): Member?

    fun existsByMemberId(memberId: String): Boolean
    fun existsByEmail(email: String): Boolean
    fun existsByNickname(nickname: String): Boolean

    // provider + providerId 로 회원 조회
    fun findByProviderAndProviderId(provider: Provider, providerId: String): Member?
    fun existsByProviderAndProviderId(provider: Provider, providerId: String): Boolean
}