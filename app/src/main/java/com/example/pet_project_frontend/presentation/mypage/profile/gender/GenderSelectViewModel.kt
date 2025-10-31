package com.example.pet_project_frontend.presentation.mypage.profile.gender

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.request.UpdatePetRequest
import com.example.pet_project_frontend.domain.model.Gender
import com.example.pet_project_frontend.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class GenderUi { MALE, FEMALE }

data class GenderUiState(
    val selected: GenderUi? = null,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GenderSelectViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val petRepository: PetRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(GenderUiState())
    val uiState = _ui.asStateFlow()

    private var petId: String? = savedStateHandle
        .get<String>("petId")
        ?.takeUnless { it.isNullOrBlank() || it == "null" }

    init {
        val initialRaw = savedStateHandle.get<String>("initialGender")
            ?.lowercase(Locale.getDefault())
        val initial = when (initialRaw) {
            "male", "수컷" -> GenderUi.MALE
            "female", "암컷" -> GenderUi.FEMALE
            else -> null
        }
        _ui.value = _ui.value.copy(selected = initial)
    }

    fun initializePetId(id: String?) {
        if (!id.isNullOrBlank() && id != "null" && petId == null) {
            petId = id
            savedStateHandle["petId"] = id
        }
    }

    fun onSelect(gender: GenderUi) {
        _ui.value = _ui.value.copy(selected = gender, error = null)
    }

    fun onSave(onSuccess: (GenderUi, Boolean) -> Unit) = viewModelScope.launch {
        val selected = _ui.value.selected ?: run {
            _ui.value = _ui.value.copy(error = "성별을 선택해 주세요.")
            return@launch
        }

        if (_ui.value.isSaving) return@launch
        _ui.value = _ui.value.copy(isSaving = true, error = null)

        val requestGender = when (selected) {
            GenderUi.MALE -> "MALE"
            GenderUi.FEMALE -> "FEMALE"
        }

        val targetPetId = petId ?: run {
            val profile = when (val result = petRepository.getMyPetProfile()) {
                is AppResult.Success -> result.data
                is AppResult.Error -> {
                    val message = result.message ?: result.validation?.generalMessage ?: GENERIC_SAVE_ERROR_MESSAGE
                    _ui.value = _ui.value.copy(isSaving = false, error = message)
                    return@launch
                }
                is AppResult.Exception -> {
                    val message = result.throwable.message ?: GENERIC_SAVE_ERROR_MESSAGE
                    _ui.value = _ui.value.copy(isSaving = false, error = message)
                    return@launch
                }
            }

            petId = profile.id
            savedStateHandle["petId"] = profile.id
            profile.id
        }

        val request = UpdatePetRequest(
            gender = requestGender
        )

        when (val update = petRepository.updatePetProfile(targetPetId, request)) {
            is AppResult.Success -> {
                val updatedUi = when (update.data.gender) {
                    Gender.MALE -> GenderUi.MALE
                    Gender.FEMALE -> GenderUi.FEMALE
                    Gender.UNKNOWN -> selected
                }
                _ui.value = _ui.value.copy(isSaving = false, selected = updatedUi)
                onSuccess(updatedUi, true)
            }
            is AppResult.Error -> {
                val message = update.message ?: update.validation?.generalMessage ?: GENERIC_SAVE_ERROR_MESSAGE
                _ui.value = _ui.value.copy(isSaving = false, error = message)
            }
            is AppResult.Exception -> {
                val message = update.throwable.message ?: GENERIC_SAVE_ERROR_MESSAGE
                _ui.value = _ui.value.copy(isSaving = false, error = message)
            }
        }
    }

    companion object {
        private const val GENERIC_SAVE_ERROR_MESSAGE = "성별을 저장하는 중 문제가 발생했어요. 잠시 후 다시 시도해주세요."
    }
}
