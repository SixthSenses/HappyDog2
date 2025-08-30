package com.example.pet_project_frontend.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.data.remote.dto.response.SocialLoginResponse
import com.example.pet_project_frontend.domain.usecase.auth.SocialLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val socialLoginUseCase: SocialLoginUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun socialLogin(authCode: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            when (val result = socialLoginUseCase(authCode)) {
                is com.example.pet_project_frontend.data.remote.result.NetworkResult.Success -> {
                    _authState.value = AuthState.Success(result.data)
                }
                is com.example.pet_project_frontend.data.remote.result.NetworkResult.Error -> {
                    _authState.value = AuthState.Error(result.message ?: "로그인에 실패했습니다.")
                }
                is com.example.pet_project_frontend.data.remote.result.NetworkResult.Exception -> {
                    _authState.value = AuthState.Error(result.throwable.message ?: "로그인에 실패했습니다.")
                }
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
