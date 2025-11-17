package com.backend.domain.plan.service

import com.backend.domain.member.entity.Member
import com.backend.domain.member.service.MemberService
import com.backend.domain.plan.dto.PlanMemberAddRequestBody
import com.backend.domain.plan.dto.PlanMemberAnswerRequestBody
import com.backend.domain.plan.dto.PlanMemberMyResponseBody
import com.backend.domain.plan.dto.PlanMemberResponseBody
import com.backend.domain.plan.entity.PlanMember
import com.backend.domain.plan.repository.PlanMemberRepository
import com.backend.global.exception.BusinessException
import com.backend.global.response.ErrorCode
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service
import java.util.function.Supplier

@Slf4j
@Service
@RequiredArgsConstructor
class PlanMemberService(
    private val planMemberRepository: PlanMemberRepository,
    private val planService: PlanService,
    private val memberService: MemberService
) {


    fun invitePlanMember(requestBody: PlanMemberAddRequestBody, memberId: Long): PlanMemberResponseBody {
        val planMember = isValidInvite(requestBody, memberId)
        planMemberRepository.save<PlanMember?>(planMember)
        return PlanMemberResponseBody(planMember)
    }

    fun myInvitedPlanList(memberPkId: Long): List<PlanMemberMyResponseBody> {
        // TODO : 이후 ID 값만 있는 멤버 객체를 생성하는 방법 찾기
        val member: Member? = memberService.findById(memberPkId)

        val planMemberList = planMemberRepository.getPlanMembersByMember(member)
        val myPlanMemberList =
            planMemberList.map{
                PlanMemberMyResponseBody(it)
            }.toList()

        return myPlanMemberList
    }

    fun DeletePlanMember(requestBody: PlanMemberAddRequestBody, memberPkId: Long): PlanMemberResponseBody {
        val planMember : PlanMember = planMemberRepository
            .getMyInviteByMemberIdAndPlanId(requestBody.planId,requestBody.memberId,memberPkId)
            ?: throw BusinessException(ErrorCode.NOT_FOUND_INVITE)

        planMemberRepository.delete(planMember)
        return PlanMemberResponseBody(planMember)
    }

    fun acceptInvitePlanMember(requestBody: PlanMemberAnswerRequestBody, memberPkId: Long): PlanMemberResponseBody {
        val planMember = isMyInvite(requestBody, memberPkId)
        planMember.inviteAccept()
        planMemberRepository.save<PlanMember>(planMember)
        return PlanMemberResponseBody(planMember)
    }

    fun denyInvitePlanMember(requestBody: PlanMemberAnswerRequestBody, memberPkId: Long): PlanMemberResponseBody {
        val planMember = isMyInvite(requestBody, memberPkId)
        planMember.inviteDeny()
        planMemberRepository.save<PlanMember>(planMember)
        return PlanMemberResponseBody(planMember)
    }

    // 초대가 유효한지 검사합니다.
    private fun isValidInvite(requestBody: PlanMemberAddRequestBody, memberId: Long): PlanMember {
        val plan = planService.getPlanById(requestBody.planId)
        if (plan.member.id != memberId) {
            throw BusinessException(ErrorCode.NOT_MY_PLAN)
        }

        val invitedMember = memberService.findById(requestBody.memberId)

        // 데이터 베이스 오류 처리를 서비스 로직 처리로 변경
        if (planMemberRepository.existsByMemberInPlanId(
                invitedMember.id?:throw BusinessException(ErrorCode.INVALID_MEMBER),
                plan.id?:throw BusinessException(ErrorCode.NOT_FOUND_PLAN))) {
            throw BusinessException(ErrorCode.DUPLICATE_MEMBER_INVITE)
        }


        val planMember = PlanMember(null, invitedMember, plan, null, null, 0)
        return planMember
    }

    // 내 초대가 맞는지 검사합니다.
    private fun isMyInvite(requestBody: PlanMemberAnswerRequestBody, memberPkId: Long): PlanMember {
        // TODO : 이후 ID 값만 있는 멤버 객체를 생성하는 방법 찾기
        val member: Member = memberService.findById(memberPkId)

        val plan = planService.getPlanById(requestBody.planId)

        if (requestBody.memberId != member.id) {
            throw BusinessException(ErrorCode.NOT_MY_PLAN)
        }

        val planMember = planMemberRepository.findById(requestBody.planMemberId).orElseThrow<BusinessException?>(
            Supplier { BusinessException(ErrorCode.NOT_FOUND_INVITE) }
        )

        return planMember
    }

    fun isAvailablePlanMember(planId: Long, member: Member): Boolean {
        return  planMemberRepository.existsByPlanIdAndMemberIdAndIsConfirmed(
            planId?: throw BusinessException(ErrorCode.NOT_FOUND_PLAN),
            member.id?: throw BusinessException(ErrorCode.INVALID_MEMBER),
            1)

    //        return planMemberRepository.existsByMemberInPlanId(member.id?: throw BusinessException(ErrorCode.INVALID_MEMBER)
//        , planId)
    }
}
