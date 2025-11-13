package com.backend.global.init

import com.backend.domain.member.entity.Member
import com.backend.domain.member.entity.Role
import com.backend.domain.member.repository.MemberRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.List

@Configuration
class BaseInitData(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder
) {
    private val log = KotlinLogging.logger {}

    @Bean
    fun InitData(): ApplicationRunner {
        return ApplicationRunner { args: ApplicationArguments? ->
            if (memberRepository.count() == 0L) {

                val member1 = Member(
                    memberId = "member1",
                    password = passwordEncoder.encode("1111"),
                    email = "member1@gmail.com",
                    nickname = "사용자1",
                    role = Role.USER,
                )

                val member2 = Member(
                    memberId = "member2",
                    password = passwordEncoder.encode("2222"),
                    email = "member2@gmail.com",
                    nickname = "사용자2",
                    role = Role.USER
                )

                val admin = Member(
                    memberId = "admin",
                    password = passwordEncoder.encode("admin1234"),
                    email = "admin@gmail.com",
                    nickname = "관리자",
                    role = Role.ADMIN
                )

                memberRepository.saveAll(List.of(member1, member2, admin))
                log.info { "초기 member 데이터 세팅 완료: " }
            }
        }
    }
}
