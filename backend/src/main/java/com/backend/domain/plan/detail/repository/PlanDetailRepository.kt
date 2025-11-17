package com.backend.domain.plan.detail.repository

import com.backend.domain.plan.detail.entity.PlanDetail
import com.backend.domain.plan.entity.Plan
import jakarta.validation.constraints.NotNull
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

interface PlanDetailRepository : JpaRepository<PlanDetail?, Long?> {
    fun getPlanDetailById(planDetailId: Long): Optional<PlanDetail>

    fun getPlanDetailsByPlanId(planId: Long): MutableList<PlanDetail>

    @Query("""
SELECT COUNT(pd)>0
FROM PlanDetail pd
WHERE pd.plan.id = :planId
AND NOT (:endTime < pd.startTime OR :startTime > pd.endTime)
AND (:#{#detailId} IS NULL OR pd.id != :detailId)
    """)
    fun existsOverlapping(
        @Param("planId") planId: Long,
        @Param("startTime") startTime: LocalDateTime,
        @Param("endTime") endTime: LocalDateTime,
        @Param("detailId") detailId: Long?
    ): Boolean

    fun deletePlanDetailsByPlan(plan: Plan)

    fun getPlanDetailsByMemberId(memberId: Long): MutableList<PlanDetail>
    fun findByTitle(title: String): MutableList<PlanDetail>
    fun deletePlanDetailsByTitle(title: String)
    fun deletePlanDetailById(id: Long)


}
