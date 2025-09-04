package com.example.pet_project_frontend.domain.usecase

import com.example.pet_project_frontend.core.common.AppResult
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

    fun execute(petId: String): Flow<AppResult<PetProfileWithUser>> = flow {
        // 1) 반려동물 프로필 조회
        when (val petRes: AppResult<Pet> = petRepository.getPetProfile(petId)) {
            is AppResult.Success<Pet> -> {
                // 2) 소유자 프로필 조회 (간단화: 내 프로필로 대체)
                when (val userRes: AppResult<User> = userRepository.getUserInfo()) {
                    is AppResult.Success<User> -> emit(
                        AppResult.Success(
                            PetProfileWithUser(
                                pet = petRes.data,
                                owner = userRes.data
                            )
                        )
                    )
                    is AppResult.Error -> emit(
                        AppResult.Error(
                            code = userRes.code,
                            message = userRes.message,
                            validation = userRes.validation,
                            cause = userRes.cause
                        )
                    )
                    is AppResult.Exception -> emit(AppResult.Exception(userRes.throwable))
                }
            }
            is AppResult.Error -> emit(
                AppResult.Error(
                    code = petRes.code,
                    message = petRes.message,
                    validation = petRes.validation,
                    cause = petRes.cause
                )
            )
            is AppResult.Exception -> emit(AppResult.Exception(petRes.throwable))
        }
    }
}
