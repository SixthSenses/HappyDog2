package com.example.pet_project_frontend.domain.model

import java.time.LocalDateTime

/**
 * 만화 변환 작업
 */
data class CartoonJob(
    val jobId: String,
    val userId: String,
    val status: CartoonJobStatus,
    val originalImageUrl: String?,
    val resultImageUrl: String?,
    val errorMessage: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)

/**
 * 만화 변환 작업 상태
 */
enum class CartoonJobStatus(val value: String) {
    PENDING("pending"),
    PROCESSING("processing"),
    COMPLETED("completed"),
    FAILED("failed"),
    CANCELLED("cancelled");
    
    companion object {
        fun fromString(value: String): CartoonJobStatus {
            return entries.find { it.value == value } ?: FAILED
        }
    }
}

/**
 * 만화 변환 서비스 헬스 정보
 */
data class CartoonJobHealth(
    val activeJobs: Int,
    val queueSize: Int,
    val maxWorkers: Int,
    val integrationHealth: Map<String, String>
)

/**
 * 업로드 결과
 */
data class UploadResult(
    val uploadUrl: String,
    val filePath: String
)
