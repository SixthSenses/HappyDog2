package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.request.CareRecordCreateRequest
import com.example.pet_project_frontend.data.remote.dto.request.CareRecordUpdateRequest
import com.example.pet_project_frontend.data.remote.dto.response.PetCareSettings
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordResponse
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordsResponse
import retrofit2.Response
import retrofit2.http.*

interface PetCareApi {
    // 펫 케어 설정 조회/수정 (인증 자동 부착)
    @GET("api/pet-care/settings")
    suspend fun getPetCareSettings(): PetCareSettings

    @PUT("api/pet-care/settings")
    suspend fun updatePetCareSettings(
        @Body settingsRequest: PetCareSettings
    ): PetCareSettings

    // 케어 기록 조회: 경로 기반 pet_id, 표준 sort 값(timestamp_asc|timestamp_desc)
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
    suspend fun createCareRecord(
        @Path("pet_id") petId: String,
        @Body recordRequest: CareRecordCreateRequest
    ): CareRecordResponse

    // 특정 타입의 케어 기록 조회: 경로 기반 pet_id 및 record_type
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