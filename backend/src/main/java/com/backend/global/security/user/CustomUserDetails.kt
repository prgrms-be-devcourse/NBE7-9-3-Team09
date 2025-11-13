package com.backend.global.security.user

import com.backend.domain.member.entity.Member
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(member: Member) : UserDetails {

    val id: Long? = member.id
    val memberId: String = member.memberId
    private val password: String = member.password
    val nickname: String = member.nickname
    private val role: String = member.role.name

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        mutableSetOf(GrantedAuthority { "ROLE_$role" })

    override fun getPassword() = password

    override fun getUsername() = memberId // 로그인용 ID 기준

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun isCredentialsNonExpired() = true

    override fun isEnabled() = true
}
