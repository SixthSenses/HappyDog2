@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pet_project_frontend.presentation.mypage.settings.legal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pet_project_frontend.core.components.TopBar

@Composable
fun TermsOfServiceScreen(onBack: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            title = "이용약관",
            onNavigateBack = onBack
        )
        Text(
            text = "이용약관 내용이 여기에 표시됩니다. (임시 화면)",
            modifier = Modifier.padding(16.dp)
        )
    }
}
