package com.example.pet_project_frontend.presentation.mypage.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.pet_project_frontend.R

@Composable
fun CommonAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    title: String,
    text: String,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    // Wireframe dimensions
    val wireframeWidth = 412.dp
    val wireframeHeight = 917.dp

    // Calculate ratios for the frame itself
    val frameWidthRatio = 336f / 412f
    val frameHeightRatio = 173f / 917f

    // Calculate dimensions based on ratios
    val frameWidth = screenWidth * frameWidthRatio
    val frameHeight = screenHeight * frameHeightRatio

    // Calculate padding and sizes for elements inside the frame
    val headerTopPadding = frameHeight * (27.5f / 173f)
    val contentHorizontalPadding = frameWidth * (23f / 336f)
    val bodyTopPadding = frameHeight * (13.5f / 173f)
    val buttonBottomPadding = frameHeight * (17f / 173f)

    val buttonWidth = frameWidth * (302f / 336f)
    val buttonHeight = frameHeight * (50f / 173f)

    val pretendard = FontFamily(
        Font(R.font.pretendard_400, FontWeight.W400),
        Font(R.font.pretendard_500, FontWeight.W500),
        Font(R.font.pretendard_600, FontWeight.W600),
        Font(R.font.pretendard_bold, FontWeight.W700)
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(frameWidth)
                .height(frameHeight),
            shape = RoundedCornerShape(26.dp),
            color = Color.White
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = headerTopPadding)
                        .padding(horizontal = contentHorizontalPadding)
                ) {
                    Text(
                        text = title,
                        style = TextStyle(
                            fontSize = 21.sp,
                            lineHeight = 21.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(600),
                            color = Color(0xFF333D4B),
                        )
                    )

                    Spacer(modifier = Modifier.height(bodyTopPadding))

                    Text(
                        text = text,
                        style = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 23.2.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(500),
                            color = Color(0xFF6B7684),
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = buttonBottomPadding)
                        .width(buttonWidth)
                        .height(buttonHeight)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE4E7EA))
                        .clickable { onConfirmation() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "확인",
                        style = TextStyle(
                            fontSize = 18.sp,
                            lineHeight = 18.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(600),
                            color = Color(0xFF4E5968),
                            textAlign = TextAlign.Center,
                        )
                    )
                }
            }
        }
    }
}
