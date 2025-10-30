package com.example.pet_project_frontend.presentation.mypage.profile.birthdate

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.request.UpdatePetRequest
import com.example.pet_project_frontend.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BirthEditUiState(
    val textFieldValue: TextFieldValue = TextFieldValue(""),
    val error: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class BirthEditViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val petRepository: PetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BirthEditUiState())
    val uiState = _uiState.asStateFlow()

    private var petId: String? = savedStateHandle
        .get<String>("petId")
        ?.takeUnless { it.isNullOrBlank() || it == "null" }

    private val displayFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    private val requestFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    init {
        val initial = savedStateHandle.get<String>("initialBirth") ?: ""
        setFormattedValue(initial)
    }

    fun onTextChange(newValue: TextFieldValue) {
        setFormattedValue(newValue.text)
    }

    fun onClear() {
        _uiState.update {
            it.copy(
                textFieldValue = TextFieldValue("", TextRange.Zero),
                error = null
            )
        }
    }

    fun onSave(onSuccess: (String, Boolean) -> Unit) {
        val input = _uiState.value.textFieldValue.text.trim()
        val validationMessage = validateBirth(input)
        if (validationMessage != null) {
            _uiState.update { it.copy(error = validationMessage) }
            return
        }

        val digits = input.filter { it.isDigit() }
        if (digits.length != 8) {
            _uiState.update { it.copy(error = GENERIC_SAVE_ERROR_MESSAGE) }
            return
        }

        val year = digits.substring(0, 4).toInt()
        val month = digits.substring(4, 6).toInt()
        val day = digits.substring(6, 8).toInt()
        val requestDate = LocalDate.of(year, month, day)

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

        val targetPetId = petId ?: run {
            val profile = when (val result = petRepository.getMyPetProfile()) {
                is AppResult.Success -> result.data
                is AppResult.Error -> {
                    val message = result.message ?: result.validation?.generalMessage ?: GENERIC_SAVE_ERROR_MESSAGE
                    _uiState.update { it.copy(isSaving = false, error = message) }
                    return@launch
                }
                is AppResult.Exception -> {
                    val message = result.throwable.message ?: GENERIC_SAVE_ERROR_MESSAGE
                    _uiState.update { it.copy(isSaving = false, error = message) }
                    return@launch
                }
            }

            petId = profile.id
            savedStateHandle["petId"] = profile.id
            profile.id
        }

        val request = UpdatePetRequest(
            birthdate = requestDate.format(requestFormatter),
        )

            when (val updateResult = petRepository.updatePetProfile(targetPetId, request)) {
                is AppResult.Success -> {
                    val serverBirth = updateResult.data.birthDate.format(displayFormatter)
                    setFormattedValue(serverBirth)
                    _uiState.update { it.copy(isSaving = false) }
                    onSuccess(serverBirth, true)
                }
                is AppResult.Error -> {
                    val message = updateResult.message ?: updateResult.validation?.generalMessage ?: GENERIC_SAVE_ERROR_MESSAGE
                    _uiState.update { it.copy(isSaving = false, error = message) }
                }
                is AppResult.Exception -> {
                    val message = updateResult.throwable.message ?: GENERIC_SAVE_ERROR_MESSAGE
                    _uiState.update { it.copy(isSaving = false, error = message) }
                }
            }
        }
    }

    private fun validateBirth(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return "생년월일을 입력해 주세요."

        val regex = Regex("""^\d{4}([./-])\d{1,2}\1\d{1,2}$""")
        if (!regex.matches(trimmed)) return "YYYY.MM.DD 형식으로 입력해 주세요."

        val normalized = normalizeForDisplay(trimmed)
        val parts = normalized.split(".")
        if (parts.size != 3) return "유효한 날짜가 아니에요."

        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()

        if (year !in 1900..2100) return "연도 범위를 확인해 주세요."
        if (month !in 1..12) return "월은 1~12 범위여야 해요."

        val maxDay = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28
            else -> 31
        }
        if (day !in 1..maxDay) return "유효한 날짜가 아니에요."

        return null
    }

    private fun setFormattedValue(raw: String) {
        val formatted = formatInput(raw)
        _uiState.update {
            it.copy(
                textFieldValue = TextFieldValue(
                    text = formatted,
                    selection = TextRange(formatted.length)
                ),
                error = null
            )
        }
    }

    private fun formatInput(raw: String): String {
        val digits = raw.filter { it.isDigit() }.take(8)
        if (digits.isEmpty()) return ""
        val builder = StringBuilder()
        digits.forEachIndexed { index, char ->
            builder.append(char)
            val isYearEnd = index == 3
            val isMonthEnd = index == 5
            val hasMore = index != digits.lastIndex
            if ((isYearEnd || isMonthEnd) && hasMore) {
                builder.append('.')
            }
        }
        return builder.toString()
    }

    private fun normalizeForDisplay(input: String): String =
        input.replace("-", ".")
            .replace("/", ".")
            .replace(" ", "")

    companion object {
        private const val GENERIC_SAVE_ERROR_MESSAGE = "생년월일을 저장하는 중 문제가 발생했어요. 잠시 후 다시 시도해주세요."
    }
}
