package com.backend.global.config

import com.backend.global.security.jwt.JwtAuthenticationFilter
import com.backend.global.security.jwt.handler.JwtAccessDeniedHandler
import com.backend.global.security.jwt.handler.JwtAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration

/**
 * Spring Security의 전반적인 설정을 담당하는 클래스
 * 1. 인증(Authentication) - JWT 필터에서 검증
 * 2. 인가(Authorization) - 역할(Role)에 따른 접근 제어
 * 3. 세션 관리 - JWT는 Stateless(세션 비사용)
 * 4. 예외 처리 - 401 / 403 에러 핸들러 연결
 */
@Configuration
@EnableWebSecurity
open class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val jwtAccessDeniedHandler: JwtAccessDeniedHandler
) {
    @Bean
    @Throws(Exception::class)
    open fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors {
                it.configurationSource {
                    CorsConfiguration().apply {
                        allowCredentials = true
                        addAllowedOriginPattern("http://localhost:3000")
                        addAllowedHeader("*")
                        addAllowedMethod("*")
                        addExposedHeader("Set-Cookie")
                    }
                }
            }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/api/members/signup",
                    "/api/auth/**",
                    "/h2-console/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler)
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .headers { it.frameOptions { frame -> frame.disable() } }

        return http.build()
    }
}
