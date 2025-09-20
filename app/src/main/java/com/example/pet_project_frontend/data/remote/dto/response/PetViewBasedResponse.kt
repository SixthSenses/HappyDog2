package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * PetViewBasedResponseSchema (openapi)
 */
 data class PetViewBasedResponse(
     @SerializedName("is_verified") val isVerified: Boolean,
     @SerializedName("post_count") val postCount: Int?,
     @SerializedName("name") val name: String,
     @SerializedName("birthdate") val birthdate: String?,
     @SerializedName("pet_id") val petId: String,
     @SerializedName("age_months") val ageMonths: Int?,
     @SerializedName("profile_image_url") val profileImageUrl: String?,
     @SerializedName("gender") val gender: String?,
     @SerializedName("breed") val breed: String?
 )
