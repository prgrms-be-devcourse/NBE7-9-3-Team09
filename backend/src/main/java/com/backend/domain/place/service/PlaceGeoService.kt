package com.backend.domain.place.service

import com.backend.domain.place.repository.PlaceRepository
import com.backend.global.kakao.local.KakaoLocalApiClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import io.github.oshai.kotlinlogging.KotlinLogging

@Service
class PlaceGeoService(
    private val placeRepository: PlaceRepository,
    private val kakaoLocalApiClient: KakaoLocalApiClient,
) {

    private val log = KotlinLogging.logger {}

    // PlaceGeoService 안에 있는 주소 정제 함수 교체
    private fun normalizeAddress(raw: String): String {
        var result = raw

        // 1) 괄호 안 내용 제거: (신정동), (청량리동) 등
        result = result.substringBefore("(")

        // 2) 콤마 뒤 추가 정보 제거: ", 1층 ~", ", 105~107호" 등
        result = result.substringBefore(",")

        // 3) 공백 정리
        result = result.replace(Regex("\\s+"), " ").trim()

        return result
    }

    /**
     * ✅ 2) 한 번에 최대 100개만 처리하는 기존 배치 함수
     */
    @Transactional
    fun fillMissingCoordinates(batchSize: Int = 100): Int {
        // 여기서 어차피 100개만 가져옴
        val places = placeRepository.findTop100ByLatitudeIsNullOrderByIdAsc()

        if (places.isEmpty()) {
            log.info { "✅ 좌표가 비어 있는 Place가 더 이상 없습니다." }
            return 0
        }

        log.info { "🔍 좌표 미설정 Place ${places.size}개 처리 시작" }

        var successCount = 0

        for (place in places) {
            val address = place.address

            if (address.isNullOrBlank()) {
                log.warn { "⚠️ address가 비어 있어 좌표를 구할 수 없습니다. placeId=${place.id}, name=${place.placeName}" }
                continue
            }

            try {
                val normalized = normalizeAddress(address)
                val coord = kakaoLocalApiClient.searchAddress(normalized)

                if (coord == null) {
                    log.warn {
                        "⚠️ 카카오에서 좌표를 찾지 못했습니다. " +
                                "placeId=${place.id}, 원본주소=$address, 정제주소=$normalized"
                    }
                    continue
                }

                place.latitude = coord.latitude
                place.longitude = coord.longitude
                successCount++

                log.info {
                    "📍 좌표 설정 완료: placeId=${place.id}, name=${place.placeName}, " +
                            "lat=${coord.latitude}, lng=${coord.longitude}"
                }

            } catch (ex: Exception) {
                log.error(ex) {
                    "❌ 좌표 변환 중 오류 발생. placeId=${place.id}, address=$address"
                }
            }
        }

        log.info { "✅ 이번 배치 완료: 성공 ${successCount}개 / 전체 ${places.size}개" }
        return successCount
    }

    /**
     * ✅ 3) 남은 좌표 없을 때까지 ‘계속’ 돌리는 버전
     *  - 여기서 진짜 전체 다 채움
     */
    @Transactional
    fun fillAllMissingCoordinates(): Int {
        var totalSuccess = 0

        while (true) {
            val places = placeRepository.findTop100ByLatitudeIsNullOrderByIdAsc()

            if (places.isEmpty()) {
                log.info { "🎉 더 이상 처리할 Place가 없습니다. 전체 완료!" }
                break
            }

            log.info { "🔁 새 배치 시작: ${places.size}개 처리 시도" }

            var successCount = 0

            for (place in places) {
                val address = place.address

                if (address.isNullOrBlank()) {
                    log.warn { "⚠️ address가 비어 있어 좌표를 구할 수 없습니다. placeId=${place.id}, name=${place.placeName}" }
                    continue
                }

                try {
                    val normalized = normalizeAddress(address)
                    val coord = kakaoLocalApiClient.searchAddress(normalized)

                    if (coord == null) {
                        log.warn {
                            "⚠️ 카카오에서 좌표를 찾지 못했습니다. " +
                                    "placeId=${place.id}, 원본주소=$address, 정제주소=$normalized"
                        }
                        continue
                    }

                    place.latitude = coord.latitude
                    place.longitude = coord.longitude
                    successCount++
                    totalSuccess++

                    log.info {
                        "📍 좌표 설정 완료: placeId=${place.id}, name=${place.placeName}, " +
                                "lat=${coord.latitude}, lng=${coord.longitude}"
                    }

                } catch (ex: Exception) {
                    log.error(ex) {
                        "❌ 좌표 변환 중 오류 발생. placeId=${place.id}, address=$address"
                    }
                }
            }

            log.info { "✅ 이번 배치 성공: $successCount 개, 누적: $totalSuccess 개" }

            // 🔥 여기 추가 – 더 이상 새로 채운 게 없으면 루프 종료
            if (successCount == 0) {
                log.warn {
                    "⚠️ 이번 배치에서 새로 좌표를 채운 Place가 없습니다. " +
                            "남은 애들은 카카오에서 매칭이 잘 안 되는 주소일 가능성이 큽니다. 루프를 종료합니다."
                }
                break
            }

            Thread.sleep(200L) // 선택: 카카오 쿼터 보호용
        }

        log.info { "🎉 전체 좌표 채우기 완료: 총 $totalSuccess 개 성공" }
        return totalSuccess
    }
}