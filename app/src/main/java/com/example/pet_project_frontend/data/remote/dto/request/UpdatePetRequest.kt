package com.example.pet_project_frontend.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class UpdatePetRequest(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("fur_color")
    val furColor: String? = null,
    @SerializedName("health_concerns")
    val healthConcerns: List<String>? = null
)
