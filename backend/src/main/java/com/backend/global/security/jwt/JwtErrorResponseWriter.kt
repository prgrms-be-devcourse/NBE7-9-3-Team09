package com.backend.global.security.jwt

import com.backend.global.response.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class JwtErrorResponseWriter(
    private val objectMapper: ObjectMapper
) {

    @Throws(IOException::class)
    fun write(response: HttpServletResponse, errorCode: ErrorCode) {
        response.status = errorCode.status.value()
        response.contentType = "application/json;charset=UTF-8"

        val errorBody = mapOf(
            "code" to errorCode.code,
            "status" to errorCode.status.value(),
            "message" to errorCode.message
        )

        objectMapper.writeValue(response.writer, errorBody)
    }
}