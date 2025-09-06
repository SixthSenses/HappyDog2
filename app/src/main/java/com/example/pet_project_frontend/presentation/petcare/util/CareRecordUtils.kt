package com.example.pet_project_frontend.presentation.petcare.util

import com.example.pet_project_frontend.data.remote.dto.response.CareRecordResponse
import java.time.Instant
import java.time.ZoneId

fun toLong(any: Any?): Long = when (any) {
    is Number -> any.toLong()
    is String -> any.toLongOrNull() ?: 0L
    else -> 0L
}

fun toFloat(any: Any?): Float = when (any) {
    is Number -> any.toFloat()
    is String -> any.toFloatOrNull() ?: 0f
    else -> 0f
}

fun latestValue(list: List<CareRecordResponse>): String {
    val sorted = list.sortedBy { it.timestamp }
    return sorted.lastOrNull()?.data?.toString() ?: ""
}

fun previousValue(list: List<CareRecordResponse>): String {
    val sorted = list.sortedBy { it.timestamp }
    return if (sorted.size >= 2) sorted[sorted.size - 2].data.toString() else ""
}

fun aggregatePerHour(list: List<CareRecordResponse>): IntArray {
    val hours = IntArray(24)
    list.forEach { r ->
        val tsMs = if (r.timestamp > 10_000_000_000L) r.timestamp else r.timestamp * 1000
        val hour = Instant.ofEpochMilli(tsMs).atZone(ZoneId.systemDefault()).hour
        hours[hour] += toFloat(r.data).toInt()
    }
    return hours
}
