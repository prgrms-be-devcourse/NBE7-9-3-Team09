package com.backend.global.kakao.local.dto

data class KakaoAddressResponse(
    val documents: List<KakaoDocument> = emptyList()
)

data class KakaoDocument(
    val x: String,   // 경도
    val y: String,   // 위도
    val address_name: String? = null,
    val road_address_name: String? = null,
)

// 좌표만 담는 DTO
data class KakaoCoordinate(
    val latitude: Double,   // y
    val longitude: Double   // x
)