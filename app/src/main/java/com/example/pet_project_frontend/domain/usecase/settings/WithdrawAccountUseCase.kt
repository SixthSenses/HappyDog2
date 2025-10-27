package com.example.pet_project_frontend.domain.usecase.settings

import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.domain.repository.AuthRepository
import javax.inject.Inject

class WithdrawAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AppResult<Unit> {
        return authRepository.withdraw()
    }
}