# HappyDog Android App - AI Coding Agent Instructions

<<<<<<< HEAD
- **Android Architecture Compliance (must follow)**: Adhere end-to-end to Google's official Android Architecture recommendations: https://developer.android.com/topic/architecture/recommendations?hl=en. Enforce separation of concerns across layered architecture (UI, Domain, Data), MVVM with unidirectional data flow and a single source of truth, repositories as data boundaries, coroutines/Flow between layers, lifecycle-aware state collection (repeatOnLifecycle / collectAsStateWithLifecycle), and Hilt DI. ViewModels expose a single immutable uiState (StateFlow) created with stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial). Avoid Android lifecycle references or AndroidViewModel in ViewModels; never place business logic in Activities/Fragments/Composables.
- **Professional, production-grade code**: All code must be production quality—clear, typed APIs; minimal but meaningful tests when changing public behavior; precise naming; comments only when non-obvious; remove dead code; no TODOs without an owner/issue; follow Kotlin/Android style guides.
- **Root-cause fixes only**: When resolving errors, identify and fix the true root cause—do not add workarounds, silence exceptions, blanket-disable lint rules, or add arbitrary delays. Document the cause and resolution in commits and add a regression test when feasible.
- **File corruption awareness**: EyeHealthScreen.kt and similar UI files are prone to import/content corruption. When build errors mention "Function declaration must have a name" or duplicate imports, delete corrupted files completely and recreate them cleanly rather than attempting to edit.

## HappyDog2 – AI Agent Working Guide

**Architecture and State Management**
- Modules: UI/Domain/Data inside `app/`, shared: `core:common`, `core:navigation`, `core:designsystem` (see `settings.gradle.kts`)
- MVVM + UDF/SSoT: ViewModel owns a single `StateFlow<UiState>`; repositories persist via DataStore. No mutable state to UI.
- Only expose `suspend`/`Flow` from Data/Domain. Launch coroutines in ViewModel with `viewModelScope`.
- State collection pattern: Use `collectAsStateWithLifecycle()` in UI, `stateIn(scope, SharingStarted.WhileSubscribed(5000), initial)` in ViewModels

**Critical StateFlow Pattern**
- ALL ViewModels must expose state via `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue)`
- Example from `MainViewModel.kt`: `val isLoggedIn: StateFlow<Boolean> = userRepository.getAccessToken().map { !it.isNullOrBlank() }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = false)`
- UI collection: `val uiState by viewModel.uiState.collectAsStateWithLifecycle()` in all Composables
- Never expose MutableStateFlow to UI; always use StateFlow/immutable data classes

**Navigation System**
- Sealed class routes in `core/navigation/NavigationRoutes.kt`: `Screen.Login`, `Screen.PetCare`, `Screen.EyeHealth`, etc.
- Navigation logic in `MainActivity.kt`: `!isLoggedIn -> Screen.Login.route`, `!hasPet -> Screen.PetRegistration.route`, else `Screen.PetCare.route`
- Single-pet policy enforced in navigation: `LaunchedEffect(hasPet, currentRoute, isLoggedIn)` redirects to registration if no pet exists
- Bottom nav routes: `Screen.PetCare`, `Screen.Map`, `Screen.Community`, `Screen.Translator`, `Screen.MyPage`
- Deep links: Register constants in `core/navigation/DeepLinks.kt` and routes in `core/navigation/Routes.kt`

**Networking, Auth, and Idempotency**
- Retrofit + OkHttp; APIs under `app/data/remote/api/*Api.kt`; provided via Hilt in `NetworkModule.kt`
- Bearer auth: `AuthInterceptor` injects token; `TokenAuthenticator` refreshes on 401 using empty JSON `{}` body
- **Idempotency (Critical)**: ALL write requests (POST/PUT/PATCH) auto-inject `X-Idempotency-Key` (UUIDv4) via `IdempotencyInterceptor`. Treat response headers `Idempotent-Replay: true` or `Idempotency-Replay: true` as success replay. Apply to DELETE for specific endpoints (cartoon jobs). See `docs/DEEPLINKS_AND_IDEMPOTENCY.md`
- DTOs use Gson `@SerializedName` (`app/data/remote/dto/**`); map to domain in `app/data/mapper/**`
- Align with `docs/openapi_pretty.json` and `docs/api/*` documentation

**DI and Repository Binding (Hilt)**
- All repositories bind in `RepositoryModule.kt` using `@Binds @Singleton abstract fun bind*Repository(*RepositoryImpl): *Repository`
- Pattern: 1) Define in `domain/repository/*`, 2) Implement in `data/repository/*Impl.kt`, 3) Bind in `RepositoryModule`
- Hilt modules in `app/src/main/java/com/example/pet_project_frontend/core/di/` (NetworkModule, RepositoryModule, DataStoreModule, AppModule)

**Storage and Environment Setup**
- Tokens only: `TokenManager` via AndroidX DataStore. No other client-side selections persisted (single-pet policy)
- Backend URL: `local.properties` → `BuildConfig.API_BASE_URL`. Emulator: `http://10.0.2.2:5000/`. Real device: use `ipconfig` IPv4 + `:5000`
- Required keys in `local.properties`: `API_BASE_URL`, `GOOGLE_SERVER_CLIENT_ID`, `KAKAO_NATIVE_APP_KEY`
- Firebase: Add `app/google-services.json` and register SHA1 (`gradlew.bat signingReport`)

**Build and Development (Windows)**
- Build: `.\gradlew.bat assembleDebug` (use `.\` prefix in PowerShell)
- Tests: `gradlew.bat test`; Device tests: `gradlew.bat connectedAndroidTest`
- Backend setup: Use appropriate conda env from `pet_project_backend/envs/` (Windows CPU/CUDA, macOS CPU)

**Navigation Architecture Patterns**
- `PetCareNavigation.kt` defines all screen routes in `NavHost` with proper argument handling
- Two-tier routing: Management screens (`*Management.route`) → Record screens (`*Record.route`)
- Example: `FeedManagement` → `FeedRecord`, `ActivityManagement` → `ActivityRecord`
- Deep link support via `navDeepLink` with URI patterns from `DeepLinks.kt`
- Screen arguments use `navArgument` with proper types and nullability

**Critical Component Patterns**
- ViewModel state: Single `StateFlow<UiState>` exposed via `stateIn(WhileSubscribed(5000))` - see `MainViewModel.kt`
- UI collection: `collectAsStateWithLifecycle()` - see `MainActivity.kt` branching on `isLoggedIn`/`hasPet`
- Repository implementation: Return domain models wrapped in `AppResult<T>`, never throw exceptions to UI layer
- Error handling: Map to domain errors via `SafeApi.body { }`, convert to `UiState.Error` in ViewModel with user-safe messages
- UiState initial loading: ALWAYS start with `isLoading = false` to prevent infinite loading states; only set true during actual operations

**Critical UiState Anti-Pattern (MUST AVOID)**
❌ NEVER initialize UiState with `isLoading = true`:
```kotlin
data class UiState(val isLoading: Boolean = true)  // WRONG - causes infinite loading
```
✅ ALWAYS initialize with `isLoading = false`:
```kotlin
data class UiState(val isLoading: Boolean = false)  // CORRECT
```
- Set `isLoading = true` only when operation starts
- Immediately reset to `false` after operation completes (both success/error)
- See `WeightManagementViewModel.kt` for correct pattern

**New Feature Implementation Checklist**
1) **API Layer**:
   - Add interface in `data/remote/api/*Api.kt` with Retrofit annotations
   - Create DTOs in `data/remote/dto/request|response/` with `@SerializedName` annotations
   - Provide API in `NetworkModule.kt` if creating new service
   - Align with `docs/openapi_pretty.json` and `docs/api/*` specs
   
2) **Repository Layer**:
   - Define contract in `domain/repository/*Repository.kt` (interface)
   - Implement in `data/repository/*RepositoryImpl.kt` with `@Singleton`
   - Wrap all API calls in `SafeApi.body { }` to return `AppResult<T>`
   - Bind in `RepositoryModule.kt` using `@Binds @Singleton abstract fun`
   
3) **ViewModel Layer**:
   - Inject repository via constructor with `@Inject`
   - Define nested `data class UiState(val isLoading: Boolean = false, ...)`
   - Expose single `StateFlow<UiState>` via `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())`
   - Handle AppResult: `is Success -> _uiState.update { ... }`, `is Error -> _uiState.update { copy(error = ...) }`
   - Launch coroutines only in ViewModel, never in repository
   
4) **UI Layer**:
   - Collect state: `val uiState by viewModel.uiState.collectAsStateWithLifecycle()`
   - Handle loading/error/success states in UI
   - Send events to ViewModel methods, never manipulate state directly
   - Navigate via `Screen.*.route` or `Screen.*.createRoute(params)`
   
5) **Navigation Layer**:
   - Add sealed class entry in `NavigationRoutes.kt`: `object ScreenName : Screen("route?param={param}")`
   - Implement in `PetCareNavigation.kt`: `composable(route, arguments, deepLinks) { }`
   - Pass date parameters via navigation arguments with proper nullability
   - Use `hiltViewModel()` to get ViewModels in composable blocks

**Critical Don'ts**
- Don't start coroutines in repository/domain layers - only expose `suspend`/`Flow`
- Don't leak mutable state to UI - always use StateFlow/immutable data
- Don't log secrets/tokens. Inject keys only via `BuildConfig`/`manifestPlaceholders`
- Don't attempt to edit corrupted files with duplicate imports - delete and recreate instead
- Don't bypass idempotency for write operations - let `IdempotencyInterceptor` handle automatically
- Don't initialize UiState with `isLoading = true` - always start with `false`
- Don't throw exceptions from repository to ViewModel - wrap in `AppResult<T>`
- Don't call repository methods from composables - only from ViewModels

**Key Reference Files**
- Navigation: `core/navigation/NavigationRoutes.kt`, `PetCareNavigation.kt`, `MainActivity.kt`
- State patterns: `MainViewModel.kt` (WhileSubscribed), `MainActivity.kt` (lifecycle collection), `WeightManagementViewModel.kt` (correct loading pattern)
- Network setup: `NetworkModule.kt`, `data/remote/interceptors/`, `data/remote/authenticator/TokenAuthenticator.kt`
- Idempotency: `data/remote/interceptors/IdempotencyInterceptor.kt`, `docs/DEEPLINKS_AND_IDEMPOTENCY.md`
- Repository pattern: `data/repository/PetCareRepositoryImpl.kt` (SafeApi.body usage), `core/di/RepositoryModule.kt` (@Binds pattern)
- DTO examples: `data/remote/dto/response/WeightMonthlyAnalysisResponse.kt` (@SerializedName usage)
=======
## Project Overview
HappyDog is a native Android pet care app built with **Jetpack Compose** and **Clean Architecture**. The app provides pet management, social features (멍스타그램), maps, translation, and care tracking.

- **Language**: Kotlin 1.9.24
- **UI**: Jetpack Compose (Material3, Compose BOM 2024.06.00)
- **DI**: Hilt 2.50
- **Network**: Retrofit 2.9.0 + OkHttp 4.12.0
- **Backend API**: FastAPI (OpenAPI spec in `docs/openapi_pretty.json`)
- **Package**: `com.example.pet_project_frontend`

## Architecture & Module Structure

### Multi-Module Setup
```
app/              # Main application module (@HiltAndroidApp)
core/
  ├─ common/      # SafeApi, AppResult, shared utilities
  ├─ designsystem/# Compose theme & reusable UI components
  └─ navigation/  # Screen sealed class, navigation routes
```

### Clean Architecture Layers (in `app/`)
```
presentation/     # @HiltViewModel, Compose UI (@Composable screens)
domain/          # Repository interfaces, Use Cases, domain models
data/
  ├─ remote/     # Retrofit API interfaces, DTOs, interceptors
  ├─ repository/ # RepositoryImpl classes (@Singleton)
  ├─ local/      # DataStore (TokenManager), Room DAO (places)
  └─ mapper/     # DTO ↔ Domain model conversion
```

**Flow**: UI → ViewModel (@Inject UseCase) → UseCase (@Inject Repository) → RepositoryImpl (@Inject ApiService) → Retrofit

## Key Conventions & Patterns

### 1. Dependency Injection (Hilt)
- **Application**: `PetCareApplication` is `@HiltAndroidApp`
- **ViewModels**: `@HiltViewModel` + constructor `@Inject`
- **MainActivity**: `@AndroidEntryPoint`, injects `@Named("NATIVE_APP_KEY")` for Kakao Map SDK
- **Modules**:
  - `NetworkModule`: Provides Retrofit, OkHttpClient, API interfaces, interceptors (AuthInterceptor, IdempotencyInterceptor, TokenAuthenticator)
  - `RepositoryModule`: Binds repository interfaces to implementations
  - `DataModule`: Provides DataStore, TokenManager, Room DB

### 2. Network Layer
- **Base URL**: Configured via `BuildConfig.API_BASE_URL` (from `local.properties` → `API_BASE_URL`)
  - Debug default: `http://10.0.2.2:5000/` (Android emulator localhost)
  - Release: `https://api.happydog.com/`
- **Error Handling**: Use `SafeApi.response {}` or `SafeApi.body {}` (returns `AppResult<T>`)
  - `AppResult.Success<T>`, `AppResult.Error(code, message)`, `AppResult.Exception(throwable)`
- **Idempotency**: POST/PUT/PATCH requests auto-include `X-Idempotency-Key` via `IdempotencyInterceptor`
  - Docs: `docs/DEEPLINKS_AND_IDEMPOTENCY.md`
  - Backend returns `Idempotent-Replay: true` for replayed requests
- **Auth**: 
  - `AuthInterceptor` adds `Authorization: Bearer <token>` from `TokenManager`
  - `TokenAuthenticator` auto-refreshes token on 401
  - Two Retrofit instances: `@Named("AuthRetrofit")` (no auth) for login/refresh, default for protected endpoints

### 3. Navigation (Compose)
- **Routes**: Defined in `Screen` sealed class (`core/navigation/NavigationRoutes.kt`)
  - Main: `Login`, `PetRegistration`, `PetCare`, `Map`, `Community`, `Translator`, `MyPage`
- **NavHost**: `PetCareNavHost` in `MainActivity` (entry point based on auth + pet status)
- **ViewModel Scoping**: Share ViewModels across nav graph via `hiltViewModel(navController.getBackStackEntry(parentRoute))`
  - Example: `MyPageViewModel` shared across all MyPage sub-screens
  - Example: `IdentityVerificationViewModel` shared across verification flow
- **Bottom Nav**: Shows for main screens only (`PetCare`, `Map`, `Community`, `Translator`, `MyPage`)
- **Deep Links**: Registered in `AndroidManifest.xml` and nav graph (`app://pet-care/dashboard`)

### 4. State Management
- **UI State**: Each screen has a `UiState` data class (e.g., `MungStarUiState(isLoading, posts, error)`)
- **StateFlow**: ViewModels expose `StateFlow<UiState>`, UI collects via `collectAsStateWithLifecycle()`
- **Loading/Error**: Always handle `isLoading`, `error: String?` in UI state

### 5. API Integration
- **OpenAPI Spec**: `docs/openapi_pretty.json` (FastAPI backend)
- **DTOs**: Named with `Dto`, `Request`, `Response` suffixes (e.g., `PostCreateDto`, `PetProfileResponse`)
- **Mappers**: `toDomain()` extension functions convert DTOs to domain models
- **Repositories**: All network calls wrapped in `SafeApi.response { apiService.method() }`

### 6. Secrets & Config
- **`local.properties`** (gitignored):
  ```properties
  KAKAO_NATIVE_APP_KEY="your_key"
  GOOGLE_SERVER_CLIENT_ID=your_id
  API_BASE_URL="http://10.0.2.2:5000/"
  ```
- **BuildConfig**: Keys injected via `buildConfigField` in `app/build.gradle.kts`
- **Manifest Placeholders**: `${kakaoAppKey}` replaced at build time

## Common Development Workflows

### Build & Run
```powershell
# Build debug APK
.\gradlew.bat assembleDebug

# Install & run on emulator
.\gradlew.bat installDebug

# Run from IDE: Use "Build Debug (Gradle)" run configuration
```

### API Testing
- Check backend API docs: `docs/openapi_pretty.json`
- Test endpoints via Retrofit API interfaces in `data/remote/api/`
- Debug network calls: OkHttp logging enabled in debug builds (see Logcat with tag `OkHttp`)

### Adding New Features
1. **Domain**: Define model in `domain/model/`, repository interface in `domain/repository/`
2. **Data**: Create DTO in `data/remote/dto/`, API interface in `data/remote/api/`, implement repository
3. **DI**: Add `@Provides` for API in `NetworkModule`, `@Binds` for repository in `RepositoryModule`
4. **Presentation**: Create ViewModel + Screen, register route in `Screen` sealed class, add to `PetCareNavHost`

### Authentication Flow
1. User taps Google login → `LoginScreen` → `AuthRepository.socialLogin(authCode)`
2. Backend returns `access_token` + `refresh_token` → saved to DataStore via `TokenManager`
3. All protected API calls auto-include token via `AuthInterceptor`
4. On 401 → `TokenAuthenticator` auto-refreshes → retries original request
5. Logout → `AuthRepository.logout()` → clears tokens → navigate to `Login`

### Common Gotchas
- **KSP**: Hilt uses KSP (not kapt). Build errors? Clean project + rebuild.
- **Kakao Map**: Requires valid `KAKAO_NATIVE_APP_KEY` in `local.properties`. Check `MainActivity` logs for key hash.
- **Emulator Network**: Use `10.0.2.2` for Android emulator to access host's `localhost:5000`.
- **Idempotency**: Backend requires UUIDv4 in `X-Idempotency-Key` for write operations. Handled automatically by `IdempotencyInterceptor`.
- **ViewModel Scope**: For multi-screen flows, get parent entry's ViewModel: `hiltViewModel(navController.getBackStackEntry(parentRoute))`

## File Naming Patterns
- **Screens**: `*Screen.kt` (e.g., `LoginScreen.kt`, `MyPageScreen.kt`)
- **ViewModels**: `*ViewModel.kt` (e.g., `LoginViewModel.kt`)
- **Repositories**: `*RepositoryImpl.kt` implements `*Repository` interface
- **API Services**: `*Api.kt` (e.g., `PostApi.kt`, `PetApi.kt`)
- **DTOs**: `*Dto.kt`, `*Request.kt`, `*Response.kt`

## Testing Notes
- Test instrumentation: AndroidJUnit4 runner configured
- No unit tests currently implemented (opportunity for contribution!)

## External Dependencies
- **Firebase**: `google-services.json` in `app/` (Google Auth, potential FCM)
- **Kakao Map SDK**: Via maven `devrepo.kakao.com`, initialized in `MainActivity.onCreate()`
- **uCrop**: For circular profile image cropping
- **TensorFlow Lite**: Listed in `libs.versions.toml` (unused in current code?)

## Questions for Clarification
1. **Testing Strategy**: Should we add unit/integration tests? Any specific testing patterns preferred?
2. **Multi-Pet Support**: Code assumes 1 pet per user. Is multi-pet planned?
3. **TensorFlow Lite**: Models listed in `libs.versions.toml` but not used. Planned for dog nose-print verification?
4. **Localization**: UI strings hardcoded in Korean. Add string resources for i18n?
5. **Cartoon Feature**: `CartoonRepository` exists but UI/ViewModel incomplete. Priority?

---

**Key Files to Reference**:
- Architecture: `app/src/main/java/com/example/pet_project_frontend/MainActivity.kt`
- DI Setup: `core/di/NetworkModule.kt`, `core/di/RepositoryModule.kt`
- Navigation: `core/navigation/PetCareNavigation.kt`
- API Patterns: `data/repository/PostRepositoryImpl.kt`, `domain/repository/PostRepository.kt`
- Error Handling: `core/common/SafeApi.kt`, `data/remote/interceptors/`
>>>>>>> eaae9698402cb8dba9e895d30b908abbe0437abf
