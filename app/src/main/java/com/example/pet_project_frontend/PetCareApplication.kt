package com.example.pet_project_frontend

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class PetCareApplication : Application(), ImageLoaderFactory {
    
    @Inject
    lateinit var okHttpClient: OkHttpClient
    
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .crossfade(true)
            .logger(DebugLogger()) // 디버그용 로거 추가
            .build()
    }
}
