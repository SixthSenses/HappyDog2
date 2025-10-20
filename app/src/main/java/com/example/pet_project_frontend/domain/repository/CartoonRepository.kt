package com.example.pet_project_frontend.domain.repository

import android.net.Uri

/**
 * 만화 변환 작업 Repository
 */
interface CartoonRepository {
    /**
     * 만화 변환 작업 생성
     * @param userText 사용자가 입력한 텍스트 (선택사항)
     * @param imageUri 변환할 이미지 URI
     * @return Result<jobId>
     */
    suspend fun createCartoonJob(userText: String?, imageUri: Uri): Result<String>
    
    /**
     * 만화 작업 상태 조회
     * @param jobId 작업 ID
     * @return Result<CartoonJobStatus>
     */
    suspend fun getCartoonJobStatus(jobId: String): Result<CartoonJobStatus>
    
    /**
     * 만화 작업 취소
     * @param jobId 작업 ID
     * @return Result<Unit>
     */
    suspend fun cancelCartoonJob(jobId: String): Result<Unit>
}

/**
 * 만화 작업 상태
 */
data class CartoonJobStatus(
    val jobId: String,
    val status: String, // "pending", "processing", "completed", "failed", "cancelled"
    val resultImageUrl: String?,
    val errorMessage: String?
)
