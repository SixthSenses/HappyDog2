# Handoff: 로그인 → 반려동물 최초등록 → 펫케어

본 문서는 현재 백엔드 스펙 변경 없이, 프론트엔드에서 구현·연동 완료/정렬된 범위와 테스트 체크리스트를 요약합니다. 팀원들이 이후 기능(피드/좋아요/카툰 등)을 이어받아 개발할 때 참고하도록 작성했습니다.

## 범위 요약
- 인증/토큰
  - 소셜 로그인 POST `/api/auth/social` → 200 {access_token, refresh_token, ...}
  - 토큰 재발급 POST `/api/auth/token/refresh` (Authorization: Bearer <refresh_token>) → 200 {access_token}
  - 로그아웃 POST `/api/auth/logout` → 200 {message}
  - Authorization 자동 부착: AuthInterceptor 사용
  - 401 처리: TokenAuthenticator가 리프레시 토큰으로 access_token만 재발급 후 원요청 1회 재시도
  - 에러 인터셉터: ErrorInterceptor는 pass-through, ProtectedErrorInterceptor는 보호 API 403/404 수신 시 selected_pet_id 초기화

- 반려동물(싱글 펫)
  - 등록 POST `/api/pets` → 성공 시 응답의 `pet_id`를 `selected_pet_id`로 저장
  - 프로필 GET `/api/pets/{pet_id}` (소유자 전용)
  - 분석: POST `/api/pets/{pet_id}/nose-print`, POST `/api/pets/{pet_id}/eye-analysis`
  - 공개 프로필은 현재 jwt_required. 익명 접근 필요 시 서버 결정 필요(현 상태로는 로그인 사용자만 접근)

- 펫케어
  - 목록 GET `/api/pet-care/{pet_id}/records`
    - 쿼리: `date` 또는 `start_date/end_date`, `record_types`, `grouped`, `limit`(1~100), `cursor`, `sort=timestamp_asc|timestamp_desc`(기본 timestamp_desc)
  - 생성 POST `/api/pet-care/{pet_id}/records`
  - 타입별 GET `/api/pet-care/{pet_id}/records/{record_type}`
  - Authorization은 인터셉터로 자동 부착(메서드 시그니처에서 헤더 제거됨)

## 코드 포인터
- 인터셉터/인증
  - Auth 헤더 부착: `data/remote/interceptors/AuthInterceptor.kt`
  - 401 리프레시: `data/remote/authenticator/TokenAuthenticator.kt`
  - 에러 pass-through: `data/remote/interceptors/ErrorInterceptor.kt`
  - 403/404 시 selected_pet_id 초기화: `data/remote/interceptors/ProtectedErrorInterceptor.kt`
  - OkHttp/Retrofit DI: `core/di/NetworkModule.kt`
  - 토큰/선택 펫 보관: `data/local/preferences/TokenManager.kt` (keys: access_token, refresh_token, selected_pet_id)

- 반려동물
  - API: `data/remote/api/PetApi.kt`
  - Repo: `data/repository/PetRepositoryImpl.kt`
  - 최초 등록 VM: `presentation/petregistration/PetRegistrationViewModel.kt` (성공 시 saveSelectedPetId)
  - 마이페이지 VM: `presentation/mypage/main/MyPageViewModel.kt` (403/404 수신 시 clearSelectedPetId, 인터셉터 레벨에서도 수행)

- 펫케어
  - API(경로형 pet_id로 정리): `data/remote/api/PetCareApi.kt`
  - Repo: `data/repository/PetCareRepositoryImpl.kt`

## 에러/예외 처리 정책
- 401: TokenAuthenticator로 갱신 및 재시도(무한루프 방지 헤더 포함)
- 403/404: 예외 던지지 않음 → SafeApiCall/호출부에서 표준 Error 처리. 보호 API에서 받으면 selected_pet_id 즉시 초기화(인터셉터 수행)
- 기타 4xx/5xx: pass-through → SafeApiCall에서 `NetworkResult.Error(code, ErrorResponse?)`

## 환경/빌드
- BASE URL: `BuildConfig.API_BASE_URL` (에뮬레이터는 `http://10.0.2.2:5000/`)
- 타임아웃: 일반 30s (업로드 PUT 30s 권장)
- 빌드: `gradlew.bat assembleDebug`

## QA 스모크 체크리스트
1) 로그인/토큰
- 소셜 로그인 성공 시 access/refresh 저장 확인
- 보호 API 호출 중 401 발생 → 자동 리프레시 후 재시도 1회 성공
- 로그아웃 시 두 토큰 삭제 및 보호 API 401/403 응답 확인

2) 최초 반려동물 등록
- 등록 성공 후 `selected_pet_id` 저장 확인
- 앱 재시작 후 마이페이지에서 해당 펫 프로필 로드됨

3) 보호 API 403/404
- 무효 pet_id로 `/api/pets/{pet_id}` 호출 시 403/404 → 인터셉터가 `selected_pet_id`를 즉시 초기화하는지 확인
- UI에서는 등록 유도 메시지 노출

4) 펫케어
- GET `/api/pet-care/{pet_id}/records` 기본 목록 정상 수신(기본 sort=timestamp_desc)
- date 또는 기간 쿼리 동작 확인
- POST `/api/pet-care/{pet_id}/records` 생성 후 목록에 반영

## 이후(타 팀원이 이어받을 부분)
- 멍스타그램(피드/상세/작성/좋아요) 구현(jwt_optional 고려)
- 카툰 작업 생성/폴링/취소 + finalize-cartoon 연동
- 공개 프로필 익명 접근 필요 시 서버 optional 인증 전환 여부 결정 및 반영

> 본 범위는 2025-09-03 기준 빌드 성공 및 런타임 기본 연동 준비 상태입니다. 추가 확인이 필요하면 이 문서에 체크박스를 늘려가며 진행해 주세요.
