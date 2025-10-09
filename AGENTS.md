# agents.md — HappyDog2 작업 에이전트 가이드

본 문서는 Codex(또는 유사 멀티에이전트) 환경에서 역할/담당/진입 조건을 정의합니다.  
**공통 원칙**: 새 파일 생성 금지, 기존 파일만 수정. 모든 주석/설명은 한국어.

## 1) Navigation Agent (네비게이션 담당)
- **목표**: 마이페이지 하위 화면(이름/성별/견종/생년월일/알림/탈퇴) 라우트 연결 및 콜백 바인딩.
- **수정 범위**
  - `core/navigation/NavigationRoutes.kt` : `sealed class Screen` 내 하위 route 문자열 **추가만**.
  - `core/navigation/PetCareNavigation.kt` : `NavHost`에 `composable` **추가만**.
  - `presentation/mypage/main/MyPageScreen.kt` : onNameClick, onGenderClick 등 **콜백에 navigate 연결**.
- **완료 기준**
  - 마이페이지 → 각 편집/설정 화면으로 진입/복귀 가능.
  - 뒤로가기 동작 정상, 바텀바 표시 정책 유지.

## 2) Profile Edit Agent (프로필 편집 담당)
- **목표**: 이름/성별/견종/생년월일 편집 화면의 초기값 주입/저장 임시 구현.
- **수정 범위**
  - 각 ViewModel(`NameEditViewModel` 등)에서 `SavedStateHandle`로 네비 인자 읽어 초기 UI 상태 반영.
  - 저장 시: **임시로 MyPage 표시값만 갱신**(popBackStack 후 MyPageViewModel refresh).
- **완료 기준**
  - 진입 시 입력값/선택값이 기존 정보로 채워짐.
  - 저장 시 MyPage에 즉시 반영.

## 3) Notification Agent (알림 설정 담당)
- **목표**: 알림 설정을 DataStore에 **임시 지속화**.
- **수정 범위**
  - `data/local/preferences/UserPreferences.kt` : `isPushEnabled` 등 키/Flow 구현.
  - `presentation/mypage/settings/notification/NotificationSettingsViewModel.kt` :
    - 초기 로드 시 DataStore 값 반영
    - 토글 시 DataStore 업데이트
- **완료 기준**
  - 앱 재시작 후에도 알림 설정 값 유지.
  - 추후 API 준비 시 `UpdateNotificationUseCase`로 대체 가능한 구조.

## 4) Withdrawal Agent (탈퇴 담당)
- **목표**: 탈퇴 동작 연결.
- **수정 범위**
  - `presentation/mypage/withdrawal/WithdrawalViewModel.kt` :
    - (임시) `UserRepository.deleteUser()` 호출 → 성공 시 토큰 삭제 → 로그인 화면 navigate.
- **완료 기준**
  - 탈퇴 성공 후 세션 클리어 및 로그인 화면으로 이동.

## 5) Theming Agent (테마/폰트 담당)
- **목표**: Pretendard 전역 적용(선택).
- **수정 범위**
  - `core/designsystem/Theme.kt` 의 `AppTypography`를 Pretendard로 교체 또는 `MaterialTheme.typography` 커스텀.
- **완료 기준**
  - 전체 화면에서 Pretendard가 적용됨(시각적 확인).

## 6) Build-Fix Agent (빌드/런타임 오류 즉시 대응)
- **자주 발생 이슈**
  - `LocalContext` import 누락 → `import androidx.compose.ui.platform.LocalContext`
  - `BottomNavBar` 미정의 → 실제 구현은 `BottomNavigation`
  - Kakao 키 정의 이중 따옴표 오류 제거
- **완료 기준**
  - Clean Build 성공, 런타임 크래시 없음.

## 공통 체크리스트
- [ ] 새 파일 생성 금지 원칙 준수
- [ ] 변경 파일 상단에 한국어 주석으로 변경 의도/배경 기입
- [ ] 네비 인자/상태는 이름 충돌 없이 일관된 키 사용(예: `initialName`, `selectedGender`)
- [ ] 임시 구현부에는 `// TODO 서버 연동 시 치환` 주석 남기기

Project-Specific AI Working Guide (HappyDog2)

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