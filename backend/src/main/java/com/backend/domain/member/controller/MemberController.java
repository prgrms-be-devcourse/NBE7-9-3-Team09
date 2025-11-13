package com.backend.domain.member.controller;

import com.backend.domain.member.dto.request.MemberSignupRequest;
import com.backend.domain.member.dto.request.MemberUpdateRequest;
import com.backend.domain.member.dto.response.MemberResponse;
import com.backend.domain.member.service.MemberService;
import com.backend.global.response.ApiResponse;
import com.backend.global.security.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ApiResponse<MemberResponse> signup(@Valid @RequestBody MemberSignupRequest request) {
        MemberResponse response = memberService.signup(request);
        return ApiResponse.created(response);
    }

    @GetMapping("/me")
    public ApiResponse<MemberResponse> getMyInfo(@AuthenticationPrincipal CustomUserDetails member) {
        MemberResponse response = memberService.getMember(member.getId());
        return ApiResponse.success(response, "내 정보 조회 성공");
    }

    @PatchMapping("/me")
    public ApiResponse<MemberResponse> updateMember(
            @AuthenticationPrincipal CustomUserDetails member,
            @Valid @RequestBody MemberUpdateRequest request
    ) {
        MemberResponse response = memberService.updateMember(member.getId(), request);
        return ApiResponse.success(response, "회원정보가 수정되었습니다");
    }

    @DeleteMapping("/me")
    public ApiResponse<MemberResponse> deleteMember(@AuthenticationPrincipal CustomUserDetails member) {
        MemberResponse response = memberService.deleteMember(member.getId());
        return ApiResponse.success(response, "회원 탈퇴가 완료되었습니다.");
    }
}
