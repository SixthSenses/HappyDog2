// app/src/main/java/com/example/pet_project_frontend/core/di/NetworkModule.kt

package com.example.pet_project_frontend.core.di

import com.example.pet_project_frontend.data.remote.api.*
import com.example.pet_project_frontend.data.remote.authenticator.TokenAuthenticator
import com.example.pet_project_frontend.data.remote.interceptors.AuthInterceptor
import com.example.pet_project_frontend.data.remote.interceptors.ErrorInterceptor
import com.example.pet_project_frontend.data.remote.interceptors.IdempotencyInterceptor
import com.example.pet_project_frontend.data.remote.interceptors.ProtectedErrorInterceptor
import com.example.pet_project_frontend.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.HttpUrl.Companion.toHttpUrl

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("BASE_URL")
    fun provideBaseUrl(): String = BuildConfig.API_BASE_URL

    @Provides
    @Singleton
    @Named("API_BASE_HOST")
    fun provideApiBaseHost(@Named("BASE_URL") baseUrl: String): String {
        return try {
            val httpUrl = baseUrl.toHttpUrl()
            httpUrl.host
        } catch (e: IllegalArgumentException) {
            // fallback: strip scheme if any and take host-like token
            baseUrl.replace(Regex("^https?://"),"")
                .trimEnd('/')
                .substringBefore('/')
        }
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    @Named("AuthRetrofit")
    fun provideAuthRetrofit(
        @Named("BASE_URL") baseUrl: String,
        httpLoggingInterceptor: HttpLoggingInterceptor,
        idempotencyInterceptor: IdempotencyInterceptor
    ): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(idempotencyInterceptor)
            .addInterceptor(ErrorInterceptor())
            .connectTimeout(300, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        protectedErrorInterceptor: ProtectedErrorInterceptor,
        idempotencyInterceptor: IdempotencyInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(idempotencyInterceptor)
            .addInterceptor(ErrorInterceptor())
            .addInterceptor(protectedErrorInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(300, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        @Named("BASE_URL") baseUrl: String,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ===== API 인터페이스 제공 =====
    
    @Provides
    @Singleton
    fun provideAuthApi(@Named("AuthRetrofit") retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun providePetApi(retrofit: Retrofit): PetApi {
        return retrofit.create(PetApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    @Provides
    @Singleton
    fun providePetCareApi(retrofit: Retrofit): PetCareApi {
        return retrofit.create(PetCareApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBreedApi(retrofit: Retrofit): BreedApi {
        return retrofit.create(BreedApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUploadApi(retrofit: Retrofit): UploadApi {
        return retrofit.create(UploadApi::class.java)
    }

    @Provides
    @Singleton
    fun providePostApi(retrofit: Retrofit): PostApi {
        return retrofit.create(PostApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCommentApi(retrofit: Retrofit): CommentApi {
        return retrofit.create(CommentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCartoonJobApi(retrofit: Retrofit): CartoonJobApi {
        return retrofit.create(CartoonJobApi::class.java)
    }

    @Provides
    @Singleton
    fun provideEyeHealthApi(retrofit: Retrofit): EyeHealthApi {
        return retrofit.create(EyeHealthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi {
        return retrofit.create(NotificationApi::class.java)
    }
}