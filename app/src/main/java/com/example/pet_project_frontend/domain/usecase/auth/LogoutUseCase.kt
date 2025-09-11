package com.example.pet_project_frontend.domain.usecase.auth

import com.example.pet_project_frontend.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(accessToken: String, refreshToken: String) =
        authRepository.logout(accessToken, refreshToken)
}