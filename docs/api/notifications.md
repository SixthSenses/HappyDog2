# Notifications 도메인 명세

알림 목록/확인/미확인 카운트.

기본 정보
- 베이스 URL: `/api`
- 태그: `notifications`
- 인증: Bearer 필요
- 멱등성: `POST /api/notifications/{notification_id}/ack`에 적용

스키마
- `NotificationListRequestSchema`, `NotificationListResponseSchema`, `NotificationAckRequestSchema`, `NotificationAckResponseSchema`, `NotificationUnreadCountResponseSchema`, `NotificationItemSchema`

Endpoints
1) GET `/api/notifications`
- 설명: 알림 목록 조회(페이지네이션 파라미터는 스키마 참고)

2) POST `/api/notifications/{notification_id}/ack`
- 설명: 알림 확인 처리
- 요청: `NotificationAckRequestSchema`
- 응답: 200(JSON `NotificationAckResponseSchema`)
- 멱등성: 적용

3) GET `/api/notifications/unread-count`
- 설명: 미확인 알림 수 조회
- 응답: 200(JSON `NotificationUnreadCountResponseSchema`)
