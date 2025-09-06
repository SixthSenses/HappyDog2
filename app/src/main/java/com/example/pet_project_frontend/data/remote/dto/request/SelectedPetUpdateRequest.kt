package com.example.pet_project_frontend.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class SelectedPetUpdateRequest(
    @SerializedName("pet_id") val petId: String
)
