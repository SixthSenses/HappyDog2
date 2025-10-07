package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * 만화 변환 작업 응답
 */
data class CartoonJobResponse(
    @SerializedName("job_id")
    val jobId: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("status")
    val status: String, // "pending", "processing", "completed", "failed", "cancelled"
    @SerializedName("original_image_url")
    val originalImageUrl: String?,
    @SerializedName("result_image_url")
    val resultImageUrl: String?,
    @SerializedName("error_message")
    val errorMessage: String?,
    @SerializedName("created_at")
    val createdAt: String, // ISO 8601
    @SerializedName("updated_at")
    val updatedAt: String? // ISO 8601
)

/**
 * 만화 변환 서비스 헬스 체크 응답
 */
data class CartoonJobHealthResponse(
    @SerializedName("active_jobs")
    val activeJobs: Int,
    @SerializedName("queue_size")
    val queueSize: Int,
    @SerializedName("max_workers")
    val maxWorkers: Int,
    @SerializedName("integration_health")
    val integrationHealth: Map<String, String>
)

/**
 * 만화 이미지 공개 전환 응답
 */
data class FinalizeCartoonResponse(
    @SerializedName("public_url")
    val publicUrl: String
)
