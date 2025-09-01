// app/src/main/java/com/example/pet_project_frontend/data/repository/PetRepositoryImpl.kt

package com.example.pet_project_frontend.data.repository

import android.util.Log
import com.example.pet_project_frontend.data.mapper.PetMapper
import com.example.pet_project_frontend.data.remote.api.PetApi
import com.example.pet_project_frontend.data.remote.dto.request.*
import com.example.pet_project_frontend.data.remote.dto.response.*
import com.example.pet_project_frontend.data.remote.result.NetworkResult
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
        return try {
            Log.d(TAG, "Registering pet: ${request.name}")
            val response = petApi.registerPet(request)
            
            if (response.isSuccessful) {
                response.body()?.let { petProfile ->
                    Log.d(TAG, "Pet registered successfully: ${petProfile.petId}")
                    // 매퍼를 사용하여 DTO를 도메인 모델로 변환
                    NetworkResult.Success(PetMapper.mapToDomainModel(petProfile))
                } ?: run {
                    Log.e(TAG, "Pet registration response body is null")
                    NetworkResult.Error(response.code(), "Empty response body")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Pet registration failed. Code: ${response.code()}, Error: $errorBody")
                NetworkResult.Error(response.code(), errorBody ?: "Registration failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during pet registration", e)
            NetworkResult.Exception(e)
        }
    }

    override suspend fun getPetProfile(petId: String): NetworkResult<Pet> {
        return try {
            Log.d(TAG, "Getting pet profile: $petId")
            val response = petApi.getPetProfile(petId)
            
            if (response.isSuccessful) {
                response.body()?.let { petProfile ->
                    Log.d(TAG, "Pet profile fetched successfully: ${petProfile.petId}")
                    // 매퍼를 사용하여 DTO를 도메인 모델로 변환
                    NetworkResult.Success(PetMapper.mapToDomainModel(petProfile))
                } ?: run {
                    Log.e(TAG, "Pet profile response body is null")
                    NetworkResult.Error(response.code(), "Empty response body")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to get pet profile. Code: ${response.code()}, Error: $errorBody")
                NetworkResult.Error(response.code(), errorBody ?: "Failed to get pet profile")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting pet profile", e)
            NetworkResult.Exception(e)
        }
    }

    override suspend fun updatePetProfile(petId: String, request: PetUpdateRequest): NetworkResult<Pet> {
        return try {
            Log.d(TAG, "Updating pet profile: $petId")
            val response = petApi.updatePetProfile(petId, request)
            
            if (response.isSuccessful) {
                response.body()?.let { petProfile ->
                    Log.d(TAG, "Pet profile updated successfully: ${petProfile.petId}")
                    // 매퍼를 사용하여 DTO를 도메인 모델로 변환
                    NetworkResult.Success(PetMapper.mapToDomainModel(petProfile))
                } ?: run {
                    Log.e(TAG, "Pet profile update response body is null")
                    NetworkResult.Error(response.code(), "Empty response body")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to update pet profile. Code: ${response.code()}, Error: $errorBody")
                NetworkResult.Error(response.code(), errorBody ?: "Update failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while updating pet profile", e)
            NetworkResult.Exception(e)
        }
    }

    override suspend fun registerNosePrint(petId: String, filePath: String): NetworkResult<BiometricAnalysisResponse> {
        return try {
            Log.d(TAG, "Registering nose print for pet: $petId")
            val request = BiometricAnalysisRequest(filePath = filePath)
            val response = petApi.registerNosePrint(petId, request)
            
            if (response.isSuccessful) {
                response.body()?.let { biometricResponse ->
                    Log.d(TAG, "Nose print registration completed: ${biometricResponse.status}")
                    NetworkResult.Success(biometricResponse)
                } ?: run {
                    Log.e(TAG, "Nose print registration response body is null")
                    NetworkResult.Error(response.code(), "Empty response body")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Nose print registration failed. Code: ${response.code()}, Error: $errorBody")
                NetworkResult.Error(response.code(), errorBody ?: "Nose print registration failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during nose print registration", e)
            NetworkResult.Exception(e)
        }
    }

    override suspend fun analyzeEye(petId: String, filePath: String): NetworkResult<EyeAnalysisResponse> {
        return try {
            Log.d(TAG, "Analyzing eye for pet: $petId")
            val request = BiometricAnalysisRequest(filePath = filePath)
            val response = petApi.analyzeEye(petId, request)
            
            if (response.isSuccessful) {
                response.body()?.let { eyeAnalysisResponse ->
                    Log.d(TAG, "Eye analysis completed: ${eyeAnalysisResponse.analysisId}")
                    NetworkResult.Success(eyeAnalysisResponse)
                } ?: run {
                    Log.e(TAG, "Eye analysis response body is null")
                    NetworkResult.Error(response.code(), "Empty response body")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Eye analysis failed. Code: ${response.code()}, Error: $errorBody")
                NetworkResult.Error(response.code(), errorBody ?: "Eye analysis failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during eye analysis", e)
            NetworkResult.Exception(e)
        }
    }
}