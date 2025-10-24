package com.example.pet_project_frontend.presentation.mypage.settings.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.upload.FileUploadManager
import com.example.pet_project_frontend.data.remote.upload.UploadType
import com.example.pet_project_frontend.domain.usecase.pet.RegisterNosePrintUseCase
import com.example.pet_project_frontend.presentation.mypage.settings.verification.components.VerificationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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

    data class IntroUiState(
        val showAlreadyVerifiedDialog: Boolean = false,
        val showUnknownErrorDialog: Boolean = false,
        val unknownErrorMessage: String? = null
    )

    data class ProcessingUiState(
        val isLoading: Boolean = false,
        val result: VerificationResult? = null,
        val errorMessage: String? = null,
        val isCancelled: Boolean = false
    )

    private val _introUiState = MutableStateFlow(IntroUiState())
    val introUiState: StateFlow<IntroUiState> = _introUiState.asStateFlow()

    private val _processingUiState = MutableStateFlow(ProcessingUiState())
    val processingUiState: StateFlow<ProcessingUiState> = _processingUiState.asStateFlow()

    private var selectedImage: File? = null
    private var currentJob: Job? = null

    fun onImageSelected(image: File) {
        selectedImage = image
    }

    fun startVerification(petId: String) {
        if (currentJob?.isActive == true) return
        val imageFile = selectedImage
        if (imageFile == null) {
            triggerUnknownError("이미지 파일을 찾을 수 없어요.")
            _processingUiState.value = ProcessingUiState(
                isLoading = false,
                result = VerificationResult.Unknown,
                errorMessage = "이미지 파일이 존재하지 않습니다."
            )
            return
        }

        currentJob = viewModelScope.launch {
            try {
                _processingUiState.value = ProcessingUiState(isLoading = true)

                when (val uploadResult = fileUploadManager.uploadFile(imageFile, UploadType.PET_NOSE_PRINT)) {
                    is AppResult.Success -> {
                        handleRegisterResult(petId, uploadResult.data)
                    }

                    is AppResult.Error -> {
                        triggerUnknownError(uploadResult.message)
                        _processingUiState.value = ProcessingUiState(
                            isLoading = false,
                            result = VerificationResult.Unknown,
                            errorMessage = uploadResult.message
                        )
                    }

                    is AppResult.Exception -> {
                        if (uploadResult.throwable is CancellationException) throw uploadResult.throwable
                        triggerUnknownError(uploadResult.throwable.message)
                        _processingUiState.value = ProcessingUiState(
                            isLoading = false,
                            result = VerificationResult.Unknown,
                            errorMessage = uploadResult.throwable.message
                        )
                    }
                }
            } catch (ce: CancellationException) {
                _processingUiState.value = ProcessingUiState(
                    isLoading = false,
                    result = null,
                    errorMessage = null,
                    isCancelled = true
                )
                throw ce
            } finally {
                currentJob = null
            }
        }
    }

    private suspend fun handleRegisterResult(petId: String, filePath: String) {
        when (val result = registerNosePrintUseCase(petId, filePath)) {
            is AppResult.Success -> {
                handleAnalysisStatus(result.data.status)
            }

            is AppResult.Error -> {
                triggerUnknownError(result.message)
                _processingUiState.value = ProcessingUiState(
                    isLoading = false,
                    result = VerificationResult.Unknown,
                    errorMessage = result.message
                )
            }

            is AppResult.Exception -> {
                val throwable = result.throwable
                if (throwable is CancellationException) throw throwable
                triggerUnknownError(throwable.message)
                _processingUiState.value = ProcessingUiState(
                    isLoading = false,
                    result = VerificationResult.Unknown,
                    errorMessage = throwable.message
                )
            }
        }
    }

    private fun handleAnalysisStatus(status: String) {
        when (status.uppercase(Locale.ROOT)) {
            "SUCCESS" -> {
                _processingUiState.value = ProcessingUiState(
                    isLoading = false,
                    result = VerificationResult.Success()
                )
                selectedImage = null
            }

            "ALREADY_VERIFIED", "ALREADY_REGISTERED" -> {
                showAlreadyVerifiedDialog()
                _processingUiState.value = ProcessingUiState(
                    isLoading = false,
                    result = VerificationResult.AlreadyVerified
                )
            }

            "DUPLICATE" -> {
                _processingUiState.value = ProcessingUiState(
                    isLoading = false,
                    result = VerificationResult.Duplicate
                )
            }

            "INVALID_IMAGE", "NOT_FOUND" -> {
                _processingUiState.value = ProcessingUiState(
                    isLoading = false,
                    result = VerificationResult.DetectionFailed
                )
            }

            else -> {
                triggerUnknownError(null)
                _processingUiState.value = ProcessingUiState(
                    isLoading = false,
                    result = VerificationResult.Unknown
                )
            }
        }
    }

    fun consumeProcessingResult() {
        _processingUiState.update { it.copy(result = null) }
    }

    fun cancelVerification() {
        currentJob?.cancel()
        currentJob = null
        _processingUiState.value = ProcessingUiState(
            isLoading = false,
            result = null,
            isCancelled = true
        )
    }

    fun onCancelCompleted() {
        _processingUiState.update { it.copy(isCancelled = false) }
    }

    fun showAlreadyVerifiedDialog() {
        _introUiState.update { it.copy(showAlreadyVerifiedDialog = true) }
    }

    fun dismissAlreadyVerifiedDialog() {
        _introUiState.update { it.copy(showAlreadyVerifiedDialog = false) }
    }

    fun showUnknownErrorDialog(message: String? = null) {
        _introUiState.update {
            it.copy(
                showUnknownErrorDialog = true,
                unknownErrorMessage = message
            )
        }
    }

    fun dismissUnknownErrorDialog() {
        _introUiState.update {
            it.copy(
                showUnknownErrorDialog = false,
                unknownErrorMessage = null
            )
        }
    }

    private fun triggerUnknownError(message: String?) {
        showUnknownErrorDialog(message)
    }
}
