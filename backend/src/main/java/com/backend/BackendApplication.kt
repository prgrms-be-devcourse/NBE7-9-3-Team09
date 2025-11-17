package com.backend

import com.backend.global.config.KakaoProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(KakaoProperties::class)
class BackendApplication

fun main(args: Array<String>){
    runApplication<BackendApplication>(*args)
}

