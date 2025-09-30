# Health 도메인 명세

시스템/프로세서 헬스체크.

기본 정보
- 베이스 URL: `/api`
- 태그: `health`
- 인증: 불필요

Endpoints
1) GET `/health`
- 설명: 전체 시스템 상태 반환
- 응답: 200(JSON `HealthStatusResponseSchema`)
