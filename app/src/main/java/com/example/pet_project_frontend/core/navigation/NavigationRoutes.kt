package com.example.pet_project_frontend.core.navigation

import android.net.Uri

object NavigationRoutes {
	const val PET_CARE = "petcare"
	const val MAP = "map"
	const val COMMUNITY = "community"
	const val TRANSLATOR = "translator"
	const val MY_PAGE = "mypage"

	const val GENDER_SELECT = "mypage/profile/gender?initial={initial}"

	const val NOTIFICATION = "mypage/settings/notification"


	// 이미 있는 object/파일 안에 아래 2줄 추가
	const val BIRTH_EDIT = "birth_edit"
	fun birthEdit(initialBirth: String) = "$BIRTH_EDIT?initialBirth=$initialBirth"


	fun genderSelect(initial: String? = null): String =
		if (initial.isNullOrBlank()) "mypage/profile/gender" else "mypage/profile/gender?initial=$initial"

	const val NAME_EDIT = "mypage/profile/name?initialName={initialName}"

	fun nameEdit(initialName: String): String =
		"mypage/profile/name?initialName=${Uri.encode(initialName)}"
}