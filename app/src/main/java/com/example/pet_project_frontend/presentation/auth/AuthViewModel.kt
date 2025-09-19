// app/src/main/java/com/example/pet_project_frontend/presentation/auth/AuthViewModel.kt

package com.example.pet_project_frontend.presentation.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.data.remote.dto.response.SocialLoginResponse
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.mapper.UserMapper
import com.example.pet_project_frontend.domain.repository.AuthRepository
import com.example.pet_project_frontend.domain.repository.UserRepository
import com.example.pet_project_frontend.domain.usecase.auth.SocialLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val socialLoginUseCase: SocialLoginUseCase,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun socialLogin(authCode: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                Log.d("AuthViewModel", "Starting social login with auth code")

                when (val result = socialLoginUseCase(authCode)) {
                    is AppResult.Success -> {
                        val response = result.data
                        Log.d("AuthViewModel", "Login successful, saving tokens")

                        // 토큰 저장
                        authRepository.saveTokens(
                            accessToken = response.accessToken,
                            refreshToken = response.refreshToken
                        )

                        // 사용자 정보 저장 (도메인 중심)
                        val domainUser = UserMapper.fromAuthUserInfo(response.userInfo)
                        authRepository.saveUser(domainUser)

                        // UserRepository에도 액세스 토큰 저장 (중복이지만 기존 코드 호환성을 위해)
                        userRepository.saveAccessToken(response.accessToken)

                        Log.d("AuthViewModel", "Tokens and user info saved successfully")
                        Log.d("AuthViewModel", "Is new user: ${response.isNewUser}")

                        _authState.value = AuthState.Success(response)
                    }
                    is AppResult.Error -> {
                        val errorMessage = "로그인 실패: ${result.message}"
                        Log.e("AuthViewModel", "$errorMessage (Code: ${result.code})")
                        _authState.value = AuthState.Error(errorMessage)
                    }
                    is AppResult.Exception -> {
                        val errorMessage = result.throwable.message ?: "알 수 없는 오류가 발생했습니다."
                        Log.e("AuthViewModel", "Login exception", result.throwable)
                        _authState.value = AuthState.Error(errorMessage)
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Unexpected error during login", e)
                _authState.value = AuthState.Error("로그인 중 오류가 발생했습니다: ${e.message}")
            }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val accessToken = authRepository.getAccessToken()
                val refreshToken = authRepository.getRefreshToken()

                if (accessToken != null && refreshToken != null) {
                    when (val result = authRepository.logout(accessToken, refreshToken)) {
                        is AppResult.Success -> {
                            Log.d("AuthViewModel", "Logout successful")
                        }
                        is AppResult.Error -> {
                            Log.e("AuthViewModel", "Logout failed: ${result.message}")
                        }
                        is AppResult.Exception -> {
                            Log.e("AuthViewModel", "Logout exception", result.throwable)
                        }
                    }
                }

                // 로컬 토큰은 항상 삭제
                authRepository.clearTokens()
                authRepository.clearUserInfo()

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error during logout", e)
            }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val response: SocialLoginResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}