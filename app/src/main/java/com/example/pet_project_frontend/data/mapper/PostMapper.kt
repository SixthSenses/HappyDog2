package com.example.pet_project_frontend.data.mapper

import com.example.pet_project_frontend.data.mapper.CommunityMapper.toDomain
import com.example.pet_project_frontend.data.remote.dto.response.PostListResponse
import com.example.pet_project_frontend.data.remote.dto.response.PostResponse
import com.example.pet_project_frontend.domain.model.Post
import com.example.pet_project_frontend.domain.model.PostsPage
import java.time.ZonedDateTime

/**
 * Post DTO → Domain 모델 변환
 * CommunityMapper와 동일한 변환 로직
 */
object PostMapper {
    fun map(dto: PostResponse): Post = Post(
        postId = dto.postId,
        author = dto.author.toDomain(),
        pet = dto.pet.toDomain(),
        imageUrls = dto.imageUrls,
        text = dto.text,
        likeCount = dto.likeCount,
        commentCount = dto.commentCount,
        createdAt = ZonedDateTime.parse(dto.createdAt).toLocalDateTime(),
        updatedAt = if (dto.updatedAt.isNotBlank()) ZonedDateTime.parse(dto.updatedAt).toLocalDateTime() else null,
        isLiked = dto.isLiked ?: false
    )

    fun mapPage(dto: PostListResponse): PostsPage = PostsPage(
        posts = dto.posts.map { map(it) },
        nextCursor = dto.nextCursor
    )
}
