package com.example.pet_project_frontend.data.repository

import com.example.pet_project_frontend.data.local.database.dao.PetDao
import com.example.pet_project_frontend.data.local.database.entities.PetEntity
import com.example.pet_project_frontend.data.mapper.PetMapper
import com.example.pet_project_frontend.data.remote.api.PetApi
import com.example.pet_project_frontend.data.remote.dto.request.*
import com.example.pet_project_frontend.data.remote.dto.response.*
import com.example.pet_project_frontend.data.remote.result.NetworkResult
import com.example.pet_project_frontend.domain.model.Pet
import com.example.pet_project_frontend.domain.repository.PetRepository
import java.time.LocalDate
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor(
    private val petApi: PetApi,
    private val petDao: PetDao
) : PetRepository {

    override suspend fun registerPet(request: PetRegistrationRequest): NetworkResult<Pet> {
        return try {
            val response = petApi.registerPet(request)
            if (response.isSuccessful) {
                response.body()?.let { petProfile ->
                    // 로컬 DB에 저장
                    petDao.insertPet(petProfile.toPetEntity())
                    // 매퍼를 사용하여 DTO를 도메인 모델로 변환하여 반환
                    NetworkResult.Success(PetMapper.mapToDomainModel(petProfile))
                } ?: NetworkResult.Error(response.code(), "Empty response body")
            } else {
                NetworkResult.Error(response.code(), "Registration failed: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    override suspend fun getPetProfile(petId: String): NetworkResult<Pet> {
        return try {
            // 먼저 로컬 DB에서 확인
            val localPet = petDao.getPetById(petId)
            if (localPet != null) {
                return NetworkResult.Success(localPet.toDomainModel())
            }

            // 로컬에 없으면 API 호출
            val response = petApi.getPetProfile(petId)
            if (response.isSuccessful) {
                response.body()?.let { petProfile ->
                    // 로컬 DB에 저장
                    petDao.insertPet(petProfile.toPetEntity())
                    // 매퍼를 사용하여 DTO를 도메인 모델로 변환하여 반환
                    NetworkResult.Success(PetMapper.mapToDomainModel(petProfile))
                } ?: NetworkResult.Error(response.code(), "Empty response body")
            } else {
                NetworkResult.Error(response.code(), "Failed to get pet profile: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    override suspend fun updatePetProfile(petId: String, request: PetUpdateRequest): NetworkResult<Pet> {
        return try {
            val response = petApi.updatePetProfile(petId, request)
            if (response.isSuccessful) {
                response.body()?.let { petProfile ->
                    // 로컬 DB 업데이트
                    petDao.updatePet(petProfile.toPetEntity())
                    // 매퍼를 사용하여 DTO를 도메인 모델로 변환하여 반환
                    NetworkResult.Success(PetMapper.mapToDomainModel(petProfile))
                } ?: NetworkResult.Error(response.code(), "Update failed: ${response.code()}")
            } else {
                NetworkResult.Error(response.code(), "Update failed: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    override suspend fun registerNosePrint(petId: String, filePath: String): NetworkResult<BiometricAnalysisResponse> {
        return try {
            val request = BiometricAnalysisRequest(filePath = filePath)
            val response = petApi.registerNosePrint(petId, request)
            if (response.isSuccessful) {
                response.body()?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error(response.code(), "Empty response body")
            } else {
                NetworkResult.Error(response.code(), "Nose print registration failed: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    override suspend fun analyzeEye(petId: String, filePath: String): NetworkResult<EyeAnalysisResponse> {
        return try {
            val request = BiometricAnalysisRequest(filePath = filePath)
            val response = petApi.analyzeEye(petId, request)
            if (response.isSuccessful) {
                response.body()?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error(response.code(), "Empty response body")
            } else {
                NetworkResult.Error(response.code(), "Eye analysis failed: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
}

// [수정됨] Extension functions for data conversion
private fun PetProfileResponse.toPetEntity(): PetEntity {
    return PetEntity(
        petId = petId, // 'id' -> 'petId'
        userId = userId, // 'ownerId' -> 'userId'
        name = name,
        breed = breed,
        birthDate = LocalDate.parse(birthdate), // String을 LocalDate로 타입 변환
        gender = gender,
        currentWeight = currentWeight.toFloat(), // 'weight' -> 'currentWeight', Double을 Float으로 타입 변환
        // createdAt, updatedAt은 Entity에서 기본값(now())으로 생성되므로 여기서 제외
    )
}

private fun PetEntity.toDomainModel(): Pet {
    return Pet(
        id = petId, // 'id' -> 'petId'
        name = name,
        breed = breed,
        birthDate = birthDate, // 이미 LocalDate 타입이므로 파싱 불필요
        gender = when (gender.lowercase()) {
            "male", "수컷" -> com.example.pet_project_frontend.domain.model.Gender.MALE
            "female", "암컷" -> com.example.pet_project_frontend.domain.model.Gender.FEMALE
            else -> com.example.pet_project_frontend.domain.model.Gender.UNKNOWN
        },
        weight = currentWeight, // 'weight' -> 'currentWeight'
        ownerId = userId, // 'ownerId' -> 'userId'
        isVerified = isVerified, // Entity의 값 사용
        nosePrintUrl = nosePrintUrl, // Entity의 값 사용
        healthConcerns = healthConcerns // Entity의 값 사용
    )
}