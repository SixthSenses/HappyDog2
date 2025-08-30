package com.example.pet_project_frontend.core.di

import com.example.pet_project_frontend.BuildConfig
import com.example.pet_project_frontend.data.remote.api.AuthApi
import com.example.pet_project_frontend.data.remote.api.BreedApi
import com.example.pet_project_frontend.data.remote.api.PetApi
import com.example.pet_project_frontend.data.remote.api.PetCareApi
import com.example.pet_project_frontend.data.remote.interceptors.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: com.example.pet_project_frontend.data.local.preferences.TokenManager): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }
    
    @Provides
    @Singleton
    fun providePetApi(retrofit: Retrofit): PetApi {
        return retrofit.create(PetApi::class.java)
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
}