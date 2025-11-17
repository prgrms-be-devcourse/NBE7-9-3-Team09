package com.backend.domain.plan.detail.controller

import com.backend.domain.auth.service.AuthService
import com.backend.domain.plan.detail.dto.PlanDetailRequestBody
import com.backend.domain.plan.detail.dto.PlanDetailResponseBody
import com.backend.domain.plan.detail.dto.PlanDetailsElementBody
import com.backend.domain.plan.detail.service.PlanDetailService
import com.backend.global.response.ApiResponse
import com.backend.global.response.ApiResponse.Companion.created
import com.backend.global.response.ApiResponse.Companion.success
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Null
import lombok.RequiredArgsConstructor
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/plan/detail")
@RequiredArgsConstructor
class PlanDetailController(
    private val planDetailService: PlanDetailService,
    private val authService: AuthService

) {

    @PostMapping("/add")
    fun addPlanDetail(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) accessToken: String,
        @RequestBody planDetailRequestBody: @Valid PlanDetailRequestBody
    ): ApiResponse<PlanDetailResponseBody> {
        val memberPkId = authService.getMemberId(accessToken)
        val planDetail = planDetailService.addPlanDetail(planDetailRequestBody, memberPkId)

        return created<PlanDetailResponseBody>(
            PlanDetailResponseBody(planDetail)
        )
    }

    @GetMapping("/{planDetailId}")
    fun getPlanDetail(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) accessToken: String,
        @PathVariable planDetailId: @NotNull Long
    ): ApiResponse<PlanDetailsElementBody> {
        val memberPkId = authService.getMemberId(accessToken)

        val planDetailsElementBody = planDetailService.getPlanDetailById(planDetailId, memberPkId)

        return success<PlanDetailsElementBody>(
            planDetailsElementBody
        )
    }

    @GetMapping("/{planId}/list")
    fun getAllPlanDetail(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) accessToken: String,
        @PathVariable planId: @NotNull Long
    ): ApiResponse<List<PlanDetailsElementBody>> {
        val memberPkId = authService.getMemberId(accessToken)
        val planDetailsElementBodies = planDetailService.getPlanDetailsByPlanId(planId, memberPkId)
        return success<List<PlanDetailsElementBody>>(planDetailsElementBodies,"계획 상세 조회에 성공했습니다.")
    }

    @GetMapping("/{planId}/todaylist")
    fun getTodayPlanDetail(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) accessToken: String,
        @PathVariable planId: @NotNull Long
    ): ApiResponse<List<PlanDetailsElementBody>?> {
        val memberPkId = authService.getMemberId(accessToken)
        val planDetailsElementBodies = planDetailService.getTodayPlanDetails(planId, memberPkId)
        return success<List<PlanDetailsElementBody>?>(planDetailsElementBodies,"오늘 계획 상세 조회에 성공했습니다.")
    }


    @PatchMapping("/update/{planDetailId}")
    fun updatePlanDetail(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) accessToken: String,
        @PathVariable planDetailId: @NotNull Long,
        @RequestBody planDetailRequestBody: @Valid PlanDetailRequestBody
    ): ApiResponse<PlanDetailResponseBody> {
        val memberPkId = authService.getMemberId(accessToken)

        val planDetailResponseBody =
            planDetailService.updatePlanDetail(planDetailRequestBody, memberPkId, planDetailId)
        return success<PlanDetailResponseBody>(
            planDetailResponseBody
        )
    }

    @DeleteMapping("/delete/{detailId}")
    fun deletePlanDetail(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) accessToken: String,
        @PathVariable detailId: @NotNull Long
    ): ApiResponse<Null?> {
        val memberPkId = authService.getMemberId(accessToken)
        planDetailService.deletePlanDetail(detailId, memberPkId)
        return success<Null?>()
    }
}
