package com.example.pet_project_frontend.domain.usecase.pet

import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.response.BiometricAnalysisResponse
import com.example.pet_project_frontend.domain.repository.PetRepository
import javax.inject.Inject

class RegisterNosePrintUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(
        petId: String,
        filePath: String
    ): AppResult<BiometricAnalysisResponse> {
        if (petId.isBlank()) {
            return AppResult.Error(code = 400, message = "반려견 정보가 필요합니다.")
        }
        if (filePath.isBlank()) {
            return AppResult.Error(code = 400, message = "비문 이미지가 선택되지 않았습니다.")
        }
        return petRepository.registerNosePrint(
            petId = petId.trim(),
            filePath = filePath.trim()
        )
    }
}
