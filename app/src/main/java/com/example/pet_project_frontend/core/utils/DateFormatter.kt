package com.example.pet_project_frontend.core.utils

import java.time.*
import java.time.format.DateTimeFormatter

/**
 * 날짜/시간 UTC 헬퍼 유틸
 * - 서버 표준: ISO-8601, UTC 기준 문자열 전달을 권장
 */
object DateFormatter {
	private val isoDate: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
	private val isoInstant: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT

	// 오늘(UTC) yyyy-MM-dd
	fun todayUtcDate(): String = LocalDate.now(ZoneOffset.UTC).format(isoDate)

	// LocalDate -> yyyy-MM-dd (UTC 기준 명시적 포맷)
	fun toUtcDateString(date: LocalDate): String = date.atStartOfDay(ZoneOffset.UTC).toLocalDate().format(isoDate)

	// epoch milli -> ISO-8601 UTC 문자열
	fun epochMsToIsoUtc(ms: Long): String = Instant.ofEpochMilli(ms).atOffset(ZoneOffset.UTC).format(isoInstant)

	// LocalDate의 UTC 시작 시각(00:00:00Z) ISO 문자열
	fun localDateStartUtc(date: LocalDate): String = date.atStartOfDay(ZoneOffset.UTC).toInstant().atOffset(ZoneOffset.UTC).format(isoInstant)

	// LocalDate의 UTC 종료 시각(23:59:59.999...Z) ISO 문자열
	fun localDateEndUtc(date: LocalDate): String = date.plusDays(1).atStartOfDay(ZoneOffset.UTC)
		.toInstant().minusNanos(1).atOffset(ZoneOffset.UTC).format(isoInstant)

	/**
	 * 주간 범위(최근 7일) 계산: [startDate, endDate] 모두 포함(inclusive) 가정.
	 * today(UTC)를 포함하여 6일 전부터 오늘까지를 반환합니다.
	 * 반환 형식: Pair<yyyy-MM-dd, yyyy-MM-dd>
	 */
	fun weekRangeUtc(today: LocalDate = LocalDate.now(ZoneOffset.UTC)): Pair<String, String> {
		val start = today.minusDays(6)
		return toUtcDateString(start) to toUtcDateString(today)
	}

	/**
	 * 월간 범위 계산: 해당 월 1일 ~ today(UTC)까지 포함(inclusive) 가정.
	 * 반환 형식: Pair<yyyy-MM-dd, yyyy-MM-dd>
	 */
	fun monthRangeUtc(today: LocalDate = LocalDate.now(ZoneOffset.UTC)): Pair<String, String> {
		val start = today.withDayOfMonth(1)
		return toUtcDateString(start) to toUtcDateString(today)
	}
}