package com.example.pet_project_frontend.presentation.mypage.settings.verification

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.components.TopBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationGuideScreen(
    onBack: () -> Unit,
    onPickImage: (String) -> Unit,
    onOpenCamera: () -> Unit,
    errorDialog: VerificationGuideError? = null,
    onDismissError: () -> Unit = {}
) {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onPickImage(it.toString()) }
    }

    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TopBar(
            title = { Text(text = "") },
            onNavigateBack = onBack
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(647.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "카메라 미리보기 영역",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F1115), Color(0xFF060709))
                    )
                )
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 46.dp, start = 32.dp, end = 32.dp),
                text = "반려견의 비문(코)을 근접 촬영해 주세요",
                color = Color(0xFFB1B8C0),
                textAlign = TextAlign.Center,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.34).sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 112.dp)
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .size(width = 42.dp, height = 57.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF777777), Color(0xFF444444))
                                )
                            )
                            .border(
                                BorderStroke(1.dp, Color(0x33888B90)),
                                RoundedCornerShape(6.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoLibrary,
                            contentDescription = "앨범",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(22.dp)
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "앨범",
                        color = Color(0xFFB1B8C0),
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(4.dp, Color(0xFF888B90), CircleShape)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }

                Box(
                    modifier = Modifier
                        .offset(x = (-58).dp)
                        .size(58.dp)
                        .noRippleClickable { onOpenCamera() }
                )

                var flashOn by remember { mutableStateOf(false) }
                IconButton(
                    onClick = { flashOn = !flashOn },
                    modifier = Modifier.size(width = 16.dp, height = 26.667.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FlashOn,
                        contentDescription = "플래시",
                        tint = if (flashOn) Color(0xFFFFD54F) else Color.White
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(start = 63.dp, top = 112.dp)
                    .size(width = 42.dp, height = 57.dp)
                    .noRippleClickable { galleryLauncher.launch("image/*") }
            )
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            containerColor = Color.White,
            dragHandle = {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(13.dp))
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFCED3D8))
                )
                Spacer(Modifier.height(30.dp))

                Text(
                    text = "비문 촬영 안내",
                    color = Color(0xFF191F28),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 21.sp
                )

                Spacer(Modifier.height(19.dp))

                Text(
                    text = "반려견의 코를 가까이에서 정면으로 촬영해 주세요.\n빛 반사가 적도록 자연광에서 촬영하고,\n비문 전체가 선명하게 나오도록 해주세요.",
                    color = Color(0xFF4E5968),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 26.1.sp,
                    letterSpacing = (-0.18).sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(35.dp))

                Button(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            showSheet = false
                        }
                    },
                    modifier = Modifier
                        .width(348.dp)
                        .height(58.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFD8800)),
                    contentPadding = PaddingValues()
                ) {
                    Text(
                        text = "확인",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    when (errorDialog) {
        VerificationGuideError.Duplicate ->
            VerificationGuideDuplicateErrorDialog(onConfirm = onDismissError)
        VerificationGuideError.DetectionFailed ->
            VerificationGuideDetectionErrorDialog(onConfirm = onDismissError)
        null -> Unit
    }
}

@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
