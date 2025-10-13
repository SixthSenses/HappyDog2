@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.pet_project_frontend.presentation.mypage.withdrawal

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.components.TopBar
import com.example.pet_project_frontend.presentation.mypage.common.CommonAlertDialog

@Composable
fun WithdrawalScreen(
    onBack: () -> Unit,
    onFinished: () -> Unit = {},
    viewModel: WithdrawalViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val topMargin = screenHeight * (10f / 917f)
    val headerBottomMargin = screenHeight * (20f / 917f)
    val bulletMargin = screenHeight * (11f / 917f)

    Scaffold(
        topBar = { TopBar(title = {}, onNavigateBack = onBack) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Spacer(Modifier.height(topMargin))

                    Text(
                        text = "탈퇴하기",
                        style = headerStyle(),
                        color = Color(0xFF191F28)
                    )

                    Spacer(Modifier.height(headerBottomMargin))

                    BulletText("회원 탈퇴는 행복하개의 모든 사용자가 진행할 수\n있어요.")
                    Spacer(Modifier.height(bulletMargin))
                    BulletText("회원 탈퇴 시, 사용자의 개인정보는 법령에서 정한\n기간동안 보관이 요구되는 정보를 제외하고 모두\n파기돼요. 따라서 행복하개에서 관리했던 사용자의\n모든 개인정보를 다시 볼 수 없어요.")

                    if (!ui.errorMessage.isNullOrEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = ui.errorMessage ?: "",
                            color = Color(0xFFE42A38),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = { showConfirmDialog = true },
                        enabled = !ui.isProcessing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (ui.isProcessing) Color(0xFFE42A38).copy(alpha = 0.4f) else Color(0xFFE42A38),
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (ui.isProcessing) "탈퇴 진행 중..." else "탈퇴하기", style = ctaTextStyle())
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "돌아가기",
                        style = backTextStyle(),
                        color = Color(0xFF4E5968),
                        modifier = Modifier.clickable { onBack() }
                    )

                    Spacer(Modifier.height(16.dp))
                }
            }

            if (showConfirmDialog) {
                CommonAlertDialog(
                    onDismissRequest = { showConfirmDialog = false },
                    onConfirmation = {
                        showConfirmDialog = false
                        viewModel.onClickWithdraw()
                    },
                    title = "회원 탈퇴",
                    text = "탈퇴 시 모든 정보가 삭제되며, 복구할 수 없습니다. 정말로 탈퇴하시겠습니까?"
                )
            }

            if (ui.showCompleted) {
                CompletionDialog(
                    onConfirm = {
                        viewModel.onDismissCompleted()
                        onFinished()
                    }
                )
            }
        }
    }
}

@Composable
private fun BulletText(text: String) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Text("•", color = Color(0xFF4E5968), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color(0xFF4E5968), style = bulletTextStyle())
    }
}

@Composable
private fun CompletionDialog(onConfirm: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.32f))
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .width(320.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("탈퇴 완료", style = dialogTitleStyle(), color = Color(0xFF191F28))
                    Text("탈퇴 처리가 성공적으로 완료되었습니다.", style = dialogBodyStyle(), color = Color(0xFF4E5968))
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE4E7EA),
                        contentColor = Color(0xFF333D4B)
                    )
                ) {
                    Text("확인", style = confirmTextStyle())
                }
            }
        }
    }
}

@Composable
private fun headerStyle(): TextStyle =
    TextStyle(
        fontSize = 26.sp,
        lineHeight = 36.4.sp,
        fontFamily = FontFamily(Font(R.font.pretendard_medium)),
        fontWeight = FontWeight(600),
        color = Color(0xFF191F28)
    )

@Composable
private fun bulletTextStyle(): TextStyle =
    TextStyle(
        fontSize = 15.sp,
        lineHeight = 23.2.sp,
        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
        fontWeight = FontWeight(300),
        color = Color(0xFF4E5968)
    )

@Composable
private fun ctaTextStyle(): TextStyle =
    TextStyle(
        fontSize = 18.sp,
        lineHeight = 18.sp,
        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
        fontWeight = FontWeight(600)
    )

@Composable
private fun backTextStyle(): TextStyle =
    TextStyle(
        fontSize = 18.sp,
        lineHeight = 18.sp,
        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
        fontWeight = FontWeight(600),
        color = Color(0xFF4E5968)
    )

@Composable
private fun dialogTitleStyle(): TextStyle =
    MaterialTheme.typography.titleMedium.copy(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold
    )

@Composable
private fun dialogBodyStyle(): TextStyle =
    MaterialTheme.typography.bodyMedium.copy(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal
    )

@Composable
private fun confirmTextStyle(): TextStyle =
    MaterialTheme.typography.titleSmall.copy(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )
