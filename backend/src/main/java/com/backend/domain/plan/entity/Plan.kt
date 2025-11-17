package com.backend.domain.plan.entity

import com.backend.domain.member.entity.Member
import com.backend.domain.plan.dto.PlanUpdateRequestBody
import jakarta.persistence.*
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle
import org.springframework.data.jpa.domain.AbstractAuditable_.createdDate
import org.springframework.data.jpa.domain.AbstractPersistable_.id
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
class Plan(
    id: Long? = null,
    member: Member,
    createdDate: LocalDateTime? = LocalDateTime.now(),
    modifyDate: LocalDateTime? = LocalDateTime.now(),
    startDate: LocalDateTime,
    endDate: LocalDateTime,
    title: String,
    content: String,
    version: Long? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id = id

    @ManyToOne(fetch = FetchType.LAZY)
    val member = member;

    @Column(nullable = false)
    val createDate = createdDate ?: LocalDateTime.now()

    @Column(nullable = false)
    val modifyDate = modifyDate ?: LocalDateTime.now()

    @Column(nullable = false)
    val startDate = startOfDay(startDate)

    @Column(nullable = false)
    val endDate = endOfDay(endDate)

    @Column(nullable = false, length = 50)
    val title = title

    @Column(columnDefinition = "TEXT")
    val content = content

    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
    val planMembers: MutableList<PlanMember?> = ArrayList<PlanMember?>()

    @Version
    val version: Long? = version

    fun updatePlan(planUpdateRequestBody: PlanUpdateRequestBody, member: Member): Plan {
        return Plan(
            this.id,
            member,
            createDate,
            LocalDateTime.now(),
            startOfDay(planUpdateRequestBody.startDate),
            endOfDay(planUpdateRequestBody.endDate),
            planUpdateRequestBody.title,
            planUpdateRequestBody.content,
            this.version
        )
    }


    fun startOfDay(date: LocalDateTime): LocalDateTime {
        return date.toLocalDate().atStartOfDay()
    }

    fun endOfDay(date: LocalDateTime): LocalDateTime {
        return date.toLocalDate().atTime(LocalTime.MAX)
    }


}
