package com.backend.domain.member.controller

import com.backend.domain.member.dto.request.MemberSignupRequest
import com.backend.domain.member.dto.request.MemberUpdateRequest
import com.backend.domain.member.dto.response.MemberResponse
import com.backend.domain.member.service.MemberService
import com.backend.global.response.ApiResponse
import com.backend.global.security.user.CustomUserDetails
import jakarta.validation.Valid
import lombok.RequiredArgsConstructor
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequiredArgsConstructor
@RequestMapping("api/members")
class MemberController(
    private val memberService: MemberService
) {

    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: MemberSignupRequest
    ): ApiResponse<MemberResponse> {
        val response = memberService.signup(request)
        return ApiResponse.created(response)
    }

    @GetMapping("/me")
    fun getMyInfo(@AuthenticationPrincipal member: CustomUserDetails
    ): ApiResponse<MemberResponse> {
        val response = memberService.getMember(member.id!!)
        return ApiResponse.success(response, "내 정보 조회 성공")
    }

    @PatchMapping("/me")
    fun updateMember(
        @AuthenticationPrincipal member: CustomUserDetails,
        @RequestBody request: @Valid MemberUpdateRequest
    ): ApiResponse<MemberResponse> {
        val response = memberService.updateMember(member.id!!, request)
        return ApiResponse.success(response, "회원정보가 수정되었습니다")
    }

    @DeleteMapping("/me")
    fun deleteMember(@AuthenticationPrincipal member: CustomUserDetails
    ): ApiResponse<MemberResponse> {
        val response = memberService.deleteMember(member.id!!)
        return ApiResponse.success(response, "회원 탈퇴가 완료되었습니다.")
    }
}
