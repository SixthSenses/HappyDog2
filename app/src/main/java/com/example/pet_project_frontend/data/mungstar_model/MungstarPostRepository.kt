package com.example.pet_project_frontend.data.mungstar_model

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.pet_project_frontend.BuildConfig
import com.example.pet_project_frontend.data.auth.TokenManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MungstarPostRepository(private val context: Context) {

    private val BASE_URL = BuildConfig.API_BASE_URL

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    private fun getAuthToken(): String {
        Log.d("TokenDebug", "=== 토큰 디버깅 시작 ===")

        // SharedPreferences 직접 확인
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val allKeys = sharedPrefs.all
        Log.d("TokenDebug", "SharedPreferences 모든 키-값: $allKeys")

        val tokenManager = TokenManager(context)
        Log.d("TokenDebug", "TokenManager 생성 완료")

        val token = tokenManager.getToken()
        Log.d("TokenDebug", "TokenManager.getToken() 결과: $token")
        Log.d("TokenDebug", "TokenManager.hasToken() 결과: ${tokenManager.hasToken()}")

        // 다른 가능한 키 이름들도 확인
        val possibleKeys = listOf("access_token", "auth_token", "token", "user_token", "bearer_token")
        for (key in possibleKeys) {
            val value = sharedPrefs.getString(key, null)
            Log.d("TokenDebug", "키 '$key': $value")
        }

        Log.d("MungstarPostRepository", "BASE_URL: $BASE_URL")
        Log.d("MungstarPostRepository", "저장된 토큰: $token")
        Log.d("MungstarPostRepository", "토큰 존재 여부: ${tokenManager.hasToken()}")

        if (token == null) {
            Log.e("MungstarPostRepository", "토큰이 null입니다!")
            Log.e("TokenDebug", "=== 토큰을 찾을 수 없습니다! ===")
            throw Exception("로그인이 필요합니다.")
        }

        val authHeader = "Token $token"
        Log.d("MungstarPostRepository", "Authorization 헤더: $authHeader")

        return authHeader
    }

    suspend fun getPosts(): List<CreatePostResponse> {
        Log.d("MungstarPostRepository", "게시물 목록 조회 시작")
        try {
            val response = apiService.getPosts(getAuthToken())
            Log.d("MungstarPostRepository", "API 응답 코드: ${response.code()}")
            Log.d("MungstarPostRepository", "API 응답 메시지: ${response.message()}")

            if (response.isSuccessful) {
                val posts = response.body() ?: emptyList()
                Log.d("MungstarPostRepository", "받은 게시물 수: ${posts.size}")
                return posts
            } else {
                Log.e("MungstarPostRepository", "게시물 조회 실패: ${response.code()} - ${response.message()}")
                throw Exception("게시물을 불러올 수 없습니다: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("MungstarPostRepository", "게시물 조회 중 오류", e)
            throw e
        }
    }

    suspend fun createPost(request: CreatePostRequest): CreatePostResponse {
        Log.d("MungstarPostRepository", "게시물 작성 시작")
        Log.d("MungstarPostRepository", "요청 텍스트: ${request.text}")
        Log.d("MungstarPostRepository", "파일 경로 수: ${request.filePaths.size}")

        try {
            val response = apiService.createPost(getAuthToken(), request)
            Log.d("MungstarPostRepository", "게시물 작성 응답 코드: ${response.code()}")
            Log.d("MungstarPostRepository", "게시물 작성 응답 메시지: ${response.message()}")

            if (response.isSuccessful) {
                val result = response.body() ?: throw Exception("응답 데이터가 비어있습니다.")
                Log.d("MungstarPostRepository", "게시물 작성 성공: ${result.postId}")
                return result
            } else {
                Log.e("MungstarPostRepository", "게시물 작성 실패: ${response.code()} - ${response.message()}")
                Log.e("MungstarPostRepository", "응답 에러 바디: ${response.errorBody()?.string()}")
                throw Exception("게시물 작성에 실패했습니다: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("MungstarPostRepository", "게시물 작성 중 오류", e)
            throw e
        }
    }

    suspend fun uploadImage(context: Context, imageUri: Uri): String {
        Log.d("MungstarPostRepository", "이미지 업로드 시작: $imageUri")

        try {
            // Uri에서 실제 파일로 변환
            val file = uriToFile(context, imageUri)
            Log.d("MungstarPostRepository", "임시 파일 생성: ${file.absolutePath}, 크기: ${file.length()} bytes")

            // Multipart body 생성
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

            val response = apiService.uploadImage(getAuthToken(), body)
            Log.d("MungstarPostRepository", "이미지 업로드 응답 코드: ${response.code()}")

            // 임시 파일 삭제
            file.delete()

            if (response.isSuccessful) {
                val result = response.body()?.filePath ?: throw Exception("파일 경로를 받을 수 없습니다.")
                Log.d("MungstarPostRepository", "이미지 업로드 성공: $result")
                return result
            } else {
                Log.e("MungstarPostRepository", "이미지 업로드 실패: ${response.code()} - ${response.message()}")
                throw Exception("이미지 업로드에 실패했습니다: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("MungstarPostRepository", "이미지 업로드 중 오류", e)
            throw e
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("파일을 읽을 수 없습니다.")

        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(tempFile)

        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }
}