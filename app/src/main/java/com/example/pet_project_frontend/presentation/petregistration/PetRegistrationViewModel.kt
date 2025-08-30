package com.example.pet_project_frontend.presentation.petregistration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.data.remote.result.NetworkResult
import com.example.pet_project_frontend.domain.model.Pet
import com.example.pet_project_frontend.domain.usecase.pet.RegisterPetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetRegistrationViewModel @Inject constructor(
    private val registerPetUseCase: RegisterPetUseCase
) : ViewModel() {

    // UI 상태 (사용자 입력 값)
    val petName = MutableStateFlow("")
    val gender = MutableStateFlow("MALE")
    val breed = MutableStateFlow("")
    val birthdate = MutableStateFlow("")
    val currentWeight = MutableStateFlow("")
    val furColor = MutableStateFlow("")
    val healthConcerns = MutableStateFlow("")

    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess = _registrationSuccess.asStateFlow()

    fun onRegisterClicked() {
        viewModelScope.launch {
            val weight = currentWeight.value.toFloatOrNull() ?: 0f
            val healthConcernsList = if (healthConcerns.value.isNotEmpty()) {
                listOf(healthConcerns.value)
            } else {
                emptyList()
            }
            
            when (val result = registerPetUseCase(
                name = petName.value,
                gender = gender.value,
                breed = breed.value,
                birthdate = birthdate.value,
                currentWeight = weight,
                furColor = if (furColor.value.isNotEmpty()) furColor.value else null,
                healthConcerns = healthConcernsList
            )) {
                is NetworkResult.Success -> {
                    _registrationSuccess.value = true
                }
                is NetworkResult.Error -> {
                    // 에러 처리: 사용자에게 에러 메시지 표시
                    // TODO: 에러 상태를 UI에 반영
                }
                is NetworkResult.Exception -> {
                    // 예외 처리: 네트워크 오류 등
                    // TODO: 예외 상태를 UI에 반영
                }
            }
        }
    }
}