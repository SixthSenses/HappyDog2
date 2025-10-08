// app/src/main/java/com/example/pet_project_frontend/data/repository/PetRepositoryImpl.kt

package com.example.pet_project_frontend.data.repository

import android.util.Log
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.core.common.SafeApi
import com.example.pet_project_frontend.data.mapper.PetMapper
import com.example.pet_project_frontend.data.remote.api.PetApi
import com.example.pet_project_frontend.data.remote.dto.request.*
import com.example.pet_project_frontend.data.remote.dto.response.*
import com.example.pet_project_frontend.domain.model.Pet
import com.example.pet_project_frontend.domain.model.PetStatus
import com.example.pet_project_frontend.domain.repository.PetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepositoryImpl @Inject constructor(
    private val petApi: PetApi
) : PetRepository {

    companion object {
        private const val TAG = "PetRepositoryImpl"
    }

    // StateFlow로 반려동물 상태 관리 (Loading/HasPet/NoPet)
    private val _petStatus = MutableStateFlow<PetStatus>(PetStatus.Loading)
    
    /**
     * 반려동물 상태를 외부에 노출
     */
    override fun getPetStatus(): StateFlow<PetStatus> = _petStatus.asStateFlow()
    
    /**
     * 서버에서 반려동물 상태를 확인하고 StateFlow 업데이트
     */
    private suspend fun refreshPetStatus() {
        Log.d(TAG, "Refreshing pet status from server")
        _petStatus.value = PetStatus.Loading
        
        when (val res = SafeApi.response { petApi.getMyPetProfile() }) {
            is AppResult.Success -> {
                Log.d(TAG, "Pet found, setting status = HasPet")
                _petStatus.value = PetStatus.HasPet
            }
            is AppResult.Error -> {
                // 404는 펫이 없는 정상 상태
                if (res.code == 404) {
                    Log.d(TAG, "Pet not found (404), setting status = NoPet")
                    _petStatus.value = PetStatus.NoPet
                } else {
                    Log.e(TAG, "Error checking pet status (${res.code}): ${res.message}")
                    // 다른 에러는 일단 NoPet으로 처리 (로그인 화면으로 가지 않도록)
                    _petStatus.value = PetStatus.NoPet
                }
            }
            is AppResult.Exception -> {
                Log.e(TAG, "Exception checking pet status", res.throwable)
                // 예외 발생 시에도 NoPet으로 처리 (앱이 멈추지 않도록)
                _petStatus.value = PetStatus.NoPet
            }
        }
    }
    
    /**
     * 명시적으로 펫 상태를 새로고침 (로그인 후, 등록 후 등)
     * MainViewModel에서 호출
     */
    override suspend fun refreshPetStatusManually() {
        refreshPetStatus()
    }

    override suspend fun registerPet(request: PetRegistrationRequest): AppResult<Pet> {
        Log.d(TAG, "Registering pet: ${request.name}")
        return SafeApi.response { petApi.registerPet(request) }
            .let { res ->
                when (res) {
                    is AppResult.Success -> {
                        Log.d(TAG, "Pet registered successfully: ${res.data.petId}")
                        // 🔥 핵심: 반려동물 등록 성공 시 즉시 상태 업데이트
                        _petStatus.value = PetStatus.HasPet
                        Log.d(TAG, "Updated pet status to HasPet after registration")
                        AppResult.Success(PetMapper.mapToDomainModel(res.data))
                    }
                    is AppResult.Error -> res
                    is AppResult.Exception -> res
                }
            }
    }

    override suspend fun getPetProfile(petId: String): AppResult<Pet> {
        Log.d(TAG, "Getting pet profile: $petId")
        return SafeApi.response { petApi.getPetProfile(petId) }
            .let { res ->
                when (res) {
                    is AppResult.Success -> {
                        Log.d(TAG, "Pet profile fetched successfully: ${res.data.petId}")
                        AppResult.Success(PetMapper.mapToDomainModel(res.data))
                    }
                    is AppResult.Error -> res
                    is AppResult.Exception -> res
                }
            }
    }

    override suspend fun getMyPetProfile(): AppResult<Pet> {
        Log.d(TAG, "Getting my pet profile (single-pet policy)")
        // OpenAPI: /api/pets/profile returns PetViewBasedResponse (요약 뷰)
        // 도메인 Pet에는 상세 필드가 필요하므로 petId로 상세 조회를 한 번 더 수행합니다.
        return when (val viewRes = SafeApi.response { petApi.getMyPetProfile() }) {
            is AppResult.Success -> {
                val petId = viewRes.data.petId
                when (val profRes = SafeApi.response { petApi.getPetProfile(petId) }) {
                    is AppResult.Success -> {
                        Log.d(TAG, "My pet profile fetched successfully: ${profRes.data.petId}")
                        AppResult.Success(PetMapper.mapToDomainModel(profRes.data))
                    }
                    is AppResult.Error -> profRes
                    is AppResult.Exception -> profRes
                }
            }
            is AppResult.Error -> viewRes
            is AppResult.Exception -> viewRes
        }
    }

    override suspend fun getPetProfileForPetCare(): AppResult<PetViewBasedResponse> {
        Log.d(TAG, "Getting pet profile for petcare view")
        return SafeApi.response { petApi.getPetProfileForPetCare() }
            .let { res ->
                when (res) {
                    is AppResult.Success -> {
                        Log.d(TAG, "PetCare profile fetched successfully: ${res.data.petId}")
                        res
                    }
                    is AppResult.Error -> res
                    is AppResult.Exception -> res
                }
            }
    }

    override suspend fun updatePetProfile(petId: String, request: PetUpdateRequest): AppResult<Pet> {
        Log.d(TAG, "Updating pet profile: $petId")
        return SafeApi.response { petApi.updatePetProfile(petId, request) }
            .let { res ->
                when (res) {
                    is AppResult.Success -> {
                        Log.d(TAG, "Pet profile updated successfully: ${res.data.petId}")
                        AppResult.Success(PetMapper.mapToDomainModel(res.data))
                    }
                    is AppResult.Error -> res
                    is AppResult.Exception -> res
                }
            }
    }

    override suspend fun registerNosePrint(petId: String, filePath: String): AppResult<BiometricAnalysisResponse> {
        Log.d(TAG, "Registering nose print for pet: $petId")
        val request = BiometricAnalysisRequest(filePath = filePath)
        return SafeApi.response { petApi.registerNosePrint(petId, request) }
    }

    override suspend fun analyzeEye(petId: String, filePath: String): AppResult<EyeAnalysisResponse> {
        Log.d(TAG, "Analyzing eye for pet: $petId")
        val request = BiometricAnalysisRequest(filePath = filePath)
        return SafeApi.response { petApi.analyzeEye(petId, request) }
    }

    /**
     * @Deprecated 하위 호환성을 위해 유지. getPetStatus() 사용 권장
     */
    @Deprecated(
        message = "Use getPetStatus() instead",
        replaceWith = ReplaceWith("getPetStatus().map { it.hasPet }")
    )
    override fun hasPet(): kotlinx.coroutines.flow.Flow<Boolean> =
        _petStatus.map { status ->
            status.hasPet
        }
}
