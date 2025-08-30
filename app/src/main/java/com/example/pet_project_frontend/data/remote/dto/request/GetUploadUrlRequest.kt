package com.example.pet_project_frontend.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class GetUploadUrlRequest(
    @SerializedName("upload_type")
    val uploadType: String,
    @SerializedName("filename")
    val filename: String,
    @SerializedName("content_type")
    val contentType: String
)
