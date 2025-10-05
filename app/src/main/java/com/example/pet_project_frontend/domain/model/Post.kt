package com.example.pet_project_frontend.domain.model

import java.time.LocalDateTime

/**
 * 게시글 도메인 모델
 */
data class Post(
    val postId: String,
    val author: Author,
    val pet: PetInfo?,
    val imageUrls: List<String>,
    val text: String,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val isLiked: Boolean
)

/**
 * 게시글 페이지 (페이지네이션)
 */
data class PostsPage(
    val posts: List<Post>,
    val nextCursor: String?
)

/**
 * 작성자 정보
 */
data class Author(
    val userId: String,
    val nickname: String
)

/**
 * 펫 정보
 */
data class PetInfo(
    val petId: String,
    val name: String,
    val breed: String,
    val birthdate: LocalDateTime,
    val profileImageUrl: String?
)
