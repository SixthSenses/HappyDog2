package com.example.pet_project_frontend.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class CreatePetRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("gender")
    val gender: String, // "MALE" or "FEMALE"
    @SerializedName("breed")
    val breed: String,
    @SerializedName("birthdate")
    val birthdate: String, // "YYYY-MM-DD"
    @SerializedName("fur_color")
    val furColor: String? = null,
    @SerializedName("health_concerns")
    val healthConcerns: List<String>? = null,
    // Optional field per OpenAPI PetRegistrationSchema
    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null
)
