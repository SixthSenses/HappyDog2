// data/mungstar_model/LocalPostManager.kt
package com.example.pet_project_frontend.data.mungstar_model

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

object LocalPostManager {
    private val _posts = MutableStateFlow<List<PostResponse>>(emptyList())
    val posts: StateFlow<List<PostResponse>> = _posts.asStateFlow()

    fun addPost(text: String, imageUris: List<Uri>) {
        val currentTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())
        val postId = UUID.randomUUID().toString()

        val newPost = PostResponse(
            author = Author(
                nickname = "사용자",
                profile_image_url = null,
                user_id = "local-user-id"
            ),
            comment_count = 0,
            created_at = currentTime,
            image_urls = imageUris.map { it.toString() },
            is_liked = false,
            like_count = 0,
            pet = Pet(
                birthdate = "2020-01-01T00:00:00+00:00",
                breed = "믹스",
                name = "우리 강아지",
                pet_id = "local-pet-id"
            ),
            post_id = postId,
            text = text,
            updated_at = currentTime
        )

        // 새 게시물을 맨 앞에 추가
        _posts.value = listOf(newPost) + _posts.value
    }

    fun toggleLike(postId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.post_id == postId) {
                post.copy(
                    is_liked = !post.is_liked,
                    like_count = if (post.is_liked) post.like_count - 1 else post.like_count + 1
                )
            } else {
                post
            }
        }
    }
}