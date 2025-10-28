package com.example.pet_project_frontend.data.mapper

import com.example.pet_project_frontend.data.remote.dto.*
import com.example.pet_project_frontend.domain.model.*

// Author DTO → Domain
fun AuthorDto.toDomain(): Author {
    return Author(
        userId = userId,
        displayName = nickname, // DTO의 nickname → Domain의 displayName
        profilePictureUrl = profilePictureUrl
    )
}

// PetInfo DTO → Domain
fun PetInfoDto.toDomain(): PetInfo {
    // birthdate (ISO 8601)를 age로 변환
    val age = try {
        val birth = java.time.LocalDate.parse(birthdate.substring(0, 10))
        val now = java.time.LocalDate.now()
        java.time.Period.between(birth, now).years
    } catch (e: Exception) {
        null
    }
    
    return PetInfo(
        petId = petId,
        name = name,
        breed = breed,
        age = age,
        profileImageUrl = profileImageUrl,
        isVerified = isVerified
    )
}

// Post DTO → Domain
fun PostResponseDto.toDomain(): Post {
    return Post(
        postId = postId,
        text = text,
        mediaUrls = imageUrls, // DTO의 imageUrls → Domain의 mediaUrls
        author = author.toDomain(),
        pet = pet?.toDomain(),
        likesCount = likeCount, // DTO의 likeCount → Domain의 likesCount
        commentsCount = commentCount, // DTO의 commentCount → Domain의 commentsCount
        isLiked = isLiked,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// PostsFeed DTO → Domain
fun PostsFeedResponseDto.toDomain(): PostsFeed {
    return PostsFeed(
        posts = posts.map { it.toDomain() },
        nextCursor = nextCursor,
        hasMore = hasMore
    )
}

// UploadUrl DTO → Domain
fun UploadUrlResponseDto.toDomain(): UploadUrl {
    return UploadUrl(
        uploadUrl = uploadUrl,
        filePath = filePath,
        publicUrl = publicUrl,
        expiresAt = expiresAt
    )
}
