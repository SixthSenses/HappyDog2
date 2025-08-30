package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.request.CareRecordCreateRequest
import com.example.pet_project_frontend.data.remote.dto.response.PetCareSettings
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordResponse
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordsResponse
import retrofit2.http.*

interface PetCareApi {
    // 펫 케어 설정 조회
    @GET("api/pet-care/settings")
    suspend fun getPetCareSettings(@Header("Authorization") accessToken: String): PetCareSettings
    
    // 펫 케어 설정 수정
    @PUT("api/pet-care/settings")
    suspend fun updatePetCareSettings(
        @Header("Authorization") accessToken: String,
        @Body settingsRequest: PetCareSettings
    ): PetCareSettings
    
    // 케어 기록 조회
    @GET("api/pet-care/records")
    suspend fun getCareRecords(
        @Header("Authorization") accessToken: String,
        @Query("pet_id") petId: String,
        @Query("date") date: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("record_types") recordTypes: String? = null,
        @Query("grouped") grouped: Boolean = false,
        @Query("limit") limit: Int = 10,
        @Query("cursor") cursor: String? = null,
        @Query("sort") sort: String = "desc"
    ): CareRecordsResponse
    
    // 케어 기록 생성
    @POST("api/pet-care/records")
    suspend fun createCareRecord(
        @Header("Authorization") accessToken: String,
        @Body recordRequest: CareRecordCreateRequest
    ): CareRecordResponse
    
    // 특정 타입의 케어 기록 조회
    @GET("api/pet-care/records/{record_type}")
    suspend fun getRecordsByType(
        @Header("Authorization") accessToken: String,
        @Path("record_type") recordType: String,
        @Query("pet_id") petId: String,
        @Query("date") date: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("limit") limit: Int = 10,
        @Query("cursor") cursor: String? = null
    ): CareRecordsResponse
}