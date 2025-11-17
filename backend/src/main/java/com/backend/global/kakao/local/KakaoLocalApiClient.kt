package com.backend.global.kakao.local

import com.backend.global.config.KakaoProperties
import com.backend.global.kakao.local.dto.KakaoAddressResponse
import com.backend.global.kakao.local.dto.KakaoCoordinate
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class KakaoLocalApiClient(
    private val restTemplate: RestTemplate,
    private val kakaoProperties: KakaoProperties
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun searchAddress(query: String): KakaoCoordinate? {
        if (query.isBlank()) return null

        val url = UriComponentsBuilder
            .fromHttpUrl("https://dapi.kakao.com/v2/local/search/address.json")
            .queryParam("query", query)
            .build()
            .toUriString()

        val headers = HttpHeaders().apply {
            set("Authorization", "KakaoAK ${kakaoProperties.restApiKey}")
        }

        val entity = HttpEntity<Void>(headers)

        return try {
            log.info("🔍 Kakao Local API 주소 검색 호출: query={}", query)

            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                KakaoAddressResponse::class.java
            )

            val body = response.body ?: return null
            val doc = body.documents.firstOrNull() ?: return null

            KakaoCoordinate(
                latitude = doc.y.toDouble(),
                longitude = doc.x.toDouble()
            )
        } catch (ex: Exception) {
            log.error("❌ Kakao Local API 호출 실패. query={}", query, ex)
            null
        }
    }
}