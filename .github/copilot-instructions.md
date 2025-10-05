## Non-negotiables

- **Android Architecture Compliance (mandatory)**: Follow Google's official Android Architecture guide end-to-end: https://developer.android.com/topic/architecture/recommendations. Strict layering (UI → Domain → Data), MVVM with UDF + SSoT, repositories as data boundaries, coroutines/Flow between layers, lifecycle-aware collection (repeatOnLifecycle/collectAsStateWithLifecycle), and Hilt DI throughout. ViewModels expose a single immutable `StateFlow<UiState>` created with `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial)`. Never reference Android lifecycle in ViewModels; never place business logic in Activities/Fragments/Composables.
- **Production-grade code only**: Clear, typed APIs; meaningful tests when changing public behavior; precise naming; comments only when non-obvious; remove dead code; no TODOs without owner/issue; follow Kotlin/Android style guides.
- **Root-cause fixes only**: Identify and fix true root causes—no workarounds, no silenced exceptions, no blanket lint disables, no arbitrary delays. Document cause/resolution in commits; add regression tests when feasible.
- **File corruption awareness**: `EyeHealthScreen.kt` and similar UI files corrupt easily. When build errors mention "Function declaration must have a name" or duplicate imports, **delete and recreate** the entire file rather than attempting edits.

## HappyDog2 – AI Agent Working Guide

**Architecture and State Management**
- **Module structure**: UI/Domain/Data layers in `app/`; shared modules: `core:common` (AppResult, SafeApi), `core:navigation` (Screen routes), `core:designsystem` (AppTheme). See `settings.gradle.kts`.
- **MVVM + UDF/SSoT pattern**: ViewModels own a single `StateFlow<UiState>` (immutable). Repositories return `AppResult<T>` (Success/Error/Exception wrapper). No mutable state exposed to UI. Example: `EyeHealthViewModel` combines multiple flows into single `uiState` via `stateIn(WhileSubscribed(5000))`.
- **Coroutine boundaries**: Only `suspend`/`Flow` from Data/Domain layers. Launch coroutines in ViewModels with `viewModelScope`. Repositories never call `runBlocking` or start coroutines.
- **State collection**: UI uses `collectAsStateWithLifecycle()` (see `MyPageScreen`, `PetCareHomeScreen`). ViewModels use `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial)` for derived state (see `MainViewModel.isLoggedIn`, `EyeHealthViewModel.uiState`).

**Navigation System**
- **Route definitions**: Sealed class `Screen` in `app/src/.../core/navigation/NavigationRoutes.kt`. Each route has a `route: String` property. Examples: `Screen.Login`, `Screen.PetCare`, `Screen.EyeHealth`, `Screen.BreedGuide`. Routes with params: `Screen.BreedDetail.createRoute(breedName)`.
- **Conditional navigation**: `MainActivity` observes `MainViewModel.isLoggedIn`/`hasPet` to determine start destination: `!isLoggedIn → Screen.Login.route`, `!hasPet → Screen.PetRegistration.route`, else `Screen.PetCare.route`. Uses `LaunchedEffect(hasPet, currentRoute, isLoggedIn)` to redirect at runtime if pet is deleted.
- **Deep links**: Register in `AndroidManifest.xml` with `<intent-filter>` for `app://` scheme. Constants in `core/navigation/DeepLinks.kt` and `Routes.kt`. Example: `PetCare.Dashboard = "pet_care/dashboard"`.
- **Bottom navigation**: Five main tabs: `Screen.PetCare`, `Screen.Map`, `Screen.Community`, `Screen.Translator`, `Screen.MyPage`. See `BottomNavigation` composable and `bottomBarRoutes` list in `MainActivity`.

**Networking, Auth, and Idempotency**
- **Stack**: Retrofit + OkHttp + Gson. API interfaces in `app/data/remote/api/*Api.kt`; provided via Hilt in `NetworkModule.kt`. All APIs return `Response<T>` wrapped by `SafeApi.response {}` → `AppResult<T>`.
- **Bearer auth**: `AuthInterceptor` (in `data/remote/interceptors/`) auto-injects `Authorization: Bearer <token>` from `TokenManager`. `TokenAuthenticator` (in `data/remote/authenticator/`) handles 401 by calling `POST /api/auth/token/refresh` with `Bearer <refresh_token>` and empty JSON `{}`. Uses mutex to prevent concurrent refreshes. Sets `Retry-After-Refresh: true` header to prevent retry loops.
- **Idempotency**: `IdempotencyInterceptor` auto-adds `X-Idempotency-Key` (UUIDv4) to POST/PUT/PATCH and whitelisted DELETE requests targeting `apiBaseHost` (not external URLs like Firebase). Backend responds with `Idempotent-Replay: true` if replaying cached response. See `docs/DEEPLINKS_AND_IDEMPOTENCY.md` for full spec.
- **DTOs and mapping**: DTOs in `app/data/remote/dto/request` and `.../dto/response` use Gson `@SerializedName`. Map to domain models (in `app/domain/model`) via mapper functions in `app/data/mapper/`. Never expose DTOs to ViewModels.
- **Error handling**: Use `SafeApi.response {}` or `.body {}` to wrap all API calls. Maps HTTP errors to `AppResult.Error(code, message, validation)`. Backend validation errors parsed from JSON `{message, errors: {field: msg}}` into `ValidationError`. Always align with `docs/openapi_pretty.json` and `docs/api/*.md`.

**DI and Repository Binding (Hilt)**
- **Repository binding**: All repositories bound in `RepositoryModule.kt` via `@Binds @Singleton`. Pattern: 1) Define interface in `app/domain/repository/*`, 2) Implement in `app/data/repository/*Impl.kt` (inject APIs, DataStore, etc.), 3) Bind in `RepositoryModule.kt`.
- **Hilt modules**: Located in `app/src/main/java/com/example/pet_project_frontend/core/di/`:
  - `NetworkModule`: Provides Retrofit, OkHttpClient (with interceptors/authenticator), API interfaces. Uses `@Named("API_BASE_HOST")` for idempotency checks.
  - `RepositoryModule`: Binds all repository implementations (Auth, Pet, User, PetCare, Breed, Map, Post, EyeHealth).
  - `DataStoreModule`: Provides AndroidX DataStore for tokens/preferences.
  - `AppModule`: Provides BuildConfig-based strings (`GOOGLE_SERVER_CLIENT_ID`, `KAKAO_NATIVE_APP_KEY`, `API_BASE_URL`).

**Storage and Environment Setup**
- Tokens only: `TokenManager` via AndroidX DataStore. No other client-side selections persisted (single-pet policy)
- Backend URL: `local.properties` → `BuildConfig.API_BASE_URL`. Emulator: `http://10.0.2.2:5000/`. Real device: use `ipconfig` IPv4 + `:5000`
- Required keys in `local.properties`: `API_BASE_URL`, `GOOGLE_SERVER_CLIENT_ID`, `KAKAO_NATIVE_APP_KEY`
- Firebase: Add `app/google-services.json` and register SHA1 (`gradlew.bat signingReport`)

**Build and Development (Windows)**
- Build: `.\gradlew.bat assembleDebug` (use `.\` prefix in PowerShell)
- Tests: `gradlew.bat test`; Device tests: `gradlew.bat connectedAndroidTest`
- Backend setup: Use appropriate conda env from `pet_project_backend/envs/` (Windows CPU/CUDA, macOS CPU)

**Critical Component Patterns**
- ViewModel state: Single `StateFlow<UiState>` exposed via `stateIn(WhileSubscribed(5000))` - see `MainViewModel.kt`
- UI collection: `collectAsStateWithLifecycle()` - see `MainActivity.kt` branching on `isLoggedIn`/`hasPet`
- Repository implementation: Return domain models, never throw exceptions to UI layer
- Error handling: Map to domain errors, convert to `UiState.Error` in ViewModel with user-safe messages

**New Feature Implementation Checklist**
1) API: Add interface in `data/remote/api/*Api.kt` + provide in `NetworkModule.kt` if needed
2) Repository: Add contract in `domain/repository/*`, implement `data/repository/*Impl`, bind in `RepositoryModule`
3) ViewModel: Inject repository, expose single `uiState` via `stateIn(WhileSubscribed(5000))`, handle events
4) UI: Collect with `collectAsStateWithLifecycle()`, send events to ViewModel, navigate via `Screen.*.route`

**Critical Don'ts**
- Don't start coroutines in repository/domain layers - only expose `suspend`/`Flow`
- Don't leak mutable state to UI - always use StateFlow/immutable data
- Don't log secrets/tokens. Inject keys only via `BuildConfig`/`manifestPlaceholders`
- Don't attempt to edit corrupted files with duplicate imports - delete and recreate instead

**Key Reference Files**
- Navigation: `core/navigation/NavigationRoutes.kt`, `PetCareNavigation.kt`, `MainActivity.kt`
- State patterns: `MainViewModel.kt` (WhileSubscribed), `MainActivity.kt` (lifecycle collection)
- Network setup: `NetworkModule.kt`, `data/remote/interceptors/`, `TokenAuthenticator.kt`
- Policies: `docs/DEEPLINKS_AND_IDEMPOTENCY.md`