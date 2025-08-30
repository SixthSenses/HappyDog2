package com.example.pet_project_frontend.presentation.mypage.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pet_project_frontend.core.theme.MyPageColors

/**
 * 1-1. 미디어 피커 바텀시트
 * - 시스템 Photo Picker 호출 (이미지 전용)
 * - 디자인은 와이어 기준으로 헤더/툴바/그리드 골격만 구현
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MediaPickerSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    onPicked: (Uri) -> Unit
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onPicked(it) }
    }

    val placeholders = remember { List(12) { "placeholder-$it" } }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = null,
        scrimColor = MyPageColors.Scrim
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(width = 24.dp, height = 4.dp)
                    .background(Color(0xFFF3DDE0))
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "This app can only access the media you select",
                color = Color(0xFF22191B)
            )
            // Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, contentDescription = "닫기")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = {
                        launcher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) { Text("Photos") }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = {
                        launcher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) { Text("Albums") }
                }
                IconButton(onClick = { /* menu placeholder */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "메뉴")
                }
            }
        }

        Divider()

        // Recent + Grid (더미 그리드 / 아무 타일이나 눌러도 시스템 피커 실행)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 400.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Recent",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                color = Color(0xFF22191B)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),     // ← cells → columns 로 변경
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(placeholders) { _ ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .background(Color(0xFFFFF8F7))
                            .clickable {
                                launcher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) { Text("image", color = Color(0xFF514346)) }
                }
            }
            }
        }
}
