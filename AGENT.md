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
- UI collects via `collectAsStateWithLifecycle()` to respect lifecycle. Example: `MainActivity.setContent` collects login and `selectedPetId` to branch navigation.
- Navigation uses sealed `Screen` routes. Deep-link constants live in `core/navigation/DeepLinks.kt`.
- Recommended UI state model: `sealed interface UiState { object Loading; data class Success<T>(val data: T); data class Error(val code: String?, val message: String?) }`.

4) Navigation Rules
- Start at `Screen.PetCare`; if `selectedPetId` is empty at runtime, redirect to registration. Files: `MainActivity.kt`, `PetCareNavigation.kt`, `NavigationRoutes.kt`.
- Bottom navigation: `core/navigation/BottomNavigation.kt` highlights based on `Screen.*.route`.
- Deep links: Register `Routes.PetCare.Dashboard` path `pet_care/dashboard?petId={?}&date={?}&tab={?}` in `NavHost` using `DeepLinks.PET_CARE_DASHBOARD`.

5) Backend Integration Protocol
- Retrofit + OkHttp are standard. API interfaces in `app/data/remote/api/*Api.kt`; provided by `NetworkModule.kt`.
- Auth tokens: `AuthInterceptor` injects Bearer; on 401, `TokenAuthenticator` refreshes using a `Retry-After-Refresh` loop guard header.
- Protected APIs: On 403/404, `ProtectedErrorInterceptor` clears global `selected_pet_id` in DataStore; UI reacts by collecting flows.
- Ownership: `TokenManager` (DataStore) stores tokens/selected pet; expose read/write as Flow/suspend.
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