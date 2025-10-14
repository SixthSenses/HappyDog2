package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * 품종 가이드북 응답 DTO
 */
data class BreedGuidebookResponse(
    @SerializedName("breed_name")
    val breedName: String,
    
    @SerializedName("english_name")
    val englishName: String,
    
    @SerializedName("basic_info")
    val basicInfo: BasicInfo,
    
    @SerializedName("personality")
    val personality: Personality,
    
    @SerializedName("common_diseases")
    val commonDiseases: List<String>,
    
    @SerializedName("care_points")
    val carePoints: List<String>
)

/**
 * 기본 정보
 */
data class BasicInfo(
    @SerializedName("weight")
    val weight: String,
    
    @SerializedName("height")
    val height: String,
    
    @SerializedName("life_span")
    val lifeSpan: String,
    
    @SerializedName("origin")
    val origin: String
)

/**
 * 성격 정보
 */
data class Personality(
    @SerializedName("strengths")
    val strengths: String,
    
    @SerializedName("weaknesses")
    val weaknesses: String,
    
    @SerializedName("traits")
    val traits: String
)