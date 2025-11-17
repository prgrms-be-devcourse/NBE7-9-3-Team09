package com.backend.domain.place.controller

import com.backend.domain.place.service.PlaceGeoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/places")
class PlaceGeoAdminController(
    private val placeGeoService: PlaceGeoService,
) {

    // (1) ⛏ 한 번만, 최대 batchSize개 처리 (기존 기능)
    @PostMapping("/fill-coordinates")
    fun fillMissingCoordinates(
        @RequestParam(defaultValue = "100") batchSize: Int,
    ): ResponseEntity<FillCoordinatesResponse> {

        val processedCount = placeGeoService.fillMissingCoordinates(batchSize)

        val message = if (processedCount == 0) {
            "좌표가 비어 있는 Place가 더 이상 없습니다."
        } else {
            "이번 배치에서 좌표를 채운 Place 개수: $processedCount"
        }

        return ResponseEntity.ok(
            FillCoordinatesResponse(
                requestedBatchSize = batchSize,
                processedCount = processedCount,
                message = message,
            )
        )
    }


    // (2) 🔥 전체 좌표를 다 채울 때까지 반복
    @PostMapping("/fill-coordinates/all")
    fun fillAllMissingCoordinates(): ResponseEntity<FillCoordinatesResponse> {

        val totalProcessed = placeGeoService.fillAllMissingCoordinates()

        val message = if (totalProcessed == 0) {
            "좌표가 비어 있는 Place가 더 이상 없습니다."
        } else {
            "전체 반복 실행 완료. 총 좌표를 채운 Place 개수: $totalProcessed"
        }

        return ResponseEntity.ok(
            FillCoordinatesResponse(
                requestedBatchSize = -1,  // 의미 없음
                processedCount = totalProcessed,
                message = message,
            )
        )
    }
}

data class FillCoordinatesResponse(
    val requestedBatchSize: Int,
    val processedCount: Int,
    val message: String,
)