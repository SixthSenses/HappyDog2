package com.example.pet_project_frontend.data.repository

import com.example.pet_project_frontend.data.local.database.dao.HealthRecordDao
import com.example.pet_project_frontend.data.local.database.entities.HealthRecordEntity
import com.example.pet_project_frontend.data.remote.api.PetCareApi
import com.example.pet_project_frontend.data.remote.dto.request.*
import com.example.pet_project_frontend.data.remote.dto.response.*
import com.example.pet_project_frontend.domain.repository.PetCareRepository
import javax.inject.Inject

class PetCareRepositoryImpl @Inject constructor(
    private val petCareApi: PetCareApi,
    private val healthRecordDao: HealthRecordDao
) : PetCareRepository {
    
    override suspend fun getCareRecords(
        petId: String,
        date: String?,
        startDate: String?,
        endDate: String?,
        recordTypes: List<String>?,
        grouped: Boolean,
        limit: Int,
        cursor: String?,
        sort: String
    ): Result<CareRecordsResponse> = runCatching {
        val careRecords = petCareApi.getCareRecords(
            accessToken = "Bearer token", // TODO: 실제 토큰 사용
            petId = petId,
            date = date,
            startDate = startDate,
            endDate = endDate,
            recordTypes = recordTypes?.joinToString(","),
            grouped = grouped,
            limit = limit,
            cursor = cursor,
            sort = sort
        )
        
        // 로컬 DB에 저장
        careRecords.records.forEach { record ->
            healthRecordDao.insertHealthRecord(record.toHealthRecordEntity(petId))
        }
        
        careRecords
    }
    
    override suspend fun createCareRecord(
        petId: String,
        recordType: String,
        timestamp: Long,
        data: Any,
        notes: String?
    ): Result<CareRecordResponse> = runCatching {
        val request = CareRecordCreateRequest(
            recordType = recordType,
            timestamp = timestamp,
            data = data,
            notes = notes
        )
        val careRecord = petCareApi.createCareRecord(
            accessToken = "Bearer token", // TODO: 실제 토큰 사용
            recordRequest = request
        )
        
        // 로컬 DB에 저장
        healthRecordDao.insertHealthRecord(careRecord.toHealthRecordEntity(petId))
        
        careRecord
    }
    
    override suspend fun getRecordsByType(
        petId: String,
        recordType: String,
        date: String?,
        startDate: String?,
        endDate: String?,
        limit: Int,
        cursor: String?
    ): Result<CareRecordsResponse> = runCatching {
        val careRecords = petCareApi.getRecordsByType(
            accessToken = "Bearer token", // TODO: 실제 토큰 사용
            recordType = recordType,
            petId = petId,
            date = date,
            startDate = startDate,
            endDate = endDate,
            limit = limit,
            cursor = cursor
        )
        
        // 로컬 DB에 저장
        careRecords.records.forEach { record ->
            healthRecordDao.insertHealthRecord(record.toHealthRecordEntity(petId))
        }
        
        careRecords
    }
}

// Extension function for data conversion
private fun CareRecordResponse.toHealthRecordEntity(petId: String): HealthRecordEntity {
    return HealthRecordEntity(
        id = this.logId,
        petId = petId,
        recordType = this.recordType,
        value = this.data.toString(),
        unit = null,
        date = this.timestamp.toString(),
        notes = this.notes,
        createdAt = this.timestamp.toString()
    )
}
