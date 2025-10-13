package com.example.pet_project_frontend.data.remote.dto.request

import com.google.gson.annotations.SerializedName

/**
 * PATCH /api/pets/{pet_id} 요청 스키마
 * OpenAPI PetUpdateSchema와 일치
 * 모든 필드가 optional (부분 업데이트 지원)
 * 
 * 업데이트 가능 필드 (2025-10-13 백엔드 업데이트):
 * - name: 반려동물 이름
 * - gender: 성별 (FEMALE, MALE)
 * - breed: 견종
 * - birthdate: 생년월일 (yyyy-MM-dd)
 * - fur_color: 털 색깔
 * - health_concerns: 건강 관심사 목록
 * - profile_image_url: 프로필 이미지 URL
 */
data class UpdatePetRequest(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("breed")
    val breed: String? = null,
    @SerializedName("birthdate")
    val birthdate: String? = null,
    @SerializedName("fur_color")
    val furColor: String? = null,
    @SerializedName("health_concerns")
    val healthConcerns: List<String>? = null,
    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null
)
