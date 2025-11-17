package com.backend.domain.plan.controller

import com.backend.domain.auth.service.AuthService
import com.backend.domain.plan.dto.*
import com.backend.domain.plan.service.PlanMemberService
import com.backend.domain.plan.service.PlanService
import com.backend.global.response.ApiResponse
import com.backend.global.response.ApiResponse.Companion.created
import com.backend.global.response.ApiResponse.Companion.success
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import lombok.RequiredArgsConstructor
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plan")
@Tag(name = "여행 계획 Api", description = "여행 계획을 생성하고 관리합니다.")
class PlanController(
    private val planService: PlanService,
    private val planMemberService: PlanMemberService,
    private val authService: AuthService
) {

    @PostMapping("/create")
    @Operation(summary = "여행 계획을 생성합니다.", description = "여행 계획을 생성합니다.")
    fun create(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) accessToken: String,
        @RequestBody planCreateRequestBody: @Valid PlanCreateRequestBody
    ): ApiResponse<PlanResponseBody> {
        val memberPkId = authService.getMemberId(accessToken)

        val plan = planService.createPlan(planCreateRequestBody, memberPkId)
        val planResponseBody = PlanResponseBody(plan)
        return created<PlanResponseBody>(planResponseBody)
    }

    @GetMapping("/list")
    fun getList(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) accessToken: String
    ): ApiResponse<List<PlanResponseBody>> {
        val memberPkId = authService.getMemberId(accessToken)
        val plans: List<PlanResponseBody> = planService.getPlanList(memberPkId)
        return success<List<PlanResponseBody>>(plans)
    }

    //초대가 승낙된 모임만 표시
    @GetMapping("/myInvitedPlan")
    fun getMyInvitedPlan(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) accessToken: String
    ): ApiResponse<List<PlanResponseBody>> {
        val memberPkId = authService.getMemberId(accessToken)
        val plans: List<PlanResponseBody> = planService.getInvitedAcceptedPlan(memberPkId)
        return success<List<PlanResponseBody>>(plans)
    }

    @GetMapping("/todayPlan")
    fun getTodayPlan(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) accessToken: String
    ): ApiResponse<PlanResponseBody?> {
        val memberPkId = authService.getMemberId(accessToken)
        return success<PlanResponseBody?>(planService.getTodayPlan(memberPkId))
    }

    @PatchMapping("/update/{planId}")
    fun updatePlan(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) accessToken: String,
        @RequestBody planUpdateRequestBody: @Valid PlanUpdateRequestBody,
        @PathVariable planId: Long
    ): ApiResponse<PlanResponseBody?> {
        //TODO JWT 토큰에서 멤버 아이디 정보 가져오기
        val memberPkId = authService.getMemberId(accessToken)

        val planResponseBody = planService.updatePlan(planId, planUpdateRequestBody, memberPkId)

        return success<PlanResponseBody?>(planResponseBody)
    }

    @GetMapping("/{planId}")
    fun getPlan(
        @PathVariable planId: @NotNull Long
    ): ApiResponse<PlanResponseBody> {
        val planResponseBody = planService.getPlanResponseBodyById(planId)
        return success<PlanResponseBody>(planResponseBody)
    }

    @DeleteMapping("/delete/{planId}")
    fun deletePlan(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) accessToken: String,
        @PathVariable planId: @NotNull Long
    ): ResponseEntity<*> {
        val memberPkId = authService.getMemberId(accessToken)

        planService.deletePlanById(planId, memberPkId)
        return ResponseEntity<Any?>(HttpStatus.OK)
    }

    @PostMapping("/member/invite")
    fun inviteMember(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) accessToken: String,
        @RequestBody memberRequestBody: @Valid PlanMemberAddRequestBody
    ): ApiResponse<PlanMemberResponseBody?> {
        val memberPkId = authService.getMemberId(accessToken)
        val planMemberResponseBody = planMemberService.invitePlanMember(memberRequestBody, memberPkId)
        return success<PlanMemberResponseBody?>(planMemberResponseBody)
    }

    @GetMapping("/member/mylist")
    fun getMyPlanMember(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) accessToken: String
    ): ApiResponse<List<PlanMemberMyResponseBody>> {
        val memberPkId = authService.getMemberId(accessToken)
        return success<List<PlanMemberMyResponseBody>>(planMemberService.myInvitedPlanList(memberPkId))
    }

    @PatchMapping("/member/accept")
    fun acceptMember(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) accessToken: String,
        @RequestBody memberAnswerRequestBody: @Valid PlanMemberAnswerRequestBody
    ): ApiResponse<PlanMemberResponseBody?> {
        val memberPkId = authService.getMemberId(accessToken)
        val planMemberResponseBody = planMemberService.acceptInvitePlanMember(memberAnswerRequestBody, memberPkId)

        return success<PlanMemberResponseBody?>(planMemberResponseBody)
    }

    @PatchMapping("/member/deny")
    fun denyMember(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) accessToken: String,
        @RequestBody memberAnswerRequestBody: @Valid PlanMemberAnswerRequestBody
    ): ApiResponse<PlanMemberResponseBody?> {
        val memberPkId = authService.getMemberId(accessToken)
        val planMemberResponseBody = planMemberService.denyInvitePlanMember(memberAnswerRequestBody, memberPkId)

        return success<PlanMemberResponseBody?>(planMemberResponseBody)
    }
}
