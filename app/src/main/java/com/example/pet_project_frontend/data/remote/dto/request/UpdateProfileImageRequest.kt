package com.example.pet_project_frontend.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class UpdateProfileImageRequest(
    @SerializedName("file_path")
    val filePath: String
)
