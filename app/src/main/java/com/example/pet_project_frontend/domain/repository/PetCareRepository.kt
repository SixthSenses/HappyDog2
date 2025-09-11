package com.example.pet_project_frontend.domain.repository

import com.example.pet_project_frontend.data.remote.dto.response.CareRecordsResponse
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordResponse
import com.example.pet_project_frontend.data.remote.dto.response.PetCareSettings
import com.example.pet_project_frontend.data.remote.dto.request.CareRecordUpdateRequest

import com.example.pet_project_frontend.core.common.AppResult
interface PetCareRepository {
    suspend fun getCareRecords(
        petId: String,
        date: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        recordTypes: List<String>? = null,
        grouped: Boolean = false,
        limit: Int = 10,
        cursor: String? = null,
        // API 표준값은 timestamp_desc|timestamp_asc
        sort: String = "timestamp_desc"
    ): AppResult<CareRecordsResponse>
    
    suspend fun createCareRecord(
        petId: String,
        recordType: String,
        timestamp: Long,
        data: Any,
        notes: String? = null,
        requestId: String? = null
    ): AppResult<CareRecordResponse>
    
    suspend fun getRecordsByType(
        petId: String,
        recordType: String,
        date: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        limit: Int = 10,
        cursor: String? = null
    ): AppResult<CareRecordsResponse>

    // 펫케어 설정 조회/수정
    suspend fun getPetCareSettings(): AppResult<PetCareSettings>
    suspend fun updatePetCareSettings(settings: PetCareSettings): AppResult<PetCareSettings>

    // 기록 수정/삭제
    suspend fun updateCareRecord(
        petId: String,
        logId: String,
        update: CareRecordUpdateRequest
    ): AppResult<CareRecordResponse>

    suspend fun deleteCareRecord(
        petId: String,
        logId: String
    ): AppResult<Unit>
}
