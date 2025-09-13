package com.example.pet_project_frontend.data.mungstar_model

import com.google.gson.annotations.SerializedName

data class CreatePostRequest(
    val text: String,
    val file_paths: List<String>
)

data class PostResponse(
    val author: Author,
    val comment_count: Int,
    val created_at: String,
    val image_urls: List<String>,
    val is_liked: Boolean,
    val like_count: Int,
    val pet: Pet,
    val post_id: String,
    val text: String,
    val updated_at: String
)

data class Author(
    val nickname: String,
    val profile_image_url: String?,
    val user_id: String
)

data class Pet(
    val birthdate: String,
    val breed: String,
    val name: String,
    val pet_id: String
)