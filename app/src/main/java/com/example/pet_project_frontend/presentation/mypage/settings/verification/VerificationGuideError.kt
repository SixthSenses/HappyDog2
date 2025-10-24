package com.example.pet_project_frontend.presentation.mypage.settings.verification

enum class VerificationGuideError {
    Duplicate,
    DetectionFailed,
    AlreadyVerified,
    Unknown;

    companion object {
        fun fromName(value: String?): VerificationGuideError? =
            values().firstOrNull { it.name == value }
    }
}
