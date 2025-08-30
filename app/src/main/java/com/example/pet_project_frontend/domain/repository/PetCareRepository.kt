package com.example.pet_project_frontend.domain.repository

import com.example.pet_project_frontend.data.remote.dto.response.CareRecordsResponse
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordResponse

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
        sort: String = "desc"
    ): Result<CareRecordsResponse>
    
    suspend fun createCareRecord(
        petId: String,
        recordType: String,
        timestamp: Long,
        data: Any,
        notes: String? = null
    ): Result<CareRecordResponse>
    
    suspend fun getRecordsByType(
        petId: String,
        recordType: String,
        date: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        limit: Int = 10,
        cursor: String? = null
    ): Result<CareRecordsResponse>
}
