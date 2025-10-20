package com.example.pet_project_frontend.data.repository

import android.util.Log
import com.example.pet_project_frontend.data.mapper.toDomain
import com.example.pet_project_frontend.data.remote.api.CommentApi
import com.example.pet_project_frontend.data.remote.dto.CommentCreateDto
import com.example.pet_project_frontend.domain.model.Comment
import com.example.pet_project_frontend.domain.model.CommentList
import com.example.pet_project_frontend.domain.repository.CommentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val commentApi: CommentApi
) : CommentRepository {
    
    companion object {
        private const val TAG = "CommentRepositoryImpl"
    }
    
    override suspend fun createComment(postId: String, text: String): Result<Comment> {
        return try {
            Log.d(TAG, "createComment: postId=$postId, text=${text.take(50)}")
            val response = commentApi.createComment(
                postId = postId,
                comment = CommentCreateDto(text = text)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val comment = response.body()!!.toDomain()
                Log.d(TAG, "createComment: 성공 - commentId=${comment.commentId}")
                Result.success(comment)
            } else {
                val errorMsg = "댓글 작성 실패: ${response.code()} - ${response.errorBody()?.string()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "createComment: 예외 발생", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getComments(
        postId: String,
        limit: Int,
        cursor: String?
    ): Result<CommentList> {
        return try {
            Log.d(TAG, "getComments: postId=$postId, limit=$limit, cursor=$cursor")
            val response = commentApi.getComments(
                postId = postId,
                limit = limit,
                cursor = cursor
            )
            
            if (response.isSuccessful && response.body() != null) {
                val commentList = response.body()!!.toDomain()
                Log.d(TAG, "getComments: 성공 - ${commentList.comments.size}개 조회")
                Result.success(commentList)
            } else {
                val errorMsg = "댓글 조회 실패: ${response.code()} - ${response.errorBody()?.string()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getComments: 예외 발생", e)
            Result.failure(e)
        }
    }
    
    override suspend fun deleteComment(commentId: String): Result<Unit> {
        return try {
            Log.d(TAG, "deleteComment: commentId=$commentId")
            val response = commentApi.deleteComment(commentId)
            
            if (response.isSuccessful) {
                Log.d(TAG, "deleteComment: 성공")
                Result.success(Unit)
            } else {
                val errorMsg = "댓글 삭제 실패: ${response.code()} - ${response.errorBody()?.string()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteComment: 예외 발생", e)
            Result.failure(e)
        }
    }
    
    override suspend fun toggleCommentLike(commentId: String): Result<Unit> {
        return try {
            Log.d(TAG, "toggleCommentLike: commentId=$commentId")
            val response = commentApi.toggleCommentLike(commentId)
            
            if (response.isSuccessful) {
                Log.d(TAG, "toggleCommentLike: 성공")
                Result.success(Unit)
            } else {
                val errorMsg = "댓글 좋아요 실패: ${response.code()} - ${response.errorBody()?.string()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "toggleCommentLike: 예외 발생", e)
            Result.failure(e)
        }
    }
}
