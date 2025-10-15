## Non-negotiables

- Android Architecture Compliance (must follow): Adhere end-to-end to Google's official Android Architecture recommendations: https://developer.android.com/topic/architecture/recommendations?hl=en. Enforce separation of concerns across layered architecture (UI, Domain, Data), MVVM with unidirectional data flow and a single source of truth, repositories as data boundaries, coroutines/Flow between layers, lifecycle-aware state collection (repeatOnLifecycle / collectAsStateWithLifecycle), and Hilt DI. ViewModels expose a single immutable uiState (StateFlow) created with stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial). Avoid Android lifecycle references or AndroidViewModel in ViewModels; never place business logic in Activities/Fragments/Composables.
- Professional, production-grade code: All code must be production quality—clear, typed APIs; minimal but meaningful tests when changing public behavior; precise naming; comments only when non-obvious; remove dead code; no TODOs without an owner/issue; follow Kotlin/Android style guides.
- Root-cause fixes only: When resolving errors, identify and fix the true root cause—do not add workarounds, silence exceptions, blanket-disable lint rules, or add arbitrary delays. Document the cause and resolution in commits and add a regression test when feasible.

## HappyDog2 – AI Agent Working Guide

Essential Knowledge for Immediate Productivity

**Core Architecture (Android Official)**
- Modules: UI/Domain/Data inside `app/`, shared: `core:common`, `core:navigation`, `core:designsystem` (see `settings.gradle.kts`)
- MVVM + UDF/SSoT: ViewModel owns a single `StateFlow<UiState>`; repositories persist via DataStore. No mutable state to UI
- Only expose `suspend`/`Flow` from Data/Domain. Launch coroutines in ViewModel with `viewModelScope`

**Essential Error Handling Pattern - AppResult + SafeApi**
This project uses a standardized error handling system that ALL network and repository operations must follow:

```kotlin
// Repository pattern - wrap ALL API calls with SafeApi
override suspend fun getMyPetProfile(): AppResult<Pet> {
    return SafeApi.response { petApi.getMyPetProfile() }
        .let { res ->
            when (res) {
                is AppResult.Success -> AppResult.Success(PetMapper.mapToDomainModel(res.data))
                is AppResult.Error -> res
                is AppResult.Exception -> res
            }
        }
}

// ViewModel pattern - convert AppResult to UI state
when (val result = repository.getMyPetProfile()) {
    is AppResult.Success -> _uiState.value = UiState.Success(result.data)
    is AppResult.Error -> _uiState.value = UiState.Error(result.message ?: "Unknown error")
    is AppResult.Exception -> _uiState.value = UiState.Error(result.throwable.message ?: "Network error")
}
```

**StateFlow Pattern (Critical)**
- Use `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial)` for ALL ViewModel state
- Example from `MainViewModel.kt`: `val isLoggedIn: StateFlow<Boolean> = userRepository.getAccessToken().map { !it.isNullOrBlank() }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = false)`
- Collect in UI with `collectAsStateWithLifecycle()` - see `MainActivity.kt`

**Navigation System**
- Sealed class `Screen` routes in `core/navigation/NavigationRoutes.kt`
- Start route logic in `MainActivity.kt`: `!isLoggedIn -> Screen.Login.route`, `!hasPet -> Screen.PetRegistration.route`, else `Screen.PetCare.route`
- Navigation host in `PetCareNavigation.kt` with lifecycle-aware state observation

**DI & Repository Binding (Hilt)**
- All repositories bind in `RepositoryModule.kt` using `@Binds @Singleton abstract fun bind*Repository(*RepositoryImpl): *Repository`
- API interfaces provided in `NetworkModule.kt`
- Pattern: 1) Define in `domain/repository/*`, 2) Implement in `data/repository/*Impl.kt`, 3) Bind in `RepositoryModule`

**Build & Environment (Windows)**
- Build: `gradlew.bat assembleDebug`; Tests: `gradlew.bat test`
- Backend URL: `local.properties` → `BuildConfig.API_BASE_URL` (emulator: `http://10.0.2.2:5000/`)
- Required keys: `API_BASE_URL`, `GOOGLE_SERVER_CLIENT_ID`, `KAKAO_NATIVE_APP_KEY`

**New Feature Implementation Checklist**
1) API: Add in `data/remote/api/*Api.kt` + provide in `NetworkModule.kt` if needed
2) Repository: Add contract in `domain/repository/*`, implement `data/repository/*Impl`, bind in `RepositoryModule`
3) ViewModel: Inject repository, expose single `uiState` via `stateIn(WhileSubscribed(5000))`
4) UI: Collect with `collectAsStateWithLifecycle()`, navigate via `Screen.*.route`

**Critical Don'ts**
- Don't start coroutines in repository/domain layers - only expose `suspend`/`Flow`
- Don't leak mutable state to UI - always use StateFlow/immutable data
- Don't log secrets/tokens in production code
- Don't bypass SafeApi wrapper for network calls

**Reference Files**
- Error handling: `core/common/SafeApi.kt`, `core/common/AppResult.kt`
- State examples: `MainViewModel.kt`, `MainActivity.kt`
- DI patterns: `core/di/RepositoryModule.kt`
- Navigation: `core/navigation/NavigationRoutes.kt`, `core/navigation/PetCareNavigation.kt`