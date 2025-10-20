package com.example.pet_project_frontend.presentation.mypage.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.core.theme.PretendardFont
import com.example.pet_project_frontend.presentation.mypage.main.MyPageViewModel

@Composable
fun EditGenderScreen(
    onDismiss: () -> Unit,
    viewModel: MyPageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedGender by remember { mutableStateOf(uiState.gender) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // 검은색 배경 (32% 불투명도)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(
                        onClick = { onDismiss() },
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    )
            )

            // 다이얼로그 박스
            Box(
                modifier = Modifier
                    .width(336.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(26.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 제목
                    Text(
                        text = "성별 선택",
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(600),
                        fontSize = 21.sp,
                        color = Color(0xFF333D4B)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 성별 선택 옵션
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 수컷 옵션
                        GenderOption(
                            label = "수컷",
                            isSelected = selectedGender == "수컷",
                            onClick = { selectedGender = "수컷" }
                        )

                        // 암컷 옵션
                        GenderOption(
                            label = "암컷",
                            isSelected = selectedGender == "암컷",
                            onClick = { selectedGender = "암컷" }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 버튼들
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 취소 버튼
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .background(
                                    color = Color(0xFFF3F4F6),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "취소",
                                fontFamily = PretendardFont,
                                fontWeight = FontWeight(600),
                                fontSize = 18.sp,
                                color = Color(0xFF4E5968)
                            )
                        }

                        // 저장 버튼
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .background(
                                    color = Color(0xFF3182F6),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    viewModel.updateGender(selectedGender)
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "저장",
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
}

@Composable
private fun GenderOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = if (isSelected) Color(0xFFE8F3FF) else Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF3182F6) else Color(0xFFE5E8EB),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 라디오 버튼
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(
                    width = 2.dp,
                    color = if (isSelected) Color(0xFF3182F6) else Color(0xFFD1D5DB),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = Color(0xFF3182F6),
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 라벨
        Text(
            text = label,
            fontFamily = PretendardFont,
            fontWeight = FontWeight(500),
            fontSize = 16.sp,
            color = if (isSelected) Color(0xFF333D4B) else Color(0xFF6B7684)
        )
    }
}
