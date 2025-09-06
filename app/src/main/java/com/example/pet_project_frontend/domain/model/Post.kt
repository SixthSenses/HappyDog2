package com.example.pet_project_frontend.domain.model

data class Post(
    val postId: String,
    val imageUrls: List<String>,
    val text: String?,
    val likeCount: Int,
    val commentCount: Int,
)

data class PostsPage(
    val items: List<Post>,
    val nextCursor: String?
)
