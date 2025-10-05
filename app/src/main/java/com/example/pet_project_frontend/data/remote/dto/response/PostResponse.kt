package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * 게시글 목록 응답 (커서 페이지네이션)
 */
data class PostListResponse(
    @SerializedName("posts")
    val posts: List<PostResponse>,
    @SerializedName("next_cursor")
    val nextCursor: String?
)

/**
 * 게시글 응답
 */
data class PostResponse(
    @SerializedName("post_id")
    val postId: String,
    @SerializedName("author")
    val author: AuthorDto,
    @SerializedName("pet")
    val pet: PetInfoDto,
    @SerializedName("image_urls")
    val imageUrls: List<String>,
    @SerializedName("text")
    val text: String,
    @SerializedName("like_count")
    val likeCount: Int,
    @SerializedName("comment_count")
    val commentCount: Int,
    @SerializedName("created_at")
    val createdAt: String, // ISO 8601 format
    @SerializedName("updated_at")
    val updatedAt: String, // ISO 8601 format
    @SerializedName("is_liked")
    val isLiked: Boolean? = null // 로그인 사용자만 포함
)

/**
 * 게시글 좋아요 토글 응답
 */
data class PostLikeToggleResponse(
    @SerializedName("message")
    val message: String
)
