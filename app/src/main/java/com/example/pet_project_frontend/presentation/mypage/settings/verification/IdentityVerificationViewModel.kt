package com.example.pet_project_frontend.presentation.mypage.settings.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.upload.FileUploadManager
import com.example.pet_project_frontend.data.remote.upload.UploadType
import com.example.pet_project_frontend.domain.usecase.pet.RegisterNosePrintUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class IdentityVerificationViewModel @Inject constructor(
    private val registerNosePrintUseCase: RegisterNosePrintUseCase,
    private val fileUploadManager: FileUploadManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(IdentityVerificationUiState())
    val uiState: StateFlow<IdentityVerificationUiState> = _uiState.asStateFlow()

    private var selectedImageFile: File? = null

    fun onImageSelected(file: File?) {
        selectedImageFile = file
        _uiState.update {
            it.copy(
                selectedImagePath = file?.absolutePath,
                errorMessage = null,
                verificationResult = VerificationResult.Idle,
                progressStep = 0
            )
        }
    }

    fun submitVerification(petId: String) {
        val imageFile = selectedImageFile
        if (_uiState.value.isUploading || _uiState.value.isVerifying) return
        if (petId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "반려견 ID가 필요합니다.") }
            return
        }
        if (imageFile?.exists() != true) {
            _uiState.update { it.copy(errorMessage = "분석할 비문 이미지를 선택해 주세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUploading = true,
                    errorMessage = null,
                    verificationResult = VerificationResult.Idle,
                    progressStep = 0
                )
            }

            when (val uploadResult = fileUploadManager.uploadFile(
                file = imageFile,
                uploadType = UploadType.PET_NOSE_PRINT
            )) {
                is AppResult.Success -> handleUploadSuccess(petId, uploadResult.data)
                is AppResult.Error -> _uiState.update {
                    it.copy(
                        isUploading = false,
                        errorMessage = uploadResult.message ?: "비문 이미지 업로드에 실패했습니다.",
                        progressStep = 0
                    )
                }

                is AppResult.Exception -> _uiState.update {
                    it.copy(
                        isUploading = false,
                        errorMessage = uploadResult.throwable.message
                            ?: "비문 이미지 업로드 중 오류가 발생했습니다.",
                        progressStep = 0
                    )
                }
            }
        }
    }

    private suspend fun handleUploadSuccess(petId: String, filePath: String) {
        _uiState.update {
            it.copy(
                isUploading = false,
                isVerifying = true,
                uploadedFilePath = filePath,
                errorMessage = null,
                progressStep = 1
            )
        }

        when (val result = registerNosePrintUseCase(petId = petId, filePath = filePath)) {
            is AppResult.Success -> {
                val status = VerificationResult.from(result.data.status)
                _uiState.update {
                    it.copy(
                        isVerifying = false,
                        verificationResult = status,
                        rawStatus = result.data.status,
                        errorMessage = if (status == VerificationResult.Unknown) {
                            "알 수 없는 검증 결과가 반환되었습니다. (${result.data.status})"
                        } else null,
                        progressStep = 2
                    )
                }
            }

            is AppResult.Error -> _uiState.update {
                it.copy(
                    isVerifying = false,
                    errorMessage = result.message ?: "비문 검증에 실패했습니다.",
                    verificationResult = VerificationResult.Failed,
                    progressStep = 2
                )
            }

            is AppResult.Exception -> _uiState.update {
                it.copy(
                    isVerifying = false,
                    errorMessage = result.throwable.message ?: "비문 검증 중 오류가 발생했습니다.",
                    verificationResult = VerificationResult.Failed,
                    progressStep = 2
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetVerificationResult() {
        selectedImageFile = null
        _uiState.update {
            it.copy(
                selectedImagePath = null,
                uploadedFilePath = null,
                isUploading = false,
                isVerifying = false,
                errorMessage = null,
                verificationResult = VerificationResult.Idle,
                rawStatus = null,
                progressStep = 0
            )
        }
    }
}

data class IdentityVerificationUiState(
    val selectedImagePath: String? = null,
    val uploadedFilePath: String? = null,
    val isUploading: Boolean = false,
    val isVerifying: Boolean = false,
    val verificationResult: VerificationResult = VerificationResult.Idle,
    val rawStatus: String? = null,
    val errorMessage: String? = null,
    val progressStep: Int = 0
)

enum class VerificationResult {
    Idle,
    Success,
    AlreadyVerified,
    Duplicate,
    InvalidImage,
    Failed,
    Unknown;

    companion object {
        fun from(status: String?): VerificationResult {
            return when (status?.uppercase()) {
                "SUCCESS" -> Success
                "ALREADY_VERIFIED" -> AlreadyVerified
                "DUPLICATE" -> Duplicate
                "INVALID_IMAGE" -> InvalidImage
                "ERROR" -> Failed
                null, "" -> Unknown
                else -> Unknown
            }
        }
    }
}
