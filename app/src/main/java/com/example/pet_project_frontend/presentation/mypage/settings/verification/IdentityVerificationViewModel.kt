package com.example.pet_project_frontend.presentation.mypage.settings.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.upload.FileUploadManager
import com.example.pet_project_frontend.data.remote.upload.UploadType
import com.example.pet_project_frontend.domain.usecase.pet.RegisterNosePrintUseCase
import com.example.pet_project_frontend.presentation.mypage.settings.verification.components.VerificationResult
import android.content.Context
import android.net.Uri
import com.example.pet_project_frontend.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
class IdentityVerificationViewModel @Inject constructor(
    private val registerNosePrintUseCase: RegisterNosePrintUseCase,
    private val fileUploadManager: FileUploadManager,
    private val petRepository: PetRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "IdentityVerificationViewModel"
    }

    private val bypassVerification = false  // 실제 백엔드 호출 활성화
    
    private var cachedPetId: String? = null

    data class IntroUiState(
        val showAlreadyVerifiedDialog: Boolean = false,
        val showUnknownErrorDialog: Boolean = false,
        val unknownErrorMessage: String? = null
    )

    data class GuideUiState(
        val errorDialog: VerificationGuideError? = null
    )

    data class ProcessingUiState(
        val isLoading: Boolean = false,
        val result: VerificationResult? = null,
        val errorMessage: String? = null,
        val isCancelled: Boolean = false
    )

    private val _introUiState = MutableStateFlow(IntroUiState())
    val introUiState: StateFlow<IntroUiState> = _introUiState.asStateFlow()

    private val _guideUiState = MutableStateFlow(GuideUiState())
    val guideUiState: StateFlow<GuideUiState> = _guideUiState.asStateFlow()

    private val _processingUiState = MutableStateFlow(ProcessingUiState())
    val processingUiState: StateFlow<ProcessingUiState> = _processingUiState.asStateFlow()

    private var selectedImage: File? = null
    private var currentJob: Job? = null

    fun onImageSelected(image: File) {
        selectedImage = image
    }

    /**
     * URI를 통해 이미지를 선택하고 File로 변환
     */
    fun setImageFromUri(uriString: String) {
        try {
            val uri = Uri.parse(uriString)
            selectedImage = uriToFile(context, uri)
        } catch (e: Exception) {
            e.printStackTrace()
            selectedImage = null
        }
    }

    /**
     * Intro 화면 진입 시 펫의 인증 상태를 사전에 확인
     */
    fun checkVerificationStatus() {
        viewModelScope.launch {
            try {
                when (val result = petRepository.getMyPetProfile()) {
                    is AppResult.Success -> {
                        // 캐시 저장
                        cachedPetId = result.data.id
                        
                        // 이미 인증된 경우 다이얼로그 표시
                        if (result.data.isVerified) {
                            showAlreadyVerifiedDialog()
                        }
                    }
                    is AppResult.Error -> {
                        // 펫 정보 로드 실패는 무시 (진행 허용)
                        android.util.Log.w(TAG, "Failed to check verification status: ${result.message}")
                    }
                    is AppResult.Exception -> {
                        android.util.Log.w(TAG, "Exception checking verification status", result.throwable)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error checking verification status", e)
            }
        }
    }

    /**
     * 현재 사용자의 petId를 가져옴 (캐싱 적용)
     */
    suspend fun getPetId(): String? {
        if (cachedPetId != null) return cachedPetId
        
        return when (val result = petRepository.getMyPetProfile()) {
            is AppResult.Success -> {
                cachedPetId = result.data.id
                cachedPetId
            }
            else -> null
        }
    }

    /**
     * URI를 File 객체로 변환
     */
    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            when (uri.scheme) {
                "file" -> {
                    File(uri.path ?: return null)
                }
                "content" -> {
                    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
                    val tempFile = File.createTempFile(
                        "temp_nose_print_${System.currentTimeMillis()}",
                        ".jpg",
                        context.cacheDir
                    )
                    tempFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    inputStream.close()
                    tempFile
                }
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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

        if (bypassVerification) {
            currentJob = viewModelScope.launch {
                _processingUiState.value = ProcessingUiState(isLoading = true)
                delay(2100)
                _processingUiState.value = ProcessingUiState(
                    isLoading = false,
                    result = VerificationResult.Success()
                )
                selectedImage = null
            }
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
                // HTTP 에러 코드를 분석 status로 변환하여 통합 처리
                val status = mapErrorCodeToStatus(result.code, result.message)
                android.util.Log.d(TAG, "Error response mapped to status: $status (code: ${result.code})")
                handleAnalysisStatus(status)
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

    /**
     * HTTP 에러 코드를 비문 분석 status로 변환
     * 백엔드는 두 가지 방식으로 응답:
     * 1) 200 OK + status 필드 (SUCCESS, ALREADY_VERIFIED, DUPLICATE, etc.)
     * 2) 4xx/5xx + error_code (BIO_ALREADY_VERIFIED, etc.)
     * 
     * 이 메서드는 에러 응답을 status 형식으로 변환하여 통합 처리
     */
    private fun mapErrorCodeToStatus(code: Int?, message: String?): String {
        return when {
            // 409 CONFLICT: BIO_ALREADY_VERIFIED
            code == 409 || message?.contains("ALREADY_VERIFIED", ignoreCase = true) == true -> {
                "ALREADY_VERIFIED"
            }
            // 400 BAD_REQUEST: 중복 비문 등
            message?.contains("DUPLICATE", ignoreCase = true) == true -> {
                "DUPLICATE"
            }
            // 400/404: 비문 감지 실패
            message?.contains("NOT_FOUND", ignoreCase = true) == true ||
            message?.contains("INVALID_IMAGE", ignoreCase = true) == true -> {
                "INVALID_IMAGE"
            }
            // 기타 에러는 ERROR로 매핑
            else -> {
                "ERROR"
            }
        }
    }

    /**
     * 비문 분석 결과 status를 처리하여 UI 상태 업데이트
     * 
     * Status 값:
     * - SUCCESS: 신원 인증 성공
     * - ALREADY_VERIFIED/ALREADY_REGISTERED: 이미 인증된 펫
     * - DUPLICATE: 다른 펫에 이미 등록된 비문
     * - INVALID_IMAGE/NOT_FOUND: 비문 감지 실패
     * - ERROR: 알 수 없는 오류
     */
    private fun handleAnalysisStatus(status: String) {
        android.util.Log.d(TAG, "Handling analysis status: $status")
        
        when (status.uppercase(Locale.ROOT)) {
            "SUCCESS" -> {
                android.util.Log.d(TAG, "Verification succeeded")
                _processingUiState.value = ProcessingUiState(
                    isLoading = false,
                    result = VerificationResult.Success()
                )
                selectedImage = null
            }

            "ALREADY_VERIFIED", "ALREADY_REGISTERED" -> {
                android.util.Log.d(TAG, "Pet already verified")
                showAlreadyVerifiedDialog()
                _processingUiState.value = ProcessingUiState(
                    isLoading = false,
                    result = VerificationResult.AlreadyVerified
                )
            }

            "DUPLICATE" -> {
                android.util.Log.d(TAG, "Duplicate nose print detected")
                _processingUiState.value = ProcessingUiState(
                    isLoading = false,
                    result = VerificationResult.Duplicate
                )
            }

            "INVALID_IMAGE", "NOT_FOUND" -> {
                android.util.Log.d(TAG, "Nose print detection failed")
                _processingUiState.value = ProcessingUiState(
                    isLoading = false,
                    result = VerificationResult.DetectionFailed
                )
            }

            "ERROR" -> {
                android.util.Log.w(TAG, "Error status received: $status")
                triggerUnknownError(null)
                _processingUiState.value = ProcessingUiState(
                    isLoading = false,
                    result = VerificationResult.Unknown
                )
            }

            else -> {
                android.util.Log.w(TAG, "Unknown status: $status")
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

    fun showGuideError(error: VerificationGuideError) {
        _guideUiState.update { it.copy(errorDialog = error) }
    }

    fun dismissGuideError() {
        _guideUiState.update { it.copy(errorDialog = null) }
    }

    private fun triggerUnknownError(message: String?) {
        showUnknownErrorDialog(message)
    }
}
