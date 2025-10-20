package com.example.pet_project_frontend.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.pet_project_frontend.data.mapper.toDomain
import com.example.pet_project_frontend.data.remote.api.PostApi
import com.example.pet_project_frontend.data.remote.api.UploadApi
import com.example.pet_project_frontend.data.remote.dto.PostCreateDto
import com.example.pet_project_frontend.data.remote.dto.UploadUrlRequestDto
import com.example.pet_project_frontend.domain.model.Post
import com.example.pet_project_frontend.domain.model.PostsFeed
import com.example.pet_project_frontend.domain.model.UploadUrl
import com.example.pet_project_frontend.domain.repository.PostRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postApi: PostApi,
    private val uploadApi: UploadApi,
    @ApplicationContext private val context: Context
) : PostRepository {

    override suspend fun getUploadUrl(contentType: String, uploadType: String, filename: String): Result<UploadUrl> {
        return try {
            val response = uploadApi.getUploadUrl(
                UploadUrlRequestDto(
                    contentType = contentType,
                    uploadType = uploadType,
                    filename = filename
                )
            )
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("업로드 URL 생성 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadFile(uploadUrl: String, fileUri: Uri, contentType: String): Result<Unit> {
        return try {
            Log.d(TAG, "uploadFile: Starting upload to $uploadUrl")
            Log.d(TAG, "uploadFile: File URI: $fileUri")
            
            // Uri를 파일로 변환
            val inputStream = context.contentResolver.openInputStream(fileUri)
            if (inputStream == null) {
                Log.e(TAG, "uploadFile: Cannot open input stream for $fileUri")
                return Result.failure(Exception("파일을 열 수 없습니다"))
            }
            
            val tempFile = File(context.cacheDir, "upload_temp_${System.currentTimeMillis()}")
            Log.d(TAG, "uploadFile: Creating temp file: ${tempFile.absolutePath}")
            
            FileOutputStream(tempFile).use { output ->
                val bytesCopied = inputStream.copyTo(output)
                Log.d(TAG, "uploadFile: Copied $bytesCopied bytes to temp file")
            }
            inputStream.close()

            // RequestBody 생성
            val requestBody = tempFile.asRequestBody(contentType.toMediaTypeOrNull())
            Log.d(TAG, "uploadFile: Created RequestBody with content-type: $contentType")

            // Pre-signed URL로 업로드
            Log.d(TAG, "uploadFile: Uploading to pre-signed URL...")
            val response = uploadApi.uploadFile(
                url = uploadUrl,
                contentType = contentType,
                file = requestBody
            )

            // 임시 파일 삭제
            tempFile.delete()

            if (response.isSuccessful) {
                Log.d(TAG, "uploadFile: Upload successful! Response code: ${response.code()}")
                Result.success(Unit)
            } else {
                val errorMsg = "파일 업로드 실패: ${response.code()}, ${response.message()}"
                Log.e(TAG, "uploadFile: $errorMsg")
                Log.e(TAG, "uploadFile: Error body: ${response.errorBody()?.string()}")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "uploadFile: Exception occurred", e)
            Result.failure(e)
        }
    }

    override suspend fun createPost(text: String, filePaths: List<String>): Result<Post> {
        return try {
            val response = postApi.createPost(
                PostCreateDto(
                    text = text,
                    filePaths = filePaths
                )
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("게시글 생성 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPostsFeed(limit: Int, cursor: String?): Result<PostsFeed> {
        return try {
            val response = postApi.getPostsFeed(limit, cursor)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("피드 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPost(postId: String): Result<Post> {
        return try {
            val response = postApi.getPost(postId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("게시글 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleLike(postId: String): Result<Unit> {
        return try {
            val response = postApi.toggleLike(postId)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("좋아요 토글 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePost(postId: String, text: String): Result<Post> {
        return try {
            Log.d(TAG, "updatePost: postId=$postId, text=${text.take(50)}")
            val response = postApi.updatePost(
                postId = postId,
                request = com.example.pet_project_frontend.data.remote.dto.PostUpdateDto(text = text)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val post = response.body()!!.toDomain()
                Log.d(TAG, "updatePost: 성공")
                Result.success(post)
            } else {
                val errorMsg = "게시글 수정 실패: ${response.code()} - ${response.errorBody()?.string()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "updatePost: 예외 발생", e)
            Result.failure(e)
        }
    }

    override suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            Log.d(TAG, "deletePost: postId=$postId")
            val response = postApi.deletePost(postId)
            
            if (response.isSuccessful) {
                Log.d(TAG, "deletePost: 성공")
                Result.success(Unit)
            } else {
                val errorMsg = "게시글 삭제 실패: ${response.code()} - ${response.errorBody()?.string()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "deletePost: 예외 발생", e)
            Result.failure(e)
        }
    }

    override suspend fun getUserPosts(authorId: String, limit: Int, cursor: String?): Result<PostsFeed> {
        return try {
            Log.d(TAG, "getUserPosts: authorId=$authorId, limit=$limit, cursor=$cursor")
            val response = postApi.getUserPosts(
                authorId = authorId,
                limit = limit,
                cursor = cursor
            )
            
            if (response.isSuccessful && response.body() != null) {
                val feed = response.body()!!.toDomain()
                Log.d(TAG, "getUserPosts: 성공 - ${feed.posts.size}개 조회")
                Result.success(feed)
            } else {
                val errorMsg = "사용자 게시물 조회 실패: ${response.code()} - ${response.errorBody()?.string()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getUserPosts: 예외 발생", e)
            Result.failure(e)
        }
    }

    private fun getExtensionFromMimeType(mimeType: String): String? {
        return when {
            mimeType.contains("jpeg") || mimeType.contains("jpg") -> "jpg"
            mimeType.contains("png") -> "png"
            mimeType.contains("gif") -> "gif"
            mimeType.contains("webp") -> "webp"
            else -> null
        }
    }
    
    companion object {
        private const val TAG = "PostRepositoryImpl"
    }
}
