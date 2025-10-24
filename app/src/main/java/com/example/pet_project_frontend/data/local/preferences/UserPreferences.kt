// 변경의도: 알림 설정 값을 DataStore에 영속화해 화면 토글과 동기화하도록 키와 Flow, 저장 함수를 정비한다.
package com.example.pet_project_frontend.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    data class NotificationSettings(
        val pushEnabled: Boolean = true,
        val weeklyReport: Boolean = true,
        val likeEnabled: Boolean = false,
        val commentEnabled: Boolean = false
    )

    private object Keys {
        val PUSH_ENABLED = booleanPreferencesKey("push_enabled")
        val WEEKLY_REPORT = booleanPreferencesKey("weekly_report_enabled")
        val LIKE_ENABLED = booleanPreferencesKey("community_like_enabled")
        val COMMENT_ENABLED = booleanPreferencesKey("community_comment_enabled")
    }

    val notificationSettings: Flow<NotificationSettings> = dataStore.data.map { prefs ->
        NotificationSettings(
            pushEnabled = prefs[Keys.PUSH_ENABLED] ?: true,
            weeklyReport = prefs[Keys.WEEKLY_REPORT] ?: true,
            likeEnabled = prefs[Keys.LIKE_ENABLED] ?: false,
            commentEnabled = prefs[Keys.COMMENT_ENABLED] ?: false
        )
    }

    suspend fun setPushEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.PUSH_ENABLED] = enabled
        }
    }

    suspend fun setWeeklyReport(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.WEEKLY_REPORT] = enabled
        }
    }

    suspend fun setLikeEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.LIKE_ENABLED] = enabled
        }
    }

    suspend fun setCommentEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.COMMENT_ENABLED] = enabled
        }
    }
}
