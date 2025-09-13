package com.example.pet_project_frontend.data.mungstar_model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.pet_project_frontend.BuildConfig
import java.util.concurrent.TimeUnit

class PostRepository {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    suspend fun createPost(text: String, filePaths: List<String>, token: String): Result<PostResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CreatePostRequest(text = text, file_paths = filePaths)
                val response = apiService.createPost("Token $token", request)

                if (response.isSuccessful) {
                    response.body()?.let { postResponse ->
                        Result.success(postResponse)
                    } ?: Result.failure(Exception("Response body is null"))
                } else {
                    Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}