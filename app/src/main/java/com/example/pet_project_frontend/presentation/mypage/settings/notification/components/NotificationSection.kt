package com.example.pet_project_frontend.presentation.mypage.settings.notification.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pet_project_frontend.core.theme.MyPageColors

@Composable
fun NotificationSection(
    title: String,
    description: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MyPageColors.CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MyPageColors.Primary)
            if (description != null) {
                Spacer(Modifier.height(6.dp))
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MyPageColors.Tertiary)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
