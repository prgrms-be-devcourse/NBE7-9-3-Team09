package com.backend.global.init

import com.backend.domain.member.entity.Member
import com.backend.domain.member.entity.Role
import com.backend.domain.member.repository.MemberRepository
import com.backend.domain.place.repository.PlaceRepository
import com.backend.domain.place.service.PlaceGeoService
import com.backend.domain.plan.detail.dto.PlanDetailRequestBody
import com.backend.domain.plan.detail.entity.PlanDetail
import com.backend.domain.plan.detail.repository.PlanDetailRepository
import com.backend.domain.plan.entity.Plan
import com.backend.domain.plan.entity.PlanMember
import com.backend.domain.plan.repository.PlanMemberRepository
import com.backend.domain.plan.repository.PlanRepository
import com.backend.external.seoul.hotel.controller.HotelImportController
import com.backend.external.seoul.modelrestaurant.controller.ModelRestaurantImportController
import com.backend.external.seoul.nightspot.controller.controller.NightSpotImportController
import com.backend.global.exception.BusinessException
import com.backend.global.response.ErrorCode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.List


@Configuration
class BaseInitData(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val planRepository: PlanRepository,
    private val planMemberRepository: PlanMemberRepository,
    private val hotelImportController: HotelImportController,
    private val nightSpotImportController: NightSpotImportController,
    private val modelRestaurantImportController: ModelRestaurantImportController,
    private val placeRepository: PlaceRepository,
    private val planDetailRepository: PlanDetailRepository,
    private val placeGeoService: PlaceGeoService,

    ) {
    private val log = KotlinLogging.logger {}

    @Bean
    fun InitData(): ApplicationRunner {
        return ApplicationRunner { args: ApplicationArguments? ->
            if (memberRepository.count() == 0L) {

                val member1 = Member.createLocal(
                    memberId = "member1",
                    password = passwordEncoder.encode("1111"),
                    email = "member1@gmail.com",
                    nickname = "사용자1",
                )

                val member2 = Member.createLocal(
                    memberId = "member2",
                    password = passwordEncoder.encode("2222"),
                    email = "member2@gmail.com",
                    nickname = "사용자2",
                )

                val admin = Member.createLocal(
                    memberId = "admin",
                    password = passwordEncoder.encode("admin1234"),
                    email = "admin@gmail.com",
                    nickname = "관리자",
                ).apply {
                    role = Role.ADMIN
                }

                memberRepository.saveAll(List.of(member1, member2, admin))
                log.info { "초기 member 데이터 세팅 완료: " }
            }
            if(placeRepository.count() == 0L) {
                hotelImportController.importHotels();
                log.info { "초기 호텔 데이터 세팅 완료 " }

                nightSpotImportController.importNightSpots()
                log.info { "야경명소 Import 완료" }

                modelRestaurantImportController.importAll()
                log.info { "초기 모범음식점 데이터 세팅 완료" }

                val filledCount = placeGeoService.fillAllMissingCoordinates()
                log.info { "초기 Place 좌표 세팅 완료: $filledCount 개" }
            }

            if (planRepository.count() == 0L) {
                val plan1: Plan = Plan(
                    null,
                    memberRepository.getReferenceById(1L),
                    null,
                    null,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1L),
                    "초기 일정 데이터1",
                    "초기 일정 데이터 내용"
                )


                val plan2: Plan = Plan(
                    null,
                    memberRepository.getReferenceById(2L),
                    null,
                    null,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1L),
                    "초기 일정 데이터2",
                    "초기 일정 데이터 내용2"
                )

                val plan3: Plan = Plan(
                    null,
                    memberRepository.getReferenceById(1L),
                    null,
                    null,
                    LocalDateTime.now().plusDays(3L),
                    LocalDateTime.now().plusDays(5L),
                    "초기 일정 데이터2",
                    "초기 일정 데이터 내용2"
                )

                planRepository.saveAll(List.of<Plan>(plan1, plan2, plan3))

                val planMember1 = PlanMember(
                    null,
                    memberRepository.getReferenceById(1L),
                    planRepository.getReferenceById(1L)?:throw Exception(),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    1
                )

                val planMember2 = PlanMember(
                    null,
                    memberRepository.getReferenceById(2L),
                    planRepository.getReferenceById(2L)?:throw Exception(),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    1
                )

                val planMember3 = PlanMember(
                    null,
                    memberRepository.getReferenceById(1L),
                    planRepository.getReferenceById(3L)?:throw Exception(),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    1
                )

                val planMember4 = PlanMember(
                    null,
                    memberRepository.getReferenceById(2L),
                    planRepository.getReferenceById(1L)?:throw Exception(),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    1
                )

                planMemberRepository.saveAll(List.of<PlanMember>(planMember1, planMember2, planMember3,planMember4))
                log.info("초기 plan 데이터 세팅 완료: ")
            }

            if(planDetailRepository.count() == 0L) {
                var planDetailRequestBody : PlanDetailRequestBody = PlanDetailRequestBody(
                    1L,
                    1L,
                    LocalDateTime.now().plusHours(1),
                    LocalDateTime.now().plusHours(2),
                    "초기 여행 데이터 상세1",
                    "초기 여행 데이터 상세 내용1"
                )

                val planDetail1: PlanDetail = PlanDetail(
                    memberRepository.getReferenceById(1L),
                    planRepository.getReferenceById(1L)?:throw BusinessException(ErrorCode.NOT_FOUND_PLACE),
                    placeRepository.getReferenceById(1L),
                    planDetailRequestBody
                )

                planDetailRepository.save<PlanDetail>(planDetail1)
            }


        }
    }
}
