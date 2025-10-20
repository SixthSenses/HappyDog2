package com.example.pet_project_frontend.domain.repository

import android.net.Uri
import com.example.pet_project_frontend.domain.model.Post
import com.example.pet_project_frontend.domain.model.PostsFeed
import com.example.pet_project_frontend.domain.model.UploadUrl

interface PostRepository {
    // 업로드 URL 생성
    suspend fun getUploadUrl(contentType: String, uploadType: String, filename: String): Result<UploadUrl>
    
    // 파일 업로드
    suspend fun uploadFile(uploadUrl: String, fileUri: Uri, contentType: String): Result<Unit>
    
    // 게시글 생성
    suspend fun createPost(text: String, filePaths: List<String>): Result<Post>
    
    // 게시글 피드 조회
    suspend fun getPostsFeed(limit: Int = 20, cursor: String? = null): Result<PostsFeed>
    
    // 특정 게시글 조회
    suspend fun getPost(postId: String): Result<Post>
    
    // 게시글 좋아요 토글
    suspend fun toggleLike(postId: String): Result<Unit>
    
    // 게시글 수정
    suspend fun updatePost(postId: String, text: String): Result<Post>
    
    // 게시글 삭제
    suspend fun deletePost(postId: String): Result<Unit>
    
    // 특정 사용자의 게시물 피드 조회
    suspend fun getUserPosts(authorId: String, limit: Int = 20, cursor: String? = null): Result<PostsFeed>
}
