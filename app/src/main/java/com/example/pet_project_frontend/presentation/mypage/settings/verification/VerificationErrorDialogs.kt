package com.example.pet_project_frontend.presentation.mypage.settings.verification

import androidx.compose.runtime.Composable
import com.example.pet_project_frontend.core.components.ErrorDialog

@Composable
fun VerificationAlreadyVerifiedDialog(onConfirm: () -> Unit) {
    ErrorDialog(
        title = "이미 신원 인증을 했어요",
        message = "멍스타그램에서 인증 배지를 볼 수 있어요",
        confirmText = "확인",
        onConfirm = onConfirm
    )
}

@Composable
fun VerificationUnknownErrorDialog(onConfirm: () -> Unit) {
    ErrorDialog(
        title = "알 수 없는 오류가 발생했어요.",
        message = "잠시 후 다시 시도해주세요.",
        confirmText = "확인",
        onConfirm = onConfirm
    )
}

@Composable
fun VerificationDuplicateNoseDialog(onConfirm: () -> Unit) {
    ErrorDialog(
        title = "이미 등록된 비문이에요",
        message = "등록되지 않은 반려견만 등록할 수 있어요",
        confirmText = "확인",
        onConfirm = onConfirm
    )
}

@Composable
fun VerificationDetectionFailedDialog(onConfirm: () -> Unit) {
    ErrorDialog(
        title = "비문을 찾을 수 없어요",
        message = "이미지가 반려견의 비문을 포함하고 있는지 확인해 주세요",
        confirmText = "확인",
        onConfirm = onConfirm
    )
}
