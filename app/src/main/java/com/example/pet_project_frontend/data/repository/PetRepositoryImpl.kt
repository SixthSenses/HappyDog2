// app/src/main/java/com/example/pet_project_frontend/data/repository/PetRepositoryImpl.kt

package com.example.pet_project_frontend.data.repository

import android.util.Log
import com.example.pet_project_frontend.data.mapper.PetMapper
import com.example.pet_project_frontend.data.remote.api.PetApi
import com.example.pet_project_frontend.data.remote.dto.request.*
import com.example.pet_project_frontend.data.remote.dto.response.*
import com.example.pet_project_frontend.data.remote.result.NetworkResult
import com.example.pet_project_frontend.data.remote.util.SafeApiCall
import com.example.pet_project_frontend.domain.model.Pet
import com.example.pet_project_frontend.domain.repository.PetRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepositoryImpl @Inject constructor(
    private val petApi: PetApi
) : PetRepository {

    companion object {
        private const val TAG = "PetRepositoryImpl"
    }

    override suspend fun registerPet(request: PetRegistrationRequest): NetworkResult<Pet> {
        Log.d(TAG, "Registering pet: ${request.name}")
        val result: NetworkResult<PetProfileResponse> = SafeApiCall.call { petApi.registerPet(request) }
        return when (result) {
            is NetworkResult.Success -> {
                Log.d(TAG, "Pet registered successfully: ${result.data.petId}")
                NetworkResult.Success(PetMapper.mapToDomainModel(result.data))
            }
            is NetworkResult.Error -> {
                Log.e(TAG, "Pet registration failed. Code: ${result.code}, Error: ${result.message}")
                result
            }
            is NetworkResult.Exception -> {
                Log.e(TAG, "Exception during pet registration", result.throwable)
                result
            }
        }
    }

    override suspend fun getPetProfile(petId: String): NetworkResult<Pet> {
        Log.d(TAG, "Getting pet profile: $petId")
        val result: NetworkResult<PetProfileResponse> = SafeApiCall.call { petApi.getPetProfile(petId) }
        return when (result) {
            is NetworkResult.Success -> {
                Log.d(TAG, "Pet profile fetched successfully: ${result.data.petId}")
                NetworkResult.Success(PetMapper.mapToDomainModel(result.data))
            }
            is NetworkResult.Error -> {
                Log.e(TAG, "Failed to get pet profile. Code: ${result.code}, Error: ${result.message}")
                result
            }
            is NetworkResult.Exception -> {
                Log.e(TAG, "Exception while getting pet profile", result.throwable)
                result
            }
        }
    }

    override suspend fun updatePetProfile(petId: String, request: PetUpdateRequest): NetworkResult<Pet> {
        Log.d(TAG, "Updating pet profile: $petId")
        val result: NetworkResult<PetProfileResponse> = SafeApiCall.call { petApi.updatePetProfile(petId, request) }
        return when (result) {
            is NetworkResult.Success -> {
                Log.d(TAG, "Pet profile updated successfully: ${result.data.petId}")
                NetworkResult.Success(PetMapper.mapToDomainModel(result.data))
            }
            is NetworkResult.Error -> {
                Log.e(TAG, "Failed to update pet profile. Code: ${result.code}, Error: ${result.message}")
                result
            }
            is NetworkResult.Exception -> {
                Log.e(TAG, "Exception while updating pet profile", result.throwable)
                result
            }
        }
    }

    override suspend fun registerNosePrint(petId: String, filePath: String): NetworkResult<BiometricAnalysisResponse> {
    Log.d(TAG, "Registering nose print for pet: $petId")
    val request = BiometricAnalysisRequest(filePath = filePath)
    return SafeApiCall.call { petApi.registerNosePrint(petId, request) }
    }

    override suspend fun analyzeEye(petId: String, filePath: String): NetworkResult<EyeAnalysisResponse> {
    Log.d(TAG, "Analyzing eye for pet: $petId")
    val request = BiometricAnalysisRequest(filePath = filePath)
    return SafeApiCall.call { petApi.analyzeEye(petId, request) }
    }
}