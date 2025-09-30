# Uploads 도메인 명세

업로드 URL 발급과 만화 이미지 공개 전환.

기본 정보
- 베이스 URL: `/api`
- 태그: `uploads`
- 인증: Bearer 필요(정책에 따라 제한 가능)

스키마
- `UploadUrlRequestSchema`, `UploadUrlResponseSchema`, `FinalizeCartoonRequestSchema`, `FinalizeCartoonResponseSchema`

Endpoints
1) POST `/api/uploads/url`
- 설명: 범용 Pre-signed 업로드 URL 발급
- 요청: `UploadUrlRequestSchema`
- 응답: 200(JSON `UploadUrlResponseSchema`)

2) POST `/api/uploads/finalize-cartoon`
- 설명: 만화 이미지 공개 전환 및 URL 반환
- 요청: `FinalizeCartoonRequestSchema`
- 응답: 200(JSON `FinalizeCartoonResponseSchema`)
