// app/src/main/java/com/example/pet_project_frontend/data/repository/PetCareRepositoryImpl.kt

package com.example.pet_project_frontend.data.repository

import android.util.Log
import com.example.pet_project_frontend.data.local.preferences.TokenManager
import com.example.pet_project_frontend.data.remote.api.PetCareApi
import com.example.pet_project_frontend.data.remote.dto.request.CareRecordCreateRequest
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordResponse
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordsResponse
// no NetworkResult here; PetCareRepository uses kotlin.Result
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
    ): kotlin.Result<CareRecordsResponse> {
        return try {
            Log.d(TAG, "Getting care records for pet: $petId")
            
            val accessToken = tokenManager.getAccessToken()
            if (accessToken == null) {
                Log.e(TAG, "Access token is null")
                return Result.failure(IllegalStateException("Access token is required"))
            }
            
            val response = petCareApi.getCareRecords(
                accessToken = "Bearer $accessToken",
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
            kotlin.Result.success(response)
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting care records", e)
            kotlin.Result.failure(e)
        }
    }

    override suspend fun createCareRecord(
        petId: String,
        recordType: String,
        timestamp: Long,
        data: Any,
        notes: String?
    ): kotlin.Result<CareRecordResponse> {
        return try {
            Log.d(TAG, "Creating care record for pet: $petId, type: $recordType")
            
            val accessToken = tokenManager.getAccessToken()
            if (accessToken == null) {
                Log.e(TAG, "Access token is null")
                return kotlin.Result.failure(IllegalStateException("Access token is required"))
            }
            
            val request = CareRecordCreateRequest(
                recordType = recordType,
                timestamp = timestamp,
                data = data,
                notes = notes
            )
            
            val response = petCareApi.createCareRecord(
                accessToken = "Bearer $accessToken",
                recordRequest = request
            )
            
            Log.d(TAG, "Care record created successfully: ${response.logId}")
            kotlin.Result.success(response)
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception while creating care record", e)
            kotlin.Result.failure(e)
        }
    }

    override suspend fun getRecordsByType(
        petId: String,
        recordType: String,
        date: String?,
        startDate: String?,
        endDate: String?,
        limit: Int,
        cursor: String?
    ): kotlin.Result<CareRecordsResponse> {
        return try {
            Log.d(TAG, "Getting care records by type for pet: $petId, type: $recordType")
            
            val accessToken = tokenManager.getAccessToken()
            if (accessToken == null) {
                Log.e(TAG, "Access token is null")
                return kotlin.Result.failure(IllegalStateException("Access token is required"))
            }
            
            val response = petCareApi.getRecordsByType(
                accessToken = "Bearer $accessToken",
                recordType = recordType,
                petId = petId,
                date = date,
                startDate = startDate,
                endDate = endDate,
                limit = limit,
                cursor = cursor
            )
            
            Log.d(TAG, "Care records by type fetched successfully: ${response.records.size} records")
            kotlin.Result.success(response)
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting care records by type", e)
            kotlin.Result.failure(e)
        }
    }
}