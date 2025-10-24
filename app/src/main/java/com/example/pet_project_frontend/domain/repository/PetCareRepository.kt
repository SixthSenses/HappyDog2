package com.example.pet_project_frontend.domain.repository

import com.example.pet_project_frontend.data.remote.dto.response.CareRecordsResponse
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordResponse
import com.example.pet_project_frontend.data.remote.dto.response.DailyRecordsResponse
import com.example.pet_project_frontend.data.remote.dto.response.DailySummaryResponse
import com.example.pet_project_frontend.data.remote.dto.response.RangeSummaryResponse
import com.example.pet_project_frontend.data.remote.dto.request.PetCareSettingsRequest
import com.example.pet_project_frontend.data.remote.dto.response.PetCareSettings
import com.example.pet_project_frontend.data.remote.dto.request.CareRecordUpdateRequest

import com.example.pet_project_frontend.core.common.AppResult
interface PetCareRepository {
    // 일일 기록 조회 (특정 날짜의 모든 케어 기록)
    suspend fun getDailyRecords(
        petId: String,
        date: String? = null // yyyy-MM-dd 형식, null이면 오늘
    ): AppResult<DailyRecordsResponse>
    
    // 일일 요약 + 목표 진행률 조회
    suspend fun getDailySummary(
        petId: String,
        date: String? = null // yyyy-MM-dd 형식, null이면 오늘
    ): AppResult<DailySummaryResponse>
    
    // 기간 요약 + 트렌드 + 목표 추적 조회
    suspend fun getRangeSummary(
        petId: String,
        startDate: String, // yyyy-MM-dd 형식
        endDate: String // yyyy-MM-dd 형식
    ): AppResult<RangeSummaryResponse>
    
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
        data: Any, // String 또는 Map<String, Any> 모두 지원
        memo: String? = null
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
    suspend fun getPetCareSettings(petId: String): AppResult<PetCareSettings>
    suspend fun updatePetCareSettings(petId: String, settings: PetCareSettingsRequest): AppResult<PetCareSettings>

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

    // 몸무게 월간 분석
    suspend fun getWeightMonthlyAnalysis(petId: String): AppResult<com.example.pet_project_frontend.data.remote.dto.response.WeightMonthlyAnalysisResponse>
}
