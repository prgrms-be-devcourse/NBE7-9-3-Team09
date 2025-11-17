package com.backend.domain.plan.util

import java.time.LocalDateTime
import java.time.LocalTime

object TimeUtil {
    fun LocalDateTime.startOfTodayPlusOneSecond() : LocalDateTime = LocalDateTime.now().toLocalDate().atStartOfDay().plusSeconds(1);
    fun LocalDateTime.endOfTodayMinusOneSecond() : LocalDateTime = LocalDateTime.now().toLocalDate().atTime(LocalTime.MAX).minusSeconds(1);
    fun LocalDateTime.startOfToday(): LocalDateTime = this.toLocalDate().atStartOfDay();
    fun LocalDateTime.endOfToday() : LocalDateTime = this.toLocalDate().atTime(LocalTime.MAX);
}