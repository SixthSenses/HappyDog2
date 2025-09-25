package com.example.pet_project_frontend.presentation.eye_health

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * 이미지를 전체화면으로 보여주는 뷰어 화면
 */
@Composable
fun ImageViewerScreen(
    imageUrl: String,
    onCloseClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onCloseClick() }, // 배경 클릭 시 닫기
        contentAlignment = Alignment.Center
    ) {
        // 이미지
        AsyncImage(
            model = imageUrl,
            contentDescription = "안구 분석 이미지",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        
        // 닫기 버튼 (우상단)
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}