package com.example.pet_project_frontend.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import com.example.pet_project_frontend.BuildConfig // BuildConfig import

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

	@Provides
	@Named("NATIVE_APP_KEY")
	fun provideNativeAppKey(): String {
		// 이제 코드 어디에도 실제 키가 보이지 않습니다.
		return BuildConfig.KAKAO_NATIVE_APP_KEY
	}
}