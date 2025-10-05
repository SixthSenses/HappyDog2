package com.example.pet_project_frontend.data.remote.dto.request

import com.google.gson.annotations.SerializedName

/**
 * 게시글 생성 요청
 * POST /api/posts/
 */
data class PostCreateRequest(
    @SerializedName("text")
    val text: String, // 1-2000자
    @SerializedName("file_paths")
    val filePaths: List<String> // 최소 1개
)

/**
 * 게시글 수정 요청
 * PATCH /api/posts/{post_id}
 */
data class PostUpdateRequest(
    @SerializedName("text")
    val text: String // 1-2000자
)

/**
 * 댓글 생성 요청
 * POST /api/comments/posts/{post_id}/comments
 */
data class CommentCreateRequest(
    @SerializedName("text")
    val text: String // 1-1000자
)

// EmptyRequest는 EmptyRequest.kt에 정의되어 있음
