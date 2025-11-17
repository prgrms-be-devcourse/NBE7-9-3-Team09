package com.backend.global.kakao.local

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class KakaoLocalTestController(
    private val kakaoLocalApiClient: KakaoLocalApiClient
) {

    @GetMapping("/api/test/geocode")
    fun geocode(@RequestParam address: String): ResponseEntity<Any> {
        val coord = kakaoLocalApiClient.searchAddress(address)
            ?: return ResponseEntity.badRequest().body("좌표를 찾을 수 없습니다.")

        return ResponseEntity.ok(coord)
    }
}