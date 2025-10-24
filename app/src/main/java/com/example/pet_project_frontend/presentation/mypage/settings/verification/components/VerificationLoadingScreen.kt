package com.example.pet_project_frontend.presentation.mypage.settings.verification.components

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.PretendardFont
import com.example.pet_project_frontend.presentation.mypage.settings.verification.IdentityVerificationViewModel
import kotlinx.coroutines.delay

/**
 * Verification loading screen that mirrors the cartoon loading UX while the nose print is processed.
 */
@Composable
fun VerificationLoadingScreen(
    viewModel: IdentityVerificationViewModel,
    petId: String,
    onResult: (VerificationResult) -> Unit
) {
    val uiState by viewModel.processingUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var activeLoadingDot by remember { mutableStateOf(0) }
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(petId) {
        viewModel.startVerification(petId)
    }

    LaunchedEffect(uiState.isLoading) {
        if (uiState.isLoading) {
            while (true) {
                delay(300)
                activeLoadingDot = (activeLoadingDot + 1) % 3
            }
        }
    }

    LaunchedEffect(uiState.result) {
        uiState.result?.let {
            onResult(it)
            viewModel.consumeProcessingResult()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState.isCancelled) {
        if (uiState.isCancelled) {
            viewModel.onCancelCompleted()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color.White)
                .align(Alignment.TopCenter)
        ) {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "뒤로가기",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = 16.dp)
                    .clickable { showCancelDialog = true }
            )
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 46.dp)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = if (activeLoadingDot == index) {
                                    Color(0xFF8B95A1)
                                } else {
                                    Color(0xFFE2E5E8)
                                },
                                shape = CircleShape
                            )
                    )
                }
            }

            Text(
                text = "비문 사진을\n분석하고 있어요",
                fontFamily = PretendardFont,
                fontWeight = FontWeight(600),
                fontSize = 28.sp,
                color = Color(0xFF191F28),
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )
        }

        if (showCancelDialog) {
            Dialog(
                onDismissRequest = { showCancelDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { showCancelDialog = false },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .width(336.dp)
                            .height(144.dp)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(26.dp)
                            )
                            .clickable(enabled = false) { }
                    ) {
                        Text(
                            text = "인증을 중단할까요?",
                            fontFamily = PretendardFont,
                            fontWeight = FontWeight(600),
                            fontSize = 21.sp,
                            color = Color(0xFF333D4B),
                            modifier = Modifier.padding(start = 17.dp, top = 17.dp)
                        )

                        Spacer(modifier = Modifier.height(21.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 17.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp)
                                    .background(
                                        color = Color(0xFFF3F4F6),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { showCancelDialog = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "아니요",
                                    fontFamily = PretendardFont,
                                    fontWeight = FontWeight(600),
                                    fontSize = 18.sp,
                                    color = Color(0xFF4E5968)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp)
                                    .background(
                                        color = Color(0xFFEC4453),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        showCancelDialog = false
                                        viewModel.cancelVerification()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "중단하기",
                                    fontFamily = PretendardFont,
                                    fontWeight = FontWeight(600),
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        if (uiState.isLoading) {
            Spacer(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .height(1.dp)
            )
        }
    }
}
