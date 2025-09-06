// app/src/main/java/com/example/pet_project_frontend/core/di/RepositoryModule.kt

package com.example.pet_project_frontend.core.di

import com.example.pet_project_frontend.data.repository.AuthRepositoryImpl
import com.example.pet_project_frontend.data.repository.BreedRepositoryImpl
import com.example.pet_project_frontend.data.repository.MapRepositoryImpl
import com.example.pet_project_frontend.data.repository.PetCareRepositoryImpl
import com.example.pet_project_frontend.data.repository.PetRepositoryImpl
import com.example.pet_project_frontend.data.repository.PostRepositoryImpl
import com.example.pet_project_frontend.data.repository.UserRepositoryImpl
import com.example.pet_project_frontend.domain.repository.AuthRepository
import com.example.pet_project_frontend.domain.repository.BreedRepository
import com.example.pet_project_frontend.domain.repository.MapRepository
import com.example.pet_project_frontend.domain.repository.PetCareRepository
import com.example.pet_project_frontend.domain.repository.PetRepository
import com.example.pet_project_frontend.domain.repository.PostRepository
import com.example.pet_project_frontend.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.pet_project_frontend.core.remoteconfig.FeatureToggles
import com.example.pet_project_frontend.core.remoteconfig.RemoteConfigToggles
import dagger.Provides
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
    
    @Binds
    @Singleton
    abstract fun bindPetRepository(
        petRepositoryImpl: PetRepositoryImpl
    ): PetRepository
    
    @Binds
    @Singleton
    abstract fun bindPetCareRepository(
        petCareRepositoryImpl: PetCareRepositoryImpl
    ): PetCareRepository
    
    @Binds
    @Singleton
    abstract fun bindBreedRepository(
        breedRepositoryImpl: BreedRepositoryImpl
    ): BreedRepository
    
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
    
    @Binds
    @Singleton
    abstract fun bindMapRepository(
        mapRepositoryImpl: MapRepositoryImpl
    ): MapRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        postRepositoryImpl: PostRepositoryImpl
    ): PostRepository
}

@Module
@InstallIn(SingletonComponent::class)
object RemoteConfigModule {
    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    @Provides
    @Singleton
    fun provideFeatureToggles(rc: FirebaseRemoteConfig): FeatureToggles = RemoteConfigToggles(rc)
}