package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class SelectedPetResponse(
    @SerializedName("pet_id") val petId: String,
    @SerializedName("name") val name: String?,
    @SerializedName("profile_image_url") val profileImageUrl: String?,
    @SerializedName("updated_at") val updatedAt: String?
)
