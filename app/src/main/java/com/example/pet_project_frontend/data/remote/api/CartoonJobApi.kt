package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.request.CartoonJobCreateRequest
import com.example.pet_project_frontend.data.remote.dto.response.CartoonJobResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * 만화 변환 작업 API
 */
interface CartoonJobApi {
    /**
     * 만화 변환 작업 생성
     * POST /api/cartoon-jobs/
     */
    @POST("api/cartoon-jobs/")
    suspend fun createCartoonJob(
        @Body request: CartoonJobCreateRequest
    ): Response<CartoonJobResponse>
    
    /**
     * 만화 작업 상태 조회
     * GET /api/cartoon-jobs/{job_id}
     */
    @GET("api/cartoon-jobs/{job_id}")
    suspend fun getCartoonJobStatus(
        @Path("job_id") jobId: String
    ): Response<CartoonJobResponse>
    
    /**
     * 만화 작업 취소
     * DELETE /api/cartoon-jobs/{job_id}
     */
    @DELETE("api/cartoon-jobs/{job_id}")
    suspend fun cancelCartoonJob(
        @Path("job_id") jobId: String
    ): Response<CartoonJobResponse>
}
