package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class PostListResponse(
    @SerializedName("posts") val posts: List<PostResponse>,
    @SerializedName("next_cursor") val nextCursor: String?
)

data class PostResponse(
    @SerializedName("post_id") val postId: String,
    @SerializedName("image_urls") val imageUrls: List<String>?,
    @SerializedName("text") val text: String?,
    @SerializedName("like_count") val likeCount: Int?,
    @SerializedName("comment_count") val commentCount: Int?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("is_liked") val isLiked: Boolean?
)
