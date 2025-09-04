package com.example.pet_project_frontend.presentation.common

import com.example.pet_project_frontend.core.common.ValidationError

// UI 폼에서 ValidationError를 간편히 소비하기 위한 헬퍼
fun ValidationError?.messageFor(field: String): String? = this?.fields?.get(field)

fun ValidationError?.general(): String? = this?.generalMessage

// Pet 업데이트 폼 전용 에러 묶음
data class PetUpdateFormErrors(
    val furColor: String? = null,
    val healthConcerns: String? = null,
    val weight: String? = null,
    val bcs: String? = null,
    val other: String? = null,
)

fun ValidationError?.toPetUpdateFormErrors(): PetUpdateFormErrors = PetUpdateFormErrors(
    furColor = messageFor("furColor"),
    healthConcerns = messageFor("healthConcerns"),
    weight = messageFor("weight"),
    bcs = messageFor("bcs"),
    other = general(),
)

// 프로필 이미지 업데이트 전용 에러 묶음
// 서버 필드명이 "image" | "file" | "file_path" 등으로 다를 수 있어 우선순위로 조회
data class ImageUpdateErrors(
    val image: String? = null,
    val other: String? = null,
)

fun ValidationError?.toImageUpdateErrors(): ImageUpdateErrors = ImageUpdateErrors(
    image = messageFor("image") ?: messageFor("file") ?: messageFor("file_path"),
    other = general(),
)
