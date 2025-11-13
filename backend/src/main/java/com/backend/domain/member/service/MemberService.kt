package com.backend.domain.member.service

import com.backend.domain.member.dto.request.MemberSignupRequest
import com.backend.domain.member.dto.request.MemberUpdateRequest
import com.backend.domain.member.dto.response.MemberResponse
import com.backend.domain.member.entity.Member
import com.backend.domain.member.repository.MemberRepository
import com.backend.global.exception.BusinessException
import com.backend.global.response.ErrorCode
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Supplier

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun signup(request: MemberSignupRequest): MemberResponse {
        validateDuplicate(request)

        // 비밀번호 암호화
        val encodedPassword = passwordEncoder.encode(request.password)

        val member = request.toEntity(encodedPassword)
        memberRepository.save<Member?>(member)

        return MemberResponse.from(member)
    }

    //TODO: 수정 시 비밀번호 입력하기
    @Transactional
    fun updateMember(id: Long, request: MemberUpdateRequest): MemberResponse {
        val member = findById(id)

        if (request.email != null) member.updateEmail(request.email)
        if (request.nickname != null) member.updateNickname(request.nickname)

        return MemberResponse.from(member)
    }

    @Transactional
    fun deleteMember(id: Long): MemberResponse {
        val member = findById(id)

        if (member.isDeleted) {
            throw BusinessException(ErrorCode.ALREADY_DELETED_MEMBER)
        }

        member.delete()
        return MemberResponse.from(member)
    }

    /** 회원 조회용  */
    @Transactional
    fun getMember(id: Long): MemberResponse {
        val member = findById(id)
        return MemberResponse.from(member)
    }

    // TODO: Member, MemberResponse 각각 반환 메서드가 필요?
    @Transactional(readOnly = true)
    fun findByMemberId(memberId: String): Member =
        memberRepository.findByMemberId(memberId)
            ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)

    @Transactional(readOnly = true)
    fun findById(id: Long): Member =
        memberRepository.findById(id)
            .orElseThrow<BusinessException?>(Supplier { BusinessException(ErrorCode.MEMBER_NOT_FOUND) })


    @Transactional(readOnly = true)
    fun findByIdEntity(memberPk: Long): Member {
        return memberRepository.findById(memberPk)
            .orElseThrow<BusinessException?>(Supplier { BusinessException(ErrorCode.MEMBER_NOT_FOUND) })
    }

    private fun validateDuplicate(request: MemberSignupRequest) {
        if (memberRepository.existsByMemberId(request.memberId)) {
            throw BusinessException(ErrorCode.DUPLICATE_MEMBER_ID)
        }

        if (memberRepository.existsByEmail(request.email)) {
            throw BusinessException(ErrorCode.DUPLICATE_EMAIL)
        }

        if (memberRepository.existsByNickname(request.nickname)) {
            throw BusinessException(ErrorCode.DUPLICATE_NICKNAME)
        }
    }
}
