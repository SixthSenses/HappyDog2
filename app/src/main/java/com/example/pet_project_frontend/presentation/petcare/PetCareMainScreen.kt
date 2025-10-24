package com.example.pet_project_frontend.presentation.petcare

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// 다른 탭 화면 완성되면 파일 지우기
@Composable
fun PetCareMainScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "펫케어 메인 화면 (임시)")
    }
}

