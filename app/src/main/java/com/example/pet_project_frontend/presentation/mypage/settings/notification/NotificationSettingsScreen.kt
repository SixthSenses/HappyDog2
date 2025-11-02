@file:OptIn(ExperimentalMaterial3Api::class)

// 변경의도: 전체 푸시 토글과 세부 항목을 DataStore 상태와 연동하도록 UI 구성을 보완한다.
package com.example.pet_project_frontend.presentation.mypage.settings.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val switchesEnabled = !uiState.loading
    val detailSwitchEnabled = switchesEnabled && uiState.pushEnabled

    Scaffold(
        topBar = {
            TopBar(
                title = {
                    Text(
                        text = "알림",
                        style = appBarTitleStyle(),
                        color = Gray900
                    )
                },
                onNavigateBack = onBack
            )
        }
    ) { inner ->
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(inner)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(10.dp))

                Text(
                    text = "전체 알림",
                    fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                    fontWeight = FontWeight(600),
                    fontSize = 21.sp,
                    lineHeight = 21.sp,
                    color = Gray900
                )
                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "푸시 알림",
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                        fontWeight = FontWeight(600),
                        fontSize = 18.sp,
                        lineHeight = 18.sp,
                        color = Gray700
                    )
                    Switch(
                        checked = uiState.pushEnabled,
                        onCheckedChange = { viewModel.onTogglePush(it) },
                        enabled = switchesEnabled,
                        colors = noBorderSwitchColors()
                    )
                }

                Spacer(Modifier.height(15.dp))

                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    color = DividerColor,
                    thickness = 1.dp
                )

                Spacer(Modifier.height(15.dp))

                Text(
                    text = "멍스타그램",
                    fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                    fontWeight = FontWeight(600),
                    fontSize = 21.sp,
                    lineHeight = 21.sp,
                    style = sectionTitleStyle(),
                    color = Gray900
                )
                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "좋아요",
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                        fontWeight = FontWeight(600),
                        fontSize = 18.sp,
                        lineHeight = 18.sp,
                        style = itemLabelStyle(),
                        color = Gray700
                    )
                    Switch(
                        checked = uiState.likeEnabled,
                        onCheckedChange = { viewModel.onToggleLike(it) },
                        enabled = detailSwitchEnabled,
                        colors = noBorderSwitchColors()
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "댓글",
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                        fontWeight = FontWeight(600),
                        fontSize = 18.sp,
                        lineHeight = 18.sp,
                        color = Gray700
                    )
                    Switch(
                        checked = uiState.commentEnabled,
                        onCheckedChange = { viewModel.onToggleComment(it) },
                        enabled = detailSwitchEnabled,
                        colors = noBorderSwitchColors()
                    )
                }

                Spacer(Modifier.height(43.dp))
            }

            if (uiState.loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    color = Blue
                )
            }
        }
    }
}

/* ======================= Text 스타일 ======================= */
@Composable
private fun appBarTitleStyle(): TextStyle =
    MaterialTheme.typography.bodyLarge.copy(
        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 18.sp,
        letterSpacing = (-0.45).sp
    )

@Composable
private fun sectionTitleStyle(): TextStyle =
    MaterialTheme.typography.titleMedium.copy(
        fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
        fontSize = 21.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 21.sp
    )

@Composable
private fun itemLabelStyle(): TextStyle =
    MaterialTheme.typography.bodyLarge.copy(
        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 18.sp,
        letterSpacing = (-0.45).sp
    )

/* ======================= Switch 컬러 ======================= */
@Composable
private fun noBorderSwitchColors() = SwitchDefaults.colors(
    checkedTrackColor = Blue,
    checkedThumbColor = Color.White,
    uncheckedTrackColor = Gray300,
    uncheckedThumbColor = Color.White,
    checkedBorderColor = Color.Transparent,
    uncheckedBorderColor = Color.Transparent,
    disabledCheckedBorderColor = Color.Transparent,
    disabledUncheckedBorderColor = Color.Transparent
)

/* ======================= 색상 값 ======================= */
private val Gray900 = Color(0xFF333D4B)
private val Gray700 = Color(0xFF4E5968)
private val Gray300 = Color(0xFFD1D6DA)
private val DividerColor = Color(0xFFF2F4F6)
private val Blue = Color(0xFF3182F6)
