package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

// ===== 반려동물 (Pets) Response =====

data class PetProfileResponse(
    @SerializedName("pet_id")
    val petId: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("breed")
    val breed: String,
    @SerializedName("birthdate")
    val birthdate: String,
    @SerializedName("initial_weight")
    val initialWeight: Float,
    @SerializedName("fur_color")
    val furColor: String?,
    @SerializedName("health_concerns")
    val healthConcerns: List<String>?,
    @SerializedName("is_verified")
    val isVerified: Boolean,
    @SerializedName("nose_print_url")
    val nosePrintUrl: String?,
    @SerializedName("faiss_id")
    val faissId: Int?
)

data class BiometricAnalysisResponse(
    @SerializedName("status")
    val status: String // "SUCCESS", "ALREADY_VERIFIED", "DUPLICATE", "INVALID_IMAGE", "ERROR"
)

data class EyeAnalysisResponse(
    @SerializedName("analysis_id")
    val analysisId: String,
    @SerializedName("disease_name")
    val diseaseName: String,
    @SerializedName("probability")
    val probability: Float
)