package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class PetCareRecordResponse(
    @SerializedName("log_id")
    val logId: String,
    @SerializedName("pet_id")
    val petId: String,
    @SerializedName("record_type")
    val recordType: String,
    val data: Double,
    val timestamp: String,
    @SerializedName("searchDate")
    val searchDate: String,
    val notes: String?
)
