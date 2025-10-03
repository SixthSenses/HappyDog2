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
