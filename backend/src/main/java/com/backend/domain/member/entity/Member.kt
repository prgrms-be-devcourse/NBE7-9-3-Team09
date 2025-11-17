package com.backend.domain.member.entity

import com.backend.global.entity.BaseEntity
import jakarta.persistence.*
import lombok.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "member")
class Member(

    /* === 소셜 로그인 정보 === */
    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 20)
    var provider: Provider? = null,  // LOCAL, KAKAO, GOOGLE…

    @Column(nullable = true, unique = true)
    var providerId: String? = null, // 소셜 로그인 고유 ID (카카오: kakao id)

    /* === 일반/소셜 공통 정보 === */
    @Column(nullable = false, unique = true, length = 30)
    val memberId: String, // 로그인용 아이디 / 카카오: 자동 생성

    @Column(nullable = false)
    var password: String, // 암호화된 비밀번호 / 카카오: "{SOCIAL_LOGIN}"

    @Column(nullable = false, unique = true, length = 200)
    var email: String, // 중복 가입 방지

    @Column(nullable = false, unique = true, length = 20)
    var nickname: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var role: Role = Role.USER,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var status: MemberStatus = MemberStatus.ACTIVE,

    @Column(name = "deleted_at")
    private var deletedAt: LocalDateTime? = null
) : BaseEntity() {
    //TODO: name

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set // 외부 수정 금지 (DB에서 자동 생성만 허용)

    /* === 비즈니스 로직 === */

    val isDeleted: Boolean
        get() = deletedAt != null

    fun delete() {
        check(this.status != MemberStatus.DELETED) { "이미 탈퇴한 회원입니다." }
        this.status = MemberStatus.DELETED
        this.deletedAt = LocalDateTime.now()
    }

    fun updatePassword(newPassword: String) {
        this.password = newPassword
    }

    fun updateNickname(newNickname: String) {
        this.nickname = newNickname
    }

    fun updateEmail(newEmail: String) {
        this.email = newEmail
    }

    /* === 정적 팩토리: 일반 회원 생성 === */
    companion object {
        fun createLocal(
            memberId: String,
            password: String,
            email: String,
            nickname: String
        ): Member {
            return Member(
                provider = Provider.LOCAL,
                providerId = null,
                memberId = memberId,
                password = password,
                email = email,
                nickname = nickname,
                role = Role.USER,
                status = MemberStatus.ACTIVE
            )
        }

        /* === 정적 팩토리: 카카오 회원 생성 === */
        fun createKakao(
            providerId: String,
            email: String,
            nickname: String
        ): Member {
            return Member(
                provider = Provider.KAKAO,
                providerId = providerId,
                memberId = "social_" + java.util.UUID.randomUUID().toString().replace("-", "").take(16),      // 자동 생성
                password = "{SOCIAL_LOGIN}",         // 소셜 회원 dummy PW
                email = email,
                nickname = nickname,
                role = Role.USER,
                status = MemberStatus.ACTIVE
            )
        }

        @JvmStatic
        fun builder() = Builder()
    }

    // === Builder 사용을 위한 팩토리 ===

    class Builder {
        private var id: Long? = null
        private var memberId: String = ""
        private var password: String = ""
        private var email: String = ""
        private var nickname: String = ""
        private var role: Role = Role.USER
        private var status: MemberStatus = MemberStatus.ACTIVE
        private var deletedAt: LocalDateTime? = null

        fun id(id: Long?) = apply { this.id = id }
        fun memberId(memberId: String) = apply { this.memberId = memberId }
        fun password(password: String) = apply { this.password = password }
        fun email(email: String) = apply { this.email = email }
        fun nickname(nickname: String) = apply { this.nickname = nickname }
        fun role(role: Role) = apply { this.role = role }
        fun status(status: MemberStatus) = apply { this.status = status }
        fun deletedAt(deletedAt: LocalDateTime?) = apply { this.deletedAt = deletedAt }

        fun build(): Member {
            return Member(
                memberId = memberId,
                password = password,
                email = email,
                nickname = nickname,
                role = role,
                status = status,
                deletedAt = deletedAt
            )
        }
    }
}
