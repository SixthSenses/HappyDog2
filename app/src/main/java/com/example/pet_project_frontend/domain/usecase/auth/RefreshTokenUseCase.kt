package com.example.pet_project_frontend.domain.usecase.auth

import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.response.TokenRefreshResponse
import com.example.pet_project_frontend.domain.repository.AuthRepository
import javax.inject.Inject

class RefreshTokenUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(refreshToken: String): AppResult<TokenRefreshResponse> =
        authRepository.refreshToken(refreshToken)
}
