package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.request.CareRecordCreateRequest
import com.example.pet_project_frontend.data.remote.dto.request.CareRecordUpdateRequest
import com.example.pet_project_frontend.data.remote.dto.request.PetCareSettingsRequest
import com.example.pet_project_frontend.data.remote.dto.response.PetCareSettings
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordResponse
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordsResponse
import com.example.pet_project_frontend.data.remote.dto.response.DailyRecordsResponse
import com.example.pet_project_frontend.data.remote.dto.response.DailySummaryResponse
import com.example.pet_project_frontend.data.remote.dto.response.RangeSummaryResponse
import retrofit2.Response
import retrofit2.http.*

interface PetCareApi {
    // 펫 케어 설정 조회/수정 (인증 자동 부착)
    @GET("api/pet-care/{pet_id}/settings")
    suspend fun getPetCareSettings(@Path("pet_id") petId: String): PetCareSettings

    @PUT("api/pet-care/{pet_id}/settings")
    @Headers("Content-Type: application/json")
    suspend fun updatePetCareSettings(
        @Path("pet_id") petId: String,
        @Body settingsRequest: PetCareSettingsRequest
    ): PetCareSettings

    // 일일 케어 기록 조회 (특정 날짜의 모든 기록)
    @GET("api/pet-care/{pet_id}/records/daily")
    suspend fun getDailyRecords(
        @Path("pet_id") petId: String,
        @Query("date") date: String? = null // yyyy-MM-dd 형식, 없으면 오늘
    ): DailyRecordsResponse

    // 일일 요약 + 목표 진행률 조회
    @GET("api/pet-care/{pet_id}/records/daily/summary")
    suspend fun getDailySummary(
        @Path("pet_id") petId: String,
        @Query("date") date: String? = null // yyyy-MM-dd 형식, 없으면 오늘
    ): DailySummaryResponse

    // 기간 요약 + 트렌드 + 목표 추적 조회
    @GET("api/pet-care/{pet_id}/records/summary/range")
    suspend fun getRangeSummary(
        @Path("pet_id") petId: String,
        @Query("start_date") startDate: String, // yyyy-MM-dd 형식
        @Query("end_date") endDate: String // yyyy-MM-dd 형식
    ): RangeSummaryResponse

    // 케어 기록 조회: GET 메서드로 변경 (레거시, 필요시 사용) -> 사용 안하는 것 같음
    @GET("api/pet-care/{pet_id}/records")
    suspend fun getCareRecords(
        @Path("pet_id") petId: String,
        @Query("date") date: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("record_types") recordTypes: String? = null,
        @Query("grouped") grouped: Boolean = false,
        @Query("limit") limit: Int = 10,
        @Query("cursor") cursor: String? = null,
        @Query("sort") sort: String = "timestamp_desc"
    ): CareRecordsResponse

    // 케어 기록 생성: 경로 기반 pet_id
    @POST("api/pet-care/{pet_id}/records")
    @Headers("Content-Type: application/json")
    suspend fun createCareRecord(
        @Path("pet_id") petId: String,
        @Body recordRequest: CareRecordCreateRequest
    ): CareRecordResponse

    // 특정 타입의 케어 기록 조회: 경로 기반 pet_id 및 record_type -> 사용 안하는것 같음
    @GET("api/pet-care/{pet_id}/records/{record_type}")
    suspend fun getRecordsByType(
        @Path("pet_id") petId: String,
        @Path("record_type") recordType: String,
        @Query("date") date: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("limit") limit: Int = 10,
        @Query("cursor") cursor: String? = null
    ): CareRecordsResponse

    // 케어 기록 수정: 부분 업데이트(PATCH)
    @PATCH("api/pet-care/{pet_id}/records/{log_id}")
    suspend fun updateCareRecord(
        @Path("pet_id") petId: String,
        @Path("log_id") logId: String,
        @Body updateRequest: CareRecordUpdateRequest
    ): CareRecordResponse

    // 케어 기록 삭제
    @DELETE("api/pet-care/{pet_id}/records/{log_id}")
    suspend fun deleteCareRecord(
        @Path("pet_id") petId: String,
        @Path("log_id") logId: String
    ): Response<Unit>
}