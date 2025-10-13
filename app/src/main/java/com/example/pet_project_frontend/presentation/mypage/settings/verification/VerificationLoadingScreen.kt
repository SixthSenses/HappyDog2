package com.example.pet_project_frontend.presentation.mypage.settings.verification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.core.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationLoadingScreen(
    petId: String,
    viewModel: IdentityVerificationViewModel,
    onResult: (VerificationResult) -> Unit,
    onBack: () -> Unit = {}
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    val inactive = Color(0xFFA5AEB8)
    val active = Color(0xFF8B95A1)
    val activeDot = uiState.progressStep.coerceIn(0, 2)

    LaunchedEffect(petId) {
        viewModel.submitVerification(petId)
    }

    LaunchedEffect(uiState.isUploading, uiState.isVerifying, uiState.verificationResult) {
        if (!uiState.isUploading && !uiState.isVerifying && uiState.verificationResult != VerificationResult.Idle) {
            onResult(uiState.verificationResult)
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = { Text(text = "") },
                onNavigateBack = onBack
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(329.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 120.dp)
                    .height(458.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .width(52.dp)
                        .height(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Dot(isActive = activeDot >= 0, active = active, inactive = inactive)
                    Dot(isActive = activeDot >= 1, active = active, inactive = inactive)
                    Dot(isActive = activeDot >= 2, active = active, inactive = inactive)
                }
            }

            Spacer(Modifier.height(46.dp))

            Text(
                text = when (uiState.progressStep) {
                    0 -> "비문 이미지를 준비하고 있어요."
                    1 -> "비문을 분석하고 있어요."
                    else -> "분석 결과를 정리하고 있어요."
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                color = Color(0xFF191F28),
                textAlign = TextAlign.Center,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 36.4.sp,
                letterSpacing = (-0.7).sp
            )

            if (uiState.errorMessage != null) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = uiState.errorMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    color = Color(0xFFD14343),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun Dot(
    isActive: Boolean,
    active: Color,
    inactive: Color
) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(if (isActive) active else inactive)
    )
}
