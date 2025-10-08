## Core Principles

- **MVVM + UDF (Unidirectional Data Flow)**: ViewModels expose single immutable `StateFlow<UiState>` via `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial)`. UI collects with `collectAsStateWithLifecycle()`. No mutable state exposed to UI. See `MainViewModel.isLoggedIn`, `EyeHealthViewModel.uiState` for reference patterns.
- **Layered architecture**: UI → Domain → Data. Repositories return `AppResult<T>` (Success/Error/Exception wrapper from `core:common`). Only `suspend`/`Flow` from Data/Domain—never start coroutines in repositories. Launch in ViewModels with `viewModelScope`.
- **Network error handling**: All API calls wrapped with `SafeApi.response {}` or `.responseUnit {}` (for 204 No Content). Maps HTTP errors to `AppResult.Error` with validation details. Backend validation errors parsed from `{message, errors: {field: msg}}` JSON.
- **File corruption warning**: Compose UI files (especially `*Screen.kt`) corrupt easily with duplicate imports or missing function names. When build fails with these symptoms, **delete and recreate** the entire file—don't attempt incremental edits.

## Project Structure

**Module layout** (`settings.gradle.kts`):
- `app/`: Main application (UI/Domain/Data layers)
- `core:common`: Shared types (`AppResult`, `SafeApi`, `ValidationError`)
- `core:navigation`: Route definitions (`Screen` sealed class)
- `core:designsystem`: Theme and UI components (`AppTheme`)

**State management pattern** (see `MainViewModel`, `EyeHealthViewModel`, `CommunityViewModel`):
```kotlin
// ViewModel: Single StateFlow<UiState> exposed via stateIn
val uiState: StateFlow<MyUiState> = combine(
    _loading, _data, _error
) { loading, data, error ->
    MyUiState(loading, data, error)
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial)

// UI: Collect with lifecycle awareness
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

**Repository pattern** (see `PetRepositoryImpl`, `CommunityRepositoryImpl`):
```kotlin
override suspend fun getData(): AppResult<MyData> {
    return SafeApi.response { myApi.getData() }
        .onSuccess { /* optional post-processing */ }
}
```

## Navigation Architecture

**Route definitions** (`core/navigation/NavigationRoutes.kt`):
```kotlin
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object PetCare : Screen("petcare")
    object ImageViewer : Screen("image_viewer/{imageUrl}") {
        fun createRoute(imageUrl: String) = "image_viewer/$imageUrl"
    }
}
```

**Conditional root navigation** (`MainActivity`):
- Observes `MainViewModel.isLoggedIn` (token exists?) and `hasPet` (pet profile exists?)
- Start destination logic: `!isLoggedIn → Login`, `!hasPet → PetRegistration`, else `PetCare`
- Runtime redirect via `LaunchedEffect(hasPet, currentRoute, isLoggedIn)` if pet deleted during session

**Bottom navigation** (`MainActivity.bottomBarRoutes`):
Five main tabs: `PetCare`, `Map`, `Community`, `Translator`, `MyPage`

**Deep links** (`AndroidManifest.xml` + `DeepLinks.kt`):
- Scheme: `app://` (e.g., `app://pet-care/dashboard?petId=123`)
- Register `<intent-filter>` in manifest with `<data android:scheme="app" />`

## Network Layer

**Stack**: Retrofit + OkHttp + Gson (see `NetworkModule.kt`)

**Authentication flow**:
1. `AuthInterceptor` auto-injects `Authorization: Bearer <token>` from `TokenManager` (AndroidX DataStore)
2. On 401, `TokenAuthenticator` calls `POST /api/auth/token/refresh` with `Bearer <refresh_token>` + empty body `{}`
3. Uses mutex to prevent concurrent refresh; adds `Retry-After-Refresh: true` to prevent retry loops
4. Tokens stored in DataStore (never client-side prefs for other data)

**Idempotency** (`IdempotencyInterceptor`):
- Auto-adds `X-Idempotency-Key: <UUIDv4>` to POST/PUT/PATCH + whitelisted DELETE (e.g., `/api/cartoon-jobs/{id}`)
- Only for requests to `apiBaseHost` (excludes Firebase URLs)
- Backend echoes key + adds `Idempotent-Replay: true` if replaying cached response
- See `docs/DEEPLINKS_AND_IDEMPOTENCY.md` for conflict handling (409 on key reuse with different body)

**Error mapping** (`SafeApi` in `core:common`):
```kotlin
// Success path: unwrap body
SafeApi.response { api.getData() } // → AppResult.Success(T)

// Error path: parse validation details
// Backend: {message: "...", errors: {field: "msg"}}
// Maps to: AppResult.Error(code, message, ValidationError(fields, generalMessage))
```

**DTO mapping**:
- DTOs in `data/remote/dto/*` with Gson `@SerializedName`
- Map to domain models (`domain/model/*`) via `data/mapper/*`
- Never expose DTOs to ViewModels/UI

## Dependency Injection (Hilt)

**Repository binding pattern** (`RepositoryModule.kt`):
1. Define interface in `domain/repository/*Repository.kt`
2. Implement in `data/repository/*RepositoryImpl.kt` (inject APIs, DataStore)
3. Bind with `@Binds @Singleton` in `RepositoryModule`

**Hilt modules** (`app/src/main/.../core/di/`):
- `NetworkModule`: Retrofit + OkHttpClient (with Auth/Idempotency/Error interceptors + TokenAuthenticator). Provides `@Named("API_BASE_HOST")` for idempotency filtering
- `RepositoryModule`: Binds all repositories (Auth, Pet, User, PetCare, Breed, Map, Post, EyeHealth, Community, CartoonJob)
- `DataStoreModule`: AndroidX DataStore for token persistence
- `AppModule`: BuildConfig values (`GOOGLE_SERVER_CLIENT_ID`, `KAKAO_NATIVE_APP_KEY`, `API_BASE_URL`)

## Configuration & Environment

**Required `local.properties` keys**:
```properties
API_BASE_URL="http://10.0.2.2:5000/"  # Emulator (or http://<IPv4>:5000/ for device)
GOOGLE_SERVER_CLIENT_ID=<client_id>
KAKAO_NATIVE_APP_KEY=<app_key>
```

**Token storage**: Only tokens persisted via `TokenManager` (AndroidX DataStore). Single-pet policy—no client-side pet selection state

**Firebase setup**: Add `app/google-services.json` + register SHA1 via `.\gradlew.bat signingReport`

## Build & Development (Windows)

**Commands** (use `.\` prefix in PowerShell):
```powershell
.\gradlew.bat assembleDebug    # Build debug APK
.\gradlew.bat test             # Unit tests
.\gradlew.bat connectedAndroidTest  # Instrumented tests
.\gradlew.bat signingReport    # Get SHA1 for Firebase
```

**VS Code task**: "Build Debug (Gradle)" available in task runner

## Critical Patterns & Anti-Patterns

**ViewModel state management**:
- Single source of truth: `StateFlow<UiState>` via `stateIn(WhileSubscribed(5000))`
- Reference: `MainViewModel.isLoggedIn`, `EyeHealthViewModel.uiState`, `CommunityViewModel.uiState`
- Never expose `MutableStateFlow` or mutable collections to UI

**UI state collection**:
```kotlin
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
// See: MainActivity (isLoggedIn/hasPet branching), PetCareHomeScreen, MyPageScreen
```

**Repository error handling**:
```kotlin
// ❌ Don't throw exceptions
throw IllegalStateException("Pet not found")

// ✅ Return AppResult
return SafeApi.response { petApi.getMyPetProfile() }
    .onSuccess { /* optional mapping */ }
```

**Coroutine boundaries**:
- Repositories/Domain: Only expose `suspend`/`Flow`—never call `runBlocking()` or start coroutines
- ViewModels: Launch with `viewModelScope.launch { }`
- See: All `*RepositoryImpl.kt`, `*ViewModel.kt` files

**New feature checklist**:
1. API interface → `data/remote/api/*Api.kt` + provide in `NetworkModule.kt`
2. Repository contract → `domain/repository/*` + implement `data/repository/*Impl` + bind in `RepositoryModule`
3. ViewModel → inject repository, expose `uiState: StateFlow<UiState>` via `stateIn()`
4. Screen → collect with `collectAsStateWithLifecycle()`, send events to ViewModel

**Critical anti-patterns**:
- ❌ Coroutines in repositories/domain
- ❌ Mutable state exposed to UI (`MutableStateFlow` public)
- ❌ Logging secrets/tokens (use BuildConfig/manifestPlaceholders)
- ❌ Editing corrupted Compose files (delete + recreate instead)
- ❌ Direct DTO exposure to ViewModels/UI (always map to domain models)

**Reference files**:
- State patterns: `MainViewModel.kt`, `EyeHealthViewModel.kt`, `CommunityViewModel.kt`
- Navigation: `NavigationRoutes.kt`, `PetCareNavigation.kt`, `MainActivity.kt`
- Network: `NetworkModule.kt`, `SafeApi.kt`, `TokenAuthenticator.kt`, `IdempotencyInterceptor.kt`
- Policies: `docs/DEEPLINKS_AND_IDEMPOTENCY.md`