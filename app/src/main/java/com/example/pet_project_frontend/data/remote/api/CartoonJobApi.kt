package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.request.CartoonJobCreateRequest
import com.example.pet_project_frontend.data.remote.dto.request.EmptyRequest
import com.example.pet_project_frontend.data.remote.dto.response.CartoonJobHealthResponse
import com.example.pet_project_frontend.data.remote.dto.response.CartoonJobResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * 만화 변환(Cartoon) API 인터페이스
 * 비동기 이미지 변환 작업 관리
 */
interface CartoonJobApi {
    
    /**
     * 만화 변환 작업 생성 (POST /api/cartoon-jobs/)
     * 멱등성 적용: X-Idempotency-Key 헤더 사용
     * @return 202 Accepted - 작업이 큐에 추가됨
     */
    @POST("/api/cartoon-jobs/")
    suspend fun createCartoonJob(
        @Body request: CartoonJobCreateRequest,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null
    ): Response<CartoonJobResponse>
    
    /**
     * 만화 변환 작업 상태 조회 (GET /api/cartoon-jobs/{job_id})
     */
    @GET("/api/cartoon-jobs/{job_id}")
    suspend fun getCartoonJob(
        @Path("job_id") jobId: String
    ): Response<CartoonJobResponse>
    
    /**
     * 만화 변환 작업 취소 (DELETE /api/cartoon-jobs/{job_id})
     * 멱등성 적용: X-Idempotency-Key 헤더 사용
     */
    @HTTP(method = "DELETE", path = "/api/cartoon-jobs/{job_id}", hasBody = true)
    suspend fun cancelCartoonJob(
        @Path("job_id") jobId: String,
        @Body emptyRequest: EmptyRequest = EmptyRequest(),
        @Header("X-Idempotency-Key") idempotencyKey: String? = null
    ): Response<CartoonJobResponse>
    
    /**
     * 만화 변환 서비스 헬스 체크 (GET /api/cartoon-jobs/health)
     */
    @GET("/api/cartoon-jobs/health")
    suspend fun getCartoonJobHealth(): Response<CartoonJobHealthResponse>
}
