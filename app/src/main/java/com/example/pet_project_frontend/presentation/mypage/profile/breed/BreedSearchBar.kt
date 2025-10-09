@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pet_project_frontend.presentation.mypage.profile.breed

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun BreedSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }

    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        placeholder = if (!focused && value.isBlank()) {
            { Text(text = "견종 검색", style = placeholderStyle(), color = Gray600) }
        } else null,
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "검색 아이콘",
                tint = Gray600,
                modifier = Modifier.semantics { contentDescription = "검색" }
            )
        },
        trailingIcon = {
            if (focused || value.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier
                        .semantics { contentDescription = "입력 내용 지우기" }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = Gray500,
                        modifier = Modifier.size(16.dp) // ⬅ 16dp 고정
                    )
                }
            }
        },
        textStyle = inputTextStyle(),
        modifier = modifier
            .heightIn(min = 56.dp) // ⬅ 잘림 방지 (46dp 대신)
            .onFocusChanged { focused = it.isFocused },
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Gray100,
            unfocusedContainerColor = Gray100,
            disabledContainerColor = Gray100,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = Gray700,
            focusedLeadingIconColor = Gray600,
            unfocusedLeadingIconColor = Gray600,
            focusedTrailingIconColor = Gray500,
            unfocusedTrailingIconColor = Gray500,
            focusedPlaceholderColor = Gray600,
            unfocusedPlaceholderColor = Gray600
        )
    )
}


/* ===== 텍스트 스타일  ===== */
@Composable
private fun inputTextStyle(): TextStyle =
    MaterialTheme.typography.bodyLarge.copy(
        fontSize = 17.sp,
        fontWeight = FontWeight.Medium, // 500
        lineHeight = 17.sp,
        letterSpacing = (-0.17).sp
    )

@Composable
private fun placeholderStyle(): TextStyle =
    MaterialTheme.typography.bodyLarge.copy(
        fontSize = 17.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 17.sp,
        letterSpacing = (-0.17).sp
    )

/* ===== 색상 상수 ===== */
private val Gray700 = Color(0xFF4E5968)
private val Gray600 = Color(0xFF6B7684)
private val Gray500 = Color(0xFF8B95A1)
private val Gray100 = Color(0xFFF2F4F6)
private val TextPrimary = Color(0xFF191F28)

