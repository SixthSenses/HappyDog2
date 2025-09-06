package com.example.pet_project_frontend

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.Intent
import android.content.IntentFilter
import android.content.Context

@HiltAndroidApp
class PetCareApplication : Application() {
	override fun onCreate() {
		super.onCreate()
		// Remote Config 초기화: 기본값 설정 후 비동기 fetch & activate
		val rc = FirebaseRemoteConfig.getInstance()
		rc.setDefaultsAsync(
			mapOf(
				"petcare_cards_order" to "[\"water\",\"activity\",\"meal\",\"weight\",\"bcs\"]",
				"petcare_cards_hidden" to "[]"
			)
		)
		CoroutineScope(Dispatchers.IO).launch {
			runCatching { rc.fetchAndActivate().result }
				.onSuccess {
					// RC 적용 시 브로드캐스트로 알림
					sendBroadcast(Intent(RC_APPLIED_ACTION))
				}
		}
	}

	companion object {
		const val RC_APPLIED_ACTION = "com.example.pet_project_frontend.RC_APPLIED"
	}
}
