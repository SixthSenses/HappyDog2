package com.example.pet_project_frontend.domain.repository

import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.request.UploadType
import com.example.pet_project_frontend.domain.model.CartoonJob
import com.example.pet_project_frontend.domain.model.CartoonJobHealth
import com.example.pet_project_frontend.domain.model.UploadResult
import java.io.File

/**
 * 만화 변환 및 업로드 Repository 인터페이스
 */
interface CartoonJobRepository {
    
    /**
     * 파일 업로드 URL 생성 및 파일 업로드
     * @param file 업로드할 파일
     * @param uploadType 업로드 타입
     * @param contentType MIME 타입 (예: "image/jpeg")
     * @return 업로드된 파일의 전체 공개 URL (예: "https://storage.googleapis.com/bucket/path")
     */
    suspend fun uploadFile(
        file: File,
        uploadType: UploadType,
        contentType: String
    ): AppResult<String> // 전체 Storage URL 반환
    
    /**
     * 만화 변환 작업 생성
     * @param filePath 변환할 이미지의 전체 Storage URL (예: "https://storage.googleapis.com/...")
     * @param userText 사용자 입력 텍스트 (선택 사항, 최대 500자)
     * @param idempotencyKey 멱등성 키 (null이면 자동 생성)
     * @return 생성된 작업 정보
     */
    suspend fun createCartoonJob(
        filePath: String,
        userText: String? = null,
        idempotencyKey: String? = null
    ): AppResult<CartoonJob>
    
    /**
     * 만화 변환 작업 상태 조회
     * @param jobId 작업 ID
     * @return 작업 정보
     */
    suspend fun getCartoonJob(jobId: String): AppResult<CartoonJob>
    
    /**
     * 만화 변환 작업 취소
     * @param jobId 작업 ID
     * @param idempotencyKey 멱등성 키 (null이면 자동 생성)
     * @return 취소된 작업 정보
     */
    suspend fun cancelCartoonJob(
        jobId: String,
        idempotencyKey: String? = null
    ): AppResult<CartoonJob>
    
    /**
     * 만화 변환 서비스 헬스 체크
     * @return 서비스 상태 정보
     */
    suspend fun getCartoonJobHealth(): AppResult<CartoonJobHealth>
    
    /**
     * 만화 이미지 공개 전환
     * @param filePath Storage 경로
     * @return 공개 URL
     */
    suspend fun finalizeCartoonImage(filePath: String): AppResult<String>
}
