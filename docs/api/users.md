# Users 도메인 명세

사용자 정보 및 요약/환경설정 관리.

기본 정보
- 베이스 URL: `/api`
- 태그: `users`
- 인증: 대부분 Bearer 필요.

Endpoints
1) GET `/api/users/me`
- 설명: 현재 로그인 사용자 정보 조회
- 응답: 200(JSON `UserMeResponseSchema`)

2) GET `/api/users/me/summary`
- 설명: 사용자/반려동물/펫케어 설정 통합 요약
- 응답: 200(JSON `UserSummaryResponseSchema`)

3) GET `/api/users/{user_id}/public` [DEPRECATED]
- 설명: 공개 프로필(사용 중단 예정) → `GET /api/pets/profile?view=social&user_id={user_id}`로 마이그레이션 권장
- 응답: 200(JSON `UserPublicResponseSchema`)

4) GET `/api/users/me/notification-preferences`
- 설명: 알림 설정 조회
- 응답: 200(JSON `NotificationPreferencesResponseSchema`)

5) PUT `/api/users/me/notification-preferences`
- 설명: 알림 설정 수정
- 요청: `NotificationPreferencesSchema`
- 응답: 200(JSON `NotificationPreferencesResponseSchema`)
- 멱등성: 일반적으로 PUT은 멱등이지만 서버 저장 로직은 중복 방지를 위해 `X-Idempotency-Key` 사용 불필요(명세 미적용)

6) PUT `/api/users/me/fcm-token`
- 설명: FCM 토큰 업데이트
- 요청: `FCMTokenSchema`
- 응답: 200(JSON `FCMTokenUpdateResponseSchema`)

에러 규약
- 공통 오류 스키마 적용(UNAUTHORIZED, NOT_FOUND, INTERNAL_ERROR 등)
