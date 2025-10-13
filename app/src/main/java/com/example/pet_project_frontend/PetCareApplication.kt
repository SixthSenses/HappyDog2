package com.example.pet_project_frontend

import android.app.Application
import coil.Coil
import coil.ImageLoader
import com.example.pet_project_frontend.util.FirebaseStorageInterceptor
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PetCareApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Coil ImageLoader 설정
        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(FirebaseStorageInterceptor())
            }
            .build()
        
        Coil.setImageLoader(imageLoader)
    }
}
