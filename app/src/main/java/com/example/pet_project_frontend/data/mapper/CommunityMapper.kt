package com.example.pet_project_frontend.data.mapper

import com.example.pet_project_frontend.data.remote.dto.response.AuthorDto
import com.example.pet_project_frontend.data.remote.dto.response.CommentListResponse
import com.example.pet_project_frontend.data.remote.dto.response.CommentResponse
import com.example.pet_project_frontend.data.remote.dto.response.PetInfoDto
import com.example.pet_project_frontend.data.remote.dto.response.PostListResponse
import com.example.pet_project_frontend.data.remote.dto.response.PostResponse
import com.example.pet_project_frontend.domain.model.Author
import com.example.pet_project_frontend.domain.model.Comment
import com.example.pet_project_frontend.domain.model.CommentsPage
import com.example.pet_project_frontend.domain.model.PetInfo
import com.example.pet_project_frontend.domain.model.Post
import com.example.pet_project_frontend.domain.model.PostsPage
import java.time.LocalDateTime
import java.time.ZonedDateTime

/**
 * Community DTO → Domain 모델 변환
 */
object CommunityMapper {
    
    fun AuthorDto.toDomain(): Author {
        return Author(
            userId = userId,
            nickname = nickname
        )
    }
    
    fun PetInfoDto.toDomain(): PetInfo {
        return PetInfo(
            petId = petId,
            name = name,
            breed = breed,
            birthdate = ZonedDateTime.parse(birthdate).toLocalDateTime(),
            profileImageUrl = profileImageUrl
        )
    }
    
    fun PostResponse.toDomain(): Post {
        return Post(
            postId = postId,
            author = author.toDomain(),
            pet = pet.toDomain(),
            imageUrls = imageUrls,
            text = text,
            likeCount = likeCount,
            commentCount = commentCount,
            createdAt = ZonedDateTime.parse(createdAt).toLocalDateTime(),
            updatedAt = if (updatedAt.isNotBlank()) ZonedDateTime.parse(updatedAt).toLocalDateTime() else null,
            isLiked = isLiked ?: false
        )
    }
    
    fun PostListResponse.toDomain(): PostsPage {
        return PostsPage(
            posts = posts.map { it.toDomain() },
            nextCursor = nextCursor
        )
    }
    
    fun CommentResponse.toDomain(): Comment {
        return Comment(
            commentId = commentId,
            postId = postId,
            author = author.toDomain(),
            pet = pet.toDomain(),
            text = text,
            likeCount = likeCount,
            createdAt = ZonedDateTime.parse(createdAt).toLocalDateTime(),
            isLiked = isLiked ?: false
        )
    }
    
    fun CommentListResponse.toDomain(): CommentsPage {
        return CommentsPage(
            comments = comments.map { it.toDomain() },
            nextCursor = nextCursor
        )
    }
}
