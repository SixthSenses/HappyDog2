// app/src/main/java/com/example/pet_project_frontend/data/repository/PetCareRepositoryImpl.kt

package com.example.pet_project_frontend.data.repository

import android.util.Log
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.core.common.SafeApi
import com.example.pet_project_frontend.data.local.preferences.TokenManager
import com.example.pet_project_frontend.data.remote.api.PetCareApi
import com.example.pet_project_frontend.data.remote.dto.request.CareRecordCreateRequest
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordResponse
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordsResponse
import com.example.pet_project_frontend.data.remote.dto.response.DailyRecordsResponse
import com.example.pet_project_frontend.data.remote.dto.response.DailySummaryResponse
import com.example.pet_project_frontend.data.remote.dto.response.RangeSummaryResponse
import com.example.pet_project_frontend.data.remote.dto.request.PetCareSettingsRequest
import com.example.pet_project_frontend.data.remote.dto.response.PetCareSettings
import com.example.pet_project_frontend.data.remote.dto.request.CareRecordUpdateRequest
import com.example.pet_project_frontend.domain.repository.PetCareRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetCareRepositoryImpl @Inject constructor(
    private val petCareApi: PetCareApi,
    private val tokenManager: TokenManager
) : PetCareRepository {

    companion object {
        private const val TAG = "PetCareRepositoryImpl"
    }

    override suspend fun getDailyRecords(
        petId: String,
        date: String?
    ): AppResult<DailyRecordsResponse> {
        return SafeApi.body {
            Log.d(TAG, "Getting daily care records for pet: $petId, date: $date")
            val response = petCareApi.getDailyRecords(
                petId = petId,
                date = date
            )
            Log.d(TAG, "Daily care records fetched successfully: ${response.records.size} records")
            response
        }
    }

    override suspend fun getDailySummary(
        petId: String,
        date: String?
    ): AppResult<DailySummaryResponse> {
        return SafeApi.body {
            Log.d(TAG, "Getting daily summary for pet: $petId, date: $date")
            val response = petCareApi.getDailySummary(
                petId = petId,
                date = date
            )
            Log.d(TAG, "Daily summary fetched successfully")
            response
        }
    }

    override suspend fun getRangeSummary(
        petId: String,
        startDate: String,
        endDate: String
    ): AppResult<RangeSummaryResponse> {
        return SafeApi.body {
            Log.d(TAG, "Getting range summary for pet: $petId, period: $startDate ~ $endDate")
            val response = petCareApi.getRangeSummary(
                petId = petId,
                startDate = startDate,
                endDate = endDate
            )
            Log.d(TAG, "Range summary fetched successfully: ${response.recordsByDate.size} days")
            response
        }
    }

    // 사용 안하는 것 같음 -> 확인해보기
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
    ): AppResult<CareRecordsResponse> {
        return SafeApi.body {
            Log.d(TAG, "Getting care records for pet: $petId")
            val response = petCareApi.getCareRecords(
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
            Log.d(TAG, "Care records fetched successfully: ${response.records.size} records")
            response
        }
    }

    override suspend fun createCareRecord(
        petId: String,
        recordType: String,
        timestamp: Long,
        data: Any, // String 또는 Map<String, Any> 모두 지원
        memo: String?
    ): AppResult<CareRecordResponse> {
        return SafeApi.body {
            Log.d(TAG, "Creating care record for pet: $petId, type: $recordType")
            val request = CareRecordCreateRequest(
                recordType = recordType,
                timestamp = timestamp,
                data = data,
                memo = memo
            )
            val response = petCareApi.createCareRecord(petId = petId, recordRequest = request)
            Log.d(TAG, "Care record created successfully: ${response.logId}")
            response
        }
    }
    // 사용 안하는 것 같음 -> 확인해보기
    override suspend fun getRecordsByType(
        petId: String,
        recordType: String,
        date: String?,
        startDate: String?,
        endDate: String?,
        limit: Int,
        cursor: String?
    ): AppResult<CareRecordsResponse> {
        return SafeApi.body {
            Log.d(TAG, "Getting care records by type for pet: $petId, type: $recordType")
            val response = petCareApi.getRecordsByType(
                petId = petId,
                recordType = recordType,
                date = date,
                startDate = startDate,
                endDate = endDate,
                limit = limit,
                cursor = cursor
            )
            Log.d(TAG, "Care records by type fetched successfully: ${response.records.size} records")
            response
        }
    }

    override suspend fun getPetCareSettings(petId: String): AppResult<PetCareSettings> {
        return SafeApi.body { 
            Log.d(TAG, "Getting pet care settings for pet: $petId")
            petCareApi.getPetCareSettings(petId) 
        }
    }

    override suspend fun updatePetCareSettings(petId: String, settings: PetCareSettingsRequest): AppResult<PetCareSettings> {
        return SafeApi.body { 
            Log.d(TAG, "Updating pet care settings for pet: $petId with settings: $settings")
            petCareApi.updatePetCareSettings(petId, settings) 
        }
    }

    override suspend fun updateCareRecord(
        petId: String,
        logId: String,
        update: CareRecordUpdateRequest
    ): AppResult<CareRecordResponse> {
        return SafeApi.body {
            Log.d(TAG, "Updating care record: pet=$petId log=$logId")
            petCareApi.updateCareRecord(petId, logId, update)
        }
    }

    override suspend fun deleteCareRecord(petId: String, logId: String): AppResult<Unit> {
        return SafeApi.responseUnit {
            Log.d(TAG, "Deleting care record: pet=$petId log=$logId")
            petCareApi.deleteCareRecord(petId, logId)
        }
    }

    override suspend fun getWeightMonthlyAnalysis(
        petId: String
    ): AppResult<com.example.pet_project_frontend.data.remote.dto.response.WeightMonthlyAnalysisResponse> {
        return SafeApi.body {
            Log.d(TAG, "Getting weight monthly analysis for pet: $petId")
            val response = petCareApi.getWeightMonthlyAnalysis(petId)
            Log.d(TAG, "Weight monthly analysis fetched successfully")
            response
        }
    }
}