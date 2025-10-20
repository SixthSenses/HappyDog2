package com.example.pet_project_frontend.util

import java.time.Duration
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimeUtil {
    /**
     * ISO 8601 타임스탬프를 상대적 시간 문자열로 변환
     * 
     * 규칙:
     * - 1분 미만: "방금 전"
     * - 1분 이상~59분 미만: "n분 전"
     * - 1시간 이상~24시간 미만: "n시간 전"
     * - 24시간 이상~1주 미만: "n일 전"
     * - 1주 이상~48주 미만: "n주 전"
     * - 48주 이상: "n년 전"
     */
    fun getRelativeTimeString(timestamp: String): String {
        return try {
            // ISO 8601 형식 파싱 (서버의 UTC 시간을 로컬 시간대로 변환)
            val zonedDateTime = if (timestamp.endsWith("Z") || timestamp.contains("+") || timestamp.contains("T")) {
                // UTC 또는 timezone 정보가 있는 경우
                ZonedDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME)
            } else {
                // timezone 정보가 없는 경우 서버를 UTC로 가정
                ZonedDateTime.parse(timestamp + "Z", DateTimeFormatter.ISO_DATE_TIME)
            }
            
            val now = ZonedDateTime.now(ZoneId.systemDefault())
            val postTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
            val duration = Duration.between(postTime, now)
            
            val seconds = duration.seconds.coerceAtLeast(0) // 음수 방지 (미래 시간 방지)
            
            when {
                seconds < 60 -> "방금 전"
                seconds < 3600 -> "${seconds / 60}분 전"
                seconds < 86400 -> "${seconds / 3600}시간 전"
                seconds < 604800 -> "${seconds / 86400}일 전"
                seconds < 29030400 -> "${seconds / 604800}주 전" // 48주 = 336일
                else -> "${seconds / 31536000}년 전"
            }
        } catch (e: Exception) {
            // 파싱 실패 시 원본 반환
            timestamp
        }
    }
}
