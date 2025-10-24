package com.example.pet_project_frontend.domain.model

// 게시글 작성자 정보
data class Author(
    val userId: String,
    val displayName: String,
    val profilePictureUrl: String?
)

// 반려동물 정보
data class PetInfo(
    val petId: String,
    val name: String,
    val breed: String,
    val age: Int?,
    val profileImageUrl: String?,
    val isVerified: Boolean = false
)

// 게시글
data class Post(
    val postId: String,
    val text: String,
    val mediaUrls: List<String>,
    val author: Author,
    val pet: PetInfo?,
    val likesCount: Int,
    val commentsCount: Int,
    val isLiked: Boolean,
    val createdAt: String,
    val updatedAt: String?
)

// 피드 (페이지네이션 포함)
data class PostsFeed(
    val posts: List<Post>,
    val nextCursor: String?,
    val hasMore: Boolean
)

// 업로드 URL 정보
data class UploadUrl(
    val uploadUrl: String,
    val filePath: String,
    val publicUrl: String?,
    val expiresAt: String? // 백엔드가 보내지 않는 경우 있음
)

// 댓글
data class Comment(
    val commentId: String,
    val postId: String,
    val text: String,
    val author: Author,
    val pet: PetInfo?,
    val likeCount: Int,
    val isLiked: Boolean,
    val createdAt: String
)

// 댓글 목록 (페이지네이션 포함)
data class CommentList(
    val comments: List<Comment>,
    val nextCursor: String?
)
