package com.example.pet_project_frontend.core.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign

/**
 * 공통 에러 다이얼로그
 * - 제목, 메시지, 확인/취소(선택) 버튼 제공
 * - 메시지에 서버 오류 코드 등을 포함해 전달 가능
 */
@Composable
fun ErrorDialog(
	title: String = "오류가 발생했어요",
	message: String,
	confirmText: String = "확인",
	onConfirm: () -> Unit,
	onDismiss: (() -> Unit)? = null,
	dismissText: String = "취소"
) {
	AlertDialog(
		onDismissRequest = { onDismiss?.invoke() ?: onConfirm() },
		title = {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium
			)
		},
		text = {
			Text(
				text = message,
				style = MaterialTheme.typography.bodyMedium,
				textAlign = TextAlign.Start
			)
		},
		confirmButton = {
			TextButton(onClick = onConfirm) {
				Text(text = confirmText)
			}
		},
		dismissButton = if (onDismiss != null) {
			{
				TextButton(onClick = onDismiss) {
					Text(text = dismissText)
				}
			}
		} else null
	)
}


