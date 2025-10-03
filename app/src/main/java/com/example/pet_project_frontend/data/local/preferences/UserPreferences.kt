package com.example.pet_project_frontend.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// DataStore에 보관된 알림 설정을 다루는 헬퍼
@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    data class NotificationSettings(
        val weeklyReport: Boolean = true,
        val likeEnabled: Boolean = false,
        val commentEnabled: Boolean = false
    )

    private object Keys {
        val WEEKLY_REPORT = booleanPreferencesKey("weekly_report_enabled")
        val LIKE_ENABLED = booleanPreferencesKey("community_like_enabled")
        val COMMENT_ENABLED = booleanPreferencesKey("community_comment_enabled")
    }

    val notificationSettings: Flow<NotificationSettings> = dataStore.data.map { prefs ->
        NotificationSettings(
            weeklyReport = prefs[Keys.WEEKLY_REPORT] ?: true,
            likeEnabled = prefs[Keys.LIKE_ENABLED] ?: false,
            commentEnabled = prefs[Keys.COMMENT_ENABLED] ?: false
        )
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
