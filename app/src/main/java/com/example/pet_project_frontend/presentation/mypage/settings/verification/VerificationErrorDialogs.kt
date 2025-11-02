 package com.example.pet_project_frontend.presentation.mypage.settings.verification

import androidx.compose.runtime.Composable
import com.example.pet_project_frontend.presentation.mypage.common.CommonAlertDialog

@Composable
fun VerificationAlreadyVerifiedDialog(onConfirm: () -> Unit) {
    CommonAlertDialog(
        onDismissRequest = onConfirm,
        onConfirmation = onConfirm,
        title = "이미 신원 인증을 했어요",
        text = "멍스타그램에서 인증 배지를 볼 수 있어요"
    )
}

@Composable
fun VerificationUnknownErrorDialog(onConfirm: () -> Unit) {
    CommonAlertDialog(
        onDismissRequest = onConfirm,
        onConfirmation = onConfirm,
        title = "알 수 없는 오류가 발생했어요.",
        text = "잠시 후 다시 시도해주세요."
    )
}

@Composable
fun VerificationDuplicateNoseDialog(onConfirm: () -> Unit) {
    CommonAlertDialog(
        onDismissRequest = onConfirm,
        onConfirmation = onConfirm,
        title = "이미 등록된 비문이에요",
        text = "등록되지 않은 반려견만 등록할 수 있어요"
    )
}

@Composable
fun VerificationDetectionFailedDialog(onConfirm: () -> Unit) {
    CommonAlertDialog(
        onDismissRequest = onConfirm,
        onConfirmation = onConfirm,
        title = "비문을 찾을 수 없어요",
        text = "이미지가 반려견의 비문을 포함하고 있는지 확인해 주세요"
    )
}
