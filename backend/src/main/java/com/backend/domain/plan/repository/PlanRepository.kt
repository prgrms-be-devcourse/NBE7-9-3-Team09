package com.backend.domain.plan.repository

import com.backend.domain.plan.entity.Plan
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PlanRepository : JpaRepository<Plan?, Long?> {
    fun getPlansByMember_MemberId(memberID: String?): List<Plan>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun getPlanById(id: Long): Plan?

    fun getPlansByMember_Id(memberId: Long): List<Plan>

    fun getPlanByStartDateAndMemberId(startDate: LocalDateTime?, memberId: Long?): Plan?

    fun getPlanByTitle(title: String): Plan?

    fun getPlanByStartDateBeforeAndEndDateAfter(startDateBefore: LocalDateTime, endDateAfter: LocalDateTime): Plan?

    @Query("""
        SELECT 
        Plan
        FROM
            Plan p,
            PlanMember pm
        WHERE
            p.id = pm.plan.id
            AND
            pm.member.id = :memberId
            AND
            pm.isConfirmed = 1
    """)
    fun getMyInvitedAcceptedPlansByMemberId(@Param("memberId") memberPkId: Long): List<Plan>
    fun getPlanByStartDateBeforeAndEndDateAfterAndMemberId(
        startDateBefore: LocalDateTime,
        endDateAfter: LocalDateTime,
        memberId: Long
    ): Plan

    @Query("""
        SELECT
        COUNT(p) > 0
        FROM 
        Plan p
        WHERE 
        p.member.id = :memberId
        AND NOT 
        (:endTime <= p.startDate OR :startTime >= p.endDate)
    """)
    fun existsOverlappingPlan(
        @Param("memberId") memberId: Long,
        @Param("startTime") startTime: LocalDateTime,
        @Param("endTime") endTime: LocalDateTime
    ) : Boolean
}
