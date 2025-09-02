package com.example.pet_project_frontend.domain.usecase

import com.example.pet_project_frontend.data.remote.result.NetworkResult
import com.example.pet_project_frontend.domain.model.Pet
import com.example.pet_project_frontend.domain.model.User
import com.example.pet_project_frontend.domain.repository.PetRepository
import com.example.pet_project_frontend.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Pets + Owner 조합 응답을 UI 용으로 가공하는 유즈케이스 예시.
 * - 우선 간단히 순차 호출하고, 실패 시 첫 에러를 전파합니다.
 */
class GetPetProfileWithOwnerUseCase @Inject constructor(
    private val petRepository: PetRepository,
    private val userRepository: UserRepository
) {
    data class PetProfileWithUser(
        val pet: Pet,
        val owner: User
    )

    fun execute(petId: String): Flow<NetworkResult<PetProfileWithUser>> = flow {
        // 1) 반려동물 프로필 조회
        when (val petRes = petRepository.getPetProfile(petId)) {
            is NetworkResult.Success -> {
                // 2) 소유자 프로필 조회 (간단화: 내 프로필로 대체)
                when (val userRes = userRepository.getUserInfo()) {
                    is NetworkResult.Success -> emit(
                        NetworkResult.Success(
                            PetProfileWithUser(
                                pet = petRes.data,
                                owner = userRes.data
                            )
                        )
                    )
                    is NetworkResult.Error -> emit(NetworkResult.Error(userRes.code, userRes.message, userRes.error))
                    is NetworkResult.Exception -> emit(NetworkResult.Exception(userRes.throwable))
                }
            }
            is NetworkResult.Error -> emit(NetworkResult.Error(petRes.code, petRes.message, petRes.error))
            is NetworkResult.Exception -> emit(NetworkResult.Exception(petRes.throwable))
        }
    }
}
