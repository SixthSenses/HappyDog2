package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * 만화 작업 응답
 * GET /api/cartoon-jobs/{job_id}
 * POST /api/cartoon-jobs/ (202)
 * DELETE /api/cartoon-jobs/{job_id}
 */
data class CartoonJobResponse(
    @SerializedName("job_id")
    val jobId: String,
    
    @SerializedName("user_id")
    val userId: String,
    
    @SerializedName("status")
    val status: String, // "pending", "processing", "completed", "failed", "cancelled"
    
    @SerializedName("original_image_url")
    val originalImageUrl: String,
    
    @SerializedName("result_image_url")
    val resultImageUrl: String?,
    
    @SerializedName("user_text")
    val userText: String?,
    
    @SerializedName("error_message")
    val errorMessage: String?,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String
)
