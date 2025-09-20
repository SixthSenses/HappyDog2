# Endpoint Index (HappyDog API2)

이 문서는 `docs/openapi_pretty.json`(= Postman: HappyDog API2) 기준으로 주요 엔드포인트를 빠르게 찾기 위한 인덱스입니다. 상세 스키마/예시는 스펙 파일을 참고하세요.

## health
- GET /health — 시스템 헬스 체크

## api/auth
- GET /api/auth/google/authorize — Google OAuth 인증 URL 생성(redirect=1 지원)
- GET /api/auth/social — OAuth 리디렉션 콜백(code)
- POST /api/auth/social — 소셜 로그인 및 신규 가입 처리
- POST /api/auth/token/refresh — Refresh 토큰으로 Access 재발급
- POST /api/auth/logout — 로그아웃(토큰 블랙리스트)

## api/uploads
- POST /api/uploads/url — 업로드용 Pre-signed URL 발급
- POST /api/uploads/finalize-cartoon — 만화 이미지 공개 전환

## api/pet-care
- POST /api/pet-care/:pet_id/records — 펫케어 기록 생성
- GET /api/pet-care/:pet_id/records/daily — 특정 날짜 기록 목록 조회
- PATCH /api/pet-care/:pet_id/records/:log_id — 기록 수정
- DELETE /api/pet-care/:pet_id/records/:log_id — 기록 삭제

## api/notifications
- GET /api/notifications — 알림 목록 조회
- GET /api/notifications/unread-count — 미확인 알림 수 조회
- POST /api/notifications/:notification_id/ack — 알림 확인 처리

---
Note
- 대부분의 /api/* 엔드포인트는 Bearer 인증 필요
- POST/PATCH 등 쓰기 요청은 X-Idempotency-Key 헤더를 반드시 포함
