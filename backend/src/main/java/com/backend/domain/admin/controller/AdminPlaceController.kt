package com.backend.domain.admin.controller

import com.backend.domain.admin.service.AdminPlaceService
import com.backend.domain.place.dto.RequestPlaceDto
import com.backend.domain.place.dto.ResponsePlaceDto
import com.backend.global.response.ApiResponse
import com.backend.global.response.ApiResponse.Companion.success
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/places")
class AdminPlaceController(
    private val adminPlaceService: AdminPlaceService
) {

    /** 전체 장소 조회  */
    @GetMapping
    fun getAllPlaces(): ApiResponse<List<ResponsePlaceDto>> =
        success(adminPlaceService.getAllPlaces())

    /** 카테고리별 장소 조회 */
    @GetMapping("/category/{categoryId}")
    fun getPlacesByCategory(@PathVariable categoryId: Int): ApiResponse<List<ResponsePlaceDto>> =
        success(adminPlaceService.getPlacesByCategory(categoryId))

    /** 장소 단건 조회 */
    @GetMapping("/{id}")
    fun getPlace(@PathVariable id: Long): ApiResponse<ResponsePlaceDto> =
        success(adminPlaceService.getPlace(id))

    /** 장소 등록 */
    @PostMapping
    fun createPlace(@RequestBody dto: RequestPlaceDto): ApiResponse<Unit> {
        adminPlaceService.createPlace(dto)
        return success(Unit, "장소 등록 완료")
    }

    /** 장소 수정 */
    @PutMapping("/{id}")
    fun updatePlace(
        @PathVariable id: Long,
        @RequestBody dto: RequestPlaceDto
    ): ApiResponse<ResponsePlaceDto> =
        success(adminPlaceService.updatePlace(id, dto))

    /** 장소 삭제 */
    @DeleteMapping("/{id}")
    fun deletePlace(@PathVariable id: Long): ApiResponse<Unit> {
        adminPlaceService.deletePlace(id)
        return success(Unit, "장소 삭제 완료")
    }
}
