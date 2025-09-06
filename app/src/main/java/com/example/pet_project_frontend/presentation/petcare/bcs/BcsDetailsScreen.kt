@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.example.pet_project_frontend.presentation.petcare.bcs

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * BCS 상세 화면 (1..5 단계 캐러셀)
 * - 이미지 리소스는 임시로 외부에서 주입합니다. (docs/image/bcs1~5.png를 추후 res/drawable로 이관 예정)
 * - 이미지가 없을 경우 텍스트 플레이스홀더를 표시합니다.
 */
@Composable
fun BcsDetailsScreen(
    modifier: Modifier = Modifier,
    selected: Int = 3,
    images: List<Painter?> = emptyList(),
    labels: List<String> = listOf("매우 마름", "마름", "정상", "과체중", "비만"),
) {
    val pageCount = remember(images, labels) { 5 }
    var index by remember(selected) { mutableIntStateOf((selected - 1).coerceIn(0, pageCount - 1)) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "체지방상태지수(BCS)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        val painter = images.getOrNull(index)
        val cd = "BCS ${index + 1} - ${labels.getOrNull(index) ?: ""}"
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(index) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (dragAmount > 10) {
                            // 오른쪽으로 드래그: 이전
                            index = (index - 1).coerceAtLeast(0)
                        } else if (dragAmount < -10) {
                            // 왼쪽으로 드래그: 다음
                            index = (index + 1).coerceAtMost(pageCount - 1)
                        }
                    }
                }
                .semantics { contentDescription = cd },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (painter != null) {
                Image(painter = painter, contentDescription = cd)
            } else {
                Text(text = "BCS ${index + 1}", fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            Text(text = labels.getOrNull(index) ?: "", style = MaterialTheme.typography.bodyLarge)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { index = (index - 1).coerceAtLeast(0) }, enabled = index > 0) { Text("이전") }
            Button(onClick = { index = (index + 1).coerceAtMost(pageCount - 1) }, enabled = index < pageCount - 1) { Text("다음") }
        }
    }
}
