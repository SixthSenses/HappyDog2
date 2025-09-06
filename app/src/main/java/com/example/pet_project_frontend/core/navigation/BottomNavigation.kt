package com.example.pet_project_frontend.core.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet_project_frontend.core.theme.MyPageColors

// 하단바 아이템을 위한 데이터 클래스
data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

@Composable
fun BottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit = {}
) {
    // 하단바에 표시할 아이템 리스트를 만듭니다.
    val bottomNavItems = listOf(
        BottomNavItem(Screen.PetCare, Icons.Default.Favorite, "펫케어"),
        BottomNavItem(Screen.Map, Icons.Default.LocationOn, "지도"),
        BottomNavItem(Screen.Community, Icons.Default.Star, "멍스타그램"),
        BottomNavItem(Screen.Translator, Icons.Default.Translate, "번역기"),
        BottomNavItem(Screen.MyPage, Icons.Default.PersonPin, "마이페이지")
    )

    NavigationBar(
        containerColor = androidx.compose.ui.graphics.Color.White
    ) {
        // 리스트를 순회하며 아이템을 동적으로 생성합니다.
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                //  하드코딩된 문자열 대신 item의 route 사용
                selected = currentRoute == item.screen.route,
                onClick = { onNavigate(item.screen.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 12.sp,
                        fontWeight = if (currentRoute == item.screen.route) FontWeight.SemiBold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MyPageColors.BottomBarActive,
                    selectedTextColor = MyPageColors.BottomBarActive,
                    unselectedIconColor = MyPageColors.BottomBarInactive,
                    unselectedTextColor = MyPageColors.BottomBarInactive
                )
            )
        }
    }
}