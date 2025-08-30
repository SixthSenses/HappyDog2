package com.example.pet_project_frontend.core.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/**
 * 공통 상단바
 *
 * 디자인 스펙(요약):
 * - 높이: Material3 기본(64dp)
 * - 배경: #FFFFFF
 * - 타이틀: 24sp, weight SemiBold, 색상 #191F28
 * - 아이콘: #1D1B20
 *
 * 스크롤 동작이 필요하면 scrollBehavior를 넘겨주세요.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = Color.White,
    titleColor: Color = Color(0xFF191F28),
    iconColor: Color = Color(0xFF1D1B20),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = iconColor
                    )
                }
            }
        },
        title = {
            Text(
                text = title,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 20.sp,        // Compose M3 TopAppBar 기본 대비 살짝 키우고 싶다면 24.sp로
                fontWeight = FontWeight.SemiBold
            )
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = titleColor,
            navigationIconContentColor = iconColor,
            actionIconContentColor = iconColor
        ),
        scrollBehavior = scrollBehavior
    )
}
@OptIn(ExperimentalMaterial3Api::class)
// ✅ 추가: 제목을 Composable 슬롯으로 받는 오버로드
@Composable
fun TopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = Color.White,
    iconColor: Color = Color(0xFF1D1B20),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = title, // 그대로 전달
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = iconColor
                )

                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = containerColor
        ),
        scrollBehavior = scrollBehavior
    )
}

/**
 * 이름 설정 화면 등에서 바로 쓰기 좋은 helper.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopBar(
        title = title,
        onNavigateBack = onBack,
        actions = actions,
        modifier = modifier,
        scrollBehavior = scrollBehavior
    )
}

/**
 * 스크롤 시 상단바 컬러 구분이 필요하면 아래 scrollBehavior 사용:
 *
 * val behavior = TopAppBarDefaults.pinnedScrollBehavior()
 * Scaffold(
 *   modifier = Modifier.nestedScroll(behavior.nestedScrollConnection),
 *   topBar = { TopBar(title = "이름 설정", onNavigateBack = onBack, scrollBehavior = behavior) }
 * ) { ... }
 *
 * 라이트/다크 전환은 containerColor/titleColor/iconColor를 theme에 맞춰 주입하면 됩니다.
 */

