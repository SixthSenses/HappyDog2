-----

# 프로젝트 지침서: AI 기반 반려견 케어 앱 ‘행복하개’

## 1\. 프로젝트 개요

  - **프로젝트명**: **AI 기반 통합 반려견 케어 앱 ‘행복하개’** (졸업 과제)
  - **핵심 기능**: 펫케어, 애견지도, 행동번역기, 멍스타그램, 마이페이지
  - **현재 목표**: 마이페이지 기능 완성
      - **화면 구성**: 총 8개 화면 (메인 1 + 프로필 편집 4 + 설정 2 + 회원탈퇴 1)
      - **주요 내용**: 프로필(이름·생년월일·성별·견종), 앱 설정(알림/신원인증), 법적 정보, 회원탈퇴

## 2\. 기술 스택

  - **UI**: Android Studio (Jetpack Compose)
  - **DI**: Hilt
  - **로컬 저장소**: DataStore
  - **아키텍처**: 도메인 레이어 / 리포지토리 패턴 (MVVM)
  - **백엔드**: Flask연동 예정 (현재는 Repository/UseCase 추상화로 분리됨)

## 3\. 코드베이스 상세

> **[중요]** 아래의 모든 경로, 심볼, 함수명은 실제 파일 및 시그니처입니다. **이 전제를 절대 변경하지 마세요.** 누락된 연결을 보완하거나 `TODO` 주석이 달린 부분을 완성하는 데 집중해 주세요.

### 3.1. 네비게이션

  - **라우트 정의**: `core/navigation/NavigationRoutes.kt`
    ```kotlin
    sealed class Screen {
        object MyPage : Screen("mypage")
        object EditPetName : Screen("mypage/edit/name?initialName={initialName}&petId={petId}") {
            fun createRoute(initialName: String, petId: String?): String // ...
        }
        object EditBirthDate : Screen("mypage/edit/birth?initialBirth={initialBirth}")
        object SelectGender : Screen("mypage/edit/gender?initialGender={initialGender}")
        object SelectBreed : Screen("mypage/edit/breed?initialBreed={initialBreed}")
        object NotificationSettings : Screen("mypage/settings/notification")
        // 신원 인증 관련 라우트들 (VerificationIntro/Guide/Loading/Success ...)
        object Withdraw : Screen("mypage/withdraw")
    }
    ```
  - **네비게이션 그래프**: `core/navigation/PetCareNavigation.kt`
      - `NavHost`에 `MyPage` 및 각 편집·설정 화면들이 연결되어 있음.
      - 이름 편집 라우트는 `initialName`, `petId` 인자를 사용.
      - 대부분의 편집 화면은 `navController.popBackStack()` 호출 후 `MyPageViewModel`의 갱신 함수를 호출하는 패턴을 사용.

### 3.2. 마이페이지 메인

  - **메인 화면**: `presentation/mypage/main/MyPageScreen.kt`
      - `DisposableEffect(ON_RESUME)`에서 `viewModel.loadUserData()`를 호출.
      - 각 항목 클릭 시 호출되는 콜백 람다 함수들이 정의되어 있음 (e.g., `onNameClick: (String?, String) -> Unit`).
  - **뷰모델**: `presentation/mypage/main/MyPageViewModel.kt`
      - **UI 상태**:
        ```kotlin
        data class MyPageUiState(
            val petId: String?,
            val petName: String,
            val breed: String,
            val age: String,
            val birthDate: String,
            val gender: String,
            val profileImageUrl: String?,
            val isLoading: Boolean,
            val error: String?
        )
        ```
      - **주요 함수**: `updatePetName`, `updateBirthDate`, `updateGender`, `updateBreed`, `loadUserData`
  - **컴포넌트**:
      - `.../components/ProfileHeader.kt`: 프로필 이미지
      - `.../components/ProfileInfoSection.kt`: 이름, 생일, 성별, 견종 정보
      - `.../components/SettingsSection.kt`: 알림, 신원 인증
      - `.../components/LegalSection.kt`, `.../WithdrawalSection.kt`, `.../AppVersionSection.kt`
  - **프로필 이미지 관련**:
      - `presentation/mypage/main/MediaPickerScreen.kt`
      - `presentation/mypage/main/PhotoCropScreen.kt`

### 3.3. 프로필 편집 화면 (4개)

  - **이름**: `.../profile/name/NameEditScreen.kt` & `.../NameEditViewModel.kt`
      - `hiltViewModel()`을 통해 `NameEditViewModel` 주입.
      - `parentEntry`를 통해 부모의 `MyPageViewModel`을 참조.
      - 저장 시 `myPageViewModel.updatePetName()` 호출 후 `popBackStack()`.
      - `SavedStateHandle`로 `petId`, `initialName` 수신.
      - 저장 로직: 유효성 검사 → `PetRepository.updatePetProfile()` 호출.
  - **생년월일**: `.../profile/birthdate/BirthDateScreen.kt` & `.../BirthDateViewModel.kt`
  - **성별**: `.../profile/gender/GenderSelectScreen.kt` & `.../GenderSelectViewModel.kt`
  - **견종**: `.../profile/breed/BreedSelectScreen.kt` & `.../BreedSelectViewModel.kt`

### 3.4. 설정 화면 (2개)

  - **알림 설정**: `.../settings/notification/NotificationSettingsScreen.kt` & `.../ViewModel.kt`
      - `UserPreferences` (DataStore)를 사용하여 `weeklyReport`, `likeEnabled`, `commentEnabled` 토글 상태를 영속화.
  - **신원 인증 (비문 등록)**: `.../settings/verification/IdentityVerificationScreen.kt` & `.../ViewModel.kt`
      - 가이드, 로딩, 결과 등 다수의 관련 화면 존재.
      - `domain/usecase/pet/RegisterNosePrintUseCase`를 사용.

### 3.5. 회원탈퇴 화면 (1개)

  - **파일**: `.../withdrawal/WithdrawalScreen.kt` & `.../WithdrawalViewModel.kt`
  - **로직**: `WithdrawAccountUseCase` 수행 성공 시, `popUpTo`를 포함한 네비게이션 로직을 통해 로그인 화면으로 이동.

### 3.6. 데이터 및 도메인 레이어

  - **리포지토리**: `domain/repository/PetRepository.kt` (`updatePetProfile` 등 추상 메소드 정의)
  - **로컬 저장소**: `data/local/preferences/UserPreferences.kt` (DataStore 키 정의)

## 4\. 구현 지침 (규칙)

### 4.1. 금지 사항

  - **절대 변경 금지**: 아래 항목들은 코드 전체의 일관성을 위해 수정하지 마세요.
      - `Screen` 라우트 경로 문자열
      - 네비게이션 파라미터 키: `initialName`, `petId`, `initialBirth`, `initialGender`, `initialBreed`

### 4.2. 권장 사항

  - **작은 단위 커밋**: 기능 단위로 잘게 나누어 커밋해 주세요.
  - **주석 작성**: 함수나 파일 헤더에 변경 이유를 한 줄이라도 코멘트로 남겨주세요.

1) Architectural Blueprint (Android Official Guidance)
- Layers: UI (app/presentation) – Domain (app/domain) – Data (app/data). Shared modules: `core:common`, `core:navigation`, `core:designsystem`.
- UDF/SSoT: ViewModel owns state and exposes a single `StateFlow<UiState>` down to UI. Repositories (with DataStore) own data persistence.
- Official pattern: MVVM with unidirectional data flow. ViewModel receives events, updates state, and never leaks mutable state to UI.
- Data/Domain layers expose only `suspend` functions and `Flow`. Coroutine creation and scope management live in ViewModel (AndroidX lifecycle scope). Cross-cutting concerns (auth, protected errors) belong to OkHttp Interceptors/Authenticator.

2) Code Style & Conventions
- Follow Kotlin official code style. One top-level class per file, 4-space indent, prefer max line length ~100.
- Naming: packages lower_snakecase, classes/interfaces PascalCase, functions/vars camelCase, constants UPPER_SNAKE_CASE.
- Layered models separation: Network DTO ↔ Domain ↔ UI models. Public surfaces should be immutable data types.
- Avoid hardcoding: Externalize strings, numbers, URLs, and feature flags. Use `BuildConfig` and `manifestPlaceholders` for secrets/keys. Keep routes, deep links, and query names in `core:navigation`.

3) UI & State (Jetpack Compose Best Practices)
- ViewModel exposes a single `StateFlow`. Convert `Flow` to `StateFlow` using `stateIn(scope, SharingStarted.WhileSubscribed(5000), initial)`.
- UI collects via `collectAsStateWithLifecycle()` to respect lifecycle. Example: `MainActivity.setContent`는 `isLoggedIn`과 `hasPet`를 수집해 네비게이션을 분기합니다.
- Navigation uses sealed `Screen` routes. Deep-link constants live in `core/navigation/DeepLinks.kt`.
- Recommended UI state model: `sealed interface UiState { object Loading; data class Success<T>(val data: T); data class Error(val code: String?, val message: String?) }`.

4) Navigation Rules
- Start at `Screen.PetCare`; 단일 펫 정책에 따라 `hasPet == false`이면 등록 화면으로 리다이렉트합니다. Files: `MainActivity.kt`, `PetCareNavigation.kt`, `NavigationRoutes.kt`.
- Bottom navigation: `core/navigation/BottomNavigation.kt` highlights based on `Screen.*.route`.
- Deep links: Register `Routes.PetCare.Dashboard` path `pet_care/dashboard?petId={?}&date={?}&tab={?}` in `NavHost` using `DeepLinks.PET_CARE_DASHBOARD`.

5) Backend Integration Protocol
- Retrofit + OkHttp are standard. API interfaces in `app/data/remote/api/*Api.kt`; provided by `NetworkModule.kt`.
- Auth tokens: `AuthInterceptor` injects Bearer; on 401, `TokenAuthenticator` refreshes using a `Retry-After-Refresh` loop guard header. 재발급 요청 본문은 빈 JSON `{}` 입니다.
- Protected APIs: 403/404는 도메인 에러로 전달하며 선택 펫 상태는 존재하지 않습니다(단일 펫 정책).
- Ownership: `TokenManager` (DataStore)에는 Access/Refresh 토큰만 저장합니다.
- Repository bindings: Bound in `RepositoryModule.kt` to domain interfaces under `app/domain/repository/*`.
- OpenAPI source of truth: Match DTOs and endpoints to `docs/openapi_pretty.json`. Keep domain docs under `docs/api/*` in sync.
- DTO policy: Keep Gson with `@SerializedName`. Create DTOs under `app/data/remote/dto`, and mappers under `app/data/mapper`.
- Repository returns domain models and domain errors; ViewModel maps to `UiState` and exposes retry intents.

6) DI & Environment Setup
- Hilt DI modules in `core/di/*.kt` (NetworkModule, RepositoryModule, DataStoreModule, AppModule).
- API Base URL: `BuildConfig.API_BASE_URL` (debug reads from `local.properties` `API_BASE_URL`). Kakao key via `BuildConfig.KAKAO_NATIVE_APP_KEY`, provided as `@Named("NATIVE_APP_KEY")` in `AppModule`.
- Required local keys: `API_BASE_URL`, `GOOGLE_SERVER_CLIENT_ID`, `KAKAO_NATIVE_APP_KEY` in `local.properties`. Kakao `manifestPlaceholders` are injected automatically.

7) Coroutines & Threading (Official Rules)
- Do not launch coroutines in Repository/Data/Domain layers; only expose `suspend`/`Flow`. Launch in ViewModel using `viewModelScope` or lifecycle scopes.
- Use structured concurrency. Cancel scopes on `onCleared`. Avoid `GlobalScope` and ad-hoc `SupervisorJob` unless designing a top-level supervisor.
- Dispatchers: Default to `Dispatchers.IO` for blocking I/O via `withContext`. Keep CPU-bound work on `Dispatchers.Default`. Switch context explicitly in use-cases if needed.
- Error handling: Prefer `Result`/domain error model mapping; avoid swallowing exceptions. Convert to `UiState.Error` with user-safe messages and error codes.

8) Hardcoding Avoidance
- Strings: move to `strings.xml` or Kotlin constants in `core:common` when not localized. For Compose, prefer `stringResource()`.
- URLs and routes: centralize in `BuildConfig` (base) and `core:navigation` (paths). Do not inline URL strings in code.
- Feature flags: use `BuildConfig` fields or a simple in-app flag provider. Never hardcode secrets/tokens; inject via `BuildConfig`/`manifestPlaceholders`.

9) Development Workflow
- Build: VS Code task "Build Debug (Gradle)" or `gradlew.bat assembleDebug`.
- Tests: `gradlew.bat test` or device tests via `gradlew.bat connectedAndroidTest`.
- Firebase: Register SHA1 then place `app/google-services.json`.
- Backend local: See README Conda/`run.py`. Emulator base URL: `http://10.0.2.2:5000/`.

10) Deep Links (docs/DEEPLINKS_AND_IDEMPOTENCY.md)
- Server does not provide app-scheme redirect endpoints; app interprets web links for internal routing.
- OAuth: Start via `GET /api/auth/google/authorize` (supports `redirect=1`). Server handles callback at `GET /api/auth/social?code=...`. App exchanges tokens with `POST /api/auth/social`.
- In-app: Register deep links in `NavHost`, then guard with login/permissions before navigation.

11) Idempotency (docs/DEEPLINKS_AND_IDEMPOTENCY.md)
- Always attach `X-Idempotency-Key` (UUIDv4 recommended) to POST/PUT/PATCH. Reuse the same key on retries.
- If same key + different body → expect 409 `IDEMPOTENCY_KEY_REUSED_DIFFERENT_BODY`; surface guidance to user to review body/key.
- Treat `Idempotent-Replay: true` or `Idempotency-Replay: true` headers as successful replay; skip duplicate UI and show normal success.
- Implementation: Prefer an OkHttp Interceptor that injects the header on write methods only, or have Repository generate and pass keys for specific actions.

12) Long-Horizon Error Strategy (4 Phases)
- Phase 1 – Identify & Replicate: Reproduce with minimal case; log steps/inputs/state (no secrets). Add failing tests when possible.
- Phase 2 – Root Cause: Trace logs, state, and data flows to find the true cause. Avoid band-aid fixes.
- Phase 3 – Sustainable Fix: Align with UDF/SSoT/MVVM, consider resilience and extensibility. Add proper retries, backoff, and state recovery.
- Phase 4 – Implement & Verify: Add unit/integration tests; improve logging/telemetry; document in `docs/` and update API docs if needed.

13) Gradle Changes: Tracking & Safety
- All `build.gradle.kts` and `settings.gradle.kts` edits must be traceable. Summarize intent and impact in PR description and changelog.
- When changing dependencies or plugin versions: 1) explain reason, 2) note transitive impacts (Compose BOM, Kotlin, AGP), 3) include build scan or test run results.
- Add or modify `BuildConfig` fields via `buildTypes`/`flavorDimensions` explicitly; document new env vars in README and `local.properties` example.
- Do not mix unrelated refactors with Gradle changes. Keep patches minimal and focused.

14) Acceptance Checklist for New Features
1) API: add methods in `data/remote/api` → add `@Provides` if needed in `NetworkModule`.
2) Repository: add contract in `domain/repository` → implement `data/repository/*Impl` → bind in `RepositoryModule`.
3) ViewModel: inject repository, expose single `uiState` via `stateIn(WhileSubscribed(5000))`.
4) UI: collect via `collectAsStateWithLifecycle()`; send events to ViewModel; navigate using `Screen.*.route`.

References
- Deep links & idempotency: `docs/DEEPLINKS_AND_IDEMPOTENCY.md`.
- OpenAPI single source of truth: `docs/openapi_pretty.json`. Per-domain docs: `docs/api/*`.
- Navigation helpers: `core/navigation/DeepLinks.kt` and related routes.
- Networking/auth: `core/di/NetworkModule.kt`, `.../interceptors/*.kt`, `.../authenticator/TokenAuthenticator.kt`.
- State examples: `MainViewModel.kt` (WhileSubscribed(5000)), `MainActivity.kt` (lifecycle-aware collection/branching).

Cautions
- Never log secrets/tokens. Inject keys only via `BuildConfig`/`manifestPlaceholders`.
- Do not create coroutines in data/domain; expose only `suspend`/`Flow`.

This document reflects the actual code and will evolve with changes.