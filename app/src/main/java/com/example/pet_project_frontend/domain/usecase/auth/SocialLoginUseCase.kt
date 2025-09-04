package com.example.pet_project_frontend.domain.usecase.auth

import com.example.pet_project_frontend.data.remote.dto.response.SocialLoginResponse
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.domain.repository.AuthRepository
import javax.inject.Inject

class SocialLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(authCode: String): AppResult<SocialLoginResponse> {
        return authRepository.socialLogin(authCode)
    }
}